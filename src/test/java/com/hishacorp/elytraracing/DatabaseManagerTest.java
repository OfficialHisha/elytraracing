package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Ring;
import com.hishacorp.elytraracing.persistance.DatabaseManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private Elytraracing plugin;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        databaseManager = plugin.getDatabaseManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testCreateAndGetRing() throws SQLException {
        databaseManager.createRace("test_race");
        int raceId = databaseManager.getRaceId("test_race");
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        Ring ring = new Ring(0, raceId, new Location(world, 1, 2, 3), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1);

        databaseManager.createRing(ring);

        List<Ring> rings = databaseManager.getRings(raceId);
        assertNotNull(rings);
        assertEquals(1, rings.size());

        Ring retrievedRing = rings.get(0);
        assertEquals(ring.getRaceId(), retrievedRing.getRaceId());
        assertEquals(ring.getRadius(), retrievedRing.getRadius());
    }

    @Test
    public void testUpdateRing() throws SQLException {
        databaseManager.createRace("test_race");
        int raceId = databaseManager.getRaceId("test_race");
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        Ring ring = new Ring(0, raceId, new Location(world, 1, 2, 3), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1);
        databaseManager.createRing(ring);
        List<Ring> rings = databaseManager.getRings(raceId);
        Ring retrievedRing = rings.get(0);

        retrievedRing.setRadius(10);
        databaseManager.updateRing(retrievedRing);

        Ring updatedRing = databaseManager.getRing(retrievedRing.getId());
        assertEquals(10, updatedRing.getRadius());
    }

    @Test
    public void testDeleteRing() throws SQLException {
        databaseManager.createRace("test_race");
        int raceId = databaseManager.getRaceId("test_race");
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        Ring ring = new Ring(0, raceId, new Location(world, 1, 2, 3), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1);
        databaseManager.createRing(ring);
        List<Ring> rings = databaseManager.getRings(raceId);
        Ring retrievedRing = rings.get(0);

        databaseManager.deleteRing(retrievedRing.getId());

        assertNull(databaseManager.getRing(retrievedRing.getId()));
    }
}
