package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.screens.RingConfigGui;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ToolManager implements Listener {

    private final Elytraracing plugin;
    private final Map<UUID, String> editingRace = new HashMap<>();
    private final Map<UUID, Location[]> selections = new HashMap<>();

    public ToolManager(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public ItemStack getRingTool() {
        ItemStack tool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Elytra Racing Tool");
            tool.setItemMeta(meta);
        }
        return tool;
    }

    public void giveTool(Player player, String raceName) {
        String lowerCaseRaceName = raceName.toLowerCase();
        editingRace.put(player.getUniqueId(), lowerCaseRaceName);
        ItemStack tool = getRingTool();
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setLore(Arrays.asList("§eRace: " + lowerCaseRaceName));
            tool.setItemMeta(meta);
        }
        player.getInventory().addItem(tool);

        try {
            var raceData = plugin.getDatabaseManager().getRaceData(lowerCaseRaceName);
            if (raceData != null) {
                plugin.getRingManager().loadRings(raceData.id);
                plugin.getRingRenderer().setVisibleRings(player, new java.util.HashSet<>(plugin.getRingManager().getRings(raceData.id)));
                plugin.getRaceManager().getRace(lowerCaseRaceName).ifPresent(race -> {
                    plugin.getRingRenderer().setVisibleBorders(player, race.getBorders());
                });
                if (isTool(player.getInventory().getItemInMainHand())) {
                    plugin.getRingRenderer().updatePlayerView(player);
                }
            }
        } catch (Exception e) {
            player.sendMessage("§cAn error occurred while loading the rings for this race.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isTool(item)) {
            return;
        }

        if (!player.hasPermission(Permissions.TOOL.getPermission())) {
            player.sendMessage("§cYou do not have permission to use the tool.");
            return;
        }

        String raceName = editingRace.get(player.getUniqueId());
        if (raceName == null) {
            player.sendMessage("§cYou are not editing a race. Use /er tool <race> to start editing.");
            return;
        }

        Ring currentlyConfiguring = plugin.getRingRenderer().getPlayerConfiguringRing(player.getUniqueId());

        if (player.isSneaking()) {
            event.setCancelled(true);
            Block targetBlock = player.getTargetBlockExact(100);
            Location location;
            if (targetBlock != null) {
                location = targetBlock.getLocation();
            } else {
                location = player.getEyeLocation().add(player.getLocation().getDirection().multiply(5));
            }

            Location[] selection = selections.computeIfAbsent(player.getUniqueId(), k -> new Location[2]);
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                selection[0] = location;
                player.sendMessage("§aSelection Pos 1 set to " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                selection[1] = location;
                player.sendMessage("§aSelection Pos 2 set to " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            }
            plugin.getRingRenderer().setSelection(player, selection[0], selection[1]);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Ring clickedRing = getTargetedRing(player);

            if (clickedRing != null) {
                plugin.getRingRenderer().setConfiguringRingForPlayer(player, clickedRing);
                plugin.getGuiManager().openGui(player, new RingConfigGui(plugin, clickedRing, clickedRing.getId() == 0));
            } else if (currentlyConfiguring != null) {
                plugin.getGuiManager().openGui(player, new RingConfigGui(plugin, currentlyConfiguring, currentlyConfiguring.getId() == 0));
            } else {
                try {
                    int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                    if (raceId == -1) {
                        player.sendMessage("§cRace not found.");
                        return;
                    }

                    // Create the ring 5 blocks in front of the player
                    Location ringLocation = player.getEyeLocation().add(player.getLocation().getDirection().multiply(5));

                    // Determine initial orientation based on player's direction
                    float pitch = player.getLocation().getPitch();
                    float yaw = player.getLocation().getYaw();
                    Ring.Orientation orientation;

                    if (pitch < -45 || pitch > 45) {
                        orientation = Ring.Orientation.HORIZONTAL;
                    } else {
                        yaw = (yaw % 360 + 360) % 360; // Normalize yaw
                        if (yaw >= 45 && yaw < 135 || yaw >= 225 && yaw < 315) {
                            orientation = Ring.Orientation.VERTICAL_X;
                        } else {
                            orientation = Ring.Orientation.VERTICAL_Z;
                        }
                    }

                    int nextIndex = 0;
                    if (plugin.getRingManager().getRings(raceId) != null) {
                        for (Ring ring : plugin.getRingManager().getRings(raceId)) {
                            if (ring.getIndex() >= nextIndex) {
                                nextIndex = ring.getIndex() + 1;
                            }
                        }
                    }

                    Ring newRing = new Ring(0, raceId, ringLocation, 5, orientation, Material.WHITE_STAINED_GLASS, nextIndex);
                    plugin.getRingRenderer().setConfiguringRingForPlayer(player, newRing);
                    player.sendMessage("§aStarted configuring a new ring.");
                } catch (Exception e) {
                    player.sendMessage("§cAn error occurred while creating the ring.");
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Ring clickedRing = getTargetedRing(player);

            if (currentlyConfiguring != null) {
                Location newLocation;
                if (clickedRing != null) {
                    newLocation = clickedRing.getLocation();
                } else {
                    newLocation = player.getEyeLocation().add(player.getLocation().getDirection().multiply(5));
                }
                currentlyConfiguring.setLocation(newLocation);
                plugin.getRingRenderer().updatePlayerView(player); // Force a redraw
                player.sendMessage("§aRing relocated.");
            } else {
                if (clickedRing != null) {
                    plugin.getRingRenderer().setConfiguringRingForPlayer(player, clickedRing);
                    player.sendMessage("§aStarted configuring ring " + clickedRing.getIndex());
                }
            }
        }
    }

    private Ring getTargetedRing(Player player) {
        Location eyeLocation = player.getEyeLocation();
        String raceName = editingRace.get(player.getUniqueId());
        if (raceName == null) {
            return null;
        }
        try {
            int raceId = plugin.getDatabaseManager().getRaceId(raceName);
            List<Ring> rings = plugin.getRingManager().getRings(raceId);
            if (rings == null) {
                return null;
            }

            for (double i = 0; i < 100; i += 0.5) {
                Location point = eyeLocation.clone().add(eyeLocation.getDirection().clone().multiply(i));
                for (Ring ring : rings) {
                    if (ring.getLocation().distance(point) < ring.getRadius()) {
                        return ring;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get targeted ring: " + e.getMessage());
        }
        return null;
    }

    public boolean isTool(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains("Elytra Racing Tool") || displayName.contains("Ring Tool");
    }

    public boolean isPlayerUsingTool(Player player) {
        return editingRace.containsKey(player.getUniqueId());
    }

    public void stopEditing(Player player) {
        editingRace.remove(player.getUniqueId());
        selections.remove(player.getUniqueId());
        plugin.getRingRenderer().clearRingsForPlayer(player);
    }

    public String getRaceNameFromTool(ItemStack tool) {
        if (!isTool(tool)) {
            return null;
        }
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return null;
        }
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return null;
        }
        // The race name is expected to be on the first line of the lore, prefixed with "§eRace: "
        String raceLine = lore.get(0);
        if (raceLine.startsWith("§eRace: ")) {
            return raceLine.substring("§eRace: ".length());
        }
        return null;
    }

    public void reinitializeEditing(Player player, String raceName) {
        String lowerCaseRaceName = raceName.toLowerCase();
        editingRace.put(player.getUniqueId(), lowerCaseRaceName);
        try {
            var raceData = plugin.getDatabaseManager().getRaceData(lowerCaseRaceName);
            if (raceData != null) {
                plugin.getRingManager().loadRings(raceData.id);
                plugin.getRingRenderer().setVisibleRings(player, new java.util.HashSet<>(plugin.getRingManager().getRings(raceData.id)));
                plugin.getRaceManager().getRace(lowerCaseRaceName).ifPresent(race -> {
                    plugin.getRingRenderer().setVisibleBorders(player, race.getBorders());
                });
            }
        } catch (Exception e) {
            player.sendMessage("§cAn error occurred while re-initializing the rings for this race.");
        }
    }

    public Location[] getSelection(UUID playerUuid) {
        return selections.get(playerUuid);
    }

    public String getEditingRace(UUID playerUuid) {
        return editingRace.get(playerUuid);
    }
}
