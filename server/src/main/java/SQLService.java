import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class SQLService implements Serializable {

    private static Connection connection;
    private static String URL = "jdbc:mysql://127.0.0.1:3306/storage";
    private static String USERNAME = "root";
    private static String PASSWORD = "123456";


    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        return connection;
    }


    public void signUpUser(NewUser newUser) {
        String insert = "INSERT INTO users(nick,login,pass) VALUES(?,?,?)";
        try {
            PreparedStatement preparedSt = getConnection().prepareStatement(insert);
            preparedSt.setString(1, newUser.getNick());
            preparedSt.setString(2, newUser.getLogin());
            preparedSt.setString(3, newUser.getPassword());
            preparedSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public String getNickByLoginAndPass(String login, String pass) {
        String select = String.format("SELECT nick FROM users WHERE login = '%s' and pass = '%s'", login, pass);
        try {
            PreparedStatement preparedSt = getConnection().prepareStatement(select);
            ResultSet resultSet = preparedSt.executeQuery();
            if (resultSet.next()) {
                String s = resultSet.getString(1);
                return s;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<String> getNickGivingAccess(String toNick, String access) {
        String fromNick = String.format("SELECT nick FROM users WHERE id IN (SELECT from_user FROM users_access " +
                "INNER JOIN (users u, access a) ON (u.id = to_user AND a.id = id_access) WHERE nick = '%s' AND type = '%s')", toNick, access);
        try {
            PreparedStatement statement = getConnection().prepareStatement(fromNick);
            boolean isResultSet = statement.execute();
            List<String> list = new ArrayList<>();
            while (true) {
                if (isResultSet) {
                    try (ResultSet rs = statement.getResultSet()) {
                        while (rs.next()) {
                            list.add(rs.getString("nick"));
                        }
                        return list;
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void openAccess(String fromUser, String toUser, String access) {
        String from = String.format("SELECT id FROM users WHERE nick = '%s'", fromUser);
        String to = String.format("SELECT id FROM users WHERE nick = '%s'", toUser);
        String idAccess = String.format("SELECT id FROM access WHERE type = '%s'", access);
        String insert = "INSERT INTO users_access(from_user,to_user,id_access) VALUES(?,?,?)";
        try {
            PreparedStatement preparedSt = getConnection().prepareStatement(insert);
            preparedSt.setInt(1, transferStringToId(from));
            preparedSt.setInt(2, transferStringToId(to));
            preparedSt.setInt(3, transferStringToId(idAccess));
            preparedSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private int transferStringToId(String param) {
        try {
            PreparedStatement preparedSt = getConnection().prepareStatement(param);
            ResultSet resultSet = preparedSt.executeQuery();
            if (resultSet.next()) {
                int s = resultSet.getInt(1);
                return s;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}


