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
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test_race"));
        assertTrue(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testCreateRaceWithEmptyName() {
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, ""));
        assertFalse(plugin.getDatabaseManager().raceExists(""));
    }

    @Test
    public void testDeleteRace() {
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test_race"));
        raceManager.deleteRace(new com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent(player, "test_race"));
        assertFalse(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testDeleteNonExistentRace() {
        raceManager.deleteRace(new com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent(player, "non_existent_race"));
        assertFalse(plugin.getDatabaseManager().raceExists("non_existent_race"));
    }

    @Test
    public void testPlayerCannotJoinMultipleRaces() {
        // Given
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test-race-1"));
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test-race-2"));

        // When
        raceManager.joinRace(player, "test-race-1");
        raceManager.joinRace(player, "test-race-2");

        // Then
        assertTrue(raceManager.getRace("test-race-1").get().getPlayers().contains(player.getUniqueId()));
        assertFalse(raceManager.getRace("test-race-2").get().getPlayers().contains(player.getUniqueId()));
    }

    @Test
    public void testDeleteRaceRemovesFromMemory() {
        // Given
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test-race"));

        // When
        raceManager.deleteRace(new com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent(player, "test-race"));

        // Then
        assertTrue(raceManager.getRace("test-race").isEmpty());
    }

    @Test
    public void testCannotDeleteRaceWithPlayers() {
        // Given
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test-race"));
        raceManager.joinRace(player, "test-race");

        // When
        raceManager.deleteRace(new com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent(player, "test-race"));

        // Then
        assertTrue(raceManager.getRace("test-race").isPresent());
    }

    @Test
    public void testCannotDeleteRaceInProgress() {
        // Given
        raceManager.createRace(new com.hishacorp.elytraracing.input.events.CreateRaceInputEvent(player, "test-race"));
        raceManager.getRace("test-race").get().start();

        // When
        raceManager.deleteRace(new com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent(player, "test-race"));

        // Then
        assertTrue(raceManager.getRace("test-race").isPresent());
    }
}
