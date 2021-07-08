package me.jackz.simplebungee.managers;

import com.sun.javaws.exceptions.MissingFieldException;
import me.jackz.simplebungee.SimpleBungee;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataManager {
    private final SimpleBungee plugin;

    private boolean mysql_enabled;
    private Connection connection;
    private String host, database, username, password;
    private int port;

    public DataManager(SimpleBungee plugin) throws MissingFieldException {
        this.plugin = plugin;

        this.mysql_enabled = plugin.getConfig().getBoolean("use-mysql",false);
        if(mysql_enabled) {
            this.host = plugin.getConfig().getString("mysql.host");
            this.database = plugin.getConfig().getString("mysql.database");
            this.username = plugin.getConfig().getString("mysql.username","");
            this.password = plugin.getConfig().getString("mysql.password","");
            this.port = plugin.getConfig().getInt("mysql.port",3306);

            if(host == null ) {
                //possibly throw error
                plugin.getLogger().severe("Mysql is enabled, but host is not defined. ");
                throw new MissingFieldException("Host or database field is undefined.","host");
            }else if(database == null){
                plugin.getLogger().severe("Mysql is enabled, but database is not defined. ");
                throw new MissingFieldException("Host or database field is undefined.","database");
            }
            try {
                openConnection();
                Statement statement = connection.createStatement();
            } catch (ClassNotFoundException | SQLException e) {
                plugin.getLogger().severe("Exception occurred while opening mysql connection. " + e.getMessage());
            }
        }
    }

    public void save() throws SQLException, IOException {
        if(mysql_enabled) {
            checkForTables();
            connection.createStatement();
            //idk
        }else{
            plugin.saveData();
        }
    }

    private void checkForTables() throws SQLException {
        connection.createStatement().executeQuery("CREATE TABLE IF NOT EXISTS `sb_friends`( `player` VARCHAR(32) NOT NULL, `friend` VARCHAR(32), PRIMARY KEY(`player`) ); CREATE TABLE IF NOT EXISTS `sb_requests`( `player` VARCHAR(32) NOT NULL, `friend` VARCHAR(32), PRIMARY KEY(`player`) ); CREATE TABLE IF NOT EXISTS `sb_notes`( `player` VARCHAR(32) NOT NULL, `created` BIGINT, `name` VARCHAR(32), `text` TEXT(), PRIMARY KEY(`player`));");
    }

    private void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
}
