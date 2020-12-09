package util;

import main.Main;
import org.json.JSONObject;
import sql.SQL;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class Config {

    public static Integer PORT;
    public static DateTimeFormatter DATE_FORMAT;
    public static Integer MAX_FAILED_LOGINS;

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
                Logging.log(Level.FINEST, "Please Adjust the Configuration");
                System.exit(0);
            } catch (IOException e) {
                Logging.log("Could not create Config File", e);
                System.exit(1);
            }
        }

        try {
            JSONObject object = new JSONObject(getConfig(file));
            // TODO: 02.12.2020 Load Config
            PORT = object.getInt("port");
            DATE_FORMAT = DateTimeFormatter.ofPattern(object.getString("dateformat"));
            MAX_FAILED_LOGINS = object.getInt("failed logins");
            //SQL Connection
            JSONObject sql = (JSONObject) object.get("sql");
            SQL.connect(sql.getString("url"), sql.getString("user"), sql.getString("password"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getConfig(File file) throws IOException {
        FileReader reader = new FileReader(file);
        StringBuilder builder = new StringBuilder();
        while (reader.ready()) {
            builder.append((char) reader.read());
        }
        return builder.toString();
    }
}
