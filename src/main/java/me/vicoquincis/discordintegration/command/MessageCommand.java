package me.vicoquincis.discordintegration.command;

import io.socket.socketio.server.SocketIoSocket;
import me.vicoquincis.discordintegration.DiscordIntegration;
import me.vicoquincis.discordintegration.api.resource.PrivateMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageCommand implements CommandExecutor {
    DiscordIntegration plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) return false;
        String target = args[0];
        if (plugin.getServer().getPlayerExact(target) == null) { // Probably a discord user
            Pattern userTag = Pattern.compile("#\\d{4}");
            String author = (sender instanceof Player) ? sender.getName() : "Server";
            int index;
            for (index = 0; index < args.length; index++) {
                Matcher match = userTag.matcher(args[index]);
                if (match.find()) {
                    target = String.join(" ", Arrays.asList(args).subList(0, index+1));
                    break;
                }
            }
            if (!userTag.matcher(target).find()) return false;
            String message = String.join(" ", Arrays.asList(args).subList(index+1, args.length));
            String targetId = plugin.getUserProvider().getUserId(target);
            String targetTag = target;
            if (targetId != null) target = "id " + targetId;
            else target = "tag " + targetTag;
            String finalTarget = target;
            SocketIoSocket.ReceivedByRemoteAcknowledgementCallback callback = (data) -> {
                String response = (String) ((JSONObject) data[0]).get("status");
                switch (response) {
                    case "ok" -> {
                        JSONObject user = ((JSONObject) data[0]).getJSONObject("user");
                        // If we used a tag, that means the user is not on the database
                        if (targetId == null) {
                            plugin.getUserProvider().addUser(user.getString("id"), user.getString("tag"));
                        } else if (!targetTag.equals(user.getString("tag"))) {
                            // User is in database, but with different tags
                            // this indicates a name change, so we update the database to reflect that
                            plugin.getUserProvider().addUser(targetId, user.getString("tag"));
                            // notify player of name change
                            sender.sendMessage(ChatColor.GRAY +
                                    "discord user has new name: " + targetTag
                                    + " -> " + user.getString("tag"));
                        }
                        sender.sendMessage(String.format(PrivateMessage.MESSAGE_TEMPLATE, ChatColor.GRAY + "you",
                        ChatColor.DARK_PURPLE + user.getString("tag"), message));
                    }
                    case "user not found" -> {
                        sender.sendMessage(ChatColor.RED + "The message could not be sent because the user was not found. " +
                        "Check for misspellings or verify the user is a member of the Discord server.");
                    }
                    case "user unavailable" -> {
                        sender.sendMessage(ChatColor.RED + "The message could not be sent because the user is not a verified " +
                        "SMP member. Please message them via Discord or ask them to be verified by joining the SMP first.");
                    }
                    default -> {
                        plugin.getLogger().warning("Unkown callback while sending private message: " + data[0]);
                        sender.sendMessage(ChatColor.RED + "An unkown error ocurred while sending the message.");
                    }
                }
            };
            if (plugin.getSocket() != null) {
                plugin.getSocket().send("whisper", new String[]{author, target, message}, callback);
            } else {
                sender.sendMessage(ChatColor.RED + "Error sending message: connection to Discord lost. Try again later.");
            }

            return true;
        } else if (plugin.getServer().getPlayerExact(target).isOnline()) {
            String message = String.join(" ", Arrays.asList(args).subList(1, args.length));
            sender.sendMessage(String.format(PrivateMessage.MESSAGE_TEMPLATE, ChatColor.GRAY + "you",
                    ChatColor.GRAY + target, message));
            message = String.format(PrivateMessage.MESSAGE_TEMPLATE, ChatColor.GRAY + sender.getName(),
                    ChatColor.GRAY + "you", message);
            plugin.getServer().getPlayerExact(target).sendMessage(message);
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Could not send message. Player " + target + "is not online.");
        return false;
    }
}