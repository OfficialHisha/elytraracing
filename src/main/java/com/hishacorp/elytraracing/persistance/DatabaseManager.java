package com.hishacorp.elytraracing.persistance;

import com.hishacorp.elytraracing.persistance.data.RaceStat;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
                    name TEXT UNIQUE NOT NULL,
                    world TEXT NOT NULL
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
            try (var ps = connection.prepareStatement(
                    "SELECT 1 FROM races WHERE name = ?")) {
                ps.setString(1, raceName);
                return ps.executeQuery().next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check if race exists: " + e.getMessage());
            return false;
        }
    }

    public synchronized void createRace(String raceName, String world) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO races (name, world) VALUES (?, ?)")) {
            ps.setString(1, raceName);
            ps.setString(2, world);
            ps.executeUpdate();
        }
    }

    public synchronized int deleteRace(String raceName) throws SQLException {
        try (var ps = connection.prepareStatement(
                "DELETE FROM races WHERE name = ?")) {
            ps.setString(1, raceName);
            return ps.executeUpdate();
        }
    }

    public synchronized RaceStat getTopTimeByWorld(String world, int position) {
        try (var ps = connection.prepareStatement("""
            SELECT player_uuid, best_time FROM race_stats
            JOIN races ON race_stats.race_id = races.id
            WHERE races.world = ?
            ORDER BY best_time ASC
            LIMIT 1
            OFFSET ?
        """)) {
            ps.setString(1, world);
            ps.setInt(2, position - 1);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new RaceStat(
                        rs.getString("player_uuid"),
                        rs.getInt("best_time")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top time: " + e.getMessage());
        }
        return null;
    }
}
