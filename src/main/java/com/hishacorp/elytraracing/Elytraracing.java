package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.GuiManager;
import com.hishacorp.elytraracing.input.ChatInputListener;
import com.hishacorp.elytraracing.input.InputManager;
import com.hishacorp.elytraracing.listeners.FireworkUseListener;
import com.hishacorp.elytraracing.listeners.PlayerMoveListener;
import com.hishacorp.elytraracing.listeners.PlayerQuitListener;
import com.hishacorp.elytraracing.persistance.DatabaseManager;
import com.hishacorp.elytraracing.placeholders.StatExpansion;
import com.hishacorp.elytraracing.scoreboard.ScoreboardManager;
import com.hishacorp.elytraracing.util.RingRenderer;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Elytraracing extends JavaPlugin {

    private GuiManager guiManager;
    private DatabaseManager databaseManager;
    private InputManager inputManager;
    private RaceManager raceManager;
    private ScoreboardManager scoreboardManager;
    private RingManager ringManager;
    private ToolManager toolManager;
    private RingRenderer ringRenderer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        guiManager = new GuiManager(this);
        databaseManager = new DatabaseManager(this);
        inputManager = new InputManager(this, guiManager);
        raceManager = new RaceManager(this);
        ringManager = new RingManager(this);
        toolManager = new ToolManager(this);
        ringRenderer = new RingRenderer();
        scoreboardManager = new ScoreboardManager(this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new StatExpansion(this).register();
        }

        try {
            databaseManager.connect();
        } catch (SQLException e) {
            getLogger().severe("Could not initialize database!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        raceManager.loadRaces();

        getCommand("er").setExecutor(new ERCommand(this));
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new FireworkUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(toolManager, this);

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

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public RingManager getRingManager() {
        return ringManager;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public RingRenderer getRingRenderer() {
        return ringRenderer;
    }
}
