package com.hishacorp.elytraracing.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class WorldUtil {
    public static void setWorldSpawnFromPlayerLocation(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();

        world.setSpawnLocation(location);
    }

    public static void setWorldSpawnFromPlayerLocation(HumanEntity player) {
        World world = player.getWorld();
        Location location = player.getLocation();

        world.setSpawnLocation(location);
    }
}
