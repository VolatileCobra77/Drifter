package ca.volatilecobra.terrain.interaction.brush;

import ca.volatilecobra.terrain.interaction.brush.shape.BrushShape;
import ca.volatilecobra.terrain.material.WorldMaterial;
import com.jme3.math.Vector3f;

/**
 * Provides methods for listening to brush changes.
 */
public interface BrushListener {

    void brushChanged(BrushShape brush);
    void SizeChanged(Vector3f size);
    void worldMaterialChanged(WorldMaterial worldMaterial);

}
