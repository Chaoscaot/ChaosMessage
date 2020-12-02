package util;

import main.Main;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.logging.*;

public class Logging {

    private static final Logger logger;

    static {
        logger = java.util.logging.Logger.getLogger("chaosmessage.logger");
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "[" + Timestamp.from(Instant.now()).toLocalDateTime().format(Main.DATE_FORMAT) + "] [" + record.getLevel() + "] " + record.getMessage() + "\n";
            }
        });

        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
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
