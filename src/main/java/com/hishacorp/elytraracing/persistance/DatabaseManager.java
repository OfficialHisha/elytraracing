package com.hishacorp.elytraracing.persistance;

import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

            // Rings
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS rings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    race_id INTEGER NOT NULL,
                    world TEXT NOT NULL,
                    x REAL NOT NULL,
                    y REAL NOT NULL,
                    z REAL NOT NULL,
                    radius REAL NOT NULL,
                    orientation TEXT NOT NULL,
                    material TEXT NOT NULL,
                    ring_index INTEGER NOT NULL,
                    FOREIGN KEY (race_id) REFERENCES races(id)
                );
            """);
        }
    }

    public synchronized int getRaceId(String raceName) throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT id FROM races WHERE name = ?")) {
            ps.setString(1, raceName);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
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

    public synchronized void createRace(String raceName) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO races (name) VALUES (?)")) {
            ps.setString(1, raceName);
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

    public synchronized int createRing(Ring ring) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO rings (race_id, world, x, y, z, radius, orientation, material, ring_index) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ring.getRaceId());
            ps.setString(2, ring.getLocation().getWorld().getName());
            ps.setDouble(3, ring.getLocation().getX());
            ps.setDouble(4, ring.getLocation().getY());
            ps.setDouble(5, ring.getLocation().getZ());
            ps.setDouble(6, ring.getRadius());
            ps.setString(7, ring.getOrientation().name());
            ps.setString(8, ring.getMaterial().name());
            ps.setInt(9, ring.getIndex());
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }

    public synchronized Ring getRing(int ringId) throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT * FROM rings WHERE id = ?")) {
            ps.setInt(1, ringId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Ring(
                        rs.getInt("id"),
                        rs.getInt("race_id"),
                        new Location(
                                Bukkit.getWorld(rs.getString("world")),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z")
                        ),
                        rs.getDouble("radius"),
                        Ring.Orientation.valueOf(rs.getString("orientation")),
                        Material.valueOf(rs.getString("material")),
                        rs.getInt("ring_index")
                );
            }
            return null;
        }
    }

    public synchronized List<Ring> getRings(int raceId) throws SQLException {
        List<Ring> rings = new ArrayList<>();
        try (var ps = connection.prepareStatement(
                "SELECT * FROM rings WHERE race_id = ? ORDER BY ring_index")) {
            ps.setInt(1, raceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rings.add(new Ring(
                        rs.getInt("id"),
                        rs.getInt("race_id"),
                        new Location(
                                Bukkit.getWorld(rs.getString("world")),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z")
                        ),
                        rs.getDouble("radius"),
                        Ring.Orientation.valueOf(rs.getString("orientation")),
                        Material.valueOf(rs.getString("material")),
                        rs.getInt("ring_index")
                ));
            }
        }
        return rings;
    }

    public synchronized void updateRing(Ring ring) throws SQLException {
        try (var ps = connection.prepareStatement(
                "UPDATE rings SET race_id = ?, world = ?, x = ?, y = ?, z = ?, radius = ?, orientation = ?, material = ?, ring_index = ? WHERE id = ?")) {
            ps.setInt(1, ring.getRaceId());
            ps.setString(2, ring.getLocation().getWorld().getName());
            ps.setDouble(3, ring.getLocation().getX());
            ps.setDouble(4, ring.getLocation().getY());
            ps.setDouble(5, ring.getLocation().getZ());
            ps.setDouble(6, ring.getRadius());
            ps.setString(7, ring.getOrientation().name());
            ps.setString(8, ring.getMaterial().name());
            ps.setInt(9, ring.getIndex());
            ps.setInt(10, ring.getId());
            ps.executeUpdate();
        }
    }

    public synchronized void deleteRing(int ringId) throws SQLException {
        try (var ps = connection.prepareStatement(
                "DELETE FROM rings WHERE id = ?")) {
            ps.setInt(1, ringId);
            ps.executeUpdate();
        }
    }
}
