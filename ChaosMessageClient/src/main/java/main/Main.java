package main;

import network.packets.MessageObject;
import yapion.YAPIONUtils;
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
import java.security.interfaces.RSAPublicKey;

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
        receiver.add("keysync", yapionPacket -> {
            System.out.println(yapionPacket);
            sendToKey = (RSAPublicKey) yapionPacket.get("publicKey");
            YAPIONObject yapionObject = new YAPIONObject();
            yapionObject.add("publicKey", YAPIONSerializer.serialize(pair.getPublic()));
            yapionObject.add("type", "password");
            yapionObject.add("username", "Chaoscaot");
            yapionObject.add("password", "Test123");
            System.out.println(yapionObject.toString());
            MessageObject object = new MessageObject(yapionObject.toString(), sendToKey);
            YAPIONPacket packet = new YAPIONPacket("keysync");
            packet.add("message", object);
            System.out.println(packet.getYAPION().toString());
            yout.write(packet);
        });
        receiver.add("login", yapionPacket -> {
            System.out.println(yapionPacket.getType() + " " + YAPIONUtils.stringGetAllYAPIONObjects(((MessageObject) yapionPacket.get("message")).getMessage()).get(0).toString());
        });
        receiver.add("@exception", yapionPacket -> {
            ((Exception) yapionPacket.get("@exception")).printStackTrace();
            System.out.println(yapionPacket);
        });
        receiver.add("@error", yapionPacket -> {
            System.out.println(yapionPacket);
        });
        yin.setYAPIONPacketReceiver(receiver);
        while (true) {}
    }
}
