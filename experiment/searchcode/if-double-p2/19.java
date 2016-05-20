package com.jordair.gmail.zombiesurvival;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Stats {
	public Plugin plugin;
	private static Map<String, Double> totalpoints = new HashMap<String, Double>();
	private static Map<String, Double> totalkills = new HashMap<String, Double>();
	private static Map<String, Double> totaldeaths = new HashMap<String, Double>();
	private static Map<String, Double> sessionpoints = new HashMap<String, Double>();
	private static Map<String, Double> sessionkills = new HashMap<String, Double>();
	private static Map<String, Double> sessiondeaths = new HashMap<String, Double>();
	private static Map<String, Map<String, Double>> classpoints = new HashMap<String, Map<String, Double>>();
	private static Map<String, Map<String, Double>> classkills = new HashMap<String, Map<String, Double>>();
	private static Map<String, Map<String, Double>> classdeaths = new HashMap<String, Map<String, Double>>();
	private static FileConfiguration SConfig = null;
	private static File SConfigFile = null;

	public void LoadStats() {
		for (String p : getSConfig().getStringList("Players")) {
			addDeaths(p, getSConfig().getDouble(p + ".td"));
			addKills(p, getSConfig().getDouble(p + ".tk"));
			addPoints(p, getSConfig().getDouble(p + ".tp"));
		}
	}

	public static double getTotalPoints(String player) {
		if (totalpoints.get(player) != null) { return ((Double) totalpoints.get(player)).doubleValue(); }
		return 0.0D;
	}

	public static double getTotalKills(String player) {
		if (totalkills.get(player) != null) { return ((Double) totalkills.get(player)).doubleValue(); }
		return 0.0D;
	}

	public static double getTotalDeaths(String player) {
		if (totaldeaths.get(player) != null) { return ((Double) totaldeaths.get(player)).doubleValue(); }
		return 0.0D;
	}

	public static double getSesPoints(String player) {
		if (sessionpoints.get(player) != null) { return ((Double) sessionpoints.get(player)).doubleValue(); }
		return 0.0D;
	}

	public static double getSesKills(String player) {
		if (sessionkills.get(player) != null) { return ((Double) sessionkills.get(player)).doubleValue(); }
		return 0.0D;
	}

	public static double getSesDeaths(String player) {
		if (sessiondeaths.get(player) != null) { return ((Double) sessiondeaths.get(player)).doubleValue(); }
		return 0.0D;
	}

	public static double getClassPoints(String player) {
		Player p = Bukkit.getPlayer(player);
		String cls = Classes.getUserClass(p);
		if ((classpoints.get(cls) != null) && (((Map<?, ?>) classpoints.get(cls)).get(PlayerMethods.playerGame(p)) != null)) { return ((Double) ((Map<?, ?>) classpoints
				.get(cls)).get(PlayerMethods.playerGame(p))).doubleValue(); }
		return 0.0D;
	}

	public static double getClassKills(String player) {
		Player p = Bukkit.getPlayer(player);
		String cls = Classes.getUserClass(p);
		if ((classkills.get(cls) != null) && (((Map<?, ?>) classkills.get(cls)).get(PlayerMethods.playerGame(p)) != null)) { return ((Double) ((Map<?, ?>) classkills
				.get(cls)).get(PlayerMethods.playerGame(p))).doubleValue(); }
		return 0.0D;
	}

	public static double getClassDeaths(String player) {
		Player p = Bukkit.getPlayer(player);
		String cls = Classes.getUserClass(p);
		if ((classdeaths.get(cls) != null) && (((Map<?, ?>) classdeaths.get(cls)).get(PlayerMethods.playerGame(p)) != null)) { return ((Double) ((Map<?, ?>) classdeaths
				.get(cls)).get(PlayerMethods.playerGame(p))).doubleValue(); }
		return 0.0D;
	}

	public static String topClass(String map) {
		double initp = 0.0D;
		String tcls = "ERROR";
		for (String cls : Classes.getAllClasses()) {
			if ((classpoints.get(cls) != null) && (((Map<?, ?>) classpoints.get(cls)).get(map) != null)
					&& (((Double) ((Map<?, ?>) classpoints.get(cls)).get(map)).doubleValue() > initp)) {
				initp = ((Double) ((Map<?, ?>) classpoints.get(cls)).get(map)).doubleValue();
				tcls = cls;
			}
		}

		return tcls;
	}

	public static Set<String> getPlayers() {
		return totalpoints.keySet();
	}

	public static void addPoints(String player, double points) {
		if (totalpoints.get(player) != null) {
			double p1 = ((Double) totalpoints.get(player)).doubleValue();
			double p2 = p1 + points;
			totalpoints.put(player, p2);
		} else {
			totalpoints.put(player, points);
		}
		if (sessionpoints.get(player) != null) {
			double p1 = ((Double) sessionpoints.get(player)).doubleValue();
			double p2 = p1 + points;
			sessionpoints.put(player, p2);
		} else {
			sessionpoints.put(player, points);
		}
		Player p = Bukkit.getPlayer(player);
		if ((p != null) && (Classes.isClassed(p))) {
			String cls = Classes.getUserClass(p);
			if ((classpoints.get(cls) != null) && (((Map<?, ?>) classpoints.get(cls)).get(PlayerMethods.playerGame(p)) != null)) {
				double p1 = ((Double) ((Map<?, ?>) classpoints.get(cls)).get(PlayerMethods.playerGame(p))).doubleValue();
				double p2 = p1 + points;
				((Map<String, Double>) classpoints.get(cls)).put(PlayerMethods.playerGame(p), p2);
			} else {
				classpoints.put(cls, new HashMap<String, Double>());
				((Map<String, Double>) classpoints.get(cls)).put(PlayerMethods.playerGame(p), points);
			}
		}
	}

	public static void addKills(String player, double points) {
		if (totalkills.get(player) != null) {
			double p1 = ((Double) totalkills.get(player)).doubleValue();
			double p2 = p1 + points;
			totalkills.put(player, p2);
		} else {
			totalkills.put(player, points);
		}
		if (sessionkills.get(player) != null) {
			double p1 = ((Double) sessionkills.get(player)).doubleValue();
			double p2 = p1 + points;
			sessionkills.put(player, p2);
		} else {
			sessionkills.put(player, points);
		}
		Player p = Bukkit.getPlayer(player);
		if ((p != null) && (Classes.isClassed(p))) {
			String cls = Classes.getUserClass(p);
			if ((classkills.get(cls) != null) && (((Map<?, ?>) classkills.get(cls)).get(PlayerMethods.playerGame(p)) != null)) {
				double p1 = ((Double) ((Map<?, ?>) classkills.get(cls)).get(PlayerMethods.playerGame(p))).doubleValue();
				double p2 = p1 + points;
				((Map<String, Double>) classkills.get(cls)).put(PlayerMethods.playerGame(p), p2);
			} else {
				classkills.put(cls, new HashMap<String, Double>());
				((Map<String, Double>) classkills.get(cls)).put(PlayerMethods.playerGame(p), points);
			}
		}
	}

	public static void addDeaths(String player, double points) {
		if (totaldeaths.get(player) != null) {
			double p1 = ((Double) totaldeaths.get(player)).doubleValue();
			double p2 = p1 + points;
			totaldeaths.put(player, p2);
		} else {
			totaldeaths.put(player, points);
		}
		if (sessiondeaths.get(player) != null) {
			double p1 = ((Double) sessiondeaths.get(player)).doubleValue();
			double p2 = p1 + points;
			sessiondeaths.put(player, p2);
		} else {
			sessiondeaths.put(player, points);
		}
		Player p = Bukkit.getPlayer(player);
		if ((p != null) && (Classes.isClassed(p))) {
			String cls = Classes.getUserClass(p);
			if ((classdeaths.get(cls) != null) && (((Map<?, ?>) classdeaths.get(cls)).get(PlayerMethods.playerGame(p)) != null)) {
				double p1 = ((Double) ((Map<?, ?>) classdeaths.get(cls)).get(PlayerMethods.playerGame(p))).doubleValue();
				double p2 = p1 + points;
				((Map<String, Double>) classdeaths.get(cls)).put(PlayerMethods.playerGame(p), p2);
			} else {
				classdeaths.put(cls, new HashMap<String, Double>());
				((Map<String, Double>) classdeaths.get(cls)).put(PlayerMethods.playerGame(p), points);
			}
		}
	}

	public static void clearClassStats(String map) {
		for (String cls : Classes.getAllClasses()) {
			if (classpoints.get(cls) != null) {
				((Map<?, ?>) classpoints.get(cls)).remove(map);
			}
			if (classdeaths.get(cls) != null) {
				((Map<?, ?>) classdeaths.get(cls)).remove(map);
			}
			if (classkills.get(cls) != null)
				((Map<?, ?>) classkills.get(cls)).remove(map);
		}
	}

	public static void setPoints(String player, double p) {
		sessionpoints.put(player, Double.valueOf(p));
	}

	public static void setKills(String player, double p) {
		sessionkills.put(player, Double.valueOf(p));
	}

	public static void setDeaths(String player, double p) {
		sessiondeaths.put(player, Double.valueOf(p));
	}

	public static void clear() {
		sessiondeaths.clear();
		sessionkills.clear();
		sessionpoints.clear();
	}

	public static void removeSplayer(String player) {
		sessiondeaths.remove(player);
		sessionkills.remove(player);
		sessionpoints.remove(player);
	}

	public static void removeTplayer(String player) {
		totaldeaths.remove(player);
		totalkills.remove(player);
		totalpoints.remove(player);
	}

	public static void removeSplayerPoints(String player) {
		sessionpoints.remove(player);
	}

	public static void removeSplayerKills(String player) {
		sessionkills.remove(player);
	}

	public static void removeSplayerDeaths(String player) {
		sessiondeaths.remove(player);
	}

	public static void removeTplayerPoints(String player) {
		totalpoints.remove(player);
	}

	public static void removeTplayerKills(String player) {
		totalkills.remove(player);
	}

	public static void removeTplayerDeaths(String player) {
		totaldeaths.remove(player);
	}

	public static String topScorePlayer() {
		double initp = 0.0D;
		String user = "ERROR";
		for (String p : totalpoints.keySet()) {
			if (((Double) totalpoints.get(p)).doubleValue() > initp) {
				initp = ((Double) totalpoints.get(p)).doubleValue();
				user = p;
			}
		}
		return user;
	}

	public static String topKillsPlayer() {
		double initp = 0.0D;
		String user = "ERROR";
		for (String p : totalkills.keySet()) {
			if (((Double) totalkills.get(p)).doubleValue() > initp) {
				initp = ((Double) totalkills.get(p)).doubleValue();
				user = p;
			}
		}
		return user;
	}

	public static String topDeathsPlayer() {
		double initp = 0.0D;
		String user = "ERROR";
		for (String p : totaldeaths.keySet()) {
			if (((Double) totaldeaths.get(p)).doubleValue() > initp) {
				initp = ((Double) totaldeaths.get(p)).doubleValue();
				user = p;
			}
		}
		return user;
	}

	public static double topScore() {
		return ((Double) totalpoints.get(topScorePlayer())).doubleValue();
	}

	public static double topKills() {
		return ((Double) totalkills.get(topKillsPlayer())).doubleValue();
	}

	public static double topDeaths() {
		return ((Double) totaldeaths.get(topDeathsPlayer())).doubleValue();
	}

	public void saveSConfig() {
		if ((SConfig == null) || (SConfigFile == null))
			return;
		try {
			getSConfig().save(SConfigFile);
		} catch (IOException ex) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + SConfigFile, ex);
		}
	}

	public FileConfiguration getSConfig() {
		if (SConfig == null) {
			reloadSConfig();
		}
		return SConfig;
	}

	public void reloadSConfig() {
		if (SConfigFile == null) {
			SConfigFile = new File(this.plugin.getDataFolder(), "stats.yml");
		}
		SConfig = YamlConfiguration.loadConfiguration(SConfigFile);
		InputStream defConfigStream = this.plugin.getResource("stats.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			SConfig.setDefaults(defConfig);
		}
	}

	public void saveStats() {
		List<String> players = new ArrayList<String>();
		for (String p : getPlayers()) {
			players.add(p);
			getSConfig().set(p + ".td", Double.valueOf(getTotalDeaths(p)));
			getSConfig().set(p + ".tk", Double.valueOf(getTotalKills(p)));
			getSConfig().set(p + ".tp", Double.valueOf(getTotalPoints(p)));
		}
		getSConfig().set("Players", players);
		saveSConfig();
	}

	public void Destroy() {
		try {
			finalize();
		} catch (Throwable e) {
			this.plugin.getLogger().warning("Failed to destroy class.");
		}
	}
}
