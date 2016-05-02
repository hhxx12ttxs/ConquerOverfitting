package me.samkio.levelcraft.Functions;

import java.util.HashMap;

import me.samkio.levelcraft.Levelcraft;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PlayerFunctions {
	public  Levelcraft plugin;
	private final  HashMap<CommandSender, Boolean> NotifyUsers = new HashMap<CommandSender, Boolean>();
	public PlayerFunctions(Levelcraft instance) {
		plugin = instance;
	}

	public void doThis(CommandSender sender, String[] split, Levelcraft plugin) {
		if (sender instanceof Player){
			Player player  = (Player) sender;
			if ((split[0].equalsIgnoreCase("wc")
					|| split[0].equalsIgnoreCase("wood")
					|| split[0].equalsIgnoreCase("woodcut") || split[0]
					.equalsIgnoreCase("w")) && plugin.Settings.enableWCLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "w")) {
				showStat(sender, "w");
			} else if ((split[0].equalsIgnoreCase("mine")
					|| split[0].equalsIgnoreCase("m") || split[0]
                    .equalsIgnoreCase("mining"))
                    && plugin.Settings.enableMineLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "m")) {
				showStat(sender, "m");
			} else if ((split[0].equalsIgnoreCase("slay")
					|| split[0].equalsIgnoreCase("s") || split[0]
					.equalsIgnoreCase("slayer"))
					&& plugin.Settings.enableSlayerLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "s")) {
				showStat(sender, "s");
			} else if ((split[0].equalsIgnoreCase("range")
					|| split[0].equalsIgnoreCase("r") || split[0].equalsIgnoreCase("ranging") )
					&& plugin.Settings.enableRangeLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "r")) {
				showStat(sender, "r"); 
			} else if ((split[0].equalsIgnoreCase("fist")
					|| split[0].equalsIgnoreCase("c") || split[0].equalsIgnoreCase("fisticuffs"))
				    && plugin.Settings.enableFisticuffsLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "c")) {
				showStat(sender, "c"); 
			} else if ((split[0].equalsIgnoreCase("archer")
					|| split[0].equalsIgnoreCase("a") || split[0].equalsIgnoreCase("archery"))
					&& plugin.Settings.enableArcherLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "a")) {
				showStat(sender, "a");
			}else if ((split[0].equalsIgnoreCase("dig")
					|| split[0].equalsIgnoreCase("d") || split[0].equalsIgnoreCase("digger"))
					&& plugin.Settings.enableDigLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "d")) {
				showStat(sender, "d");
			}else if ((split[0].equalsIgnoreCase("forge")
					|| split[0].equalsIgnoreCase("f") || split[0].equalsIgnoreCase("forger"))
					&& plugin.Settings.enableForgeLevel == true && !plugin.Whitelist.isAvoid((Player) sender, "f")) {
				showStat(sender, "f");
			} else if (split[0].equalsIgnoreCase("list")) {
				plugin.Help.ListLevels(sender);
			}else if (split[0].equalsIgnoreCase("total")) {
				plugin.Help.Total(sender);
			} else if (split[0].equalsIgnoreCase("admin")
					&& plugin.Whitelist.isAdmin(player) == true
					&& split.length >= 3) {
				plugin.Admin.dothis(sender, split);
			} else if (split[0].equalsIgnoreCase("notify")) {
				toggleNotify(sender);
			} else if (split[0].equalsIgnoreCase("shout") && split.length >= 2) {
				plugin.Help.shout(sender, split[1], plugin);
			} else if ((split[0].equalsIgnoreCase("all"))) {
				int level = 0;
				int level2 = 0;
				int level3 = 0;
				int level4 = 0;
				int level5 = 0;
				int level6 = 0;
				int level7 = 0;
				int level8 = 0;
				double mineexp = 0;
				double slayexp = 0;
				double wcexp = 0;
				double rangexp = 0;
				double fisticuffsexp = 0;
				double archeryexp = 0;
				double digexp = 0;
				double forgeexp = 0;
				level = plugin.Level.getLevel(sender, "m");
				level2 = plugin.Level.getLevel(sender, "s");
				level3 = plugin.Level.getLevel(sender, "w");
				level4 = plugin.Level.getLevel(sender, "r");
				level5 = plugin.Level.getLevel(sender, "c");
				level6 = plugin.Level.getLevel(sender, "a");
				level7 = plugin.Level.getLevel(sender, "d");
				level8 = plugin.Level.getLevel(sender, "f");
				mineexp = plugin.Level.getExp(sender, "m");
				slayexp = plugin.Level.getExp(sender, "s");
				wcexp = plugin.Level.getExp(sender, "w");
				rangexp = plugin.Level.getExp(sender, "r");
				fisticuffsexp = plugin.Level.getExp(sender, "c");
				archeryexp = plugin.Level.getExp(sender, "a");
				digexp = plugin.Level.getExp(sender, "d");
				forgeexp = plugin.Level.getExp(sender, "f");
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1)
						+ "[LC] ---LevelCraftPlugin By Samkio--- ");
				if(plugin.Settings.enableMineLevel && !plugin.Whitelist.isAvoid((Player) sender, "m"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (Mining): " + level + ". Exp:" + mineexp);
				if(plugin.Settings.enableSlayerLevel && !plugin.Whitelist.isAvoid((Player) sender, "s"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (Slayer): " + level2 + ", Exp:" + slayexp);
				if(plugin.Settings.enableWCLevel && !plugin.Whitelist.isAvoid((Player) sender, "w"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (WoodCutting): " + level3 + ". Exp:" + wcexp);
				if(plugin.Settings.enableRangeLevel && !plugin.Whitelist.isAvoid((Player) sender, "r"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (Range): " + level4 + ". Exp:" + rangexp);
				if(plugin.Settings.enableFisticuffsLevel && !plugin.Whitelist.isAvoid((Player) sender, "c"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (Fitisicuffs): " + level5 + ". Exp:" + fisticuffsexp);
				if(plugin.Settings.enableArcherLevel && !plugin.Whitelist.isAvoid((Player) sender, "a"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (Archery): " + level6 + ". Exp:" + archeryexp);
				if(plugin.Settings.enableDigLevel && !plugin.Whitelist.isAvoid((Player) sender, "d"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (Digging): " + level7 + ". Exp:" + digexp);
				if(plugin.Settings.enableForgeLevel && !plugin.Whitelist.isAvoid((Player) sender, "f"))
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " (Forge): " + level8 + ". Exp:" + forgeexp);
			} else if (split[0].equalsIgnoreCase("unlocks")) {
				if (split.length >= 2) {
					plugin.Help.unlocks(sender, split);
				} else {
					plugin.Help.IncorrectExp(sender);
				}
			} else {
				sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c2)
						+ " Stat not found type '/level list' to list all stats. ");
			}
		}	
	}

	public void checkAccount(CommandSender sender) {
		if (plugin.Settings.database.equalsIgnoreCase("flatfile")) {
			boolean HasWCAccount = plugin.LevelFunctions.containskey(sender,
					plugin.WCExpFile);
			boolean HasMineAcc = plugin.LevelFunctions.containskey(sender,
					plugin.MiExpFile);
			boolean HasSlayAcc = plugin.LevelFunctions.containskey(sender,
					plugin.SlayExpFile);
			boolean HasRangeAcc = plugin.LevelFunctions.containskey(sender,
					plugin.RangeExpFile);
			boolean HasFisticuffsAcc = plugin.LevelFunctions.containskey(sender,
					plugin.FisticuffsExpFile);
			boolean HasArcherAcc = plugin.LevelFunctions.containskey(sender,
					plugin.ArcherExpFile);
			boolean HasDigAcc = plugin.LevelFunctions.containskey(sender,
					plugin.DiggingExpFile);
			boolean HasForgeAcc = plugin.LevelFunctions.containskey(sender,
					plugin.ForgeExpFile);
			if (HasWCAccount == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.WCExpFile);
			}
			if (HasDigAcc == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.DiggingExpFile);
			}
			if (HasMineAcc == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.MiExpFile);
			}

			if (HasSlayAcc == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.SlayExpFile);
			}
			if (HasRangeAcc == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.RangeExpFile);
			}
			if (HasFisticuffsAcc == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.FisticuffsExpFile);
			}
			if (HasArcherAcc == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.ArcherExpFile);
			}
			if (HasForgeAcc == false) {
				plugin.LevelFunctions.write(sender, 0, plugin.ForgeExpFile);
			}
		} else if (plugin.Settings.database.equalsIgnoreCase("mysql") && plugin.DataMySql.PlayerExsists(sender) == false) {
			plugin.DataMySql.NewPlayer(sender, 0);
		} else if (plugin.Settings.database.equalsIgnoreCase("sqlite")
				&& plugin.DataSqlite.PlayerExsists(sender) == false) {
			plugin.DataSqlite.NewPlayer(sender, 0);
		}
	}

	public boolean enabled(CommandSender sender) {
		return NotifyUsers.containsKey(sender);
	}

	public void toggleNotify(CommandSender sender) {
		if (enabled(sender)) {
			NotifyUsers.remove(sender);
			plugin.Toolbox.sendMessage(sender, "Experience notify disabled.", true);
		} else {
			NotifyUsers.put(sender, null);
			sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC]" + ChatColor.valueOf(plugin.Settings.c3)
						+ " Experience notify enabled.");
			}
	}

	public void showStat(CommandSender sender, String string) {
		if (sender instanceof Player) {
			int level = 0;
			double stat = 0;
			double expLeft = 0;
			String str = "NULL";
			if (string.equalsIgnoreCase("W") && plugin.Settings.enableWCLevel && !plugin.Whitelist.isAvoid((Player) sender, "w")) {
				level = plugin.Level.getLevel(sender, "w");
				stat = plugin.Level.getExp(sender, "w");
				expLeft = plugin.Level.getExpLeft(sender, "w");
				str = "Woodcut";
			} else if (string.equalsIgnoreCase("M") && plugin.Settings.enableMineLevel && !plugin.Whitelist.isAvoid((Player) sender, "m")) {
				level = plugin.Level.getLevel(sender, "m");
				stat = plugin.Level.getExp(sender, "m");
				expLeft = plugin.Level.getExpLeft(sender, "m");
				str = "Mining";
			} else if (string.equalsIgnoreCase("S") && plugin.Settings.enableSlayerLevel && !plugin.Whitelist.isAvoid((Player) sender, "s")) {
				level = plugin.Level.getLevel(sender, "s");
				stat = plugin.Level.getExp(sender, "s");
				expLeft = plugin.Level.getExpLeft(sender, "s");
				str = "Slaying";
			} else if (string.equalsIgnoreCase("R") && plugin.Settings.enableRangeLevel && !plugin.Whitelist.isAvoid((Player) sender, "r")) {
				level = plugin.Level.getLevel(sender, "r");
				stat = plugin.Level.getExp(sender, "r");
				expLeft = plugin.Level.getExpLeft(sender, "r");
				str = "Ranging";
			} else if (string.equalsIgnoreCase("C") && plugin.Settings.enableFisticuffsLevel && !plugin.Whitelist.isAvoid((Player) sender, "c")) {
				level = plugin.Level.getLevel(sender, "c");
				stat = plugin.Level.getExp(sender, "c");
				expLeft = plugin.Level.getExpLeft(sender, "c");
				str = "Fisticuffs";
			} else if (string.equalsIgnoreCase("A") && plugin.Settings.enableArcherLevel && !plugin.Whitelist.isAvoid((Player) sender, "a")) {
				level = plugin.Level.getLevel(sender, "a");
				stat = plugin.Level.getExp(sender, "a");
				expLeft = plugin.Level.getExpLeft(sender, "a");
				str = "Archery";
			}else if (string.equalsIgnoreCase("D") && plugin.Settings.enableDigLevel && !plugin.Whitelist.isAvoid((Player) sender, "d")) {
				level = plugin.Level.getLevel(sender, "d");
				stat = plugin.Level.getExp(sender, "d");
				expLeft = plugin.Level.getExpLeft(sender, "d");
				str = "Digging";
			}else if (string.equalsIgnoreCase("f") && plugin.Settings.enableForgeLevel && !plugin.Whitelist.isAvoid((Player) sender, "f")) {
				level = plugin.Level.getLevel(sender, "f");
				stat = plugin.Level.getExp(sender, "f");
				expLeft = plugin.Level.getExpLeft(sender, "f");
				str = "Forge";
			}

			sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1)
					+ "[LC] ---LevelCraftPlugin By Samkio--- ");
			sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC] " + ChatColor.valueOf(plugin.Settings.c3) + str
					+ " experience: " + stat);
			sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC] " + ChatColor.valueOf(plugin.Settings.c3) + str
					+ " level: " + level);
			sender.sendMessage(ChatColor.valueOf(plugin.Settings.c1) + "[LC] " + ChatColor.valueOf(plugin.Settings.c3)
					+ "Experience left to next level: " + expLeft);
		} else {
			sender.sendMessage("Error: Invalid player!");
		}
	}
}

