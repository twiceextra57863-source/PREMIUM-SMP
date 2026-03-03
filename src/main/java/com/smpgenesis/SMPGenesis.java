package com.smpgenesis;

import com.smpgenesis.commands.SMPCommand;
import com.smpgenesis.listeners.CombatListener;
import com.smpgenesis.listeners.FeatureListener;
import com.smpgenesis.listeners.PlayerListener;
import com.smpgenesis.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;

public class SMPGenesis extends JavaPlugin {

    private static SMPGenesis instance;
    private DataManager dataManager;
    private SMPManager smpManager;
    private GraceManager graceManager;
    private TimerManager timerManager;
    private BorderManager borderManager;
    private ProtectionManager protectionManager;
    private KitManager kitManager;
    private DiscordManager discordManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize Managers
        dataManager = new DataManager(this);
        timerManager = new TimerManager(this);
        borderManager = new BorderManager(this);
        graceManager = new GraceManager(this);
        protectionManager = new ProtectionManager(this);
        kitManager = new KitManager(this);
        discordManager = new DiscordManager(this);
        smpManager = new SMPManager(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new FeatureListener(this), this);

        // Register Commands
        getCommand("smp").setExecutor(new SMPCommand(this));

        // Extra Feature: Set Sleep Percentage natively
        if (getConfig().getBoolean("extras.sleep-percentage.enabled", true)) {
            int pct = getConfig().getInt("extras.sleep-percentage.percentage", 20);
            Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, pct));
        }

        // Extra Feature: Auto Broadcast
        startAutoBroadcast();

        getLogger().info("SMPGenesis v1.0.0 Enabled successfully! Loaded async-optimized core.");
    }

    @Override
    public void onDisable() {
        if (timerManager != null) {
            timerManager.saveState();
        }
        if (graceManager != null) {
            graceManager.saveState();
        }
        if (dataManager != null) {
            dataManager.save();
        }
        getLogger().info("SMPGenesis Disabled successfully! State saved.");
    }

    private void startAutoBroadcast() {
        if (!getConfig().getBoolean("extras.auto-broadcast.enabled", false)) return;
        int interval = getConfig().getInt("extras.auto-broadcast.interval", 600) * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            int index = 0;
            @Override
            public void run() {
                var messages = getConfig().getStringList("extras.auto-broadcast.messages");
                if (messages.isEmpty()) return;
                if (index >= messages.size()) index = 0;
                Bukkit.broadcastMessage(utils.Chat.color(messages.get(index)));
                index++;
            }
        }, interval, interval);
    }

    public static SMPGenesis getInstance() { return instance; }
    public DataManager getDataManager() { return dataManager; }
    public SMPManager getSmpManager() { return smpManager; }
    public GraceManager getGraceManager() { return graceManager; }
    public TimerManager getTimerManager() { return timerManager; }
    public BorderManager getBorderManager() { return borderManager; }
    public ProtectionManager getProtectionManager() { return protectionManager; }
    public KitManager getKitManager() { return kitManager; }
    public DiscordManager getDiscordManager() { return discordManager; }
}