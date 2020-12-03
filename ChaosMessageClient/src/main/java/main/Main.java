package main;

import network.MessageObject;
import yapion.hierarchy.types.YAPIONObject;
import yapion.packet.YAPIONInputStream;
import yapion.packet.YAPIONOutputStream;
import yapion.packet.YAPIONPacket;
import yapion.packet.YAPIONPacketReceiver;
import yapion.serializing.YAPIONDeserializer;
import yapion.serializing.YAPIONSerializer;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Main {

    public static KeyPair pair;
    public static PublicKey sendToKey;

    static {
        try {
            pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 9547);
        YAPIONInputStream yin = new YAPIONInputStream(socket.getInputStream());
        YAPIONOutputStream yout = new YAPIONOutputStream(socket.getOutputStream());
        YAPIONPacketReceiver receiver = new YAPIONPacketReceiver();
        receiver.add("login", yapionPacket -> {
            sendToKey = (PublicKey) YAPIONDeserializer.deserialize(yapionPacket.getYAPION().getObject("publicKey"));
            YAPIONObject yapionObject = new YAPIONObject();
            yapionObject.add("publicKey", YAPIONSerializer.serialize(pair.getPublic()));
            MessageObject object = new MessageObject(yapionObject.toString(), sendToKey);
            YAPIONPacket packet = new YAPIONPacket("login");
            yout.write(packet);
        });
        yin.setYAPIONPacketReceiver(receiver);
        while (true) {}
    }
}
