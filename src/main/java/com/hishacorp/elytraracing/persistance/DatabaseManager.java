package com.hishacorp.elytraracing.persistance;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File dbFile = new File(plugin.getDataFolder(), "data.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        connection = DriverManager.getConnection(url);
        plugin.getLogger().info("Connected to SQLite database.");

        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // Races
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS races (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL
                );
            """);

            // Player stats per race
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS race_stats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    race_id INTEGER NOT NULL,
                    player_uuid TEXT NOT NULL,
                    best_time INTEGER,
                    finishes INTEGER DEFAULT 0,
                    FOREIGN KEY (race_id) REFERENCES races(id)
                );
            """);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
        }
    }

    public boolean raceExists(String raceName) {
        try {
            var conn = getConnection();
            try (var ps = conn.prepareStatement(
                    "SELECT 1 FROM races WHERE name = ?")) {
                ps.setString(1, raceName);
                return ps.executeQuery().next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
