package com.hishacorp.elytraracing.persistance.data;

import java.util.UUID;

public class RaceStat {
    private final UUID playerUUID;
    private final long bestTime;
    private final long bestLapTime;
    private final int wins;
    private final int rounds;
    private final int finishes;

    public RaceStat(UUID playerUUID, long bestTime, long bestLapTime, int wins, int rounds, int finishes) {
        this.playerUUID = playerUUID;
        this.bestTime = bestTime;
        this.bestLapTime = bestLapTime;
        this.wins = wins;
        this.rounds = rounds;
        this.finishes = finishes;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getBestTime() {
        return bestTime;
    }

    public long getBestLapTime() {
        return bestLapTime;
    }

    public int getWins() {
        return wins;
    }

    public int getRoundsPlayed() {
        return rounds;
    }

    public int getFinishes() {
        return finishes;
    }
}
