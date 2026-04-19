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
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        databaseManager.createRace("test_race", world.getName());
        int raceId = databaseManager.getRaceId("test_race");
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
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        databaseManager.createRace("test_race", world.getName());
        int raceId = databaseManager.getRaceId("test_race");
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
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        databaseManager.createRace("test_race", world.getName());
        int raceId = databaseManager.getRaceId("test_race");
        Ring ring = new Ring(0, raceId, new Location(world, 1, 2, 3), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1);
        databaseManager.createRing(ring);
        List<Ring> rings = databaseManager.getRings(raceId);
        Ring retrievedRing = rings.get(0);

        databaseManager.deleteRing(retrievedRing.getId());

        assertNull(databaseManager.getRing(retrievedRing.getId()));
    }

    @Test
    public void testGetTopStatByRace() throws SQLException {
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        databaseManager.createRace("test_race", world.getName());
        int raceId = databaseManager.getRaceId("test_race");

        java.util.UUID uuid1 = java.util.UUID.randomUUID();
        java.util.UUID uuid2 = java.util.UUID.randomUUID();

        // uuid1: better time, worse wins
        databaseManager.incrementRoundsPlayed(uuid1, raceId);
        databaseManager.incrementRoundsPlayed(uuid1, raceId);
        databaseManager.saveRaceStat(uuid1, raceId, 1000L, 500L, false);
        databaseManager.saveRaceStat(uuid1, raceId, 1000L, 500L, false); // finishes = 2, wins = 0, time = 1000, rounds = 2

        // uuid2: worse time, better wins
        databaseManager.incrementRoundsPlayed(uuid2, raceId);
        databaseManager.saveRaceStat(uuid2, raceId, 2000L, 1000L, true); // finishes = 1, wins = 1, time = 2000, rounds = 1

        com.hishacorp.elytraracing.persistance.data.RaceStat topTime = databaseManager.getTopStatByRace("test_race", "time", 1);
        assertEquals(uuid1, topTime.getPlayerUUID());
        assertEquals(1000L, topTime.getBestTime());

        com.hishacorp.elytraracing.persistance.data.RaceStat topWins = databaseManager.getTopStatByRace("test_race", "wins", 1);
        assertEquals(uuid2, topWins.getPlayerUUID());
        assertEquals(1, topWins.getWins());

        com.hishacorp.elytraracing.persistance.data.RaceStat topRounds = databaseManager.getTopStatByRace("test_race", "rounds", 1);
        assertEquals(uuid1, topRounds.getPlayerUUID());
        assertEquals(2, topRounds.getRoundsPlayed());
    }

    @Test
    public void testGetPlayerRank() throws SQLException {
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        databaseManager.createRace("test_race", world.getName());
        int raceId = databaseManager.getRaceId("test_race");

        java.util.UUID uuid1 = java.util.UUID.randomUUID();
        java.util.UUID uuid2 = java.util.UUID.randomUUID();
        java.util.UUID uuid3 = java.util.UUID.randomUUID();

        // uuid1: best time (1000)
        databaseManager.saveRaceStat(uuid1, raceId, 1000L, 500L, true);
        // uuid2: second best time (2000)
        databaseManager.saveRaceStat(uuid2, raceId, 2000L, 1000L, false);
        // uuid3: third best time (3000)
        databaseManager.saveRaceStat(uuid3, raceId, 3000L, 1500L, false);

        assertEquals(1, databaseManager.getPlayerRank("test_race", uuid1, "time"));
        assertEquals(2, databaseManager.getPlayerRank("test_race", uuid2, "time"));
        assertEquals(3, databaseManager.getPlayerRank("test_race", uuid3, "time"));

        // Test with wins
        // uuid1: 1 win
        // uuid2: 0 wins
        // uuid3: 0 wins
        assertEquals(1, databaseManager.getPlayerRank("test_race", uuid1, "wins"));
        assertEquals(2, databaseManager.getPlayerRank("test_race", uuid2, "wins")); // Tied with uuid3, but only uuid1 is better

        // Test non-existent player
        assertEquals(-1, databaseManager.getPlayerRank("test_race", java.util.UUID.randomUUID(), "time"));

        // Test non-existent race
        assertEquals(-1, databaseManager.getPlayerRank("non_existent_race", uuid1, "time"));
    }

    @Test
    public void testGetPlayerStat() throws SQLException {
        World world = MockBukkit.getMock().addSimpleWorld("test_world");
        databaseManager.createRace("test_race", world.getName());
        int raceId = databaseManager.getRaceId("test_race");

        java.util.UUID uuid = java.util.UUID.randomUUID();

        databaseManager.incrementRoundsPlayed(uuid, raceId);
        databaseManager.saveRaceStat(uuid, raceId, 1500L, 750L, true);

        com.hishacorp.elytraracing.persistance.data.RaceStat stat = databaseManager.getPlayerStat("test_race", uuid);

        assertNotNull(stat);
        assertEquals(uuid, stat.getPlayerUUID());
        assertEquals(1500L, stat.getBestTime());
        assertEquals(750L, stat.getBestLapTime());
        assertEquals(1, stat.getWins());
        assertEquals(1, stat.getRoundsPlayed());
        assertEquals(1, stat.getFinishes());

        // Test non-existent stat
        assertNull(databaseManager.getPlayerStat("test_race", java.util.UUID.randomUUID()));
        assertNull(databaseManager.getPlayerStat("non_existent_race", uuid));
    }
}
