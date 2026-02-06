package com.hishacorp.elytraracing.listeners;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.ToolManager;
import com.hishacorp.elytraracing.util.RingRenderer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class ToolListener implements Listener {

    private final Elytraracing plugin;
    private final ToolManager toolManager;
    private final RingRenderer ringRenderer;

    public ToolListener(Elytraracing plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.ringRenderer = plugin.getRingRenderer();
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newStack = player.getInventory().getItem(event.getNewSlot());

        if (toolManager.isTool(newStack)) {
            // Player is holding the tool. Check if we need to re-initialize their session.
            if (!toolManager.isPlayerUsingTool(player)) {
                String raceName = toolManager.getRaceNameFromTool(newStack);
                if (raceName != null) {
                    toolManager.reinitializeEditing(player, raceName);
                }
            }
            // Now that the session is potentially restored, update the view.
            ringRenderer.updatePlayerView(player);
        } else {
            // Player is holding something else. If they were holding the tool before, hide the rings.
            ItemStack oldStack = player.getInventory().getItem(event.getPreviousSlot());
            if (toolManager.isTool(oldStack)) {
                ringRenderer.revertPlayerView(player);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemDrop().getItemStack();

        if (toolManager.isTool(itemStack)) {
            ringRenderer.revertPlayerView(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (toolManager.isPlayerUsingTool(player)) {
                ItemStack currentItem = player.getInventory().getItemInMainHand();
                if (!toolManager.isTool(currentItem)) {
                    ringRenderer.revertPlayerView(player);
                }
            }
        }
    }
}
