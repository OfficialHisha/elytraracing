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

        updateRaceConfigItems(player);
    }

    private void updateRaceConfigItems(Player player) {
        String editingRace = plugin.getToolManager().getEditingRace(player.getUniqueId());
        if (editingRace != null) {
            plugin.getRaceManager().getRace(editingRace).ifPresent(race -> {
                inventory.setItem(19, button(REPEATER, "§eLaps: " + race.getLaps(), "§7Left-click to increase", "§7Right-click to decrease"));
                inventory.setItem(21, button(CLOCK, "§eReset Delay: " + race.getResetDelay() + "s", "§7Click to cycle through", "§70, 5, 10, 30, 60 seconds"));
                inventory.setItem(23, button(ENDER_PEARL, "§eDNF Timer: " + race.getDnfTimer() + "s", "§7Click to cycle through", "§710, 30, 60, 120, 300 seconds"));
            });
        } else {
            inventory.setItem(19, null);
            inventory.setItem(21, null);
            inventory.setItem(23, null);
        }
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
            case 19 -> {
                if (editingRace != null) {
                    plugin.getRaceManager().getRace(editingRace).ifPresent(race -> {
                        int currentLaps = race.getLaps();
                        if (event.isLeftClick()) {
                            currentLaps = (currentLaps % 10) + 1;
                        } else if (event.isRightClick()) {
                            currentLaps = (currentLaps == 1) ? 10 : currentLaps - 1;
                        }
                        race.setLaps(currentLaps);
                        try {
                            plugin.getDatabaseManager().updateRaceLaps(editingRace, currentLaps);
                            player.sendMessage("§aLaps for race '" + editingRace + "' set to " + currentLaps);
                        } catch (Exception e) {
                            player.sendMessage("§cFailed to save laps configuration.");
                        }
                        updateRaceConfigItems(player);
                    });
                }
            }
            case 23 -> {
                if (editingRace != null) {
                    plugin.getRaceManager().getRace(editingRace).ifPresent(race -> {
                        int[] timers = {10, 30, 60, 120, 300};
                        int currentTimer = race.getDnfTimer();
                        int nextTimer = timers[0];
                        for (int i = 0; i < timers.length; i++) {
                            if (timers[i] == currentTimer) {
                                nextTimer = timers[(i + 1) % timers.length];
                                break;
                            }
                        }
                        race.setDnfTimer(nextTimer);
                        try {
                            plugin.getDatabaseManager().updateRaceDnfTimer(editingRace, nextTimer);
                            player.sendMessage("§aDNF timer for race '" + editingRace + "' set to " + nextTimer + "s");
                        } catch (Exception e) {
                            player.sendMessage("§cFailed to save DNF timer configuration.");
                        }
                        updateRaceConfigItems(player);
                    });
                }
            }
            case 21 -> {
                if (editingRace != null) {
                    plugin.getRaceManager().getRace(editingRace).ifPresent(race -> {
                        int[] delays = {0, 5, 10, 30, 60};
                        int currentDelay = race.getResetDelay();
                        int nextDelay = 0;
                        for (int i = 0; i < delays.length; i++) {
                            if (delays[i] == currentDelay) {
                                nextDelay = delays[(i + 1) % delays.length];
                                break;
                            }
                        }
                        race.setResetDelay(nextDelay);
                        try {
                            plugin.getDatabaseManager().updateRaceResetDelay(editingRace, nextDelay);
                            player.sendMessage("§aReset delay for race '" + editingRace + "' set to " + nextDelay + "s");
                        } catch (Exception e) {
                            player.sendMessage("§cFailed to save reset delay configuration.");
                        }
                        updateRaceConfigItems(player);
                    });
                }
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
