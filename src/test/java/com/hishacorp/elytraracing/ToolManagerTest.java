package com.hishacorp.elytraracing;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import com.hishacorp.elytraracing.gui.screens.RingConfigGui;
import org.junit.jupiter.api.Disabled;
import org.bukkit.block.Block;
import org.bukkit.inventory.EquipmentSlot;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

public class ToolManagerTest {

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
    public void testGiveTool() {
        player.performCommand("er tool test_race");
        assertNotNull(player.getInventory().getItem(0));
        assertEquals(Material.BLAZE_ROD, player.getInventory().getItem(0).getType());
    }

    @Test
    public void testPermissionCheck() {
        // Give tool to OP player
        player.performCommand("er tool test_race");
        ItemStack tool = player.getInventory().getItem(0);
        assertNotNull(tool);
        // Clear the "you received the tool" message
        while (player.nextMessage() != null);

        // Revoke permissions
        player.setOp(false);

        // Trigger the interaction and check for the correct permission message from the ToolManager
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, org.bukkit.event.block.Action.RIGHT_CLICK_AIR, tool, null, null));
        player.assertSaid("§cYou do not have permission to use this command");
    }

    @Test
    public void testRingSelection() throws Exception {
        plugin.getDatabaseManager().createRace("test_race");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");
        // Create the ring at the player's eye location to guarantee selection
        plugin.getDatabaseManager().createRing(new com.hishacorp.elytraracing.model.Ring(0, raceId, player.getEyeLocation(), 1, com.hishacorp.elytraracing.model.Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1));
        plugin.getRingManager().loadRings(raceId);

        player.performCommand("er tool test_race");
        ItemStack tool = player.getInventory().getItem(0);
        while (player.nextMessage() != null);

        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, org.bukkit.event.block.Action.LEFT_CLICK_AIR, tool, null, null));

        player.assertSaid("§aStarted configuring ring 1");
    }

    @Test
    public void testRingPlacement() throws Exception {
        plugin.getDatabaseManager().createRace("test_race");
        player.performCommand("er tool test_race");
        ItemStack tool = player.getInventory().getItem(0);
        assertNotNull(tool);
        while (player.nextMessage() != null);

        player.getTargetBlock(100);
        Location targetLocation = player.getTargetBlock(100).getLocation();
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, tool, player.getTargetBlock(100), org.bukkit.block.BlockFace.UP));

        // Get the ring that is being configured
        Ring ring = plugin.getToolManager().getConfiguringRing(player);
        assertNotNull(ring);
        assertEquals(targetLocation, ring.getLocation());
    }

    @Test
    public void testDefaultIndex() throws Exception {
        plugin.getDatabaseManager().createRace("test_race");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");
        plugin.getDatabaseManager().createRing(new com.hishacorp.elytraracing.model.Ring(0, raceId, player.getLocation(), 1, com.hishacorp.elytraracing.model.Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 0));
        plugin.getRingManager().loadRings(raceId);

        player.performCommand("er tool test_race");
        ItemStack tool = player.getInventory().getItem(0);
        assertNotNull(tool);
        while (player.nextMessage() != null);

        // Set a target block for the player
        player.getTargetBlock(100);

        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, tool, player.getTargetBlock(100), org.bukkit.block.BlockFace.UP));

        Ring ring = plugin.getToolManager().getConfiguringRing(player);
        assertNotNull(ring);
        assertEquals(1, ring.getIndex());
    }

    @Test
    public void testVisualStateOnClose() throws Exception {
        plugin.getDatabaseManager().createRace("test_race");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");
        plugin.getDatabaseManager().createRing(new com.hishacorp.elytraracing.model.Ring(0, raceId, player.getLocation(), 1, com.hishacorp.elytraracing.model.Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 0));
        plugin.getRingManager().loadRings(raceId);

        player.performCommand("er tool test_race");
        ItemStack tool = player.getInventory().getItem(0);
        assertNotNull(tool);
        while (player.nextMessage() != null);

        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, tool, null, null));
        RingConfigGui gui = (RingConfigGui) plugin.getGuiManager().getOpenGui(player);
        assertNotNull(gui);
        gui.onClose(player);

        assertNull(plugin.getToolManager().getConfiguringRing(player));
        assertNull(plugin.getGuiManager().getOpenGui(player));
    }

    @Test
    public void testIsNewFlag() throws Exception {
        plugin.getDatabaseManager().createRace("test_race");
        player.performCommand("er tool test_race");
        ItemStack tool = player.getInventory().getItem(0);
        assertNotNull(tool);
        while (player.nextMessage() != null);

        player.getTargetBlock(100);
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, tool, player.getTargetBlock(100), org.bukkit.block.BlockFace.UP));

        RingConfigGui gui = (RingConfigGui) plugin.getGuiManager().getOpenGui(player);
        assertNotNull(gui);
        assertTrue(gui.isNew());
    }

    @Test
    public void testRelocationWorkflow() throws Exception {
        plugin.getDatabaseManager().createRace("test_race");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");
        plugin.getDatabaseManager().createRing(new com.hishacorp.elytraracing.model.Ring(0, raceId, player.getEyeLocation(), 1, com.hishacorp.elytraracing.model.Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1));
        plugin.getRingManager().loadRings(raceId);

        player.performCommand("er tool test_race");
        ItemStack tool = player.getInventory().getItem(0);
        while (player.nextMessage() != null);

        // Select the ring
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, tool, null, null, EquipmentSlot.HAND));
        player.assertSaid("§aStarted configuring ring 1");

        // Relocate the ring
        Block targetBlock = player.getTargetBlock(100);
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, tool, targetBlock, org.bukkit.block.BlockFace.UP, EquipmentSlot.HAND));
        player.assertSaid("§aRing relocated.");
        assertEquals(targetBlock.getLocation(), plugin.getToolManager().getConfiguringRing(player).getLocation());

        // Open the GUI
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, tool, null, null, EquipmentSlot.HAND));
        assertNotNull(plugin.getGuiManager().getOpenGui(player));
    }
}
