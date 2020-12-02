package network;

import com.ibm.jvm.Log;
import util.Logging;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ChaosMessageServer {

    private ServerSocket server;
    private List<ChaosMessageSocket> sockets;
    private Thread thread;
    private KeyPair keyPair;

    public ChaosMessageServer(int port) {
        sockets = new ArrayList<>();
        try {
            server = new ServerSocket(port);
            initKeys();
            thread = new Thread(() -> {
                Logging.log(Level.INFO, "Listening on Port " + port);
                while (!server.isClosed()) {
                    try {
                        Socket socket = server.accept();
                        sockets.add(new ChaosMessageSocket(socket));
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

    private void initKeys() {
        File privateFile = new File(System.getProperty("user.home") + "/MessageServer/private.key");
        File publicFile = new File(System.getProperty("user.home") + "/MessageServer/public.key");
        if(!privateFile.exists() || !publicFile.exists()) {
            privateFile.getParentFile().mkdirs();
            try {
                privateFile.createNewFile();
                publicFile.createNewFile();
                createNewKeys(privateFile, publicFile);
            } catch (Exception e) {
                Logging.log("Could not Create Key Files", e);
                System.exit(1);
            }
        }
        try {
            keyPair = new KeyPair(readPublicKey(privateFile.getAbsolutePath()), readPrivateKey(privateFile.getAbsolutePath()));
        } catch (Exception e) {
            Logging.log("Could not Load Keys", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createNewKeys(File privateFile, File publicFile) throws NoSuchAlgorithmException, IOException {
        KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(pair.getPublic().getEncoded());
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(pair.getPrivate().getEncoded());
        writeFileBytes(publicSpec.getEncoded(), publicFile.getAbsolutePath());
        writeFileBytes(privateSpec.getEncoded(), privateFile.getAbsolutePath());
    }

    private void writeFileBytes(byte[] bytes, String filename) throws IOException {
        Path path = Paths.get(filename);
        Files.write(path, bytes);
    }

    private byte[] readFileBytes(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    private PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicSpec);
    }

    private PrivateKey readPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public List<ChaosMessageSocket> getSockets() {
        return sockets;
    }
}
