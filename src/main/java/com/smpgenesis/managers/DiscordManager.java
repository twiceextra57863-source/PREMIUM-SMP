package com.smpgenesis.managers;

import com.smpgenesis.SMPGenesis;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class DiscordManager {
    private final SMPGenesis plugin;
    private final String webhookUrl;
    private final boolean enabled;

    public DiscordManager(SMPGenesis plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("extras.discord-webhook.enabled", false);
        this.webhookUrl = plugin.getConfig().getString("extras.discord-webhook.url", "");
    }

    public void sendWebhook(String message) {
        if (!enabled || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL_HERE")) return;

        CompletableFuture.runAsync(() -> {
            try {
                String payload = "{\"content\": \"" + message.replace("\"", "\\\"") + "\"}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();
                
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }
}