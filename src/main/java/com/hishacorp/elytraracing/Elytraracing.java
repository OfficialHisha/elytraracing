package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.GuiManager;
import com.hishacorp.elytraracing.input.ChatInputListener;
import com.hishacorp.elytraracing.input.InputManager;
import com.hishacorp.elytraracing.listeners.FireworkUseListener;
import com.hishacorp.elytraracing.listeners.PlayerDamageListener;
import com.hishacorp.elytraracing.listeners.PlayerJoinListener;
import com.hishacorp.elytraracing.listeners.PlayerMoveListener;
import com.hishacorp.elytraracing.listeners.PlayerQuitListener;
import com.hishacorp.elytraracing.listeners.ToolListener;
import com.hishacorp.elytraracing.persistance.DatabaseManager;
import com.hishacorp.elytraracing.placeholders.StatExpansion;
import com.hishacorp.elytraracing.scoreboard.ScoreboardManager;
import com.hishacorp.elytraracing.util.RingRenderer;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Elytraracing extends JavaPlugin {

    private GuiManager guiManager;
    private DatabaseManager databaseManager;
    private InputManager inputManager;
    private RaceManager raceManager;
    private ScoreboardManager scoreboardManager;
    private RingManager ringManager;
    private ToolManager toolManager;
    private RingRenderer ringRenderer;
    private final Map<Material, SpecialRingConfig> specialRings = new HashMap<>();

    public record SpecialRingConfig(String command, long cooldown, boolean global, boolean enabled) {}

    @Override
    public void onEnable() {
        saveDefaultConfig();

        guiManager = new GuiManager(this);
        databaseManager = new DatabaseManager(this);
        inputManager = new InputManager(this, guiManager);
        ringManager = new RingManager(this);
        raceManager = new RaceManager(this);
        toolManager = new ToolManager(this);
        ringRenderer = raceManager.getRingRenderer();
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

        loadSpecialRings();
        raceManager.loadRaces();

        getCommand("er").setExecutor(new ERCommand(this));
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new FireworkUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolListener(this), this);
        getServer().getPluginManager().registerEvents(toolManager, this);

        getLogger().info("ElytraRacing enabled!");
    }

    @Override
    public void onDisable() {
        if (ringRenderer != null) {
            ringRenderer.clearAllEntities();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void loadSpecialRings() {
        specialRings.clear();
        if (getConfig().contains("special-rings")) {
            org.bukkit.configuration.ConfigurationSection section = getConfig().getConfigurationSection("special-rings");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    try {
                        Material material = Material.valueOf(key.toUpperCase());
                        String command;
                        long cooldown = 1000;
                        boolean global = false;
                        boolean enabled = true;

                        if (section.isConfigurationSection(key)) {
                            org.bukkit.configuration.ConfigurationSection ringSection = section.getConfigurationSection(key);
                            command = ringSection.getString("command");
                            cooldown = ringSection.getLong("cooldown", 1000);
                            global = ringSection.getBoolean("global-cooldown", false);
                            enabled = ringSection.getBoolean("enabled", true);
                        } else {
                            command = section.getString(key);
                        }

                        if (command != null) {
                            specialRings.put(material, new SpecialRingConfig(command, cooldown, global, enabled));
                        }
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Invalid material in special-rings config: " + key);
                    }
                }
            }
        }
    }

    public SpecialRingConfig getSpecialRingConfig(Material material) {
        return specialRings.get(material);
    }

    public boolean isSpecialRing(Material material) {
        SpecialRingConfig config = specialRings.get(material);
        return config != null && config.enabled();
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
