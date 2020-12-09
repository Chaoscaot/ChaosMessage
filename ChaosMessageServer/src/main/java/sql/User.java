package sql;

import com.mysql.jdbc.log.Log;
import main.Main;
import org.mindrot.jbcrypt.BCrypt;
import util.Logging;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class User {

    private static final Map<UUID, User> uuidUserCache = new HashMap<>();
    private static final Map<Integer, User> idUserCache = new HashMap<>();
    private static final Map<String, User> nameUserCache = new HashMap<>();

    public static void clearCache() {
        uuidUserCache.clear();
        idUserCache.clear();
        nameUserCache.clear();
    }

    public static User getUserByName(String username) {
        if(nameUserCache.containsKey(username))
            return nameUserCache.get(username);
        else {
            ResultSet set = SQL.select("SELECT id, uuid, username FROM userdata WHERE username = ?", username);
            try {
                if(!set.next())
                    return null;
                return new User(set);
            } catch (SQLException throwables) {
                Logging.log("Could not Execute SQL Statement", throwables);
                return null;
            }
        }
    }

    public static User getUserById(int id) {
        if(idUserCache.containsKey(id))
            return idUserCache.get(id);
        else {
            ResultSet set = SQL.select("SELECT id, uuid, username FROM userdata WHERE id = ?", id);
            try {
                if(!set.next())
                    return null;
                return new User(set);
            } catch (SQLException throwables) {
                Logging.log("Could not Execute SQL Statement", throwables);
                return null;
            }
        }
    }

    public static User getUserByUuid(UUID uuid) {
        if(uuidUserCache.containsKey(uuid))
            return uuidUserCache.get(uuid);
        else {
            ResultSet set = SQL.select("SELECT id, uuid, username FROM userdata WHERE uuid = ?", uuid.toString());
            try {
                if(!set.next())
                    return null;
                return new User(set);
            } catch (SQLException throwables) {
                Logging.log("Could not Execute SQL Statement", throwables);
                return null;
            }
        }
    }

    public static User createUser(String username, String password) {
        UUID uuid = UUID.randomUUID();
        String hash = BCrypt.hashpw(password, Main.cryptSalt);
        SQL.update("INSERT INTO userdata (uuid, username, password) VALUES (?, ?, ?)", uuid.toString(), username, hash);
        return getUserByUuid(uuid);
    }

    public static boolean nameTaken(String username) {
        ResultSet set = SQL.select("SELECT count(*) AS count FROM userdata WHERE username = ?", username);
        try {
            if(!set.next())
                return true;
            return set.getInt("count") >= 1;
        } catch (SQLException throwables) {
            Logging.log("Could not Execute SQL Statement", throwables);
            return true;
        }
    }

    public static List<User> searchUsersByName(String username) {
        ResultSet set = SQL.select("SELECT id, uuid, username FROM userdata WHERE username LIKE '" + username + "%'");
        try {
            List<User> users = new ArrayList<>();
            while (set.next()) {
                users.add(new User(set));
            }
            return users;
        } catch (SQLException throwables) {
            Logging.log("Could not Execute SQL Statement", throwables);
            return null;
        }
    }

    private final int id;
    private final UUID uuid;
    private String username;

    private User(ResultSet set) throws SQLException {
        id = set.getInt("id");
        uuid = UUID.fromString(set.getString("uuid"));
        username = set.getString("username");
        uuidUserCache.put(uuid, this);
        idUserCache.put(id, this);
        nameUserCache.put(username, this);
    }

    public String getPassword() {
        ResultSet set = SQL.select("SELECT password FROM userdata WHERE id = ?", id);
        try {
            if(!set.next()) {
                Logging.log(Level.SEVERE, "Could not fetch password of User " + id);
                return null;
            }
            return set.getString("password");
        } catch (SQLException throwables) {
            Logging.log("Could not Execute SQL Statement", throwables);
            return "";
        }
    }

    public boolean checkPassword(String input) {
        return BCrypt.hashpw(input, Main.cryptSalt).equals(getPassword());
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        updateDb();
    }

    public void updateDb() {
        SQL.update("INSERT INTO userdata (username) VALUES (?) ON DUPLICATE KEY UPDATE username = VALUES(username)", username);
    }
}
