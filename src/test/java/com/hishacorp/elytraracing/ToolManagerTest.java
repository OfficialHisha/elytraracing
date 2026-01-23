package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.screens.RingConfigGui;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
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
    public void testRingCreationAndOrientation() {
        player.performCommand("er create test_race");
        player.performCommand("er tool test_race");

        // Looking straight
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, null));
        Ring ring = plugin.getRingRenderer().getPlayerConfiguringRing(player.getUniqueId());
        assertNotNull(ring);
        assertEquals(Ring.Orientation.VERTICAL_Z, ring.getOrientation());
    }

    @Test
    public void testRingSelectionByBlock() throws Exception {
        plugin.getDatabaseManager().createRace("test_race", "test_world");
        int raceId = plugin.getDatabaseManager().getRaceId("test_race");
        Ring testRing = new Ring(1, raceId, player.getEyeLocation().add(5, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 0);
        plugin.getRingManager().addRing(testRing);
        player.performCommand("er tool test_race");

        // Manually render the ring for the test
        plugin.getRingRenderer().addRingForPlayer(player, testRing);

        // Point player towards the ring
        Location targetLocation = testRing.getLocation().clone().add(5,0,0);
        Location playerLocation = player.getLocation();
        player.teleport(playerLocation.setDirection(targetLocation.toVector().subtract(playerLocation.toVector())));


        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, null, EquipmentSlot.HAND));

        assertEquals(testRing, plugin.getRingRenderer().getPlayerConfiguringRing(player.getUniqueId()));
    }

    @Test
    public void testGuiStatePersistsOnClose() throws Exception {
        plugin.getDatabaseManager().createRace("test_race", "test_world");
        player.performCommand("er tool test_race");

        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, null));
        assertNotNull(plugin.getRingRenderer().getPlayerConfiguringRing(player.getUniqueId()));

        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, null));
        assertNotNull(plugin.getGuiManager().getOpenGui(player));

        player.closeInventory();

        assertNotNull(plugin.getRingRenderer().getPlayerConfiguringRing(player.getUniqueId()));
        assertNull(plugin.getGuiManager().getOpenGui(player));
    }

    @Test
    public void testSaveButton() throws Exception {
        plugin.getDatabaseManager().createRace("test_race", "test_world");
        player.performCommand("er tool test_race");

        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, null));
        plugin.getToolManager().onPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, null));
        RingConfigGui gui = (RingConfigGui) plugin.getGuiManager().getOpenGui(player);

        gui.onClick(new InventoryClickEvent(player.getOpenInventory(), InventoryType.SlotType.CONTAINER, 26, null, null));

        assertNull(plugin.getRingRenderer().getPlayerConfiguringRing(player.getUniqueId()));
        assertEquals(1, plugin.getRingManager().getRings(plugin.getDatabaseManager().getRaceId("test_race")).size());
    }

    @Test
    public void testToolRemovalOnQuit() {
        player.performCommand("er tool test_race");
        assertNotNull(player.getInventory().getItemInMainHand());

        player.disconnect();

        assertTrue(player.getInventory().isEmpty());
    }
}
