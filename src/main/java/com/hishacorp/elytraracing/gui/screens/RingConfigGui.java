package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.RingManager;
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
    private final List<Material> allowedMaterials = Arrays.asList(Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.REDSTONE_BLOCK);
    private final Runnable onCloseCallback;
    private final Ring originalRing;
    private boolean saved = false;

    public RingConfigGui(Elytraracing plugin, Ring ring, boolean isNew, Runnable onCloseCallback) {
        this.plugin = plugin;
        this.ring = ring;
        this.inventory = Bukkit.createInventory(null, 27, "Configure Ring");
        this.isNew = isNew;
        this.onCloseCallback = onCloseCallback;
        this.originalRing = new Ring(ring.getId(), ring.getRaceId(), ring.getLocation(), ring.getRadius(), ring.getOrientation(), ring.getMaterial(), ring.getIndex());

        // Add items to the inventory
        updateItems();
    }

    public boolean isNew() {
        return isNew;
    }

    private void updateItems() {
        inventory.clear();

        // Radius controls
        inventory.setItem(10, createItem(Material.ENDER_PEARL, "§aRadius: §e" + ring.getRadius(), "§7Click to change"));

        // Orientation controls
        inventory.setItem(12, createItem(Material.COMPASS, "§aOrientation: §e" + ring.getOrientation().name(), "§7Click to change"));

        // Material controls
        inventory.setItem(14, createItem(ring.getMaterial(), "§aMaterial: §e" + ring.getMaterial().name(), "§7Click to change"));

        // Index controls
        inventory.setItem(16, createItem(Material.WRITABLE_BOOK, "§aIndex: §e" + ring.getIndex(), "§7Click to change"));

        // Save and Delete buttons
        inventory.setItem(26, createItem(Material.GREEN_STAINED_GLASS_PANE, "§aSave", "§7Save the ring configuration"));
        inventory.setItem(0, createItem(Material.RED_STAINED_GLASS_PANE, "§cDelete", "§7Delete this ring"));
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClose(Player player) {
        if (!saved) {
            ring.setLocation(originalRing.getLocation());
            ring.setRadius(originalRing.getRadius());
            ring.setOrientation(originalRing.getOrientation());
            ring.setMaterial(originalRing.getMaterial());
            ring.setIndex(originalRing.getIndex());
        }
        if (isNew && !saved) {
            plugin.getRingRenderer().removeRingForPlayer(player, ring);
        } else {
            plugin.getRingRenderer().addRingForPlayer(player, ring);
        }
        onCloseCallback.run();
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        switch (event.getSlot()) {
            case 10:
                // Change radius
                ring.setRadius(ring.getRadius() + (event.isLeftClick() ? 1 : -1));
                if (ring.getRadius() < 1) ring.setRadius(1);
                updateItems();
                break;
            case 12:
                // Change orientation
                Ring.Orientation[] orientations = Ring.Orientation.values();
                int next = (ring.getOrientation().ordinal() + 1) % orientations.length;
                ring.setOrientation(orientations[next]);
                updateItems();
                break;
            case 14:
                // Change material
                int nextMaterialIndex = (allowedMaterials.indexOf(ring.getMaterial()) + 1) % allowedMaterials.size();
                ring.setMaterial(allowedMaterials.get(nextMaterialIndex));
                updateItems();
                break;
            case 16:
                // Change index
                player.sendMessage("§aEnter the new index for the ring in chat.");
                plugin.getInputManager().awaitChatInput(player, message -> {
                    try {
                        int newIndex = Integer.parseInt(message);
                        ring.setIndex(newIndex);
                        plugin.getGuiManager().openGui(player, this);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid number.");
                    }
                });
                break;
            case 0:
                // Delete
                if (!isNew) {
                    plugin.getRingManager().deleteRing(ring);
                }
                plugin.getRingRenderer().removeRingForPlayer(player, ring);
                saved = true;
                player.closeInventory();
                break;
            case 26:
                // Save
                if (isNew) {
                    plugin.getRingManager().addRing(ring);
                } else {
                    plugin.getRingManager().updateRing(ring);
                }
                plugin.getRingRenderer().addRingForPlayer(player, ring);
                saved = true;
                player.closeInventory();
                break;
        }
    }
}
