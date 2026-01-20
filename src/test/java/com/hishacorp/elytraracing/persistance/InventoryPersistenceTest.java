package com.hishacorp.elytraracing.persistance;

import com.hishacorp.elytraracing.Elytraracing;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InventoryPersistenceTest {

    private ServerMock server;
    private Elytraracing plugin;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        databaseManager = plugin.getDatabaseManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testInventoryPersistence() {
        // Given
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();
        ItemStack[] originalInventory = player.getInventory().getContents();
        originalInventory[0] = new ItemStack(Material.DIAMOND, 64);

        // When
        databaseManager.saveInventory(playerUUID, originalInventory);
        ItemStack[] loadedInventory = databaseManager.loadInventory(playerUUID);
        databaseManager.deleteInventory(playerUUID);
        ItemStack[] deletedInventory = databaseManager.loadInventory(playerUUID);


        // Then
        assertArrayEquals(originalInventory, loadedInventory);
        assertNull(deletedInventory);
    }
}
