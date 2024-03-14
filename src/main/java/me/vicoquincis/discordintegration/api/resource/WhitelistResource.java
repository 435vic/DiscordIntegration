package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import me.vicoquincis.discordintegration.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import java.util.UUID;

public class WhitelistResource extends ServerResource {
    private final Plugin plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);
    private final Server server = plugin.getServer();

    // Get whitelist status of player, or 404 error if player does not exist.
    @Get("json")
    public String getPlayerStatus() {
        Player p = Bukkit.getPlayerExact(getAttribute("uname"));
        if (p == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return "{\"player\": \"" + p.getName() + "\", \"whitelisted\": \"" + p.isWhitelisted() + "\"}";
    }

    @Put("json")
    public String whitelistPlayer() {
        // If whitelist is not enabled, send error 405 not allowed
        if (!server.hasWhitelist()) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return null;
        }
        OfflinePlayer p = Bukkit.getOfflinePlayer(getAttribute("uname"));
        // Check with mojang servers if player is real
        UUID uuid = UUIDFetcher.getUUID(getAttribute("uname"));
        String pname = UUIDFetcher.getName(uuid);
        if (uuid == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        if (p.isWhitelisted()) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
        }
        String out;
        try {
            out = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
               p.setWhitelisted(true);
               return p.getName();
            }).get();
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
        return out;
    }

    @Delete
    public String removePlayerFromWhitelist() {
        // If whitelist is not enabled, send error 405 not allowed
        if (!server.hasWhitelist()) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return null;
        }
        OfflinePlayer p = Bukkit.getOfflinePlayer(getAttribute("uname"));
        UUID uuid = UUIDFetcher.getUUID(getAttribute("uname"));
        if (uuid == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        if (!p.isWhitelisted()) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
        }
        String pname;
        try {
            pname = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                p.setWhitelisted(false);
                return UUIDFetcher.getName(uuid);
            }).get();
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
        return pname;
    }
}
