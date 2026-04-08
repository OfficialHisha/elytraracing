package com.hishacorp.elytraracing.model;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Racer {

    private final UUID uuid;
    private final org.bukkit.Location joinLocation;
    private int currentRingIndex;
    private long startTime;
    private long finishTime;
    private boolean completed;
    private int currentLap;
    private long bestLapTime = -1;
    private long lastLapStartTime;
    private final Map<Integer, Long> specialRingCooldowns = new ConcurrentHashMap<>();

    public Racer(Player player) {
        this.uuid = player.getUniqueId();
        this.joinLocation = player.getLocation();
        this.currentRingIndex = 0;
        this.completed = false;
        this.currentLap = 1;
    }

    public org.bukkit.Location getJoinLocation() {
        return joinLocation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getCurrentRingIndex() {
        return currentRingIndex;
    }

    public void setCurrentRingIndex(int currentRingIndex) {
        this.currentRingIndex = currentRingIndex;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getCurrentLap() {
        return currentLap;
    }

    public void setCurrentLap(int currentLap) {
        this.currentLap = currentLap;
    }

    public long getBestLapTime() {
        return bestLapTime;
    }

    public void setBestLapTime(long bestLapTime) {
        this.bestLapTime = bestLapTime;
    }

    public long getLastLapStartTime() {
        return lastLapStartTime;
    }

    public void setLastLapStartTime(long lastLapStartTime) {
        this.lastLapStartTime = lastLapStartTime;
    }

    public Map<Integer, Long> getSpecialRingCooldowns() {
        return specialRingCooldowns;
    }
}
