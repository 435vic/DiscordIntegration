package me.vicoquincis.discordintegration.api.resource;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.ServerResource;


public class PlayerResource extends ServerResource {
    private final Plugin plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);
    private final Server server = plugin.getServer();


    // Kick player. Returns the player's name if successful, otherwise a 404 error, if the player is online or does not exist.
    @Delete("json")
    public String kickPlayer(String data) {
        JSONObject body = new JSONObject(data);
        String uname = getAttribute("uname");
        String reason = body.getString("reason");
        plugin.getLogger().info("API Player kick, reason: " + reason);
        Player p = server.getPlayerExact(uname);
        if (p == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        String pname;
        try {
            // Since the API is on a separate thread we have to use the scheduler to do server operations synchronously
            pname = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                p.kickPlayer("§4§lYou have been kicked!\n§rReason: §l" + reason);
                return p.getName();
            }).get();
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
        return pname;
    }

    // Eventually, if needed, a GET request could provide player info
}
