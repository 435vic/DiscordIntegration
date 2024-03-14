package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.List;

public class BanList extends ServerResource {
    private final Server server = DiscordIntegration.getPlugin(DiscordIntegration.class).getServer();

    @Get("json")
    public Representation getBanList() {
        List<String> banlist = server.getBannedPlayers().stream().map(OfflinePlayer::getName).toList();
        return new JacksonRepresentation<>(banlist);
    }
}
