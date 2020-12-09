package sql;

import com.google.common.io.BaseEncoding;
import util.Logging;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Token {

    private static final Map<Integer, List<Token>> tokenCache = new HashMap<>();

    public static List<Token> getTokensFromUser(int userId) {
        if(tokenCache.containsKey(userId))
            return tokenCache.get(userId);
        ResultSet set = SQL.select("SELECT * FROM logintokens WHERE userId = ?", userId);
        try {
            List<Token> tokens = new ArrayList<>();
            while (set.next())
                tokens.add(new Token(set));
            return tokens;
        } catch (SQLException throwables) {
            Logging.log("Could not load Tokens for User " + userId, throwables);
            return new ArrayList<>();
        }
    }

    public static Token getTokenFromToken(String token) {
        ResultSet set = SQL.select("SELECT * FROM logintokens WHERE token = ?", token);
        try {
            if(!set.next())
                return null;
            return new Token(set);
        } catch (SQLException throwables) {
            Logging.log("Could not load Token " + token, throwables);
            return null;
        }
    }

    public static Token createToken(User user) {
        MessageDigest cript;
        try {
            cript = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException var4) {
            throw new SecurityException(var4);
        }

        cript.reset();
        cript.update((Instant.now().toString() + user.getUsername() + user.getPassword() + user.getUuid()).getBytes());
        String hash = BaseEncoding.base64().encode(cript.digest());
        Timestamp expire = new Timestamp(Timestamp.from(Instant.now()).toLocalDateTime().toLocalTime().plus(1, ChronoUnit.MONTHS).get(ChronoField.INSTANT_SECONDS));
        SQL.update("INSERT INTO logintokens (userId, token, expire) VALUES (?, ?, ?)", user.getId(), hash, expire);
        return getTokenFromToken(hash);
    }

    private final int userId;
    private final Timestamp timestamp;
    private final String token;
    private Timestamp expire;

    public Token(ResultSet set) throws SQLException {
        this.userId = set.getInt("userId");
        this.timestamp = set.getTimestamp("timestamp");
        this.token = set.getString("token");
        this.expire = set.getTimestamp("expire");
        if(!tokenCache.containsKey(userId))
            tokenCache.put(userId, new ArrayList<>());
        tokenCache.get(userId).add(this);
    }

    public int getUserId() {
        return userId;
    }

    public User getUser() {
        return User.getUserById(userId);
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getToken() {
        return token;
    }

    public Timestamp getExpire() {
        return expire;
    }

    public void extendExpire() {
        this.expire = new Timestamp(expire.toLocalDateTime().toLocalTime().plus(1, ChronoUnit.MONTHS).get(ChronoField.INSTANT_SECONDS));
        updateDb();
    }

    private void updateDb() {
        SQL.update("INSERT INTO logintokens (expire) VALUES (?) ON DUPLICATE KEY UPDATE expire = VALUES(expire)", expire);
    }
}
