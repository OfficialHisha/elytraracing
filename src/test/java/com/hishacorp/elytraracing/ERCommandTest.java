package com.hishacorp.elytraracing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

public class ERCommandTest {

    private ServerMock server;
    private Elytraracing plugin;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        player = server.addPlayer();
        player.setOp(true);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testCreateRaceCommand() {
        player.performCommand("er create test_race");
        assertTrue(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testDeleteRaceCommand() {
        player.performCommand("er create test_race");
        player.performCommand("er delete test_race");
        assertFalse(plugin.getDatabaseManager().raceExists("test_race"));
    }
}
