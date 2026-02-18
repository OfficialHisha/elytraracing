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
        player.assertSaid("§aRace 'test_race' created!");
        player.assertSaid("§aYou have been given the admin tool for race test_race.");
        assertTrue(plugin.getToolManager().isTool(player.getInventory().getItemInMainHand()));
    }

    @Test
    public void testDeleteRaceCommand() {
        player.performCommand("er create test_race");
        player.nextMessage(); // Consume create message
        player.nextMessage(); // Consume tool message
        player.performCommand("er delete test_race");
        player.assertSaid("§aRace 'test_race' deleted!");
        assertFalse(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testRingsCommand() throws Exception {
        plugin.getDatabaseManager().createRace("test_race", "test_world");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");
        plugin.getDatabaseManager().createRing(new com.hishacorp.elytraracing.model.Ring(0, raceId, player.getLocation(), 5, com.hishacorp.elytraracing.model.Ring.Orientation.HORIZONTAL, org.bukkit.Material.GOLD_BLOCK, 1));

        player.performCommand("er rings test_race");

        player.assertSaid("§aRings for race test_race:");
        player.assertSaid("§e- Ring 1 at " + player.getLocation().toString());
    }

    @Test
    public void testToolCommand() {
        player.performCommand("er create test_race");
        player.nextMessage(); // Consume create message
        player.nextMessage(); // Consume tool message
        player.performCommand("er tool test_race");
        player.assertSaid("§aYou have been given the admin tool for race test_race.");
    }

    @Test
    public void testToolCommandNoRace() {
        player.performCommand("er tool");
        player.assertSaid("§cUsage: /er tool <race>");
    }

    @Test
    public void testToolCommandNonExistentRace() {
        player.performCommand("er tool non_existent_race");
        player.assertSaid("§cRace not found: non_existent_race");
    }

    @Test
    public void testCaseInsensitivity() {
        // Test race creation
        player.performCommand("er create MyRace");
        player.assertSaid("§aRace 'myrace' created!");
        player.assertSaid("§aYou have been given the admin tool for race myrace.");
        assertTrue(plugin.getToolManager().isTool(player.getInventory().getItemInMainHand()));
        player.getInventory().clear();

        // Test tool command
        player.performCommand("er tool myrace");
        player.assertSaid("§aYou have been given the admin tool for race myrace.");
        assertNotNull(player.getInventory().getItemInMainHand());
        player.getInventory().clear(); // Clean up for the next command

        // Test tool command with different casing
        player.performCommand("er tool MyRace");
        player.assertSaid("§aYou have been given the admin tool for race myrace.");
        assertNotNull(player.getInventory().getItemInMainHand());
        player.getInventory().clear();

        // Test race deletion
        player.performCommand("er delete MYRACE");
        player.assertSaid("§aRace 'myrace' deleted!");
    }

    @Test
    public void testJoinRace() {
        player.performCommand("er create test_race");
        player.nextMessage(); // Consume create message
        player.nextMessage(); // Consume tool message
        player.performCommand("er join test_race");
        player.assertSaid("§aYou have joined the race: test_race");
    }

    @Test
    public void testJoinNonExistentRace() {
        player.performCommand("er join non_existent_race");
        player.assertSaid("§cRace not found: non_existent_race");
    }

    @Test
    public void testLeaveRaceNotInRace() {
        player.performCommand("er leave");
        player.assertSaid("§cYou are not in a race.");
    }

    @Test
    public void testEndRaceNotInProgress() {
        player.performCommand("er create test_race");
        player.nextMessage(); // Consume create message
        player.nextMessage(); // Consume tool message
        player.performCommand("er end test_race");
        player.assertSaid("§cRace 'test_race' is not in progress.");
    }

    @Test
    public void testStartRaceNoPlayers() {
        player.performCommand("er create test_race");
        player.nextMessage(); // Consume create message
        player.nextMessage(); // Consume tool message
        player.performCommand("er start test_race");
        player.assertSaid("§cCannot start a race with no players.");
    }

    @Test
    public void testStartRaceNonExistent() {
        player.performCommand("er start non_existent_race");
        player.assertSaid("§cRace not found: non_existent_race");
    }

    @Test
    public void testListCommand() {
        player.performCommand("er list");
        player.assertSaid("§eNo races found.");

        player.performCommand("er create race1");
        player.nextMessage(); // Consume create message
        player.nextMessage(); // Consume tool message
        player.performCommand("er create race2");
        player.nextMessage(); // Consume create message
        player.nextMessage(); // Consume tool message

        player.performCommand("er list");
        player.assertSaid("§aAvailable Races:");
        player.assertSaid("§e- race1 §7(World: " + player.getWorld().getName() + ")");
        player.assertSaid("§e- race2 §7(World: " + player.getWorld().getName() + ")");
    }

    @Test
    public void testJoinDisabledRace() {
        player.performCommand("er create test_race");
        player.nextMessage(); // Consume create
        player.nextMessage(); // Consume tool

        player.performCommand("er disable test_race");
        player.assertSaid("§aRace 'test_race' has been disabled.");

        player.performCommand("er join test_race");
        player.assertSaid("§cThis race is currently disabled.");
    }
}
