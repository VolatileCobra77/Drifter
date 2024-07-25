package ca.volatilecobra;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import org.joml.SimplexNoise;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {

    private TerrainQuad terrain;

    public TerrainGenerator(AssetManager assetManager, Camera camera) {
        // Initialize terrain generator
        int size = 512; // Size of the terrain
        float scale = 0.25f;
        float heightMult = 2f;

        terrain = new TerrainQuad("terrain", 65, size + 1, null);
        Material terrainMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        terrainMat.setTexture("ColorMap", assetManager.loadTexture("Textures/rocks.png"));

//        Material matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
//
//        matTerrain.setTexture("Alpha", assetManager.loadTexture("Textures/Heightmap.png"));
//
//        Texture normalRocks = assetManager.loadTexture(
//                "Textures/rocks.png");
//        normalRocks.setWrap(Texture.WrapMode.Repeat);
//        matTerrain.setTexture("Tex1", normalRocks);
//        matTerrain.setFloat("Tex1Scale", 64f);
//
//        Texture darkerRocks = assetManager.loadTexture(
//                "Textures/rocks_2.png");
//        darkerRocks.setWrap(Texture.WrapMode.Repeat);
//        matTerrain.setTexture("Tex2", darkerRocks);
//        matTerrain.setFloat("Tex1Scale", 64f);
//
//        Texture darkestRocks = assetManager.loadTexture(
//                "Textures/rocks_2.png");
//        darkestRocks.setWrap(Texture.WrapMode.Repeat);
//        matTerrain.setTexture("Tex3", darkestRocks);
//        matTerrain.setFloat("Tex1Scale", 64f);

        // Generate image-based heightmap
        List<Vector3f> heightmap = new ArrayList<Vector3f>();
        for (int i = -size; i < size; i++) {
            for (int j = -size; j < size; j++) {
                float height = SimplexNoise.noise(i * scale, j * scale);
                heightmap.add(new Vector3f(j, i, height*heightMult));
            }
        }

        // Apply heightmap to the terrain
        for (Vector3f height : heightmap) {
            terrain.adjustHeight(new Vector2f(height.x, height.y), height.z);
        }
//        AbstractHeightMap heightmap = null;
//        Texture heightMapImage = assetManager.loadTexture(
//                "Textures/Heightmap.png");
//        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
//        heightmap.load();

//        float[] heightmapFloat = new float[size];
//
//        for (float height : heightmap.getHeightMap()){
//            //heightmapFloat.
//        }



        terrain.setLocalTranslation(0, -1500, 0);
        terrain.setLocalScale(scale, 1, scale);
        terrain.setMaterial(terrainMat);
        TerrainLodControl control = new TerrainLodControl(terrain, camera);
        terrain.addControl(control);

    }
    public TerrainGenerator(List<Vector3f> heightmap, AssetManager assetManager, Camera camera  ) {
        // Initialize terrain generator
        int size = 512; // Size of the terrain
        float scale = 0.25f;
        float heightMult = 2f;

        terrain = new TerrainQuad("terrain", 65, size + 1, null);
        Material terrainMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        terrainMat.setTexture("ColorMap", assetManager.loadTexture("Textures/rocks.png"));

//        Material matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
//
//        matTerrain.setTexture("Alpha", assetManager.loadTexture("Textures/Heightmap.png"));
//
//        Texture normalRocks = assetManager.loadTexture(
//                "Textures/rocks.png");
//        normalRocks.setWrap(Texture.WrapMode.Repeat);
//        matTerrain.setTexture("Tex1", normalRocks);
//        matTerrain.setFloat("Tex1Scale", 64f);
//
//        Texture darkerRocks = assetManager.loadTexture(
//                "Textures/rocks_2.png");
//        darkerRocks.setWrap(Texture.WrapMode.Repeat);
//        matTerrain.setTexture("Tex2", darkerRocks);
//        matTerrain.setFloat("Tex1Scale", 64f);
//
//        Texture darkestRocks = assetManager.loadTexture(
//                "Textures/rocks_2.png");
//        darkestRocks.setWrap(Texture.WrapMode.Repeat);
//        matTerrain.setTexture("Tex3", darkestRocks);
//        matTerrain.setFloat("Tex1Scale", 64f);

        // Generate image-based heightmap

        // Apply heightmap to the terrain
        for (Vector3f height : heightmap) {
            terrain.adjustHeight(new Vector2f(height.x, height.y), height.z);
        }
//        AbstractHeightMap heightmap = null;
//        Texture heightMapImage = assetManager.loadTexture(
//                "Textures/Heightmap.png");
//        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
//        heightmap.load();

//        float[] heightmapFloat = new float[size];
//
//        for (float height : heightmap.getHeightMap()){
//            //heightmapFloat.
//        }



        terrain.setLocalTranslation(0, -1500, 0);
        terrain.setLocalScale(scale, 1, scale);
        terrain.setMaterial(terrainMat);
        TerrainLodControl control = new TerrainLodControl(terrain, camera);
        terrain.addControl(control);

    }

    public TerrainQuad getTerrain() {
        return terrain;
    }

    private byte[] convertToBytes(float[] data) {
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) (data[i] * 255); // Convert float to byte (0-255 range)
        }
        return bytes;
    }
}