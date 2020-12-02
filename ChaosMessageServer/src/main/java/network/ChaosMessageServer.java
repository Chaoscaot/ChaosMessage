package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChaosMessageServer {

    private ServerSocket server;
    private List<Socket> sockets;

    public ChaosMessageServer(int port) {
        sockets = new ArrayList<>();
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
