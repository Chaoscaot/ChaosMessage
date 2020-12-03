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
import yapion.serializing.YAPIONSerializer;

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

    public ChaosMessageSocket(Socket socket) throws IOException {
        this.socket = socket;
        out = socket.getOutputStream();
        in = socket.getInputStream();
        yout = new YAPIONOutputStream(out);
        yin = new YAPIONInputStream(in);
        yin.setYAPIONPacketReceiver(receiver);
        login();
    }

    private void login() {
        YAPIONPacket packet = new YAPIONPacket("login");
        packet.add("publicKey", Main.getServer().getPublicKey());
        yout.write(packet);
        receiver.add("login", yapionPacket -> {
            MessageObject object = (MessageObject) YAPIONDeserializer.deserialize(yapionPacket.getYAPION().getObject("message"));
            YAPIONObject yapionObject = YAPIONUtils.stringGetAllYAPIONObjects(object.getMessage()).get(0);
            key = (PublicKey) YAPIONDeserializer.deserialize(yapionObject.getObject("publicKey"));
            Logging.log(Level.INFO, "Connected with Client! " + socket.getInetAddress().getHostAddress());
        });
    }
}
