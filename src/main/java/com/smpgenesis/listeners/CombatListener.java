package com.smpgenesis.listeners;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.utils.Chat;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class CombatListener implements Listener {
    private final SMPGenesis plugin;

    public CombatListener(SMPGenesis plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        boolean isAttackerPlayer = event.getDamager() instanceof Player;
        Player attacker = isAttackerPlayer ? (Player) event.getDamager() : null;

        // First Join Protection Logic
        if (plugin.getProtectionManager().isProtected(victim.getUniqueId())) {
            event.setCancelled(true);
            if (attacker != null) {
                attacker.sendMessage(Chat.color("&cThis player has first-join protection!"));
            }
            return;
        }

        // Attacker is protected and tries to hit someone
        if (attacker != null && plugin.getProtectionManager().isProtected(attacker.getUniqueId())) {
            // Remove their protection if they initiate combat
            plugin.getProtectionManager().removeProtection(attacker.getUniqueId());
            attacker.sendMessage(Chat.color("&cYour first-join protection has been removed because you initiated combat!"));
        }

        // Grace Period Logic
        if (plugin.getGraceManager().isGraceActive() && attacker != null) {
            event.setDamage(0);
            event.setCancelled(true); // Cancel to prevent armor degradation

            // Advanced Knockback System
            double horiz = plugin.getConfig().getDouble("knockback.horizontal", 1.2);
            double vert = plugin.getConfig().getDouble("knockback.vertical", 0.4);

            // Calculate velocity based on attacker's yaw per prompt
            Vector direction = attacker.getLocation().getDirection().setY(0).normalize();
            Vector velocity = direction.multiply(horiz).setY(vert);
            victim.setVelocity(velocity);

            // Particles & Sounds
            int particles = plugin.getConfig().getInt("particles.amount", 20);
            Particle particleType = Particle.ASH;
            try {
                particleType = Particle.valueOf(plugin.getConfig().getString("particles.type", "ASH"));
            } catch (IllegalArgumentException ignored) {}

            victim.getWorld().spawnParticle(particleType, victim.getLocation().add(0, 1, 0), particles, 0.5, 0.5, 0.5, 0);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.5f);
        }
    }
}