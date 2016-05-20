package com.system.commandsystem.executors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.system.ranksystem.Main;

public class Spawn implements CommandExecutor {

	public static String prefix = ChatColor.GREEN + "COMMAND>> ";
	public static String prefix1 = ChatColor.LIGHT_PURPLE + "SPAWN>> ";
	
	public static double x;
	public static double y;
	public static double z;
	//public static float yaw;
	//public static float pitch;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		// TODO Auto-generated method stub
		if(cmd.getName().equalsIgnoreCase("spawn")){
			if(sender instanceof Player){
				Player p = (Player) sender;
			
				String r = "ranksystem.spawnpoint.x" ;
				String r1 = "ranksystem.spawnpoint.y" ;
				String r2 = "ranksystem.spawnpoint.z" ;
				String r3 = "ranksystem.spawnpoint.yaw" ;
				String r4 = "ranksystem.spawnpoint.pitch" ;
				x = Main.instance.getConfig().getDouble(r);
				y = Main.instance.getConfig().getDouble(r1);
				z = Main.instance.getConfig().getDouble(r2);
			//	yaw = (float) Main.instance.getConfig().get(r3);
			//	pitch = (float) Main.instance.getConfig().get(r4);

				Location loc = new Location(p.getWorld(), x, y, z);
			//	loc.setYaw(yaw);
			//	loc.setPitch(pitch);
			//	p.getLocation().setYaw(yaw);
			//	p.getLocation().setPitch(pitch);
				p.teleport(loc);
				p.sendMessage(prefix1 + ChatColor.GRAY + "You have been teleported to the spawn!");
			}else{
				sender.sendMessage(prefix + ChatColor.RED + "Player command only!");

				return false;
			}
		}
		if(cmd.getName().equalsIgnoreCase("setspawn")){
			if(sender instanceof Player){
				Player p = (Player) sender;
				double x1 = p.getLocation().getX();
				String r = "ranksystem.spawnpoint.x" ;
				Main.instance.getConfig().set(r, x1);
				double y1 = p.getLocation().getY();
				String r1 = "ranksystem.spawnpoint.y" ;
				Main.instance.getConfig().set(r1, y1);
				double z1 = p.getLocation().getZ();
				String r2 = "ranksystem.spawnpoint.z" ;
				Main.instance.getConfig().set(r2, z1);
				float yaw1 = p.getLocation().getYaw();
				String r3 = "ranksystem.spawnpoint.yaw" ;
				Main.instance.getConfig().set(r3, yaw1);
				float pitch1 = p.getLocation().getPitch();
				String r4 = "ranksystem.spawnpoint.pitch" ;
				Main.instance.getConfig().set(r4, pitch1);
				Main.instance.saveConfig();
				Main.instance.reloadConfig();
				p.sendMessage(prefix1 + ChatColor.GRAY + "You succusfully setted the spawn!!");

			}else{
				sender.sendMessage(prefix + ChatColor.RED + "Player command only!");

				return false;
			}
		}
		return false;
	}

}

