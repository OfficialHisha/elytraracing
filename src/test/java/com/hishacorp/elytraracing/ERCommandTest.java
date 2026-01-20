package com.hishacorp.elytraracing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    public void testRingsCommand() throws Exception {
        plugin.getDatabaseManager().createRace("test_race");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");
        plugin.getDatabaseManager().createRing(new com.hishacorp.elytraracing.model.Ring(0, raceId, player.getLocation(), 5, com.hishacorp.elytraracing.model.Ring.Orientation.HORIZONTAL, org.bukkit.Material.GOLD_BLOCK, 1));

        player.performCommand("er rings test_race");

        player.assertSaid("§aRings for race test_race:");
        player.assertSaid("§e- Ring 1 at " + player.getLocation().toString());
    }

    @Test
    public void testToolCommand() {
        player.performCommand("er tool test_race");
        player.assertSaid("§aYou have been given the ring tool for race test_race.");
    }

    @Test
    public void testToolCommandNoRace() {
        player.performCommand("er tool");
        player.assertSaid("§cUsage: /er tool <race>");
    }

    @Test
    public void testCaseInsensitivity() {
        // Test race creation
        player.performCommand("er create MyRace");
        player.assertSaid("§aRace 'myrace' created!");

        // Test tool command
        player.performCommand("er tool myrace");
        player.assertSaid("§aYou have been given the ring tool for race myrace.");
        assertNotNull(player.getInventory().getItemInMainHand());
        player.getInventory().clear(); // Clean up for the next command

        // Test tool command with different casing
        player.performCommand("er tool MyRace");
        player.assertSaid("§aYou have been given the ring tool for race myrace.");
        assertNotNull(player.getInventory().getItemInMainHand());
        player.getInventory().clear();

        // Test race deletion
        player.performCommand("er delete MYRACE");
        player.assertSaid("§aRace 'myrace' deleted!");
    }
}
