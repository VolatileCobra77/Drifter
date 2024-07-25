package ca.volatilecobra.terrain.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by James on 28/04/2017.
 * Edited by VolatileCobra77 on 24/07/2024
 */
public class StoragePaths {

    public static Path SAVEGAME_DIR = Paths.get("./data/saves");
    public static Path SETTINGS_DIR = Paths.get("./data/settings");

    public static void create() {

        if (!SAVEGAME_DIR.toFile().exists()) {
            SAVEGAME_DIR.toFile().mkdirs();
        }

        if (!SETTINGS_DIR.toFile().exists()) {
            SETTINGS_DIR.toFile().mkdirs();
        }

    }
}
