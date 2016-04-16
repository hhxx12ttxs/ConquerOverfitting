/*
 * Copyright 2012 Andrew Bashore
 * This file is part of GeoBot.
 * 
 * GeoBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GeoBot is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GeoBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.bashtech.geobot;

import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Channel {
	public JSONObject config;

	private String channel;
	private String twitchname;

	boolean staticChannel;
	private HashMap<String, String> commands = new HashMap<String, String>();
	private HashMap<String, Integer> commandsRestrictions = new HashMap<String, Integer>();
	private HashMap<String, Integer> commandCounts = new HashMap<String, Integer>();
	private ArrayList<String> quotes = new ArrayList<String>();
	HashMap<String, RepeatCommand> commandsRepeat = new HashMap<String, RepeatCommand>();
	HashMap<String, ScheduledCommand> commandsSchedule = new HashMap<String, ScheduledCommand>();
	List<Pattern> autoReplyTrigger = new ArrayList<Pattern>();
	List<String> autoReplyResponse = new ArrayList<String>();
	private boolean filterCaps;
	private int filterCapsPercent;
	private int filterCapsMinCharacters;
	private int filterCapsMinCapitals;
	private boolean filterLinks;
	private boolean filterOffensive;
	private boolean filterEmotes;
	private boolean filterSymbols;
	private int filterSymbolsPercent;
	private int filterSymbolsMin;
	private int filterEmotesMax;
	private boolean filterEmotesSingle;
	private int filterMaxLength;
	private String topic;
	private int topicTime;
	private Set<String> regulars = new HashSet<String>();
	// private Set<String> subscribers = new HashSet<String>();
	private Set<String> moderators = new HashSet<String>();
	Set<String> tagModerators = new HashSet<String>();
	private Set<String> owners = new HashSet<String>();
	private Set<String> raidWhitelist = new HashSet<String>();
	private Set<String> permittedUsers = new HashSet<String>();
	private ArrayList<String> permittedDomains = new ArrayList<String>();
	public boolean useTopic = true;
	public boolean useFilters = true;
	private Poll currentPoll;
	private Giveaway currentGiveaway;
	private boolean enableThrow;
	private boolean signKicks;
	private boolean announceJoinParts;
	private String lastfm;
	private String steamID;
	private int mode; // 0: Admin/owner only; 1: Mod Only; 2: Everyone; -1
						// Special mode to admins to use for channel moderation

	Raffle raffle;
	public boolean logChat;
	public long messageCount;
	public int commercialLength;
	String clickToTweetFormat;
	private boolean filterColors;
	private boolean filterMe;
	private Set<String> offensiveWords = new HashSet<String>();
	private List<Pattern> offensiveWordsRegex = new LinkedList<Pattern>();
	Map<String, EnumMap<FilterType, Integer>> warningCount;
	Map<String, Long> warningTime;
	private int timeoutDuration;
	private boolean enableWarnings;
	Map<String, Long> commandCooldown;
	Set<WebSocket> wsSubscribers = new HashSet<WebSocket>();
	String prefix;
	String emoteSet;
	boolean subscriberRegulars;
	String lastSong = "";
	long songUpdated = System.currentTimeMillis();
	private boolean wpOn;
	private long sinceWp = System.currentTimeMillis();
	private int wpCount = 0;
	private String bullet = "#!";
	private String gamerTag;

	private JSONObject defaults = new JSONObject();

	private int cooldown = 0;

	private int maxViewers = 0;
	private boolean streamUp = false;
	private int streamMax = 0;
	private int streamNumber = 0;
	private int runningMaxViewers = 0;

	private int punishCount = 0;
	private int updateDelay = 120;

	private long sincePunish = System.currentTimeMillis();
	private String maxviewerDate = new java.util.Date().toString();

	public boolean subsRegsMinusLinks;

	public boolean active;
	private static Timer commercial;
	private int lastStrawpoll;
	// private long timeAliveStart = System.currentTimeMillis();
	private boolean streamAlive = false;
	private boolean urbanEnabled = false;
	private ArrayList<String> ignoredUsers = new ArrayList<String>();
	private long extraLifeID;

	public Channel(String name) {
		channel = name;
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(channel + ".json"));
			config = (JSONObject) obj;
		} catch (Exception e) {
			System.out.println("Generating new config for " + channel);
			config = new JSONObject();
		}

		loadProperties(name);
		warningCount = new HashMap<String, EnumMap<FilterType, Integer>>();
		warningTime = new HashMap<String, Long>();
		commandCooldown = new HashMap<String, Long>();

		twitchname = channel.substring(1);

		// Timer delayer = new Timer("start", true);
		// delayer.schedule(new java.util.TimerTask() {
		// @Override
		// public void run() {
		// startCheckers();
		//
		// }
		// }, 30000);

	}

	public Channel(String name, int mode) {
		this(name);
		setMode(mode);
	}

	// public void startCheckers() {
	// ScheduledExecutorService service =
	// Executors.newScheduledThreadPool(1);
	// AsyncRunner uptimeChecker = new AsyncRunner(twitchname, 1);
	// service.scheduleAtFixedRate(uptimeChecker, 0, 150, TimeUnit.SECONDS);
	// AsyncRunner lastFMChecker = new AsyncRunner(twitchname, 2);
	// service.scheduleAtFixedRate(lastFMChecker, 0, 90, TimeUnit.SECONDS);

	// Timer isLiveChecker = new Timer("islivechecker", true);
	// isLiveChecker.scheduleAtFixedRate(new java.util.TimerTask() {
	// @Override
	// public void run() {
	// handleAsyncIsLive(JSONUtil.krakenIsLive(twitchname));
	//
	// }
	// }, 0, 90000);
	//
	// Timer lastFMChecker = new Timer("lastfmchecker");
	// lastFMChecker.scheduleAtFixedRate(new java.util.TimerTask() {
	// @Override
	// public void run() {
	// updateSong(JSONUtil.lastFM(getLastfm()));
	//
	// }
	// }, 0, 45000);

	// }

	// public void handleAsyncIsLive(boolean output) {
	//
	// if (output && !streamAlive) {
	// timeAliveStart = System.currentTimeMillis();
	// config.put("timeAliveStart", timeAliveStart);
	// streamAlive = true;
	// alive(twitchname);
	// config.put("streamAlive", streamAlive);
	// } else if (!output && streamAlive) {
	// dead(twitchname);
	// streamAlive = false;
	// config.put("streamAlive", streamAlive);
	// }
	// saveConfig();
	// }
	//
	// public String getUptime() {
	// if (streamAlive) {
	// long deltaTime = System.currentTimeMillis() - timeAliveStart;
	// return getDurationBreakdown(deltaTime);
	// } else
	// return null;
	//
	// }

	public String getChannel() {
		return channel;
	}

	public String getTwitchName() {
		return twitchname;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix.charAt(0) + "";

		config.put("commandPrefix", this.prefix);
		saveConfig();
	}

	public void setLastStrawpoll(int newId) {
		lastStrawpoll = newId;
	}

	public int getLastStrawpoll() {
		return lastStrawpoll;
	}

	public boolean getWp() {
		return wpOn;
	}

	public void setStreamCount(int newCount) {
		streamNumber = newCount;
		config.put("streamCount", streamNumber);
		saveConfig();
	}

	public void setWp(boolean state) {
		wpOn = state;
		config.put("wpTimer", wpOn);
		saveConfig();
	}

	public long timeSinceSaid() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sinceWp) / 1000L;
		sinceWp = now;
		config.put("sinceWp", sinceWp);
		saveConfig();
		return (differenceInSeconds);
	}

	public long timeSinceNoUpdate() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sinceWp) / 1000L;

		return (differenceInSeconds);
	}

	public long timeSincePunished() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sincePunish) / 1000L;

		return (differenceInSeconds);
	}

	public void setBullet(String newBullet) {
		bullet = newBullet;
		config.put("bullet", newBullet);
		saveConfig();
	}

	public String getChannelBullet() {
		return bullet;
	}

	public void increaseWpCount() {
		wpCount++;
		config.put("wpCount", wpCount);
		saveConfig();
	}

	public int getWpCount() {
		return wpCount;
	}

	public String getEmoteSet() {
		return emoteSet;
	}

	public void setEmoteSet(String emoteSet) {
		this.emoteSet = emoteSet;

		config.put("emoteSet", emoteSet);
		saveConfig();
	}

	public boolean getSubsRegsMinusLinks() {
		return subsRegsMinusLinks;
	}

	public void setSubsRegsMinusLinks(boolean on) {

		// subscribers.clear();

		subsRegsMinusLinks = on;
		config.put("subsRegsMinusLinks", subsRegsMinusLinks);
		saveConfig();

	}

	public boolean getSubscriberRegulars() {
		return subscriberRegulars;
	}

	public void setSubscriberRegulars(boolean subscriberRegulars) {

		// subscribers.clear();

		this.subscriberRegulars = subscriberRegulars;
		config.put("subscriberRegulars", subscriberRegulars);
		saveConfig();
	}

	// ##############################################################
	public int addQuote(String quote) {

		if (quotes.contains(quote)) {
			return -1;
		} else {
			quotes.add(quote);
			JSONArray quotesArray = new JSONArray();
			String quotesString = "";
			for (int i = 0; i < quotes.size(); i++) {
				quotesArray.add(quotes.get(i));
			}
			config.put("quotes", quotesArray);
			saveConfig();
			return quotes.indexOf(quote);
		}
	}

	public int getQuoteSize() {
		return quotes.size();
	}

	public String getQuote(int index) {
		if (index < quotes.size())
			return quotes.get(index);
		else
			return "No quote at requested index.";
	}

	public boolean deleteQuote(int index) {
		if (index > quotes.size() - 1)
			return false;
		else {
			quotes.remove(index);
			JSONArray quotesArray = new JSONArray();
			String quotesString = "";
			for (int i = 0; i < quotes.size(); i++) {
				quotesArray.add(quotes.get(i));
			}
			config.put("quotes", quotesArray);
			saveConfig();
			return true;
		}
	}

	public int getQuoteIndex(String quote) {
		if (quotes.contains(quote))
			return quotes.indexOf(quote);
		else
			return -1;
	}

	// ################################################################
	public String getCommand(String key) {
		key = key.toLowerCase();

		if (commands.containsKey(key)) {
			return commands.get(key);
		} else {
			return null;
		}
	}

	public void setCommand(String key, String command) {
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("Key: " + key);
		command = command.replaceAll(",,", "");

		if (key.length() < 1)
			return;

		if (commands.containsKey(key)) {

			commands.remove(key);
			commands.put(key, command);

		} else {
			commands.put(key, command);
			commandCounts.put(key, 0);
		}

		saveCommands();

	}

	public void removeCommand(String key) {
		if (commands.containsKey(key)) {
			commands.remove(key);
			commandsRestrictions.remove(key);
			commandCounts.remove(key);

			saveCommands();

		}

	}

	public void saveCommands() {
		JSONArray commandsArr = new JSONArray();

		Iterator itr = commands.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject commandObj = new JSONObject();
			commandObj.put("key", pairs.getKey());
			commandObj.put("value", pairs.getValue());
			if (commandsRestrictions.containsKey(pairs.getKey())) {
				commandObj.put("restriction",
						commandsRestrictions.get(pairs.getKey()));
			} else {
				commandObj.put("restriction", 1);
			}
			commandObj.put("count", commandCounts.get(pairs.getKey()));
			commandsArr.add(commandObj);

		}

		config.put("commands", commandsArr);
		saveConfig();
	}

	public void increaseCommandCount(String commandName) {
		commandName = commandName.toLowerCase();
		if (commandCounts.containsKey(commandName)) {
			int currentCount = commandCounts.get(commandName);
			currentCount++;
			commandCounts.put(commandName, currentCount);
		}
		saveCommands();

	}

	public int getCurrentCount(String commandName) {
		commandName = commandName.toLowerCase();
		if (commandCounts.containsKey(commandName)) {
			int currentCount = commandCounts.get(commandName);
			return currentCount;
		} else
			return -1;
	}

	public boolean setCommandsRestriction(String command, int level) {
		command = command.toLowerCase();

		if (!commands.containsKey(command))
			return false;

		commandsRestrictions.put(command, level);

		saveCommands();

		return true;
	}

	public boolean checkCommandRestriction(String command, int level) {
		System.out.println("Checking command: " + command + " User level: "
				+ level);
		if (!commandsRestrictions.containsKey(command.toLowerCase()))
			return true;

		if (level >= commandsRestrictions.get(command.toLowerCase()))
			return true;

		return false;
	}

	// public void saveCommandRestrictions() {
	// String commandRestrictionsString = "";
	// JSONArray commandRestrictionsKey = new JSONArray();
	// JSONArray commandRestrictionsValue = new JSONArray();
	//
	// Iterator itr = commandsRestrictions.entrySet().iterator();
	//
	// while (itr.hasNext()) {
	// Map.Entry pairs = (Map.Entry) itr.next();
	// commandRestrictionsKey.add(pairs.getKey());
	// commandRestrictionsValue.add(pairs.getValue());
	//
	// }
	//
	// config.put("commandRestrictionsKey", commandRestrictionsKey);
	// config.put("commandRestrictionsValue", commandRestrictionsValue);
	// saveConfig();
	// }

	public void setRepeatCommand(String key, int delay, int diff) {
		key = key.toLowerCase();
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).timer.cancel();
			commandsRepeat.remove(key);
			RepeatCommand rc = new RepeatCommand(channel, key, delay, diff,
					true);
			commandsRepeat.put(key, rc);
		} else {
			RepeatCommand rc = new RepeatCommand(channel, key, delay, diff,
					true);
			commandsRepeat.put(key, rc);
		}

		saveRepeatCommands();
	}

	public void removeRepeatCommand(String key) {
		key = key.toLowerCase();
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).timer.cancel();
			commandsRepeat.remove(key);

			saveRepeatCommands();
		}
	}

	public void setRepeatCommandStatus(String key, boolean status) {
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).setStatus(status);
			saveRepeatCommands();
		}
	}

	private void saveRepeatCommands() {
		JSONArray repeatedCommands = new JSONArray();
		Iterator itr = commandsRepeat.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject repeatObj = new JSONObject();
			repeatObj.put("name", pairs.getKey());
			repeatObj.put("delay", ((RepeatCommand) pairs.getValue()).delay);
			repeatObj.put("messageDifference",
					((RepeatCommand) pairs.getValue()).messageDifference);
			repeatObj.put("active", ((RepeatCommand) pairs.getValue()).active);
			repeatedCommands.add(repeatObj);

		}

		config.put("repeatedCommands", repeatedCommands);
		saveConfig();
	}

	public void setScheduledCommand(String key, String pattern, int diff) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).s.stop();
			commandsSchedule.remove(key);
			ScheduledCommand rc = new ScheduledCommand(channel, key, pattern,
					diff, true);
			commandsSchedule.put(key, rc);
		} else {
			ScheduledCommand rc = new ScheduledCommand(channel, key, pattern,
					diff, true);
			commandsSchedule.put(key, rc);
		}

		saveScheduledCommands();

	}

	public void removeScheduledCommand(String key) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).s.stop();
			commandsSchedule.remove(key);

			saveScheduledCommands();
		}
	}

	public void setScheduledCommandStatus(String key, boolean status) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).setStatus(status);
			saveScheduledCommands();
		}
	}

	private void saveScheduledCommands() {

		JSONArray scheduledCommands = new JSONArray();

		Iterator itr = commandsSchedule.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject scheduleObj = new JSONObject();
			scheduleObj.put("name", pairs.getKey());
			scheduleObj.put("pattern",
					((ScheduledCommand) pairs.getValue()).pattern);
			scheduleObj.put("messageDifference",
					((ScheduledCommand) pairs.getValue()).messageDifference);
			scheduleObj.put("active",
					((ScheduledCommand) pairs.getValue()).active);
			scheduledCommands.add(scheduleObj);

		}

		config.put("scheduledCommands", scheduledCommands);
		saveConfig();
	}

	public ArrayList<String> getCommandList() {

		ArrayList<String> sorted = new ArrayList<String>(commands.keySet());

		java.util.Collections.sort(sorted);
		return sorted;

	}

	public void addAutoReply(String trigger, String response) {
		trigger = trigger.replaceAll(",,", "");
		response.replaceAll(",,", "");

		if (!trigger.startsWith("REGEX:")) {
			String[] parts = trigger.replaceFirst("^\\*", "")
					.replaceFirst("\\*$", "").split("\\*");

			// Only apply leading & trailing any if an one was requested
			boolean trailingAny = trigger.endsWith("*");
			if (trigger.startsWith("*"))
				trigger = ".*";
			else
				trigger = "";

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].length() < 1)
					continue;

				trigger += Pattern.quote(parts[i]);
				if (i != parts.length - 1)
					trigger += ".*";
			}

			if (trailingAny)
				trigger += ".*";

		} else {
			trigger = trigger.replaceAll("REGEX:", "");
		}

		System.out.println("Final: " + trigger);
		autoReplyTrigger
				.add(Pattern.compile(trigger, Pattern.CASE_INSENSITIVE));
		autoReplyResponse.add(response);

		saveAutoReply();
	}

	public boolean removeAutoReply(int pos) {
		pos = pos - 1;

		if (pos > autoReplyTrigger.size() - 1)
			return false;

		autoReplyTrigger.remove(pos);
		autoReplyResponse.remove(pos);

		saveAutoReply();

		return true;
	}

	private void saveAutoReply() {
		JSONArray triggerString = new JSONArray();
		JSONArray responseString = new JSONArray();
		JSONArray autoReplies = new JSONArray();

		for (int i = 0; i < autoReplyTrigger.size(); i++) {
			JSONObject autoreplyObj = new JSONObject();
			autoreplyObj.put("trigger", autoReplyTrigger.get(i).toString());
			autoreplyObj.put("response", autoReplyResponse.get(i).toString());
			autoReplies.add(autoreplyObj);
		}

		config.put("autoReplies", autoReplies);
		saveConfig();
	}

	// #####################################################

	public String getTopic() {
		return topic;
	}

	public void setTopic(String s) {
		topic = s;
		config.put("topic", topic);
		topicTime = (int) (System.currentTimeMillis() / 1000);
		config.put("topicTime", topicTime);
		saveConfig();
	}

	public void updateGame(String game) throws IOException {
		System.out.println(BotManager.putRemoteData(
				"https://api.twitch.tv/kraken/channels/"
						+ this.channel.substring(1),
				"{\"channel\": {\"game\": \"" + JSONObject.escape(game)
						+ "\"}}"));
	}

	public void updateStatus(String status) throws IOException {
		System.out.println(BotManager.putRemoteData(
				"https://api.twitch.tv/kraken/channels/"
						+ this.channel.substring(1),
				"{\"channel\": {\"status\": \"" + JSONObject.escape(status)
						+ "\"}}"));
	}

	public String getTopicTime() {
		int difference = (int) (System.currentTimeMillis() / 1000) - topicTime;
		String returnString = "";

		if (difference >= 86400) {
			int days = (int) (difference / 86400);
			returnString += days + "d ";
			difference -= days * 86400;
		}
		if (difference >= 3600) {
			int hours = (int) (difference / 3600);
			returnString += hours + "h ";
			difference -= hours * 3600;
		}

		int seconds = (int) (difference / 60);
		returnString += seconds + "m";
		difference -= seconds * 60;

		return returnString;
	}

	// #####################################################

	public int getFilterSymbolsMin() {
		return filterSymbolsMin;
	}

	public int getFilterSymbolsPercent() {
		return filterSymbolsPercent;
	}

	public void setFilterSymbolsMin(int symbols) {
		filterSymbolsMin = symbols;
		config.put("filterSymbolsMin", filterSymbolsMin);
		saveConfig();
	}

	public void setFilterSymbolsPercent(int symbols) {
		filterSymbolsPercent = symbols;
		config.put("filterSymbolsPercent", filterSymbolsPercent);
		saveConfig();
	}

	public boolean getFilterCaps() {
		return filterCaps;
	}

	public int getfilterCapsPercent() {
		return filterCapsPercent;
	}

	public int getfilterCapsMinCharacters() {
		return filterCapsMinCharacters;
	}

	public int getfilterCapsMinCapitals() {
		return filterCapsMinCapitals;
	}

	public void setFilterCaps(boolean caps) {
		filterCaps = caps;
		config.put("filterCaps", filterCaps);
		saveConfig();
	}

	public void setfilterCapsPercent(int caps) {
		filterCapsPercent = caps;
		config.put("filterCapsPercent", filterCapsPercent);
		saveConfig();
	}

	public void setfilterCapsMinCharacters(int caps) {
		filterCapsMinCharacters = caps;
		config.put("filterCapsMinCharacters", filterCapsMinCharacters);
		saveConfig();
	}

	public void setfilterCapsMinCapitals(int caps) {
		filterCapsMinCapitals = caps;
		config.put("filterCapsMinCapitals", filterCapsMinCapitals);
		saveConfig();
	}

	public void setFilterLinks(boolean links) {
		filterLinks = links;
		config.put("filterLinks", links);
		saveConfig();
	}

	public boolean getFilterLinks() {
		return filterLinks;
	}

	public void setFilterOffensive(boolean option) {
		filterOffensive = option;
		config.put("filterOffensive", option);
		saveConfig();
	}

	public boolean getFilterOffensive() {
		return filterOffensive;
	}

	public void setFilterEmotes(boolean option) {
		filterEmotes = option;
		config.put("filterEmotes", option);
		saveConfig();
	}

	public boolean getFilterEmotes() {
		return filterEmotes;
	}

	public void setFilterSymbols(boolean option) {
		filterSymbols = option;
		config.put("filterSymbols", option);
		saveConfig();
	}

	public boolean getFilterSymbols() {
		return filterSymbols;
	}

	public int getFilterMax() {
		return filterMaxLength;
	}

	public void setFilterMax(int option) {
		filterMaxLength = option;
		config.put("filterMaxLength", option);
		saveConfig();
	}

	public void setFilterEmotesMax(int option) {
		filterEmotesMax = option;
		config.put("filterEmotesMax", option);
		saveConfig();
	}

	public int getFilterEmotesMax() {
		return filterEmotesMax;
	}

	public boolean getFilterEmotesSingle() {
		return filterEmotesSingle;
	}

	public void setFilterEmotesSingle(boolean filterEmotesSingle) {
		this.filterEmotesSingle = filterEmotesSingle;

		config.put("filterEmotesSingle", filterEmotesSingle);
		saveConfig();
	}

	public void setAnnounceJoinParts(boolean bol) {
		announceJoinParts = bol;
		config.put("announceJoinParts", bol);
		saveConfig();
	}

	public boolean getAnnounceJoinParts() {
		return announceJoinParts;
	}

	public void setFilterColor(boolean option) {
		filterColors = option;
		config.put("filterColors", option);
		saveConfig();
	}

	public boolean getFilterColor() {
		return filterColors;
	}

	public void setFilterMe(boolean option) {
		filterMe = option;
		config.put("filterMe", option);
		saveConfig();
	}

	public boolean getFilterMe() {
		return filterMe;
	}

	public void setEnableWarnings(boolean option) {
		enableWarnings = option;
		config.put("enableWarnings", option);
		saveConfig();
	}

	public boolean getEnableWarnings() {
		return enableWarnings;
	}

	public void setTimeoutDuration(int option) {
		timeoutDuration = option;
		config.put("timeoutDuration", option);
		saveConfig();
	}

	public int getTimeoutDuration() {
		return timeoutDuration;
	}

	// ###################################################

	public boolean isRegular(String name) {
		synchronized (regulars) {
			for (String s : regulars) {
				if (s.equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addRegular(String name) {
		synchronized (regulars) {
			regulars.add(name.toLowerCase());

		}

		JSONArray regularsArray = new JSONArray();

		synchronized (regulars) {
			for (String s : regulars) {
				regularsArray.add(s);
			}
		}

		config.put("regulars", regularsArray);
		saveConfig();
	}

	public void removeRegular(String name) {
		synchronized (regulars) {
			if (regulars.contains(name.toLowerCase()))
				regulars.remove(name.toLowerCase());
		}
		JSONArray regularsArray = new JSONArray();

		synchronized (regulars) {
			for (String s : regulars) {
				regularsArray.add(s);
			}
		}

		config.put("regulars", regularsArray);
		saveConfig();
	}

	public Set<String> getRegulars() {
		return regulars;
	}

	public void permitUser(String name) {
		synchronized (permittedUsers) {
			if (permittedUsers.contains(name.toLowerCase()))
				return;
		}

		synchronized (permittedUsers) {
			permittedUsers.add(name.toLowerCase());
		}
	}

	public boolean linkPermissionCheck(String name) {

		if (this.isRegular(name)) {
			return true;
		}

		synchronized (permittedUsers) {
			if (permittedUsers.contains(name.toLowerCase())) {
				permittedUsers.remove(name.toLowerCase());
				return true;
			}
		}

		return false;
	}

	// public boolean isSubscriber(String name) {
	// if (subscribers.contains(name.toLowerCase()))
	// return true;
	//
	// if (emoteSet.length() > 0)
	// if (BotManager.getInstance().checkEmoteSetMapping(name, emoteSet))
	// return true;
	// return false;
	// }
	public void addRaidWhitelist(String name) {
		raidWhitelist.add(name.toLowerCase());
		JSONArray raidWhitelistArray = new JSONArray();
		for (String s : raidWhitelist) {
			raidWhitelistArray.add(s);
		}
		config.put("raidWhitelist", raidWhitelistArray);
		saveConfig();
	}

	public void setGamertag(String gamerTag) {
		this.gamerTag = gamerTag.replaceAll(" ", "+");
		config.put("gamerTag", this.gamerTag);
		saveConfig();
	}

	public String getGamerTag() {
		return (gamerTag);
	}

	public void deleteRaidWhitelist(String name) {
		raidWhitelist.remove(name);
		JSONArray raidWhitelistArray = new JSONArray();
		for (String s : raidWhitelist) {
			raidWhitelistArray.add(s);
		}
		config.put("raidWhitelist", raidWhitelistArray);
		saveConfig();
	}

	public ArrayList<String> getRaidWhitelist() {
		ArrayList<String> list = new ArrayList<String>();
		for (String s : raidWhitelist) {
			list.add(s);
		}
		java.util.Collections.sort(list);
		return list;
	}

	// public void addSubscriber(String name) {
	// subscribers.add(name.toLowerCase());
	//
	// String subsString="";
	// for (String s : subscribers) {
	// subsString += s + "&&&";
	// }
	// config.setString("subscribers",subsString);
	// }

	// ###################################################

	public boolean isModerator(String name) {
		synchronized (tagModerators) {
			if (tagModerators.contains(name))
				return true;
		}
		synchronized (moderators) {
			if (moderators.contains(name.toLowerCase()))
				return true;
		}

		return false;
	}

	public void addModerator(String name) {
		synchronized (moderators) {
			moderators.add(name.toLowerCase());
		}

		JSONArray moderatorsArray = new JSONArray();

		synchronized (moderators) {
			for (String s : moderators) {
				moderatorsArray.add(s);
			}
		}

		config.put("moderators", moderatorsArray);
		saveConfig();
	}

	public void removeModerator(String name) {
		synchronized (moderators) {
			if (moderators.contains(name.toLowerCase()))
				moderators.remove(name.toLowerCase());
		}

		JSONArray moderatorsArray = new JSONArray();

		synchronized (moderators) {
			for (String s : moderators) {
				moderatorsArray.add(s);
			}
		}

		config.put("moderators", moderatorsArray);
		saveConfig();
	}

	public Set<String> getModerators() {
		return moderators;
	}

	// ###################################################

	public boolean isOwner(String name) {
		synchronized (owners) {
			if (owners.contains(name.toLowerCase()))
				return true;
		}

		return false;
	}

	public void addOwner(String name) {
		synchronized (owners) {
			owners.add(name.toLowerCase());
		}

		JSONArray ownersString = new JSONArray();

		synchronized (owners) {
			for (String s : owners) {
				ownersString.add(s);
			}
		}

		config.put("owners", ownersString);
		saveConfig();
	}

	public void removeOwner(String name) {
		synchronized (owners) {
			if (owners.contains(name.toLowerCase()))
				owners.remove(name.toLowerCase());
		}

		JSONArray ownersString = new JSONArray();

		synchronized (owners) {
			for (String s : owners) {
				ownersString.add(s);
			}
		}

		config.put("owners", ownersString);
		saveConfig();
	}

	public Set<String> getOwners() {
		return owners;
	}

	// ###################################################

	public void addPermittedDomain(String name) {
		synchronized (permittedDomains) {
			permittedDomains.add(name.toLowerCase());
		}

		JSONArray permittedDomainsString = new JSONArray();

		synchronized (permittedDomains) {
			for (String s : permittedDomains) {
				permittedDomainsString.add(s);
			}
		}

		config.put("permittedDomains", permittedDomainsString);
		saveConfig();
	}

	public void removePermittedDomain(String name) {
		synchronized (permittedDomains) {
			for (int i = 0; i < permittedDomains.size(); i++) {
				if (permittedDomains.get(i).equalsIgnoreCase(name)) {
					permittedDomains.remove(i);
				}
			}
		}

		JSONArray permittedDomainsString = new JSONArray();

		synchronized (permittedDomains) {
			for (String s : permittedDomains) {
				permittedDomainsString.add(s);
			}
		}

		config.put("permittedDomains", permittedDomainsString);
		saveConfig();

	}

	public boolean isDomainPermitted(String domain) {
		for (String d : permittedDomains) {
			if (d.equalsIgnoreCase(domain)) {
				return true;
			}
		}

		return false;
	}

	public ArrayList<String> getpermittedDomains() {
		return permittedDomains;
	}

	// #################################################

	public void addOffensive(String word) {
		synchronized (offensiveWords) {
			offensiveWords.add(word);
		}

		synchronized (offensiveWordsRegex) {
			if (word.startsWith("REGEX:")) {
				String line = word.substring(6);
				System.out.println("Adding: " + line);
				Pattern tempP = Pattern.compile(line);
				offensiveWordsRegex.add(tempP);
			} else {
				String line = ".*" + Pattern.quote(word) + ".*";
				System.out.println("Adding: " + line);
				Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
				offensiveWordsRegex.add(tempP);
			}

		}

		JSONArray offensiveWordsArray = new JSONArray();

		synchronized (offensiveWords) {
			for (String s : offensiveWords) {
				offensiveWordsArray.add(s);
			}
		}

		config.put("offensiveWords", offensiveWordsArray);
		saveConfig();
	}

	public void removeOffensive(String word) {
		synchronized (offensiveWords) {
			if (offensiveWords.contains(word))
				offensiveWords.remove(word);
		}

		JSONArray offensiveWordsArray = new JSONArray();

		synchronized (offensiveWords) {
			for (String s : offensiveWords) {
				offensiveWordsArray.add(s);
			}
		}

		config.put("offensiveWords", offensiveWordsArray);
		saveConfig();

		synchronized (offensiveWordsRegex) {
			offensiveWordsRegex.clear();

			for (String w : offensiveWords) {
				if (w.startsWith("REGEX:")) {
					String line = w.substring(6);
					System.out.println("ReAdding: " + line);
					Pattern tempP = Pattern.compile(line);
					offensiveWordsRegex.add(tempP);
				} else {
					String line = ".*" + Pattern.quote(w) + ".*";
					System.out.println("ReAdding: " + line);
					Pattern tempP = Pattern.compile(line);
					offensiveWordsRegex.add(tempP);
				}
			}
		}
	}

	public void clearBannedPhrases() {
		offensiveWords.clear();
		offensiveWordsRegex.clear();
		config.put("offensiveWords", new JSONArray());
		saveConfig();
	}

	public boolean isBannedPhrase(String phrase) {
		return offensiveWords.contains(phrase);
	}

	public boolean isOffensive(String word) {
		for (Pattern reg : offensiveWordsRegex) {
			Matcher match = reg.matcher(word.toLowerCase());
			if (match.find()) {
				System.out.println("Matched: " + reg.toString());
				return true;
			}
		}

		int severity = ((Long) config.get("banPhraseSeverity")).intValue();
		if (BotManager.getInstance().banPhraseLists.containsKey(severity)) {
			for (Pattern reg : BotManager.getInstance().banPhraseLists
					.get(severity)) {
				Matcher match = reg.matcher(word.toLowerCase());
				if (match.find()) {
					System.out.println("Matched: " + reg.toString());
					return true;
				}
			}
		}

		return false;
	}

	public Set<String> getOffensive() {
		return offensiveWords;
	}

	// ##################################################

	public void setTopicFeature(boolean setting) {
		this.useTopic = setting;
		config.put("useTopic", this.useTopic);
		saveConfig();

	}

	public void setFiltersFeature(boolean setting) {
		this.useFilters = setting;
		config.put("useFilters", this.useFilters);
		saveConfig();
	}

	public Poll getPoll() {
		return currentPoll;
	}

	public void setPoll(Poll _poll) {
		currentPoll = _poll;
	}

	public Giveaway getGiveaway() {
		return currentGiveaway;
	}

	public void setGiveaway(Giveaway _gw) {
		currentGiveaway = _gw;
	}

	public boolean checkThrow() {
		return enableThrow;
	}

	public void setThrow(boolean setting) {
		this.enableThrow = setting;
		config.put("enableThrow", this.enableThrow);
		saveConfig();
	}

	public boolean checkSignKicks() {
		return signKicks;
	}

	public void setSignKicks(boolean setting) {
		this.signKicks = setting;
		config.put("signKicks", this.signKicks);
		saveConfig();
	}

	public void setLogging(boolean option) {
		logChat = option;
		config.put("logChat", option);
		saveConfig();
	}

	public boolean getLogging() {
		return logChat;
	}

	public int getCommercialLength() {
		return commercialLength;
	}

	public void setCommercialLength(int commercialLength) {
		this.commercialLength = commercialLength;
		config.put("commercialLength", commercialLength);
		saveConfig();
	}

	// ##################################################

	public boolean checkPermittedDomain(String message) {
		// Allow base domain w/o a path
		if (message.matches(".*(twitch\\.tv|twitchtv\\.com|justin\\.tv)")) {
			System.out
					.println("INFO: Permitted domain match on jtv/ttv base domain.");
			return true;
		}

		for (String d : permittedDomains) {
			// d = d.replaceAll("\\.", "\\\\.");

			String test = ".*(\\.|^|//)" + Pattern.quote(d) + "(/|$).*";
			if (message.matches(test)) {
				// System.out.println("DEBUG: Matched permitted domain: " +
				// test);
				return true;
			}
		}
		return false;
	}

	// #################################################

	public String getLastfm() {
		return lastfm;
	}

	public void setLastfm(String string) {
		lastfm = string;
		config.put("lastfm", lastfm);
		saveConfig();
	}

	public void updateSong(String newSong) {

		if (!streamAlive || newSong.equals(lastSong)
				|| newSong.equals("(Nothing)")
				|| newSong.equalsIgnoreCase("(Error querying API)")) {
			return;

		} else {
			long now = System.currentTimeMillis();
			if ((now * 1L) >= (songUpdated + updateDelay * 1000L)) {
				lastSong = newSong;
				songUpdated = now + 5000;
				String currBullet = bullet;
				bullet = "âŤ";
				BotManager.getInstance().receiverBot.send(channel,
						"Now Playing: " + newSong);
				bullet = currBullet;
			}
			return;
		}

	}

	public void checkViewerStats(String name) {
		long viewers = JSONUtil.krakenViewers(name);
		if (viewers > maxViewers) {
			maxViewers = (int) viewers;
			config.put("maxViewers", maxViewers);
			maxviewerDate = new java.util.Date().toString();
			config.put("maxviewerDate", maxviewerDate);

		}
		saveConfig();

	}

	public int getViewerStats() {
		return maxViewers;
	}

	public String getViewerStatsTime() {
		return maxviewerDate;
	}

	public void resetMaxViewers(int newMax) {
		maxViewers = newMax;
		config.put("maxViewers", maxViewers);
		saveConfig();
	}

	public void increasePunCount() {
		punishCount++;
		sincePunish = System.currentTimeMillis();
		config.put("sincePunish", sincePunish);
		config.put("punishCount", punishCount);
		saveConfig();
	}

	public int getPunCount() {
		return punishCount;
	}

	public void alive(String name) {

		long curViewers = JSONUtil.krakenViewers(name);
		if (curViewers > streamMax) {
			streamMax = (int) curViewers;
			config.put("maxViewersStream", curViewers);
		}
	}

	public void dead(String name) {

		streamNumber++;
		config.put("streamCount", streamNumber);
		runningMaxViewers += streamMax;
		config.put("runningMaxViewers", runningMaxViewers);
		streamMax = 0;
		config.put("maxViewersStream", 0);
		saveConfig();
	}

	public double getAverage() {
		return (double) runningMaxViewers / (streamNumber);
	}

	// #################################################

	public String getSteam() {
		return steamID;
	}

	public void setSteam(String string) {
		steamID = string;
		config.put("steamID", steamID);
		saveConfig();
	}

	// #################################################

	public String getClickToTweetFormat() {
		return clickToTweetFormat;
	}

	public void setClickToTweetFormat(String string) {
		clickToTweetFormat = string;
		config.put("clickToTweetFormat", clickToTweetFormat);
		saveConfig();
	}

	public int getWarningCount(String name, FilterType type) {
		if (warningCount.containsKey(name.toLowerCase())
				&& warningCount.get(name.toLowerCase()).containsKey(type))
			return warningCount.get(name.toLowerCase()).get(type);
		else
			return 0;
	}

	public void incWarningCount(String name, FilterType type) {
		clearWarnings();
		synchronized (warningCount) {
			if (warningCount.containsKey(name.toLowerCase())) {
				if (warningCount.get(name.toLowerCase()).containsKey(type)) {
					warningCount.get(name.toLowerCase()).put(type,
							warningCount.get(name.toLowerCase()).get(type) + 1);
					warningTime.put(name.toLowerCase(), getTime());
				} else {
					warningCount.get(name.toLowerCase()).put(type, 1);
					warningTime.put(name.toLowerCase(), getTime());
				}
			} else {
				warningCount.put(name.toLowerCase(),
						new EnumMap<FilterType, Integer>(FilterType.class));
				warningCount.get(name.toLowerCase()).put(type, 1);
				warningTime.put(name.toLowerCase(), getTime());
			}
		}
	}

	public void clearWarnings() {
		List<String> toRemove = new ArrayList<String>();
		synchronized (warningTime) {
			synchronized (warningCount) {
				long time = getTime();
				for (Map.Entry<String, Long> entry : warningTime.entrySet()) {
					if ((time - entry.getValue()) > 3600) {
						toRemove.add((String) entry.getKey());
					}
				}
				for (String name : toRemove) {
					warningCount.remove(name);
					warningTime.remove(name);
				}
			}
		}
	}

	public void registerCommandUsage(String command) {
		synchronized (commandCooldown) {
			System.out.println("DEBUG: Adding command " + command
					+ " to cooldown list");
			commandCooldown.put(command.toLowerCase(), getTime());
		}
	}

	public boolean onCooldown(String command) {
		command = command.toLowerCase();
		if (commandCooldown.containsKey(command)) {
			long lastUse = commandCooldown.get(command);
			if ((getTime() - lastUse) > 30) {
				// Over
				System.out.println("DEBUG: Cooldown for " + command
						+ " is over");
				
				return false;
			} else {
				// Not Over
				System.out.println("DEBUG: Cooldown for " + command
						+ " is NOT over");
				return true;
			}
		} else {
			
			return false;
		}
	}

	public void setUpdateDelay(int newDelay) {
		updateDelay = newDelay;
		config.put("updateDelay", newDelay);
		saveConfig();
	}

	public void reload() {
		BotManager.getInstance().removeChannel(channel);
		BotManager.getInstance().addChannel(channel, mode);
	}

	private void setDefaults() {

		// defaults.put("channel", channel);
		defaults.put("ignoredUsers",new JSONArray());
		defaults.put("urbanEnabled", true);
		defaults.put("extraLifeID", 0);
		defaults.put("subsRegsMinusLinks", new Boolean(false));
		defaults.put("filterCaps", new Boolean(false));
		defaults.put("filterOffensive", new Boolean(true));
		defaults.put("filterCapsPercent", 50);
		defaults.put("filterCapsMinCharacters", 0);
		defaults.put("filterCapsMinCapitals", 6);
		defaults.put("filterLinks", new Boolean(false));
		defaults.put("filterEmotes", new Boolean(false));
		defaults.put("filterSymbols", new Boolean(false));
		defaults.put("filterEmotesMax", 4);

		defaults.put("punishCount", 0);
		defaults.put("sincePunish", sincePunish);
		defaults.put("sinceWp", System.currentTimeMillis());
		defaults.put("maxviewerDate", "");

		defaults.put("topic", "");
		defaults.put("commands", new JSONArray());

		defaults.put("repeatedCommands", new JSONArray());
		defaults.put("scheduledCommands", new JSONArray());
		defaults.put("autoReplies", new JSONArray());
		defaults.put("regulars", new JSONArray());
		defaults.put("moderators", new JSONArray());
		defaults.put("owners", new JSONArray());
		defaults.put("useTopic", new Boolean(true));
		defaults.put("useFilters", new Boolean(false));
		defaults.put("enableThrow", new Boolean(true));
		defaults.put("permittedDomains", new JSONArray());
		defaults.put("signKicks", new Boolean(false));
		defaults.put("topicTime", 0);
		defaults.put("mode", 2);
		defaults.put("announceJoinParts", new Boolean(false));
		defaults.put("lastfm", "");
		defaults.put("steamID", "");
		defaults.put("logChat", new Boolean(false));
		defaults.put("filterMaxLength", 500);
		defaults.put("offensiveWords", new JSONArray());
		defaults.put("commercialLength", 30);
		defaults.put("filterColors", new Boolean(false));
		defaults.put("filterMe", new Boolean(false));
		defaults.put("staticChannel", new Boolean(false));
		defaults.put("enableWarnings", new Boolean(true));
		defaults.put("timeoutDuration", 600);
		defaults.put("clickToTweetFormat",
				"Checkout (_CHANNEL_URL_) playing (_GAME_) on @TwitchTV");
		defaults.put("filterSymbolsPercent", 50);
		defaults.put("filterSymbolsMin", 5);
		defaults.put("commandPrefix", "!");

		defaults.put("emoteSet", "");
		defaults.put("subscriberRegulars", new Boolean(false));
		defaults.put("filterEmotesSingle", new Boolean(false));
		defaults.put("subMessage", "(_1_) has subscribed!");
		defaults.put("subscriberAlert", new Boolean(false));
		defaults.put("banPhraseSeverity", 99);

		defaults.put("wpTimer", new Boolean(false));
		defaults.put("wpCount", 0);
		defaults.put("bullet", "coeBot");
		defaults.put("cooldown", 5);

		defaults.put("maxViewers", 0);
		defaults.put("runningMaxViewers", 0);
		defaults.put("streamCount", 0);
		defaults.put("streamAlive", new Boolean(false));
		defaults.put("maxViewersStream", 0);

		defaults.put("updateDelay", 120);
		defaults.put("quotes", new JSONArray());
		// defaults.put("subscribers", new JSONArray());
		defaults.put("raidWhitelist", new JSONArray());
		defaults.put("gamerTag", "");
		// defaults.put("timeAliveStart", System.currentTimeMillis());

		Iterator it = defaults.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String key = String.valueOf(pairs.getKey());
			Object value = pairs.getValue();
			if (value instanceof Integer) {
				value = Integer.parseInt(String.valueOf(value)) * 1L;
			}
			if (!config.containsKey(key))
				config.put(key, value);
		}
		saveConfig();
	}

	public void updateConfigs() {
		config.put("subsRegsMinusLinks", subsRegsMinusLinks);
		config.put("filterCaps", filterCaps);
		config.put("filterOffensive", true);
		config.put("filterCapsPercent", filterCapsPercent);
		config.put("filterCapsMinCharacters", filterCapsMinCharacters);
		config.put("filterCapsMinCapitals", filterCapsMinCapitals);
		config.put("filterLinks", filterLinks);
		config.put("filterEmotes", filterEmotes);
		config.put("filterSymbols", filterSymbols);
		config.put("filterEmotesMax", filterEmotesMax);

		config.put("punishCount", punishCount);
		config.put("sincePunish", sincePunish);
		config.put("sinceWp", System.currentTimeMillis());
		config.put("maxviewerDate", maxviewerDate);

		config.put("topic", topic);
		this.saveCommands();
		this.saveAutoReply();
		this.saveRepeatCommands();
		this.saveScheduledCommands();

		this.addRegular("JSONCONVERT");
		this.removeRegular("JSONCONVERT");
		this.addModerator("JSONCONVERT");
		this.removeModerator("JSONCONVERT");
		this.addOwner("JSONCONVERT");
		this.removeOwner("JSONCONVERT");

		config.put("useTopic", useTopic);
		config.put("useFilters", useFilters);
		config.put("enableThrow", enableThrow);

		this.addPermittedDomain("JSONCONVERT");
		this.removePermittedDomain("JSONCONVERT");

		config.put("signKicks", signKicks);
		config.put("topicTime", topicTime);
		config.put("mode", mode);
		config.put("announceJoinParts", false);
		config.put("lastfm", lastfm);
		config.put("steamID", steamID);
		config.put("logChat", logChat);
		config.put("filterMaxLength", filterMaxLength);
		this.addOffensive("JSONCONVERT");
		this.removeOffensive("JSONCONVERT");

		config.put("commercialLength", commercialLength);
		config.put("filterColors", filterColors);
		config.put("filterMe", filterMe);
		config.put("staticChannel", staticChannel);
		config.put("enableWarnings", enableWarnings);
		config.put("timeoutDuration", timeoutDuration);
		config.put("clickToTweetFormat", clickToTweetFormat);
		config.put("filterSymbolsPercent", filterSymbolsPercent);
		config.put("filterSymbolsMin", filterSymbolsMin);
		config.put("commandPrefix", prefix);

		config.put("emoteSet", emoteSet);
		config.put("subscriberRegulars", subscriberRegulars);
		config.put("filterEmotesSingle", filterEmotesSingle);
		config.put("subMessage", config.get("subMessage"));
		config.put("subscriberAlert",
				Boolean.valueOf((Boolean) config.get("subscriberAlert")));
		config.put("banPhraseSeverity",
				((Long) config.get("banPhraseSeverity")).intValue());

		config.put("wpTimer", Boolean.valueOf((Boolean) config.get("wpTimer")));
		config.put("wpCount", wpCount);
		config.put("bullet", bullet);
		config.put("cooldown", cooldown);

		config.put("maxViewers", maxViewers);
		config.put("runningMaxViewers", runningMaxViewers);
		config.put("streamCount", streamNumber);
		config.put("streamAlive", streamAlive);
		config.put("maxViewersStream", streamMax);

		config.put("updateDelay", updateDelay);
		this.addQuote("JSONCONVERT");
		this.deleteQuote(this.getQuoteIndex("JSONCONVERT"));

		// config.put("subscribers", subscribers);
		this.addRaidWhitelist("JSONCONVERT");
		this.deleteRaidWhitelist("JSONCONVERT");

		config.put("gamerTag", gamerTag);
		saveConfig();

	}

	private void loadProperties(String name) {

		setDefaults();
		
		urbanEnabled = Boolean.valueOf((Boolean) config.get("urbanEnabled"));
		extraLifeID = ((Long)config.get("extraLifeID"));
		gamerTag = (String) config.get("gamerTag");
		// channel = config.getString("channel");
		subsRegsMinusLinks = Boolean.valueOf((Boolean) config
				.get("subsRegsMinusLinks"));
		updateDelay = ((Long) config.get("updateDelay")).intValue();
		punishCount = ((Long) config.get("punishCount")).intValue();
		streamAlive = (Boolean) config.get("streamAlive");
		sinceWp = ((Long) config.get("sinceWp"));
		maxviewerDate = (String) config.get("maxviewerDate");
		runningMaxViewers = ((Long) config.get("runningMaxViewers")).intValue();
		streamNumber = ((Long) config.get("streamCount")).intValue();
		streamMax = ((Long) config.get("maxViewersStream")).intValue();
		maxViewers = ((Long) config.get("maxViewers")).intValue();
		filterCaps = Boolean.valueOf((Boolean) config.get("filterCaps"));

		filterCapsPercent = ((Long) config.get("filterCapsPercent")).intValue();
		filterCapsMinCharacters = ((Long) config.get("filterCapsMinCharacters"))
				.intValue();
		filterCapsMinCapitals = ((Long) config.get("filterCapsMinCapitals"))
				.intValue();
		filterLinks = Boolean.valueOf((Boolean) config.get("filterLinks"));
		filterOffensive = Boolean.valueOf((Boolean) config
				.get("filterOffensive"));
		filterEmotes = Boolean.valueOf((Boolean) config.get("filterEmotes"));

		wpOn = Boolean.valueOf((Boolean) config.get("wpTimer"));
		wpCount = ((Long) config.get("wpCount")).intValue();
		bullet = (String) config.get("bullet");
		cooldown = ((Long) config.get("cooldown")).intValue();
		sincePunish = (Long) config.get("sincePunish");

		filterSymbols = Boolean.valueOf((Boolean) config.get("filterSymbols"));
		filterSymbolsPercent = ((Long) config.get("filterSymbolsPercent"))
				.intValue();
		filterSymbolsMin = ((Long) config.get("filterSymbolsMin")).intValue();
		filterEmotesMax = ((Long) config.get("filterEmotesMax")).intValue();
		filterEmotesSingle = Boolean.valueOf((Boolean) config
				.get("filterEmotesSingle"));
		// announceJoinParts =
		// Boolean.parseBoolean(config.getString("announceJoinParts"));
		announceJoinParts = false;
		topic = (String) config.get("topic");
		topicTime = ((Long) config.get("topicTime")).intValue();
		useTopic = Boolean.valueOf((Boolean) config.get("useTopic"));
		useFilters = Boolean.valueOf((Boolean) config.get("useFilters"));
		enableThrow = Boolean.valueOf((Boolean) config.get("enableThrow"));
		signKicks = Boolean.valueOf((Boolean) config.get("signKicks"));
		lastfm = (String) config.get("lastfm");
		steamID = (String) config.get("steamID");
		logChat = Boolean.valueOf((Boolean) config.get("logChat"));
		mode = ((Long) config.get("mode")).intValue();
		filterMaxLength = ((Long) config.get("filterMaxLength")).intValue();
		commercialLength = ((Long) config.get("commercialLength")).intValue();
		filterColors = Boolean.valueOf((Boolean) config.get("filterColors"));
		filterMe = Boolean.valueOf((Boolean) config.get("filterMe"));
		staticChannel = Boolean.valueOf((Boolean) config.get("staticChannel"));
		clickToTweetFormat = (String) config.get("clickToTweetFormat");

		enableWarnings = Boolean
				.valueOf((Boolean) config.get("enableWarnings"));
		timeoutDuration = ((Long) config.get("timeoutDuration")).intValue();
		prefix = (String) config.get("commandPrefix");
		emoteSet = (String) config.get("emoteSet");
		subscriberRegulars = Boolean.valueOf((Boolean) config
				.get("subscriberRegulars"));

		// timeAliveStart = (Long)config.get("timeAliveStart");
		
		JSONArray jsonignoredUsers = (JSONArray) config.get("ignoredUsers");
		for (int i = 0; i<jsonignoredUsers.size();i++){
			ignoredUsers.add((String)jsonignoredUsers.get(i));
		}

		JSONArray quotesArray = (JSONArray) config.get("quotes");

		for (int i = 0; i < quotesArray.size(); i++) {
			quotes.add((String) quotesArray.get(i));
		}

		JSONArray raidWhitelistArray = (JSONArray) config.get("raidWhitelist");

		for (int i = 0; i < raidWhitelistArray.size(); i++) {
			if (!raidWhitelistArray.get(i).equals("")) {
				raidWhitelist.add((String) raidWhitelistArray.get(i));
			}
		}

		// String[] subsArray = oldconfig.getString("subscribers").split("&&&");
		//
		// for (int i = 0; i < subsArray.length; i++) {
		// subscribers.add(subsArray[i]);
		// }

		JSONArray commandsArray = (JSONArray) config.get("commands");

		for (int i = 0; i < commandsArray.size(); i++) {
			JSONObject commandObject = (JSONObject) commandsArray.get(i);
			commands.put((String) commandObject.get("key"),
					(String) commandObject.get("value"));
			if (commandObject.containsKey("restriction")) {
				commandsRestrictions.put((String) commandObject.get("key"),
						((Long) commandObject.get("restriction")).intValue());
			}
			if (commandObject.containsKey("count")
					&& commandObject.get("count") != null) {
				commandCounts.put((String) commandObject.get("key"),
						((Long) commandObject.get("count")).intValue());
			} else {
				commandCounts.put((String) commandObject.get("key"), 0);
			}

		}
		saveCommands();

		//
		// String[] commandsRepeatKey = oldconfig.getString("commandsRepeatKey")
		// .split(",");
		// String[] commandsRepeatDelay = oldconfig.getString(
		// "commandsRepeatDelay").split(",");
		// String[] commandsRepeatDiff =
		// oldconfig.getString("commandsRepeatDiff")
		// .split(",");
		// String[] commandsRepeatActive = oldconfig.getString(
		// "commandsRepeatActive").split(",");
		JSONArray repeatedCommandsArray = (JSONArray) config
				.get("repeatedCommands");

		for (int i = 0; i < repeatedCommandsArray.size(); i++) {
			JSONObject repeatedCommandObj = (JSONObject) repeatedCommandsArray
					.get(i);
			RepeatCommand rc = new RepeatCommand(channel,
					((String) repeatedCommandObj.get("name")).replaceAll(
							"[^a-zA-Z0-9]", ""),
					((Long) repeatedCommandObj.get("delay")).intValue(),
					((Long) repeatedCommandObj.get("messageDifference"))
							.intValue(),
					Boolean.valueOf((Boolean) repeatedCommandObj.get("active")));
			commandsRepeat.put(((String) repeatedCommandObj.get("name"))
					.replaceAll("[^a-zA-Z0-9]", ""), rc);

		}

		// String[] commandsScheduleKey = oldconfig.getString(
		// "commandsScheduleKey").split(",,");
		// String[] commandsSchedulePattern = oldconfig.getString(
		// "commandsSchedulePattern").split(",,");
		// String[] commandsScheduleDiff = oldconfig.getString(
		// "commandsScheduleDiff").split(",,");
		// String[] commandsScheduleActive = oldconfig.getString(
		// "commandsScheduleActive").split(",,");
		JSONArray scheduledCommandsArray = (JSONArray) config
				.get("scheduledCommands");

		for (int i = 0; i < scheduledCommandsArray.size(); i++) {
			JSONObject scheduledCommandsObj = (JSONObject) scheduledCommandsArray
					.get(i);
			ScheduledCommand rc = new ScheduledCommand(channel,
					((String) scheduledCommandsObj.get("name")).replaceAll(
							"[^a-zA-Z0-9]", ""),
					(String) scheduledCommandsObj.get("pattern"),
					((Long) scheduledCommandsObj.get("messageDifference"))
							.intValue(),
					Boolean.valueOf((Boolean) scheduledCommandsObj
							.get("active")));
			commandsSchedule.put(((String) scheduledCommandsObj.get("name"))
					.replaceAll("[^a-zA-Z0-9]", ""), rc);

		}

		// String[] autoReplyTriggersString = oldconfig.getString(
		// "autoReplyTriggers").split(",,");
		// String[] autoReplyResponseString = oldconfig.getString(
		// "autoReplyResponse").split(",,");
		JSONArray autoReplyArray = (JSONArray) config.get("autoReplies");
		for (int i = 0; i < autoReplyArray.size(); i++) {
			JSONObject autoReplyObj = (JSONObject) autoReplyArray.get(i);
			autoReplyTrigger.add(Pattern.compile(
					(String) autoReplyObj.get("trigger"),
					Pattern.CASE_INSENSITIVE));
			autoReplyResponse.add((String) autoReplyObj.get("response"));

		}

		// String[] regularsRaw = oldconfig.getString("regulars").split(",");
		JSONArray regularsJSONArray = (JSONArray) config.get("regulars");
		synchronized (regulars) {
			for (int i = 0; i < regularsJSONArray.size(); i++) {

				regulars.add(((String) regularsJSONArray.get(i)).toLowerCase());

			}
		}

		// String[] moderatorsRaw =
		// oldconfig.getString("moderators").split(",");
		JSONArray modsArray = (JSONArray) config.get("moderators");
		synchronized (moderators) {
			for (int i = 0; i < modsArray.size(); i++) {

				moderators.add(((String) modsArray.get(i)).toLowerCase());

			}
		}

		// String[] ownersRaw = oldconfig.getString("owners").split(",");
		JSONArray ownersArray = (JSONArray) config.get("owners");
		synchronized (owners) {
			for (int i = 0; i < ownersArray.size(); i++) {

				owners.add(((String) ownersArray.get(i)).toLowerCase());

			}
		}

		// String[] domainsRaw =
		// oldconfig.getString("permittedDomains").split(",");
		JSONArray domainsArray = (JSONArray) config.get("permittedDomains");

		synchronized (permittedDomains) {
			for (int i = 0; i < domainsArray.size(); i++) {

				// permittedDomains.add(domainsRaw[i].toLowerCase().replaceAll("\\.",
				// "\\\\."));
				permittedDomains.add(((String) domainsArray.get(i))
						.toLowerCase());

			}
		}
		// System.out.println(oldconfig.getString("offensiveWords"));
		// String[] offensiveWordsRaw = oldconfig.getString("offensiveWords")
		// .split(",,");
		JSONArray offensiveArray = (JSONArray) config.get("offensiveWords");

		synchronized (offensiveWords) {
			synchronized (offensiveWordsRegex) {
				for (int i = 0; i < offensiveArray.size(); i++) {

					String w = (String) offensiveArray.get(i);
					offensiveWords.add(w);
					if (w.startsWith("REGEX:")) {
						String line = w.substring(6);
						System.out.println("Adding: " + line);
						Pattern tempP = Pattern.compile(line);
						offensiveWordsRegex.add(tempP);
					} else {
						String line = "(?i).*" + Pattern.quote(w) + ".*";
						System.out.println("Adding: " + line);
						Pattern tempP = Pattern.compile(line);
						offensiveWordsRegex.add(tempP);
					}

				}
			}

		}

	}

	public void setMode(int mode) {
		this.mode = mode;
		config.put("mode", this.mode);

		if (mode == -1) {
			this.setFiltersFeature(true);
			this.setFilterEmotes(false);
			this.setFilterEmotesMax(5);
			this.setFilterSymbols(true);
			this.setFilterCaps(false);
			this.setFilterLinks(false);
			this.setFilterOffensive(true);
			this.setSignKicks(false);
			this.setTopicFeature(false);
			this.setThrow(false);
		}
		saveConfig();
	}

	public int getMode() {
		return mode;
	}

	private long getTime() {
		return (System.currentTimeMillis() / 1000L);
	}

	public void cancelCommercial() {
		commercial.cancel();
	}

	public void scheduleCommercial() {

		commercial = new java.util.Timer();
		commercial.schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				runCommercial();
				
			}
		}, 45000);
	}
	public void scheduleCommercial(final int commercialTime) {

		commercial = new java.util.Timer();
		commercial.schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				runCommercial(commercialTime);
				
			}
		}, 45000);
	}

	public void snoozeCommercial() {
		if (commercial != null) {
			commercial.cancel();
			commercial.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					scheduleCommercial();
				}
			}, 300000);
		}
	}

	public void testChannelSend() {
		BotManager.getInstance().receiverBot.send(getChannel(), "Success!");
	}

	public void runCommercial() {

		if (JSONUtil.krakenIsLive(getChannel().substring(1))) {
			String dataIn = "";
			dataIn = BotManager.postRemoteDataTwitch(
					"https://api.twitch.tv/kraken/channels/"
							+ getChannel().substring(1) + "/commercial",
					"length=" + commercialLength, 2);

		} else {
			System.out.println(getChannel().substring(1)
					+ " is not live. Skipping commercial.");
		}
	}
	public void runCommercial(int commercialTime) {

		if (JSONUtil.krakenIsLive(getChannel().substring(1))) {
			String dataIn = "";
			dataIn = BotManager.postRemoteDataTwitch(
					"https://api.twitch.tv/kraken/channels/"
							+ getChannel().substring(1) + "/commercial",
					"length=" + commercialTime, 2);

		} else {
			System.out.println(getChannel().substring(1)
					+ " is not live. Skipping commercial.");
		}
	}

	public void setCooldown(int newCooldown) {
		cooldown = newCooldown;
		config.put("cooldown", newCooldown);
		saveConfig();
	}

	public long getCooldown() {

		return cooldown;
	}

	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
			throw new IllegalArgumentException(
					"Duration must be greater than zero!");
		}

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		StringBuilder sb = new StringBuilder(64);
		if (days > 0) {
			sb.append(days);
			sb.append(" days, ");
		}

		if (hours < 10)
			sb.append(0);
		sb.append(hours);

		sb.append(" hours, ");
		if (minutes < 10)
			sb.append(0);
		sb.append(minutes);
		sb.append(" minutes, and ");
		if (seconds < 10)
			sb.append(0);
		sb.append(seconds);
		sb.append(" seconds.");

		return (sb.toString());
	}

	public void saveConfig() {
		try {

			FileWriter file = new FileWriter(channel + ".json");
			
			StringWriter out = new StringWriter();
			JSONValue.writeJSONString(config, out);
			String jsonText = out.toString();
			file.write(jsonText);
			// file.write(config.toJSONString());
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUrban(boolean enabled) {
		urbanEnabled = enabled;
		config.put("urbanEnabled", enabled);
		saveConfig();

	}

	public boolean getUrban() {
		return urbanEnabled;
	}

	public void setExtraLifeID(String string) {
		extraLifeID = Long.parseLong(string);
		config.put("extraLifeID", Long.parseLong(string));
		saveConfig();
		
	}

	
	
	public Long getExtraLifeID() {
		
		return extraLifeID;
	}
	
	public ArrayList<String> getIgnoredUsers(){
		return ignoredUsers;
	}
	public boolean addIgnoredUser(String user){
		if(ignoredUsers.contains(user)){
			return false;
		}
		else{
		ignoredUsers.add(user);
		config.put("ignoredUsers", ignoredUsers);
		saveConfig();
		return true;
		}
		
	}
	public boolean removeIgnoredUser(String user){
	if(ignoredUsers.contains(user)){
		ignoredUsers.remove(user);
		config.put("ignoredUsers", ignoredUsers);
		saveConfig();
		return true;
	}else
		return false;
	}
}

