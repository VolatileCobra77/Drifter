package ca.volatilecobra.terrain;

import ca.volatilecobra.terrain.chunk.WorldChunk;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

public class Utils {
    public static int calculateChunkSize(WorldChunk chunk) {
        try {
            // Serialize the chunk to JSON
            Gson gson = new Gson();
            String jsonString = gson.toJson(chunk);

            // Measure the size of the serialized data in bytes
            byte[] jsonData = jsonString.getBytes("UTF-8");
            int sizeInBytes = jsonData.length;

            System.out.println("Size of the chunk data in bytes: " + sizeInBytes);
            return sizeInBytes;
        } catch (UnsupportedEncodingException e) {
            System.out.println("WARNING: An error occured seralizing a chunk to JSON");
            e.printStackTrace();
            return 0;
        }
    }
}
