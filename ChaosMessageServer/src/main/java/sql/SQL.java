package sql;

import util.Logging;

import java.sql.*;

public class SQL {
    private SQL(){}

    private static Connection con;
    private static String url;
    private static String user;
    private static String password;

    public static void connect(String url, String user, String password) {
        SQL.url = url;
        SQL.user = user;
        SQL.password = password;
        try {
            con = DriverManager.getConnection(url + "?autoreconnect=true", user, password);
        }catch (SQLException e) {
            System.exit(0);
            throw new SecurityException("Could not start SQL-Exception", e);
        }
    }

    public static void close() {
        try {
            if(con != null)
                con.close();
        }catch (SQLException e) {
            Logging.log("Could not close SQL-Connection", e);
        }
    }

    public static void update(String qry, Object... objects) {
        try {
            prepare(con, qry, objects).executeUpdate();
        } catch (SQLException e) {
            sqlException();
            try (PreparedStatement st = con.prepareStatement(qry)) {
                st.executeUpdate();
            } catch (SQLException ex) {
                throw new SecurityException("Could not execute update statement", ex);
            }
        }
    }

    public static ResultSet select(String qry, Object... objects){
        try{
            return prepare(con, qry, objects).executeQuery();
        } catch (SQLException e) {
            sqlException();
            try {
                return prepare(con, qry, objects).executeQuery();
            } catch (SQLException ex) {
                throw new SecurityException("Could not run Select-Statement", ex);
            }
        }
    }

    private static PreparedStatement prepare(Connection connection, String qry, Object... objects) throws SQLException{
        PreparedStatement st = connection.prepareStatement(qry);
        for(int i = 0; i < objects.length; i++){
            st.setObject(i+1, objects[i]);
        }
        return st;
    }

    private static void sqlException(){
        close();
        connect(url, user, password);
    }
}
