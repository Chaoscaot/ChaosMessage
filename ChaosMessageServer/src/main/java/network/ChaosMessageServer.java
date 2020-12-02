package network;

import util.Logging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ChaosMessageServer {

    private ServerSocket server;
    private List<Socket> sockets;
    private Thread thread;

    public ChaosMessageServer(int port) {
        sockets = new ArrayList<>();
        try {
            server = new ServerSocket(port);
            thread = new Thread(() -> {
                Logging.log(Level.INFO, "Listening on Port " + port);
                while (!server.isClosed()) {
                    try {
                        Socket socket = server.accept();
                        sockets.add(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            },"ServerThread-1");
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
