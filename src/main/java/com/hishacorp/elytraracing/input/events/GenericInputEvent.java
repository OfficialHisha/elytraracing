package com.hishacorp.elytraracing.input.events;

import org.bukkit.entity.Player;

public class GenericInputEvent implements InputEvent {
    public Player player;
    public String value;

    public GenericInputEvent(Player player, String value) {
        this.player = player;
        this.value = value;
    }

    public Player getPlayer() {
        return player;
    }

    public String getValue() {
        return value;
    }
}
