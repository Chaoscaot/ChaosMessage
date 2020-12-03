package network;

import util.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        File privateFile = new File("private.key");
        File publicFile = new File("public.key");
        if(!privateFile.exists() || !publicFile.exists()) {
            try {
                privateFile.createNewFile();
                publicFile.createNewFile();
                createNewKeys();
            } catch (Exception e) {
                Logging.log("Could not Create Key Files", e);
                e.printStackTrace();
                System.exit(1);
            }
        }
        try {
            readKeys();
        } catch (Exception e) {
            Logging.log("Could not Load Keys", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createNewKeys() throws NoSuchAlgorithmException, IOException {
        KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream("public.key");
        fos.write(x509EncodedKeySpec.getEncoded());
        fos.close();

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        fos = new FileOutputStream("private.key");
        fos.write(pkcs8EncodedKeySpec.getEncoded());
        fos.close();
    }

    private void readKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Read Public Key.
        File filePublicKey = new File("public.key");
        FileInputStream fis = new FileInputStream(filePublicKey);
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        fis.close();

        // Read Private Key.
        File filePrivateKey = new File("private.key");
        fis = new FileInputStream(filePrivateKey);
        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
        fis.read(encodedPrivateKey);
        fis.close();

        // Generate KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        keyPair = new KeyPair(publicKey, privateKey);
    }

    public List<ChaosMessageSocket> getSockets() {
        return sockets;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}
