package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.listeners.PlayerMoveListener;
import com.hishacorp.elytraracing.model.Border;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BorderTest {

    private ServerMock server;
    private Elytraracing plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        plugin.getRaceManager().getRaces().clear();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testMultipleBorders() {
        PlayerMock player = server.addPlayer();
        Location joinLoc = new Location(player.getWorld(), 0, 100, 0);
        player.teleport(joinLoc);

        Race race = new Race(plugin, "test-race");
        Location spawnLoc = new Location(player.getWorld(), 1, 101, 1);
        race.setSpawnLocation(spawnLoc);
        race.addPlayer(player);

        // Border 1: 0 to 10
        Border b1 = new Border(1, new Location(player.getWorld(), -1, 0, -1), new Location(player.getWorld(), 11, 200, 11));
        // Border 2: 20 to 30
        Border b2 = new Border(2, new Location(player.getWorld(), 19, 0, 19), new Location(player.getWorld(), 31, 200, 31));

        race.setBorders(Arrays.asList(b1, b2));

        plugin.getRaceManager().getRaces().add(race);
        race.start();
        player.nextMessage(); // Consume "The race has started!"

        PlayerMoveListener listener = new PlayerMoveListener(plugin);

        // Inside Border 1
        player.teleport(new Location(player.getWorld(), 5, 100, 5));
        org.bukkit.event.player.PlayerMoveEvent e1 = new org.bukkit.event.player.PlayerMoveEvent(player, player.getLocation(), player.getLocation());
        listener.onPlayerMove(e1);
        assertEquals(5, e1.getTo().getX(), 0.001);

        // Outside both borders
        player.teleport(new Location(player.getWorld(), 15, 100, 15));
        org.bukkit.event.player.PlayerMoveEvent e2 = new org.bukkit.event.player.PlayerMoveEvent(player, player.getLocation(), player.getLocation());
        listener.onPlayerMove(e2);
        assertEquals(1, e2.getTo().getX(), 0.001); // Teleported back to spawnLoc
        player.assertSaid("§cYou have went out of bounds and were teleported back!");

        // Inside Border 2
        player.teleport(new Location(player.getWorld(), 25, 100, 25));
        org.bukkit.event.player.PlayerMoveEvent e3 = new org.bukkit.event.player.PlayerMoveEvent(player, player.getLocation(), player.getLocation());
        listener.onPlayerMove(e3);
        assertEquals(25, e3.getTo().getX(), 0.001);
    }
}
