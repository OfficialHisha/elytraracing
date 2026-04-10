package com.hishacorp.elytraracing.model;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class Ring {

    private int id;
    private int raceId;
    private Location location;
    private double radius;
    private Orientation orientation;
    private Material material;
    private int index;

    public Ring(int id, int raceId, Location location, double radius, Orientation orientation, Material material, int index) {
        this.id = id;
        this.raceId = raceId;
        this.location = location;
        this.radius = radius;
        this.orientation = orientation;
        this.material = material;
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRaceId() {
        return raceId;
    }

    public void setRaceId(int raceId) {
        this.raceId = raceId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Location> getRingPoints() {
        List<Location> points = new ArrayList<>();
        Location center = location;
        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double xOffset = 0, yOffset = 0, zOffset = 0;

            switch (orientation) {
                case HORIZONTAL:
                    xOffset = radius * Math.cos(angle);
                    zOffset = radius * Math.sin(angle);
                    break;
                case VERTICAL_X:
                    yOffset = radius * Math.cos(angle);
                    zOffset = radius * Math.sin(angle);
                    break;
                case VERTICAL_Z:
                    xOffset = radius * Math.cos(angle);
                    yOffset = radius * Math.sin(angle);
                    break;
            }

            points.add(center.clone().add(xOffset, yOffset, zOffset).toBlockLocation());
        }
        return points;
    }

    public enum Orientation {
        VERTICAL_X,
        VERTICAL_Z,
        HORIZONTAL
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ring ring = (Ring) o;
        return id == ring.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
