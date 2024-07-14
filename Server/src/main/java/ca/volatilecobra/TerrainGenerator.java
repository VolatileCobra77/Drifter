package ca.volatilecobra;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import org.joml.SimplexNoise;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {

    private int size;
    private float scale;
    private float heightMult;
    public List<Vector3f> heightmap;

    public TerrainGenerator(int size, float scale, float heightMult) {
        // Initialize terrain generator
        this.size = size;
        this.scale = scale;
        this.heightMult = heightMult;

    }

    public List<Vector3f> generateTerrain(Vector2f offset){
        List<Vector3f> map = new ArrayList<Vector3f>();
        for (int i = -size; i < size; i++) {
            for (int j = -size; j < size; j++) {
                float height = SimplexNoise.noise((i * scale)+offset.x, (j * scale)+offset.y);
                map.add(new Vector3f(j, i, height*heightMult));
            }
        }
        heightmap = map;
        return map;
    }
    public List<Vector3f> generateTerrain(){
        List<Vector3f> map = new ArrayList<Vector3f>();
        for (int i = -size; i < size; i++) {
            for (int j = -size; j < size; j++) {
                float height = SimplexNoise.noise(i * scale, j * scale);
                map.add(new Vector3f(j, i, height*heightMult));
            }
        }
        heightmap = map;
        return map;
    }
}