package com.hishacorp.elytraracing.listeners;

import com.hishacorp.elytraracing.Elytraracing;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final Elytraracing plugin;

    public PlayerJoinListener(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack[] inventory = plugin.getDatabaseManager().loadInventory(player.getUniqueId());

        if (inventory != null) {
            player.getInventory().setContents(inventory);
            plugin.getDatabaseManager().deleteInventory(player.getUniqueId());
            player.sendMessage("§aYour inventory has been restored.");
        }
    }
}
