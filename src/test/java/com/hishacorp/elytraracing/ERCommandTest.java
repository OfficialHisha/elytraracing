package com.hishacorp.elytraracing;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ERCommandTest {
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
    public void testCreateRaceCommand() {
        player.setOp(true);
        player.performCommand("er create test_race");
        assertEquals("§aRace 'test_race' created!", player.nextMessage());
    }

    @Test
    public void testDeleteRaceCommand() {
        player.setOp(true);
        player.performCommand("er create test_race");
        player.nextMessage(); // consume the "created" message
        player.performCommand("er delete test_race");
        assertEquals("§aRace 'test_race' deleted!", player.nextMessage());
    }

    @Test
    public void testSetSpawnCommand() {
        player.setOp(true);
        player.performCommand("er setspawn");
        assertEquals("§aSpawn set to your location.", player.nextMessage());
    }
}
