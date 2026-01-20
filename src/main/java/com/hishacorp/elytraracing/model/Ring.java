package com.hishacorp.elytraracing.model;

import org.bukkit.Location;
import org.bukkit.Material;

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

    public enum Orientation {
        VERTICAL_X,
        VERTICAL_Z,
        HORIZONTAL
    }
}
