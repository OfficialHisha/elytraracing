package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.gui.Gui;
import com.hishacorp.elytraracing.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.Arrays;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.Material.*;

public class SetupGui implements Gui {
    private final Elytraracing plugin;
    private final Inventory inventory;

    public SetupGui(Elytraracing plugin, Player player) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 27, "Elytra Racing Setup");

        inventory.setItem(10, button(ELYTRA, "§aCreate Race"));
        inventory.setItem(12, button(BARRIER, "§cDelete Race"));
        inventory.setItem(14, button(COMPASS, "§eSet Spawn", "§7Sets the race spawn if editing a race,", "§7otherwise sets the world spawn."));

        updateToggleItem(player);
    }

    private void updateToggleItem(Player player) {
        boolean canSee = plugin.getRaceManager().canSeeSpectators(player);
        inventory.setItem(17, button(canSee ? ENDER_EYE : ENDER_PEARL, "§eSee Spectators: " + (canSee ? "§aON" : "§cOFF")));
    }

    private ItemStack button(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String editingRace = plugin.getToolManager().getEditingRace(player.getUniqueId());

        switch (event.getSlot()) {
            case 10 -> plugin.getRaceManager().prepareCreateRace(event);
            case 12 -> plugin.getRaceManager().prepareDeleteRace(event);
            case 14 -> {
                if (editingRace != null) {
                    setRaceSpawn(player, editingRace);
                } else {
                    setWorldSpawn(player);
                }
            }
            case 16 -> {
                plugin.getRaceManager().toggleSeeSpectators(player);
                updateToggleItem(player);
            }
        }
    }

    private void setRaceSpawn(Player player, String raceName) {
        plugin.getRaceManager().getRace(raceName).ifPresent(race -> {
            try {
                Location loc = player.getLocation();
                race.setSpawnLocation(loc);
                plugin.getDatabaseManager().updateRaceSpawn(raceName, loc);
                player.sendMessage("§aSpawn for race '" + raceName + "' set to your location.");
            } catch (Exception e) {
                player.sendMessage("§cFailed to save race spawn.");
            }
        });
    }

    private void setWorldSpawn(HumanEntity player) {
        WorldUtil.setWorldSpawnFromPlayerLocation(player);
        player.sendMessage("§aGlobal world spawn set to your location.");
    }
}
