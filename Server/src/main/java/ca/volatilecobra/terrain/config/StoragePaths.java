package ca.volatilecobra.terrain.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

/**
 * Created by James on 28/04/2017.
 * Edited by VolatileCobra77 on 24/07/2024
 * Edited by VolatileCobra77 on 25/07/2024
 */
public class StoragePaths {

    public static Path SAVEGAME_DIR = Paths.get("./data/saves");
    public static Path SETTINGS_DIR = Paths.get("./data/settings");
    public static Path MODELS_DIR = Paths.get("./data/models");
    public static Path TEXTURES_DIR = Paths.get("./data/textures");

    public static void changeSaveGameDir(Path path) {
        changeDirectory(SAVEGAME_DIR, path);
        SAVEGAME_DIR = path;
    }

    public static void changeSettingsDir(Path path) {
        changeDirectory(SETTINGS_DIR, path);
        SETTINGS_DIR = path;
    }

    public static void changeModelsDir(Path path) {
        changeDirectory(MODELS_DIR, path);
        MODELS_DIR = path;
    }

    public static void changeTexturesDir(Path path) {
        changeDirectory(TEXTURES_DIR, path);
        TEXTURES_DIR = path;
    }

    private static void changeDirectory(Path oldDir, Path newDir) {
        try {
            if (!Files.exists(newDir)) {
                Files.createDirectories(newDir);
            }

            Files.walk(oldDir).forEach(sourcePath -> {
                try {
                    Path targetPath = newDir.resolve(oldDir.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectory(targetPath);
                        }
                    } else {
                        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to move " + sourcePath + " to " + newDir + ": " + e);
                }
            });

            // Delete the old directory after moving its contents
            Files.walk(oldDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete " + path + ": " + e);
                        }
                    });

        } catch (IOException e) {
            System.err.println("Failed to move files from " + oldDir + " to " + newDir + ": " + e);
        }
    }

    public static void create() {

        if (!SAVEGAME_DIR.toFile().exists()) {
            SAVEGAME_DIR.toFile().mkdirs();
        }

        if (!SETTINGS_DIR.toFile().exists()) {
            SETTINGS_DIR.toFile().mkdirs();
        }

        if (!MODELS_DIR.toFile().exists()) {
            MODELS_DIR.toFile().mkdirs();
        }

        if (!TEXTURES_DIR.toFile().exists()) {
            TEXTURES_DIR.toFile().mkdirs();
        }

    }
}
