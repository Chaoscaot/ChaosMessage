package util;

import main.Main;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.logging.Level;

public class Config {

    public static Integer PORT;

    public static void loadConfig(String config) {
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
                Logging.log(Level.INFO, "Loading Standard Config!");
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

        try {
            JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));
            // TODO: 02.12.2020 Load Config
            PORT = ((Long) object.get("port")).intValue();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            Logging.log("Could not Parse JSON", e);
        }
    }
}
