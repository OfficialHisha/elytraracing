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
        race = new Race(plugin, "test_race");
        race.setLaps(2);

        player = server.addPlayer();
        race.addPlayer(player);
        racer = race.getRacers().get(player.getUniqueId());

        rings = new ArrayList<>();
        rings.add(new Ring(1, 1, new Location(null, 10, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.WHITE_STAINED_GLASS, 0));
        rings.add(new Ring(2, 1, new Location(null, 20, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.WHITE_STAINED_GLASS, 1));
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
}
