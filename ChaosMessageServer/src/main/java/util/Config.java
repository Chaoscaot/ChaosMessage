package util;

import main.Main;

import java.io.*;
import java.util.logging.Level;

public class Config {

    public static void loadConfig(String config) {
        System.out.println("config = " + config);
        File file = new File(config);
        if(!file.canRead()) {
            try {
                file.setReadable(true);
            }catch (SecurityException e) {
                Logging.log(Level.SEVERE, "Could not read Config File");
                System.exit(1);
            }
        }

        if(!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                InputStream reader = Main.class.getResourceAsStream("/standard.json");
                while (reader.available() > 0) {
                    writer.write(reader.read());
                }
                writer.flush();
                reader.close();
                writer.close();
            } catch (IOException e) {
                Logging.log("Could not create Config File", e);
                System.exit(1);
            }
        }

    }
}
