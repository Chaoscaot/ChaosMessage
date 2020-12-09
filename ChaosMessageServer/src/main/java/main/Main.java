package main;

import network.ChaosMessageServer;
import org.mindrot.jbcrypt.BCrypt;
import util.ArgumentMapper;
import util.ArgumentParser;
import util.Config;
import util.Logging;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Main {

    private static final ArgumentMapper argumentMap = new ArgumentMapper("-");
    private static ChaosMessageServer server;
    public static String cryptSalt;

    static {
        argumentMap.addValue("config", "config.json");
    }

    public static void main(String[] args) {
        ArgumentParser.ArgumentMapping mapping = ArgumentParser.parse(argumentMap, args);
        Config.loadConfig(mapping.getKey("config"));
        if(!new File("dont.delete").exists()) {
            try {
                firstStart();
            } catch (IOException e) {
                Logging.log("Could not init", e);
                System.exit(1);
            }
        }
        loadSalt();
        Logging.log(Level.INFO, "Starting Server...");
        server = new ChaosMessageServer(Config.PORT);
    }

    public static ChaosMessageServer getServer() {
        return server;
    }

    private static void loadSalt() {
        File saltFile = new File("password.salt");
        try {
            FileInputStream fis = new FileInputStream(saltFile);
            cryptSalt = new String(fis.readAllBytes());
            cryptSalt = new String(fis.readAllBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void firstStart() throws IOException {
        Logging.log(Level.FINEST, "First start, Init Database...");
        File saltFile = new File("password.salt");
        String salt = BCrypt.gensalt();
        if(!saltFile.exists())
            saltFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(saltFile);
        fos.write(salt.getBytes(StandardCharsets.UTF_8));
        fos.close();
        new File("dont.delete").createNewFile();
    }
}
