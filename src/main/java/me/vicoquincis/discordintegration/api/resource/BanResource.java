package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import me.vicoquincis.discordintegration.util.UUIDFetcher;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class BanResource extends ServerResource {
    private final Plugin plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);
    private final Server server = plugin.getServer();

    @Put("json:json")
    public String banPlayer(String data) {
        JSONObject body = new JSONObject(data);
        String uname = getAttribute("uname");
        String reason = body.getString("reason");
        String author = body.getString("author");
//        Date expiration = (body.getString("expiration") == null) ? null : Date.from(Instant.ofEpochSecond(Long.parseLong(body.getString("expiration"))));
        Date expiration = null;
        OfflinePlayer p = server.getOfflinePlayer(uname);
        UUID uuid = UUIDFetcher.getUUID(uname);
        if (uuid == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        if (p.isBanned()) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
        }
        plugin.getLogger().info("API Player ban, reason: " + reason);
        String pname;
        try {
            pname = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(), reason, expiration, (author != null) ? author : "Console");
                if (p.isOnline()) {
                    Player onlinePlayer = server.getPlayerExact(uname);
                    onlinePlayer.kickPlayer("§lThe Ban Hammer Hath Spoken. §4§lThy Have Been Banned!\n§rBanned by §l" + author + ": §4§l" + reason);
                }
                return p.getName();
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
        return pname;
    }

    @Delete("json:json")
    public String unbanPlayer() {
        OfflinePlayer p = Bukkit.getOfflinePlayer(getAttribute("uname"));
        if (!p.isBanned()) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
        }
        String pname;
        try {
            pname = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                Bukkit.getBanList(BanList.Type.NAME).pardon(p.getName());
                return p.getName();
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
        return pname;
    }
}
