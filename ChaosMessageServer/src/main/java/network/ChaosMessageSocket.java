package network;

import main.Main;
import network.packets.MessageObject;
import util.Logging;
import yapion.YAPIONUtils;
import yapion.hierarchy.types.YAPIONObject;
import yapion.packet.YAPIONInputStream;
import yapion.packet.YAPIONOutputStream;
import yapion.packet.YAPIONPacket;
import yapion.packet.YAPIONPacketReceiver;
import yapion.serializing.YAPIONDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.logging.Level;

public class ChaosMessageSocket {

    private final Socket socket;
    public final OutputStream out;
    public final YAPIONOutputStream yout;
    public final InputStream in;
    public final YAPIONInputStream yin;
    private YAPIONPacketReceiver receiver = new YAPIONPacketReceiver();
    public PublicKey key;
    private Boolean connected;

    public ChaosMessageSocket(Socket socket) throws IOException {
        this.socket = socket;
        out = socket.getOutputStream();
        in = socket.getInputStream();
        yout = new YAPIONOutputStream(out);
        yin = new YAPIONInputStream(in);
        yin.setYAPIONPacketReceiver(receiver);
        connected = false;
        keysync();
    }

    private synchronized void keysync() {
        YAPIONPacket packet = new YAPIONPacket("login");
        packet.add("publicKey", Main.getServer().getPublicKey());
        System.out.println(packet.getYAPION().toString());
        yout.write(packet);
        receiver.add("login", yapionPacket -> {
            MessageObject object = (MessageObject) yapionPacket.get("message");
            YAPIONObject yapionObject = YAPIONUtils.stringGetAllYAPIONObjects(object.getMessage()).get(0);
            key = (PublicKey) YAPIONDeserializer.deserialize(yapionObject.getObject("publicKey"));
            Logging.log(Level.INFO, "Connected with Client! " + socket.getInetAddress().getHostAddress());
            postKeysync(true);
        });
        receiver.add("@exception", yapionPacket -> {
            ((Exception) yapionPacket.get("@exception")).printStackTrace();
            postKeysync(false);
        });
        receiver.add("@error", yapionPacket -> {
            postKeysync(false);
        });
    }

    private void postKeysync(boolean succesfull) {

    }
}
