package com.hishacorp.elytraracing.persistance;

import com.hishacorp.elytraracing.persistance.data.RaceStat;
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
import java.sql.Statement;
import java.util.UUID;

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
                    world TEXT NOT NULL,
                    spawn_x REAL,
                    spawn_y REAL,
                    spawn_z REAL,
                    spawn_yaw REAL,
                    spawn_pitch REAL,
                    enabled INTEGER DEFAULT 1
                );
            """);

            // Add spawn columns if they don't exist
            String[] spawnColumns = {"spawn_x", "spawn_y", "spawn_z", "spawn_yaw", "spawn_pitch"};
            for (String col : spawnColumns) {
                try {
                    stmt.executeUpdate("ALTER TABLE races ADD COLUMN " + col + " REAL;");
                } catch (SQLException ignored) {}
            }

            // Add enabled column if it doesn't exist (for existing databases)
            try {
                stmt.executeUpdate("ALTER TABLE races ADD COLUMN enabled INTEGER DEFAULT 1;");
            } catch (SQLException ignored) {}

            // Player stats per race
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS race_stats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    race_id INTEGER NOT NULL,
                    player_uuid TEXT NOT NULL,
                    best_time INTEGER,
                    finishes INTEGER DEFAULT 0,
                    wins INTEGER DEFAULT 0,
                    rounds_played INTEGER DEFAULT 0,
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

            // Borders
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS borders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    race_id INTEGER NOT NULL,
                    pos1_x REAL NOT NULL,
                    pos1_y REAL NOT NULL,
                    pos1_z REAL NOT NULL,
                    pos2_x REAL NOT NULL,
                    pos2_y REAL NOT NULL,
                    pos2_z REAL NOT NULL,
                    FOREIGN KEY (race_id) REFERENCES races(id)
                );
            """);
        }
    }

    public synchronized int getRaceId(String raceName) throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT id FROM races WHERE name = ?")) {
            ps.setString(1, raceName.toLowerCase());
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        }
    }

    public synchronized String getRaceName(int raceId) throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT name FROM races WHERE id = ?")) {
            ps.setInt(1, raceId);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
            return null;
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
                ps.setString(1, raceName.toLowerCase());
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
            ps.setString(1, raceName.toLowerCase());
            ps.setString(2, world);
            ps.executeUpdate();
        }
    }

    public synchronized void updateRaceSpawn(String raceName, Location spawn) throws SQLException {
        try (var ps = connection.prepareStatement(
                "UPDATE races SET spawn_x = ?, spawn_y = ?, spawn_z = ?, spawn_yaw = ?, spawn_pitch = ? WHERE name = ?")) {
            if (spawn != null) {
                ps.setDouble(1, spawn.getX());
                ps.setDouble(2, spawn.getY());
                ps.setDouble(3, spawn.getZ());
                ps.setDouble(4, spawn.getYaw());
                ps.setDouble(5, spawn.getPitch());
            } else {
                ps.setNull(1, java.sql.Types.REAL);
                ps.setNull(2, java.sql.Types.REAL);
                ps.setNull(3, java.sql.Types.REAL);
                ps.setNull(4, java.sql.Types.REAL);
                ps.setNull(5, java.sql.Types.REAL);
            }
            ps.setString(6, raceName.toLowerCase());
            ps.executeUpdate();
        }
    }

    public synchronized int addBorder(int raceId, Location pos1, Location pos2) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO borders (race_id, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, raceId);
            ps.setDouble(2, pos1.getX());
            ps.setDouble(3, pos1.getY());
            ps.setDouble(4, pos1.getZ());
            ps.setDouble(5, pos2.getX());
            ps.setDouble(6, pos2.getY());
            ps.setDouble(7, pos2.getZ());
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }

    public synchronized void deleteBorder(int borderId) throws SQLException {
        try (var ps = connection.prepareStatement("DELETE FROM borders WHERE id = ?")) {
            ps.setInt(1, borderId);
            ps.executeUpdate();
        }
    }

    public synchronized void clearBorders(int raceId) throws SQLException {
        try (var ps = connection.prepareStatement("DELETE FROM borders WHERE race_id = ?")) {
            ps.setInt(1, raceId);
            ps.executeUpdate();
        }
    }

    public static class BorderData {
        public final int id;
        public final Location pos1;
        public final Location pos2;

        public BorderData(int id, Location pos1, Location pos2) {
            this.id = id;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }
    }

    public synchronized List<BorderData> getBorders(int raceId, org.bukkit.World world) throws SQLException {
        List<BorderData> borders = new ArrayList<>();
        try (var ps = connection.prepareStatement("SELECT * FROM borders WHERE race_id = ?")) {
            ps.setInt(1, raceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                borders.add(new BorderData(
                        rs.getInt("id"),
                        new Location(world, rs.getDouble("pos1_x"), rs.getDouble("pos1_y"), rs.getDouble("pos1_z")),
                        new Location(world, rs.getDouble("pos2_x"), rs.getDouble("pos2_y"), rs.getDouble("pos2_z"))
                ));
            }
        }
        return borders;
    }

    public static class RaceData {
        public final int id;
        public final String name;
        public final String world;
        public final Location spawn;
        public final boolean enabled;

        public RaceData(int id, String name, String world, Location spawn, boolean enabled) {
            this.id = id;
            this.name = name;
            this.world = world;
            this.spawn = spawn;
            this.enabled = enabled;
        }
    }

    public synchronized RaceData getRaceData(String raceName) throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT * FROM races WHERE name = ?")) {
            ps.setString(1, raceName.toLowerCase());
            var rs = ps.executeQuery();
            if (rs.next()) {
                String worldName = rs.getString("world");
                var world = Bukkit.getWorld(worldName);
                Location spawn = null;
                if (rs.getObject("spawn_x") != null) {
                    spawn = new Location(world, rs.getDouble("spawn_x"), rs.getDouble("spawn_y"), rs.getDouble("spawn_z"), (float) rs.getDouble("spawn_yaw"), (float) rs.getDouble("spawn_pitch"));
                }

                return new RaceData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        worldName,
                        spawn,
                        rs.getInt("enabled") == 1
                );
            }
            return null;
        }
    }

    public synchronized void setRaceEnabled(String raceName, boolean enabled) throws SQLException {
        try (var ps = connection.prepareStatement(
                "UPDATE races SET enabled = ? WHERE name = ?")) {
            ps.setInt(1, enabled ? 1 : 0);
            ps.setString(2, raceName.toLowerCase());
            ps.executeUpdate();
        }
    }

    public synchronized int deleteRace(String raceName) throws SQLException {
        try (var ps = connection.prepareStatement(
                "DELETE FROM races WHERE name = ?")) {
            ps.setString(1, raceName.toLowerCase());
            return ps.executeUpdate();
        }
    }

    public List<String> getAllRaceNames() {
        List<String> raceNames = new ArrayList<>();
        try (var ps = connection.prepareStatement("SELECT name FROM races")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                raceNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all race names: " + e.getMessage());
        }
        return raceNames;
    }

    public RaceStat getTopTimeByRace(String raceName, int position) {
        try (var ps = connection.prepareStatement(
                "SELECT player_uuid, best_time, wins, rounds_played FROM race_stats rs " +
                        "JOIN races r ON rs.race_id = r.id " +
                        "WHERE r.name = ? AND rs.best_time IS NOT NULL " +
                        "ORDER BY rs.best_time ASC LIMIT 1 OFFSET ?")) {
            ps.setString(1, raceName);
            ps.setInt(2, position - 1);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new RaceStat(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getLong("best_time"),
                        rs.getInt("wins"),
                        rs.getInt("rounds_played")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top time by race: " + e.getMessage());
        }
        return null;
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
