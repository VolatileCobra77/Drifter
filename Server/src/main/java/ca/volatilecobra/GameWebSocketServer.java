package ca.volatilecobra;

import ca.volatilecobra.terrain.core.ApplicationContext;
import ca.volatilecobra.terrain.world.AnimaliaWorld;
import ca.volatilecobra.terrain.world.World;
import ca.volatilecobra.terrain.world.WorldType;
import ca.volatilecobra.terrain.config.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ca.volatilecobra.GameServer.*;

class GameWebSocketServer extends WebSocketServer {

    private SimpleApplication app;
    private String ip;
    private int port;
    private Config config;
    private GameServer gameServer;
    private World world;
    private ApplicationContext appContext;
    private List<Player> players = new ArrayList<Player>();
    private OfflinePlayerChecker offlinePlayerChecker;
    private List<WebSocket> clients = new ArrayList<WebSocket>();
    private Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassTypeAdapter()).create();

    public GameWebSocketServer(String ip, int port, Config configFile, SimpleApplication app, GameServer server){
        super(new InetSocketAddress(ip, port));
        this.ip = ip;
        this.port = port;
        this.config = configFile;
        this.app = app;
        this.gameServer = server;
        this.appContext = new ApplicationContext(app);
        this.gameServer = server;
    }

    public OfflinePlayerChecker getOfflinePlayerChecker(){return this.offlinePlayerChecker;}

    public List<Player> getPlayers(){return this.players;}

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        clients.add(webSocket);
        System.out.println(GREEN + "WEBSOCKET: INFO: New Connection from " + webSocket.getRemoteSocketAddress()+ RESET);
        webSocket.send(gson.toJson(new MessageWrapper(SuccessMessage.class, new SuccessMessage(true))));
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        clients.remove(webSocket);
        for (int j = 0; j < players.size(); j++){
            Player player = players.get(j);
            if (player.connection == webSocket){
                players.remove(i);
                System.out.println(YELLOW + "WEBSOCKET: WARNING: Player " + player.name + " disconnected" + RESET);
                return;
            }
        }
        System.out.println(YELLOW + "WEBSOCKET: WARNING: Connection to " + webSocket.getRemoteSocketAddress() + " before player was created!" + RESET);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        if (s.toLowerCase().startsWith("{") && s.toLowerCase().endsWith("}")) {
            MessageWrapper messageWrapper = gson.fromJson(s, MessageWrapper.class);
            Class<?> type = messageWrapper.getType();
            Object contents = gson.fromJson(gson.toJson(messageWrapper.getContents()), type);
            if (contents instanceof ClientPlayer clientPlayer){
                for (Player player : players) {
                    System.out.println(GREEN + "WEBSOCKET: INFO: Checking for update by player " + clientPlayer.name + " " + clientPlayer.id + " checking: " + player.name + " " + player.id);
                    if (player.ClientPlayer.id == clientPlayer.id -1) {
                        player.position = clientPlayer.position;
                        player.lastUpdate = System.currentTimeMillis();
                        System.out.println(GREEN + "WEBSOCKET: INFO: Player " + clientPlayer.id + " at address: " + webSocket.getRemoteSocketAddress() + " updated position to " + clientPlayer.position.toString());
                        return;
                    }
                }
                Player newPlayer =new Player(clientPlayer.name,clientPlayer.position, webSocket, clientPlayer.id);
                players.add(newPlayer);
                webSocket.send(gson.toJson(new MessageWrapper(Id.class, new Id(players.size()))));
                System.out.println(GREEN + "WEBSOCKET: INFO: New player " + clientPlayer.id + " " + clientPlayer.name + " at address:" + webSocket.getRemoteSocketAddress() + " spawned in at " + clientPlayer.position.toString());

            }
            if (contents instanceof miscReq){
                miscReq req = (miscReq) contents;
                if (req.request == "players"){
                    List<ClientPlayer> clientPlayers = new ArrayList<ClientPlayer>();
                    for (Player player : players){
                        clientPlayers.add(player.ClientPlayer);
                    }
                    webSocket.send(gson.toJson(new MessageWrapper(playersList.class, clientPlayers)));
                }
            }
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        webSocket.close();
        e.printStackTrace();
        System.err.println(RED + "WEBSOCKET: ERROR: " + e.getMessage() + " from " + webSocket.getRemoteSocketAddress() + RESET);
    }

    @Override
    public void onStart() {
        System.out.println(GREEN + "WEBSOCKET: INFO: Starting WS server on address: " + getAddress() + RESET);
        System.out.println(GREEN + "WEBSOCKET: INFO: Checking config file"+ RESET);
        if (config == null){
            System.err.println(RED + "WEBSOCKET: ERROR: No config file was found, using defualts" + RESET);
            config = new Config();
        }else{
            System.out.println(GREEN + "WEBSOCKET: INFO: Config file check Successful"+ RESET);
        }
        System.out.println(GREEN + "WEBSOCKET: INFO: Applying configs"+ RESET);
        StoragePaths.changeSaveGameDir(Paths.get(config.getNewTerrainGen().getStoragePaths().getSaveFilePath()));
        StoragePaths.changeSettingsDir(Paths.get(config.getNewTerrainGen().getStoragePaths().getSettingsPath()));
        StoragePaths.changeModelsDir(Paths.get(config.getNewTerrainGen().getStoragePaths().getModelsPath()));
        StoragePaths.changeTexturesDir(Paths.get(config.getNewTerrainGen().getStoragePaths().getTexturesPath()));
        System.out.println(GREEN + "WEBSOCKET: INFO: Configs applied");
        System.out.println(GREEN + "WEBSOCKET: INFO: Starting terrain generator"+ RESET);
        world = new AnimaliaWorld(appContext, WorldType.EARTH, config.getNewTerrainGen().getSeed(), config.getNewTerrainGen().getSaveFile().getSaveFileName());
        System.out.println(GREEN + "WEBSOCKET: INFO: Terrain generator started"+ RESET);
        System.out.println(GREEN + "WEBSOCKET: INFO: Starting services:" + RESET);
        offlinePlayerChecker = new OfflinePlayerChecker(config.getServer().getIdlePingTimeout(), config.getServer().getPingCheckInterval());
        Thread th = new Thread(offlinePlayerChecker);
        th.start();
        System.out.println(GREEN + "WEBSOCKET: INFO: Services started" + RESET);
        System.out.println(GREEN + "WEBSOCKET: INFO: Server is up at " + getAddress() + RESET);

    }

    public World getWorld(){return this.world;}
    class OfflinePlayerChecker implements Runnable {

        float idleTimeoutDelay = 30000;
        float pingCheckInterval = 5000;
        boolean running = true;

        public OfflinePlayerChecker() {

        }

        public OfflinePlayerChecker(float idleTimeoutDelay, float pingCheckInterval) {
            this.idleTimeoutDelay = idleTimeoutDelay;
            this.pingCheckInterval = pingCheckInterval;
        }

        public void stop(){
            running = false;
        }

        @Override
        public void run() {
            System.out.println(GREEN + "TIMEOUTKICK: INFO: Started timeout checker" + RESET);
            while (running) {
                try {
                    // Check for offline players
                    System.out.println(GREEN + "TIMEOUTKICK: INFO: Checking for offline players..." + RESET);
                    for (int i = 0; i < players.size(); i++) {
                        Player user = players.get(i);
                        System.out.printf(GREEN + "    INFO: Checking %s, lastUpdate at %s, current time %s, difference %s, should time out: %s\n" + RESET, user.name, user.lastUpdate, System.currentTimeMillis(), System.currentTimeMillis() - user.lastUpdate, user.lastUpdate + idleTimeoutDelay < System.currentTimeMillis());
                        if (user.lastUpdate + idleTimeoutDelay < System.currentTimeMillis()) {
                            players.remove(i);
                            user.connection.close(0, "idleTimeOut");
                            System.out.println(YELLOW + "TIMEOUTKICK: WARNING: Player " + user.name + " timed out" + RESET);
                        }
                    }
                    Thread.sleep((long) pingCheckInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(RED + "TIMEOUTKICK: INFO: Stopping thread" + RESET);
        }
    }

}
class Player{
    public String name;
    public Vector3f position;
    public long lastUpdate;
    public WebSocket connection;
    public ClientPlayer ClientPlayer;
    public int id;
    public Player(String name, Vector3f position, WebSocket connection, int id){
        this.name = name;
        this.position = position;
        this.lastUpdate = System.currentTimeMillis();
        this.connection = connection;
        this.ClientPlayer = new ClientPlayer(name, position, id);
        this.id = id;
    }

}
class ClientPlayer {
    public String name;
    public Vector3f position;
    public int id;

    public ClientPlayer(String name, Vector3f position, int id) {
        this.name = name;
        this.position = position;
        this.id = id;
    }
}
