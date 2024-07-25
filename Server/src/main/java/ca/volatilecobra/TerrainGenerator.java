package ca.volatilecobra;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import org.joml.SimplexNoise;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {

    private Vector2f size;
    private float scale;
    private float heightMult;
    public List<Vector3f> heightmap;

    public TerrainGenerator(Vector2f size, float scale, float heightMult, float ironOreFreq, float ironOreSize) {
        // Initialize terrain generator
        this.size = size;
        this.scale = scale;
        this.heightMult = heightMult;

    }

    public List<Vector3f> generateTerrain(Vector2f offset){
        List<Vector3f> map = new ArrayList<Vector3f>();
        for (int i = (int)-size.x; i < size.x; i++) {
            for (int j = (int)-size.y; j < size.y; j++) {
                float height = SimplexNoise.noise((i * scale)+offset.x, (j * scale)+offset.y);
                map.add(new Vector3f(j, i, height*heightMult));
            }
        }
        heightmap = map;
        return map;
    }
    public List<Vector3f> generateTerrain(){
        List<Vector3f> map = new ArrayList<Vector3f>();
        for (int i = (int)-size.x; i < size.x; i++) {
            for (int j = (int)-size.y; j < size.y; j++) {
                float height = SimplexNoise.noise(i * scale, j * scale);
                map.add(new Vector3f(j, i, height*heightMult));
            }
        }
        heightmap = map;
        return map;
    }
}