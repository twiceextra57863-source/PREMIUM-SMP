package com.smpgenesis.managers;

import com.smpgenesis.SMPGenesis;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class BorderManager {
    private final SMPGenesis plugin;

    public BorderManager(SMPGenesis plugin) {
        this.plugin = plugin;
    }

    public void startShrink() {
        if (!plugin.getConfig().getBoolean("world-border.enabled", true)) return;
        
        String worldName = plugin.getConfig().getString("world-border.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World for border shrink not found!");
            return;
        }

        double startSize = plugin.getConfig().getDouble("world-border.start-size", 5000);
        double endSize = plugin.getConfig().getDouble("world-border.end-size", 1000);
        long shrinkTime = plugin.getConfig().getLong("world-border.shrink-time", 1800);

        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(startSize);
        border.setSize(endSize, shrinkTime);
        
        plugin.getLogger().info("World border shrinking initialized.");
    }
}