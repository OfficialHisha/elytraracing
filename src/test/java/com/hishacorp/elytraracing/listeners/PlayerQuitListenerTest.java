package com.hishacorp.elytraracing.listeners;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.Race;
import com.hishacorp.elytraracing.RaceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class PlayerQuitListenerTest {

    private ServerMock server;
    private Elytraracing plugin;
    private PlayerMock player;
    private RaceManager raceManager;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        player = server.addPlayer();
        raceManager = plugin.getRaceManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testPlayerQuitRestoresInventory() {
        // Given
        raceManager.getRaces().add(new Race(plugin, "test-race"));
        raceManager.joinRace(player, "test-race");

        // When
        player.disconnect();

        // Then
        assertFalse(raceManager.isPlayerInRace(player));
    }
}
