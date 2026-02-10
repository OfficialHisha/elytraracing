package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.Race;
import com.hishacorp.elytraracing.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeleportGui implements Gui {

    private final Elytraracing plugin;
    private final Race race;
    private final Inventory inventory;
    private final List<UUID> racerUuids = new ArrayList<>();

    public TeleportGui(Elytraracing plugin, Race race) {
        this.plugin = plugin;
        this.race = race;
        this.inventory = Bukkit.createInventory(null, 54, "§8Teleport to Racer");
        updateInventory();
    }

    private void updateInventory() {
        inventory.clear();
        racerUuids.clear();

        for (UUID uuid : race.getRacers().keySet()) {
            Player racer = Bukkit.getPlayer(uuid);
            if (racer != null && racer.isOnline()) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(racer);
                    meta.setDisplayName("§a" + racer.getName());
                    List<String> lore = new ArrayList<>();
                    lore.add("§eClick to teleport!");
                    meta.setLore(lore);
                    head.setItemMeta(meta);
                }
                inventory.addItem(head);
                racerUuids.add(uuid);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != inventory) return;

        int slot = event.getSlot();
        if (slot >= 0 && slot < racerUuids.size()) {
            Player spectator = (Player) event.getWhoClicked();
            Player target = Bukkit.getPlayer(racerUuids.get(slot));

            if (target != null && target.isOnline()) {
                spectator.teleport(target.getLocation());
                spectator.sendMessage("§aTeleported to " + target.getName());
                spectator.closeInventory();
            } else {
                spectator.sendMessage("§cThat player is no longer online.");
                updateInventory();
            }
        }
    }
}
