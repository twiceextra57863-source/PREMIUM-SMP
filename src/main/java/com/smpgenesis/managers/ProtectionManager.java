package com.smpgenesis.managers;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProtectionManager {
    private final SMPGenesis plugin;
    private final Map<UUID, Long> protectedPlayers = new ConcurrentHashMap<>();

    public ProtectionManager(SMPGenesis plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    public void protectPlayer(Player player) {
        int duration = plugin.getConfig().getInt("first-join-protection.duration", 600);
        long expiryTime = System.currentTimeMillis() + (duration * 1000L);
        protectedPlayers.put(player.getUniqueId(), expiryTime);
        player.sendMessage(Chat.color("&aYou have first-join protection for " + (duration / 60) + " minutes."));
    }

    public boolean isProtected(UUID uuid) {
        if (!protectedPlayers.containsKey(uuid)) return false;
        if (System.currentTimeMillis() > protectedPlayers.get(uuid)) {
            protectedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    public void removeProtection(UUID uuid) {
        protectedPlayers.remove(uuid);
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                protectedPlayers.entrySet().removeIf(entry -> now > entry.getValue());
            }
        }.runTaskTimerAsynchronously(plugin, 200L, 200L); // Check every 10 seconds
    }
}