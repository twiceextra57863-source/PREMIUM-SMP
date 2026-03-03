package com.smpgenesis.managers;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerManager {
    private final SMPGenesis plugin;
    private int remainingSeconds;
    private BossBar bossBar;
    private BukkitRunnable timerTask;

    public TimerManager(SMPGenesis plugin) {
        this.plugin = plugin;
        this.remainingSeconds = plugin.getDataManager().getConfig().getInt("timer.remaining", 0);
        
        if (remainingSeconds > 0 && plugin.getSmpManager().getStatus() != SMPManager.SMPStatus.WAITING) {
            startTimer(remainingSeconds);
        }
    }

    public void startTimer(int seconds) {
        this.remainingSeconds = seconds;
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(Chat.color("&b&lSMP TIME REMAINING"), BarColor.BLUE, BarStyle.SOLID);
        }
        bossBar.setVisible(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
        }

        if (timerTask != null) timerTask.cancel();

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    bossBar.setVisible(false);
                    Bukkit.broadcastMessage(Chat.color("&c&lThe SMP Phase has ended!"));
                    this.cancel();
                    return;
                }

                remainingSeconds--;
                String formattedTime = formatTime(remainingSeconds);
                bossBar.setTitle(Chat.color("&b&lSMP Time: &f" + formattedTime));
                
                // Send action bar every second
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(net.kyori.adventure.text.Component.text(Chat.color("&eTime Remaining: &f" + formattedTime)));
                }
            }
        };
        timerTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopTimer() {
        if (timerTask != null) timerTask.cancel();
        if (bossBar != null) bossBar.setVisible(false);
        remainingSeconds = 0;
        saveState();
    }

    public void updateBossBarPlayers(Player player) {
        if (bossBar != null && bossBar.isVisible()) {
            bossBar.addPlayer(player);
        }
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void saveState() {
        plugin.getDataManager().getConfig().set("timer.remaining", remainingSeconds);
        plugin.getDataManager().save();
    }

    public static String formatTime(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}