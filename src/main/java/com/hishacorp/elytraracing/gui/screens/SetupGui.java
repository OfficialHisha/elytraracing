package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.gui.Gui;
import com.hishacorp.elytraracing.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.Material.*;

public class SetupGui implements Gui {
    private final Elytraracing plugin;
    private final Inventory inventory;

    public SetupGui(Elytraracing plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 27, "Elytra Racing Setup");

        inventory.setItem(10, button(ELYTRA, "§aCreate Race"));
        inventory.setItem(12, button(BARRIER, "§cDelete Race"));
        inventory.setItem(14, button(COMPASS, "§eSet Spawn"));
        inventory.setItem(16, button(IRON_BARS, "§6Manage Borders"));
    }

    private ItemStack button(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
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
        switch (event.getSlot()) {
            case 10 -> plugin.getRaceManager().prepareCreateRace(event);
            case 12 -> plugin.getRaceManager().prepareDeleteRace(event);
            case 14 -> setWorldSpawn(player);
            case 16 -> {
                String raceName = plugin.getToolManager().getEditingRace(player.getUniqueId());
                if (raceName != null) {
                    plugin.getGuiManager().openGui(player, new BorderManageGui(plugin, raceName));
                } else {
                    player.sendMessage("§cYou must be editing a race to manage its borders.");
                }
            }
        }
    }

    private void setWorldSpawn(HumanEntity player) {
        WorldUtil.setWorldSpawnFromPlayerLocation(player);

        player.sendMessage("§aSpawn set to your location.");
    }
}
