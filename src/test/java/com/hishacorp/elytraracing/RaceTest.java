package com.hishacorp.elytraracing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RaceTest {

    private ServerMock server;
    private Elytraracing plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testStartAndEnd() {
        // Given
        Race race = new Race(plugin, "test-race");
        PlayerMock player = server.addPlayer();
        ItemStack[] originalInventory = player.getInventory().getContents();
        race.addPlayer(player);

        // When
        race.start();

        // Then
        assertNotNull(player.getInventory().getChestplate());
        assertEquals(Material.ELYTRA, player.getInventory().getChestplate().getType());
        assertTrue(player.getInventory().contains(Material.FIREWORK_ROCKET));

        // Verify replenishment
        player.getInventory().removeItem(new ItemStack(Material.FIREWORK_ROCKET, 1));
        race.startFireworkCooldown(player);
        server.getScheduler().performTicks(plugin.getConfig().getLong("firework-replenishment-cooldown") * 20);
        assertTrue(player.getInventory().contains(Material.FIREWORK_ROCKET));

        // When
        race.end();

        // Then
        assertFalse(race.isInProgress());
    }
}
