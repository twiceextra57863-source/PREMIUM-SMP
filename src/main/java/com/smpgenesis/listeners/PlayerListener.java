package com.smpgenesis.listeners;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.managers.SMPManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final SMPGenesis plugin;

    public PlayerListener(SMPGenesis plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        
        // Update Timer Bossbar
        if (plugin.getSmpManager().getStatus() != SMPManager.SMPStatus.WAITING) {
            plugin.getTimerManager().updateBossBarPlayers(p);
        }

        // Extra Feature: Auto Recipe Unlocker
        if (plugin.getConfig().getBoolean("extras.auto-recipe-unlocker.enabled", true)) {
            p.discoverRecipes(p.getServer().recipeIterator());
        }

        // First Join Logic
        if (!p.hasPlayedBefore()) {
            // Starter Kit on first join if SMP isn't managed via start command exclusively
            // Usually wait for /smp start, but if they join mid-SMP:
            if (plugin.getSmpManager().getStatus() != SMPManager.SMPStatus.WAITING) {
                plugin.getKitManager().giveStarterKit(p);
                if (plugin.getConfig().getBoolean("first-join-protection.enabled", true)) {
                    plugin.getProtectionManager().protectPlayer(p);
                }
            }
            // Webhook
            plugin.getDiscordManager().sendWebhook("👋 **" + p.getName() + "** has joined the SMP for the first time!");
        }

        // Extra Feature: Playtime Tracker (Store join time)
        if (plugin.getConfig().getBoolean("extras.playtime-tracker.enabled", true)) {
            plugin.getDataManager().getConfig().set("playtime.session." + p.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        // Extra Feature: Playtime Tracker (Calculate on quit)
        if (plugin.getConfig().getBoolean("extras.playtime-tracker.enabled", true)) {
            String path = "playtime.session." + p.getUniqueId();
            long joinTime = plugin.getDataManager().getConfig().getLong(path, System.currentTimeMillis());
            long sessionMillis = System.currentTimeMillis() - joinTime;
            long totalTime = plugin.getDataManager().getConfig().getLong("playtime.total." + p.getUniqueId(), 0L);
            plugin.getDataManager().getConfig().set("playtime.total." + p.getUniqueId(), totalTime + sessionMillis);
            plugin.getDataManager().getConfig().set(path, null);
            plugin.getDataManager().save();
        }
    }
}