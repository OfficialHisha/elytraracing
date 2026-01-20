package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.GuiManager;
import com.hishacorp.elytraracing.input.ChatInputListener;
import com.hishacorp.elytraracing.input.InputManager;
import com.hishacorp.elytraracing.persistance.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Elytraracing extends JavaPlugin {

    private GuiManager guiManager;
    private DatabaseManager databaseManager;
    private InputManager inputManager;
    private RaceManager raceManager;

    @Override
    public void onEnable() {
        guiManager = new GuiManager(this);
        databaseManager = new DatabaseManager(this);
        inputManager = new InputManager(this, guiManager);
        raceManager = new RaceManager(this);

        try {
            databaseManager.connect();
        } catch (SQLException e) {
            getLogger().severe("Could not initialize database!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("er").setExecutor(new ERCommand(this));
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        getLogger().info("ElytraRacing enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public RaceManager getRaceManager() {
        return raceManager;
    }
}
