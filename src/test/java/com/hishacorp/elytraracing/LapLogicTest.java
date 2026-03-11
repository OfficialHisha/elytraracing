package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Racer;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LapLogicTest {

    private ServerMock server;
    private Elytraracing plugin;
    private Race race;
    private PlayerMock player;
    private Racer racer;
    private List<Ring> rings;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);

        // Register the race in the manager
        Race newRace = new Race(plugin, "test_race");
        plugin.getRaceManager().getRaces().add(newRace);
        race = newRace;
        race.setLaps(2);

        player = server.addPlayer();
        plugin.getRaceManager().joinRace(player, "test_race");
        racer = race.getRacers().get(player.getUniqueId());

        rings = new ArrayList<>();
        rings.add(new Ring(1, 1, new Location(player.getWorld(), 10, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.WHITE_STAINED_GLASS, 0));
        rings.add(new Ring(2, 1, new Location(player.getWorld(), 20, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.WHITE_STAINED_GLASS, 1));
        race.setRings(rings);

        race.start();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testLapIncrement() {
        assertEquals(1, racer.getCurrentLap());
        assertEquals(0, racer.getCurrentRingIndex());

        // Pass ring 1
        race.playerPassedRing(player);
        assertEquals(1, racer.getCurrentLap());
        assertEquals(1, racer.getCurrentRingIndex());

        // Pass ring 2 (completes lap 1)
        race.playerPassedRing(player);
        assertEquals(2, racer.getCurrentLap());
        assertEquals(0, racer.getCurrentRingIndex());
        assertTrue(racer.getBestLapTime() >= 0);
    }

    @Test
    public void testRaceCompletionAfterMultipleLaps() {
        // Lap 1
        race.playerPassedRing(player);
        race.playerPassedRing(player);
        assertFalse(racer.isCompleted());
        assertEquals(2, racer.getCurrentLap());

        // Lap 2
        race.playerPassedRing(player);
        race.playerPassedRing(player);
        assertTrue(racer.isCompleted());
    }

    @Test
    public void testOutOfBoundsTeleportOnSecondLap() {
        // Add a border so we can go out of bounds
        race.getBorders().add(new com.hishacorp.elytraracing.model.Border(1, new Location(player.getWorld(), -10, -10, -10), new Location(player.getWorld(), 100, 100, 100)));

        // Pass first ring of first lap
        race.playerPassedRing(player);

        // Pass second ring of first lap (now on second lap, first ring)
        race.playerPassedRing(player);
        assertEquals(2, racer.getCurrentLap());
        assertEquals(0, racer.getCurrentRingIndex());

        // Set player location out of bounds
        Location outOfBounds = new Location(player.getWorld(), 1000, 1000, 1000);
        player.teleport(outOfBounds);

        // Trigger move event
        org.bukkit.event.player.PlayerMoveEvent moveEvent = new org.bukkit.event.player.PlayerMoveEvent(player, player.getLocation(), outOfBounds);
        new com.hishacorp.elytraracing.listeners.PlayerMoveListener(plugin).onPlayerMove(moveEvent);

        // Should be teleported to the last ring of the race
        Location lastRingLoc = rings.get(rings.size() - 1).getLocation();
        assertEquals(lastRingLoc.getX(), moveEvent.getTo().getX());
        assertEquals(lastRingLoc.getY(), moveEvent.getTo().getY());
        assertEquals(lastRingLoc.getZ(), moveEvent.getTo().getZ());
    }
}
