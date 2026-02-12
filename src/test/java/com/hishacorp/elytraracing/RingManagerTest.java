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
    public void testUpdateRing() throws Exception {
        RingManager ringManager = new RingManager(plugin);

        plugin.getDatabaseManager().createRace("test_race", "test_world");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");

        Ring ring = new Ring(1, raceId, player.getLocation(), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 0);
        int id = plugin.getDatabaseManager().createRing(ring);
        ring.setId(id);

        ringManager.loadRings(raceId);

        ring.setRadius(10);
        ringManager.updateRing(ring);

        List<Ring> updatedRings = ringManager.getRings(raceId);
        assertEquals(1, updatedRings.size());
        assertEquals(10, updatedRings.get(0).getRadius());
    }
}
