package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.gui.Gui;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class RingConfigGui implements Gui {

    private final Elytraracing plugin;
    private final Ring ring;
    private final Inventory inventory;
    private final boolean isNew;
    private final List<Material> allowedMaterials = Arrays.asList(
            Material.WHITE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS,
            Material.PINK_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
            Material.BROWN_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.RED_STAINED_GLASS,
            Material.BLACK_STAINED_GLASS
    );
    private final int originalIndex;

    public RingConfigGui(Elytraracing plugin, Ring ring, boolean isNew) {
        this.plugin = plugin;
        this.ring = ring;
        this.inventory = Bukkit.createInventory(null, 27, "Configure Ring");
        this.isNew = isNew;
        this.originalIndex = ring.getIndex();

        updateItems();
    }

    public boolean isNew() {
        return isNew;
    }

    private void updateItems() {
        inventory.clear();

        inventory.setItem(10, createItem(Material.ENDER_PEARL, "§aRadius: §e" + ring.getRadius(), "§7Click to change"));
        inventory.setItem(12, createItem(Material.COMPASS, "§aOrientation: §e" + ring.getOrientation().name(), "§7Click to change"));
        inventory.setItem(14, createItem(ring.getMaterial(), "§aMaterial: §e" + ring.getMaterial().name().replace("_", " "), "§7Click to change"));
        inventory.setItem(16, createItem(Material.WRITABLE_BOOK, "§aIndex: §e" + ring.getIndex(), "§7Click to change"));

        inventory.setItem(0, createItem(Material.RED_STAINED_GLASS_PANE, "§cDelete", "§7Delete this ring"));
        inventory.setItem(26, createItem(Material.GREEN_STAINED_GLASS_PANE, "§aSave", "§7Save the ring configuration"));
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
    public void onClose(Player player) {
        // Do nothing, let the state persist
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        boolean needsUpdate = false;

        switch (event.getSlot()) {
            case 10: // Radius
                ring.setRadius(ring.getRadius() + (event.isLeftClick() ? 1 : -1));
                if (ring.getRadius() < 1) ring.setRadius(1);
                needsUpdate = true;
                break;
            case 12: // Orientation
                Ring.Orientation[] orientations = Ring.Orientation.values();
                int next = (ring.getOrientation().ordinal() + 1) % orientations.length;
                ring.setOrientation(orientations[next]);
                needsUpdate = true;
                break;
            case 14: // Material
                int nextMaterialIndex = (allowedMaterials.indexOf(ring.getMaterial()) + 1) % allowedMaterials.size();
                ring.setMaterial(allowedMaterials.get(nextMaterialIndex));
                needsUpdate = true;
                break;
            case 16: // Index
                player.closeInventory();
                player.sendMessage("§aEnter the new index for the ring in chat.");
                plugin.getInputManager().awaitChatInput(player, message -> {
                    try {
                        int newIndex = Integer.parseInt(message);
                        ring.setIndex(newIndex);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid number.");
                    }
                    plugin.getGuiManager().openGui(player, this);
                });
                break;
            case 0: // Delete
                if (!isNew) {
                    plugin.getRingManager().deleteRing(ring);
                }
                plugin.getRingRenderer().removeRingForPlayer(player, ring);
                plugin.getRingRenderer().setConfiguringRingForPlayer(player, null);
                player.closeInventory();
                break;
            case 26: // Save
                if (isNew) {
                    plugin.getRingManager().addRing(ring);
                    plugin.getRingRenderer().addRingForPlayer(player, ring);
                } else {
                    if (ring.getIndex() != originalIndex) {
                        plugin.getRingManager().updateRingWithIndexShift(ring);
                    } else {
                        plugin.getRingManager().updateRing(ring);
                    }
                }
                plugin.getRingRenderer().setConfiguringRingForPlayer(player, null);
                player.closeInventory();
                break;
        }

        if (needsUpdate) {
            plugin.getRingRenderer().updatePlayerView(player);
            updateItems();
        }
    }
}
