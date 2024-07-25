package ca.volatilecobra.terrain.world;

import ca.volatilecobra.terrain.biome.BiomeGenerator;
import ca.volatilecobra.terrain.chunk.Chunk;
import ca.volatilecobra.terrain.chunk.ChunkLoader;
import ca.volatilecobra.terrain.chunk.GridPosition;
import ca.volatilecobra.terrain.core.ApplicationContext;
import ca.volatilecobra.terrain.pager.ChunkPager;
import com.jme3.scene.Node;

/**
 * Represents a World
 */
public interface World {

    Node getWorldNode();
    long getSeed();
    ApplicationContext getAppContext();
    ChunkLoader getChunkLoader();
    ChunkPager getChunkPager();
    BiomeGenerator getBiomeGenerator();

    WorldType getWorldType();
    String getWorldName();

    Chunk getChunk(GridPosition gridPosition);

}
