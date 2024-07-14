package ca.volatilecobra;

import com.google.gson.JsonParseException;
import com.jme3.math.Vector3f;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.framing.Framedata;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import ca.volatilecobra.TerrainGenerator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GameWebSocketServer extends WebSocketServer {
    private static final Gson gson = new Gson();
    TerrainGenerator terrainGenerator;
    private List<player> players = new ArrayList<player>();

    public GameWebSocketServer(InetSocketAddress address) {
        super(address);
        this.terrainGenerator = new TerrainGenerator(512, 0.25f, 2f);
        terrainGenerator.generateTerrain();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

        conn.send(String.format("{connection:true}")); //send client Inital Data for this server, including Heightmap, ore positions, player positions, item positions, physics objects.
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Recieved message from " + conn.getRemoteSocketAddress().getAddress().getHostAddress() +" Message contents: " + message);
        if (message.startsWith("{terrainRequest:")){
            conn.send(String.format("{heightMap:%s}", gson.toJson(terrainGenerator.heightmap)));
        }else if (message.startsWith("{player:")){
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            for (player user:players){
                try {
                    if (user.id == json.get("id").getAsInt()) {
                        user.position = new Vector3f(json.get("position").getAsJsonObject().get("x").getAsFloat(),json.get("position").getAsJsonObject().get("y").getAsFloat(),json.get("position").getAsJsonObject().get("z").getAsFloat());
                        System.out.printf("Updating user Postion to %s\n", new Vector3f(json.get("position").getAsJsonObject().get("x").getAsFloat(),json.get("position").getAsJsonObject().get("y").getAsFloat(),json.get("position").getAsJsonObject().get("z").getAsFloat()));
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
               System.out.printf("Player Connected: %s, Position %s\n", user.name, user.position);
           }
            conn.send(String.format("{players:%s}", gson.toJson(clientPlayers)));
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // Handle error on connection
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server up on address " + this.getAddress());
        System.out.println("Starting Services...");
        Thread th = new Thread(new OfflinePlayerChecker());
        th.start();
    }
    private class OfflinePlayerChecker implements Runnable {
        @Override
        public void run() {
            System.out.println("Started timeout checker");
            while (true) {
                try {
                    // Check for offline players
                    System.out.println("Checking for offline players...");
                    for (int i = 0; i < players.size(); i++){
                        player user = players.get(i);
                        System.out.printf("    Checking %s, lastUpdate at %s, current time %s, difference %s\n", user.name, (int)user.lastUpdate, System.currentTimeMillis(), System.currentTimeMillis() - (int)user.lastUpdate);
                        if (user.lastUpdate + 30000 < System.currentTimeMillis()){
                            players.remove(user);
                            user.connection.close(0, "idleTimeOut");
                            System.out.println("Player " + user.name + " timed out");
                        }
                    }
                    Thread.sleep(5000); // Check every 5 seconds
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
    public float lastUpdate;
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
