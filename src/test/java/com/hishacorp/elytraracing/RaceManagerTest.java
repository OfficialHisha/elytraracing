package com.hishacorp.elytraracing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

public class RaceManagerTest {

    private ServerMock server;
    private Elytraracing plugin;
    private PlayerMock player;
    private RaceManager raceManager;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        player = server.addPlayer();
        player.setOp(true);
        raceManager = plugin.getRaceManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testCreateRace() {
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test_race", "world"));
        assertTrue(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testCreateRaceWithEmptyName() {
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "", "world"));
        assertFalse(plugin.getDatabaseManager().raceExists(""));
    }

    @Test
    public void testDeleteRace() {
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test_race", "world"));
        raceManager.deleteRace(new com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent(player, "test_race"));
        assertFalse(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testDeleteNonExistentRace() {
        raceManager.deleteRace(new com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent(player, "non_existent_race"));
        assertFalse(plugin.getDatabaseManager().raceExists("non_existent_race"));
    }
}
