package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.screens.RingConfigGui;
import com.hishacorp.elytraracing.model.Ring;
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
        editingRace.put(player.getUniqueId(), raceName);
        ItemStack tool = getRingTool();
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setLore(Arrays.asList("§eRace: " + raceName, "§eRight-click to create or configure a ring.", "§eLeft-click to move a ring."));
            tool.setItemMeta(meta);
        }
        player.getInventory().addItem(tool);

        try {
            int raceId = plugin.getDatabaseManager().getRaceId(raceName);
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
                plugin.getGuiManager().openGui(player, new RingConfigGui(plugin, currentlyConfiguring, currentlyConfiguring.getId() == 0, () -> {
                    plugin.getRingRenderer().setConfiguringRingForPlayer(player, null);
                }));
            } else {
                try {
                    int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                    if (raceId == -1) {
                        player.sendMessage("§cRace not found.");
                        return;
                    }
                    Block targetBlock = player.getTargetBlock(null, 100);
                    if (targetBlock == null || targetBlock.getType() == Material.AIR) {
                        player.sendMessage("§cYou must be looking at a block to place a ring.");
                        return;
                    }
                    int nextIndex = plugin.getRingManager().getRings(raceId).size();
                    Ring newRing = new Ring(0, raceId, targetBlock.getLocation(), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, nextIndex);
                    plugin.getRingRenderer().setConfiguringRingForPlayer(player, newRing);
                    player.sendMessage("§aStarted configuring a new ring.");
                } catch (Exception e) {
                    player.sendMessage("§cAn error occurred while creating the ring.");
                }
            }
        } else if (event.getAction().isLeftClick()) {
            if (currentlyConfiguring != null) {
                Block targetBlock = player.getTargetBlock(null, 100);
                 if (targetBlock == null || targetBlock.getType() == Material.AIR) {
                    player.sendMessage("§cYou must be looking at a block to move the ring.");
                    return;
                }
                currentlyConfiguring.setLocation(targetBlock.getLocation());
                plugin.getRingRenderer().updatePlayerView(player); // Force a redraw
                player.sendMessage("§aRing relocated.");
            } else {
                try {
                    int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                    if (raceId != -1) {
                        for (Ring ring : plugin.getRingManager().getRings(raceId)) {
                            if (ring.getLocation().distance(player.getEyeLocation()) < 2) {
                                plugin.getRingRenderer().setConfiguringRingForPlayer(player, ring);
                                player.sendMessage("§aStarted configuring ring " + ring.getIndex());
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    player.sendMessage("§cAn error occurred while selecting a ring.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getRingRenderer().clearRingsForPlayer(event.getPlayer());
        editingRace.remove(event.getPlayer().getUniqueId());
    }
}
