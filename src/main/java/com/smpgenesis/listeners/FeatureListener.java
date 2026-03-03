package com.smpgenesis.listeners;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.managers.SMPManager;
import com.smpgenesis.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FeatureListener implements Listener {
    private final SMPGenesis plugin;

    public FeatureListener(SMPGenesis plugin) {
        this.plugin = plugin;
    }

    // Extra Feature: Custom Dynamic MOTD
    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        if (!plugin.getConfig().getBoolean("extras.custom-motd.enabled", true)) return;
        
        SMPManager.SMPStatus status = plugin.getSmpManager().getStatus();
        String motd = plugin.getConfig().getString("extras.custom-motd." + status.name().toLowerCase(), "SMP Genesis");
        event.motd(net.kyori.adventure.text.Component.text(Chat.color(motd)));
    }

    // Extra Feature: Death Leaderboard (Tracker)
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("extras.death-leaderboard.enabled", true)) return;
        Player p = event.getEntity();
        int deaths = plugin.getDataManager().getConfig().getInt("deaths." + p.getUniqueId(), 0) + 1;
        plugin.getDataManager().getConfig().set("deaths." + p.getUniqueId(), deaths);
        plugin.getDataManager().save();
        
        plugin.getDiscordManager().sendWebhook("💀 **" + p.getName() + "** died. (Total Deaths: " + deaths + ")");
    }

    // Extra Feature: First Diamond Announcer & Timber & Spawner Silk Touch
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Block b = event.getBlock();
        Material type = b.getType();

        // 1. First Diamond Announcer
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            if (plugin.getConfig().getBoolean("extras.first-diamond-announcer.enabled", true)) {
                boolean hasFound = plugin.getDataManager().getConfig().getBoolean("found-diamonds." + p.getUniqueId(), false);
                if (!hasFound) {
                    plugin.getDataManager().getConfig().set("found-diamonds." + p.getUniqueId(), true);
                    plugin.getDataManager().save();
                    Bukkit.broadcastMessage(Chat.color("&b&lDIAMONDS! &f" + p.getName() + " &7has found their first diamonds!"));
                }
            }
        }

        // 2. Timber / Tree Feller
        if (plugin.getConfig().getBoolean("extras.timber.enabled", true)) {
            if (type.name().endsWith("_LOG") && p.getInventory().getItemInMainHand().getType().name().endsWith("_AXE")) {
                if (!p.isSneaking()) {
                    handleTimber(b, p);
                }
            }
        }

        // 3. Spawner Silk Touch
        if (plugin.getConfig().getBoolean("extras.spawner-silk-touch.enabled", true)) {
            if (type == Material.SPAWNER) {
                ItemStack tool = p.getInventory().getItemInMainHand();
                if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    CreatureSpawner spawner = (CreatureSpawner) b.getState();
                    ItemStack drop = new ItemStack(Material.SPAWNER);
                    BlockStateMeta meta = (BlockStateMeta) drop.getItemMeta();
                    meta.setBlockState(spawner);
                    drop.setItemMeta(meta);
                    b.getWorld().dropItemNaturally(b.getLocation(), drop);
                    event.setExpToDrop(0);
                }
            }
        }
    }

    private void handleTimber(Block startBlock, Player player) {
        int limit = plugin.getConfig().getInt("extras.timber.limit", 128);
        Set<Block> blocks = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(startBlock);
        Material logType = startBlock.getType();

        while (!queue.isEmpty() && blocks.size() < limit) {
            Block b = queue.poll();
            if (b.getType() == logType && !blocks.contains(b)) {
                blocks.add(b);
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            queue.add(b.getRelative(x, y, z));
                        }
                    }
                }
            }
        }
        
        // Prevent massive lags, limit to a reasonable amount
        if (blocks.size() > 1 && blocks.size() <= limit) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            for (Block b : blocks) {
                if (b.equals(startBlock)) continue; // main block handles its own event
                b.breakNaturally(tool);
            }
        }
    }
}