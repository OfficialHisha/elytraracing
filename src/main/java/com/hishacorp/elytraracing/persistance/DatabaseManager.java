package com.hishacorp.elytraracing.persistance;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.Gson;
import org.bukkit.inventory.ItemStack;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;
    private final Gson gson = new Gson();

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

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_inventories (
                    player_uuid TEXT PRIMARY KEY,
                    inventory_contents TEXT NOT NULL
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

    public void saveInventory(UUID playerUUID, ItemStack[] inventoryContents) {
        List<Map<String, Object>> serializedInventory = new ArrayList<>();
        for (ItemStack itemStack : inventoryContents) {
            if (itemStack != null) {
                serializedInventory.add(itemStack.serialize());
            } else {
                serializedInventory.add(null);
            }
        }
        String json = gson.toJson(serializedInventory);
        try (var ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_inventories (player_uuid, inventory_contents) VALUES (?, ?)")) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, json);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save inventory: " + e.getMessage());
        }
    }

    public ItemStack[] loadInventory(UUID playerUUID) {
        try (var ps = connection.prepareStatement(
                "SELECT inventory_contents FROM player_inventories WHERE player_uuid = ?")) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = rs.getString("inventory_contents");
                List<Map<String, Object>> serializedInventory = gson.fromJson(json, List.class);
                ItemStack[] inventoryContents = new ItemStack[serializedInventory.size()];
                for (int i = 0; i < serializedInventory.size(); i++) {
                    Map<String, Object> serializedItemStack = serializedInventory.get(i);
                    if (serializedItemStack != null) {
                        inventoryContents[i] = ItemStack.deserialize(serializedItemStack);
                    } else {
                        inventoryContents[i] = null;
                    }
                }
                return inventoryContents;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load inventory: ".concat(e.getMessage()));
        }
        return null;
    }

    public void deleteInventory(UUID playerUUID) {
        try (var ps = connection.prepareStatement(
                "DELETE FROM player_inventories WHERE player_uuid = ?")) {
            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete inventory: " + e.getMessage());
        }
    }
}
