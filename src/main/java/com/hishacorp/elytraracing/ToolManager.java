package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Ring;
import com.hishacorp.elytraracing.model.Ring;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.hishacorp.elytraracing.Permissions;
import com.hishacorp.elytraracing.gui.screens.RingConfigGui;
import com.hishacorp.elytraracing.util.RingRenderer;
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
    private final Map<UUID, Ring> configuring = new HashMap<>();
    private final Map<UUID, String> editingRace = new HashMap<>();

    public ToolManager(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public ItemStack getRingTool() {
        ItemStack tool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName("§6Ring Tool");
        meta.setLore(Arrays.asList("§eRight-click to create or configure a ring.", "§eLeft-click to move a ring."));
        tool.setItemMeta(meta);
        return tool;
    }

    public void giveTool(Player player, String raceName) {
        editingRace.put(player.getUniqueId(), raceName);
        ItemStack tool = getRingTool();
        ItemMeta meta = tool.getItemMeta();
        meta.setLore(Arrays.asList("§eRace: " + raceName, "§eRight-click to create or configure a ring.", "§eLeft-click to move a ring."));
        tool.setItemMeta(meta);
        player.getInventory().addItem(tool);

        try {
            int raceId = plugin.getDatabaseManager().getRaceId(raceName);
            if (raceId != -1) {
                plugin.getRingManager().loadRings(raceId);
                List<Ring> rings = plugin.getRingManager().getRings(raceId);
                if (rings != null) {
                    for (Ring ring : rings) {
                        plugin.getRingRenderer().addRing(ring, false);
                    }
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

        ItemStack toolTemplate = getRingTool();
        if (item == null || item.getType() != toolTemplate.getType() || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(toolTemplate.getItemMeta().getDisplayName())) {
            return;
        }

        if (!player.hasPermission(Permissions.TOOL.getPermission())) {
            player.sendMessage("§cYou do not have permission to use this command");
            return;
        }

        if (event.getAction().isRightClick()) {
            if (configuring.containsKey(player.getUniqueId())) {
                Ring ring = configuring.get(player.getUniqueId());
                plugin.getGuiManager().openGui(player, new RingConfigGui(plugin, ring, ring.getId() == 0, () -> {
                    configuring.remove(player.getUniqueId());
                }));
            } else {
                String raceName = editingRace.get(player.getUniqueId());
                if (raceName == null) {
                    player.sendMessage("§cYou are not editing a race. Use /er tool <race> to start editing.");
                    return;
                }
                try {
                    int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                    if (raceId == -1) {
                        player.sendMessage("§cRace not found.");
                        return;
                    }
                    Block targetBlock = player.getTargetBlock(null, 100);
                    if (targetBlock == null) {
                        player.sendMessage("§cYou are not looking at a block.");
                        return;
                    }
                    int nextIndex = 0;
                    List<Ring> rings = plugin.getRingManager().getRings(raceId);
                    if (rings != null) {
                        nextIndex = rings.size();
                    }
                    Ring newRing = new Ring(0, raceId, targetBlock.getLocation(), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, nextIndex);
                    configuring.put(player.getUniqueId(), newRing);
                    plugin.getRingRenderer().addRing(newRing, true);
                    player.sendMessage("§aStarted configuring a new ring.");
                } catch (Exception e) {
                    player.sendMessage("§cAn error occurred while creating the ring.");
                }
            }
        } else if (event.getAction().isLeftClick()) {
            if (configuring.containsKey(player.getUniqueId())) {
                Ring ring = configuring.get(player.getUniqueId());
                Block targetBlock = player.getTargetBlock(null, 100);
                if (targetBlock == null) {
                    player.sendMessage("§cYou are not looking at a block.");
                    return;
                }
                ring.setLocation(targetBlock.getLocation());
                player.sendMessage("§aRing relocated.");
            } else {
                try {
                    String raceName = editingRace.get(player.getUniqueId());
                    int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                    if (raceId != -1) {
                        for (Ring ring : plugin.getRingManager().getRings(raceId)) {
                            if (ring.getLocation().distance(player.getEyeLocation()) < 2) {
                                configuring.put(player.getUniqueId(), ring);
                                plugin.getRingRenderer().addRing(ring, true);
                                player.sendMessage("§aStarted configuring ring " + ring.getIndex());
                                plugin.getGuiManager().openGui(player, new RingConfigGui(plugin, ring, false, () -> {
                                    configuring.remove(player.getUniqueId());
                                }));
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
        configuring.remove(event.getPlayer().getUniqueId());
        editingRace.remove(event.getPlayer().getUniqueId());
    }

    public Ring getConfiguringRing(Player player) {
        return configuring.get(player.getUniqueId());
    }
}
