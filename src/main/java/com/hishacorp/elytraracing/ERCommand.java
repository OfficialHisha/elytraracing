package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.gui.screens.SetupGui;
import com.hishacorp.elytraracing.gui.screens.TeleportGui;
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

                plugin.getGuiManager().openGui(player, new SetupGui(plugin, player));
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

            case "spectate" -> {
                if (!sender.hasPermission(SPECTATE.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /er spectate <race>");
                    return true;
                }
                if (sender instanceof Player player) {
                    plugin.getRaceManager().spectateRace(player, args[1]);
                }
            }

            case "tp" -> {
                if (!sender.hasPermission(TP.getPermission())) {
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                plugin.getRaceManager().getRace(player).ifPresentOrElse(race -> {
                    if (race.getSpectators().containsKey(player.getUniqueId())) {
                        plugin.getGuiManager().openGui(player, new TeleportGui(plugin, race));
                    } else {
                        player.sendMessage("§cYou must be spectating a race to use this command.");
                    }
                }, () -> player.sendMessage("§cYou are not in a race."));
            }

            case "leave" -> {
                if (sender instanceof Player player) {
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

            default -> sender.sendMessage("§cUnknown subcommand.");
        }

        return true;
    }
}

