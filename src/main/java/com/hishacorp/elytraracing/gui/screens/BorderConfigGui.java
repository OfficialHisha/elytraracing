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

public class BorderConfigGui implements Gui {

    private final Elytraracing plugin;
    private final Player player;
    private final String raceName;
    private final Border existingBorder;
    private final Inventory inventory;

    public BorderConfigGui(Elytraracing plugin, Player player, String raceName, Border existingBorder) {
        this.plugin = plugin;
        this.player = player;
        this.raceName = raceName;
        this.existingBorder = existingBorder;
        this.inventory = Bukkit.createInventory(null, 27, existingBorder == null ? "Create Border" : "Edit Border");
        updateItems();
    }

    private void updateItems() {
        inventory.clear();
        Location[] selection = plugin.getToolManager().getSelection(player.getUniqueId());
        boolean hasSelection = selection != null && selection[0] != null && selection[1] != null;

        if (existingBorder == null) {
            inventory.setItem(11, createItem(Material.GREEN_STAINED_GLASS_PANE, "§aSave as New Border",
                    hasSelection ? "§7Points: (" + selection[0].getBlockX() + "," + selection[0].getBlockY() + "," + selection[0].getBlockZ() + ") to (" + selection[1].getBlockX() + "," + selection[1].getBlockY() + "," + selection[1].getBlockZ() + ")"
                                : "§cMissing selection points!"));
        } else {
            inventory.setItem(11, createItem(Material.YELLOW_STAINED_GLASS_PANE, "§eUpdate Border with Selection",
                    hasSelection ? "§7New Points: (" + selection[0].getBlockX() + "," + selection[0].getBlockY() + "," + selection[0].getBlockZ() + ") to (" + selection[1].getBlockX() + "," + selection[1].getBlockY() + "," + selection[1].getBlockZ() + ")"
                                : "§cMissing selection points!"));
            inventory.setItem(15, createItem(Material.RED_STAINED_GLASS_PANE, "§cDelete Border", "§7Permanently remove this border."));
        }

        inventory.setItem(13, createItem(Material.FEATHER, "§eClear Selection", "§7Clears your current border selection points."));
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
        int slot = event.getSlot();

        if (slot == 11) {
            Location[] selection = plugin.getToolManager().getSelection(player.getUniqueId());
            if (selection == null || selection[0] == null || selection[1] == null) {
                player.sendMessage("§cYou must set both Pos 1 and Pos 2 using the tool first (Shift + Click).");
                return;
            }

            plugin.getRaceManager().getRace(raceName).ifPresent(race -> {
                try {
                    int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                    if (existingBorder == null) {
                        int id = plugin.getDatabaseManager().addBorder(raceId, selection[0], selection[1]);
                        if (id != -1) {
                            race.getBorders().add(new Border(id, selection[0], selection[1]));
                            player.sendMessage("§aBorder added.");
                            selection[0] = null;
                            selection[1] = null;
                            plugin.getRingRenderer().setSelection(player, null, null);
                        }
                    } else {
                        plugin.getDatabaseManager().deleteBorder(existingBorder.getId());
                        int id = plugin.getDatabaseManager().addBorder(raceId, selection[0], selection[1]);
                        if (id != -1) {
                            race.getBorders().remove(existingBorder);
                            race.getBorders().add(new Border(id, selection[0], selection[1]));
                            player.sendMessage("§aBorder updated.");
                            selection[0] = null;
                            selection[1] = null;
                            plugin.getRingRenderer().setSelection(player, null, null);
                        }
                    }
                    plugin.getRingRenderer().setVisibleBorders(player, race.getBorders());
                    plugin.getToolManager().syncRaceView(raceName);
                    player.closeInventory();
                } catch (Exception e) {
                    player.sendMessage("§cAn error occurred.");
                }
            });
        } else if (slot == 15 && existingBorder != null) {
            plugin.getRaceManager().getRace(raceName).ifPresent(race -> {
                try {
                    plugin.getDatabaseManager().deleteBorder(existingBorder.getId());
                    race.getBorders().remove(existingBorder);
                    plugin.getRingRenderer().setVisibleBorders(player, race.getBorders());
                    plugin.getToolManager().syncRaceView(raceName);
                    player.sendMessage("§aBorder removed.");
                    player.closeInventory();
                } catch (Exception e) {
                    player.sendMessage("§cFailed to delete border.");
                }
            });
        } else if (slot == 13) {
            Location[] selection = plugin.getToolManager().getSelection(player.getUniqueId());
            if (selection != null) {
                selection[0] = null;
                selection[1] = null;
            }
            plugin.getRingRenderer().setSelection(player, null, null);
            player.sendMessage("§eSelection cleared.");
            player.closeInventory();
        }
    }
}
