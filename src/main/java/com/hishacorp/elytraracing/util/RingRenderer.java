package com.hishacorp.elytraracing.util;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.model.Border;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RingRenderer {

    private final Elytraracing plugin;
    // A map of players to the rings they are viewing because they are in a race.
    private final Map<UUID, Set<Ring>> racerVisibleRings = new ConcurrentHashMap<>();
    // A map of players to the rings they are viewing because they are editing a race.
    private final Map<UUID, Set<Ring>> editorVisibleRings = new ConcurrentHashMap<>();
    // A map of players to the ring they are currently configuring.
    private final Map<UUID, Ring> playerConfiguringRing = new ConcurrentHashMap<>();
    // A map of players to the locations of the blocks that have been changed for them (for borders).
    private final Map<UUID, Map<Location, BlockData>> playerBorderOriginalBlocks = new ConcurrentHashMap<>();
    // A map of players to a map of block locations to the ring they belong to.
    private final Map<UUID, Map<Location, Ring>> playerRingBlocks = new ConcurrentHashMap<>();
    // A map of players to the entities they are viewing for rings.
    private final Map<UUID, Map<Ring, List<BlockDisplay>>> playerRingEntities = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerNextRing = new ConcurrentHashMap<>();
    // A map of players to the borders they are viewing because they are editing a race.
    private final Map<UUID, List<Border>> editorVisibleBorders = new ConcurrentHashMap<>();
    private final Map<UUID, Border> playerSelection = new ConcurrentHashMap<>();
    private static final Material HIGHLIGHT_MATERIAL = Material.GOLD_BLOCK;

    public RingRenderer(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public void addRingForPlayer(Player player, Ring ring) {
        editorVisibleRings.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(ring);
        playerRingBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(ring.getLocation(), ring);
        updatePlayerView(player);
    }

    public void removeRingForPlayer(Player player, Ring ring) {
        Set<Ring> rings = editorVisibleRings.get(player.getUniqueId());
        if (rings != null) {
            rings.remove(ring);
        }
        updatePlayerView(player);
    }

    public void setConfiguringRingForPlayer(Player player, Ring ring) {
        if (ring == null) {
            playerConfiguringRing.remove(player.getUniqueId());
        } else {
            playerConfiguringRing.put(player.getUniqueId(), ring);
        }
        updatePlayerView(player);
    }

    public void clearRacerView(Player player) {
        racerVisibleRings.remove(player.getUniqueId());
        playerNextRing.remove(player.getUniqueId());
        updatePlayerView(player);
    }

    public void clearEditorView(Player player) {
        editorVisibleRings.remove(player.getUniqueId());
        editorVisibleBorders.remove(player.getUniqueId());
        playerConfiguringRing.remove(player.getUniqueId());
        playerSelection.remove(player.getUniqueId());
        updatePlayerView(player);
    }

    public void clearRingsForPlayer(Player player) {
        racerVisibleRings.remove(player.getUniqueId());
        editorVisibleRings.remove(player.getUniqueId());
        playerConfiguringRing.remove(player.getUniqueId());
        Map<Location, Ring> ringBlocks = playerRingBlocks.get(player.getUniqueId());
        if (ringBlocks != null) ringBlocks.clear();
        playerNextRing.remove(player.getUniqueId());
        editorVisibleBorders.remove(player.getUniqueId());
        playerSelection.remove(player.getUniqueId());
        revertBlocksForPlayer(player);
    }

    public Ring getPlayerConfiguringRing(UUID uniqueId) {
        return playerConfiguringRing.get(uniqueId);
    }

    public Ring getRingAtLocation(Player player, Location location) {
        Map<Location, Ring> ringBlocks = playerRingBlocks.get(player.getUniqueId());
        if (ringBlocks != null) {
            return ringBlocks.get(location.toBlockLocation());
        }
        return null;
    }

    public Map<Location, Ring> getPlayerRingBlocks(UUID uniqueId) {
        return playerRingBlocks.get(uniqueId);
    }

    public void updatePlayerView(Player player) {
        // Revert any existing blocks first
        revertBlocksForPlayer(player);

        // Clear the mapping of blocks to rings for this player
        Map<Location, Ring> ringBlocks = playerRingBlocks.get(player.getUniqueId());
        if (ringBlocks != null) ringBlocks.clear();

        // Determine all rings that should be visible
        Set<Ring> visibleRings = new HashSet<>();
        Set<Ring> racerRings = racerVisibleRings.get(player.getUniqueId());
        if (racerRings != null) visibleRings.addAll(racerRings);

        Set<Ring> editorRings = editorVisibleRings.get(player.getUniqueId());
        if (editorRings != null && plugin.getToolManager().isHoldingTool(player)) {
            visibleRings.addAll(editorRings);
        }

        // Draw all the rings
        for (Ring ring : visibleRings) {
            drawRing(player, ring, false);
        }

        // Redraw the configuring ring with a different material
        Ring configuringRing = playerConfiguringRing.get(player.getUniqueId());
        if (configuringRing != null) {
            drawRing(player, configuringRing, true);
        }

        // Determine all borders that should be visible
        List<Border> visibleBorders = new ArrayList<>();

        if (plugin.getToolManager().isHoldingTool(player)) {
            List<Border> editorBorders = editorVisibleBorders.get(player.getUniqueId());
            if (editorBorders != null) {
                visibleBorders.addAll(editorBorders);
            }
        }

        // Draw saved borders
        for (Border border : visibleBorders) {
            drawBorder(player, border, Material.LIME_STAINED_GLASS, visibleBorders);
        }

        // Draw selection
        Border selection = playerSelection.get(player.getUniqueId());
        if (selection != null) {
            drawBorder(player, selection, Material.ORANGE_STAINED_GLASS, visibleBorders);
        }
    }

    private void drawBorder(Player player, Border border, Material material, List<Border> visibleBorders) {
        Location pos1 = border.getPos1();
        Location pos2 = border.getPos2();

        if (pos1 == null || pos2 == null) return;
        if (pos1.getWorld() == null || !pos1.getWorld().equals(player.getWorld())) return;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        org.bukkit.World world = pos1.getWorld();
        BlockData blockData = material.createBlockData();
        Map<Location, BlockData> originalBlocks = playerBorderOriginalBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        List<Border> otherBorders = new ArrayList<>(visibleBorders);
        Border selection = playerSelection.get(player.getUniqueId());
        if (selection != null) otherBorders.add(selection);

        otherBorders.removeIf(b -> b == border || (b.getId() != 0 && b.getId() == border.getId()));

        // Draw edges along X
        for (int x = minX; x <= maxX; x++) {
            setGhostBlockIfOutside(player, world, x, minY, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, x, maxY, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, x, minY, maxZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, x, maxY, maxZ, blockData, originalBlocks, otherBorders);
        }

        // Draw edges along Y
        for (int y = minY; y <= maxY; y++) {
            setGhostBlockIfOutside(player, world, minX, y, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, y, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, minX, y, maxZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, y, maxZ, blockData, originalBlocks, otherBorders);
        }

        // Draw edges along Z
        for (int z = minZ; z <= maxZ; z++) {
            setGhostBlockIfOutside(player, world, minX, minY, z, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, minX, maxY, z, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, minY, z, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, maxY, z, blockData, originalBlocks, otherBorders);
        }
    }

    private void setGhostBlockIfOutside(Player player, org.bukkit.World world, int x, int y, int z, BlockData data, Map<Location, BlockData> originalBlocks, List<Border> others) {
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) return;
        Location loc = new Location(world, x, y, z);
        if (!loc.isChunkLoaded() && !player.getName().startsWith("Player")) return;

        for (Border other : others) {
            if (other.isInside(loc)) return;
        }

        Location blockLoc = loc.toBlockLocation();
        if (!originalBlocks.containsKey(blockLoc)) {
            originalBlocks.put(blockLoc, blockLoc.getBlock().getBlockData());
        }
        player.sendBlockChange(blockLoc, data);
    }

    public void showRaceRings(Player player, List<Ring> rings, int nextRingIndex) {
        racerVisibleRings.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).addAll(rings);
        playerNextRing.put(player.getUniqueId(), nextRingIndex);
        updatePlayerView(player);
    }

    public void showSpectatorRings(Player player, List<Ring> rings) {
        racerVisibleRings.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).addAll(rings);
        playerNextRing.remove(player.getUniqueId());
        updatePlayerView(player);
    }

    public void hideRaceRings(Player player, List<Ring> rings) {
        Set<Ring> visibleRings = racerVisibleRings.get(player.getUniqueId());
        if (visibleRings != null) {
            visibleRings.removeAll(rings);
        }
        playerNextRing.remove(player.getUniqueId());
        updatePlayerView(player);
    }

    public void updateRingHighlight(Player player, Ring oldRing, Ring newRing) {
        Integer nextRingIndex = playerNextRing.get(player.getUniqueId());
        if (nextRingIndex != null && newRing != null) {
            playerNextRing.put(player.getUniqueId(), newRing.getIndex());
        }
        updatePlayerView(player);
    }

    private void revertBlocksForPlayer(Player player) {
        Map<Location, BlockData> originalBlocks = playerBorderOriginalBlocks.get(player.getUniqueId());
        if (originalBlocks != null) {
            for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
                player.sendBlockChange(entry.getKey(), entry.getValue());
            }
            originalBlocks.clear();
        }

        Map<Ring, List<BlockDisplay>> ringEntities = playerRingEntities.get(player.getUniqueId());
        if (ringEntities != null) {
            ringEntities.values().forEach(list -> list.forEach(org.bukkit.entity.Entity::remove));
            ringEntities.clear();
        }
    }

    public void clearAllEntities() {
        for (Map<Ring, List<BlockDisplay>> ringEntities : playerRingEntities.values()) {
            ringEntities.values().forEach(list -> list.forEach(org.bukkit.entity.Entity::remove));
            ringEntities.clear();
        }
        playerRingEntities.clear();
    }

    public void revertPlayerView(Player player) {
        revertBlocksForPlayer(player);
    }

    /**
     * Refresh the player's view by re-drawing rings and borders.
     * Unlike updatePlayerView, this doesn't revert first to minimize flicker
     * and ensures blocks that may have been unloaded/reloaded are updated.
     */
    public void refreshPlayerView(Player player) {
        // Only refresh if player is in a race or holding the tool
        boolean isHoldingTool = plugin.getToolManager().isHoldingTool(player);
        boolean inRace = plugin.getRaceManager().isPlayerInRace(player);

        if (!isHoldingTool && !inRace) {
            revertBlocksForPlayer(player);
            return;
        }

        // Determine all rings that should be visible
        Set<Ring> visibleRings = new HashSet<>();
        Set<Ring> racerRings = racerVisibleRings.get(player.getUniqueId());
        if (racerRings != null) visibleRings.addAll(racerRings);

        Set<Ring> editorRings = editorVisibleRings.get(player.getUniqueId());
        if (editorRings != null && isHoldingTool) {
            visibleRings.addAll(editorRings);
        }

        // Draw all the rings
        for (Ring ring : visibleRings) {
            drawRing(player, ring, false);
        }

        // Redraw the configuring ring
        Ring configuringRing = playerConfiguringRing.get(player.getUniqueId());
        if (configuringRing != null) {
            drawRing(player, configuringRing, true);
        }

        // Determine all borders that should be visible
        List<Border> visibleBorders = new ArrayList<>();

        if (isHoldingTool) {
            List<Border> editorBorders = editorVisibleBorders.get(player.getUniqueId());
            if (editorBorders != null) {
                visibleBorders.addAll(editorBorders);
            }
        }

        // Redraw saved borders
        for (Border border : visibleBorders) {
            drawBorder(player, border, Material.LIME_STAINED_GLASS, visibleBorders);
        }

        // Redraw selection
        Border selection = playerSelection.get(player.getUniqueId());
        if (selection != null) {
            drawBorder(player, selection, Material.ORANGE_STAINED_GLASS, visibleBorders);
        }
    }

    public void setVisibleRings(Player player, Set<Ring> rings) {
        editorVisibleRings.put(player.getUniqueId(), rings);
    }

    public void setVisibleBorders(Player player, List<Border> borders) {
        editorVisibleBorders.put(player.getUniqueId(), borders);
    }

    public void setSelection(Player player, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            playerSelection.remove(player.getUniqueId());
        } else {
            playerSelection.put(player.getUniqueId(), new Border(0, pos1, pos2));
        }
        updatePlayerView(player);
    }


    private void drawRing(Player player, Ring ring, boolean isBeingConfigured) {
        if (!isBeingConfigured && plugin.getRaceManager().isSpecialRingOnCooldown(player, ring)) {
            Map<Ring, List<BlockDisplay>> ringEntities = playerRingEntities.get(player.getUniqueId());
            if (ringEntities != null) {
                List<BlockDisplay> entities = ringEntities.remove(ring);
                if (entities != null) entities.forEach(org.bukkit.entity.Entity::remove);
            }
            return;
        }

        Material material = ring.getMaterial();
        Integer nextRingIndex = playerNextRing.get(player.getUniqueId());
        if (nextRingIndex != null && ring.getIndex() == nextRingIndex) {
            material = HIGHLIGHT_MATERIAL;
        }

        BlockData blockData = material.createBlockData();
        Map<Ring, List<BlockDisplay>> ringEntities = playerRingEntities.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        List<BlockDisplay> entities = ringEntities.get(ring);

        if (entities != null) {
            if (entities.isEmpty() || !entities.get(0).isValid() || !entities.get(0).getBlock().equals(blockData)) {
                entities.forEach(org.bukkit.entity.Entity::remove);
                ringEntities.remove(ring);
                entities = null;
            }
        }

        boolean spawnEntities = (entities == null);
        if (spawnEntities) {
            entities = new ArrayList<>();
        }

        Map<Location, Ring> ringBlocks = playerRingBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        for (Location blockLocation : ring.getRingPoints()) {
            // Always update ringBlocks map for tool interaction
            ringBlocks.put(blockLocation, ring);

            if (spawnEntities) {
                if (blockLocation.getWorld().equals(player.getWorld()) && (blockLocation.isChunkLoaded() || player.getName().startsWith("Player"))) {
                    BlockDisplay display = blockLocation.getWorld().spawn(blockLocation, BlockDisplay.class, entity -> {
                        entity.setBlock(blockData);
                        entity.setPersistent(false);
                        entity.setVisibleByDefault(false);
                        entity.setViewRange(10.0f);
                    });
                    player.showEntity(plugin, display);
                    entities.add(display);
                }
            }

            if (isBeingConfigured) {
                player.spawnParticle(org.bukkit.Particle.END_ROD, blockLocation.clone().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0);
            }
        }

        if (spawnEntities) {
            ringEntities.put(ring, entities);
        }
    }
}
