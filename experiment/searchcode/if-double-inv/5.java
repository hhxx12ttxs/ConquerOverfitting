package com.jordair.gmail.zombiesurvival;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_6_R1.EntityHuman;
import net.minecraft.server.v1_6_R1.EntityLiving;
import net.minecraft.server.v1_6_R1.EntityWolf;
import net.minecraft.server.v1_6_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_6_R1.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftWolf;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.jordair.gmail.zombiesurvival.api.GameFinishEvent;
import com.jordair.gmail.zombiesurvival.api.GameFinishReason;
import com.jordair.gmail.zombiesurvival.api.GameStartEvent;
import com.jordair.gmail.zombiesurvival.api.WaveChangeEvent;
import com.jordair.gmail.zombiesurvival.events.*;

public class ZombieSurvivalPlugin extends JavaPlugin implements Runnable {

	private static ZombieSurvivalPlugin instance;
	private Random random = new Random();
	private Spawns spawn = new Spawns(this);
	private Awards awards = new Awards(this);
	private Barricades bar = new Barricades();
	private Language lang = new Language(this);
	private Perk perk = new Perk();
	private Potions pot = new Potions(this);
	private SmartGames sg = new SmartGames();
	private Doors door = new Doors(this);
	private Revive revive = new Revive();
	private Classes classes = new Classes();
	private Stats stats = new Stats();
	private Map<String, Integer> secondkills = new HashMap<String, Integer>();
	private int onlinep = 0;
	private Map<String, Integer> justleftgame = new HashMap<String, Integer>();
	private Map<String, Location> playerloconjoin = new HashMap<String, Location>();
	private Map<String, Boolean> wolfwave = new HashMap<String, Boolean>();

	private Map<String, Integer> easycreate = new HashMap<String, Integer>();
	private Map<String, String> ecname = new HashMap<String, String>();
	private Map<String, Integer> ecpcount = new HashMap<String, Integer>();
	private Map<String, Integer> ecwcount = new HashMap<String, Integer>();
	private Map<String, Double> eczcount = new HashMap<String, Double>();
	private Map<String, Door> playerdoorlink = new HashMap<String, Door>();

	private List<String> twomintimer = new ArrayList<String>();
	private int startpcount;
	private Map<String, Integer> commandwave = new HashMap<String, Integer>();
	private Map<String, String> commandMap = new HashMap<String, String>();
	private Map<String, Integer> perkcount = new HashMap<String, Integer>();
	private boolean outofdate = false;
	private boolean antigrief = false;
	private boolean itemsatjoin = false;
	private boolean infectmat = false;
	private boolean spectateallow = false;
	private boolean perpnight = false;
	private boolean emptyaccount = true;
	private boolean forcespawn = false;
	private boolean respawn = true;
	private boolean allhurt = true;
	private boolean forceclear = false;
	private boolean invsave = true;
	private double seekspeed = 0.23D;
	private double fastseekspeed = 0.4D;
	private boolean wolfs = true;
	private boolean healnewwave = true;
	private boolean resetpointsdeath = true;
	private boolean jm = true;
	private boolean smartw = true;
	private String joinmessage = "Welcome to Zombie Survival.";
	private List<String> joincommand = new ArrayList<String>();
	private List<String> leavecommand = new ArrayList<String>();
	private int cooldown = 120;
	private int vspoke = 0;
	private int runnerchance = 10;
	private int skellywavechance = 0;
	private int wait = 20;
	private int doorfindradius = 6;
	private int leavetimer = 120;
	private int maxpoints = 20;
	private double deathloss = 0.0D;
	private double diffmulti = 0.1D;
	private double damagemulti = 1.0D;
	private Map<String, Integer> cooldowncount = new HashMap<String, Integer>();
	private Map<String, Location> lightloc = new HashMap<String, Location>(10);
	private List<ItemStack> joinitems = new ArrayList<ItemStack>(5);
	private List<ItemStack> joinarmor = new ArrayList<ItemStack>(5);
	private Map<ItemStack, Double> drops = new HashMap<ItemStack, Double>(5);
	private List<ItemStack> boxitems = new ArrayList<ItemStack>(10);
	private Map<Location, String> roundFire = new HashMap<Location, String>();
	private Map<String, Map<Block, BlockState>> changedBlocks = new HashMap<String, Map<Block, BlockState>>();
	private Map<String, Map<Block, BlockState>> placedBlocks = new HashMap<String, Map<Block, BlockState>>();
	private List<Integer> blockbreak = new ArrayList<Integer>();
	private List<Integer> blockplace = new ArrayList<Integer>();
	private Map<Location, String> specialblocks = new HashMap<Location, String>();
	private Map<String, ItemStack[]> inv = new HashMap<String, ItemStack[]>();
	private Map<String, ItemStack[]> invarmor = new HashMap<String, ItemStack[]>();
	private Map<Integer, String> zombies = new HashMap<Integer, String>(200);
	private Map<Integer, String> fastzombies = new HashMap<Integer, String>(5);
	private Map<Integer, Map<String, Integer>> zombiescore = new HashMap<Integer, Map<String, Integer>>();
	private static Map<String, String> dead = new ConcurrentHashMap<String, String>();
	private Map<String, Location> deathPoints = new HashMap<String, Location>();
	private Map<String, Boolean> perkend = new HashMap<String, Boolean>();
	private static Map<String, String> Maps = new HashMap<String, String>(10);
	private Set<Location> Signs = new HashSet<Location>();
	private Set<String> voted = new HashSet<String>();
	private Map<String, Integer> wavemax = new HashMap<String, Integer>();

	private Map<String, Integer> add = new HashMap<String, Integer>(2);
	private Map<String, Integer> remove = new HashMap<String, Integer>(2);
	private int task;
	private int task2;
	private static Economy econ = null;
	private static Chat chat = null;
	private boolean points = true;
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	private static Field targetSelector;
	private Locale bLocale = new Locale("en", "US");
	private Collection<String> essentialswarps;
	private Location forcewarplocation;
	private boolean forcewarp;
	private Essentials essentialsPlugin;

	public void onEnable() {
		instance = this;
		try {
			saveDefaultConfig();
			File f = new File(getDataFolder(), "language.yml");
			if (!f.exists())
				saveResource("language.yml", true);
			f = new File(getDataFolder(), "awards.yml");
			if (!f.exists())
				saveResource("awards.yml", false);
			f = new File(getDataFolder(), "classes.yml");
			if (!f.exists())
				saveResource("classes.yml", false);

			lang.LoadLanguage();
			stats.plugin = this;
			classes.plugin = this;
			door.plugin = this;
			spawn.plugin = this;
			pot.plugin = this;
			revive.plugin = this;
			bar.plugin = this;
			classes.LoadClasses();
			stats.LoadStats();
			awards.LoadAwards();
			task = scheduleTask(this, 20L, 20L);
			if (setupEconomy()) {
				points = false;
				getLogger().info("Vault configured.");
			}
			reloadPlayers();
			QuickUpdate();
			checkMobs();
			AsynchTasks();
			synchTasks();
			revive.counterTask();
			perpNight();
			dealDamage();
			if (!points) {
				points = getConfig().getBoolean("force-points");
			}
			getLogger().info("ZombieSurvival enabled.");
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e) {
				getLogger().info("Metrics failed to start.");
			}
			try {
				targetSelector = EntityLiving.class.getDeclaredField("targetSelector");
				if (targetSelector == null) {
					getLogger().warning("Wolves disabled.");
					wolfs = false;
				} else {
					targetSelector.setAccessible(true);
				}
			} catch (Exception e) {
				getLogger().warning("Wolves disabled.");
				wolfs = false;
			}
			loadConfig();
			registerEssentials();
			registerEvents();
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().severe("ZombieSurvival failed to start.");
			getLogger().severe(e.toString());
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	public void onDisable() {
		door.saveDoor();
		spawn.saveSpawn();
		bar.saveBar();
		lang.savelConfig();
		awards.saveRConfig();
		classes.savecConfig();
		saveSigns();
		for (String map : Maps.keySet()) {
			GamesOver(map, true);
			GameFinishEvent gfe = new GameFinishEvent(map, GameFinishReason.PLUGIN_DISABLE);
			Bukkit.getServer().getPluginManager().callEvent(gfe);
		}
		getServer().getScheduler().cancelTasks(this);
		awards.Destroy();
		bar.Destroy();
		door.Destroy();
		pot.Destroy();
		spawn.Destroy();
		revive.Destroy();
		spawn.Destroy();
		getLogger().info("ZombieSurvival disabled.");
	}

	public static ZombieSurvivalPlugin getInstance() {
		return instance;
	}

	private void registerEssentials() {
		if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
			essentialsPlugin = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
			essentialswarps = essentialsPlugin.getWarps().getWarpNames();
			getLogger().info("Loaded Essentials Warp Locations.");
		}
	}

	private void registerEvents() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new AsyncChat(), this);
		pm.registerEvents(new BlockEvent(), this);
		CommandExecutor c = new CommandExecutor();
		getCommand("zsa-door").setExecutor(c);
		getCommand("zsr-door").setExecutor(c);
		getCommand("zs-doorlink").setExecutor(c);
		getCommand("zsa-spawn").setExecutor(c);
		getCommand("zsr-spawn").setExecutor(c);
		getCommand("zs-start").setExecutor(c);
		getCommand("zs-end").setExecutor(c);
		getCommand("zs-create").setExecutor(c);
		getCommand("zs-remove").setExecutor(c);
		getCommand("zsa-fire").setExecutor(c);
		getCommand("zsa-spectate").setExecutor(c);
		getCommand("zsr-spectate").setExecutor(c);
		getCommand("zsa-lightning").setExecutor(c);
		getCommand("zsr-lightning").setExecutor(c);
		getCommand("zs-reload").setExecutor(c);
		getCommand("zsa-special").setExecutor(c);
		getCommand("zsr-special").setExecutor(c);
		getCommand("zsa-bar").setExecutor(c);
		getCommand("zsr-bar").setExecutor(c);
		getCommand("zsa-perk").setExecutor(c);
		getCommand("zsr-perk").setExecutor(c);
		getCommand("zsa-waiting").setExecutor(c);
		getCommand("zsr-waiting").setExecutor(c);
		getCommand("zs-c").setExecutor(c);
		getCommand("zstp").setExecutor(c);
		getCommand("zsdebug").setExecutor(c);
		getCommand("stats").setExecutor(c);
		getCommand("zshelp").setExecutor(c);
		getCommand("whisper").setExecutor(c);
		getCommand("bsapj").setExecutor(c);
		getCommand("bsapl").setExecutor(c);
		pm.registerEvents(new DeathEvent(), this);
		pm.registerEvents(new EntityDamage(), this);
		pm.registerEvents(new PlayerInteract(), this);
		pm.registerEvents(new PlayerJoin(), this);
		pm.registerEvents(new PlayerLeave(), this);
		pm.registerEvents(new PlayerMove(), this);
		pm.registerEvents(new PlayerPickup(), this);
		pm.registerEvents(new PlayerRespawn(), this);
		pm.registerEvents(new SignChange(), this);
		pm.registerEvents(new Signs(), this);
		pm.registerEvents(perk, this);
		pm.registerEvents(new Spectate(), this);
		pm.registerEvents(revive, this);
		pm.registerEvents(new Donator(), this);
	}

	public void loadConfig() {
		door.loadDoor();
		spawn.loadSpawn();
		bar.loadBar();
		pot.loadPotions();
		List<String> worldnames = new ArrayList<String>();
		for (World world2 : Bukkit.getWorlds()) {
			worldnames.add(world2.getName());
		}
		for (Iterator<String> i = worldnames.iterator(); i.hasNext();) {
			String w = (String) i.next();
			for (String s : getCustomConfig().getStringList(w + ".maps"))
				Maps.put(s, w);
		}

		for (String str2 : getCustomConfig().getStringList("signs")) {
			Signs.add(parseToLoc(str2));
		}
		saveSigns();
		for (String str : Maps.keySet()) {
			for (String str2 : getCustomConfig().getStringList((String) Maps.get(str) + "." + str + ".roundfire")) {
				roundFire.put(parseToLoc(str2), str);
			}
			for (String str2 : getCustomConfig().getStringList((String) Maps.get(str) + "." + str + ".specialblocks")) {
				specialblocks.put(parseToLoc(str2), str);
			}
			deathPoints.put(str, parseToLoc(getCustomConfig().getString((String) Maps.get(str) + "." + str + ".waiting")));
			spawn.lobbies.put(str, parseToLoc(getCustomConfig().getString((String) Maps.get(str) + "." + str + ".lobby")));
			spawn.spectate.put(str, parseToLoc(getCustomConfig().getString((String) Maps.get(str) + "." + str + ".spectate")));
			lightloc.put(str, parseToLoc(getCustomConfig().getString((String) Maps.get(str) + "." + str + ".lightning")));
			perk.addLoc(str, parseToLoc(getCustomConfig().getString((String) Maps.get(str) + "." + str + ".perk")));
			Games.setMaxZombies(str, getCustomConfig().getInt((String) Maps.get(str) + "." + str + ".maxzombies"));
			Games.setMaxPlayers(str, getCustomConfig().getInt((String) Maps.get(str) + "." + str + ".maxplayers"));
			Games.setMaxWaves(str, getCustomConfig().getInt((String) Maps.get(str) + "." + str + ".maxwaves"));
			Games.setState(str, 1);
			Games.setPcount(str, 0);
			perk.setPerk(str, 0);
			perkcount.put(str, 0);
			changedBlocks.put(str, new HashMap<Block, BlockState>());
			placedBlocks.put(str, new HashMap<Block, BlockState>());
			perkend.put(str, true);
			Games.setWave(str, 0);
			Games.setZcount(str, 0);
		}
		for (String it : getConfig().getStringList("join-items")) {
			try {
				String[] line = it.split(":");
				int itemid = Integer.parseInt(line[0]);
				short itemdamage = 0;
				if (line.length > 1) {
					itemdamage = Short.parseShort(line[1]);
				}
				ItemStack item = new ItemStack(itemid, 1, itemdamage);
				joinitems.add(item);
			} catch (Exception e) {
				getLogger().warning("Could not load item config: " + it);
			}
		}
		for (String it : getConfig().getStringList("armor-items")) {
			try {
				String[] line = it.split(":");
				int itemid = Integer.parseInt(line[0]);
				short itemdamage = 0;
				if (line.length > 1) {
					itemdamage = Short.parseShort(line[1]);
				}
				ItemStack item = new ItemStack(itemid, 1, itemdamage);
				joinarmor.add(item);
			} catch (Exception e) {
				getLogger().warning("Could not load item config: " + it);
			}
		}
		for (String it : getConfig().getStringList("mysterybox-items")) {
			try {
				String[] line = it.split(":");
				int itemid = Integer.parseInt(line[0]);
				short itemdamage = 0;
				if (line.length > 1) {
					itemdamage = Short.parseShort(line[1]);
				}
				ItemStack item = new ItemStack(itemid, 1, itemdamage);
				boxitems.add(item);
			} catch (Exception e) {
				getLogger().warning("Could not load item config: " + it);
			}
		}
		for (String it : getConfig().getStringList("drop-items")) {
			try {
				String[] line = it.split(":");
				int itemid = Integer.parseInt(line[0]);
				short itemdamage = 0;
				double chance = 0.5D;
				if (line.length > 1) {
					itemdamage = Short.parseShort(line[1]);
				}
				if (line.length > 2) {
					chance = Double.parseDouble(line[2]);
				}
				ItemStack item = new ItemStack(itemid, 1, itemdamage);
				drops.put(item, Double.valueOf(chance));
			} catch (Exception e) {
				getLogger().warning("Could not load item config: " + it);
			}
		}
		blockbreak = getConfig().getIntegerList("allowbreak");
		blockplace = getConfig().getIntegerList("allowplace");
		startpcount = getConfig().getInt("start-player-count");
		antigrief = getConfig().getBoolean("auto-anti-grief");
		itemsatjoin = getConfig().getBoolean("items-at-join");
		joinmessage = getConfig().getString("auto-join-message");
		cooldown = getConfig().getInt("auto-cooldown");
		deathloss = getConfig().getDouble("death-loss-percent");
		diffmulti = getConfig().getDouble("health-multi");
		damagemulti = getConfig().getDouble("damage-multi");
		infectmat = getConfig().getBoolean("infect-mat");
		spectateallow = getConfig().getBoolean("allow-spectate");
		wait = (getConfig().getInt("wave-wait") * 20);
		perpnight = getConfig().getBoolean("perp-night");
		runnerchance = getConfig().getInt("runner-chance");
		pot.effectchance = getConfig().getInt("effect-chance");
		emptyaccount = getConfig().getBoolean("empty-account");
		forcespawn = getConfig().getBoolean("force-spawn");
		seekspeed = getConfig().getDouble("seek-speed");
		fastseekspeed = getConfig().getDouble("fast-seek-speed");
		pot.bitelength = (getConfig().getInt("bite-effect-length") * 20);
		doorfindradius = getConfig().getInt("buy-door-find-radius");
		respawn = getConfig().getBoolean("death-non-human-respawn");
		allhurt = getConfig().getBoolean("all-hurt");
		leavetimer = getConfig().getInt("leave-timer");
		forceclear = getConfig().getBoolean("inventory-clear-join");
		invsave = getConfig().getBoolean("inventory-save");
		healnewwave = getConfig().getBoolean("heal-player-new-wave");
		forcewarp = getConfig().getBoolean("force-warp");
		if (forcewarp && essentialsPlugin != null) {
			forcewarplocation = parseLocation(getConfig().getString("force-warp-location"));
		}
		skellywavechance = getConfig().getInt("wolf-wave-chance");
		joincommand = getConfig().getStringList("join-commands");
		leavecommand = getConfig().getStringList("leave-commands");
		maxpoints = getConfig().getInt("max-points-per-kill");
		resetpointsdeath = getConfig().getBoolean("reset-points-on-death");
		jm = getConfig().getBoolean("join-message");
		smartw = getConfig().getBoolean("use-smart-waves");

		saveCustomConfig();
	}

	private Location parseLocation(String s) {
		try {
			for (String w : essentialswarps) {
				if (w.equals(s)) { return essentialsPlugin.getWarps().getWarp(w); }
			}
		} catch (Exception e) {

		}
		forcewarp = false;
		return null;
	}

	public void reload() {
		for (String map : Maps.keySet()) {
			GamesOver(map, true);
			GameFinishEvent gfe = new GameFinishEvent(map, GameFinishReason.PLUGIN_DISABLE);
			Bukkit.getServer().getPluginManager().callEvent(gfe);
		}
		lightloc.clear();
		blockbreak.clear();
		blockplace.clear();
		roundFire.clear();
		door.doors.clear();
		add.clear();
		remove.clear();
		changedBlocks.clear();
		placedBlocks.clear();
		zombies.clear();
		perkcount.clear();
		dead.clear();
		reloadConfig();
		reloadCustomConfig();
		loadConfig();
		reloadPlayers();
	}

	public void run() {
		for (String map : Maps.keySet()) {
			World world = Bukkit.getWorld((String) Maps.get(map));
			int s = Games.getState(map);
			if ((s == 2) && (Games.getZcount(map) < ((Integer) wavemax.get(map)).intValue()) && Games.getPcount(map) > 0) {
				Entity ent;
				if (((Boolean) wolfwave.get(map)).booleanValue()) {
					ent = world.spawnEntity(spawn.spawn(map, Games.getWave(map)), EntityType.WOLF);
					Wolf w = (Wolf) ent;
					w.setAngry(true);
				} else {
					ent = world.spawnEntity(spawn.spawn(map, Games.getWave(map)), EntityType.ZOMBIE);
				}
				LivingEntity lent = (LivingEntity) ent;
				if (diffmulti != 0.0D) {
					int health = (int) (Games.getWave(map) * diffmulti);
					lent.setMaxHealth(3 + health);
					lent.setHealth(3 + health);
					lent.setRemoveWhenFarAway(false);
				}
				if (runnerchance != 0) {
					int go = random.nextInt(runnerchance);
					if (go == 1) {
						fastzombies.put(ent.getEntityId(), map);
					}
				}
				zombies.put(ent.getEntityId(), map);
				Games.setZcount(map, Games.getZcount(map) + 1);
			}
			if ((s == 2) && (((Integer) secondkills.get(map)).intValue() > Games.getPcount(map)) && (perk.getPerk(map) == 0)) {
				perk.callPerk(map);
				perkend.put(map, false);
			}
			secondkills.put(map, 0);
			if ((perk.getPerk(map) > 0) && (((Integer) perkcount.get(map)).intValue() < 60)) {
				perkcount.put(map, ((Integer) perkcount.get(map).intValue() + 1));
			} else {
				if ((!((Boolean) perkend.get(map)).booleanValue()) && (!perk.isDropped)) {
					for (String str : PlayerMethods.playersInMap(map)) {
						Player p = Bukkit.getPlayer(str);
						if (p != null) {
							p.sendMessage((String) lang.getStrings().get(9));
						}
					}
					perkend.put(map, true);
				}
				perk.setPerk(map, 0);
				perkcount.put(map, 0);
			}
		}
	}

	public void StartGames(String map, boolean force) {
		World world = Bukkit.getWorld((String) Maps.get(map));
		String health;
		String zombmax;
		List<Player> pim = new ArrayList<Player>();
		if ((Games.getPcount(map) >= startpcount) || (force)) {
			Games.setState(map, 2);
			Games.setWave(map, 1);
			Games.setZslayed(map, 0);
			Games.setZcount(map, 0);
			secondkills.put(map, 0);
			wolfwave.put(map, false);
			if (smartw)
				wavemax.put(map, sg.smartWaveCount(map));
			else {
				maxfinder(map);
			}
			health = Integer.toString(4 + (int) (Games.getWave(map) * diffmulti));
			zombmax = Integer.toString(((Integer) wavemax.get(map)).intValue());
			if (lightloc.get(map) != null) {
				world.strikeLightningEffect((Location) lightloc.get(map));
			}
			getLogger().info((String) lang.getStrings().get(0) + ": " + map);
			for (String mp : PlayerMethods.playersInMap(map)) {
				Player p = Bukkit.getPlayer(mp);
				if (p != null) {
					pim.add(p);
					p.teleport((Location) spawn.lobbies.get(map));
					p.setAllowFlight(false);
					p.setFlying(false);
					p.setGameMode(GameMode.SURVIVAL);
					p.setHealth(20);
					p.setFoodLevel(20);
					dead.remove(p.getName());
					joinItems(p);
					com.jordair.gmail.zombiesurvival.Classes.setupPlayer(p);
					p.sendMessage((String) lang.getStrings().get(2));
					p.sendMessage((String) lang.getStrings().get(3));
					p.sendMessage(ChatColor.DARK_RED + zombmax + " " + (String) lang.getStrings().get(4) + " " + ChatColor.DARK_RED
							+ health + " " + (String) lang.getStrings().get(5));
				}
			}
		} else {
			for (String mp : PlayerMethods.playersInMap(map)) {
				Player p = Bukkit.getPlayer(mp);
				if (p != null)
					p.sendMessage((String) lang.getStrings().get(6));
			}
		}
		GameStartEvent gse = new GameStartEvent(map, pim);
		Bukkit.getServer().getPluginManager().callEvent(gse);
	}

	public void placeInGame(Player p, String map, boolean wave) {
		Utilities.unhidePlayer(p);
		com.jordair.gmail.zombiesurvival.Stats.setPoints(p.getName(), 0.0D);
		p.teleport((Location) spawn.lobbies.get(map));
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(20);
		p.setFoodLevel(20);
		joinItems(p);
		com.jordair.gmail.zombiesurvival.Classes.setupPlayer(p);
		if (!wave)
			p.sendMessage((String) lang.getStrings().get(2));
	}

	public void GamesOver(String map, Boolean force) {
		GamesOver(map, force, "");
	}

	public void GamesOver(String map, Boolean force, String s) {
		World world = Bukkit.getWorld((String) Maps.get(map));
		if ((PlayerMethods.playersOnline(map) < 1) || (force.booleanValue())) {
			if (s != null && !s.isEmpty()) {
				GameFinishEvent gfe = new GameFinishEvent(map, (force ? GameFinishReason.FORCE_END : GameFinishReason.WON));
				Bukkit.getServer().getPluginManager().callEvent(gfe);
			}
			resetBlocks(map);
			Utilities.clearDrops(map);
			resetDoors(map);
			stats.saveStats();
			Games.setState(map, 1);
			Games.setWave(map, 0);
			Games.setPcount(map, 0);
			com.jordair.gmail.zombiesurvival.Stats.clearClassStats(map);
			bar.resetBars(map);
			vspoke = 0;
			wavemax.put(map, null);
			getLogger().info((String) lang.getStrings().get(1) + ": " + map);
			List<LivingEntity> templist = getLivingEnts(world);
			for (Iterator<LivingEntity> i = templist.iterator(); i.hasNext();) {
				LivingEntity went = (LivingEntity) i.next();
				for (Iterator<Integer> it = zombies.keySet().iterator(); it.hasNext();) {
					int id = ((Integer) it.next()).intValue();
					if ((((String) zombies.get(id)).equalsIgnoreCase(map)) && (went.getEntityId() == id)) {
						if (went.isValid()) {
							went.remove();
						}
						it.remove();
					}
				}
			}
			for (String mp : PlayerMethods.playersInMap(map)) {
				Games.removePlayerMap(mp);
				com.jordair.gmail.zombiesurvival.Stats.setPoints(mp, 0.0D);
				com.jordair.gmail.zombiesurvival.Stats.setDeaths(mp, 0.0D);
				com.jordair.gmail.zombiesurvival.Stats.removeSplayerKills(mp);
				dead.remove(mp);
				Spectate.getSpectators().remove(mp);
				Player p = Bukkit.getPlayer(mp);
				revive.removeSign(mp);
				if (p != null) {
					p.teleport((Location) playerloconjoin.get(mp));
					playerloconjoin.remove(mp);
					p.setAllowFlight(false);
					p.setFlying(false);
					p.setGameMode(GameMode.SURVIVAL);
					p.setHealth(20);
					p.setFoodLevel(20);
					if ((invsave) && (inv.containsKey(mp))) {
						p.getInventory().setContents((ItemStack[]) inv.get(mp));
						inv.remove(mp);
					}
					if ((invsave) && (invarmor.containsKey(mp))) {
						p.getInventory().setArmorContents((ItemStack[]) invarmor.get(mp));
						invarmor.remove(mp);
					}
					Utilities.unhidePlayer(p);
					p.setDisplayName(p.getName());
					p.sendMessage((String) lang.getStrings().get(7));
					if ((emptyaccount) && (!points)) {
						double removem = econ.getBalance(mp);
						econ.withdrawPlayer(p.getName(), removem);
					}
				}
			}
		}
	}

	public Location parseToLoc(String str) throws NumberFormatException {
		if (str == null) { return null; }
		String[] strs = str.split(" ");
		double xl = Double.parseDouble(strs[0]);
		double yl = Double.parseDouble(strs[1]);
		double zl = Double.parseDouble(strs[2]);
		World worldl = Bukkit.getServer().getWorld(strs[3]);
		Location parsedLoc = new Location(worldl, xl, yl, zl);
		return parsedLoc;
	}

	public String parseToStr(Location parseloc) {
		return String.format(bLocale, "%.2f %.2f %.2f %s", new Object[] { Double.valueOf(parseloc.getX()), Double.valueOf(parseloc.getY()),
				Double.valueOf(parseloc.getZ()), parseloc.getWorld().getName() });
	}

	public int scheduleTask(Runnable runnable, long initial, long delay) {
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, runnable, initial, delay);
	}

	public void info(Player p, Player s) {
		String str3 = rPlayers(PlayerMethods.playerGame(p));
		s.sendMessage(ChatColor.GREEN + "Current Wave: " + ChatColor.DARK_RED
				+ Integer.toString(Games.getWave(PlayerMethods.playerGame(p))));
		s.sendMessage(ChatColor.GREEN + "Kills: " + ChatColor.DARK_RED
				+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getSesKills(p.getName())));
		s.sendMessage(ChatColor.GREEN + "All-Time Kills: " + ChatColor.DARK_RED
				+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getTotalKills(p.getName())));
		if (com.jordair.gmail.zombiesurvival.Classes.isClassed(p)) {
			s.sendMessage(ChatColor.GREEN + "Class Kills: " + ChatColor.DARK_RED
					+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getClassKills(p.getName())));
		}
		if (!points) {
			String score = String.format("%.1f", new Object[] { Double.valueOf(econ.getBalance(p.getName())) });
			s.sendMessage(ChatColor.GREEN + "Balance: " + ChatColor.DARK_RED + score);
			s.sendMessage(ChatColor.GREEN + "All-Time Balance: " + ChatColor.DARK_RED
					+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getTotalPoints(p.getName())));
			if (com.jordair.gmail.zombiesurvival.Classes.isClassed(p))
				s.sendMessage(ChatColor.GREEN + "Class Balance: " + ChatColor.DARK_RED
						+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getClassPoints(p.getName())));
		} else {
			s.sendMessage(ChatColor.GREEN + "Points: " + ChatColor.DARK_RED
					+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getSesPoints(p.getName())));
			s.sendMessage(ChatColor.GREEN + "All-Time Points: " + ChatColor.DARK_RED
					+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getTotalPoints(p.getName())));
			if (com.jordair.gmail.zombiesurvival.Classes.isClassed(p)) {
				s.sendMessage(ChatColor.GREEN + "Class Points: " + ChatColor.DARK_RED
						+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getClassPoints(p.getName())));
			}
		}
		s.sendMessage(ChatColor.GREEN + "Deaths: " + ChatColor.DARK_RED
				+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getSesDeaths(p.getName())));
		s.sendMessage(ChatColor.GREEN + "All-Time Deaths: " + ChatColor.DARK_RED
				+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getTotalDeaths(p.getName())));
		if (com.jordair.gmail.zombiesurvival.Classes.isClassed(p)) {
			s.sendMessage(ChatColor.GREEN + "Class Deaths: " + ChatColor.DARK_RED
					+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getClassDeaths(p.getName())));
		}
		s.sendMessage(ChatColor.GREEN + "Remaining Players: " + str3);
	}

	public String rPlayers(String map) {
		String string = ChatColor.DARK_RED + "";
		for (String p : PlayerMethods.playersInMap(map)) {
			if (!dead.containsKey(p)) {
				string = string.concat(p) + ChatColor.GRAY + ", " + ChatColor.DARK_RED;
			}
		}
		return string;
	}

	public void NewWave(final String map) {
		Games.setState(map, 3);
		Games.setWave(map, Games.getWave(map) + 1);
		bar.resetBars(map);
		if (skellywavechance != 0) {
			int go = random.nextInt(skellywavechance);
			if ((go == 1) && (Games.getWave(map) != 1) && (wolfs))
				wolfwave.put(map, true);
			else {
				wolfwave.put(map, false);
			}
		}
		if (Integer.toString(Games.getWave(map)).endsWith("0")) {
			for (Location blg : roundFire.keySet()) {
				if (((String) roundFire.get(blg)).equalsIgnoreCase(map)) {
					blg.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
				}
			}
		}
		if (Games.getWave(map) > Games.getMaxWaves(map)) {
			GamesOver(map, true);
			GameFinishEvent gfe = new GameFinishEvent(map, GameFinishReason.WAVE_MAX);
			Bukkit.getServer().getPluginManager().callEvent(gfe);
			return;
		}
		String health = "20";
		if (diffmulti != 0.0D) {
			int h = (int) (Games.getWave(map) * diffmulti);
			health = Integer.toString(3 + h);
		}
		String zombmax;

		if (smartw)
			zombmax = Integer.toString(sg.smartWaveCount(map));
		else {
			zombmax = Utilities.annouceMax(map);
		}
		// TODO Moved out of the delayed task (was before games.setState)
		Games.setZcount(map, 0);
		Games.setZslayed(map, 0);
		if (Games.getWave(map) != 1) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					if (smartw)
						wavemax.put(map, sg.smartWaveCount(map));
					else {
						maxfinder(map);
					}
					Games.setState(map, 2);
				}
			}, wait);
		}

		Boolean doorsopen = false;
		String tcls = com.jordair.gmail.zombiesurvival.Stats.topClass(map);
		WaveChangeEvent wce = new WaveChangeEvent(map, Games.getWave(map));
		Bukkit.getServer().getPluginManager().callEvent(wce);
		for (String player : PlayerMethods.playersInMap(map)) {
			Player p = Bukkit.getPlayer(player);
			if (p != null) {
				p.sendMessage(ChatColor.GREEN + "WAVE: " + ChatColor.DARK_RED + Integer.toString(Games.getWave(map)) + ChatColor.GRAY
						+ " starts in " + ChatColor.DARK_RED + Integer.toString(wait / 20) + ChatColor.GRAY + " seconds!");
				if (wolfwave.get(map)) {
					p.sendMessage(ChatColor.GRAY + "The zombies have called in the " + ChatColor.DARK_RED + "wolves!" + ChatColor.GRAY
							+ " Prepare for the enslaught!");
					p.sendMessage(ChatColor.DARK_RED + zombmax + ChatColor.GRAY + " wolves with " + ChatColor.DARK_RED + health
							+ ChatColor.GRAY + " health!");
				} else {
					p.sendMessage(ChatColor.DARK_RED + zombmax + ChatColor.GRAY + " zombies with " + ChatColor.DARK_RED + health
							+ ChatColor.GRAY + " health!");
				}
				if (!tcls.equalsIgnoreCase("ERROR")) {
					p.sendMessage(ChatColor.DARK_RED + "Wave Best Class: " + ChatColor.GRAY + tcls);
				}
			}
		}
		for (Iterator<String> it = dead.keySet().iterator(); it.hasNext();) {
			String player = (String) it.next();
			if (((String) dead.get(player)).equalsIgnoreCase(map)) {
				Player p = Bukkit.getPlayer(player);
				if (p != null) {
					placeInGame(p, (String) dead.get(player), true);
					Games.setPcount(map, Games.getPcount(map) + 1);
					it.remove();
				}
			}
		}
		for (Iterator<String> it = Spectate.getSpectators().keySet().iterator(); it.hasNext();) {
			String name = (String) it.next();
			if (((String) Spectate.getSpectators().get(name)).matches(map)) {
				Player p = Bukkit.getPlayer(name);
				Utilities.unhidePlayer(p);
				it.remove();
			}
		}
		for (Door d : door.doorWave(map, Games.getWave(map))) {
			door.openDoor(d);
			// Block dblock = d.location.getBlock();
			// dblock.setType(Material.AIR);
			doorsopen = true;
		}
		for (String player : PlayerMethods.playersInMap(map)) {
			Player p = Bukkit.getPlayer(player);
			if (p != null) {
				if (healnewwave) {
					p.setHealth(20);
					p.setFoodLevel(20);
				}
				if (doorsopen.booleanValue())
					p.sendMessage((String) lang.getStrings().get(67));
			}
		}
	}

	public void resetBlocks(String map) {
		List<Location> fire = new ArrayList<Location>();
		if ((!roundFire.isEmpty()) && (roundFire != null)) {
			for (Location str : roundFire.keySet()) {
				if (((String) roundFire.get(str)).equalsIgnoreCase(map)) {
					fire.add(str);
				}
			}
			for (Location b : fire) {
				b.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
			}
			fire.clear();
		}
		if ((!changedBlocks.isEmpty()) && (changedBlocks.get(map) != null)) {
			for (Block b : ((Map<Block, BlockState>) changedBlocks.get(map)).keySet()) {
				BlockState bstate = (BlockState) ((Map<Block, BlockState>) changedBlocks.get(map)).get(b);
				b.setTypeIdAndData(bstate.getTypeId(), bstate.getRawData(), false);
			}
			((Map<Block, BlockState>) changedBlocks.get(map)).clear();
		}
		if ((!placedBlocks.isEmpty()) && (placedBlocks.get(map) != null)) {
			for (Block p : ((Map<Block, BlockState>) placedBlocks.get(map)).keySet()) {
				p.setType(Material.AIR);
			}
			((Map<Block, BlockState>) placedBlocks.get(map)).clear();
		}
	}

	public void addLobby(Location lobbyloc, String map) {
		lobbyloc.add(0.5D, 1.0D, 0.5D);
		spawn.lobbies.put(map, lobbyloc);
		String savelobbyloc = parseToStr(lobbyloc);
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".lobby", savelobbyloc);
		saveCustomConfig();
	}

	public void removeLobby(Location lobbyloc, String map) {
		lobbyloc.add(0.5D, 1.0D, 0.5D);
		if ((spawn.lobbies.containsKey(map)) && (((Location) spawn.lobbies.get(map)).equals(lobbyloc))) {
			spawn.lobbies.remove(map);
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".lobby", null);
		saveCustomConfig();
	}

	public void addFire(Location fireblock, String map) {
		roundFire.put(fireblock, map);
		List<String> adeath = new ArrayList<String>();
		for (Location str : roundFire.keySet()) {
			if (((String) roundFire.get(str)).equalsIgnoreCase(map)) {
				adeath.add(parseToStr(str));
			}
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".roundfire", adeath);
		saveCustomConfig();
	}

	public void removeFire(Location fireblock, String map) {
		roundFire.remove(fireblock);
		List<String> adeath = new ArrayList<String>();
		for (Location str : roundFire.keySet()) {
			if (((String) roundFire.get(str)).equalsIgnoreCase(map)) {
				adeath.add(parseToStr(str));
			}
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".roundfire", adeath);
		saveCustomConfig();
	}

	public void addSpecial(Location specialblock, String map) {
		specialblocks.put(specialblock, map);
		List<String> adeath = new ArrayList<String>();
		for (Location str : specialblocks.keySet()) {
			if (((String) specialblocks.get(str)).equalsIgnoreCase(map)) {
				adeath.add(parseToStr(str));
			}
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".specialblocks", adeath);
		saveCustomConfig();
	}

	public void removeSpecial(Location specialblock, String map) {
		specialblocks.remove(specialblock);
		List<String> adeath = new ArrayList<String>();
		for (Location str : specialblocks.keySet()) {
			if (((String) specialblocks.get(str)).equalsIgnoreCase(map)) {
				adeath.add(parseToStr(str));
			}
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".specialblocks", adeath);
		saveCustomConfig();
	}

	public void addBar(Block b, String map) {
		bar.addBar(b, map);
	}

	public void removeBar(Block b, String map) {
		bar.removeBar(bar.findBar(b.getX(), b.getY(), b.getZ()));
	}

	public void removeBars(List<Barricade> b, String map) {
		for (Barricade a : b) {
			removeBar(a.getLocation().getBlock(), map);
		}
	}

	public void addfPerk(Location specialblock, String map) {
		specialblock.add(0.5D, 0.1D, 0.5D);
		perk.addLoc(map, specialblock);
		String str = parseToStr(specialblock);
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".perk", str);
		saveCustomConfig();
	}

	public void removefPerk(String map) {
		perk.removeLoc(map);
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".perk", "");
		saveCustomConfig();
	}

	public void addSpawn(Block b, int wavenumber, String map) {
		spawn.addSpawn(b, map, wavenumber);
	}

	public void removeSpawn(Block b, String map) {
		spawn.removeSpawn(spawn.findSpawn(b.getX() + 0.5D, b.getY(), b.getZ() + 0.5D));
	}

	public boolean addDoor(Block b, int wavenumber, String map) {
		return door.addDoor(b, map, wavenumber);
	}

	public void removeDoor(Location b) {
		door.removeDoor(door.findDoor(b.getX(), b.getY(), b.getZ()));
	}

	public void removeDoors(List<Door> b) {
		for (Door a : b) {
			removeDoor(a.getLocation());
		}
	}

	public void addSpec(Location specloc, String map) {
		specloc.add(0.5D, 1.0D, 0.5D);
		spawn.spectate.put(map, specloc);
		String savelobbyloc = parseToStr(specloc);
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".spectate", savelobbyloc);
		saveCustomConfig();
	}

	public void removeSpec(Location specloc, String map) {
		specloc.add(0.5D, 1.0D, 0.5D);
		if ((spawn.spectate.containsKey(map)) && (((Location) spawn.spectate.get(map)).equals(specloc))) {
			spawn.spectate.remove(map);
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".spectate", null);
		saveCustomConfig();
	}

	public void addLight(Location lightloca, String map) {
		lightloca.add(0.5D, 1.0D, 0.5D);
		lightloc.put(map, lightloca);
		String savelobbyloc = parseToStr(lightloca);
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".lightning", savelobbyloc);
		saveCustomConfig();
	}

	public void removeLight(Location lightloca, String map) {
		lightloca.add(0.5D, 1.0D, 0.5D);
		if ((lightloc.containsKey(map)) && (((Location) lightloc.get(map)).equals(lightloca))) {
			lightloc.remove(map);
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".lightning", null);
		saveCustomConfig();
	}

	public void addDeath(Location deathloc, String map) {
		deathloc.add(0.5D, 1.0D, 0.5D);
		deathPoints.put(map, deathloc);
		String savelobbyloc = parseToStr(deathloc);
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".waiting", savelobbyloc);
		saveCustomConfig();
	}

	public void removeDeath(Location deathloc, String map) {
		deathloc.add(0.5D, 1.0D, 0.5D);
		if ((deathPoints.containsKey(map)) && (((Location) deathPoints.get(map)).equals(deathloc))) {
			deathPoints.remove(map);
		}
		getCustomConfig().set((String) Maps.get(map) + "." + map + ".waiting", null);
		saveCustomConfig();
	}

	@SuppressWarnings("deprecation")
	public void joinItems(Player player) {
		if ((!player.hasPermission("zs.donator")) && (invsave)) {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
		}
		if ((!joinitems.isEmpty()) && (itemsatjoin) && (!com.jordair.gmail.zombiesurvival.Classes.isClassed(player))) {
			for (ItemStack item : joinarmor) {
				Utilities.equipPlayer(player, item);
				player.updateInventory();
			}
			for (ItemStack item : joinitems) {
				Utilities.equipPlayer(player, item);
				player.updateInventory();
			}
			player.sendMessage(ChatColor.GREEN + "Check inventory for items!");
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) { return false; }
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) { return false; }
		econ = (Economy) rsp.getProvider();
		return econ != null;
	}

	public List<ItemStack> randomItem() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (ItemStack i : drops.keySet()) {
			double chance = ((Double) drops.get(i)).doubleValue();
			double chan = random.nextDouble();
			if (chan <= chance) {
				items.add(i);
			}
		}
		return items;
	}

	public void maxfinder(String map) {
		int mz = Games.getMaxZombies(map);
		int w = Games.getWave(map);
		if (mz < 10) {
			wavemax.put(map, (int) (mz * w * 0.5D));
		}
		if ((mz >= 10) && (mz <= 50)) {
			wavemax.put(map, (int) (mz * w * 0.1D));
		}
		if ((mz >= 51) && (mz <= 100)) {
			wavemax.put(map, (int) (mz * w * 0.08D));
		}
		if ((mz >= 101) && (mz <= 200)) {
			wavemax.put(map, (int) (mz * w * 0.05D));
		}
		if (mz >= 201) {
			wavemax.put(map, (int) (mz * w * 0.04D));
		}
		if (((Integer) wavemax.get(map)).intValue() < 1)
			wavemax.put(map, 1);
	}

	public void SignUpdater() {
		for (Iterator<Location> it = Signs.iterator(); it.hasNext();) {
			Location sloc = (Location) it.next();
			Block block = sloc.getBlock();
			if ((block.getState() instanceof Sign)) {
				Sign sign = (Sign) block.getState();
				String[] lines = sign.getLines();
				if (lines[1].contains("zombies")) {
					String map = lines[2];
					if ((Maps.containsKey(map)) && (Games.getState(map) > 1)) {
						sign.setLine(1, "§4" + Integer.toString(Games.getZcount(map) - Games.getZslayed(map)) + " §0zombies");
						sign.setLine(3, "Wave: " + Integer.toString(Games.getWave(map)));
						sign.update();
					} else {
						sign.setLine(1, "no zombies in");
						sign.setLine(3, "not started");
						sign.update();
					}
					if (!Maps.containsKey(map)) {
						block.setTypeId(0);
						it.remove();
						saveSigns();
					}
				} else if (Maps.containsKey(lines[1])) {
					sign.setLine(
							2,
							"Players: " + Integer.toString(PlayerMethods.numberInMap(lines[1])) + "/"
									+ Integer.toString(Games.getMaxPlayers(lines[1])));
					sign.setLine(3, "Wave: " + Integer.toString(Games.getWave(lines[1])));
					sign.update();
				} else {
					block.setTypeId(0);
					it.remove();
					saveSigns();
				}
			}
		}
	}

	public void saveSigns() {
		List<String> temp = new ArrayList<String>();
		for (Location strloc : Signs) {
			temp.add(parseToStr(strloc));
		}
		getCustomConfig().set("signs", temp);
		saveCustomConfig();
	}

	public void reloadPlayers() {
		Player[] list = Bukkit.getOnlinePlayers();
		for (Player p : list) {
			com.jordair.gmail.zombiesurvival.Stats.setPoints(p.getName(), 0.0D);
			com.jordair.gmail.zombiesurvival.Stats.setKills(p.getName(), 0.0D);
			onlinep += 1;
		}
	}

	public boolean openDoors(Location middle, String map) {
		boolean opened = false;
		for (int x = middle.getBlockX() - doorfindradius; x <= middle.getBlockX() + doorfindradius; x++) {
			for (int y = middle.getBlockY() - doorfindradius; y <= middle.getBlockY() + doorfindradius; y++) {
				for (int z = middle.getBlockZ() - doorfindradius; z <= middle.getBlockZ() + doorfindradius; z++) {
					if (door.openDoor(door.findDoor(x, y, z))) {
						opened = true;
					}
				}
			}
		}
		return opened;
	}

	public void QuickUpdate() {
		task2 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				String map;
				List<Location> plist;
				for (Iterator<String> i = ZombieSurvivalPlugin.Maps.keySet().iterator(); i.hasNext();) {
					map = (String) i.next();
					World world = Bukkit.getWorld((String) ZombieSurvivalPlugin.Maps.get(map));
					if ((Games.getState(map) > 1) && (world != null) && (Games.getPcount(map) > 0)) {
						plist = new ArrayList<Location>();
						for (String pl : PlayerMethods.playersInMap(map)) {
							Player p = Bukkit.getPlayer(pl);
							if ((!ZombieSurvivalPlugin.dead.containsKey(pl)) && (p != null) && (!p.isDead())
									&& (p.getWorld().getName().equalsIgnoreCase((String) ZombieSurvivalPlugin.Maps.get(map)))) {
								plist.add(p.getLocation());
							}
						}
						List<LivingEntity> templist = ZombieSurvivalPlugin.getLivingEnts(world);
						for (LivingEntity went : templist)
							if (went.isValid()) {
								int id = went.getEntityId();
								if ((zombies.containsKey(id)) && (((String) zombies.get(id)).equalsIgnoreCase(map))) {
									Location myLocation = went.getLocation();
									bar.damageBars(myLocation, map);
									Location closest = (Location) spawn.lobbies.get(map);
									double closestDist;
									if (plist.size() > 0) {
										closest = (Location) plist.get(0);
										closestDist = 999999999999999.0D;
										for (Location locs : plist) {
											double dist = locs.distanceSquared(myLocation);
											if (dist < closestDist) {
												closestDist = dist;
												closest = locs;
											}
										}
									}
									if (went.getType() == EntityType.WOLF) {
										Wolf w = (Wolf) went;
										updateTarget(map, w);
									}
									Location TravelTo = Utilities.getSegment(went, closest);
									if ((fastzombies.containsKey(went.getEntityId()))
											&& (((String) fastzombies.get(went.getEntityId())).equalsIgnoreCase(map)))
										Utilities.livingEntityMoveTo(went, TravelTo.getX(), TravelTo.getY(), TravelTo.getZ(),
												(float) fastseekspeed);
									else
										Utilities.livingEntityMoveTo(went, TravelTo.getX(), TravelTo.getY(), TravelTo.getZ(),
												(float) seekspeed);
								}
							}
					}
				}
			}
		}, 5L, 5L);
	}

	public void updateTarget(String map, Wolf w) {
		if ((targetSelector != null) && ((w instanceof CraftWolf))) {
			CraftWolf cw = (CraftWolf) w;
			EntityWolf ew = cw.getHandle();
			PathfinderGoalSelector s = null;
			try {
				s = (PathfinderGoalSelector) targetSelector.get(ew);
			} catch (Exception e) {
			}
			if (s != null) {
				s.a(4, new PathfinderGoalNearestAttackableTarget(ew, EntityHuman.class, 0, true));
			}
		}
		w.setAngry(true);
	}

	public void PayPlayers(int zombid) {
		try {
			for (String player : ((Map<String, Integer>) zombiescore.get(zombid)).keySet()) {
				Player p = Bukkit.getPlayer(player);
				if (p != null) {
					int newscore = 0;
					int gained = 0;
					if (points) {
						gained = ((Integer) ((Map<String, Integer>) zombiescore.get(zombid)).get(player)).intValue();
						newscore = gained + (int) com.jordair.gmail.zombiesurvival.Stats.getSesPoints(player);
						com.jordair.gmail.zombiesurvival.Stats.addPoints(player, gained);
					} else if (!points) {
						gained = ((Integer) ((Map<String, Integer>) zombiescore.get(zombid)).get(player)).intValue();
						newscore = gained + (int) econ.getBalance(player);
						econ.depositPlayer(player, gained);
					}
					if (p != null)
						if (points) {
							p.sendMessage(ChatColor.GREEN + "You now have " + ChatColor.DARK_RED + Integer.toString(newscore)
									+ ChatColor.GREEN + " points! GAINED: " + ChatColor.DARK_RED + Integer.toString(gained));
						} else if (!points) {
							String score = String.format("%.1f", new Object[] { Double.valueOf(econ.getBalance(player)) });
							p.sendMessage(ChatColor.GREEN
									+ "You now have "
									+ ChatColor.DARK_RED
									+ score
									+ ChatColor.GREEN
									+ " dollars! GAINED: "
									+ ChatColor.DARK_RED
									+ Integer.toString(((Integer) ((Map<String, Integer>) zombiescore.get(zombid)).get(player)).intValue() / 2));
						}
				}
			}
		} catch (Exception e) {
		}
	}

	public void saveCustomConfig() {
		if ((customConfig == null) || (customConfigFile == null))
			return;
		try {
			getCustomConfig().save(customConfigFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}

	public FileConfiguration getCustomConfig() {
		if (customConfig == null) {
			reloadCustomConfig();
		}
		return customConfig;
	}

	public void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(getDataFolder(), "games.yml");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

		InputStream defConfigStream = getResource("games.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			customConfig.setDefaults(defConfig);
		}
	}

	public boolean mysteryBox(Location middle, Chest chest, Player p) {
		World world = middle.getWorld();
		for (int x = middle.getBlockX() - 2; x <= middle.getBlockX() + 2; x++) {
			for (int y = middle.getBlockY() - 2; y <= middle.getBlockY() + 2; y++) {
				for (int z = middle.getBlockZ() - 2; z <= middle.getBlockZ() + 2; z++) {
					Location temp = new Location(world, x, y, z);
					Block sign = temp.getBlock();
					if ((sign.getState() instanceof Sign)) {
						Sign actual = (Sign) sign.getState();
						String[] lines = actual.getLines();
						try {
							int cost = Integer.parseInt(lines[1]);
							List<HumanEntity> viewers = chest.getBlockInventory().getViewers();
							if (viewers.size() > 0) {
								p.sendMessage("Try again later.");
								return false;
							}
							if (lines[0].equalsIgnoreCase("§9zombie box")) {
								if ((points) && (com.jordair.gmail.zombiesurvival.Stats.getSesPoints(p.getName()) >= cost)) {
									com.jordair.gmail.zombiesurvival.Stats.addPoints(p.getName(), -cost);
									chest.getBlockInventory().clear();
									int ritem = random.nextInt(boxitems.size());
									ItemStack item = (ItemStack) boxitems.get(ritem);
									chest.getBlockInventory().setItem(random.nextInt(27), item);
									chest.update();
									String name = p.getName();
									p.setDisplayName("["
											+ Double.toString(com.jordair.gmail.zombiesurvival.Stats.getSesPoints(p.getName())) + "]"
											+ name);
									p.sendMessage(ChatColor.GREEN + "You have purchased this " + ChatColor.DARK_RED + "Mystery Box "
											+ ChatColor.GREEN + "for " + Integer.toString(cost) + " points!");
									return true;
								}
								if ((!points) && (econ.getBalance(p.getName()) >= cost)) {
									econ.withdrawPlayer(p.getName(), cost);
									chest.getBlockInventory().clear();
									int ritem = random.nextInt(boxitems.size() - 1);
									ItemStack item = (ItemStack) boxitems.get(ritem);
									chest.getBlockInventory().setItem(random.nextInt(27), item);
									chest.update();
									p.sendMessage(ChatColor.GREEN + "You have purchased this " + ChatColor.DARK_RED + "Mystery Box "
											+ ChatColor.GREEN + "for " + Integer.toString(cost) + " dollars!");
									return true;
								}
								p.sendMessage("Cannot afford this Mystery Box");
								return false;
							}
						} catch (Exception e) {
							return true;
						}
					}
				}
			}
		}
		return true;
	}

	public void checkMobs() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (!zombies.isEmpty())
					for (String map : ZombieSurvivalPlugin.Maps.keySet())
						if (Games.getState(map) == 2) {
							World world = Bukkit.getWorld((String) ZombieSurvivalPlugin.Maps.get(map));
							List<LivingEntity> templis = ZombieSurvivalPlugin.getLivingEnts(world);
							List<Integer> tempID = new ArrayList<Integer>();
							for (Entity id : templis) {
								int entid = id.getEntityId();
								tempID.add(entid);
							}
							List<Integer> IDs = new ArrayList<Integer>();
							for (Integer i : zombies.keySet()) {
								if (((String) zombies.get(i)).equalsIgnoreCase(map)) {
									IDs.add(i);
								}
							}
							for (Integer i2 : IDs) {
								if (!tempID.contains(i2)) {
									Games.setZslayed(map, Games.getZslayed(map) + 1);
									zombies.remove(i2);
								}
							}
							if (Games.getZslayed(map) >= ((Integer) wavemax.get(map)).intValue())
								NewWave(map);
						}
			}
		}, 200L, 200L);
	}

	public void AsynchTasks() {
		getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			public void run() {
				for (Iterator<String> it = justleftgame.keySet().iterator(); it.hasNext();) {
					String string = (String) it.next();
					int orig = ((Integer) justleftgame.get(string)).intValue();
					if (orig < leavetimer) {
						orig++;
						justleftgame.put(string, orig);
					} else {
						it.remove();
					}
				}
			}
		}, 20L, 20L);
	}

	public void synchTasks() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				SignUpdater();
			}
		}, 40L, 40L);
	}

	public void perpNight() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				String t = "";
				for (String s : ZombieSurvivalPlugin.Maps.keySet()) {
					String b = (String) ZombieSurvivalPlugin.Maps.get(s);
					if (!b.equalsIgnoreCase(t)) {
						t = b;
						World world = Bukkit.getWorld(b);
						if (perpnight)
							world.setTime(13000L);
					}
				}
			}
		}, 5L, 8000L);
	}

	public void dealDamage() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				Player[] list = Bukkit.getOnlinePlayers();
				for (Player p : list)
					if (((p.getLocation().getBlock().getTypeId() == 8) || (p.getLocation().getBlock().getTypeId() == 9) || (p.getLocation()
							.getBlock().getTypeId() == 30))
							&& (PlayerMethods.inGame(p)) && (infectmat))
						p.damage(2);
			}
		}, 20L, 20L);
	}

	public void zsDebug(int i, Player p) {
		if (i == 0) {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "-----------Diagnostics Started-----------");
			for (String map : Maps.keySet()) {
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " state: "
								+ Integer.toString(Games.getState(map)));
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " onlinepcount: "
								+ Integer.toString(PlayerMethods.playersOnline(map)));
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " pcount: "
								+ Integer.toString(Games.getPcount(map)));
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " numberinmap: "
								+ Integer.toString(PlayerMethods.numberInMap(map)));
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " wave: "
								+ Integer.toString(Games.getWave(map)));
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " zcount: "
								+ Integer.toString(Games.getZcount(map)));
			}
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Number of Signs: " + Integer.toString(Signs.size()));
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Number of Maps: " + Integer.toString(Maps.size()));
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Number of Zombies: " + Integer.toString(zombies.size()));
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "------------Diagnostics Ended------------");
		}
		if ((i == 1) && (p != null)) {
			p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "-----------Diagnostics Started-----------");
			for (String map : Maps.keySet()) {
				p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " state: "
						+ Integer.toString(Games.getState(map)));
				p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " onlinepcount: "
						+ Integer.toString(PlayerMethods.playersOnline(map)));
				p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " pcount: "
						+ Integer.toString(Games.getPcount(map)));
				p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " numberinmap: "
						+ Integer.toString(PlayerMethods.numberInMap(map)));
				p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " wave: "
						+ Integer.toString(Games.getWave(map)));
				p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Game: " + map + " zcount: "
						+ Integer.toString(Games.getZcount(map)));
			}
			p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Number of Signs: " + Integer.toString(Signs.size()));
			p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Number of Maps: " + Integer.toString(Maps.size()));
			p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "Number of Zombies: "
					+ Integer.toString(zombies.size()));
			p.sendMessage(ChatColor.GREEN + "[ZombieSurvival] " + ChatColor.WHITE + "------------Diagnostics Ended------------");
		}
	}

	public static synchronized List<Entity> getEnts(World world) {
		return world.getEntities();
	}

	public static synchronized List<LivingEntity> getLivingEnts(World world) {
		return world.getLivingEntities();
	}

	public void resetDoors(String m) {
		try {
			door.resetDoors(m);
		} catch (Exception e) {
			getLogger().info(m + " Did not reset doors correctly!");
		}
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public Spawns getSpawn() {
		return spawn;
	}

	public void setSpawn(Spawns spawn) {
		this.spawn = spawn;
	}

	public Awards getAwards() {
		return awards;
	}

	public void setAwards(Awards awards) {
		this.awards = awards;
	}

	public Barricades getBar() {
		return bar;
	}

	public void setBar(Barricades bar) {
		this.bar = bar;
	}

	public Language getLang() {
		return lang;
	}

	public void setLang(Language lang) {
		this.lang = lang;
	}

	public Perk getPerk() {
		return perk;
	}

	public void setPerk(Perk perk) {
		this.perk = perk;
	}

	public Potions getPot() {
		return pot;
	}

	public void setPot(Potions pot) {
		this.pot = pot;
	}

	public SmartGames getSg() {
		return sg;
	}

	public void setSg(SmartGames sg) {
		this.sg = sg;
	}

	public Doors getDoor() {
		return door;
	}

	public void setDoor(Doors door) {
		this.door = door;
	}

	public Revive getRevive() {
		return revive;
	}

	public void setRevive(Revive revive) {
		this.revive = revive;
	}

	public Classes getClasses() {
		return classes;
	}

	public void setClasses(Classes classes) {
		this.classes = classes;
	}

	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}

	public Map<String, Integer> getSecondkills() {
		return secondkills;
	}

	public void setSecondkills(Map<String, Integer> secondkills) {
		this.secondkills = secondkills;
	}

	public int getOnlinep() {
		return onlinep;
	}

	public void setOnlinep(int onlinep) {
		this.onlinep = onlinep;
	}

	public Map<String, Integer> getJustleftgame() {
		return justleftgame;
	}

	public void setJustleftgame(Map<String, Integer> justleftgame) {
		this.justleftgame = justleftgame;
	}

	public Map<String, Location> getPlayerloconjoin() {
		return playerloconjoin;
	}

	public void setPlayerloconjoin(Map<String, Location> playerloconjoin) {
		this.playerloconjoin = playerloconjoin;
	}

	public Map<String, Boolean> getWolfwave() {
		return wolfwave;
	}

	public void setWolfwave(Map<String, Boolean> wolfwave) {
		this.wolfwave = wolfwave;
	}

	public Map<String, Integer> getEasycreate() {
		return easycreate;
	}

	public void setEasycreate(Map<String, Integer> easycreate) {
		this.easycreate = easycreate;
	}

	public Map<String, String> getEcname() {
		return ecname;
	}

	public void setEcname(Map<String, String> ecname) {
		this.ecname = ecname;
	}

	public Map<String, Integer> getEcpcount() {
		return ecpcount;
	}

	public void setEcpcount(Map<String, Integer> ecpcount) {
		this.ecpcount = ecpcount;
	}

	public Map<String, Integer> getEcwcount() {
		return ecwcount;
	}

	public void setEcwcount(Map<String, Integer> ecwcount) {
		this.ecwcount = ecwcount;
	}

	public Map<String, Double> getEczcount() {
		return eczcount;
	}

	public void setEczcount(Map<String, Double> eczcount) {
		this.eczcount = eczcount;
	}

	public Map<String, Door> getPlayerdoorlink() {
		return playerdoorlink;
	}

	public void setPlayerdoorlink(Map<String, Door> playerdoorlink) {
		this.playerdoorlink = playerdoorlink;
	}

	public List<String> getTwomintimer() {
		return twomintimer;
	}

	public void setTwomintimer(List<String> twomintimer) {
		this.twomintimer = twomintimer;
	}

	public int getStartpcount() {
		return startpcount;
	}

	public void setStartpcount(int startpcount) {
		this.startpcount = startpcount;
	}

	public Map<String, Integer> getCommandwave() {
		return commandwave;
	}

	public void setCommandwave(Map<String, Integer> commandwave) {
		this.commandwave = commandwave;
	}

	public Map<String, String> getCommandMap() {
		return commandMap;
	}

	public void setCommandMap(Map<String, String> commandMap) {
		this.commandMap = commandMap;
	}

	public Map<String, Integer> getPerkcount() {
		return perkcount;
	}

	public void setPerkcount(Map<String, Integer> perkcount) {
		this.perkcount = perkcount;
	}

	public boolean isOutofdate() {
		return outofdate;
	}

	public void setOutofdate(boolean outofdate) {
		this.outofdate = outofdate;
	}

	public boolean isAntigrief() {
		return antigrief;
	}

	public void setAntigrief(boolean antigrief) {
		this.antigrief = antigrief;
	}

	public boolean isItemsatjoin() {
		return itemsatjoin;
	}

	public void setItemsatjoin(boolean itemsatjoin) {
		this.itemsatjoin = itemsatjoin;
	}

	public boolean isInfectmat() {
		return infectmat;
	}

	public void setInfectmat(boolean infectmat) {
		this.infectmat = infectmat;
	}

	public boolean isSpectateallow() {
		return spectateallow;
	}

	public void setSpectateallow(boolean spectateallow) {
		this.spectateallow = spectateallow;
	}

	public boolean isPerpnight() {
		return perpnight;
	}

	public void setPerpnight(boolean perpnight) {
		this.perpnight = perpnight;
	}

	public boolean isEmptyaccount() {
		return emptyaccount;
	}

	public void setEmptyaccount(boolean emptyaccount) {
		this.emptyaccount = emptyaccount;
	}

	public boolean isForcespawn() {
		return forcespawn;
	}

	public void setForcespawn(boolean forcespawn) {
		this.forcespawn = forcespawn;
	}

	public boolean isRespawn() {
		return respawn;
	}

	public void setRespawn(boolean respawn) {
		this.respawn = respawn;
	}

	public boolean isAllhurt() {
		return allhurt;
	}

	public void setAllhurt(boolean allhurt) {
		this.allhurt = allhurt;
	}

	public boolean isForceclear() {
		return forceclear;
	}

	public void setForceclear(boolean forceclear) {
		this.forceclear = forceclear;
	}

	public boolean isInvsave() {
		return invsave;
	}

	public void setInvsave(boolean invsave) {
		this.invsave = invsave;
	}

	public double getSeekspeed() {
		return seekspeed;
	}

	public void setSeekspeed(double seekspeed) {
		this.seekspeed = seekspeed;
	}

	public double getFastseekspeed() {
		return fastseekspeed;
	}

	public void setFastseekspeed(double fastseekspeed) {
		this.fastseekspeed = fastseekspeed;
	}

	public boolean isWolfs() {
		return wolfs;
	}

	public void setWolfs(boolean wolfs) {
		this.wolfs = wolfs;
	}

	public boolean isHealnewwave() {
		return healnewwave;
	}

	public void setHealnewwave(boolean healnewwave) {
		this.healnewwave = healnewwave;
	}

	public boolean isResetpointsdeath() {
		return resetpointsdeath;
	}

	public void setResetpointsdeath(boolean resetpointsdeath) {
		this.resetpointsdeath = resetpointsdeath;
	}

	public boolean isJm() {
		return jm;
	}

	public void setJm(boolean jm) {
		this.jm = jm;
	}

	public boolean isSmartw() {
		return smartw;
	}

	public void setSmartw(boolean smartw) {
		this.smartw = smartw;
	}

	public String getJoinmessage() {
		return joinmessage;
	}

	public void setJoinmessage(String joinmessage) {
		this.joinmessage = joinmessage;
	}

	public List<String> getJoincommand() {
		return joincommand;
	}

	public void setJoincommand(List<String> joincommand) {
		this.joincommand = joincommand;
	}

	public List<String> getLeavecommand() {
		return leavecommand;
	}

	public void setLeavecommand(List<String> leavecommand) {
		this.leavecommand = leavecommand;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public int getVspoke() {
		return vspoke;
	}

	public void setVspoke(int vspoke) {
		this.vspoke = vspoke;
	}

	public int getRunnerchance() {
		return runnerchance;
	}

	public void setRunnerchance(int runnerchance) {
		this.runnerchance = runnerchance;
	}

	public int getSkellywavechance() {
		return skellywavechance;
	}

	public void setSkellywavechance(int skellywavechance) {
		this.skellywavechance = skellywavechance;
	}

	public int getWait() {
		return wait;
	}

	public void setWait(int wait) {
		this.wait = wait;
	}

	public int getDoorfindradius() {
		return doorfindradius;
	}

	public void setDoorfindradius(int doorfindradius) {
		this.doorfindradius = doorfindradius;
	}

	public int getLeavetimer() {
		return leavetimer;
	}

	public void setLeavetimer(int leavetimer) {
		this.leavetimer = leavetimer;
	}

	public int getMaxpoints() {
		return maxpoints;
	}

	public void setMaxpoints(int maxpoints) {
		this.maxpoints = maxpoints;
	}

	public double getDeathloss() {
		return deathloss;
	}

	public void setDeathloss(double deathloss) {
		this.deathloss = deathloss;
	}

	public double getDiffmulti() {
		return diffmulti;
	}

	public void setDiffmulti(double diffmulti) {
		this.diffmulti = diffmulti;
	}

	public double getDamagemulti() {
		return damagemulti;
	}

	public void setDamagemulti(double damagemulti) {
		this.damagemulti = damagemulti;
	}

	public Map<String, Integer> getCooldowncount() {
		return cooldowncount;
	}

	public void setCooldowncount(Map<String, Integer> cooldowncount) {
		this.cooldowncount = cooldowncount;
	}

	public Map<String, Location> getLightloc() {
		return lightloc;
	}

	public void setLightloc(Map<String, Location> lightloc) {
		this.lightloc = lightloc;
	}

	public List<ItemStack> getJoinitems() {
		return joinitems;
	}

	public void setJoinitems(List<ItemStack> joinitems) {
		this.joinitems = joinitems;
	}

	public List<ItemStack> getJoinarmor() {
		return joinarmor;
	}

	public void setJoinarmor(List<ItemStack> joinarmor) {
		this.joinarmor = joinarmor;
	}

	public Map<ItemStack, Double> getDrops() {
		return drops;
	}

	public void setDrops(Map<ItemStack, Double> drops) {
		this.drops = drops;
	}

	public List<ItemStack> getBoxitems() {
		return boxitems;
	}

	public void setBoxitems(List<ItemStack> boxitems) {
		this.boxitems = boxitems;
	}

	public Map<Location, String> getRoundFire() {
		return roundFire;
	}

	public void setRoundFire(Map<Location, String> roundFire) {
		this.roundFire = roundFire;
	}

	public Map<String, Map<Block, BlockState>> getChangedBlocks() {
		return changedBlocks;
	}

	public void setChangedBlocks(Map<String, Map<Block, BlockState>> changedBlocks) {
		this.changedBlocks = changedBlocks;
	}

	public Map<String, Map<Block, BlockState>> getPlacedBlocks() {
		return placedBlocks;
	}

	public void setPlacedBlocks(Map<String, Map<Block, BlockState>> placedBlocks) {
		this.placedBlocks = placedBlocks;
	}

	public List<Integer> getBlockbreak() {
		return blockbreak;
	}

	public void setBlockbreak(List<Integer> blockbreak) {
		this.blockbreak = blockbreak;
	}

	public List<Integer> getBlockplace() {
		return blockplace;
	}

	public void setBlockplace(List<Integer> blockplace) {
		this.blockplace = blockplace;
	}

	public Map<Location, String> getSpecialblocks() {
		return specialblocks;
	}

	public void setSpecialblocks(Map<Location, String> specialblocks) {
		this.specialblocks = specialblocks;
	}

	public Map<String, ItemStack[]> getInv() {
		return inv;
	}

	public void setInv(Map<String, ItemStack[]> inv) {
		this.inv = inv;
	}

	public Map<String, ItemStack[]> getInvarmor() {
		return invarmor;
	}

	public void setInvarmor(Map<String, ItemStack[]> invarmor) {
		this.invarmor = invarmor;
	}

	public Map<Integer, String> getZombies() {
		return zombies;
	}

	public void setZombies(Map<Integer, String> zombies) {
		this.zombies = zombies;
	}

	public Map<Integer, String> getFastzombies() {
		return fastzombies;
	}

	public void setFastzombies(Map<Integer, String> fastzombies) {
		this.fastzombies = fastzombies;
	}

	public Map<Integer, Map<String, Integer>> getZombiescore() {
		return zombiescore;
	}

	public void setZombiescore(Map<Integer, Map<String, Integer>> zombiescore) {
		this.zombiescore = zombiescore;
	}

	public static Map<String, String> getDead() {
		return dead;
	}

	public static void setDead(Map<String, String> dead) {
		ZombieSurvivalPlugin.dead = dead;
	}

	public Map<String, Location> getDeathPoints() {
		return deathPoints;
	}

	public void setDeathPoints(Map<String, Location> deathPoints) {
		this.deathPoints = deathPoints;
	}

	public Map<String, Boolean> getPerkend() {
		return perkend;
	}

	public void setPerkend(Map<String, Boolean> perkend) {
		this.perkend = perkend;
	}

	public static Map<String, String> getMaps() {
		return Maps;
	}

	public static void setMaps(Map<String, String> maps) {
		Maps = maps;
	}

	public Set<Location> getSigns() {
		return Signs;
	}

	public void setSigns(Set<Location> signs) {
		Signs = signs;
	}

	public Set<String> getVoted() {
		return voted;
	}

	public void setVoted(Set<String> voted) {
		this.voted = voted;
	}

	public Map<String, Integer> getWavemax() {
		return wavemax;
	}

	public void setWavemax(Map<String, Integer> wavemax) {
		this.wavemax = wavemax;
	}

	public Map<String, Integer> getAdd() {
		return add;
	}

	public void setAdd(Map<String, Integer> add) {
		this.add = add;
	}

	public Map<String, Integer> getRemove() {
		return remove;
	}

	public void setRemove(Map<String, Integer> remove) {
		this.remove = remove;
	}

	public int getTask() {
		return task;
	}

	public void setTask(int task) {
		this.task = task;
	}

	public int getTask2() {
		return task2;
	}

	public void setTask2(int task2) {
		this.task2 = task2;
	}

	public static Economy getEcon() {
		return econ;
	}

	public static void setEcon(Economy econ) {
		ZombieSurvivalPlugin.econ = econ;
	}

	public static Chat getChat() {
		return chat;
	}

	public static void setChat(Chat chat) {
		ZombieSurvivalPlugin.chat = chat;
	}

	public boolean isPoints() {
		return points;
	}

	public void setPoints(boolean points) {
		this.points = points;
	}

	public File getCustomConfigFile() {
		return customConfigFile;
	}

	public void setCustomConfigFile(File customConfigFile) {
		this.customConfigFile = customConfigFile;
	}

	public static Field getTargetSelector() {
		return targetSelector;
	}

	public static void setTargetSelector(Field targetSelector) {
		ZombieSurvivalPlugin.targetSelector = targetSelector;
	}

	public Locale getbLocale() {
		return bLocale;
	}

	public void setbLocale(Locale bLocale) {
		this.bLocale = bLocale;
	}

	public Collection<String> getEssentialswarps() {
		return essentialswarps;
	}

	public void setEssentialswarps(Collection<String> essentialswarps) {
		this.essentialswarps = essentialswarps;
	}

	public Location getForcewarplocation() {
		return forcewarplocation;
	}

	public void setForcewarplocation(Location forcewarplocation) {
		this.forcewarplocation = forcewarplocation;
	}

	public boolean isForcewarp() {
		return forcewarp;
	}

	public void setForcewarp(boolean forcewarp) {
		this.forcewarp = forcewarp;
	}

	public Essentials getEssentialsPlugin() {
		return essentialsPlugin;
	}

	public void setEssentialsPlugin(Essentials essentialsPlugin) {
		this.essentialsPlugin = essentialsPlugin;
	}

	public static void setInstance(ZombieSurvivalPlugin instance) {
		ZombieSurvivalPlugin.instance = instance;
	}

	public void setCustomConfig(FileConfiguration customConfig) {
		this.customConfig = customConfig;
	}
}
