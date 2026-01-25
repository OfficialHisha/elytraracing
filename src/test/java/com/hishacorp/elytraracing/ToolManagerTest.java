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
        player.performCommand("er create test_race");
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
        player.performCommand("er create test_race");
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
        player.performCommand("er create test_race");
        player.performCommand("er tool test_race");
        assertNotNull(player.getInventory().getItemInMainHand());

        player.disconnect();

        assertFalse(player.getInventory().isEmpty());
    }

    @Test
    public void testReconnectFunctionality() throws Exception {
        // 1. Setup: create a race with one ring
        player.performCommand("er create reconnect_test");
        int raceId = plugin.getDatabaseManager().getRaceId("reconnect_test");
        Ring testRing = new Ring(1, raceId, player.getEyeLocation().add(5, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 0);
        plugin.getRingManager().addRing(testRing);

        // 2. Give player the tool and verify initial state
        player.performCommand("er tool reconnect_test");
        assertTrue(plugin.getToolManager().isPlayerUsingTool(player), "Player should be in editing session after receiving tool.");
        assertFalse(plugin.getRingRenderer().getPlayerRingBlocks(player.getUniqueId()).isEmpty(), "Rings should be visible after receiving tool.");

        // 3. Simulate session loss (like a disconnect/reconnect)
        // We manually stop the editing session to simulate the player's state being cleared from the map.
        plugin.getToolManager().stopEditing(player);
        assertFalse(plugin.getToolManager().isPlayerUsingTool(player), "Player should not be in editing session after it's stopped.");
        assertTrue(plugin.getRingRenderer().getPlayerRingBlocks(player.getUniqueId()).isEmpty(), "Rings should be hidden after stopping editing.");

        // 4. Find the tool in the inventory
        int toolSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (plugin.getToolManager().isTool(player.getInventory().getItem(i))) {
                toolSlot = i;
                break;
            }
        }
        assertNotEquals(-1, toolSlot, "Player should have the tool in their inventory.");

        // 5. Simulate the player re-selecting the tool by manually firing the event.
        // This is more reliable in a test environment than just calling setHeldItemSlot.
        int previousSlot = player.getInventory().getHeldItemSlot();
        // Set the slot on the inventory so getItemInMainHand is correct inside any subsequent logic.
        player.getInventory().setHeldItemSlot(toolSlot);
        // Manually fire the event that the ToolListener is waiting for.
        server.getPluginManager().callEvent(new org.bukkit.event.player.PlayerItemHeldEvent(player, previousSlot, toolSlot));


        // 6. Verify the session is restored and rings are visible again
        assertTrue(plugin.getToolManager().isPlayerUsingTool(player), "Player's editing session should be re-initialized upon holding the tool.");
        assertFalse(plugin.getRingRenderer().getPlayerRingBlocks(player.getUniqueId()).isEmpty(), "Rings should be visible again after re-selecting the tool.");
    }
}
