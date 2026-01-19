package com.hishacorp.elytraracing.input.events;

import org.bukkit.entity.Player;

public class DeleteRaceInputEvent implements InputEvent {
    public Player player;
    public String raceName;

    public DeleteRaceInputEvent(Player player, String raceName) {
        this.player = player;
        this.raceName = raceName;
    }
}
