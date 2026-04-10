package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MaterialSelectionGui implements Gui {

    private final Elytraracing plugin;
    private final Inventory inventory;
    private final Consumer<Material> onSelect;
    private final List<Material> materials;

    public MaterialSelectionGui(Elytraracing plugin, List<Material> materials, Consumer<Material> onSelect) {
        this.plugin = plugin;
        this.materials = materials;
        this.inventory = Bukkit.createInventory(null, 27, "Select a Material");
        this.onSelect = onSelect;

        populateItems();
    }

    private void populateItems() {
        for (Material material : materials) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + material.name().replace("_", " "));
                Elytraracing.SpecialRingConfig config = plugin.getSpecialRingConfig(material);
                if (config != null && config.enabled()) {
                    List<String> lore = new ArrayList<>();
                    lore.add("§7Special Ring:");
                    lore.add("§eCommand: §f" + config.command());
                    lore.add("§eCooldown: §f" + config.cooldown() + "ms");
                    lore.add("§eGlobal: §f" + (config.global() ? "Yes" : "No"));
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
            }
            inventory.addItem(item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClose(Player player) {
        // Do nothing
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null) {
            Material selectedMaterial = clickedItem.getType();
            if (materials.contains(selectedMaterial)) {
                onSelect.accept(selectedMaterial);
            }
        }
    }
}
