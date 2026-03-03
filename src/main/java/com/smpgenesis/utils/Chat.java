package com.smpgenesis.utils;

import org.bukkit.ChatColor;

public class Chat {
    /**
     * Utility to quickly translate alternate color codes.
     * Modern Paper encourages Adventure MiniMessage, but ampersand formatting remains 
     * highly practical for simple config strings.
     */
    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}