package com.smpgenesis.managers;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.utils.Chat;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SMPManager {
    private final SMPGenesis plugin;
    private SMPStatus status;

    public enum SMPStatus {
        WAITING, GRACE, ACTIVE
    }

    public SMPManager(SMPGenesis plugin) {
        this.plugin = plugin;
        String savedStatus = plugin.getDataManager().getConfig().getString("smp.status", "WAITING");
        try {
            this.status = SMPStatus.valueOf(savedStatus);
        } catch (IllegalArgumentException e) {
            this.status = SMPStatus.WAITING;
        }
    }

    public SMPStatus getStatus() {
        return status;
    }

    public void setStatus(SMPStatus status) {
        this.status = status;
        plugin.getDataManager().getConfig().set("smp.status", status.name());
        plugin.getDataManager().save();
    }

    /**
     * Executes the highly coordinated SMP Start Flow.
     */
    public void startSMP() {
        if (status != SMPStatus.WAITING) {
            plugin.getLogger().warning("SMP is already started.");
            return;
        }

        setStatus(SMPStatus.GRACE);

        // 1. Timer Starts
        int smpDuration = plugin.getConfig().getInt("smp.timer-duration", 7200);
        plugin.getTimerManager().startTimer(smpDuration);

        // 2. Border Shrinks
        plugin.getBorderManager().startShrink();

        // 3. Grace Begins
        int graceDuration = plugin.getConfig().getInt("grace.duration", 900);
        plugin.getGraceManager().startGrace(graceDuration);

        // 4. Server-wide broadcast & Actions
        for (Player p : Bukkit.getOnlinePlayers()) {
            // Give Kit
            plugin.getKitManager().giveStarterKit(p);
            
            // Apply Protection
            if (plugin.getConfig().getBoolean("first-join-protection.enabled", true)) {
                plugin.getProtectionManager().protectPlayer(p);
            }
            
            // Title & Sound
            p.showTitle(Title.title(Component.text(Chat.color("&a&lSMP STARTED")), Component.text(Chat.color("&7May the best survive!"))));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }

        Bukkit.broadcastMessage(Chat.color("&a========================================="));
        Bukkit.broadcastMessage(Chat.color("&e&l The SMP has officially started!"));
        Bukkit.broadcastMessage(Chat.color("&7 Grace period is now active."));
        Bukkit.broadcastMessage(Chat.color("&a========================================="));

        // Extra Feature: Discord Webhook
        plugin.getDiscordManager().sendWebhook("**🚀 The SMP has officially started! Good luck to all players!**");
    }

    public void stopSMP() {
        setStatus(SMPStatus.WAITING);
        plugin.getTimerManager().stopTimer();
        plugin.getGraceManager().stopGrace();
        Bukkit.broadcastMessage(Chat.color("&c&lThe SMP has been forcibly stopped by an administrator."));
    }
}