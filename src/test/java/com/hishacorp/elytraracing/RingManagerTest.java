package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RingManagerTest {

    private ServerMock server;
    private Elytraracing plugin;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        player = server.addPlayer();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testUpdateRingWithIndexShift() throws Exception {
        RingManager ringManager = new RingManager(plugin);
        List<Ring> rings = new ArrayList<>();
        rings.add(new Ring(1, 1, player.getLocation(), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 0));
        rings.add(new Ring(2, 1, player.getLocation(), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1));
        rings.add(new Ring(3, 1, player.getLocation(), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 2));

        plugin.getDatabaseManager().createRace("test_race", "test_world");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");

        for (Ring ring : rings) {
            ring.setRaceId(raceId);
            plugin.getDatabaseManager().createRing(ring);
        }

        ringManager.loadRings(raceId);

        Ring updatedRing = new Ring(1, raceId, player.getLocation(), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 2);
        ringManager.updateRingWithIndexShift(updatedRing);

        List<Ring> updatedRings = ringManager.getRings(raceId);
        updatedRings.sort(java.util.Comparator.comparingInt(Ring::getIndex));

        assertEquals(3, updatedRings.size());
        for (int i = 0; i < updatedRings.size(); i++) {
            assertEquals(i, updatedRings.get(i).getIndex());
        }
    }
}
