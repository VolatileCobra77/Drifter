package ca.volatilecobra.Server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    static public GameWebSocketServer server;

    public static void main(String[] args) {
        final String RESET = "\u001B[0m";
        final String RED = "\u001B[31m";
        final String GREEN = "\u001B[32m";
        final String YELLOW = "\u001B[33m";
        String configFilePath = null;
        String ip = "localhost";
        int port = 8080;
        for (int i = 0; i < args.length; i++){
            if (args[i].equals("-c") || args[i].equals("--config")){
                configFilePath = args[i+1];
                System.out.println("INITALIZER: config file path: " + configFilePath);
            }
        }
        try{
            JsonObject settingsFile = (JsonObject) new JsonParser().parse(new FileReader(configFilePath));
            ip = settingsFile.get("Server").getAsJsonObject().get("address").getAsJsonObject().get("ip").getAsString();
            port = settingsFile.get("Server").getAsJsonObject().get("address").getAsJsonObject().get("port").getAsInt();
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("INITALIZER: ERROR: An IOException occurred while reading config file, defaulting to localhost:8080 for address");
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("INITALIZER: ERROR: " + e.getMessage() +" occured while reading config file, defaulting to localhost:8080 for address");
        }
        server = new GameWebSocketServer(new InetSocketAddress(ip, port), configFilePath);
        server.start();

    }
}