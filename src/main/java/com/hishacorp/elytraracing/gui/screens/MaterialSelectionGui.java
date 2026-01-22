package com.hishacorp.elytraracing.gui.screens;

import com.hishacorp.elytraracing.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

public class MaterialSelectionGui implements Gui {

    private static final String title = "Select a Material";

    private final Inventory inventory;
    private final Consumer<Material> onSelect;
    private final List<Material> materials;

    public MaterialSelectionGui(List<Material> materials, Consumer<Material> onSelect) {
        this.materials = materials;
        this.inventory = Bukkit.createInventory(null, 27, title);
        this.onSelect = onSelect;

        populateItems();
    }

    private void populateItems() {
        for (Material material : materials) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + material.name().replace("_", " "));
                item.setItemMeta(meta);
            }
            inventory.addItem(item);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null) {
            Material selectedMaterial = clickedItem.getType();
            if (materials.contains(selectedMaterial)) {
                onSelect.accept(selectedMaterial);
            }
        }
    }
}
