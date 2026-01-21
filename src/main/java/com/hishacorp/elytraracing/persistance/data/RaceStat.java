package com.hishacorp.elytraracing.persistance.data;

import java.util.UUID;

public class RaceStat {
    private final UUID playerUUID;
    private final long bestTime;

    public RaceStat(UUID playerUUID, long bestTime) {
        this.playerUUID = playerUUID;
        this.bestTime = bestTime;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getBestTime() {
        return bestTime;
    }
}
