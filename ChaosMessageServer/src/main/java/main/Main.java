package main;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class Main {

    private static Logger logger;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static void main(String[] args) {
        logger = Logger.getLogger("chaosmessage.logger");
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "[" + Timestamp.from(Instant.now()).toLocalDateTime().format(DATE_FORMAT) + "] [" + record.getLevel() + "] " + record.getMessage();
            }
        });

        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        log(Level.INFO, "Starting Server...");
    }

    public static void log(Level level, String message) {
        logger.log(level, message);
    }

    public static void log(Level level, String message, Object... objects) {
        logger.log(level, message, objects);
    }

    public static void log(String message) {
        log(Level.INFO, message);
    }

    public static void log(String message, Throwable e) {
        log(Level.SEVERE, message, e);
    }
}
