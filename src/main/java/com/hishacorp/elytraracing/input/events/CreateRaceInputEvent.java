package com.hishacorp.elytraracing.input.events;

import org.bukkit.entity.Player;

public class CreateRaceInputEvent implements InputEvent {
    public Player player;
    public String raceName;
    public String world;

    public CreateRaceInputEvent(Player player, String raceName, String world) {
        this.player = player;
        this.raceName = raceName;
        this.world = world;
    }
}
