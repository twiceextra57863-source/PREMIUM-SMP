package com.smpgenesis.managers;

import com.smpgenesis.SMPGenesis;
import com.smpgenesis.utils.Chat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KitManager {
    private final SMPGenesis plugin;
    private final List<ItemStack> starterKit = new ArrayList<>();

    public KitManager(SMPGenesis plugin) {
        this.plugin = plugin;
        loadKit();
    }

    private void loadKit() {
        if (!plugin.getConfig().getBoolean("starter-kit.enabled", true)) return;

        List<?> items = plugin.getConfig().getList("starter-kit.items");
        if (items == null) return;

        for (Object obj : items) {
            if (!(obj instanceof Map)) continue;
            Map<?, ?> itemMap = (Map<?, ?>) obj;
            
            String typeStr = (String) itemMap.get("type");
            if (typeStr == null) continue;
            
            Material mat = Material.matchMaterial(typeStr);
            if (mat == null) continue;

            int amount = itemMap.containsKey("amount") ? (Integer) itemMap.get("amount") : 1;
            ItemStack item = new ItemStack(mat, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                if (itemMap.containsKey("name")) {
                    meta.setDisplayName(Chat.color((String) itemMap.get("name")));
                }
                if (itemMap.containsKey("lore")) {
                    List<String> rawLore = (List<String>) itemMap.get("lore");
                    List<String> coloredLore = new ArrayList<>();
                    for (String l : rawLore) coloredLore.add(Chat.color(l));
                    meta.setLore(coloredLore);
                }
                item.setItemMeta(meta);
            }

            if (itemMap.containsKey("enchants")) {
                Map<String, Integer> enchants = (Map<String, Integer>) itemMap.get("enchants");
                for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                    Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.getKey().toLowerCase()));
                    if (ench != null) {
                        item.addUnsafeEnchantment(ench, entry.getValue());
                    }
                }
            }
            starterKit.add(item);
        }
    }

    public void giveStarterKit(Player player) {
        if (starterKit.isEmpty()) return;
        for (ItemStack item : starterKit) {
            player.getInventory().addItem(item.clone());
        }
        player.sendMessage(Chat.color("&aYou received the Starter Kit!"));
    }
}