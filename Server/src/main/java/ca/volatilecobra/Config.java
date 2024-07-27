package ca.volatilecobra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.math.Vector2f;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Config {

    public static void main(String[] args) {
        Config config = new Config();

        config.terrainGenerator.ores.add(new Ore("Iron", 1, 12, 1, "ironOre.fbx", "ironOre.png"));
        config.newTerrainGen.ores.add(new Ore("Iron", 1, 12, 1, "ironOre.fbx", "ironOre.png"));
        config.server.whitelist.whitelistedUsers.add(new user(1,"test"));
        // Create a Gson instance
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Serialize the config object to JSON
        String json = gson.toJson(config);

        // Write the JSON to a file
        try (FileWriter writer = new FileWriter("config.json")) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Config has been written to config.json");
    }


    public static class TerrainGenerator {
        private String comment = "This is the settings for the Legacy terrainGenerator to generate terrain";
        private boolean useLegacy = true;
        private List<Ore> ores = new ArrayList<Ore>();
        private Terrain terrain = new Terrain();
        private String outFile = "worldFile.json";

        public String getComment() {
            return comment;
        }

        public boolean isUseLegacy() {
            return useLegacy;
        }

        public List<Ore> getOres() {
            return ores;
        }

        public void setOre(int index, Ore ore){
            ores.set(index, ore);
        }

        public Terrain getTerrain() {
            return terrain;
        }

        public String getOutFile() {
            return outFile;
        }
    }

    public static class Ore {
        private String name;
        private int rarity;
        private int minVeinSize;
        private int maxVeinSize;
        private String modelPath;
        private String texturePath;

        public Ore(String name, int rarity, int minVeinSize, int maxVeinSize, String modelPath, String texturePath) {
            this.name = name;
            this.rarity = rarity;
            this.minVeinSize = minVeinSize;
            this.maxVeinSize = maxVeinSize;
            this.modelPath = modelPath;
            this.texturePath = texturePath;
        }
        public String getName() {
            return name;
        }
        public int getRarity() {
            return rarity;
        }
        public int getMinVeinSize() {
            return minVeinSize;
        }
        public int getMaxVeinSize() {
            return maxVeinSize;
        }
        public String getModelPath() {
            return modelPath;
        }
        public String getTexturePath() {
            return texturePath;
        }
    }

    public static class Iron {
        private int frequencyMult = 1;
        private int sizeMult = 1;

        public int getFrequencyMult() {
            return frequencyMult;
        }

        public int getSizeMult() {
            return sizeMult;
        }
    }

    public static class Terrain {
        private int seed = 1234;
        private Size size = new Size();
        private int heightMult = 2;
        private double scale = 0.25;

        public int getSeed() {
            return seed;
        }

        public Size getSize() {
            return size;
        }

        public int getHeightMult() {
            return heightMult;
        }

        public double getScale() {
            return scale;
        }
    }

    public static class Size {
        private int x = 512;
        private int y = 512;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Vector2f getAsVec2(){
            return new Vector2f(x,y);
        }
    }

    public static class NewTerrainGen {
        private String comment = "New terrain generation thanks to jeyfella, link in README.";
        private int seed = 1234;
        private SaveFile saveFile = new SaveFile();
        private AntiStrophicFiltering antiStrophicFiltering = new AntiStrophicFiltering();
        private StoragePaths storagePaths = new StoragePaths();
        private Video video = new Video();
        private List<Ore> ores = new ArrayList<Ore>();



        public String getComment() {
            return comment;
        }

        public int getSeed() {
            return seed;
        }

        public SaveFile getSaveFile() {
            return saveFile;
        }

        public AntiStrophicFiltering getAntiStrophicFiltering() {
            return antiStrophicFiltering;
        }

        public StoragePaths getStoragePaths() {
            return storagePaths;
        }

        public Video getVideo() {
            return video;
        }

        public List<Ore> getOres() {
            return ores;
        }
    }

    public static class SaveFile {
        private boolean saveWorld = true;
        private String saveFileName = "My Save";
        private String comment = "See StoragePaths to tweak where the save Files are located";

        public boolean isSaveWorld() {
            return saveWorld;
        }

        public String getSaveFileName() {
            return saveFileName;
        }

        public String getComment() {
            return comment;
        }
    }

    public static class AntiStrophicFiltering {
        private int level = 10;

        public int getLevel() {
            return level;
        }
    }

    public static class StoragePaths {
        private String saveFilePath = "data/saves/";
        private String settingsPath = "data/settings/";
        private String modelsPath = "data/models";
        private String texturesPath = "data/textures";

        public String getSaveFilePath() {
            return saveFilePath;
        }

        public String getSettingsPath() {
            return settingsPath;
        }

        public String getModelsPath() {
            return modelsPath;
        }

        public String getTexturesPath() {
            return texturesPath;
        }
    }

    public static class Video {
        private int maxRenderDistance = 12;

        public int getMaxRenderDistance() {
            return maxRenderDistance;
        }
    }

    public static class Server {
        private String comment = "This is the settings for the server, including max Players, IP, port, whitelist, blacklist";
        private Address address = new Address();
        private int maxPlayers = 10;
        private Whitelist whitelist = new Whitelist();
        private Blacklist blacklist = new Blacklist();
        private int idlePingTimeout = 30000;
        private int pingCheckInterval = 5000;

        public String getComment() {
            return comment;
        }

        public Address getAddress() {
            return address;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public Whitelist getWhitelist() {
            return whitelist;
        }

        public Blacklist getBlacklist() {
            return blacklist;
        }

        public int getIdlePingTimeout() {
            return idlePingTimeout;
        }

        public int getPingCheckInterval() {
            return pingCheckInterval;
        }
    }

    public static class Address {
        private String ip = "localhost";
        private int port = 8080;

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }
    }

    public static class Whitelist {
        private boolean enabled = false;
        private List<user> whitelistedUsers = new ArrayList<user>();

        public boolean isEnabled() {
            return enabled;
        }

        public List<user> getWhitelistedUsers() {
            return whitelistedUsers;
        }
    }

    public static class Blacklist {
        private boolean enabled = true;
        private List<user> blacklistedUsers = new ArrayList<user>();

        public boolean isEnabled() {
            return enabled;
        }

        public List<user> getBlacklistedUsers() {
            return blacklistedUsers;
        }
    }

    public static class AWS {
        private String comment = "If not using AWS for heightmap sharing, set useAws to false, the server will then share the heightmap over the websocket connection (CAUTION: This requires a high bandwidth as the heightmap is a very large file and can easily be over a gigabyte)";
        private boolean useAws = true;
        private String bucketName = "";
        private String secretKey = "";
        private String accessKey = "";
        private String region = "us-east-2";

        public String getComment() {
            return comment;
        }

        public boolean isUseAws() {
            return useAws;
        }

        public String getBucketName() {
            return bucketName;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public String getRegion() {
            return region;
        }
    }

    private TerrainGenerator terrainGenerator = new TerrainGenerator();
    private NewTerrainGen newTerrainGen = new NewTerrainGen();
    private Server server = new Server();
    private Water water = new Water();
    private AWS aws = new AWS();


    public Water getWater(){return water;}

    public TerrainGenerator getTerrainGenerator() {
        return terrainGenerator;
    }

    public NewTerrainGen getNewTerrainGen() {
        return newTerrainGen;
    }

    public Server getServer() {
        return server;
    }

    public AWS getAws() {
        return aws;
    }

    public static class Water{
        private int sineAmmount = 10;
        public int getSineAmmount() {
            return sineAmmount;
        }
    }

    public static class user{
        private int uid;
        private String uname;
        private String hash;
        public user(int uid, String uname, String hash) {
            this.uid = uid;
            this.uname = uname;
            this.hash = hash;
        }
        public user(int uid, String uname) {
            this.uid = uid;
            this.uname = uname;
            this.hash = null;
        }
        public int getUid() {
            return uid;
        }
        public String getUname() {
            return uname;
        }
        public String getHash() {
            return hash;
        }
    }
}