package ca.volatilecobra;

import java.net.InetSocketAddress;

public class Server {
    static public GameWebSocketServer server;

    public static void main(String[] args) {
        server = new GameWebSocketServer(new InetSocketAddress("localhost", 8080));
        server.start();

    }
}