package com.hishacorp.elytraracing.model;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Racer {

    private final UUID uuid;
    private int currentRingIndex;
    private long startTime;
    private long finishTime;
    private boolean completed;

    public Racer(Player player) {
        this.uuid = player.getUniqueId();
        this.currentRingIndex = 0;
        this.completed = false;
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
}
