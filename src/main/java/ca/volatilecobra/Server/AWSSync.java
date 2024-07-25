package ca.volatilecobra.Server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

public class AWSSync {
    final String RESET = "\u001B[0m";
    final String RED = "\u001B[31m";
    final String GREEN = "\u001B[32m";
    final String YELLOW = "\u001B[33m";
    public BasicAWSCredentials creds;
    public AmazonS3 s3Client;
    public String region;
    public AWSSync(String accessKey, String secretKey, String region){
        creds = new BasicAWSCredentials(accessKey, secretKey);
        this.region = region;
    }
    public boolean sync(String filepath, String keyName, String bucketName, BasicAWSCredentials awsCreds) {
        try {

            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.fromName(region)) // or your specific region
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();
            File file = new File(filepath);
            s3Client.putObject(new PutObjectRequest(bucketName, keyName, file));
            file.createNewFile();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean sync(String filePath, String keyName, String bucketName, BasicAWSCredentials awsCreds, int multipartUploadSize) {
        // Create a list to store the upload part responses.
        System.out.printf(GREEN + "INFO: Using Multipart upload, upload part size %s MB\n", multipartUploadSize);
        List<UploadPartResult> uploadPartResults = new ArrayList<>();
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region)) // or your specific region
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        // Check if heightmap already exists
        System.out.println(GREEN + "INFO: Checking if File already exists" + RESET);
        if(doesFileExist(bucketName, keyName)){
            System.out.println(GREEN + "INFO: File already exists, using that" + RESET);
            return true;
        }else{
            System.out.println(GREEN + "INFO: File does not exist, uploading..." + RESET);
        }
        // Initiate the multipart upload.
        System.out.println(GREEN +"INFO: Initating multipart upload" + RESET);
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName);
        InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
        System.out.println(GREEN + "INFO: Initated multipart upload, starting file stream" + RESET);

        System.out.println(GREEN + "INFO: Reading file " + filePath + RESET);
        File file = new File(filePath);
        System.out.println(GREEN + "INFO: File read" + RESET);
        long contentLength = file.length();
        long partSize = (long) multipartUploadSize * 1024 * 1024; // Set part size to 5 MB.

        long uploadedBytes = 0;
        int partNumber = 1;
        System.out.println(GREEN + "INFO: Starting upload" + RESET);
        try {
            // Upload the file parts.
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) partSize];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, bytesRead);
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey(keyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(partNumber++)
                        .withInputStream(byteArrayInputStream)
                        .withPartSize(bytesRead);

                // Upload part and add response to the list.
                UploadPartResult uploadPartResult = s3Client.uploadPart(uploadRequest);
                uploadPartResults.add(uploadPartResult);

                uploadedBytes += bytesRead;
                double percentComplete = (double) uploadedBytes / contentLength * 100;
                System.out.printf(GREEN + "INFO: Upload progress: %.2f%%\n" + RESET, percentComplete);
            }
            fis.close();

            // Complete the multipart upload.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
                    bucketName,
                    keyName,
                    initResponse.getUploadId(),
                    uploadPartResults.stream().map(UploadPartResult::getPartETag).collect(Collectors.toList()));
            s3Client.completeMultipartUpload(compRequest);

            System.out.println(GREEN+"INFO: File Uploaded Successfully" + RESET);
            return true;
        } catch (Exception e) {
            // Abort the multipart upload.
            s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, keyName, initResponse.getUploadId()));
            e.printStackTrace();
            System.err.println(RED + "ERROR: File upload failed" + RESET);
            return false;
        }
    }


    public String getUrlOfObject(String bucketName, String keyName) {
        return s3Client.getUrl(bucketName, keyName).toString();

    }
    public boolean doesFileExist( String bucketName, String fileKey) {
        try {
            s3Client.getObjectMetadata(bucketName, fileKey);
            return true;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return false;
            } else {
                throw e;
            }
        }
    }
}
