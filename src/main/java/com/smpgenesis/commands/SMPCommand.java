package com.smpgenesis.commands;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.managers.SMPManager;
import com.smpgenesis.managers.TimerManager;
import com.smpgenesis.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SMPCommand implements CommandExecutor {
    private final SMPGenesis plugin;

    public SMPCommand(SMPGenesis plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("smp.admin")) {
            sender.sendMessage(Chat.color("&cNo permission."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (!sender.hasPermission("smp.start")) {
                    sender.sendMessage(Chat.color("&cNo permission."));
                    return true;
                }
                plugin.getSmpManager().startSMP();
                sender.sendMessage(Chat.color("&aInitiated SMP Start sequence."));
                break;
                
            case "stop":
                plugin.getSmpManager().stopSMP();
                sender.sendMessage(Chat.color("&cSMP successfully stopped."));
                break;
                
            case "timer":
                int remaining = plugin.getTimerManager().getRemainingSeconds();
                sender.sendMessage(Chat.color("&bTimer Status: &f" + TimerManager.formatTime(remaining)));
                break;
                
            case "grace":
                if (args.length < 2) {
                    sender.sendMessage(Chat.color("&cUsage: /smp grace <on|off>"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("on")) {
                    plugin.getGraceManager().startGrace(plugin.getConfig().getInt("grace.duration", 900));
                    sender.sendMessage(Chat.color("&aGrace period activated."));
                } else if (args[1].equalsIgnoreCase("off")) {
                    plugin.getGraceManager().stopGrace();
                    sender.sendMessage(Chat.color("&cGrace period deactivated."));
                }
                break;
                
            case "border":
                if (args.length < 2) {
                    sender.sendMessage(Chat.color("&cUsage: /smp border shrink"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("shrink")) {
                    plugin.getBorderManager().startShrink();
                    sender.sendMessage(Chat.color("&aForced border shrink."));
                }
                break;
                
            case "reload":
                if (!sender.hasPermission("smp.reload")) return true;
                plugin.reloadConfig();
                sender.sendMessage(Chat.color("&aConfiguration reloaded!"));
                break;
                
            case "status":
                SMPManager.SMPStatus status = plugin.getSmpManager().getStatus();
                sender.sendMessage(Chat.color("&b&lSMP Status: &f" + status.name()));
                sender.sendMessage(Chat.color("&b&lGrace Active: &f" + plugin.getGraceManager().isGraceActive()));
                break;
                
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Chat.color("&8[&bSMPGenesis&8] &7Commands:"));
        sender.sendMessage(Chat.color("&f/smp start &7- Starts the SMP"));
        sender.sendMessage(Chat.color("&f/smp stop &7- Stops the SMP"));
        sender.sendMessage(Chat.color("&f/smp timer &7- Checks timer"));
        sender.sendMessage(Chat.color("&f/smp grace <on|off> &7- Toggles grace"));
        sender.sendMessage(Chat.color("&f/smp border shrink &7- Forces shrink"));
        sender.sendMessage(Chat.color("&f/smp status &7- View current state"));
        sender.sendMessage(Chat.color("&f/smp reload &7- Reload config"));
    }
}