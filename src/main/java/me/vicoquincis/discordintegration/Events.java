package me.vicoquincis.discordintegration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Events implements Listener {

    DiscordIntegration plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);

    @EventHandler
    void onChat(AsyncPlayerChatEvent event) {
        plugin.socketSend("chat", event.getPlayer().getDisplayName(), event.getMessage());
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        plugin.socketSend("player join", event.getPlayer().getDisplayName());
    }

    @EventHandler
    void onPlayerLeave(PlayerQuitEvent event) {
        plugin.socketSend("player quit", event.getPlayer().getDisplayName());
    }

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event) {
        String message;
        if (event.getDeathMessage().equals("death.fell.accident.water")) { // fun shenanigans :)
            String[] messages = {
                "%1$s did %2$s",
                "%1$s tried and failed to do %2$s",
                "%1$s was too weak for %2$s",
                "%1$s's body could not withstand %2$s",
                "%1$s tripped while doing %2$s",
                "%1$s did %2$s too hard",
                "%1$s's mind was lost to %2$s",
                "%1$s tried to do" + ChatColor.ITALIC  + "The Devil's Tango" + ChatColor.RESET + " while doing %2$s. It was not a good idea",
                "%1$s could not live in a world without %2$s",
                "%1$s attempted %2$s without the proper safety protocols (please contact OSHA)",
                "%1$s tried to recreate " + ChatColor.ITALIC + ChatColor.BOLD + ChatColor.DARK_GRAY + "\"The Original Vico\"™" + "(and failed miserably)",
                "%1$s did not perform %2$s with enough " + "§cg§6l§ea§am§bo§9u§5r§r" + ", and was punished by the gods"
            };
            message = messages[new Random().nextInt(messages.length)];
            message = String.format(message, event.getEntity().getDisplayName(), "§b§l\"The Vico\"§r");
            event.setDeathMessage(message);
            message = filterFormatters(message);
        } else {
            message = event.getDeathMessage();
        }
        plugin.socketSend("player death", event.getEntity().getDisplayName(), message);
    }

    @EventHandler
    void onAchievementGet(PlayerAdvancementDoneEvent event) throws UnsupportedEncodingException {
        if (event.getAdvancement().getKey().getKey().contains("recipes")) return;
        JsonObject advancements = JsonParser.parseReader(
            new InputStreamReader(plugin.getResource("advancements.json"), StandardCharsets.UTF_8)
        ).getAsJsonObject();
        String key = event.getAdvancement().getKey().getKey();
        JsonObject advancement = advancements.get(key).getAsJsonObject();
        plugin.socketSend("achievement",
                event.getPlayer().getDisplayName(),
                advancement.get("name").getAsString(),
                advancement.get("description").getAsString(),
                advancement.get("type").getAsString());
    }

    private String filterFormatters(String in) {
        return in.replaceAll("§.", "");
    }
}
