package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.List;

public class Whitelist extends ServerResource {
    private final Server server = DiscordIntegration.getPlugin(DiscordIntegration.class).getServer();

    // Get all whitelisted players. Returns a list (may be empty) or a 404 Not Found error if the whitelist is off.
    @Get("json")
    public Representation getWhitelist() {
        if (server.hasWhitelist()) {
            List<String> whitelist = server.getWhitelistedPlayers().stream().map(OfflinePlayer::getName).toList();
            return new JacksonRepresentation<>(whitelist);
        }
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return new JacksonRepresentation<>(null);
    }
}
