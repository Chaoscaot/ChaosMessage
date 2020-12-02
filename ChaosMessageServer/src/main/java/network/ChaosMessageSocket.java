package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ChaosMessageSocket {

    private final Socket socket;
    public final OutputStream out;
    public final InputStream in;

    public ChaosMessageSocket(Socket socket) throws IOException {
        this.socket = socket;
        out = socket.getOutputStream();
        in = socket.getInputStream();
    }
}
