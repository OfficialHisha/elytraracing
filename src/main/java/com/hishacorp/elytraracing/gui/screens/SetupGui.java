package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.gui.Gui;
import com.hishacorp.elytraracing.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.Material.*;

public class SetupGui implements Gui {

    private static final String title = "Elytra Racing Setup";

    private final Elytraracing plugin;
    private final Inventory inventory;

    public SetupGui(Elytraracing plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 27, title);

        inventory.setItem(11, button(ELYTRA, "§aCreate Race"));
        inventory.setItem(13, button(BARRIER, "§cDelete Race"));
        inventory.setItem(15, button(COMPASS, "§eSet Spawn"));
    }

    private ItemStack button(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        switch (event.getSlot()) {
            case 11 -> plugin.getRaceManager().prepareCreateRace(event);
            case 13 -> plugin.getRaceManager().prepareDeleteRace(event);
            case 15 -> setWorldSpawn(event.getWhoClicked());
        }
    }

    private void setWorldSpawn(HumanEntity player) {
        WorldUtil.setWorldSpawnFromPlayerLocation(player);

        player.sendMessage("§aSpawn set to your location.");
    }
}
