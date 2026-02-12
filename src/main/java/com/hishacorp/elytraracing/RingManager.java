package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Ring;
import com.hishacorp.elytraracing.persistance.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RingManager {

    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<Integer, List<Ring>> rings = new HashMap<>();

    public RingManager(Elytraracing plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void loadRings(int raceId) {
        try {
            rings.put(raceId, databaseManager.getRings(raceId));
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load rings for race " + raceId + ": " + e.getMessage());
        }
    }

    public void addRing(Ring ring) {
        try {
            int newId = databaseManager.createRing(ring);
            ring.setId(newId);
            rings.computeIfAbsent(ring.getRaceId(), k -> new java.util.ArrayList<>()).add(ring);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add ring: " + e.getMessage());
        }
    }

    public void updateRing(Ring ring) {
        try {
            databaseManager.updateRing(ring);
            List<Ring> ringList = rings.get(ring.getRaceId());
            if (ringList != null) {
                for (int i = 0; i < ringList.size(); i++) {
                    if (ringList.get(i).getId() == ring.getId()) {
                        ringList.set(i, ring);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update ring: " + e.getMessage());
        }
    }

    public void deleteRing(Ring ring) {
        try {
            databaseManager.deleteRing(ring.getId());
            List<Ring> ringList = rings.get(ring.getRaceId());
            if (ringList != null) {
                ringList.remove(ring);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete ring: " + e.getMessage());
        }
    }

    public List<Ring> getRings(int raceId) {
        return rings.get(raceId);
    }
}
