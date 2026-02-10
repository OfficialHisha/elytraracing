package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.screens.SetupGui;
import com.hishacorp.elytraracing.input.events.CreateRaceInputEvent;
import com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent;
import com.hishacorp.elytraracing.model.Ring;
import com.hishacorp.elytraracing.util.WorldUtil;
import org.bukkit.command.Command;
import java.sql.SQLException;
import java.util.List;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.hishacorp.elytraracing.Permissions.*;

public class ERCommand implements CommandExecutor {

    private final Elytraracing plugin;

    public ERCommand(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUsage: /er <setup|tool|create|delete|time|setspawn|resetstats|join|leave|rings>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start" -> {
                if (!sender.hasPermission(START.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er start <race>");
                    return true;
                }
                plugin.getRaceManager().startRace(sender, args[1]);
            }
            case "end" -> {
                if (!sender.hasPermission(END.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er end <race>");
                    return true;
                }
                plugin.getRaceManager().endRace(sender, args[1]);
            }
            case "setup" -> {
                if (!sender.hasPermission(SETUP.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    return true;
                }

                plugin.getGuiManager().openGui(player, new SetupGui(plugin));
            }

            case "tool" -> {
                if (!sender.hasPermission(TOOL.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er tool <race>");
                    return true;
                }

                String raceName = args[1].toLowerCase();

                if (plugin.getRaceManager().getRace(raceName).isEmpty()) {
                    player.sendMessage("§cRace not found: " + raceName);
                    return true;
                }

                plugin.getToolManager().giveTool(player, raceName);
                player.sendMessage("§aYou have been given the ring tool for race " + raceName + ".");
            }

            case "create" -> {
                if (!sender.hasPermission(CREATE.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er create <name>");
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }

                plugin.getRaceManager().createRace(new CreateRaceInputEvent(player, args[1].toLowerCase(), player.getWorld().getName()));
            }

            case "delete" -> {
                if (!sender.hasPermission(DELETE.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er delete <name>");
                    return true;
                }
                if (sender instanceof Player player) {
                    plugin.getRaceManager().deleteRace(new DeleteRaceInputEvent(player, args[1].toLowerCase()));
                }
            }

            case "setspawn" -> {
                if (!sender.hasPermission(SETSPAWN.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }

                WorldUtil.setWorldSpawnFromPlayerLocation(player);

                player.sendMessage("§aSpawn set to your location.");
            }

            case "resetstats" -> {
                if (!sender.hasPermission(STATS.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er resetstats <player|race>");
                    return true;
                }
                sender.sendMessage("Resetting stats for " + args[1]);
            }

            case "join" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er join <race>");
                    return true;
                }
                if (sender instanceof Player player) {
                    plugin.getRaceManager().joinRace(player, args[1]);
                }
            }

            case "leave" -> {
                if (sender instanceof Player player) {
                    plugin.getScoreboardManager().removeScoreboard(player);
                    plugin.getRaceManager().leaveRace(player);
                }
            }

            case "rings" -> {
                if (!sender.hasPermission(RINGS.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er rings <race>");
                    return true;
                }
                try {
                    int raceId = plugin.getDatabaseManager().getRaceId(args[1]);
                    if (raceId == -1) {
                        sender.sendMessage("§cRace not found.");
                        return true;
                    }
                    List<Ring> rings = plugin.getDatabaseManager().getRings(raceId);
                    if (rings.isEmpty()) {
                        sender.sendMessage("§eNo rings found for this race.");
                        return true;
                    }
                    sender.sendMessage("§aRings for race " + args[1] + ":");
                    for (Ring ring : rings) {
                        sender.sendMessage("§e- Ring " + ring.getIndex() + " at " + ring.getLocation().toString());
                    }
                } catch (SQLException e) {
                    sender.sendMessage("§cAn error occurred while fetching the rings.");
                    plugin.getLogger().severe("Failed to fetch rings: " + e.getMessage());
                }
            }

            case "border" -> {
                if (!sender.hasPermission(TOOL.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }

                String raceName = plugin.getToolManager().getEditingRace(player.getUniqueId());
                if (raceName == null) {
                    player.sendMessage("§cYou are not editing a race. Use /er tool <race> first.");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§eUsage: /er border <add|list|remove|clear>");
                    return true;
                }

                String borderSub = args[1].toLowerCase();
                plugin.getRaceManager().getRace(raceName).ifPresentOrElse(race -> {
                    try {
                        int raceId = plugin.getDatabaseManager().getRaceId(raceName);
                        switch (borderSub) {
                            case "add" -> {
                                org.bukkit.Location[] selection = plugin.getToolManager().getSelection(player.getUniqueId());
                                if (selection == null || selection[0] == null || selection[1] == null) {
                                    player.sendMessage("§cYou must set both Pos 1 and Pos 2 using the tool first (Shift + Click).");
                                    return;
                                }
                                plugin.getDatabaseManager().addBorder(raceId, selection[0], selection[1]);
                                player.sendMessage("§aBorder added to race " + raceName);
                                // Reload borders for the race
                                List<com.hishacorp.elytraracing.model.Border> borders = plugin.getDatabaseManager().getBorders(raceId, player.getWorld()).stream()
                                        .map(bd -> new com.hishacorp.elytraracing.model.Border(bd.id, bd.pos1, bd.pos2))
                                        .toList();
                                race.setBorders(borders);
                                plugin.getRingRenderer().setVisibleBorders(player, borders);
                            }
                            case "list" -> {
                                List<com.hishacorp.elytraracing.model.Border> borders = race.getBorders();
                                if (borders.isEmpty()) {
                                    player.sendMessage("§eNo borders defined for this race.");
                                    return;
                                }
                                player.sendMessage("§aBorders for " + raceName + ":");
                                for (int i = 0; i < borders.size(); i++) {
                                    com.hishacorp.elytraracing.model.Border b = borders.get(i);
                                    player.sendMessage("§e" + (i + 1) + ". Pos1: " + b.getPos1().getBlockX() + "," + b.getPos1().getBlockY() + "," + b.getPos1().getBlockZ() +
                                            " | Pos2: " + b.getPos2().getBlockX() + "," + b.getPos2().getBlockY() + "," + b.getPos2().getBlockZ());
                                }
                            }
                            case "remove" -> {
                                if (args.length < 3) {
                                    player.sendMessage("§cUsage: /er border remove <index>");
                                    return;
                                }
                                int index = Integer.parseInt(args[2]) - 1;
                                List<com.hishacorp.elytraracing.model.Border> borders = race.getBorders();
                                if (index < 0 || index >= borders.size()) {
                                    player.sendMessage("§cInvalid border index.");
                                    return;
                                }
                                plugin.getDatabaseManager().deleteBorder(borders.get(index).getId());
                                player.sendMessage("§aBorder removed.");
                                // Reload borders
                                List<com.hishacorp.elytraracing.model.Border> newBorders = plugin.getDatabaseManager().getBorders(raceId, player.getWorld()).stream()
                                        .map(bd -> new com.hishacorp.elytraracing.model.Border(bd.id, bd.pos1, bd.pos2))
                                        .toList();
                                race.setBorders(newBorders);
                                plugin.getRingRenderer().setVisibleBorders(player, newBorders);
                            }
                            case "clear" -> {
                                plugin.getDatabaseManager().clearBorders(raceId);
                                race.getBorders().clear();
                                plugin.getRingRenderer().setVisibleBorders(player, new java.util.ArrayList<>());
                                player.sendMessage("§aAll borders cleared for race " + raceName);
                            }
                            default -> player.sendMessage("§cUnknown border subcommand.");
                        }
                    } catch (Exception e) {
                        player.sendMessage("§cAn error occurred: " + e.getMessage());
                        plugin.getLogger().severe("Border command error: " + e.getMessage());
                    }
                }, () -> player.sendMessage("§cRace not found."));
            }

            default -> sender.sendMessage("§cUnknown subcommand.");
        }

        return true;
    }
}

