package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class PrivateMessage extends ServerResource {
    private static final Plugin plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);
    private static final Server server = plugin.getServer();
    public final static String MESSAGE_TEMPLATE = ChatColor.BLUE + "[%1$s"
            + ChatColor.BLUE + " -> %2$s" + ChatColor.BLUE + "] "
            + ChatColor.RESET + "%3$s";

    @Post("json:json")
    public void privateMessage(String data) {
        JSONObject body = new JSONObject(data);
        String author = body.getString("author");
        String content = body.getString("content");
        Player p = server.getPlayerExact(getAttribute("uname"));
        if (author == null | content == null) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return;
        }
        if (p == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        try {
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                p.sendMessage(String.format(MESSAGE_TEMPLATE,
                        ChatColor.DARK_PURPLE + author,
                        ChatColor.GRAY + "you", content));
                return content;
            });
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }
}
