package com.hishacorp.elytraracing.input.events;

import org.bukkit.entity.Player;

public class CreateRaceInputEvent implements InputEvent {
    public Player player;
    public String raceName;

    public CreateRaceInputEvent(Player player, String raceName) {
        this.player = player;
        this.raceName = raceName;
    }
}
