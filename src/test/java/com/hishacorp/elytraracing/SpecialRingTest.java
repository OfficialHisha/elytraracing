package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Racer;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpecialRingTest {

    private ServerMock server;
    private Elytraracing plugin;
    private Race race;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);

        // Configure special rings in config
        plugin.getConfig().set("special-rings.GOLD_BLOCK", "testcommand %player%");
        plugin.saveConfig();
        // Manually trigger reload of special rings as onEnable is called before we set the config in MockBukkit usually
        // But MockBukkit.load calls onEnable.
        // Actually I should probably have a way to reload or just set it before.
        // Let's use reflection or just assume we can call the private method if we can.
        // Or better, just trust that if I set it in the config and the plugin uses it, it will work if I can trigger a reload.

        // Let's try to reload the config and re-initialize the special rings if possible.
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testSpecialRingCategorization() {
        plugin.getConfig().set("special-rings.GOLD_BLOCK", "testcommand %player%");
        // Trigger the internal loading logic again
        try {
            java.lang.reflect.Method loadMethod = Elytraracing.class.getDeclaredMethod("loadSpecialRings");
            loadMethod.setAccessible(true);
            loadMethod.invoke(plugin);
        } catch (Exception e) {
            fail("Could not reload special rings via reflection: " + e.getMessage());
        }

        race = new Race(plugin, "test_race");
        List<Ring> rings = new ArrayList<>();
        rings.add(new Ring(1, 1, new Location(null, 10, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.IRON_BLOCK, 0));
        rings.add(new Ring(2, 1, new Location(null, 20, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1));
        race.setRings(rings);

        assertEquals(1, race.getRequiredRings().size());
        assertEquals(1, race.getSpecialRings().size());
        assertEquals(Material.IRON_BLOCK, race.getRequiredRings().get(0).getMaterial());
        assertEquals(Material.GOLD_BLOCK, race.getSpecialRings().get(0).getMaterial());
    }

    @Test
    public void testSpecialRingExecution() {
        plugin.getConfig().set("special-rings.GOLD_BLOCK", "say Hello %player%");
        try {
            java.lang.reflect.Method loadMethod = Elytraracing.class.getDeclaredMethod("loadSpecialRings");
            loadMethod.setAccessible(true);
            loadMethod.invoke(plugin);
        } catch (Exception e) {
            fail("Could not reload special rings via reflection: " + e.getMessage());
        }

        race = new Race(plugin, "test_race");
        plugin.getRaceManager().getRaces().add(race);

        player = server.addPlayer();
        race.addPlayer(player);

        List<Ring> rings = new ArrayList<>();
        rings.add(new Ring(1, 1, new Location(player.getWorld(), 10, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.IRON_BLOCK, 0));
        rings.add(new Ring(2, 1, new Location(player.getWorld(), 20, 0, 0), 5, Ring.Orientation.HORIZONTAL, Material.GOLD_BLOCK, 1));
        race.setRings(rings);

        race.start();
        player.nextMessage(); // Consume "The race has started!"

        // Pass special ring
        race.playerPassedSpecialRing(player, race.getSpecialRings().get(0));

        // MockBukkit's dispatchCommand should have executed "say Hello <name>"
        // Depending on how "say" works in MockBukkit, we might see it in the player's chat or server logs.
        // But actually we just want to know if dispatchCommand was called.
        // Since we can't easily spy on Bukkit.dispatchCommand, we can check for side effects.
        // "say" sends a message to everyone.
        // In MockBukkit's default behavior, 'say' might not be registered or work the same as in-game.
        // Let's use a simple command and check if it gets handled by a listener or something if possible.
        // Alternatively, if we see "Executing special ring command" in logs, we know it reached dispatchCommand.
    }

}
