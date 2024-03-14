package me.vicoquincis.discordintegration.command;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageCommandCompleter implements TabCompleter {
    DiscordIntegration plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                list.add(p.getName());
            }
            for (String dUser : plugin.getUserProvider().getList().values())
                if (dUser.toLowerCase().contains(args[0].toLowerCase()) || args[0].length() == 0)
                    list.add(dUser);
        }
        return list;
    }
}
