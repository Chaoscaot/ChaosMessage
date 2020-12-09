package network;

import main.Main;
import network.packets.MessageObject;
import sql.Token;
import sql.User;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ChaosMessageSocket {

    private static final Map<Integer, ChaosMessageSocket> sockets = new HashMap<>();

    public static ChaosMessageSocket getSocket(int userId) {
        return sockets.getOrDefault(userId, null);
    }

    public static ChaosMessageSocket getSocket(User user) {
        return sockets.getOrDefault(user.getId(), null);
    }

    private final Socket socket;
    public final OutputStream out;
    public final YAPIONOutputStream yout;
    public final InputStream in;
    public final YAPIONInputStream yin;
    private YAPIONPacketReceiver receiver = new YAPIONPacketReceiver();
    public PublicKey key;
    private Boolean connected;
    private int userId;

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
        YAPIONPacket packet = new YAPIONPacket("keysync");
        packet.add("publicKey", Main.getServer().getPublicKey());
        packet.add("uuid", Main.getServer().getId());
        yout.write(packet);
        Logging.log(Level.INFO, "Syncing key with " + socket.getInetAddress().getHostAddress());
        receiver.add("keysync", yapionPacket -> {
            MessageObject object = (MessageObject) yapionPacket.get("message");
            YAPIONObject yapionObject = YAPIONUtils.stringGetAllYAPIONObjects(object.getMessage()).get(0);
            key = (PublicKey) YAPIONDeserializer.deserialize(yapionObject.getObject("publicKey"));
            Logging.log(Level.INFO, "Connected with Client! " + socket.getInetAddress().getHostAddress());
            postKeysync(true, yapionObject);
        });
        receiver.add("@exception", yapionPacket -> {
            ((Exception) yapionPacket.get("@exception")).printStackTrace();
            postKeysync(false, null);
        });
        receiver.add("@error", yapionPacket -> {
            postKeysync(false, null);
        });
        refreshReceiver();
    }

    private void postKeysync(boolean succesfull, YAPIONObject packet) {
        System.out.println(packet.toString());
        if(succesfull) {
            YAPIONObject returnPacket = new YAPIONObject();
            if(packet.getValue("type", String.class).get().equals("token")) {
                String tokenString = packet.getValue("token", String.class).get();
                Token token = Token.getTokenFromToken(tokenString);
                if(token != null) {
                    returnPacket.add("succesfull", true);
                    sockets.put(token.getUserId(), this);
                    userId = token.getUserId();
                }else {
                    returnPacket.add("succesfull", false);
                    returnPacket.add("error", "invalid token");
                    Main.getServer().addFailedLogin(socket.getInetAddress().getHostAddress());
                }
            }else if(packet.getValue("type", String.class).get().equals("password")){
                String uuid = packet.getValue("uuid", String.class).get();
                String password = packet.getValue("password", String.class).get();
                User user = User.getUserByUuid(UUID.fromString(uuid));
                if(user == null || !user.checkPassword(password)) {
                    returnPacket.add("succesfull", false);
                    returnPacket.add("error", "invalid user or password");
                    Main.getServer().addFailedLogin(socket.getInetAddress().getHostAddress());
                }else {
                    returnPacket.add("succesfull", true);
                    sockets.put(user.getId(), this);
                    userId = user.getId();
                }
            }else if(packet.getValue("type", String.class).get().equals("create")) {
                String username = packet.getValue("username", String.class).get();
                if(User.nameTaken(username)) {
                    returnPacket.add("succesfull", false);
                    returnPacket.add("error", "username taken");
                }else {
                    String password = packet.getValue("password", String.class).get();
                    User user = User.createUser(username, password);
                    this.userId = user.getId();
                    returnPacket.add("succesfull", true);
                }
            }
            MessageObject object = new MessageObject(returnPacket.toString(), key);
            YAPIONPacket packet1 = new YAPIONPacket("login");
            packet1.add("message", object);
            yout.write(packet1);
        }else {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshReceiver() {
        yin.setYAPIONPacketReceiver(receiver);
    }
}
