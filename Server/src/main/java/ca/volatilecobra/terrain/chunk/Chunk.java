package ca.volatilecobra.terrain.chunk;

import ca.volatilecobra.terrain.iso.volume.ArrayDensityVolume;
import ca.volatilecobra.terrain.material.WorldMaterial;
import ca.volatilecobra.terrain.world.World;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * Created by James on 24/04/2017.
 */
public interface Chunk {

    int SIZE_XYZ = 16;
    Vector3f SIZE = new Vector3f(SIZE_XYZ, SIZE_XYZ, SIZE_XYZ);

    World getWorld();
    GridPosition getGridPosition();
    Node getChunkNode();

    ArrayDensityVolume getDensityVolume();
    ArrayDensityVolume getVoxelVolume();

    void setVoxel(int x, int y, int z, WorldMaterial worldMaterial, float density);

    void applyToScene();
    boolean disposeSafely();

    void rebuild();
    boolean isMarkedForRebuild();
    void unmarkForRebuild();

    void save();

    int getPriority();
    void setPriority(int priority);
}
