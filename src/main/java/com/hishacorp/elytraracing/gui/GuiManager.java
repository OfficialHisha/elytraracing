package com.hishacorp.elytraracing.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, Gui> openGuis = new HashMap<>();

    public GuiManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGui(Player player, Gui gui) {
        player.openInventory(gui.getInventory());
        gui.onOpen(player);
        openGuis.put(player.getUniqueId(), gui);
    }

    public Gui getOpenGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Gui gui = openGuis.get(player.getUniqueId());
        if (gui == null) return;

        if (!event.getInventory().equals(gui.getInventory())) return;

        event.setCancelled(true); // prevent item movement by default
        gui.onClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Gui gui = openGuis.remove(player.getUniqueId());
        if (gui != null) {
            gui.onClose(player);
        }
    }
}
