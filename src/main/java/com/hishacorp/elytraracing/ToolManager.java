package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.screens.RingConfigGui;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToolManager implements Listener {

    private final Elytraracing plugin;
    private final Map<UUID, String> editingRace = new HashMap<>();

    public ToolManager(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public ItemStack getRingTool() {
        ItemStack tool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Ring Tool");
            meta.setLore(Arrays.asList("§eRight-click to create or configure a ring.", "§eLeft-click to move a ring."));
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
            meta.setLore(Arrays.asList("§eRace: " + lowerCaseRaceName, "§eRight-click to create or configure a ring.", "§eLeft-click to move a ring."));
            tool.setItemMeta(meta);
        }
        player.getInventory().addItem(tool);

        try {
            int raceId = plugin.getDatabaseManager().getRaceId(lowerCaseRaceName);
            if (raceId != -1) {
                plugin.getRingManager().loadRings(raceId);
                for (Ring ring : plugin.getRingManager().getRings(raceId)) {
                    plugin.getRingRenderer().addRingForPlayer(player, ring);
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

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().contains("Ring Tool")) {
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

        if (event.getAction().isRightClick()) {
            if (currentlyConfiguring != null) {
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

                    int nextIndex = plugin.getRingManager().getRings(raceId).size();
                    Ring newRing = new Ring(0, raceId, ringLocation, 5, orientation, Material.GOLD_BLOCK, nextIndex);
                    plugin.getRingRenderer().setConfiguringRingForPlayer(player, newRing);
                    player.sendMessage("§aStarted configuring a new ring.");
                } catch (Exception e) {
                    player.sendMessage("§cAn error occurred while creating the ring.");
                }
            }
        } else if (event.getAction().isLeftClick()) {
            Block targetBlock = player.getTargetBlock(null, 100);

            if (currentlyConfiguring != null) {
                Location newLocation;
                if (targetBlock != null && targetBlock.getType() != Material.AIR) {
                    newLocation = targetBlock.getLocation();
                } else {
                    newLocation = player.getEyeLocation().add(player.getLocation().getDirection().multiply(5));
                }
                currentlyConfiguring.setLocation(newLocation);
                plugin.getRingRenderer().updatePlayerView(player); // Force a redraw
                player.sendMessage("§aRing relocated.");
            } else {
                if (targetBlock == null) {
                    return;
                }
                Ring clickedRing = plugin.getRingRenderer().getRingAtLocation(player, targetBlock.getLocation());
                if (clickedRing != null) {
                    plugin.getRingRenderer().setConfiguringRingForPlayer(player, clickedRing);
                    player.sendMessage("§aStarted configuring ring " + clickedRing.getIndex());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRingRenderer().clearRingsForPlayer(player);
        editingRace.remove(player.getUniqueId());

        // Remove the tool from the player's inventory
        ItemStack tool = getRingTool();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(tool.getItemMeta().getDisplayName())) {
                player.getInventory().remove(item);
            }
        }
    }
}
