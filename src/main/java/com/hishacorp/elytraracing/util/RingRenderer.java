package com.hishacorp.elytraracing.util;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class RingRenderer extends BukkitRunnable {

    private final Elytraracing plugin;
    private final Map<Ring, Boolean> ringsToDraw = new HashMap<>();

    public RingRenderer(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public void addRing(Ring ring, boolean isBeingConfigured) {
        ringsToDraw.put(ring, isBeingConfigured);
    }

    public void removeRing(Ring ring) {
        ringsToDraw.remove(ring);
    }

    @Override
    public void run() {
        for (Map.Entry<Ring, Boolean> entry : ringsToDraw.entrySet()) {
            drawRing(entry.getKey(), entry.getValue());
        }
    }

    private void drawRing(Ring ring, boolean isBeingConfigured) {
        Particle particle = isBeingConfigured ? Particle.FLAME : Particle.HAPPY_VILLAGER;
        Location center = ring.getLocation();
        World world = center.getWorld();
        double radius = ring.getRadius();

        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = 0;
            double y = 0;
            double z = 0;

            switch (ring.getOrientation()) {
                case HORIZONTAL:
                    x = center.getX() + radius * Math.cos(angle);
                    y = center.getY();
                    z = center.getZ() + radius * Math.sin(angle);
                    break;
                case VERTICAL_X:
                    x = center.getX();
                    y = center.getY() + radius * Math.cos(angle);
                    z = center.getZ() + radius * Math.sin(angle);
                    break;
                case VERTICAL_Z:
                    x = center.getX() + radius * Math.cos(angle);
                    y = center.getY() + radius * Math.sin(angle);
                    z = center.getZ();
                    break;
            }
            world.spawnParticle(particle, x, y, z, 0);
        }
    }
}
