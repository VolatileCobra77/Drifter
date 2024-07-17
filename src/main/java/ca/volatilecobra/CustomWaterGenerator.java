package ca.volatilecobra;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.terrain.noise.Color;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import org.joml.SimplexNoise;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomWaterGenerator {

    public Vector2f size;
    public Vector3f center;
    public float noiseScalar;
    public Spatial waterGeometry;

    public CustomWaterGenerator(Vector2f size, float noiseScalar, Vector3f center) {

        this.size = size;
        this.center = center;
        this.noiseScalar = noiseScalar;

    }

    public Image generateWaterTexture(float[] noiseValues){

            int WIDTH = (int)(size.x);
            int HEIGHT = (int)(size.y);

            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            int i = 0;
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {

                    double noiseValue = noiseValues[i];
                    int colorValue = (int) ((noiseValue + 1) * 128); // Convert [-1,1] to [0,255]
                    Color color = new Color(colorValue, colorValue, 255); // Blueish water color
                    image.setRGB(x, y, color.toInteger());
                    i++;
                }
            }
//            File outputfile;
//            try {
//                outputfile = new File("resources\\Textures\\water_texture.png");
//                ImageIO.write(image, "png", outputfile);
//                return bufferedToReg(ImageIO.read(outputfile));
//            } catch (IOException e) {
//                //e.printStackTrace();
//            }
            return bufferedToReg(image);

    }

    public void setCenter(Geometry waterGeom, Vector3f center) {
        this.center = center;

        waterGeom.setLocalTranslation(center.subtract(new Vector3f((size.x/2f), center.y, (size.y/2f))));

    }
    private Image bufferedToReg(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bufferedImage.getRGB(x, y);
                buffer.put((byte) ((argb >> 16) & 0xFF)); // Red
                buffer.put((byte) ((argb >> 8) & 0xFF));  // Green
                buffer.put((byte) (argb & 0xFF));         // Blue
                buffer.put((byte) ((argb >> 24) & 0xFF)); // Alpha
            }
        }

        buffer.flip();
        return new Image(Image.Format.RGBA8, width, height, buffer);
    }
    public float[] regenerateMap(Vector3f location, Vector2f offset){
        Random rand = new Random();
        //float noiseScalar = rand.nextFloat(0.001f,0.09f);
        float[] newData = generate(location.z+offset.x, location.x+offset.y, noiseScalar);
        return newData;
    }

    public float[] generate(){
        float[] points = new float[(int)(size.x*size.y)];

        int i = 0;

        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                float height = SimplexNoise.noise(x * noiseScalar, y * noiseScalar);
                points[i]=height;
                i++;
            }

        }

        return points;

    }
    public float[] generate(float OffsetX, float OffsetY, float noiseScalar){
        float[] points = new float[(int)(size.x*size.y)];

        int i = 0;

        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                float height = SimplexNoise.noise((x+OffsetX) * noiseScalar, (y+OffsetY) * noiseScalar);
                points[i]=height;
                i++;
            }

        }

        return points;

    }

    public Spatial draw(AssetManager assetManager, float[] data, Material waterTexture){

        waterGeometry = createDeformedPlane(size, data);
        waterTexture.setTransparent(true);
        waterGeometry.setMaterial(waterTexture);

        return waterGeometry;



    }


    private Geometry createDeformedPlane(Vector2f size, float[] heights) {
        int width = (int) size.x;
        int depth = (int) size.y;

        Mesh mesh = new Mesh();

        Vector3f[] vertices = new Vector3f[width * depth];
        int[] indices = new int[(width - 1) * (depth - 1) * 6];

        // Create vertices
        int i = 0;
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                vertices[i] = new Vector3f(x, heights[i], z);
                i++;
            }
        }

        // Create indices
        int index = 0;
        for (int z = 0; z < depth - 1; z++) {
            for (int x = 0; x < width - 1; x++) {
                int topLeft = (z * width) + x;
                int topRight = topLeft + 1;
                int bottomLeft = ((z + 1) * width) + x;
                int bottomRight = bottomLeft + 1;

                // First triangle
                indices[index++] = topLeft;
                indices[index++] = bottomLeft;
                indices[index++] = topRight;

                // Second triangle
                indices[index++] = topRight;
                indices[index++] = bottomLeft;
                indices[index++] = bottomRight;
            }
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();

        Geometry geom = new Geometry("DeformedPlane", mesh);
        return geom;
    }

}