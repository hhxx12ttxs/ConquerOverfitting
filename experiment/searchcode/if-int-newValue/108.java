
package me.sniperzciinema.infected;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.sniperzciinema.infected.Disguise.Disguises;
import me.sniperzciinema.infected.Enums.DeathType;
import me.sniperzciinema.infected.Events.InfectedCommandEvent;
import me.sniperzciinema.infected.Events.InfectedJoinEvent;
import me.sniperzciinema.infected.Extras.Menus;
import me.sniperzciinema.infected.GameMechanics.Deaths;
import me.sniperzciinema.infected.GameMechanics.Equip;
import me.sniperzciinema.infected.GameMechanics.KDRatio;
import me.sniperzciinema.infected.GameMechanics.Settings;
import me.sniperzciinema.infected.GameMechanics.Sort;
import me.sniperzciinema.infected.GameMechanics.Stats;
import me.sniperzciinema.infected.GameMechanics.Stats.StatType;
import me.sniperzciinema.infected.Handlers.Lobby;
import me.sniperzciinema.infected.Handlers.Lobby.GameState;
import me.sniperzciinema.infected.Handlers.Arena.Arena;
import me.sniperzciinema.infected.Handlers.Classes.InfClassManager;
import me.sniperzciinema.infected.Handlers.Grenades.Grenade;
import me.sniperzciinema.infected.Handlers.Grenades.GrenadeManager;
import me.sniperzciinema.infected.Handlers.Items.ItemHandler;
import me.sniperzciinema.infected.Handlers.Location.LocationHandler;
import me.sniperzciinema.infected.Handlers.Player.InfPlayer;
import me.sniperzciinema.infected.Handlers.Player.InfPlayerManager;
import me.sniperzciinema.infected.Handlers.Player.Team;
import me.sniperzciinema.infected.Handlers.Potions.PotionHandler;
import me.sniperzciinema.infected.Messages.Msgs;
import me.sniperzciinema.infected.Messages.RandomChatColor;
import me.sniperzciinema.infected.Messages.StringUtil;
import me.sniperzciinema.infected.Messages.Time;
import me.sniperzciinema.infected.Tools.AddonManager;
import me.sniperzciinema.infected.Tools.Files;
import me.sniperzciinema.infected.Tools.FancyMessages.FancyMessage;
import me.sniperzciinema.infected.Tools.MySQL.MySQL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;


public class Commands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("Infected"))
		{
			Player p = null;
			InfPlayer ip = null;
			if (sender instanceof Player)
			{
				p = (Player) sender;
				ip = InfPlayerManager.getInfPlayer(p);
			}
			InfectedCommandEvent ce = new InfectedCommandEvent(args, p, ip);
			Bukkit.getPluginManager().callEvent(ce);
			if (!ce.isCancelled())
			{
				if (args.length >= 1 && args[0].equalsIgnoreCase("Chat"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Chat"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (!Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_Not_In.getString());

					else if (args.length == 1)
					{
						if (!ip.isInfChatting())
						{
							ip.setInfChatting(true);
							p.sendMessage(Msgs.Command_InfChat.getString("<state>", "in to"));
						} else
						{
							ip.setInfChatting(false);
							p.sendMessage(Msgs.Command_InfChat.getString("<state>", "out of"));
						}
					} else
					{
						StringBuilder message = new StringBuilder(args[1]);
						for (int arg = 2; arg < args.length; arg++)
							message.append(" ").append(args[arg]);

						for (Player u : Bukkit.getOnlinePlayers())
							if (ip.getTeam() == InfPlayerManager.getInfPlayer(p).getTeam() || u.hasPermission("Infected.Chat.Spy"))
								u.sendMessage(Msgs.Format_InfChat.getString("<team>", ip.getTeam().toString(), "<player>", p.getName(), "<score>", String.valueOf(ip.getScore()), "<message>", message.toString()));
					}
				}

				// //////////////////////////////////////////////-CLASSES-///////////////////////////////////////
				else if (args.length >= 1 && args[0].equalsIgnoreCase("Classes"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Classes"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (!Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_Not_In.getString());

					else if (Lobby.getGameState() == GameState.Infecting || Lobby.getGameState() == GameState.Started)
						p.sendMessage(Msgs.Error_Game_Started.getString());

					else
						Infected.Menus.teamMenu.open(p);
				}

				// ///////////////////////////////////////////-JOIN-//////////////////////////////////////////

				else if (args.length > 0 && args[0].equalsIgnoreCase("Join"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Join"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (Lobby.getGameState() == GameState.Disabled)
						p.sendMessage(Msgs.Error_Misc_Plugin_Disabled.getString());

					else if (Lobby.getLocation() == null)
						p.sendMessage(Msgs.Error_Lobby_Doesnt_Exist.getString());

					else if (Lobby.getValidArenas().isEmpty())
						p.sendMessage(Msgs.Error_Arena_No_Valid.getString());

					else if (Settings.isJoiningDuringGamePrevented() && (Lobby.getGameState() == GameState.Started || Lobby.getGameState() == GameState.Infecting || Lobby.getGameState() == GameState.GameOver))
						p.sendMessage(Msgs.Error_Misc_Joining_While_Game_Started.getString());

					else if (Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_In.getString());

					else
					{
						InfectedJoinEvent je = new InfectedJoinEvent(p);
						Bukkit.getPluginManager().callEvent(je);

						for (Player player : Lobby.getPlayersInGame())
							player.sendMessage(Msgs.Game_Joined_They.getString("<player>", p.getName()));

						ip.setInfo();
						Lobby.addPlayerInGame(p);

						// If the game isn't started and isn't infecting then
						// the players are all still in the lobby
						if (Lobby.getGameState() != GameState.Started && Lobby.getGameState() != GameState.Infecting)
							ip.tpToLobby();

						p.sendMessage(Msgs.Game_Joined_You.getString());

						// If the game hasn't started and there's enough players
						// for an autostart, start the timer
						if (Lobby.getGameState() == GameState.InLobby && Lobby.getPlayersInGame().size() >= Settings.getRequiredPlayers())
						{
							Bukkit.getScheduler().scheduleSyncDelayedTask(Infected.me, new Runnable()
							{

								@Override
								public void run() {

									Lobby.timerStartVote();
								}
							}, 100L);
						}
						// If voting has started, tell the new player how to
						// vote
						else if (Lobby.getGameState() == GameState.Voting)
							p.sendMessage(Msgs.Help_Vote.getString());

						// If it's already looking for the first infected,
						// respawn them as a human and equip them
						else if (Lobby.getGameState() == GameState.Infecting)
						{
							ip.setTimeIn(System.currentTimeMillis() / 1000);
							ip.respawn();
							Equip.equip(p);
						}
						// If the game has started already make the player a
						// zombie without calling any deaths(Event and stats)
						else if (Lobby.getGameState() == GameState.Started)
						{
							ip.setTimeIn(System.currentTimeMillis() / 1000);
							Deaths.playerDiesWithoutDeathStat(p);
						}
					}
				}

				// //////////////////////////////////////-INFO-/////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Info"))
				{
					if (!p.hasPermission("Infected.Info"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (Lobby.getGameState() == GameState.Disabled)
						p.sendMessage(Msgs.Error_Misc_Plugin_Disabled.getString());

					else
					{
						sender.sendMessage("");
						sender.sendMessage(Msgs.Format_Header.getString("<title>", "Status"));
						sender.sendMessage(Msgs.Command_Info_Players.getString("<players>", String.valueOf(Lobby.getPlayersInGame().size())));
						sender.sendMessage(Msgs.Command_Info_State.getString("<state>", Lobby.getGameState().toString()));
						sender.sendMessage(Msgs.Command_Info_Time_Left.getString("<time>", Time.getTime((long) Lobby.getTimeLeft())));
					}
				}

				// /////////////////////////////////////////////////////////////
				// SUICIDE
				else if (args.length > 0 && args[0].equalsIgnoreCase("Suicide"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Suicide"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (!Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_Not_In.getString());

					else if (Lobby.getGameState() != GameState.Started)
						p.sendMessage(Msgs.Error_Game_Not_Started.getString());

					else
					{
						if (ip.getTeam() == Team.Human)
							ip.Infect();

						Deaths.playerDies(DeathType.Other, null, p);
						ip.respawn();
					}
				}
				// ////////////////////////////////////////////////////-SHOP-STORE-/////////////////////////////
				else if (args.length > 0 && (args[0].equalsIgnoreCase("Shop") || args[0].equalsIgnoreCase("Store")))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Shop"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (!Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_Not_In.getString());

					else
						Infected.Menus.shopMenu.open(p);
				}
				// /////////////////////////////////////////////////-GRENADES-///////////////////////////////////////////////
				else if (args.length > 0 && (args[0].equalsIgnoreCase("Grenades") || args[0].equalsIgnoreCase("Grenade")))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Grenades"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (!Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_Not_In.getString());

					else
					{
						if (args.length == 2)
						{
							if (args[1].matches("[0-9]+"))
							{
								int gi = Integer.parseInt(args[1]) - 1;
								if (GrenadeManager.getGrenades().get(gi) != null)
								{
									if (ip.getPoints(Settings.VaultEnabled()) >= GrenadeManager.getGrenades().get(gi).getCost())
									{
										Grenade grenade = GrenadeManager.getGrenades().get(gi);
										p.getInventory().addItem(grenade.getItem());
										ip.setPoints(ip.getPoints(Settings.VaultEnabled()) - grenade.getCost(), Settings.VaultEnabled());
										p.sendMessage(Msgs.Grenades_Bought.getString("<grenade>", grenade.getName()));

									} else
										p.sendMessage(Msgs.Grenades_Cost_Not_Enough.getString());
								} else
									p.sendMessage(Msgs.Grenades_Invalid_Id.getString());
							} else
								p.sendMessage(Msgs.Grenades_Invalid_Id.getString());
						} else if (args.length == 3)
						{
							int amount = Integer.parseInt(args[2]);

							if (args[1].matches("[0-9]+"))
							{
								int gi = Integer.parseInt(args[1]) - 1;
								if (GrenadeManager.getGrenades().get(gi) != null)
								{
									if (ip.getPoints(Settings.VaultEnabled()) >= GrenadeManager.getGrenades().get(gi).getCost() * amount)
									{
										Grenade grenade = GrenadeManager.getGrenades().get(gi);
										ItemStack g = grenade.getItem();
										g.setAmount(amount);
										p.getInventory().addItem(g);
										ip.setPoints(ip.getPoints(Settings.VaultEnabled()) - (grenade.getCost() * amount), Settings.VaultEnabled());
										p.sendMessage(Msgs.Grenades_Bought.getString("<grenade>", grenade.getName()));
									} else
										p.sendMessage(Msgs.Grenades_Cost_Not_Enough.getString());
								} else
									p.sendMessage(Msgs.Grenades_Invalid_Id.getString());
							} else
								p.sendMessage(Msgs.Grenades_Invalid_Id.getString());
						} else
							Infected.Menus.grenadeMenu.open(p);
					}
				}

				// /////////////////////////////////////////////////////-SETLOBBY-/////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("SetLobby"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.SetLobby"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						Lobby.setLocation(p.getLocation());
						p.sendMessage(Msgs.Command_Lobby_Set.getString());
					}
				}
				// /////////////////////////////////////////////////////-SETLEAVE-/////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("SetLeave"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.SetLeave"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						Lobby.setLeave(p.getLocation());
						p.sendMessage(Msgs.Command_Leave_Location_Set.getString());
					}
				}

				// ////////////////////////////////////////////////////-LIST-/////////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("List"))
				{

					if (!p.hasPermission("Infected.List"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					if (args.length != 1)
					{
						if (args[1].equalsIgnoreCase("Playing"))
						{
							p.sendMessage(Msgs.Format_Header.getString("<title>", "Playing"));
							for (Player u : Lobby.getPlayersInGame())
								p.sendMessage(Msgs.Format_List.getString("<player>", u.getDisplayName()));
						} else if (args[1].equalsIgnoreCase("Humans"))
						{
							p.sendMessage(Msgs.Format_Header.getString("<title>", "Humans"));
							for (Player u : Lobby.getTeam(Team.Human))
								p.sendMessage(Msgs.Format_List.getString("<player>", u.getDisplayName()));

						} else if (args[1].equalsIgnoreCase("Zombies"))
						{
							p.sendMessage(Msgs.Format_Header.getString("<title>", "Zombies"));
							for (Player u : Lobby.getTeam(Team.Zombie))
								p.sendMessage(Msgs.Format_List.getString("<player>", u.getDisplayName()));

						} else
							p.sendMessage(Msgs.Help_Lists.getString("<lists>", "Playing, Humans, Zombies"));

					} else
						p.sendMessage(Msgs.Help_Lists.getString("<lists>", "Playing, Humans, Zombies"));
				}
				// ///////////////////////////////////////////////-LEAVE-////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Leave"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Leave"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (!Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_Not_In.getString());

					else
						ip.leaveInfected();

				}

				// ////////////////////////////////-HELP-///////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Help"))
				{
					if (args.length != 1)
					{
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage(Msgs.Format_Header.getString("<title>", "Infected Help " + args[1] + ""));
						if (sender instanceof Player)
						{
							if (args[1].equalsIgnoreCase("1"))
							{
								if (sender.hasPermission("Infected.Join"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aJoin").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Join", " ", "§7Join Infected")).suggest("/Infected Join").send(p);
								if (sender.hasPermission("Infected.Leave"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aLeave").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Leave", " ", "§7Leave Infected")).suggest("/Infected Leave").send(p);
								if (sender.hasPermission("Infected.Vote"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aVote").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Vote [Arena]", " ", "§7Typing this command without saying", "§7an arena will open the GUI.", "§7Specifing an arena will add a vote for it.")).suggest("/Infected Vote").send(p);
								if (sender.hasPermission("Infected.Classes"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aClasses").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Classes", " ", "§7Open a GUI that allows you to choose", "§7 a class for either teams.", " ", "§eRight Click to select, but not close the GUI.")).suggest("/Infected Classes").send(p);
								if (sender.hasPermission("Infected.Shop"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aShop").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Shop", " ", "§7Open a GUI shop that allows you to", "§7purchase custom items.", " ", "§eRight Click to select, but not close the GUI.")).suggest("/Infected Shop").send(p);
								if (sender.hasPermission("Infected.Grenades"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aGrenades").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Grenades", " ", "§7Open a GUI shop that allows you to", "§7purchase custom grenades.", " ", "§eRight Click to select, but not close the GUI.")).suggest("/Infected Grenades").send(p);
								if (sender.hasPermission("Infected.List"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aList <Playing/Humans/Zombies>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected List <Playing/Humans/Zombies>", " ", "§7See a list of players for that category")).suggest("/Infected List <Playing/Humans/Zombies>").send(p);
							} else if (args[1].equals("2"))
							{
								if (sender.hasPermission("Infected.Chat"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aChat").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Chat [Message]", " ", "§7Typing this command without saying", "§7a message will toggle you into Infected's", "§7team chat.")).suggest("/Infected Chat").send(p);
								if (sender.hasPermission("Infected.Stats"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aStats").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Stats [Stat]", " ", "§7See you current stats/info.")).suggest("/Infected Chat").send(p);
								if (sender.hasPermission("Infected.Suicide"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aSuicide").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Suicide", " ", "§7If you ever get stuck and you need to", "§7get out, just use this command to respawn.")).suggest("/Infected Suicide").send(p);
								if (sender.hasPermission("Infected.Info"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aInfo").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Info", " ", "§7See the current status of the game.")).suggest("/Infected Info").send(p);
								if (sender.hasPermission("Infected.Top"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aTop").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Top [Stat]", " ", "§7View the top 5 players with the", "§7highest total in that stat.", " ", "§eStats: §aKills, Deaths, Points, Score, Time, KillStreak")).suggest("/Infected Top").send(p);
								if (sender.hasPermission("Infected.Arenas"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aArenas").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Arenas", " ", "§7See the list of Arenas")).suggest("/Infected Arena").send(p);
								if (sender.hasPermission("Infected.SetLobby"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aSetLobby").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected SetLobby", " ", "§7Set Infected's Lobby")).suggest("/Infected SetLobby").send(p);
							} else if (args[1].equals("3"))
							{
								if (sender.hasPermission("Infected.SetLeave"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aSetLeave").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected SetLeave", " ", "§7Set the location of where players are", "§7sent to after leaving Infected", " ", "§eIf this isn't set players will be sent to their", "§elast location.")).suggest("/Infected SetLeave").send(p);
								if (sender.hasPermission("Infected.SetSpawn"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aSetSpawn <Zombie/Human/Global>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected SetSpawn <Zombie/Human/Global>", " ", "§7Set the specific teams spawn for this arena", " ", "§b  Global §e-> Both Teams", "§c  Zombie §e-> Spawn for just the zombies", "§a  Humans §e-> Spawn for just the humans.")).suggest("/Infected SetSpawn <Zombie/Human/Global>").send(p);
								if (sender.hasPermission("Infected.Spawns"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aSpawns <Zombie/Human/Global>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Spawns [Zombie/Human/Global]", " ", "§7See how many spawns the specific teams have for this arena", " ", "§b  Global §e-> Both Teams", "§c  Zombie §e-> Spawn for just the zombies", "§a  Humans §e-> Spawn for just the humans.")).suggest("/Infected Spawns <Zombies/Humans/Global>").send(p);
								if (sender.hasPermission("Infected.TpSpawn"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aTpSpawn <Zombie/Human/Global> <#>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Spawns [Zombie/Human/Global]", " ", "§7Teleport to the spawn for the specific teams.", " ", "§b  Global §e-> Both Teams", "§c  Zombie §e-> Spawn for just the zombies", "§a  Humans §e-> Spawn for just the humans.")).suggest("/Infected TpSpawns <Zombies/Humans/Global> <#>").send(p);
								if (sender.hasPermission("Infected.DelSpawn"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aDelSpawn <Zombie/Human/Global> <#>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected DelSpawn [Zombie/Human/Global]", " ", "§7Delete the spawn for the specific teams.", " ", "§b  Global §e-> Both Teams", "§c  Zombie §e-> Spawn for just the zombies", "§a  Humans §e-> Spawn for just the humans.")).suggest("/Infected DelSpawn <Zombies/Humans/Global> <#>").send(p);
								if (sender.hasPermission("Infected.SetArena"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aSetArena <Arena>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected SetArena <Arena>", " ", "§7Select an arena to be edited")).suggest("/Infected SetArena <Arena>").send(p);
								if (sender.hasPermission("Infected.Create"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aCreate <Arena> [Creator]").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Create <Arena> [Creator]", " ", "§7Create an arena")).suggest("/Infected Create <Arena>").send(p);
							} else if (args[1].equalsIgnoreCase("4"))
							{
								if (sender.hasPermission("Infected.Remove"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aRemove <Arena>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Remove <Arena>", " ", "§7Remove an arena")).suggest("/Infected Remove <Arena>").send(p);
								if (sender.hasPermission("Infected.Admin"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aAdmin").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Admin", " ", "§7Access Infected's Admin Menu")).suggest("/Infected Admin").send(p);
								if (sender.hasPermission("Infected.Files"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aFiles <File> [Path] [NewValue]").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected Files <File> [Path] [NewValue]", " ", "§7Edits Infected's configs/settings from in game", " ", "§eLeaving out a path and a new value will show you the file", "§eLeaving out a new value will tell you the path's current value", " ", "§eStats: §aConfig, Arenas, Classes, Grenades, Messages, Players, Shop, Signs")).suggest("/Infected Files").send(p);
								if (sender.hasPermission("Infected.SetClass"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aSetClass <ClassName> <Human/Zombie>").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected SetClass <ClassName> <Human/Zombie>", " ", "§7Create a class for Infected", " ", "§eThis will create a class out of your inventory, armor, and potions.", " ", "§c  Zombie §e-> Spawn for just the zombies", "§a  Humans §e-> Spawn for just the humans.")).suggest("/Infected SetClass <ClassName> <Human/Zombie>").send(p);
								if (sender.hasPermission("Infected.TpLobby"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aTpLobby").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected TpLobby", " ", "§7Teleport to the Lobby")).suggest("/Infected TpLobby").send(p);
								if (sender.hasPermission("Infected.TpLeave"))
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§7/Infected §aTpLeave").itemTooltip(ItemHandler.getFancyMessageItem("§a§l/Infected TpLeave", " ", "§7Teleport to the Leave Location")).suggest("/Infected TpLeave").send(p);
							}
							new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Help Page").command("/Infected Help " + String.valueOf(Integer.parseInt(args[1]) - 1)).then("             ").then("§6Next >>").tooltip("Go to the next Help Page").command("/Infected Help " + String.valueOf(Integer.parseInt(args[1]) + 1)).send(p);

						} else
						{
							if (args[1].equals("1"))
							{
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Join" + ChatColor.WHITE + " - Join Infected");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Leave" + ChatColor.WHITE + " - Leave Infected");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Vote" + ChatColor.WHITE + " - Vote for a map");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Classes" + ChatColor.WHITE + " - Choose a class");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Shop" + ChatColor.WHITE + " - See the purchasable items");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Grenades" + ChatColor.WHITE + " - See the purchasable grenades");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "List" + ChatColor.WHITE + " - See the list of players");
							} else if (args[1].equals("2"))
							{
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Chat [Msg]" + ChatColor.WHITE + " - Chat in your team's chat");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Stats [Player]" + ChatColor.WHITE + " - Check a player's stats");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Suicide" + ChatColor.WHITE + " - Suicide if you're stuck");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Info" + ChatColor.WHITE + " - Check Infected's status");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Top [Stat]" + ChatColor.WHITE + " - Check the top 5 players stats");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Arenas" + ChatColor.WHITE + " - See all possible arenas");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "SetLobby" + ChatColor.WHITE + " - Set the main lobby");
							} else if (args[1].equals("3"))
							{
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "SetSpawn [Global/Human/Zombie]" + ChatColor.WHITE + " - Set the spawn for the selected arena");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "SetLeave" + ChatColor.WHITE + " - Set the leave location");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Spawns [Global/Human/Zombie]" + ChatColor.WHITE + " - List the number of spawns for a map");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "TpSpawn [Global/Human/Zombie] [#]" + ChatColor.WHITE + " - Tp to a spawn ID");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "DelSpawn [Global/Human/Zombie] [ #]" + ChatColor.WHITE + " - Delete the spawn ID");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "SetArena [Arena]" + ChatColor.WHITE + " - Select an arena");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Create [Arena]" + ChatColor.WHITE + " - Create an arena");
							} else if (args[1].equals("4"))
							{
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Remove[Arena]" + ChatColor.WHITE + " - Remove an Arena");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Admin" + ChatColor.WHITE + " - View the admin menu");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "Files" + ChatColor.WHITE + " - Edit Files in Game");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "SetClass" + ChatColor.WHITE + " - Create a class with you inventory");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "TpLobby" + ChatColor.WHITE + " - Tp to the lobby");
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "/Inf " + ChatColor.GREEN + "TpLeave" + ChatColor.WHITE + " - Tp to the leave location");
							}

						}

						sender.sendMessage(Msgs.Format_Line.getString());
					} else
					{
						p.performCommand("Infected Help 1");
					}

				}

				// ///////////////////////////////////////////////////////-VOTE-////////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Vote"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Vote"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (!Lobby.isInGame(p))
						p.sendMessage(Msgs.Error_Game_Not_In.getString());

					else if (Lobby.getGameState() != GameState.Voting && Lobby.getGameState() != GameState.InLobby)
						p.sendMessage(Msgs.Error_Game_Started.getString());

					else if (ip.getVote() != null)
						p.sendMessage(Msgs.Error_Already_Voted.getString());

					else
					{
						// If the user didn't specify an arena, open the voting
						// GUI
						if (args.length == 1)
							Infected.Menus.voteMenu.open(p);
						else
						{
							// Check if the user voted for Random
							Arena arena;
							if (args[1].equalsIgnoreCase("Random"))
							{
								int i;
								Random r = new Random();
								i = r.nextInt(Lobby.getArenas().size());
								arena = Lobby.getArenas().get(i);
								while (!Lobby.isArenaValid(arena))
								{
									i = r.nextInt(Lobby.getArenas().size());
									arena = Lobby.getArenas().get(i);
								}
							} else
							{
								// Assign arena to what ever the user said
								arena = Lobby.getArena(args[1]);
							}
							// If its a valid arena, let the user vote and set
							// everything
							if (Lobby.isArenaValid(arena))
							{
								arena.setVotes(arena.getVotes() + ip.getAllowedVotes());
								ip.setVote(arena);

								for (Player u : Lobby.getPlayersInGame())
								{
									u.sendMessage(Msgs.Command_Vote.getString("<player>", p.getName(), "<arena>", arena.getName()) + ChatColor.GRAY + (ip.getAllowedVotes() != 0 ? " (x" + ip.getAllowedVotes() + ")" : ""));
									InfPlayer up = InfPlayerManager.getInfPlayer(u);
									up.getScoreBoard().showProperBoard();
								}
							}
							// If its not a valid arena tell them that
							else
								p.sendMessage(Msgs.Error_Arena_Not_Valid.getString());
						}
					}
				}

				// //////////////////////////////////////////////////-START-////////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Start"))
				{
					if (!sender.hasPermission("Infected.Start"))
						sender.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (Lobby.getGameState() != GameState.InLobby)
						sender.sendMessage(Msgs.Error_Game_Started.getString());

					else
						Lobby.timerStartVote();
				}

				// //////////////////////////////////////////////-END-////////////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("End"))
				{
					if (!sender.hasPermission("Infected.End"))
						sender.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (Lobby.getGameState() == GameState.InLobby)
						sender.sendMessage(Msgs.Error_Game_Started.getString());

					else
						Game.endGame(true);
				}

				// ////////////////////////////////////////////////-ARENAS-/////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Arenas"))
				{

					if (!sender.hasPermission("Infected.Arenas"))
						sender.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						p.sendMessage(Msgs.Format_Header.getString("<title>", "Arenas"));

						StringBuilder valid = new StringBuilder();
						for (Object o : Lobby.getValidArenas())
						{
							valid.append(o.toString());
							if (Lobby.getValidArenas().size() > 1)
								valid.append(", ");
						}

						StringBuilder inValid = new StringBuilder();
						for (Object o : Lobby.getInValidArenas())
						{
							inValid.append(o.toString());
							if (Lobby.getInValidArenas().size() > 1)
								inValid.append(", ");
						}

						sender.sendMessage(Msgs.Command_Arena_List.getString("<valid>", valid.toString(), "<invalid>", inValid.toString()));
					}
				}
				// ///////////////////////////////////////////////////-ADMIN-///////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Admin"))
				{

					if (!sender.hasPermission("Infected.Admin"))
						sender.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						if (args.length == 2)
						{
							// SHUTDOWN
							if (args[1].equalsIgnoreCase("Shutdown"))
							{
								if (Lobby.getGameState() != GameState.Disabled)
								{
									Lobby.setGameState(GameState.Disabled);
									sender.sendMessage(Msgs.Command_Admin_Shutdown.getString("<state>", "Disabled"));
								} else
								{
									Lobby.setGameState(GameState.InLobby);
									sender.sendMessage(Msgs.Command_Admin_Shutdown.getString("<state>", "Enabled"));
								}
							}
							// RELOAD
							else if (args[1].equalsIgnoreCase("Reload"))
							{
								System.out.println(Msgs.Format_Header.getString("<title>", "Infected"));

								Lobby.loadAllArenas();
								Files.reloadAll();
								AddonManager.getAddons();

								InfClassManager.loadConfigClasses();
								GrenadeManager.loadConfigGrenades();

								Infected.Menus.destroyAllMenus();
								Infected.Menus = new Menus();

								if (Settings.MySQLEnabled())
								{
									System.out.println("Attempting to connect to MySQL");
									Infected.MySQL = new MySQL(Infected.me,
											Files.getConfig().getString("MySQL.Host"),
											Files.getConfig().getString("MySQL.Port"),
											Files.getConfig().getString("MySQL.Database"),
											Files.getConfig().getString("MySQL.Username"),
											Files.getConfig().getString("MySQL.Password"));

									try
									{
										Infected.connection = Infected.MySQL.openConnection();
										Statement statement = Infected.connection.createStatement();

										statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + "Infected" + " (Player VARCHAR(20), Kills INT(10), Deaths INT(10), Points INT(10), Score INT(10), PlayingTime INT(15), HighestKillStreak INT(10));");
										System.out.println("MySQL Table has been loaded");
									} catch (Exception e)
									{
										Files.getConfig().set("MySQL.Enabled", false);
										Files.saveConfig();
										System.out.println("Unable to connect to MySQL");
									}
								}

								System.out.println(Msgs.Format_Line.getString());
								sender.sendMessage(Msgs.Command_Admin_Reload.getString());

							}
							// CODE
							else if (args[1].equalsIgnoreCase("Code"))
							{
								if (p != null)
								{
									p.sendMessage(Msgs.Format_Prefix.getString() + "Code: " + ChatColor.WHITE + ItemHandler.getItemStackToString(p.getItemInHand()));
									p.sendMessage(Msgs.Format_Prefix.getString() + "This code has also been sent to your console to allow for copy and paste!");
									System.out.println(ItemHandler.getItemStackToString(p.getItemInHand()));
								} else
									sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());
							} else
								sender.sendMessage(Msgs.Error_Misc_Unkown_Command.getString());
						} else if (args.length == 3)
						{
							// KICK
							if (args[1].equalsIgnoreCase("Kick"))
							{
								Player u = Bukkit.getPlayer(args[2]);
								if (u == null || !Lobby.isInGame(u))
									sender.sendMessage(Msgs.Error_Game_Not_In.getString());
								else
								{
									u.performCommand("Infected Leave");
									u.sendMessage(Msgs.Command_Admin_Kicked_You.getString());
									sender.sendMessage(Msgs.Command_Admin_Kicked_Them.getString("<player>", u.getName()));
								}
							} else
								sender.sendMessage(Msgs.Error_Misc_Unkown_Command.getString());

						} else if (args.length == 4)
						{
							String user = args[2];

							int i = Integer.parseInt(args[3]);
							if (args[1].equalsIgnoreCase("Points"))
							{
								int newValue = Stats.getPoints(user, Settings.VaultEnabled()) + i;
								Stats.setPoints(user, newValue, Settings.VaultEnabled());
								sender.sendMessage(Msgs.Command_Admin_Changed_Stat.getString("<player>", user, "<stat>", "points", "<value>", String.valueOf(newValue)));
							} else if (args[1].equalsIgnoreCase("Score"))
							{
								int newValue = Stats.getScore(user) + i;
								Stats.setScore(user, newValue);
								sender.sendMessage(Msgs.Command_Admin_Changed_Stat.getString("<player>", user, "<stat>", "score", "<value>", String.valueOf(newValue)));
							} else if (args[1].equalsIgnoreCase("Kills"))
							{
								int newValue = Stats.getKills(user) + i;
								Stats.setKills(user, newValue);
								sender.sendMessage(Msgs.Command_Admin_Changed_Stat.getString("<player>", user, "<stat>", "kills", "<value>", String.valueOf(newValue)));
							} else if (args[1].equalsIgnoreCase("Deaths"))
							{
								int newValue = Stats.getDeaths(user) + i;
								Stats.setDeaths(user, newValue);
								sender.sendMessage(Msgs.Command_Admin_Changed_Stat.getString("<player>", user, "<stat>", "deaths", "<value>", String.valueOf(newValue)));
							} else
								sender.sendMessage(Msgs.Error_Misc_Unkown_Command.getString());

						} else
						{
							sender.sendMessage(Msgs.Format_Header.getString("<title>", "Admin CMDs"));
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.AQUA + "/Inf Admin Points <Player> <#>");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "Add points to a player(Also goes negative)");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.BLUE + "/Inf Admin Score <Player> <#>");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "Add score to a player(Also goes negative)");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.DARK_AQUA + "/Inf Admin Kills <Player> <#>");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "Add kills to a player(Also goes negative)");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.DARK_BLUE + "/Inf Admin Deaths <Player> <#>");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "Add deaths to a player(Also goes negative)");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.DARK_GRAY + "/Inf Admin Kick <Player>");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "Kick a player out of Infected");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.DARK_PURPLE + "/Inf Admin Shutdown");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "Prevent joining Infected");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.DARK_RED + "/Inf Admin Reload");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "Reload the config");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GOLD + "/Inf Admin Code");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + "-> " + ChatColor.WHITE + ChatColor.ITALIC + "See Infected's item code for the item in hand");
						}
					}
				}
				// /////////////////////////////////////////-STATS-///////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Stats"))
				{

					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Stats"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						if (args.length != 1)
						{

							if (!p.hasPermission("Infected.Stats.Other"))
								p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

							else
							{
								String user = args[1];

								p.sendMessage("");
								p.sendMessage(Msgs.Format_Header.getString("<title>", user));
								p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Points: " + ChatColor.GOLD + Stats.getPoints(user, Settings.VaultEnabled()) + ChatColor.GREEN + "     Score: " + ChatColor.GOLD + Stats.getScore(user));
								p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Playing Time: " + ChatColor.GOLD + Time.getOnlineTime((long) Stats.getPlayingTime(user)));
								p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Kills: " + ChatColor.GOLD + Stats.getKills(user) + ChatColor.GREEN + "     Deaths: " + ChatColor.GOLD + Stats.getDeaths(user) + ChatColor.GREEN + "    KDR: " + ChatColor.GOLD + KDRatio.KD(user));
								p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Highest KillStreak: " + ChatColor.GOLD + Stats.getHighestKillStreak(user));
							}
						} else
						{
							String user = sender.getName();
							p.sendMessage("");
							p.sendMessage(Msgs.Format_Header.getString("<title>", user));
							p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Points: " + ChatColor.GOLD + Stats.getPoints(user, Settings.VaultEnabled()) + ChatColor.GREEN + "     Score: " + ChatColor.GOLD + Stats.getScore(user));
							p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Playing Time: " + ChatColor.GOLD + Time.getOnlineTime((long) Stats.getPlayingTime(user)));
							p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Kills: " + ChatColor.GOLD + Stats.getKills(user) + ChatColor.GREEN + "     Deaths: " + ChatColor.GOLD + Stats.getDeaths(user) + ChatColor.GREEN + "    KDR: " + ChatColor.GOLD + KDRatio.KD(user));
							p.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GREEN + "Highest KillStreak: " + ChatColor.GOLD + Stats.getHighestKillStreak(user));
						}
					}
				}

				// //////////////////////////////////////////-TPSPAWN-///////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("TpSpawn"))
				{

					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.TpSpawn"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (ip.getCreating() == null)
						p.sendMessage(Msgs.Error_Arena_None_Set.getString());

					else
					{
						if (args.length == 3 && (args[1].equalsIgnoreCase("Global") || args[1].equalsIgnoreCase("Zombie") || args[1].equalsIgnoreCase("Human")))
						{
							Team team = args[1].equalsIgnoreCase("Human") ? Team.Human : args[1].equalsIgnoreCase("Zombie") ? Team.Zombie : Team.Global;
							Arena a = Lobby.getArena(ip.getCreating());
							int i = Integer.valueOf(args[2]) - 1;
							if (i < a.getSpawns(team).size())
							{
								p.teleport(LocationHandler.getPlayerLocation(a.getSpawns(team).get(i)));
								sender.sendMessage(Msgs.Command_Spawn_Tp.getString("<spawns>", String.valueOf(i + 1)));
							} else
								sender.sendMessage(Msgs.Help_TpSpawn.getString());

						} else
							sender.sendMessage(Msgs.Help_TpSpawn.getString());
					}
				}
				// /////////////////////////////////////////////-TPLOBBY-////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("TpLobby"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.TpLobby"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						p.teleport(Lobby.getLocation());
						p.sendMessage(Msgs.Command_Lobby_Tp.getString());
					}
				}

				// /////////////////////////////////////////////-TPLEAVE-////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("TpLeave"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.TpLeave"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						p.teleport(Lobby.getLeave());
						p.sendMessage(Msgs.Command_Leave_Location_Tp.getString());
					}
				}

				// ////////////////////////////////////-DELSPAWN-////////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("DelSpawn"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.DelSpawn"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (ip.getCreating() == null)
						p.sendMessage(Msgs.Error_Arena_None_Set.getString());

					else
					{
						if (args.length == 2 && (args[1].equalsIgnoreCase("Global") || args[1].equalsIgnoreCase("Zombie") || args[1].equalsIgnoreCase("Human")))
						{
							Team team = args[1].equalsIgnoreCase("Human") ? Team.Human : args[1].equalsIgnoreCase("Zombie") ? Team.Zombie : Team.Global;

							Arena a = Lobby.getArena(ip.getCreating());
							int i = Integer.valueOf(args[1]) - 1;
							if (i < a.getSpawns(team).size())
							{
								List<String> spawns = a.getExactSpawns(team);
								spawns.remove(i);
								a.setSpawns(spawns, team);

								Infected.Menus.destroyMenu(Infected.Menus.voteMenu);
								Infected.Menus.voteMenu = Infected.Menus.getVoteMenu();

								p.sendMessage(Msgs.Command_Spawn_Deleted.getString("<team>", team.toString(), "<spawn>", String.valueOf(i + 1)));
							} else
								p.sendMessage(Msgs.Help_DelSpawn.getString());
						} else
							p.sendMessage(Msgs.Help_DelSpawn.getString());
					}
				}

				// //////////////////////////////////-SPAWNS-//////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Spawns"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					if (!p.hasPermission("Infected.Spawns"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (ip.getCreating() == null)
						p.sendMessage(Msgs.Error_Arena_None_Set.getString());

					else
					{
						Arena a = Lobby.getArena(ip.getCreating());
						if (args.length == 2 && (args[1].equalsIgnoreCase("Global") || args[1].equalsIgnoreCase("Zombie") || args[1].equalsIgnoreCase("Human")))
						{
							Team team = args[1].equalsIgnoreCase("Human") ? Team.Human : args[1].equalsIgnoreCase("Zombie") ? Team.Zombie : Team.Global;
							p.sendMessage(Msgs.Command_Spawn_Spawns.getString("<team>", team.toString(), "<spawns>", String.valueOf(a.getExactSpawns(team).size())));
						} else
							p.sendMessage(Msgs.Help_Spawns.getString());
					}
				}

				// ////////////////////////////////////////////-SETSPAWN-//////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("SetSpawn"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.SetSpawn"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (ip.getCreating() == null)
						p.sendMessage(Msgs.Error_Arena_None_Set.getString());

					else
					{
						if (args.length == 2 && (args[1].equalsIgnoreCase("Global") || args[1].equalsIgnoreCase("Zombie") || args[1].equalsIgnoreCase("Human")))
						{
							Team team = args[1].equalsIgnoreCase("Human") ? Team.Human : args[1].equalsIgnoreCase("Zombie") ? Team.Zombie : Team.Global;

							Location l = p.getLocation();
							String s = LocationHandler.getLocationToString(l);
							Arena a = Lobby.getArena(ip.getCreating());
							List<String> list = a.getExactSpawns(team);
							list.add(s);
							a.setSpawns(list, team);

							Infected.Menus.destroyMenu(Infected.Menus.voteMenu);
							Infected.Menus.voteMenu = Infected.Menus.getVoteMenu();

							p.sendMessage(Msgs.Command_Spawn_Set.getString("<team>", team.toString(), "<spawn>", String.valueOf(list.size())));
						} else
							p.sendMessage(Msgs.Help_SetSpawn.getString());
					}
				}

				// /////////////////////////////////////////-CREATE-///////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Create"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.Create"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						if (args.length != 1)
						{

							String arena = StringUtil.getWord(args[1]);

							if (Lobby.getArena(arena) != null)
								p.sendMessage(Msgs.Error_Arena_Already_Exists.getString());

							else
							{
								p.sendMessage(Msgs.Help_SetSpawn.getString());

								Arena a = new Arena(arena);
								Lobby.addArena(a);

								Infected.Menus.destroyMenu(Infected.Menus.voteMenu);
								Infected.Menus.voteMenu = Infected.Menus.getVoteMenu();

								if (args.length == 3)
									a.setCreator(args[2]);
								else
									a.setCreator("Unkown");

								Block b = p.getLocation().clone().add(0, -1, 0).getBlock();
								a.setBlock(b.getState().getData().toItemStack());

								ip.setCreating(arena);
								p.sendMessage(Msgs.Command_Arena_Created.getString("<arena>", arena));
							}
						} else
							p.sendMessage(Msgs.Help_Create.getString());
					}
				}

				// //////////////////////////////////////-REMOVE-////////////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Remove"))
				{

					if (!p.hasPermission("Infected.Remove"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						if (args.length != 1)
						{

							String arena = args[1];

							if (Lobby.getArena(arena) == null)
								sender.sendMessage(Msgs.Error_Arena_Doesnt_Exist.getString());

							else
							{
								Lobby.removeArena(Lobby.getArena(arena));

								Infected.Menus.destroyMenu(Infected.Menus.voteMenu);
								Infected.Menus.voteMenu = Infected.Menus.getVoteMenu();

								sender.sendMessage(Msgs.Command_Arena_Removed.getString("<arena>", arena));
								return true;
							}
						} else
							sender.sendMessage(Msgs.Help_Remove.getString());
					}
				}

				// /////////////////////////////////////////////////-TOP-/////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Top"))
				{

					if (!p.hasPermission("Infected.Top"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else
					{
						if (args.length == 2)
						{
							String stat = args[1].toLowerCase();
							System.out.println(stat);
							if (stat.equals("kills") || stat.equals("deaths") || stat.equals("score") || stat.equals("time") || stat.equals("points") || stat.equals("killstreak"))
							{
								StatType type = StatType.valueOf(stat);

								int i = 1;
								sender.sendMessage(Msgs.Format_Header.getString("<title>", "Top " + stat.toString()));
								for (String name : Sort.topStats(type, 5))
								{
									if (name != " ")
									{
										if (i == 1)
											sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + i + ". " + ChatColor.GOLD + ChatColor.BOLD + (name.length() == 16 ? name : (name + "                 ").substring(0, 16)) + ChatColor.GREEN + " =-= " + ChatColor.GRAY + (type == StatType.time ? Time.getOnlineTime((long) Stats.getStat(type, name)) : Stats.getStat(type, name)));
										else if (i == 2 || i == 3)
											sender.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + i + ". " + ChatColor.GRAY + ChatColor.BOLD + (name.length() == 16 ? name : (name + "                ").substring(0, 16)) + ChatColor.GREEN + " =-= " + ChatColor.GRAY + (type == StatType.time ? Time.getOnlineTime((long) Stats.getStat(type, name)) : Stats.getStat(type, name)));
										else
											sender.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + i + ". " + ChatColor.WHITE + ChatColor.BOLD + (name.length() == 16 ? name : (name + "                 ").substring(0, 16)) + ChatColor.GREEN + " =-= " + ChatColor.DARK_GRAY + (type == StatType.time ? Time.getOnlineTime((long) Stats.getStat(type, name)) : Stats.getStat(type, name)));
									}
									i++;

									if (i == 6)
										break;
								}

							} else
								sender.sendMessage(Msgs.Error_Top_Not_Stat.getString());
						} else
							sender.sendMessage(Msgs.Help_Top.getString());
					}
				}

				// ////////////////////////////////-SETARENA-/////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("SetArena"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.SetArena"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());
					else
					{
						if (args.length != 1)
						{
							String arena = StringUtil.getWord(args[1]);

							if (Lobby.getArenas().isEmpty())
								p.sendMessage(Msgs.Error_Arena_Doesnt_Exist.getString("<arena>", "Default"));

							else if (Lobby.getArena(arena) == null)
								p.sendMessage(Msgs.Error_Arena_Doesnt_Exist.getString("<arena>", arena));

							else
							{
								ip.setCreating(arena);
								p.sendMessage(Msgs.Command_Arena_Set.getString("<arena>", arena));
								return true;
							}
						} else
							p.sendMessage(Msgs.Help_SetArena.getString());

					}
				}
				// /////////////////////////////////////////////-SETCLASS-/////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("SetClass"))
				{
					if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else if (!p.hasPermission("Infected.SetClass"))
						p.sendMessage(Msgs.Error_Misc_No_Permission.getString());

					else if (args.length == 3 && (args[2].equalsIgnoreCase("Zombie") || args[2].equalsIgnoreCase("Human")))
					{

						String className = args[1];
						Team team = args[2].equalsIgnoreCase("Human") ? Team.Human : Team.Zombie;

						String helmet = p.getInventory().getHelmet() != null ? ItemHandler.getItemStackToString(p.getInventory().getHelmet()) : "id:0";
						String chestplate = p.getInventory().getChestplate() != null ? ItemHandler.getItemStackToString(p.getInventory().getChestplate()) : "id:0";
						String leggings = p.getInventory().getLeggings() != null ? ItemHandler.getItemStackToString(p.getInventory().getLeggings()) : "id:0";
						String boots = p.getInventory().getBoots() != null ? ItemHandler.getItemStackToString(p.getInventory().getBoots()) : "id:0";

						ArrayList<String> items = new ArrayList<String>();
						if (p.getInventory().getContents().length != 0)
							for (ItemStack im : p.getInventory().getContents())
								if (im != null)
									items.add(ItemHandler.getItemStackToString(im));

						String icon = p.getItemInHand().getType() != Material.AIR ? ItemHandler.getItemStackToString(p.getItemInHand()) : "id:276";

						ArrayList<String> potions = new ArrayList<String>();
						if (!p.getActivePotionEffects().isEmpty())
							for (PotionEffect pe : p.getActivePotionEffects())
								potions.add(PotionHandler.getPotionToString(pe));

						Files.getClasses().set("Classes." + team.toString() + "." + className + ".Icon", icon);

						if (Settings.DisguisesEnabled())
							if (Disguises.isPlayerDisguised(p))
								Files.getClasses().set("Classes." + team.toString() + "." + className + ".Disguise", Disguises.getDisguise(p));

						Files.getClasses().set("Classes." + team.toString() + "." + className + ".Helmet", helmet);
						Files.getClasses().set("Classes." + team.toString() + "." + className + ".Chestplate", chestplate);
						Files.getClasses().set("Classes." + team.toString() + "." + className + ".Leggings", leggings);
						Files.getClasses().set("Classes." + team.toString() + "." + className + ".Boots", boots);
						if (!items.isEmpty())
							Files.getClasses().set("Classes." + team.toString() + "." + className + ".Items", items);
						if (!potions.isEmpty())
							Files.getClasses().set("Classes." + team.toString() + "." + className + ".Potion Effects", potions);
						Files.saveClasses();
						InfClassManager.loadConfigClasses();
						Infected.Menus.destroyMenu(Infected.Menus.classHumanMenu);
						Infected.Menus.destroyMenu(Infected.Menus.classZombieMenu);
						Infected.Menus = new Menus();

						sender.sendMessage(Msgs.Command_Classes_SetClass.getString("<class>", className, "<team>", team.toString()));

					} else
						sender.sendMessage(Msgs.Help_SetClass.getString());
				}
				// /////////////////////////////////////////////-FILES-/////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Files"))
				{
					if (!sender.hasPermission("Infected.Files"))
						sender.sendMessage(Msgs.Error_Misc_No_Permission.getString());
					else
					{
						Configuration config = null;

						if (args.length > 1)
						{
							if (args[1].equalsIgnoreCase("Config"))
								config = Files.getConfig();
							else if (args[1].equalsIgnoreCase("Arenas"))
								config = Files.getArenas();
							else if (args[1].equalsIgnoreCase("Classes"))
								config = Files.getClasses();
							else if (args[1].equalsIgnoreCase("Grenades"))
								config = Files.getGrenades();
							else if (args[1].equalsIgnoreCase("Messages"))
								config = Files.getMessages();
							else if (args[1].equalsIgnoreCase("Players"))
								config = Files.getPlayers();
							else if (args[1].equalsIgnoreCase("Shop"))
								config = Files.getShop();
							else if (args[1].equalsIgnoreCase("Signs"))
								config = Files.getSigns();
							else
								sender.sendMessage(Msgs.Error_Misc_Not_A_File.getString("<files>", "Config, Arenas, Classes, Grenades, Messages, Players, Shop, Signs"));

						}
						if (args.length == 2)
						{

							if (config != null)
							{
								for (String path : config.getConfigurationSection("").getKeys(true))
								{
									if (!config.getString(path).startsWith("MemorySection"))
										sender.sendMessage(ChatColor.YELLOW + path.replaceAll(" ", "_") + ChatColor.WHITE + ": " + ChatColor.GRAY + config.getString(path).replaceAll(" ", "_"));
								}
							}
						} else if (args.length == 3)
						{
							String path = args[2].replaceAll("_", " ");

							if (config != null)
							{
								sender.sendMessage(Msgs.Command_Files_Value.getString("<path>", path, "<value>", config.getString(path).replaceAll("_", " ")));
							}
						} else if (args.length == 4)
						{
							String path = args[2].replaceAll("_", " ");
							String newvalue = args[3].replaceAll("_", " ");

							if (config != null)
							{
								if (config.get(path) != null)
								{
									sender.sendMessage(Msgs.Command_Files_Changed.getString("<path>", path, "<value>", config.getString(path), "<newvalue>", newvalue));
									if (newvalue.equalsIgnoreCase("True") || newvalue.equalsIgnoreCase("False"))
										config.set(path.replaceAll("_", " "), Boolean.valueOf(newvalue.toUpperCase()));
									else if (newvalue.startsWith(String.valueOf('[')) && newvalue.endsWith("]"))
									{
										p.sendMessage("List");
										String[] list = (newvalue.replaceAll("\\[", "").replaceAll("]", "")).split(",");
										config.set(path, list);
									} else
										try
										{
											int i = Integer.valueOf(newvalue);
											config.set(path, i);
										} catch (Exception ex)
										{
											config.set(path, newvalue);
										}
									Files.saveAll();
								} else
									sender.sendMessage(Msgs.Error_Misc_Not_A_Path.getString());

							} else
								sender.sendMessage(Msgs.Help_Files.getString("<files>", "Config, Abilities, Arenas, Classes, Grenades, Messages, Players, Shop, Signs"));

						} else
							sender.sendMessage(Msgs.Help_Files.getString("<files>", "Config, Abilities, Arenas, Classes, Grenades, Messages, Players, Shop, Signs"));
					}
				}
				// ///////////////////////////////////////////////-SETUP-///////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Setup"))
				{

					if (!sender.hasPermission("Infected.Files"))
						sender.sendMessage(Msgs.Error_Misc_No_Permission.getString());
					else if (p == null)
						sender.sendMessage(Msgs.Error_Misc_Not_Player.getString());

					else
					{
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage("");
						sender.sendMessage(Msgs.Format_Header.getString("<title>", " Setup "));
						sender.sendMessage("");
						
						if (args.length == 1)
						{
							new FancyMessage(Msgs.Format_Prefix.getString()).then("" + ChatColor.GOLD + ChatColor.BOLD + "Edit The Lobby").tooltip(ChatColor.GOLD + "Edit the Lobby").command("/Infected Setup Lobby").send(p);
							for (Arena arena : Lobby.getArenas())
								new FancyMessage(Msgs.Format_Prefix.getString()).then("" + ChatColor.YELLOW + ChatColor.BOLD + "Edit Arena: " + RandomChatColor.getColor() + arena.getName()).tooltip(ChatColor.YELLOW + "Edit " + arena.getName()).command("/Infected Setup " + arena.getName()).send(p);
						} else
						{
							if (Lobby.getArena(args[1]) != null)
							{
								Arena arena = Lobby.getArena(args[1]);
								if (args.length == 2)
								{
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.GREEN + "SetArena").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.GREEN + arena.getName() + " Select the Arena")).command("/Infected SetArena "+arena.getName()).send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.YELLOW + "Spawns").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.YELLOW + arena.getName() + " Spawns", "   §eGlobal: " + arena.getExactSpawns(Team.Global).size(), "   §aHuman: " + arena.getExactSpawns(Team.Human).size(), "   §cZombie: " + arena.getExactSpawns(Team.Zombie).size())).command("/Infected Setup "+arena.getName()+" Spawns").send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.RED + "Creator").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.RED + arena.getName() + " Creator", "   §eCreator: " + arena.getCreator())).command("/Infected Setup "+arena.getName()+" Creator").send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.DARK_AQUA + "Block").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.DARK_AQUA + arena.getName() + " Creator", "   §eBlock: " + ItemHandler.getItemStackToString(arena.getBlock()))).command("/Infected Setup "+arena.getName()+" Block").send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.LIGHT_PURPLE + "Time").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.LIGHT_PURPLE + arena.getName() + " Time", "   §eVoting Time: " + arena.getSettings().getVotingTime(), "   §cInfecting Time: " + arena.getSettings().getInfectingTime(), "   §aGame Time: " + arena.getSettings().getGameTime())).command("/Infected Setup "+arena.getName()+" Time").send(p);
									//new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.AQUA + "Booleans").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.AQUA + arena.getName() + " Booleans", "   §eInteract Blocked: " + arena.getSettings().interactDisabled(), "   §cEnchant Blocked: " + arena.getSettings().enchantDisabled(), "   §6Hunger Blocked: " + arena.getSettings().hungerDisabled())).command("/Infected Setup "+arena.getName()+" Booleans").send(p);
									p.sendMessage("");
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup").send(p);
								}
								else{

									if (args[2].equals("Spawns"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Tp to a Global Spawn").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Teleport to a Global Spawn")).suggest("/Infected TpSpawn Global #").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Tp to a Human Spawn").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Teleport to a Human Spawn")).suggest("/Infected TpSpawn Human #").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Tp to a Zombie Spawn").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Teleport to a Zombie Spawn")).suggest("/Infected TpSpawn Zombie #").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup "+ arena.getName()).send(p);
									}
									else if (args[2].equals("Creator"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Creator").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() +" Set a creator for the arena")).suggest("/Infected SetCreator <Creator>").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup "+ arena.getName()).send(p);
									}
									else if (args[2].equals("Block"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set The Block").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() +"Set the block you see in the vote menu")).suggest("/Infected SetBlock <ItemCode>").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup "+ arena.getName()).send(p);
									}
									else if (args[2].equals("Time"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Voting Time").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() +" Set the time you have to vote")).suggest("/Infected Files Arenas Arenas."+arena.getName()+".Time.Voting <#>").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Infecting Time").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() +" Set the time you have to wait for the first Infected")).suggest("/Infected Files Arenas Arenas."+arena.getName()+".Time.Infecting <#>").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Play Time").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() +" Set the time you have to play")).suggest("/Infected Files Arenas Arenas."+arena.getName()+".Time.Game <#>").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup "+ arena.getName()).send(p);
									}
								}
							} else if (args[1].equals("Lobby"))
							{
								if (args.length == 2)
								{
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.GREEN + "Arenas").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.GREEN + "Lobby Arenas", "Total Arenas: " + Lobby.getArenas().size())).command("/Infected Setup Lobby Arenas").send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.DARK_AQUA + "Classes").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.DARK_AQUA + "Lobby Classes", "§aHuman: " + InfClassManager.getClasses(Team.Human).size(), "§cZombies: " + InfClassManager.getClasses(Team.Zombie).size())).command("/Infected Setup Lobby Classes").send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.WHITE + "Location").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.WHITE + "Lobby Location", "§7Location: " + LocationHandler.getRoundedLocation(Lobby.getLocation()))).command("/Infected Setup Lobby Location").send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.GRAY + "Leave").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.GRAY + "Lobby Leave", "§fLeave: " + LocationHandler.getRoundedLocation(Lobby.getLeave()))).command("/Infected Setup Lobby Leave").send(p);
									new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.RED + "Booleans").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.RED + "Lobby Booleans", "§7Can Join Well Started: " + Settings.isJoiningDuringGamePrevented(), "§5Can Edit Inventory: " + !Settings.isEditingInventoryPrevented())).command("/Infected Setup Lobby Booleans").send(p);
									p.sendMessage("");
									new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup").send(p);
								} else
								{
									if (args[2].equals("Arenas"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.GREEN + "See all Arenas").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.GREEN + "See all the arenas")).command("/Infected Arenas").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.YELLOW + "Create an Arena").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.YELLOW + "Create an arena")).suggest("/Infected Create <Arena Name>").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.RED + "Remove an Arena").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.RED + "Remove an arena")).suggest("/Infected Remove <Arena Name>").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(ChatColor.AQUA + "Select an Arena").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.AQUA + "Select an arena")).suggest("/Infected SetArena <Arena Name>").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup Lobby").send(p);
									} else if (args[2].equals("Classes"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Create a Class").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Create a class with your inventory")).suggest("/Infected SetClass <ClassName> <Human/Zombie>").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup Lobby").send(p);
									} else if (args[2].equals("Location"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Location").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Set to where you are")).command("/Infected SetLobby").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Teleport to Location").itemTooltip(ItemHandler.getFancyMessageItem(ChatColor.GREEN + "Tp to the lobby")).command("/Infected TpLobby").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup Lobby").send(p);
									} else if (args[2].equals("Leave"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Leave").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Set to where you are")).command("/Infected SetLeave").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Teleport to Leave").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Tp to the leave")).command("/Infected TpLeave").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup Lobby").send(p);
									} else if (args[2].equals("Booleans"))
									{
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Can Join Well Started").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Set Can Join When Started")).suggest("/Infected Files Config Settings.Misc.Prevent_Joining_During_Game <True/False>").send(p);
										new FancyMessage(Msgs.Format_Prefix.getString()).then(RandomChatColor.getColor() + "Set Can Edit Inventory").itemTooltip(ItemHandler.getFancyMessageItem(RandomChatColor.getColor() + "Set if they can edit their inventory")).suggest("/Infected Files Config Settings.Misc.Prevent_Editing_Inventory <True/False>").send(p);
										p.sendMessage("");
										new FancyMessage(Msgs.Format_Prefix.getString()).then("§4<< Back").tooltip("Go back a Setup Page").command("/Infected Setup Lobby").send(p);
									}
								}
								sender.sendMessage("");
								sender.sendMessage(Msgs.Format_Line.getString());
							} else
							{
								p.performCommand("Infected Setup");
							}
						}
					}
				}
				// /////////////////////////////////////////////-ADDONS-/////////////////////////////////////////
				else if (args.length > 0 && args[0].equalsIgnoreCase("Addons"))
				{

					sender.sendMessage("");
					sender.sendMessage(Msgs.Format_Header.getString("<title>", " Addons "));
					if (p == null)
					{
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Disguise Support:" + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + (Settings.DisguisesEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled")));
						if (Settings.DisguisesEnabled())
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Disguise Plugin:" + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + Infected.Disguiser.getName());
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "CrackShot Support:" + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + (Settings.CrackShotEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled")));
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Factions Support:" + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + (Settings.FactionsEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled")));
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "mcMMO Support:" + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + (Settings.mcMMOEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled")));
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Vault Support:" + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + (Settings.VaultEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled")));
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Infected-Ranks Support:" + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + (Bukkit.getPluginManager().getPlugin("InfectedAddon-Ranks") != null ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled")));
						sender.sendMessage(Msgs.Format_Line.getString());
					} else
					{
						new FancyMessage(Msgs.Format_Prefix.getString()).then("§7Disguise Support: " + (Settings.DisguisesEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled"))).tooltip("§aIf enabled, zombies can be actual zombies!").send(p);

						if (Settings.DisguisesEnabled())
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Disguise Plugin: " + "" + ChatColor.GREEN + ChatColor.ITALIC + " " + Infected.Disguiser.getName());
						new FancyMessage(Msgs.Format_Prefix.getString()).then("§7CrackShot Support: " + (Settings.CrackShotEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled"))).tooltip("§aIf enabled, you can use guns!").send(p);
						new FancyMessage(Msgs.Format_Prefix.getString()).then("§7Factions Support: " + (Settings.FactionsEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled"))).tooltip("§aIf enabled, pvp ignores factions relations!").send(p);
						new FancyMessage(Msgs.Format_Prefix.getString()).then("§7mcMMO Support: " + (Settings.mcMMOEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled"))).tooltip("§aIf enabled, pvp ignores mcMMO's levels!").send(p);
						new FancyMessage(Msgs.Format_Prefix.getString()).then("§7Vault Support: " + (Settings.VaultEnabled() ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled"))).tooltip("§aIf enabled, money can be given as a reward!").send(p);
						new FancyMessage(Msgs.Format_Prefix.getString()).then("§7Infected-Ranks Support: " + (Bukkit.getPluginManager().getPlugin("InfectedAddon-Ranks") != null ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "Enabled") : ("" + ChatColor.RED + ChatColor.ITALIC + "Disabled"))).tooltip("§aIf enabled, Infected has ranks!").send(p);
					}
				}
				// ///////////////////////////////////////////////-ELSE-//////////////////////////////////////////////
				else
				{
					if (args.length == 0)
					{
						sender.sendMessage("");
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.DARK_AQUA + ChatColor.STRIKETHROUGH + ">>>>>>[" + ChatColor.GOLD + ChatColor.BOLD + "Infected" + ChatColor.DARK_AQUA + ChatColor.STRIKETHROUGH + "]<<<<<<");
						if (Infected.update)
							if (p == null)
								sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.RED + ChatColor.BOLD + "Update Available: " + ChatColor.WHITE + ChatColor.BOLD + Infected.updateName);
							else
								new FancyMessage(Msgs.Format_Prefix.getString()).then("§c§lUpdate Available: §f§l" + Infected.updateName).tooltip("§aClick to open page").link(Infected.updateLink).send(p);

						sender.sendMessage("");
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Author: " + ChatColor.GREEN + ChatColor.BOLD + "SniperzCiinema" + ChatColor.WHITE + ChatColor.ITALIC + "(" + ChatColor.DARK_AQUA + "xXSniperzXx_SD" + ChatColor.WHITE + ")");
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.GRAY + "Version: " + ChatColor.GREEN + ChatColor.BOLD + Infected.version);
						sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.WHITE + "BukkitDev: " + ChatColor.GREEN + ChatColor.BOLD + "http://bit.ly/McInfected");
						if (p == null)
						{
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.YELLOW + "For Help type: /Infected Help");
							sender.sendMessage(Msgs.Format_Prefix.getString() + ChatColor.YELLOW + "For Addons type: /Infected Addons");
						} else
						{
							new FancyMessage(Msgs.Format_Prefix.getString()).then("§eFor Help type: Infected Help").tooltip("§aClick to autotype").suggest("/Infected Help 1").send(p);
							new FancyMessage(Msgs.Format_Prefix.getString()).then("§eFor Help type: Infected Addons").tooltip("§aClick to autotype").suggest("/Infected Addons").send(p);
						}
						sender.sendMessage(Msgs.Format_Line.getString());
						return true;
					} else
						sender.sendMessage(Msgs.Error_Misc_Unkown_Command.getString());
				}
			}
		}
		return true;
	}
}
