package me.vicoquincis.discordintegration.command;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class DICommand implements CommandExecutor {
    DiscordIntegration plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have sufficient permissions to perform this command.");
            return true;
        }
        String subcmd = args[0];
        if (subcmd.equals("api")) {
            subcmd = args[1];
            switch(subcmd) {
                case "start" -> {
                    if (DiscordIntegration.api.isRunning()) {
                        sender.sendMessage("API server is already started!");
                    }
                    DiscordIntegration.api.start();
                }
                case "stop" -> {
                    if (!DiscordIntegration.api.isRunning()) {
                        sender.sendMessage("API server is already stopped!");
                    }
                    DiscordIntegration.api.stop();
                }
                default -> {
                    return false;
                }
            }
        } else if (subcmd.equals("socket")) {
            subcmd = args[1];
            switch(subcmd) {
                case "start" -> {
                    if (plugin.getSocketServer().isRunning()) {
                        sender.sendMessage(ChatColor.RED + "Socket server is already started!");
                    }
                    plugin.getSocketServer().startServer();
                    sender.sendMessage("§6Socket server started.");
                }
                case "stop" -> {
                    if (!plugin.getSocketServer().isRunning()) {
                        sender.sendMessage(ChatColor.RED + "Socket server is not running!");
                    }
                    plugin.getSocketServer().stopServer();
                    sender.sendMessage("§6Socket server stopped.");
                }
                default -> {
                    return false;
                }
            }
        } else if (subcmd.equals("status")) {
            boolean apiStatus = DiscordIntegration.api.isRunning();
            boolean socketStatus = plugin.getSocketServer().isRunning();
            boolean socketConnected = plugin.isSocketConnected();
            sender.sendMessage(binColor(apiStatus) + "[■]§r API Restlet server " + (apiStatus ? "§aonline" : "§coffline"));
            sender.sendMessage(binColor(socketStatus) + "[■]§r Socket.io server " + (socketStatus ? "§aonline" : "§coffline"));
            sender.sendMessage(binColor(socketConnected) + "    [◦]§r socket "
                    + (socketConnected ? ("connected " + plugin.getSocket().getInitialHeaders().get("remote_addr") + " (id " + plugin.getSocket().getId() + ")") : "not connected"));
        } else if (subcmd.equals("shutdown")) {
            if (args.length == 2) {
                if (args[1].equals("confirm")) {
                    sender.sendMessage(ChatColor.RED + "Shutting down discord integration. To re-enable, please reload or restart the server.");
                    Bukkit.getPluginManager().disablePlugin(plugin);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "This command will disable the Discord integration plugin. " +
                    "All of its features will be inaccessible until the next server reload/restart. " +
                    "To confirm this action, please run the command " + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "/mdi shutdown confirm");
        } else return false;
        return true;
    }

    private String binColor(boolean b) {
        return (b) ? "§a" : "§c";
    }

    public static TabCompleter completer = ((sender, command, label, args) -> {
        if (args.length < 2) {
            return List.of("api", "socket", "status", "shutdown");
        } else if (args[1].equals("api") || args[1].equals("socket")) {
            return List.of("start", "stop");
        }
        return null;
    });
}
