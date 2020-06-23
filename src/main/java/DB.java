import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DB {
    Connection connection = null;
    Statement statement = null;
    public DB () {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:kursach.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("create table if not exists login (id integer, username string, password string)");
            statement.executeUpdate("create table if not exists records (id integer, username string, game string, score integer)");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
    private boolean checkUserExists(String username)
    {
        int count = 0;
        try {
            String query = "SELECT COUNT(*) FROM login WHERE username=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet res = statement.executeQuery();
            while (res.next()){
                count = res.getInt(1);
            }
            if (count==0)
                return false;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return true;
    }

    public boolean registerUser(String username, String password)
    {
        if (checkUserExists(username))
            return false;
        try {
            String query = "INSERT INTO login (username, password) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, org.apache.commons.codec.digest.DigestUtils.sha256Hex(password));
            statement.executeUpdate();
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean loginCheck(String username, String password)
    {
        int count = 0;
        try {
            String query = "SELECT COUNT(*) FROM login WHERE username=? AND password=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, org.apache.commons.codec.digest.DigestUtils.sha256Hex(password));
            ResultSet res = statement.executeQuery();
            while (res.next()){
                count = res.getInt(1);
            }
            if (count==0)
                return false;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return true;
    }

    private int getRecordForGame(String username, String game)
    {
        try {
            String query = "SELECT score FROM records WHERE username=? AND game=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, game);
            ResultSet res = statement.executeQuery();
            if(res.next())
            {
                return res.getInt("score");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return 0;
    }

    public boolean addRecord(String username, String game, int record)
    {
        int currrentRecord = getRecordForGame(username, game);
        if (currrentRecord > record || record==0)
            return false;
        try {
            String query;
            if (currrentRecord > 0) {
                query = "UPDATE records SET score = ? WHERE game = ? AND username = ?";
            }
            else {
                query = "INSERT INTO records (score, game, username) VALUES (?, ?, ?)";
            }
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, record);
            statement.setString(2, game);
            statement.setString(3, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return true;
    }

    public ArrayList<HashMap<String, String>> showRecordsForUser(String username)
    {
        ArrayList<HashMap<String,String>> a = new ArrayList<HashMap<String, String>>();
        try {
            String query = "SELECT * FROM records WHERE username=? ORDER BY score DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet res = statement.executeQuery();
            while (res.next())
            {
                HashMap<String,String> hm = new HashMap<String,String>();
                hm.put("score", res.getString("score"));
                hm.put("game", res.getString("game"));
                a.add(hm);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return a;
    }
    public ArrayList<HashMap<String, String>> showRecordsForGame(String game)
    {
        ArrayList<HashMap<String,String>> a = new ArrayList<HashMap<String, String>>();
        try {
            String query = "SELECT * FROM records WHERE game=? ORDER BY score DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, game);
            ResultSet res = statement.executeQuery();
            while (res.next())
            {
                HashMap<String,String> hm = new HashMap<String,String>();
                hm.put("score", res.getString("score"));
                hm.put("username", res.getString("username"));
                a.add(hm);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return a;
    }
    public ArrayList<HashMap<String, String>> showAllRecords()
    {
        ArrayList<HashMap<String,String>> a = new ArrayList<HashMap<String, String>>();
        try {
            String query = "SELECT * FROM records ORDER BY score DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet res = statement.executeQuery();
            while (res.next())
            {
                HashMap<String,String> hm = new HashMap<String,String>();
                hm.put("score", res.getString("score"));
                hm.put("game", res.getString("game"));
                hm.put("username", res.getString("username"));
                a.add(hm);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return a;
    }
}
