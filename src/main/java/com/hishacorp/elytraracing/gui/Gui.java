package com.hishacorp.elytraracing.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface Gui {

    Inventory getInventory();

    default void onOpen(Player player) {}

    default void onClick(InventoryClickEvent event) {}

    default void onClose(Player player) {}
}
