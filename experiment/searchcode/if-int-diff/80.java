package me.Xephi.ServerUptime;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ServerUptimeCommands implements CommandExecutor {
    private final ServerUptime plugin;

    public ServerUptimeCommands(ServerUptime plugin) {
        this.plugin = plugin;
    }
    private final long serverStart = System.currentTimeMillis();
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final String commandName = cmd.getName().toLowerCase();
        final long diff = System.currentTimeMillis() - serverStart;
        final String msg = " " + (int)(diff / 86400000) + " " + plugin.getConfig().get("days").toString() + " " + (int)(diff / 3600000 % 24) + " " + plugin.getConfig().get("hours").toString() +" " + (int)(diff / 60000 % 60) + " " + plugin.getConfig().get("minutes").toString() +" " + (int)(diff / 1000 % 60) + " " + plugin.getConfig().get("seconds").toString() + ".";
        if (commandName.equals("uptime")) {
            plugin.getLogger().info("[CONSOLE]" + msg);
            sender.sendMessage(ServerUptime.replaceWithChatColor("*" + plugin.getConfig().get("color").toString().toLowerCase() + "*[" + plugin.getConfig().get("prefix").toString() + "]" + msg));
        }
        return true;
    }
}
