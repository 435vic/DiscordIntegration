package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.List;

public class PlayerList extends ServerResource {
    private final Server server = DiscordIntegration.getPlugin(DiscordIntegration.class).getServer();

    // Fetch online players. Returns a list
    @Get("json")
    public Representation getPlayerList() {
        List<String> players = new ArrayList<>();
        for (Player p : server.getOnlinePlayers()) {
            if (!isVanished(p)) {
                players.add(p.getName());
            }
        }
        if (players.isEmpty()) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
        }
        return new JacksonRepresentation<>(players);
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
