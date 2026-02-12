package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.gui.Gui;
import com.hishacorp.elytraracing.model.Border;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class BorderManageGui implements Gui {

    private final Elytraracing plugin;
    private final String raceName;
    private final Inventory inventory;

    public BorderManageGui(Elytraracing plugin, String raceName) {
        this.plugin = plugin;
        this.raceName = raceName;
        this.inventory = Bukkit.createInventory(null, 54, "Manage Borders: " + raceName);
        updateItems();
    }

    private void updateItems() {
        inventory.clear();

        plugin.getRaceManager().getRace(raceName).ifPresent(race -> {
            List<Border> borders = race.getBorders();
            for (int i = 0; i < Math.min(borders.size(), 45); i++) {
                Border border = borders.get(i);
                inventory.setItem(i, createItem(Material.BARRIER, "§eBorder #" + (i + 1),
                        "§7Pos1: " + border.getPos1().getBlockX() + ", " + border.getPos1().getBlockY() + ", " + border.getPos1().getBlockZ(),
                        "§7Pos2: " + border.getPos2().getBlockX() + ", " + border.getPos2().getBlockY() + ", " + border.getPos2().getBlockZ(),
                        "", "§cClick to delete"));
            }
        });

        inventory.setItem(48, createItem(Material.NETHER_STAR, "§aAdd Border from Selection", "§7Uses your current Shift+Click selection."));
        inventory.setItem(50, createItem(Material.FEATHER, "§eClear Selection", "§7Clears your current border selection points."));
        inventory.setItem(49, createItem(Material.ARROW, "§7Back", "§7Return to setup menu."));
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot < 45) {
            plugin.getRaceManager().getRace(raceName).ifPresent(race -> {
                if (slot < race.getBorders().size()) {
                    Border border = race.getBorders().get(slot);
                    try {
                        plugin.getDatabaseManager().deleteBorder(border.getId());
                        race.getBorders().remove(slot);
                        plugin.getRingRenderer().setVisibleBorders(player, race.getBorders());
                        player.sendMessage("§aBorder removed.");
                        updateItems();
                    } catch (Exception e) {
                        player.sendMessage("§cFailed to delete border.");
                    }
                }
            });
        } else if (slot == 48) { // Add
            Location[] selection = plugin.getToolManager().getSelection(player.getUniqueId());
            if (selection == null || selection[0] == null || selection[1] == null) {
                player.sendMessage("§cYou must set both Pos 1 and Pos 2 using the tool first (Shift + Click).");
                return;
            }
            plugin.getRaceManager().getRace(raceName).ifPresent(race -> {
                try {
                    int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                    int id = plugin.getDatabaseManager().addBorder(raceId, selection[0], selection[1]);
                    if (id != -1) {
                        Border newBorder = new Border(id, selection[0], selection[1]);
                        race.getBorders().add(newBorder);
                        plugin.getRingRenderer().setVisibleBorders(player, race.getBorders());
                        player.sendMessage("§aBorder added.");
                        selection[0] = null;
                        selection[1] = null;
                        plugin.getRingRenderer().setSelection(player, null, null);
                        updateItems();
                    }
                } catch (Exception e) {
                    player.sendMessage("§cFailed to add border.");
                }
            });
        } else if (slot == 50) { // Clear Selection
            Location[] selection = plugin.getToolManager().getSelection(player.getUniqueId());
            if (selection != null) {
                selection[0] = null;
                selection[1] = null;
            }
            plugin.getRingRenderer().setSelection(player, null, null);
            player.sendMessage("§eSelection cleared.");
        } else if (slot == 49) { // Back
            plugin.getGuiManager().openGui(player, new SetupGui(plugin));
        }
    }
}
