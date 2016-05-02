package nl.Steffion.SeekAndFind;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mcstats.Metrics;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SeekAndFind extends JavaPlugin implements Listener {
	public static SeekAndFind plugin;
	public final SeekAndFindPlayer SeekAndFindPlayer = new SeekAndFindPlayer(this);

	public Random random = new Random();

	File configFile;
	FileConfiguration configFileC;
	ConfigurationSection CconfigFile = null;

	File messagesFile;
	FileConfiguration messagesFileC;
	ConfigurationSection CmessagesFile = null;

	File arenasFile;
	FileConfiguration arenasFileC;
	ConfigurationSection CarenasFile = null;

	File signsFile;
	FileConfiguration signsFileC;
	ConfigurationSection CsignsFile = null;

	String pluginTag = null;
	int autoloadinterval = 15;
	String link = null;
	String version = null;
	String unknown = "\u00A74Unknown Message.";
	Boolean updatechecker = true;
	String normalcolour = "\u00A7b";
	String headercolour = "\u00A79";
	String warningcolour = "\u00A7e";
	String errorcolour = "\u00A7c";
	String argcolour = "\u00A7e";
	String noPerms = "%errYou don't have the permissions to do %arg/%kind%err.";
	String noPermsToDo = "%errYou don't have the permissions to do that!";
	String noCommand = "%errCommand not found. Use %arg/seekandfind <help/h>  [page number]%err.";

	public Map<Player, Location> pos1 = new HashMap<Player, Location>();
	public Map<Player, Location> pos2 = new HashMap<Player, Location>();
	public Map<Player, String> player_arena = new HashMap<Player, String>();
	public Map<Player, Location> player_locbefore = new HashMap<Player, Location>();
	public Map<Player, ItemStack[]> player_inv = new HashMap<Player, ItemStack[]>();
	public Map<Player, ItemStack[]> player_inva = new HashMap<Player, ItemStack[]>();
	public Map<Player, GameMode> player_gm = new HashMap<Player, GameMode>();
	public Map<String, String> arena_status = new HashMap<String, String>();
	public Map<String, Integer> arena_players = new HashMap<String, Integer>();
	public Map<String, Integer> arena_timer = new HashMap<String, Integer>();
	public Map<String, Player> arena_it = new HashMap<String, Player>();

	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		getServer().getPluginManager().registerEvents(this, this);

		pluginTag = "\u00A79[" + pdfFile.getName() + "\u00A79] ";

		configFile = new File(getDataFolder(), "config.yml");
		messagesFile = new File(getDataFolder(), "messages.yml");
		arenasFile = new File(getDataFolder(), "arenas.yml");
		signsFile = new File(getDataFolder(), "signs.yml");

		try {
			CheckFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}

		configFileC = new YamlConfiguration();
		messagesFileC = new YamlConfiguration();
		arenasFileC = new YamlConfiguration();
		signsFileC = new YamlConfiguration();

		LoadAllFiles(true);

		if (updatechecker) {
			if (updateCheck()) {
				this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
					@Override
					public void run() {
						Bukkit.getConsoleSender().sendMessage(pluginTag + warningcolour + "There is a new version available for SeekAndFind!");
						Bukkit.getConsoleSender().sendMessage(pluginTag + warningcolour + "Version v" + version + ".");
						Bukkit.getConsoleSender().sendMessage(pluginTag + warningcolour + "Download it here: " + link);

						for (Player player : Bukkit.getOnlinePlayers()) {
							if (player.isOp()) {
								player.sendMessage(pluginTag + warningcolour + "There is a new version available for SeekAndFind!");
								player.sendMessage(pluginTag + warningcolour + "Version v" + version + ".");
								player.sendMessage(pluginTag + warningcolour + "Download it here: " + link);
							} else if (perms(player, "seekandfind.update", "admin", null)) {
								player.sendMessage(pluginTag + warningcolour + "There is a new version available for SeekAndFind!");
								player.sendMessage(pluginTag + warningcolour + "Version v" + version + ".");
								player.sendMessage(pluginTag + warningcolour + "Download it here: " + link);
							}
						}
					}
				}, 1, 600);
			}
		} else {
			Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Checking for updates... Result: " + errorcolour + "\u00A7kDisabled!");
		}

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
			if (!metrics.configuration.getBoolean("opt-out", false)) {
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Sending " + argcolour + "MCStats" + normalcolour + ". Result: \u00A7a\u00A7kOK!");
			} else {
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Sending " + argcolour + "MCStats" + normalcolour + ". Result: " + errorcolour + "\u00A7kDisabled!");
			}
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Sending " + argcolour + "MCStats" + normalcolour + ". Result: " + errorcolour + "\u00A7kError!");
		}

		Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + pdfFile.getName() + " \u00A7a\u00A7k+\u00A7r" + normalcolour + " Enabled. Version " + pdfFile.getVersion() + ". Made by Steffion.");

		if (autoloadinterval != 0) {
			this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
				@Override  
				public void run() {
					try {
						CheckFiles();
					} catch (Exception e) {
						e.printStackTrace();
					}

					LoadAllFiles(false);
				}
			}, 40, autoloadinterval);
		}

		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				CheckSigns();
				for (String an : CarenasFile.getKeys(false)) {
					if (arena_timer.get(an) != null
							&& arena_status.get(an) != null) {
						if (arena_status.get(an) == "lobby") {
							if (arena_timer.get(an) != 0) {
								arena_timer.put(an, arena_timer.get(an) - 1);
							} else if (arena_timer.get(an) == 0) {
								ArrayList<Player> players = new ArrayList<Player>();
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (player_arena.get(player) != null) {
										if (player_arena.get(player).equals(an)) {
											players.add(player);
										}
									}
								}

								Player player = players.get(random.nextInt(players.size()));
								Location loc = new Location (Bukkit.getWorld((String) getFile(an + ".arena.world", "String", arenasFileC)),
										getInt(an + ".arena.x", arenasFileC),
										getInt(an + ".arena.y", arenasFileC),
										getInt(an + ".arena.z", arenasFileC),
										getInt(an + ".arena.yaw", arenasFileC),
										getInt(an + ".arena.pitch", arenasFileC));
								player.teleport(loc);
								player.addPotionEffect(new PotionEffect (PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1), true);
								player.getInventory().setHelmet(new ItemStack(Material.ANVIL));
								player.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
								player.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS));

								arenaBroadcast (an, (String) getFile("arenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", player.getDisplayName())
										.replaceAll("%2", getInt("timer.freerun", CconfigFile).toString()));

								arena_it.put(an, player);
								arena_status.put(an, "prepare");
								arena_timer.put(an, getInt("timer.freerun", CconfigFile));
								return;
							}

							if (arena_timer.get(an) == 1) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "1"));
							} else if (arena_timer.get(an) == 2) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "2"));
							} else if (arena_timer.get(an) == 3) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "3"));
							} else if (arena_timer.get(an) == 4) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "4"));
							} else if (arena_timer.get(an) == 5) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "5"));
							} else if (arena_timer.get(an) == 10) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "10"));
							} else if (arena_timer.get(an) == 30) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "30"));
							} else if (arena_timer.get(an) == 60) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "60"));
							} else if (arena_timer.get(an) == 120) {
								arenaBroadcast (an, getFile("beforearenaStarting", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "120"));
							}
						} else if (arena_status.get(an) == "prepare") {
							if (arena_timer.get(an) != 0) {
								arena_timer.put(an, arena_timer.get(an) - 1);
							} else if (arena_timer.get(an) == 0) {
								ArrayList<Player> players = new ArrayList<Player>();
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (player_arena.get(player) != null) {
										if (player_arena.get(player).equals(an)) {
											players.add(player);
										}
									}
								}

								Location loc = new Location (Bukkit.getWorld((String) getFile(an + ".arena.world", "String", arenasFileC)),
										getInt(an + ".arena.x", arenasFileC),
										getInt(an + ".arena.y", arenasFileC),
										getInt(an + ".arena.z", arenasFileC),
										getInt(an + ".arena.yaw", arenasFileC),
										getInt(an + ".arena.pitch", arenasFileC));
								for (Player player : players) {
									if (!arena_it.get(an).equals(player)) {
										player.teleport(loc);
									}
								}

								arenaBroadcast (an, (String) getFile("arenaFreerunover", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", getInt("timer.arena", CconfigFile).toString()));
								arena_status.put(an, "arena");
								arena_timer.put(an, getInt("timer.arena", CconfigFile));
								return;
							}
						} else if (arena_status.get(an) == "arena") {
							if (arena_timer.get(an) != 0) {
								arena_timer.put(an, arena_timer.get(an) - 1);
							} else if (arena_timer.get(an) == 0) {
								ArrayList<Player> players = new ArrayList<Player>();
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (player_arena.get(player) != null) {
										if (player_arena.get(player).equals(an)) {
											players.add(player);
										}
									}
								}

								arenaBroadcast (an, (String) getFile("arenaOvertime", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", arena_it.get(an).getDisplayName()));

								for (Player player : players) {
									arenaPlayerJoinLeave(player, an, "overtime");
									if (arena_it.get(an).equals(player)) {
										player.playSound(player.getLocation(), Sound.LEVEL_UP, 5, 1);
										Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
										FireworkMeta fwm = fw.getFireworkMeta();

										Type type = Type.BALL_LARGE;
										Color c1 = Color.fromRGB(255, 255, 0);
										FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(c1).with(type).trail(true).build();
										fwm.addEffect(effect);
										fwm.setPower(0);

										fw.setFireworkMeta(fwm);
									} else {
										player.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 10, (float) 0.1);
									}
								}

								arena_status.put(an, "lobby");
								arena_timer.put(an, null);
								arena_it.put(an, null);
								return;
							}

							if (arena_timer.get(an) == 1) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "1"));
							} else if (arena_timer.get(an) == 2) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "2"));
							} else if (arena_timer.get(an) == 3) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "3"));
							} else if (arena_timer.get(an) == 4) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "4"));
							} else if (arena_timer.get(an) == 5) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "5"));
							} else if (arena_timer.get(an) == 10) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "10"));
							} else if (arena_timer.get(an) == 30) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "30"));
							} else if (arena_timer.get(an) == 60) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "60"));
							} else if (arena_timer.get(an) == 120) {
								arenaBroadcast (an, getFile("beforearenaEnd", "String", CmessagesFile)
										.toString()
										.replaceAll("%1", "120"));
							}
						}
					}
				}
			}
		}, 0, 20);
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		try {
			CheckFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoadAllFiles(false);

		for (String an : CarenasFile.getKeys(false)) {
			arenaBroadcast(an, (String) getFile("reloadKick", "String", CmessagesFile));
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player_arena.get(player) != null) {
				arenaPlayerJoinLeave(player, player_arena.get(player), "forceleave");
			}
		}
		Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + pdfFile.getName() + " \u00A7c\u00A7k-\u00A7r" + normalcolour + " Disabled. Version " + pdfFile.getVersion() + ". Made by Steffion.");
	}

	public boolean updateCheck () {
		PluginDescriptionFile pdfFile = this.getDescription();
		try {
			URL url = new URL("http://steffion.net16.net/rssfeeds/seekandfind.rss");
			InputStream input = url.openConnection().getInputStream();
			if (input.available() != 0) {
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
				Node lastFile = document.getElementsByTagName("item").item(0);
				NodeList childs = lastFile.getChildNodes();
				version = childs.item(1).getTextContent().replaceAll("v", "");
				link = childs.item(3).getTextContent();
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Checking for updates... Result: \u00A7a\u00A7kOK!");
				if (!pdfFile.getVersion().equals(version)) {
					return true;
				}
			} else {
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Checking for updates... Result: " + errorcolour + "\u00A7kError!");
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Checking for updates... Result: " + errorcolour + "\u00A7kError!");
		}
		return false;
	}

	public void CopyFile (InputStream in, File playerFile) {
		try {
			OutputStream out = new FileOutputStream(playerFile);
			byte[] buf = new byte[1024];
			int len;
			while((len=in.read(buf))>0) {
				out.write(buf,0,len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void LoadAllFiles (boolean first) {
		PluginDescriptionFile pdfFile = this.getDescription();
		try {
			configFileC.load(configFile);
			configFileC.options().copyDefaults(true);
			CconfigFile = configFileC.getConfigurationSection("");
			CconfigFile.getKeys(true);

			if ((Boolean) getFile("tag.enabled", "Boolean", CconfigFile)) {
				if (getFile("tag.tag", "String", CconfigFile).toString().contains("%name")) {
					pluginTag = (String) getFile("tag.tag", "String", CconfigFile).toString().replaceAll("%name", pdfFile.getName());
				} else {
					pluginTag = "\u00A79[" + pdfFile.getName() + "\u00A79] ";
				}
			} else {
				pluginTag = "";
			}

			if ((Boolean) getFile("autoload.enabled", "Boolean", CconfigFile)) {
				if (getInt("autoload.interval", CconfigFile) != null) {
					autoloadinterval = getInt("autoload.interval", CconfigFile) * 20;
				} else {
					autoloadinterval = 300;
				}
			} else {
				autoloadinterval = 0;
			}

			updatechecker = (Boolean) getFile("updatechecker.enabled", "Boolean", CconfigFile);

			normalcolour = (String) getFile("chat.normalcolour", "String", CconfigFile);
			headercolour = (String) getFile("chat.headercolour", "String", CconfigFile);
			warningcolour = (String) getFile("chat.warningcolour", "String", CconfigFile);
			errorcolour = (String) getFile("chat.errorcolour", "String", CconfigFile);
			argcolour = (String) getFile("chat.argcolour", "String", CconfigFile);

			if (first) {
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "config.yml" + normalcolour + "'. Result: \u00A7a\u00A7kOK!");
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "config.yml" + normalcolour + "'. Result: " + errorcolour + "\u00A7kError!");
			e.printStackTrace();
		}

		try {
			messagesFileC.load(messagesFile);
			messagesFileC.options().copyDefaults(true);
			CmessagesFile = messagesFileC.getConfigurationSection("");
			CmessagesFile.getKeys(true);

			noPerms = (String) getFile("noPerms", "String", CmessagesFile);
			noPermsToDo = (String) getFile("noPermsToDo", "String", CmessagesFile);
			noCommand = (String) getFile("noCommand", "String", CmessagesFile);

			if (first) {
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "messages.yml" + normalcolour + "'. Result: \u00A7a\u00A7kOK!");
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "messages.yml" + normalcolour + "'. Result: " + errorcolour + "\u00A7kError!");
			e.printStackTrace();
		}

		try {
			arenasFileC.load(arenasFile);
			arenasFileC.options().copyDefaults(true);
			CarenasFile = arenasFileC.getConfigurationSection("");
			CarenasFile.getKeys(true);

			for (String an : CarenasFile.getKeys(false)) {
				if (arena_status.get(an) == null) {
					arena_status.put(an, "lobby");
				}

				if (arena_players.get(an) == null) {
					arena_players.put(an, 0);
				}
			}

			if (first) {
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "arenas.yml" + normalcolour + "'. Result: \u00A7a\u00A7kOK!");
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "arenas.yml" + normalcolour + "'. Result: " + errorcolour + "\u00A7kError!");
			e.printStackTrace();
		}

		try {
			signsFileC.load(signsFile);
			signsFileC.options().copyDefaults(true);
			CsignsFile = signsFileC.getConfigurationSection("");
			CsignsFile.getKeys(true);

			if (first) {
				Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "signs.yml" + normalcolour + "'. Result: \u00A7a\u00A7kOK!");
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(pluginTag + normalcolour + "Loading '" + argcolour + "signs.yml" + normalcolour + "'. Result: " + errorcolour + "\u00A7kError!");
			e.printStackTrace();
		}
	}

	public void CheckFile (File theFile, String name) {
		if(!theFile.exists()){
			theFile.getParentFile().mkdirs();
			CopyFile(getResource("emptyFile.yml"), theFile);
			Bukkit.getConsoleSender().sendMessage(pluginTag + warningcolour + "Couldn't find '" + argcolour + name + ".yml" + warningcolour + "' making new one.");
		}
	}

	public void CheckFiles() {
		if(!configFile.exists()){
			configFile.getParentFile().mkdirs();
			CopyFile(getResource("config.yml"), configFile);
			Bukkit.getConsoleSender().sendMessage(pluginTag + warningcolour + "Couldn't find '" + argcolour + "config.yml" + warningcolour + "' making new one.");
		}

		if(!messagesFile.exists()){
			messagesFile.getParentFile().mkdirs();
			CopyFile(getResource("messages.yml"), messagesFile);
			Bukkit.getConsoleSender().sendMessage(pluginTag + warningcolour + "Couldn't find '" + argcolour + "messages.yml" + warningcolour + "' making new one.");
		}

		if(!arenasFile.exists()){
			arenasFile.getParentFile().mkdirs();
			CheckFile(arenasFile, "arenas");
		}

		if(!signsFile.exists()){
			signsFile.getParentFile().mkdirs();
			CheckFile(signsFile, "signs");
		}
	}

	public void CheckSigns() {
		for (String sign : CsignsFile.getKeys(false)) {
			String[] signsplit = sign.split("@");
			if (signsplit.length == 4) {
				Location loc = new Location(Bukkit.getWorld(signsplit[0]),
						Integer.valueOf(signsplit[1]),
						Integer.valueOf(signsplit[2]),
						Integer.valueOf(signsplit[3]));
				Block block = loc.getBlock();
				String an = (String) getFile(sign, "String", CsignsFile);

				if (block.getType() == Material.SIGN_POST
						|| block.getType() == Material.WALL_SIGN) {
					if (an.equals("leave")) {
						Sign signblock = (Sign) block.getState();

						signblock.setLine(0, "\u00A79[\u00A7eS&F\u00A79]");
						signblock.setLine(1, "\u00A74LEAVE");
						signblock.setLine(2, "\u00A78Right-Click");
						signblock.setLine(3, "\u00A78To leave.");
						signblock.update();
					} else if (arena_status.get(an) != null) {
						Sign signblock = (Sign) block.getState();

						signblock.setLine(0, "\u00A79[\u00A7eS&F\u00A79]");
						signblock.setLine(1, "\u00A78" + an);
						signblock.setLine(2, "\u00A78" + arena_players.get(an) + "/" + getInt(an + ".players", CarenasFile));
						if (arena_status.get(an) == "lobby") {
							signblock.setLine(3, "\u00A72WAITING");
						} else if (arena_status.get(an) == "prepare"
								|| arena_status.get(an) == "arena") {
							signblock.setLine(3, "\u00A74INPROGRESS");
						}
						signblock.update();
					} else {
						CheckFile(signsFile, "signs.yml");
						block.setType(Material.AIR);

						try {
							signsFileC.load(signsFile);
							signsFileC.set(sign, null);
							signsFileC.save(signsFile);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InvalidConfigurationException e) {
							e.printStackTrace();
						}
					}
				} else {
					CheckFile(signsFile, "signs.yml");

					try {
						signsFileC.load(signsFile);
						signsFileC.set(sign, null);
						signsFileC.save(signsFile);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InvalidConfigurationException e) {
						e.printStackTrace();
					}

					block.setType(Material.AIR);
				}
			} else {
				CheckFile(signsFile, "signs.yml");

				try {
					signsFileC.load(signsFile);
					signsFileC.set(sign, null);
					signsFileC.save(signsFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Boolean perms (Player player, String perm, String perm2, String kind) {
		if (player == null) return true;
		if (player.hasPermission("*")) {
			return true;
		} else if (player.hasPermission("seekandfind.*")) {
			return true;
		} else if (player.hasPermission("seekandfind." + perm2 + ".*")) {
			return true;
		} else if (player.hasPermission(perm)) {
			return true;
		} else {
			if (kind == "-1") {
				return false;
			} else if (kind != null) {
				player.sendMessage(pluginTag + Colours(noPerms, true).replaceAll("%kind", kind));
			} else {
				player.sendMessage(pluginTag + Colours(noPermsToDo, true));
			}
			return false;
		}
	}

	public String Colours (String string, boolean enters) {
		string = string.replaceAll("&0", "\u00A70")
				.replaceAll("&1", "\u00A71")
				.replaceAll("&2", "\u00A72")
				.replaceAll("&3", "\u00A73")
				.replaceAll("&4", "\u00A74")
				.replaceAll("&5", "\u00A75")
				.replaceAll("&6", "\u00A76")
				.replaceAll("&7", "\u00A77")
				.replaceAll("&8", "\u00A78")
				.replaceAll("&9", "\u00A79")					
				.replaceAll("&a", "\u00A7a")
				.replaceAll("&b", "\u00A7b")
				.replaceAll("&c", "\u00A7c")
				.replaceAll("&d", "\u00A7d")
				.replaceAll("&e", "\u00A7e")
				.replaceAll("&f", "\u00A7f")
				.replaceAll("&k", "\u00A7k")
				.replaceAll("&l", "\u00A7l")
				.replaceAll("&m", "\u00A7m")
				.replaceAll("&n", "\u00A7n")
				.replaceAll("&o", "\u00A7o")
				.replaceAll("&r", "\u00A7r")
				.replaceAll("%norm", normalcolour)
				.replaceAll("%err", errorcolour)
				.replaceAll("%warn", warningcolour)
				.replaceAll("%arg", argcolour)
				.replaceAll("%header", headercolour);
		if (enters) {
			string = string.replaceAll("&u", "\n");
		}
		return string;
	}

	public Object getFile (String place, String kind, ConfigurationSection cs) {
		if (kind == "String") {
			String result;
			result = cs.getString(place);
			if (result == null) {
				result = unknown;
			}
			return Colours(result, true);
		} else if (kind == "Boolean") {
			boolean result = false;
			result = cs.getBoolean(place);
			return result;

		} else if (kind == "String[]") {
			String decode = null;
			try {
				decode = Colours(cs.getString(place), false);
			} catch (NullPointerException e) {
				if (decode == null) {
					decode = unknown;
				}
			}
			String[] result = decode.split("&u");
			return result;
		} else {
			return kind;
		}
	}

	public Integer getInt (String place, ConfigurationSection cs) {
		int result = 0;
		result = cs.getInt(place);
		return result;
	}

	public void msg (Player player, String msg, String perm, String perm2, String kind, Boolean tag) {
		if (msg == null) msg = unknown;
		if (perm2 == null) perm2 = "admin";
		if (tag == true) msg = pluginTag + msg;
		if (player == null) {
			Bukkit.getConsoleSender().sendMessage(Colours(msg, true));
		} else if (perm == null) {
			player.sendMessage(Colours(msg, true));
		} else if (perms(player, perm, perm2, kind)) {
			player.sendMessage(Colours(msg, true));
		}
	}

	public String header (String header) {
		return (String) getFile("tag.header", "String", CconfigFile).toString().replaceAll("%name", header);
	}

	public void arenaBroadcast (String an, String string) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player_arena.get(player) != null) {
				if (player_arena.get(player).equals(an)) {
					player.sendMessage(pluginTag + normalcolour + string);
				}
			}
		}

	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerIntractEntityEvent (PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			Player player = event.getPlayer();
			Player player2 = (Player) event.getRightClicked();

			if (player_arena.get(player) != null) {
				String an = player_arena.get(player);
				if (player_arena.get(player2) != null) {
					if (arena_it.get(an) != null) {
						if (arena_status.get(an) == "arena") {
							if (arena_it.get(an).equals(player2)) {						
								ArrayList<Player> players = new ArrayList<Player>();
								for (Player playerz : Bukkit.getOnlinePlayers()) {
									if (player_arena.get(playerz) != null) {
										if (player_arena.get(playerz).equals(an)) {
											players.add(playerz);
										}
									}
								}

								arenaBroadcast(an, (String) getFile("winArena", "String", CmessagesFile).toString()
										.replaceAll("%1", player.getDisplayName())
										.replaceAll("%2", arena_it.get(an).getDisplayName()));

								for (Player playerz2 : players) {
									arenaPlayerJoinLeave(playerz2, an, "win");
									if (arena_it.get(an).equals(playerz2)) {
										playerz2.playSound(playerz2.getLocation(), Sound.ENDERMAN_DEATH, 10, (float) 0.1);
									} else {
										playerz2.playSound(playerz2.getLocation(), Sound.LEVEL_UP, 5, 1);
									}
								}

								arena_status.put(an, "lobby");
								arena_timer.put(an, null);
								arena_it.put(an, null);

								Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
								FireworkMeta fwm = fw.getFireworkMeta();

								Type type = Type.BALL_LARGE;
								Color c1 = Color.fromRGB(255, 255, 0);
								FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(c1).with(type).trail(true).build();
								fwm.addEffect(effect);
								fwm.setPower(0);

								fw.setFireworkMeta(fwm);
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerInteractEvent (PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (getInt("wandid", CconfigFile) != null) {
			if (player.getItemInHand().getType() == Material.getMaterial(getInt("wandid", CconfigFile))) {
				if (perms(player, "seekandfind.wand", "admin", "-1")) {
					if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
						Location l = block.getLocation();
						if (pos1.get(player) != null) {
							if (pos1.get(player).equals(l)) {
								event.setCancelled(true);
								return;
							}
						}
						pos1.put(player, l);
						msg(player, getFile("setPos", "String", CmessagesFile).toString()
								.replaceAll("%1", "1")
								.replaceAll("%x", String.valueOf(l.getX()))
								.replaceAll("%y", String.valueOf(l.getY()))
								.replaceAll("%z", String.valueOf(l.getZ())), null, null, null, true);
						event.setCancelled(true);
					}
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						Location l = block.getLocation();
						if (pos2.get(player) != null) {
							if (pos2.get(player).equals(l)) {
								event.setCancelled(true);
								return;
							}
						}
						pos2.put(player, l);
						msg(player, getFile("setPos", "String", CmessagesFile).toString()
								.replaceAll("%1", "2")
								.replaceAll("%x", String.valueOf(l.getX()))
								.replaceAll("%y", String.valueOf(l.getY()))
								.replaceAll("%z", String.valueOf(l.getZ())), null, null, null, true);
						event.setCancelled(true);
					}
				}
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (block.getType() != Material.AIR) {
				if (block.getType() == Material.SIGN_POST
						|| block.getType() == Material.WALL_SIGN) {
					Sign sign = (Sign) block.getState();

					if (sign.getLine(0) == "\u00A79[\u00A7eS&F\u00A79]") {
						if (perms(player, "seekandfind.signjoin", "player", null)) {
							if (sign.getLine(1) == "\u00A74LEAVE") {
								arenaPlayerJoinLeave(player, null, "leave");
							} else {
								arenaPlayerJoinLeave(player, sign.getLine(1).replaceAll("\u00A78", ""), "join");
							}
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerQuitEvent (PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player_arena.get(player) != null) {
			arenaPlayerJoinLeave(player, player_arena.get(player), "forceleave");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void BlockBreakEvent (BlockBreakEvent event) {
		if (player_arena.get(event.getPlayer()) != null) {
			event.setCancelled(true);
		}

		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (block.getType() == Material.SIGN_POST
				|| block.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) block.getState();

			if (sign.getLine(0) == "\u00A79[\u00A7eS&F\u00A79]") {
				if (perms(player, "seekandfind.signbreak", "admin", null)) {
					return;
				} else {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void PlayerMoveEvent (PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player_arena.get(player) != null) {
			if (arena_status.get(player_arena.get(player)) == "arena") {
				String an = player_arena.get(player);
				Location locf = event.getFrom();
				Location loct = event.getTo();

				int maxX = Math.max(getInt(an + ".pos1.x", CarenasFile), getInt(an + ".pos2.x", CarenasFile));
				int minX = Math.min(getInt(an + ".pos1.x", CarenasFile), getInt(an + ".pos2.x", CarenasFile));
				int maxY = Math.max(getInt(an + ".pos1.y", CarenasFile), getInt(an + ".pos2.y", CarenasFile));
				int minY = Math.min(getInt(an + ".pos1.y", CarenasFile), getInt(an + ".pos2.y", CarenasFile));
				int maxZ = Math.max(getInt(an + ".pos1.z", CarenasFile), getInt(an + ".pos2.z", CarenasFile));
				int minZ = Math.min(getInt(an + ".pos1.z", CarenasFile), getInt(an + ".pos2.z", CarenasFile));

				if (loct.getX() < minX
						|| loct.getX() > maxX
						|| loct.getY() > maxY
						|| loct.getZ() < minZ
						|| loct.getZ() > maxZ) {
					player.teleport(locf);
					player.playEffect(loct.add(0, 1, 0), Effect.ENDER_SIGNAL, 0);
					player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
				}

				if (loct.getY() < minY) {
					player.teleport(locf.add(0, 2, 0));
					player.playEffect(loct, Effect.ENDER_SIGNAL, 0);
					player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
				}
			} else if (arena_status.get(player_arena.get(player)) == "prepare") {
				if (arena_it.get(player_arena.get(player)).equals(player)) {
					String an = player_arena.get(player);
					Location locf = event.getFrom();
					Location loct = event.getTo();

					int maxX = Math.max(getInt(an + ".pos1.x", CarenasFile), getInt(an + ".pos2.x", CarenasFile));
					int minX = Math.min(getInt(an + ".pos1.x", CarenasFile), getInt(an + ".pos2.x", CarenasFile));
					int maxY = Math.max(getInt(an + ".pos1.y", CarenasFile), getInt(an + ".pos2.y", CarenasFile));
					int minY = Math.min(getInt(an + ".pos1.y", CarenasFile), getInt(an + ".pos2.y", CarenasFile));
					int maxZ = Math.max(getInt(an + ".pos1.z", CarenasFile), getInt(an + ".pos2.z", CarenasFile));
					int minZ = Math.min(getInt(an + ".pos1.z", CarenasFile), getInt(an + ".pos2.z", CarenasFile));

					if (loct.getX() < minX
							|| loct.getX() > maxX
							|| loct.getY() > maxY
							|| loct.getZ() < minZ
							|| loct.getZ() > maxZ) {
						player.teleport(locf);
						player.playEffect(loct.add(0, 1, 0), Effect.ENDER_SIGNAL, 0);
						player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
					}

					if (loct.getY() < minY) {
						player.teleport(locf.add(0, 2, 0));
						player.playEffect(loct, Effect.ENDER_SIGNAL, 0);
						player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerTeleportEvent (PlayerTeleportEvent event) {
		if (player_arena.get(event.getPlayer()) != null) {
			if (arena_status.get(player_arena.get(event.getPlayer())).equals("arena")) {
				event.setTo(event.getFrom());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerCommandPreprocessEvent (PlayerCommandPreprocessEvent event) {
		String m = event.getMessage();
		if (player_arena.get(event.getPlayer()) != null) {
			if (m.startsWith("/seekandfind")
					|| m.startsWith("/seekandfind")
					|| m.startsWith("/saf")
					|| m.startsWith("/sf")
					|| m.startsWith("/hideandseek")
					|| m.startsWith("/has")
					|| m.startsWith("/hs")
					|| m.startsWith("/h")) {
				return;
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void BlockPlaceEvent (BlockPlaceEvent event) {
		if (player_arena.get(event.getPlayer()) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void InventoryClickEvent (InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (player_arena.get(player) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void EntityDamageEvent (EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (player_arena.get(player) != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void EntityDamageByEntityEvent (EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player player = (Player) event.getEntity();
			Player damager = (Player) event.getDamager();
			if (player_arena.get(player) != null) {
				if (player_arena.get(damager) != null) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void SignChangeEvent (SignChangeEvent event) {
		Player player = event.getPlayer();
		String an = event.getLine(1);
		Location loc = event.getBlock().getLocation();

		if (event.getLine(0).equals("[SF]")) {
			if (perms(player, "seekandfind.signcreate", "admin", null)) {
				if (an.equals("leave")) {
					CheckFile(signsFile, "signs.yml");

					try {
						signsFileC.load(signsFile);
						signsFileC.set(loc.getWorld().getName() + "@"
								+ Math.round(loc.getX()) + "@"
								+ Math.round(loc.getY()) + "@"
								+ Math.round(loc.getZ()), an);
						signsFileC.save(signsFile);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InvalidConfigurationException e) {
						e.printStackTrace();
					}

					CheckSigns();
				} else if (arena_status.get(an) != null) {
					CheckFile(signsFile, "signs.yml");

					try {
						signsFileC.load(signsFile);
						signsFileC.set(loc.getWorld().getName() + "@"
								+ Math.round(loc.getX()) + "@"
								+ Math.round(loc.getY()) + "@"
								+ Math.round(loc.getZ()), an);
						signsFileC.save(signsFile);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InvalidConfigurationException e) {
						e.printStackTrace();
					}
					
					CheckSigns();
				} else {
					msg (player, (String) getFile("notanArena", "String", CmessagesFile), null, null, null, true);
				}
			}
		}
	}

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
		PluginDescriptionFile pdfFile = this.getDescription();
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("seekandfind")) {
			if (args.length == 0) {
				msg (player, header(pdfFile.getName()) + "\n"
						+ normalcolour + "Made by " + argcolour + "Steffion" + normalcolour + ".\n"
						+ normalcolour + "Version: " + argcolour + pdfFile.getVersion() + normalcolour + ".\n"
						+ normalcolour + "Use " + argcolour + "/seekandfind [info/i] " + normalcolour + "for more info.\n"
						+ normalcolour + "Use " + argcolour + "/seekandfind <help/h> [page number] " + normalcolour + "for a list of commands.\n"
						+ header(pdfFile.getName()), "seekandfind.info", "player", "seekandfind [info/i]", false);
			} else if (args.length == 1) {
				if (args[0].equals("info") || args[0].equals("i")) {
					msg (player, header(pdfFile.getName()) + "\n"
							+ normalcolour + "Version: " + argcolour + pdfFile.getVersion() + normalcolour + ".\n"
							+ normalcolour + "Thanks for Playing! Please report bugs :)!\n"
							+ argcolour + "http://dev.bukkit.org/server-mods/seekandfind/ \n"
							+ normalcolour + "Developer: " + argcolour + "Steffion" + normalcolour + ".\n"
							+ normalcolour + "Helped by: " + argcolour + "BlueNinjar" + normalcolour + ".\n"
							+ normalcolour + "Use " + argcolour + "/seekandfind <help/h> [page number] " + normalcolour + "for a list of commands.\n"
							+ header(pdfFile.getName()), "seekandfind.info", "player", "seekandfind [info/i]", false);
				} else if (args[0].equals("help") || args[0].equals("h")) {
					String help = header(pdfFile.getName()) + "\n";
					//
					if (perms(player, "seekandfind.info", "player", "-1")) {
						help = help + argcolour + "/seekandfind [info/i]" + normalcolour + ": Shows info about this plugin.\n";
					} else {
						help = help + errorcolour + "/seekandfind [info/i]" + normalcolour + ": Shows info about this plugin.\n";
					}
					//
					if (perms(player, "seekandfind.help", "player", "-1")) {
						help = help + argcolour + "/seekandfind <help/h> [page number]" + normalcolour + ": Shows all commands.\n";
					} else {
						help = help + errorcolour + "/seekandfind <help/h> [page number]" + normalcolour + ": Shows all commands.\n";
					}
					//
					if (perms(player, "seekandfind.join", "player", "-1")) {
						help = help + argcolour + "/seekandfind <join/j> <arenaname>" + normalcolour + ": Join an arena.\n";
					} else {
						help = help + errorcolour + "/seekandfind <join/j> <arenaname>" + normalcolour + ": Join an arena.\n";
					}
					//
					if (perms(player, "seekandfind.leave", "player", "-1")) {
						help = help + argcolour + "/seekandfind <leave/l>" + normalcolour + ": Leave an arena.\n";
					} else {
						help = help + errorcolour + "/seekandfind <leave/l>" + normalcolour + ": Leave an arena.\n";
					}

					help = help + "\n\n\n\n";

					help = help + header(pdfFile.getName());
					msg (player, help, "seekandfind.help", "player", "seekandfind <help/h> [page number]", false);

				} else if (args[0].equals("join") || args[0].equals("j")) {
					if (perms(player, "seekandfind.join", "player", "seekandfind <join/j>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						msg (player, (String) getFile("notenoughArgs", "String", CmessagesFile), null, null, null, true);
					}
				} else if (args[0].equals("leave") || args[0].equals("l")) {
					if (perms(player, "seekandfind.leave", "player", "seekandfind <leave/l>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						arenaPlayerJoinLeave(player, null, "leave");
					}
				} else if (args[0].equals("wand") || args[0].equals("w")) {
					if (perms(player, "seekandfind.wand", "admin", "seekandfind <wand/w>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}
						if (getInt("wandid", CconfigFile) != null) {
							Material matwand = Material.getMaterial(getInt("wandid", CconfigFile));
							ItemStack wand = new ItemStack(matwand, 1);
							player.getInventory().addItem(wand);
							player.playSound(player.getLocation(), Sound.ORB_PICKUP, 5, 0);
							msg (player, (String) getFile("wandGiven", "String", CmessagesFile).toString()
									.replaceAll("%1", Material.getMaterial(getInt("wandid", CconfigFile))
											.toString().toLowerCase().replaceAll("_", " ")),
											"seekandfind.wand", "admin", "seekandfind <wand/w>", true);
						}
					}
				} else if (args[0].equals("create") || args[0].equals("c")) {
					if (perms(player, "seekandfind.create", "admin", "seekandfind <create/c>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						msg (player, (String) getFile("notenoughArgs", "String", CmessagesFile), null, null, null, true);
					}
				} else if (args[0].equals("setwarp") || args[0].equals("sw")) {
					if (perms(player, "seekandfind.setwarp", "admin", "seekandfind <setwarp/sw> <warpname> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						msg (player, (String) getFile("notenoughArgs", "String", CmessagesFile), null, null, null, true);
					}
				} else if (args[0].equals("set") || args[0].equals("s")) {
					if (perms(player, "seekandfind.set", "admin", "seekandfind <set/s> <argument> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						msg (player, (String) getFile("notenoughArgs", "String", CmessagesFile), null, null, null, true);
					}
				} else {
					msg (player, Colours(noCommand, true), null, null, null, true);
				}
			} else if (args.length == 2) {
				if (args[0].equals("help") || args[0].equals("h")) {
					String help = header(pdfFile.getName()) + "\n";
					if (args[1].equals("1")) {
						//
						if (perms(player, "seekandfind.info", "player", "-1")) {
							help = help + argcolour + "/seekandfind [info/i]" + normalcolour + ": Shows info about this plugin.\n";
						} else {
							help = help + errorcolour + "/seekandfind [info/i]" + normalcolour + ": Shows info about this plugin.\n";
						}
						//
						if (perms(player, "seekandfind.help", "player", "-1")) {
							help = help + argcolour + "/seekandfind <help/h> [page number]" + normalcolour + ": Shows all commands.\n";
						} else {
							help = help + errorcolour + "/seekandfind <help/h> [page number]" + normalcolour + ": Shows all commands.\n";
						}
						//
						if (perms(player, "seekandfind.join", "player", "-1")) {
							help = help + argcolour + "/seekandfind <join/j> <arenaname>" + normalcolour + ": Join an arena.\n";
						} else {
							help = help + errorcolour + "/seekandfind <join/j> <arenaname>" + normalcolour + ": Join an arena.\n";
						}
						//
						if (perms(player, "seekandfind.leave", "player", "-1")) {
							help = help + argcolour + "/seekandfind <leave/l>" + normalcolour + ": Leave an arena.\n";
						} else {
							help = help + errorcolour + "/seekandfind <leave/l>" + normalcolour + ": Leave an arena.\n";
						}

						help = help + "\n\n\n\n";
					} else if (args[1].equals("2")) {
						//
						if (perms(player, "seekandfind.wand", "admin", "-1")) {
							help = help + argcolour + "/seekandfind <wand/w>" + normalcolour + ": Gives you the item wand, to select a new arena.\n";
						} else {
							help = help + errorcolour + "/seekandfind <wand/w>" + normalcolour + ": Gives you the item wand, to select a new arena.\n";
						}
						//
						if (perms(player, "seekandfind.create", "admin", "-1")) {
							help = help + argcolour + "/seekandfind <create/c> <arenaname>" + normalcolour + ": Creates an arena from the selected field.\n";
						} else {
							help = help + errorcolour + "/seekandfind <create/c> <arenaname>" + normalcolour + ": Creates an arena from the selected field.\n";
						}
						//
						if (perms(player, "seekandfind.setwarp", "admin", "-1")) {
							help = help + argcolour + "/seekandfind <setwarp/sw> <warpname> <arenaname>" + normalcolour + ": Set a warp of an arena.\n";
						} else {
							help = help + errorcolour + "/seekandfind <setwarp/sw> <warpname> <arenaname>" + normalcolour + ": Set a warp of an arena.\n";
						}
						//
						if (perms(player, "seekandfind.set", "admin", "-1")) {
							help = help + argcolour + "/seekandfind <set/s> <setting> <argument> <arenaname>" + normalcolour + ": Set a setting of an arena.\n";
						} else {
							help = help + errorcolour + "/seekandfind <set/s> <setting> <argument> <arenaname>" + normalcolour + ": Set a setting of an arena.\n";
						}
					} else {
						return true;
					}

					help = help + header(pdfFile.getName());
					msg (player, help, "seekandfind.help", "player", "seekandfind <help/h> [page number]", false);
				} else if (args[0].equals("join") || args[0].equals("j")) {
					if (perms(player, "seekandfind.join", "player", "seekandfind <join/j>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						String an = args[1].toString();
						arenaPlayerJoinLeave(player, an, "join");
					}
				} else if (args[0].equals("create") || args[0].equals("c")) {
					if (perms(player, "seekandfind.create", "admin", "seekandfind <create/c> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}
						if (pos1.get(player) != null && pos2.get(player) != null) {
							if (pos1.get(player).getWorld() == pos2.get(player).getWorld()) {
								if (getFile(args[1].toString() + ".name", "String", arenasFileC) == unknown) {
									if (args[1].length() < 13) {
										CheckFile(arenasFile, "arenas.yml");
										String an = args[1].toString();
										Location p1 = pos1.get(player);
										Location p2 = pos2.get(player);

										try {
											arenasFileC.load(arenasFile);
											arenasFileC.set(an + ".name", an);
											arenasFileC.set(an + ".pos1.world", p1.getWorld().getName());
											arenasFileC.set(an + ".pos1.x", p1.getX());
											arenasFileC.set(an + ".pos1.y", p1.getY());
											arenasFileC.set(an + ".pos1.z", p1.getZ());
											arenasFileC.set(an + ".pos2.world", p2.getWorld().getName());
											arenasFileC.set(an + ".pos2.x", p2.getX());
											arenasFileC.set(an + ".pos2.y", p2.getY());
											arenasFileC.set(an + ".pos2.z", p2.getZ());
											arenasFileC.save(arenasFile);
											msg (player, (String) getFile("createdArena", "String", CmessagesFile)
													.toString().replaceAll("%1", an), null, null, null, true);
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										} catch (InvalidConfigurationException e) {
											e.printStackTrace();
										}
									} else {
										msg (player, (String) getFile("tooLong", "String", CmessagesFile), null, null, null, true);
									}
								} else {
									msg (player, (String) getFile("alreadyArena", "String", CmessagesFile), null, null, null, true);
								}
							} else {
								msg (player, (String) getFile("notsameWorld", "String", CmessagesFile), null, null, null, true);
							}
						} else {
							msg (player, (String) getFile("missingPos", "String", CmessagesFile), null, null, null, true);
						}
					}
				} else if (args[0].equals("setwarp") || args[0].equals("sw")) {
					if (perms(player, "seekandfind.setwarp", "admin", "seekandfind <setwarp/sw> <warpname> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						msg (player, (String) getFile("notenoughArgs", "String", CmessagesFile), null, null, null, true);
					}
				} else if (args[0].equals("set") || args[0].equals("s")) {
					if (perms(player, "seekandfind.set", "admin", "seekandfind <set/s> <setting> <argument> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						msg (player, (String) getFile("notenoughArgs", "String", CmessagesFile), null, null, null, true);
					}
				} else {
					msg (player, Colours(noCommand, true), null, null, null, true);
				}
			} else if (args.length == 3) {
				if (args[0].equals("setwarp") || args[0].equals("sw")) {
					if (perms(player, "seekandfind.setwarp", "admin", "seekandfind <setwarp/sw> <warpname> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}
						if (getFile(args[2].toString() + ".name", "String", arenasFileC) != unknown) {
							if (args[1].equals("lobby")) {
								CheckFile(arenasFile, "arenas.yml");
								String an = args[2].toString();
								Location loc = player.getLocation();

								try {
									arenasFileC.load(arenasFile);
									arenasFileC.set(an + ".name", an);
									arenasFileC.set(an + ".lobby.world", loc.getWorld().getName());
									arenasFileC.set(an + ".lobby.x", loc.getX());
									arenasFileC.set(an + ".lobby.y", loc.getY());
									arenasFileC.set(an + ".lobby.z", loc.getZ());
									arenasFileC.set(an + ".lobby.yaw", loc.getYaw());
									arenasFileC.set(an + ".lobby.pitch", loc.getPitch());
									arenasFileC.save(arenasFile);
									msg (player, (String) getFile("createdWarp", "String", CmessagesFile)
											.toString().replaceAll("%1", an)
											.replaceAll("%2", args[1].toString()), null, null, null, true);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} catch (InvalidConfigurationException e) {
									e.printStackTrace();
								}
							} else if (args[1].equals("arena")) {
								CheckFile(arenasFile, "arenas.yml");
								String an = args[2].toString();
								Location loc = player.getLocation();

								try {
									arenasFileC.load(arenasFile);
									arenasFileC.set(an + ".name", an);
									arenasFileC.set(an + ".arena.world", loc.getWorld().getName());
									arenasFileC.set(an + ".arena.x", loc.getX());
									arenasFileC.set(an + ".arena.y", loc.getY());
									arenasFileC.set(an + ".arena.z", loc.getZ());
									arenasFileC.set(an + ".arena.yaw", loc.getYaw());
									arenasFileC.set(an + ".arena.pitch", loc.getPitch());
									arenasFileC.save(arenasFile);
									msg (player, (String) getFile("createdWarp", "String", CmessagesFile)
											.toString().replaceAll("%1", an)
											.replaceAll("%2", args[1].toString()), null, null, null, true);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} catch (InvalidConfigurationException e) {
									e.printStackTrace();
								}
							} else {
								msg (player, (String) getFile("unknownWarp", "String", CmessagesFile), null, null, null, true);
							}
						} else {
							msg (player, (String) getFile("notanArena", "String", CmessagesFile), null, null, null, true);
						}
					}
				} else if (args[0].equals("set") || args[0].equals("s")) {
					if (perms(player, "seekandfind.set", "admin", "seekandfind <set/s> <setting> <argument> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}

						msg (player, (String) getFile("notenoughArgs", "String", CmessagesFile), null, null, null, true);
					}
				} else {
					msg (player, Colours(noCommand, true), null, null, null, true);
				}
			} else if (args.length == 4) {
				if (args[0].equals("set") || args[0].equals("s")) {
					if (perms(player, "seekandfind.set", "admin", "seekandfind <set/s> <setting> <argument> <arenaname>")) {
						if (player == null) {
							msg (player, (String) getFile("ingameOnly", "String", CmessagesFile), null, null, null, true);
							return true;
						}
						if (getFile(args[3].toString() + ".name", "String", arenasFileC) != unknown) {
							if (args[1].equals("players")) {
								int players = 0;
								try {
									players = Integer.parseInt(args[2]);
								} catch (NumberFormatException e) {
									msg (player, (String) getFile("notaNumber", "String", CmessagesFile), null, null, null, true);
									return true;
								}

								CheckFile(arenasFile, "arenas.yml");
								String an = args[3].toString();

								try {
									arenasFileC.load(arenasFile);
									arenasFileC.set(an + ".players", players);
									arenasFileC.save(arenasFile);
									msg (player, (String) getFile("setPlayers", "String", CmessagesFile)
											.toString().replaceAll("%1", an)
											.replaceAll("%2", String.valueOf(players)), null, null, null, true);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} catch (InvalidConfigurationException e) {
									e.printStackTrace();
								}
							} else if (args[1].equals("minplayers")) {
								int players = 0;
								try {
									players = Integer.parseInt(args[2]);
								} catch (NumberFormatException e) {
									msg (player, (String) getFile("notaNumber", "String", CmessagesFile), null, null, null, true);
									return true;
								}

								if (players > 1) {

									CheckFile(arenasFile, "arenas.yml");
									String an = args[3].toString();

									try {
										arenasFileC.load(arenasFile);
										arenasFileC.set(an + ".minplayers", players);
										arenasFileC.save(arenasFile);
										msg (player, (String) getFile("setMinPlayers", "String", CmessagesFile)
												.toString().replaceAll("%1", an)
												.replaceAll("%2", String.valueOf(players)), null, null, null, true);
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									} catch (InvalidConfigurationException e) {
										e.printStackTrace();
									}
								} else {
									msg (player, (String) getFile("notenoughPlayers", "String", CmessagesFile), null, null, null, true);
								}
							} else {
								msg (player, (String) getFile("unknownSetting", "String", CmessagesFile), null, null, null, true);
							}
						} else {
							msg (player, (String) getFile("notanArena", "String", CmessagesFile), null, null, null, true);
						}
					}
				} else {
					msg (player, Colours(noCommand, true), null, null, null, true);
				}
			} else {
				msg (player, Colours(noCommand, true), null, null, null, true);
			}
		}
		return true;
	}

	public void arenaPlayerJoinLeave(final Player player, String an, final String type) {
		if (type == "join") {
			if (player_arena.get(player) == null) {
				if (getFile(an + ".name", "String", arenasFileC) != unknown) {
					if (getFile(an + ".name", "String", arenasFileC) != unknown
							&& getFile(an + ".pos1.world", "String", arenasFileC) != unknown
							&& getFile(an + ".pos2.world", "String", arenasFileC) != unknown
							&& getFile(an + ".lobby.world", "String", arenasFileC) != unknown
							&& getFile(an + ".arena.world", "String", arenasFileC) != unknown
							&& getInt(an + ".players", arenasFileC) != 0
							&& getInt(an + ".minplayers", arenasFileC) != 0) {
						if (arena_status.get(an) == "lobby") {
							if (arena_players.get(an) < getInt(an + ".players", arenasFileC)) {
								final String an2 = an;
								getServer().getScheduler().runTaskLater(this, new Runnable() {
									@Override
									public void run() {
										player_locbefore.put(player, player.getLocation());
										player_arena.put(player, an2);
										arena_players.put(an2, arena_players.get(an2) + 1);
										player_inv.put(player, player.getInventory().getContents());
										player_inva.put(player, player.getInventory().getArmorContents());
										player_gm.put(player, player.getGameMode());

										player.getInventory().clear();
										player.getInventory().setHelmet(new ItemStack (Material.AIR));
										player.getInventory().setChestplate(new ItemStack (Material.AIR));
										player.getInventory().setLeggings(new ItemStack (Material.AIR));
										player.getInventory().setBoots(new ItemStack (Material.AIR));
										player.setGameMode(GameMode.SURVIVAL);

										Location loc = new Location (Bukkit.getWorld((String) getFile(an2 + ".lobby.world", "String", arenasFileC)),
												getInt(an2 + ".lobby.x", arenasFileC),
												getInt(an2 + ".lobby.y", arenasFileC),
												getInt(an2 + ".lobby.z", arenasFileC),
												getInt(an2 + ".lobby.yaw", arenasFileC),
												getInt(an2 + ".lobby.pitch", arenasFileC));
										player.teleport(loc);

										arenaBroadcast(an2, (String) getFile("playerJoined", "String", CmessagesFile).toString()
												.replaceAll("%player", player.getDisplayName())
												.replaceAll("%1", arena_players.get(an2).toString())
												.replaceAll("%2", getInt(an2 + ".players", arenasFileC).toString()));
										if (arena_players.get(an2) < getInt(an2 + ".minplayers", arenasFileC)) {
											arenaBroadcast(an2, (String) getFile("needAtleast", "String", CmessagesFile).toString()
													.replaceAll("%1", getInt(an2 + ".minplayers", arenasFileC).toString()));
										} else {
											if (arena_timer.get(an2) == null) {
												arena_timer.put(an2, getInt("timer.lobbycountdown", CconfigFile));
												arenaBroadcast (an2, getFile("beforearenaStarting", "String", CmessagesFile)
														.toString()
														.replaceAll("%1", getInt("timer.lobbycountdown", CconfigFile).toString()));
											}
										}
									}
								}, 1);
							} else {
								msg (player, (String) getFile("arenaFull", "String", CmessagesFile), null, null, null, true);
							}
						} else {
							msg (player, (String) getFile("arenaAlreadyinProgress", "String", CmessagesFile), null, null, null, true);
						}
					} else {
						msg (player, (String) getFile("arenaNotReady", "String", CmessagesFile), null, null, null, true);
					}
				} else {
					msg (player, (String) getFile("notanArena", "String", CmessagesFile), null, null, null, true);
				}
			} else {
				msg (player, (String) getFile("alreadyJoined", "String", CmessagesFile), null, null, null, true);
			}
		} else if (type == "leave") {
			if (player_arena.get(player) != null) {
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						String an2 = player_arena.get(player);
						arena_players.put(an2, arena_players.get(an2) - 1);
						arenaBroadcast(an2, (String) getFile("playerLeft", "String", CmessagesFile).toString()
								.replaceAll("%player", player.getDisplayName())
								.replaceAll("%1", arena_players.get(an2).toString())
								.replaceAll("%2", getInt(an2 + ".players", arenasFileC).toString()));
						player.teleport(player_locbefore.get(player));

						player.getInventory().setContents(player_inv.get(player));
						player.getInventory().setArmorContents(player_inva.get(player));
						player.setGameMode(player_gm.get(player));
						player.addPotionEffect(new PotionEffect (PotionEffectType.INVISIBILITY, 1, 1), true);
						
						player_arena.put(player, null);
						player_locbefore.put(player, null);
						player_inv.put(player, null);
						player_inva.put(player, null);
						player_gm.put(player, null);

						if (arena_status.get(an2) == "lobby") {
							if (arena_players.get(an2) < getInt(an2 + ".minplayers", arenasFileC)) {
								arenaBroadcast(an2, (String) getFile("needAtleast", "String", CmessagesFile).toString()
										.replaceAll("%1", getInt(an2 + ".minplayers", arenasFileC).toString()));
								arena_timer.put(an2, null);
							}
						} else {
							if (arena_it.get(an2) != null) {
								if (arena_it.get(an2).equals(player)) {
									arenaBroadcast(an2, (String) getFile("itLeft", "String", CmessagesFile).toString()
											.replaceAll("%1", arena_it.get(an2).getDisplayName()));
									ArrayList<Player> players = new ArrayList<Player>();
									for (Player player : Bukkit.getOnlinePlayers()) {
										if (player_arena.get(player) != null) {
											if (player_arena.get(player).equals(an2)) {
												players.add(player);
											}
										}
									}

									for (Player playerz : players) {
										if (playerz != player) {
											arenaPlayerJoinLeave(playerz, an2, "itleft");
										}
									}

									arena_status.put(an2, "lobby");
									arena_timer.put(an2, null);
									arena_it.put(an2, null);
								}
							}
						}
					}
				}, 1);
			} else {
				msg (player, (String) getFile("notinArena", "String", CmessagesFile), null, null, null, true);
			}
		} else if (type == "overtime") {
			if (player_arena.get(player) != null) {
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						String an2 = player_arena.get(player);
						player_arena.put(player, null);
						arena_players.put(an2, arena_players.get(an2) - 1);
						player.teleport(player_locbefore.get(player));

						player.getInventory().setContents(player_inv.get(player));
						player.getInventory().setArmorContents(player_inva.get(player));
						player.setGameMode(player_gm.get(player));
						player.addPotionEffect(new PotionEffect (PotionEffectType.INVISIBILITY, 1, 1), true);

						player_locbefore.put(player, null);
						player_inv.put(player, null);
						player_inva.put(player, null);
						player_gm.put(player, null);
					}
				}, 90);
			} else {
				msg (player, (String) getFile("notinArena", "String", CmessagesFile), null, null, null, true);
			}
		} else if (type == "win") {
			if (player_arena.get(player) != null) {
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						String an2 = player_arena.get(player);
						player_arena.put(player, null);
						arena_players.put(an2, arena_players.get(an2) - 1);
						player.teleport(player_locbefore.get(player));

						player.getInventory().setContents(player_inv.get(player));
						player.getInventory().setArmorContents(player_inva.get(player));
						player.setGameMode(player_gm.get(player));
						player.addPotionEffect(new PotionEffect (PotionEffectType.INVISIBILITY, 1, 1), true);

						player_locbefore.put(player, null);
						player_inv.put(player, null);
						player_inva.put(player, null);
						player_gm.put(player, null);
					}
				}, 90);
			} else {
				msg (player, (String) getFile("notinArena", "String", CmessagesFile), null, null, null, true);
			}
		} else if (type == "itleft") {
			if (player_arena.get(player) != null) {
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						String an2 = player_arena.get(player);
						player_arena.put(player, null);
						arena_players.put(an2, arena_players.get(an2) - 1);
						player.teleport(player_locbefore.get(player));

						player.getInventory().setContents(player_inv.get(player));
						player.getInventory().setArmorContents(player_inva.get(player));
						player.setGameMode(player_gm.get(player));
						player.addPotionEffect(new PotionEffect (PotionEffectType.INVISIBILITY, 1, 1), true);

						player_locbefore.put(player, null);
						player_inv.put(player, null);
						player_inva.put(player, null);
						player_gm.put(player, null);
					}
				}, 1);
			} else {
				msg (player, (String) getFile("notinArena", "String", CmessagesFile), null, null, null, true);
			}
		} else if (type == "forceleave") {
			if (player_arena.get(player) != null) {
				String an2 = player_arena.get(player);
				player_arena.put(player, null);
				arena_players.put(an2, arena_players.get(an2) - 1);
				arenaBroadcast(an2, (String) getFile("playerLeft", "String", CmessagesFile).toString()
						.replaceAll("%player", player.getDisplayName())
						.replaceAll("%1", arena_players.get(an2).toString())
						.replaceAll("%2", getInt(an2 + ".players", arenasFileC).toString()));
				player.teleport(player_locbefore.get(player));

				player.getInventory().setContents(player_inv.get(player));
				player.getInventory().setArmorContents(player_inva.get(player));
				player.setGameMode(player_gm.get(player));
				player.addPotionEffect(new PotionEffect (PotionEffectType.INVISIBILITY, 1, 1), true);

				player_locbefore.put(player, null);
				player_inv.put(player, null);
				player_inva.put(player, null);
				player_gm.put(player, null);

				if (arena_status.get(an2) == "lobby") {
					if (arena_players.get(an2) < getInt(an2 + ".minplayers", arenasFileC)) {
						arenaBroadcast(an2, (String) getFile("needAtleast", "String", CmessagesFile).toString()
								.replaceAll("%1", getInt(an2 + ".minplayers", arenasFileC).toString()));
						arena_timer.put(an2, null);
					}
				} else {
					if (arena_it.get(an2) != null) {
						if (arena_it.get(an2).equals(player)) {
							arenaBroadcast(an2, (String) getFile("itLeft", "String", CmessagesFile).toString()
									.replaceAll("%1", arena_it.get(an2).getDisplayName()));
							for (Player playerz : Bukkit.getOnlinePlayers()) {
								if (playerz != player) {
									arenaPlayerJoinLeave(playerz, an2, "itleft");
								}
							}
						}
					}
				}
			} else {
				msg (player, (String) getFile("notinArena", "String", CmessagesFile), null, null, null, true);
			}
		}
	}
}

