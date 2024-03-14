package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class ChatMessage extends ServerResource {
    private final Plugin plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);

    @Post("json:json")
    public void postChatMessage(String data) {
//        plugin.getLogger().info(data);
        JSONObject body = new JSONObject(data);
        String author = body.getString("author");
        String content = body.getString("content");
        if (author == null | content == null) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "<" + author + "> " + ChatColor.WHITE + content);
                return content;
            });
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }
}
