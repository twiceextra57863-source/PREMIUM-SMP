package com.smpgenesis.managers;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class GraceManager {
    private final SMPGenesis plugin;
    private boolean isGraceActive;
    private int graceRemaining;
    private BukkitRunnable graceTask;

    public GraceManager(SMPGenesis plugin) {
        this.plugin = plugin;
        this.graceRemaining = plugin.getDataManager().getConfig().getInt("grace.remaining", 0);
        this.isGraceActive = plugin.getDataManager().getConfig().getBoolean("grace.active", false);

        if (isGraceActive && graceRemaining > 0) {
            startGrace(graceRemaining);
        }
    }

    public void startGrace(int durationSeconds) {
        this.isGraceActive = true;
        this.graceRemaining = durationSeconds;
        plugin.getSmpManager().setStatus(SMPManager.SMPStatus.GRACE);

        if (graceTask != null) graceTask.cancel();

        graceTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (graceRemaining <= 0) {
                    stopGrace();
                    this.cancel();
                    return;
                }
                graceRemaining--;
            }
        };
        graceTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopGrace() {
        this.isGraceActive = false;
        this.graceRemaining = 0;
        if (graceTask != null) graceTask.cancel();

        if (plugin.getSmpManager().getStatus() == SMPManager.SMPStatus.GRACE) {
            plugin.getSmpManager().setStatus(SMPManager.SMPStatus.ACTIVE);
            Bukkit.broadcastMessage(Chat.color("&c&l========================================="));
            Bukkit.broadcastMessage(Chat.color("&c&l GRACE PERIOD HAS ENDED!"));
            Bukkit.broadcastMessage(Chat.color("&7 PvP and damage are now fully enabled."));
            Bukkit.broadcastMessage(Chat.color("&c&l========================================="));
            
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
            });
            plugin.getDiscordManager().sendWebhook("⚔️ **The Grace Period has ended! PvP is now enabled.**");
        }
        saveState();
    }

    public boolean isGraceActive() {
        return isGraceActive;
    }

    public void saveState() {
        plugin.getDataManager().getConfig().set("grace.remaining", graceRemaining);
        plugin.getDataManager().getConfig().set("grace.active", isGraceActive);
        plugin.getDataManager().save();
    }
}