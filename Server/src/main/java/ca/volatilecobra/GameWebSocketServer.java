package ca.volatilecobra;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;


import java.io.*;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class GameWebSocketServer extends WebSocketServer {
    public static boolean SAVE_CHUNKS = true;
    final String RESET = "\u001B[0m";
    final String RED = "\u001B[31m";
    final String GREEN = "\u001B[32m";
    final String YELLOW = "\u001B[33m";
    private static final Gson gson = new Gson();
    TerrainGenerator terrainGenerator;
    private List<player> players = new ArrayList<player>();
    private String configFilePath;
    boolean useAWS;
    AWSSync awsSync;
    String heightmapURL;

    public GameWebSocketServer(InetSocketAddress address, String configFilePath) {
        super(address);
        this.configFilePath = configFilePath;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println(GREEN + "INFO: New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + RESET);

        conn.send(String.format("{connection:true}")); //send client Inital Data for this server, including Heightmap, ore positions, player positions, item positions, physics objects.
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(GREEN + "INFO: Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + RESET);

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(GREEN + "INFO: Recieved message from " + conn.getRemoteSocketAddress().getAddress().getHostAddress() +" Message contents: " + message + RESET);
        if (message.startsWith("{terrainRequest:")){
            System.out.println(GREEN + "INFO: Sending Heightmpa" + RESET);
            if (useAWS){
                conn.send(String.format("{heightmapURL:\"%s\"}", heightmapURL));
            }else{
                System.out.println(YELLOW + "WARNING: Sending raw Heightmap over network, this requires a lot of bandwidth" + RESET);
                conn.send(String.format("{heightMap:%s}", gson.toJson(terrainGenerator.heightmap)));
            }


        }else if (message.startsWith("{player:")){
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            for (player user:players){
                try {
                    if (user.id == json.get("id").getAsInt()) {
                        user.position = new Vector3f(json.get("position").getAsJsonObject().get("x").getAsFloat(),json.get("position").getAsJsonObject().get("y").getAsFloat(),json.get("position").getAsJsonObject().get("z").getAsFloat());
                        System.out.printf("%sINFO: Updating user Postion to %s\n%s", GREEN, new Vector3f(json.get("position").getAsJsonObject().get("x").getAsFloat(),json.get("position").getAsJsonObject().get("y").getAsFloat(),json.get("position").getAsJsonObject().get("z").getAsFloat()),RESET);
                        user.lastUpdate = System.currentTimeMillis();
                        player newUser = new player(user.name, new Vector3f(json.get("position").getAsJsonObject().get("x").getAsFloat(),json.get("position").getAsJsonObject().get("y").getAsFloat(),json.get("position").getAsJsonObject().get("z").getAsFloat()), conn, user.id);
                        players.remove(user);
                        players.add(newUser);
                        conn.send("{\"update\":true}");
                        return;
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                    conn.send(String.format("{error:%s}", e.getMessage()));
                }
            }
            try {
                String name = json.get("player").getAsString();
                Vector3f position = new Vector3f(json.get("position").getAsJsonObject().get("x").getAsFloat(),json.get("position").getAsJsonObject().get("y").getAsFloat(),json.get("position").getAsJsonObject().get("z").getAsFloat());
                players.add(new player(name, position, conn, players.size()));
                conn.send(String.format("{id:%s}", players.size()-1));
            } catch (NullPointerException e) {
                e.printStackTrace();
                conn.send("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        if (message.startsWith("getPlayers")){
            List<clientPlayer> clientPlayers = new ArrayList<clientPlayer>();
            for(player user:players){
                clientPlayers.add(user.ClientPlayer);
            }
           for (player user:players){
               System.out.printf(GREEN + "INFO: Player Connected: %s, Position %s\n" + RESET, user.name, user.position);
           }
            conn.send(String.format("{players:%s}", gson.toJson(clientPlayers)));
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            //do nothing, because it breaks
        }
    }

    @Override
    public void onStart() {
        System.out.println(GREEN + "INFO: Starting Services..." + RESET);
        System.out.println(GREEN + "INFO: loading config file" + RESET);
        if (configFilePath == null || !configFilePath.endsWith(".json")){
            System.err.println(RED + "ERROR: No or non-json config file provided, Please provide a json config file using -c or --config argument" + RESET);
            System.exit(1);
        }
        System.out.println(GREEN + "INFO: Loading config from " + configFilePath + RESET);
        JsonObject settingsFile = null;
        try {
            settingsFile = (JsonObject) new JsonParser().parse(new FileReader(configFilePath));
        } catch (FileNotFoundException e) {
            System.err.printf(RED + "ERROR: Settings File %s not found, or failed to read" + RESET, configFilePath);
            System.exit(1);
        }


        //print settings file pretty to the console
        Gson prettyPrintGson = new GsonBuilder().setPrettyPrinting().create();
        Object json = prettyPrintGson.fromJson(settingsFile, Object.class);
        String prettyJson = prettyPrintGson.toJson(json);
        System.out.println(GREEN + "INFO: Loaded Json:" + RESET);
        System.out.println(GREEN + prettyJson + RESET);
        if (!settingsFile.getAsJsonObject("TerrainGenerator").get("useLegacy").getAsBoolean()){
            boolean saveWorld = false;
            String saveFileName = "";
            String saveFilePath = "";

            String settingsFilePath = "";
            String modelsFilePath = "";
            String texturesFilePath = "";

            int antiStrophicFilteringLevel = 0;

        }
        float ironOreFreq = 1;
        float ironOreSize = 1;


        float terrainSeed = 0;
        float terrainHeightMult = 1;
        float terrainScale = 0.25f;
        Vector2f terrainSize = new Vector2f(0,0);


        float idlePingTimeout = 30000;
        float pingCheckInterval = 5000;
        float maxPlayers = 10;
        boolean whitelistEnabled = false;
        boolean blacklistEnabled = true;
        List<Integer> whitelist = new ArrayList<Integer>();
        List<Integer> blacklist = new ArrayList<Integer>();
        String awsSecretKey = "";
        String awsAccessKey = "";
        String awsRegion = "";
        String outputFilePath = "";
        String awsBucketName = "";
        try {
            //load Main Objects
            System.out.println(GREEN + "INFO: Loading main objects..." + RESET);
            JsonObject terrainGeneratorSettings = settingsFile.get("TerrainGenerator").getAsJsonObject();
            JsonObject serverSettings = settingsFile.get("Server").getAsJsonObject();
            JsonObject awsSettings = settingsFile.get("AWS").getAsJsonObject();
            System.out.println(GREEN + "INFO: Main Objects loaded" + RESET);

            //load TerrainGenerator specific settings
            System.out.println(GREEN + "INFO: Loading TerrainGenerator settings" + RESET);
            JsonObject oreSettings = terrainGeneratorSettings.get("ores").getAsJsonObject();
            JsonObject terrainSettings = terrainGeneratorSettings.get("terrain").getAsJsonObject();
            outputFilePath = terrainGeneratorSettings.get("outFile").getAsString();
            System.out.println(GREEN + "INFO: TerrainGenerator settings loaded" + RESET);

            //repeat for all ores
            System.out.println(GREEN + "INFO: Loading Ores..." + RESET);
            JsonObject ironOre = oreSettings.get("iron").getAsJsonObject();
            System.out.println(GREEN + "INFO: Loading Iron" + RESET);
            ironOreFreq = ironOre.get("frequencyMult").getAsFloat();
            ironOreSize = ironOre.get("sizeMult").getAsFloat();
            System.out.println(GREEN + "INFO: Iron Loaded" + RESET);
            System.out.println(GREEN + "INFO: Ores Loaded" + RESET);

            //load Generation settings
            System.out.println(GREEN + "INFO: Loading Generation settings..." + RESET);
            terrainSeed = terrainSettings.get("seed").getAsFloat();
            terrainHeightMult = terrainSettings.get("heightMult").getAsFloat();
            terrainSize = gson.fromJson(terrainSettings.get("size").getAsJsonObject(), Vector2f.class);
            terrainScale = terrainSettings.get("scale").getAsFloat();

            System.out.println(GREEN + "INFO: Loaded Generation settings" + RESET);

            //load server Settings
            System.out.println(GREEN + "INFO: Loading Server Settings..." + RESET);
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            whitelist = gson.fromJson(serverSettings.get("whitelist").getAsJsonObject().get("whitelistedUsers").getAsJsonArray(), listType);
            whitelistEnabled = serverSettings.get("whitelist").getAsJsonObject().get("enabled").getAsBoolean();
            blacklist = gson.fromJson(serverSettings.get("blacklist").getAsJsonObject().get("blacklistedUsers").getAsJsonArray(), listType);
            blacklistEnabled = serverSettings.get("blacklist").getAsJsonObject().get("enabled").getAsBoolean();
            idlePingTimeout = serverSettings.get("idlePingTimeout").getAsFloat();
            maxPlayers = serverSettings.get("maxPlayers").getAsInt();
            pingCheckInterval = serverSettings.get("pingCheckInterval").getAsFloat();
            System.out.println(GREEN + "INFO: Blacklisted users:" + blacklist.toString() + RESET);
            System.out.println(GREEN + "INFO: Whitelisted users:" + whitelist.toString()+ RESET);
            System.out.println(GREEN + "INFO: Using Blacklist:" + blacklistEnabled + RESET);
            System.out.println(GREEN + "INFO: Using Whitelist:" + whitelistEnabled + RESET);


            System.out.println(GREEN + "INFO: Checking for AWS environmnent" + RESET);
            useAWS = awsSettings.get("useAws").getAsBoolean();
            if (useAWS){
                awsAccessKey = awsSettings.get("accessKey").getAsString();
                awsRegion = awsSettings.get("region").getAsString();
                awsSecretKey = awsSettings.get("secretKey").getAsString();
                awsBucketName = awsSettings.get("bucketName").getAsString();
            }else{
                System.out.println(YELLOW + "WARNING: Not using AWS environment, this requires a lot of bandwidth" + RESET);
            }
        }catch (NullPointerException e){
            System.err.println(RED + "ERROR: Incorrect JSON file, double check the file is correctly formatted" + RESET);
            e.printStackTrace();
        }
        System.out.println(GREEN + "INFO: Generating Terrain..."+ RESET);
        this.terrainGenerator = new TerrainGenerator(terrainSize, terrainScale, terrainHeightMult, ironOreFreq, ironOreSize);
        terrainGenerator.generateTerrain(new Vector2f(terrainSeed,terrainSeed));
        System.out.println(GREEN + "INFO: Generated Terrain"+ RESET);
        System.out.println(GREEN + "INFO: Connecting to AWS bucket" + RESET);
        awsSync = new AWSSync(awsAccessKey, awsSecretKey, awsRegion);
        System.out.println(GREEN + "INFO: Connected to AWS bucket" + RESET);
        System.out.println(GREEN + "INFO: Writing output fIle" + RESET);
        File file = new File(outputFilePath);

        try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
            file.createNewFile();

            gson.toJson(terrainGenerator.heightmap, fileWriter);
            System.out.println(GREEN + "INFO: Wrote output file" + RESET);
        }catch(IOException e){
            e.printStackTrace();
            System.err.println(RED + "ERROR: Could not write output file" + RESET);
        }
        System.out.println(GREEN + "INFO: Syncing with AWS bucket" + RESET);

        if (awsSync.sync(outputFilePath, "Heightmap.json", awsBucketName, awsSync.creds, 5)){
            heightmapURL = awsSync.getUrlOfObject(awsBucketName, "Heightmap.json");
            System.out.println(GREEN + "INFO: Synced with AWS bucket, heightmap avaliable at" + heightmapURL + RESET);
        }

        System.out.println(GREEN + "INFO: Starting Services..." + RESET);
        Thread th = new Thread(new OfflinePlayerChecker(idlePingTimeout, pingCheckInterval));
        th.start();
        System.out.println(GREEN + "INFO: Services Started" + RESET);

        System.out.println(GREEN + "INFO: Server is up on address" + getAddress()+ RESET);
    }
    private class OfflinePlayerChecker implements Runnable {

        float idleTimeoutDelay = 30000;
        float pingCheckInterval = 5000;
        public OfflinePlayerChecker(){

        }
        public OfflinePlayerChecker(float idleTimeoutDelay, float pingCheckInterval){
            this.idleTimeoutDelay = idleTimeoutDelay;
        }
        @Override
        public void run() {
            System.out.println(GREEN + "INFO: Started timeout checker");
            while (true) {
                try {
                    // Check for offline players
                    System.out.println(GREEN + "INFO: Checking for offline players..." + RESET);
                    for (int i = 0; i < players.size(); i++){
                        player user = players.get(i);
                        System.out.printf(GREEN + "    INFO: Checking %s, lastUpdate at %s, current time %s, difference %s, should time out: %s\n" + RESET, user.name, user.lastUpdate, System.currentTimeMillis(), System.currentTimeMillis() - user.lastUpdate, user.lastUpdate + idleTimeoutDelay < System.currentTimeMillis());
                        if (user.lastUpdate + idleTimeoutDelay < System.currentTimeMillis()){
                            players.remove(user);
                            user.connection.close(0, "idleTimeOut");
                            System.out.println(YELLOW + "WARNING: Player " + user.name + " timed out" + RESET);
                        }
                    }
                    Thread.sleep((long)pingCheckInterval); // Check every 5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class player{
    public String name;
    public Vector3f position;
    public long lastUpdate;
    public WebSocket connection;
    public clientPlayer ClientPlayer;
    public int id;
    public player(String name, Vector3f position, WebSocket connection, int id){
        this.name = name;
        this.position = position;
        this.lastUpdate = System.currentTimeMillis();
        this.connection = connection;
        this.ClientPlayer = new clientPlayer(name, position, id);
        this.id = id;
    }
}
class clientPlayer{
    public String name;
    public Vector3f position;
    public int id;
    public clientPlayer(String name, Vector3f position, int id){
        this.name = name;
        this.position = position;
        this.id = id;
    }
}

class OreWrapper{
    public static List<OreWrapper> globalOresList = new ArrayList<OreWrapper>();
    public String name;
    public int maxVeinSize;
    public int minVeinSize;
    public String modelPath;
    public String texturePath;
    public File model;
    public File texture;
    public int index;
    public OreWrapper(String name, int maxVeinSize, int minVeinSize, String modelPath, String texturePath){
        this.name = name;
        this.maxVeinSize = maxVeinSize;
        this.minVeinSize = minVeinSize;
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.model = new File(texturePath);
        this.texture = new File(texturePath);

        globalOresList.add(this);
        this.index = globalOresList.indexOf(this);
    }
}
class OreVein{}
class Ore{
    public OreWrapper oreType;
    public OreVein parentVein;
    public Ore(OreWrapper oreType, OreVein parentVein){
        this.oreType = oreType;
        this.parentVein = parentVein;
    }
}