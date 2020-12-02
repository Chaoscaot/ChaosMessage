package main;

import network.ChaosMessageServer;
import util.ArgumentMapper;
import util.ArgumentParser;
import util.Config;
import util.Logging;

import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class Main {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final ArgumentMapper argumentMap = new ArgumentMapper("-");
    private static ChaosMessageServer server;

    static {
        argumentMap.addValue("config", "config.json");
    }

    public static void main(String[] args) {
        ArgumentParser.ArgumentMapping mapping = ArgumentParser.parse(argumentMap, args);
        Config.loadConfig(mapping.getKey("config"));
        Logging.log(Level.INFO, "Starting Server...");
        server = new ChaosMessageServer(Config.PORT);
    }

    public static ChaosMessageServer getServer() {
        return server;
    }
}
