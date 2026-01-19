package com.hishacorp.elytraracing.input;

import com.hishacorp.elytraracing.Elytraracing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatInputListener implements Listener {

    private final Elytraracing plugin;

    public ChatInputListener(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        var input = plugin.getInputManager();

        if (input.invokeAwaitingInput(player, event.getMessage().trim())) {
            event.setCancelled(true);
        }
    }
}
