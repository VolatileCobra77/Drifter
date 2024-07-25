package ca.volatilecobra.terrain.chunk;

import ca.volatilecobra.terrain.builder.ChunkBuilderState;
import ca.volatilecobra.terrain.iso.DensityVolume;
import ca.volatilecobra.terrain.iso.fractal.ModifiableDensityVolume;
import ca.volatilecobra.terrain.world.World;
import com.google.gson.Gson;
import com.jme3.math.Vector3f;

import java.io.UnsupportedEncodingException;

/**
 * Loads and unloads chunks for a specified world.
 */
public class ChunkLoader {

    private final World world;
    private final ModifiableDensityVolume densityVolume;
    private final ChunkBuilderState chunkBuilder;

    public ChunkLoader(World world) {
        this.world = world;
        // this.densityVolume = new GemsFractalDensityVolume(world.getSeed());
        this.densityVolume = new ModifiableDensityVolume(world);

        int availableProcessors = Runtime.getRuntime().availableProcessors() - 1;
        this.chunkBuilder = new ChunkBuilderState(this.world, availableProcessors);
        world.getAppContext().getAppStateManager().attach(this.chunkBuilder);
    }

    public int calculateChunkSize(WorldChunk chunk) {
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

    public Chunk loadChunk(GridPosition gridPosition) {

        WorldChunk chunk = new WorldChunk(this.world, gridPosition);

        // set the priority based on how close the camera is to the chunk.

        GridPosition camPos = GridPosition.fromWorldLocation(world.getAppContext().getCamera().getLocation());

        int distX = Math.abs(gridPosition.getX() - camPos.getX());
        // int distY = Math.abs(gridPosition.getY() - camPos.getY());
        int distZ = Math.abs(gridPosition.getZ() - camPos.getZ());

        // int min = Math.min(distX, (distY > distZ) ? distZ : distY);
        int min = Math.min(distX, distZ);



        // int viewDistance = world.getAppContext().getAppConfig().getVideoConfig().getRenderDistance();

        // int priority = viewDistance - min;

        chunk.setPriority(min);

        chunkBuilder.buildChunk(chunk);
        calculateChunkSize(chunk);
        return chunk;
    }

    public void disposeChunk(Chunk chunk) {
        this.chunkBuilder.disposeChunk(chunk);
    }

    public DensityVolume getDensityVolume() {
        return this.densityVolume;
    }

    public ChunkBuilderState getChunkBuilder() {
        return this.chunkBuilder;
    }

    public void modifyDensity(Vector3f loc, float density) {
        this.densityVolume.setDensity(loc, density);

        // and rebuild the chunk.
    }

}
