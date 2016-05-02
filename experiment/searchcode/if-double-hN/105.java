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

import net.bashtech.geobot.modules.BotModule;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiverBot extends PircBot {
	static ReceiverBot instance;
	Timer joinCheck;
	Random random = new Random();
	private Pattern[] linkPatterns = new Pattern[4];
	private Pattern[] symbolsPatterns = new Pattern[2];
	private int lastPing = -1;
	private String bullet[] = { "#!" };
	private int bulletPos = 0;
	private int countToNewColor = BotManager.getInstance().randomNickColorDiff;
	private Pattern twitchnotifySubscriberPattern = Pattern.compile(
			"^([a-z_]+) just subscribed!$", Pattern.CASE_INSENSITIVE);
	private Pattern banNoticePattern = Pattern.compile(
			"^You are permanently banned from talking in ([a-z_]+).$",
			Pattern.CASE_INSENSITIVE);
	private Pattern toNoticePattern = Pattern
			.compile(
					"^You are banned from talking in ([a-z_]+) for (?:[0-9]+) more seconds.$",
					Pattern.CASE_INSENSITIVE);
	private Pattern vinePattern = Pattern.compile(
			".*(vine|4).*(4|vine).*(Google|\\*\\*\\*).*",
			Pattern.CASE_INSENSITIVE);
	private ArrayList<String> commandList = new ArrayList<String>();
	long lastQuoted = 0;
	long newQuoted = System.currentTimeMillis();
	long lastQuoted1 = 0;
	long newQuoted1 = System.currentTimeMillis();
	private ArrayList<String> wpOn = new ArrayList<String>();
	long lastCommand = System.currentTimeMillis();
	private boolean privMsgSub = false;
	private ArrayList<Long> msgTimer = new ArrayList<Long>();
	private ArrayList<QueuedMessage> queuedMessages = new ArrayList<QueuedMessage>();
	private boolean tried;
	private boolean delete;
	private boolean permitted;
	private long lastConch = System.currentTimeMillis();

	public ReceiverBot(String server, int port) {
		ReceiverBot.setInstance(this);

		linkPatterns[0] = Pattern.compile(".*http://.*",
				Pattern.CASE_INSENSITIVE);
		linkPatterns[1] = Pattern.compile(".*https://.*",
				Pattern.CASE_INSENSITIVE);
		linkPatterns[2] = Pattern
				.compile(
						".*[-A-Za-z0-9]+\\s?(\\.|\\(dot\\))\\s?(ac|ad|ae|aero|af|ag|ai|al|am|an|ao|aq|ar|as|asia|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|biz|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cat|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|com|coop|cr|cu|cv|cw|cx|cy|cz|de|dj|dk|dm|do|dz|ec|edu|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gov|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|info|int|io|iq|ir|is|it|je|jm|jo|jobs|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mil|mk|ml|mm|mn|mo|mobi|mp|mq|mr|ms|mt|mu|museum|mv|mw|mx|my|mz|na|name|nc|ne|net|nf|ng|ni|nl|no|np|nr|nu|nz|om|org|pa|pe|pf|pg|ph|pk|pl|pm|pn|post|pr|pro|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|st|su|sv|sx|sy|sz|tc|td|tel|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|travel|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|xxx|ye|yt|za|zm|zw)(\\W|$).*",
						Pattern.CASE_INSENSITIVE);
		linkPatterns[3] = Pattern
				.compile(".*(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\s+|:|/|$).*");

		symbolsPatterns[0] = Pattern
				.compile("(\\p{InPhonetic_Extensions}|\\p{InLetterlikeSymbols}|\\p{InDingbats}|\\p{InBoxDrawing}|\\p{InBlockElements}|\\p{InGeometricShapes}|\\p{InHalfwidth_and_Fullwidth_Forms}|つ|°|ຈ|░|▀|▄|̰̦̮̠ę̟̹ͦͯͯ́ͮ̊̐͌̉͑ͨ̊́́̚|U̶̧ͩͭͧ͊̅̊ͥͩ̿̔̔ͥ͌ͬ͊͋ͬ҉|Ọ̵͇̖̖|A̴͍̥̳̠̞̹ͩ̋̆ͤͅ|E̡̛͚̺̖̪͈̲̻̠̰̳̐̿)");
		symbolsPatterns[1] = Pattern.compile("[!-/:-@\\[-`{-~]");

		this.setName(BotManager.getInstance().getInstance().nick);
		this.setLogin("CoeBot");
		this.setMessageDelay(0);

		this.setVerbose(BotManager.getInstance().verboseLogging);

		try {
			this.connect(server, port,
					BotManager.getInstance().getInstance().password);
		} catch (NickAlreadyInUseException e) {
			logMain("RB: [ERROR] Nickname already in use - " + this.getNick()
					+ " " + this.getServer());
		} catch (IOException e) {
			logMain("RB: [ERROR] Unable to connect to server - "
					+ this.getNick() + " " + this.getServer());
		} catch (IrcException e) {
			logMain("RB: [ERROR] Error connecting to server - "
					+ this.getNick() + " " + this.getServer());
		}

		startJoinCheck();
	}

	public static ReceiverBot getInstance() {
		return instance;
	}

	public static void setInstance(ReceiverBot rb) {
		if (instance == null) {
			instance = rb;
		}
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

	private Channel getChannelObject(String channel) {
		Channel channelInfo = null;
		channelInfo = BotManager.getInstance().getChannel(channel);
		return channelInfo;
	}

	@Override
	protected void onDeop(String channel, String sourceNick,
			String sourceLogin, String sourceHostname, String recipient) {
		recipient = recipient.replace(":", "");
		System.out.println("DEBUG: Got DEOP for " + recipient + " in channel: "
				+ channel);
		this.getChannelObject(channel).tagModerators.remove(recipient);
	}

	@Override
	protected void onOp(String channel, String sourceNick, String sourceLogin,
			String sourceHostname, String recipient) {
		recipient = recipient.replace(":", "");
		System.out.println("DEBUG: Got OP for " + recipient + " in channel: "
				+ channel);
		this.getChannelObject(channel).tagModerators.add(recipient);
	}

	@Override
	protected void onConnect() {
		// Force TMI to send USERCOLOR AND SPECIALUSER messages.
		this.sendRawLine("TWITCHCLIENT 3");
	}

	@Override
	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		if (!message.startsWith("USERCOLOR") && !message.startsWith("EMOTESET")
				&& !message.startsWith("SPECIALUSER")
				&& !message.startsWith("HISTORYEND")
				&& !message.startsWith("CLEARCHAT")
				&& !message.startsWith("Your color"))
			BotManager.getInstance().log("RB PM: " + sender + " " + message);

		Matcher m = banNoticePattern.matcher(message);
		if (m.matches()) {
			String channel = "#" + m.group(1);
			BotManager.getInstance().log(
					"SB: Detected ban in " + channel + ". Parting..");
			BotManager.getInstance().removeChannel(channel);
		}

		m = toNoticePattern.matcher(message);
		if (m.matches()) {
			String channel = "#" + m.group(1);
			BotManager.getInstance().log(
					"SB: Detected timeout in " + channel + ". Parting..");
			BotManager.getInstance().removeChannel(channel);
		}

		if (sender.equals("jtv"))
			onAdministrativeMessage(message, null);
	}

	@Override
	protected void onAction(String sender, String login, String hostname,
			String target, String action) {
		this.onMessage(target, sender, login, hostname, "/me " + action);
	}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (!BotManager.getInstance().useEventFeed)
			onChannelMessage(channel, channel, sender, message);
	}

	protected void onChannelMessage(String channel, String targetChannel,
			String sender, String message) {
		if (!BotManager.getInstance().verboseLogging
				&& !message.startsWith("USERCOLOR")
				&& !message.startsWith("EMOTESET")
				&& !message.startsWith("SPECIALUSER")
				&& !message.startsWith("HISTORYEND")
				&& !message.startsWith("CLEARCHAT")
				&& !message.startsWith("Your color"))
			logMain("MSG: " + targetChannel + " " + sender + " : " + message);

		Channel channelInfo = getChannelObject(targetChannel);
		String twitchName = channelInfo.getTwitchName();
		String prefix = channelInfo.getPrefix();
		bullet[0] = channelInfo.getChannelBullet();
		// if(privMsgSub){
		// channelInfo.addSubscriber(sender);
		//
		// privMsgSub = false;
		// }
		// if (!channelInfo.active) {
		// System.out.println("DEBUG: Channel not active, message ignored.");
		// return;
		// }

		if (!sender.equalsIgnoreCase(this.getNick()))
			channelInfo.messageCount++; // Inc message count

		// Ignore messages from self.
		if (sender.equalsIgnoreCase(this.getNick())) {
			// System.out.println("Message from bot");
			return;
		}

		// Handle future administrative messages from JTV
		if (sender.equalsIgnoreCase("jtv")) {
			onAdministrativeMessage(message, channelInfo);
			return;
		}

		// Handle twitchnotify
		if (sender.equals("twitchnotify")) {
			Matcher m = twitchnotifySubscriberPattern.matcher(message);
			if (m.matches()) {
				onNewSubscriber(channelInfo, m.group(1));
			}
		}

		// Split message on spaces.
		String[] msg = message.trim().split(" ");

		// ********************************************************************************
		// ****************************** User Ranks
		// **************************************
		// ********************************************************************************

		boolean isAdmin = false;
		boolean isOwner = false;
		boolean isOp = false;
		boolean isRegular = false;
		boolean isSub = false;
		int accessLevel = 0;

		// Check for user level based on other factors.
		if (BotManager.getInstance().isAdmin(sender))
			isAdmin = true;
		if (BotManager.getInstance().isTagAdmin(sender)
				|| BotManager.getInstance().isTagStaff(sender))
			isAdmin = true;
		if (channel.equalsIgnoreCase("#" + sender))
			isOwner = true;
		if (channelInfo.isModerator(sender))
			isOp = true;
		if (channelInfo.isOwner(sender))
			isOwner = true;
		if (channelInfo.isRegular(sender)
				|| (channelInfo.subscriberRegulars && privMsgSub)) {
			isRegular = true;
			privMsgSub = false;
		}
		if (privMsgSub && channelInfo.subsRegsMinusLinks) {
			accessLevel = 1;
			isSub = true;
			privMsgSub = false;
		}
		if (channelInfo.getIgnoredUsers().contains(sender.toLowerCase())) {
			isAdmin = false;
			isOwner = false;
			isRegular = false;
			isSub = false;
		}
		// Give users all the ranks below them
		if (isAdmin) {
			log("RB: " + sender + " is admin.");
			isOwner = true;
			isOp = true;
			isRegular = true;
			isSub = true;
			accessLevel = 99;
		} else if (isOwner) {
			log("RB: " + sender + " is owner.");
			isOp = true;
			isRegular = true;
			isSub = true;
			accessLevel = 3;
		} else if (isOp) {
			log("RB: " + sender + " is op.");
			isRegular = true;
			isSub = true;
			accessLevel = 2;
		} else if (isRegular) {
			log("RB: " + sender + " is regular.");
			isSub = true;
			accessLevel = 1;
		} else if (isSub) {
			log("RB: " + sender + " is a subscriber");
		}

		checkQueued();
		// !{botname} command
		if (msg[0].equalsIgnoreCase(prefix + this.getName())) {
			if (msg.length >= 2) {

				String[] newMsg = new String[msg.length - 1];
				for (int i = 1; i < msg.length; i++) {
					newMsg[i - 1] = msg[i];
				}
				msg = newMsg;
				msg[0] = prefix + msg[0];

				message = fuseArray(msg, 0);
				System.out.println("DEBUG: Command rewritten as " + message);
			}

		}

		// !leave - Owner
		if ((msg[0].equalsIgnoreCase(prefix + "leave")
				|| msg[0].equalsIgnoreCase(prefix + "remove") || msg[0]
					.equalsIgnoreCase(prefix + "part")) && isOwner) {
			send(channel, "Leaving channel " + channelInfo.getChannel() + ".");
			BotManager.getInstance().removeChannel(channelInfo.getChannel());
			return;
		}

		// ********************************************************************************
		// ********************************** Filters
		// *************************************
		// ********************************************************************************

		// Global banned word filter
		if (!isOp && this.isGlobalBannedWord(message)) {
			this.secondaryBan(channel, sender, FilterType.GLOBALBAN);
			logMain("GLOBALBAN: Global banned word timeout: " + sender + " in "
					+ channel + " : " + message);
			logGlobalBan(channel, sender, message);
			return;
		}

		// Voluntary Filters
		if (channelInfo.useFilters) {

			// if (!isRegular) {
			// Matcher m = vinePattern.matcher(message.replaceAll(" ", ""));
			// if (m.find()) {
			// logMain("VINEBAN: " + sender + " in " + channel + " : " +
			// message);
			// this.secondaryBan(channel, sender, FilterType.VINE);
			// logGlobalBan(channel, sender, message);
			// return;
			// }
			// }

			// Me filter
			if (channelInfo.getFilterMe() && !isSub) {
				if (msg[0].equalsIgnoreCase("/me")
						|| message.startsWith("\u0001ACTION")) {
					int warningCount = 0;

					channelInfo.incWarningCount(sender, FilterType.ME);
					warningCount = channelInfo.getWarningCount(sender,
							FilterType.ME);
					this.secondaryTO(channel, sender,
							this.getTODuration(warningCount, channelInfo),
							FilterType.ME, message);

					if (channelInfo.checkSignKicks()) {
						send(channel,
								sender
										+ ", /me is not allowed in this channel - "
										+ this.getTimeoutText(warningCount,
												channelInfo));
						channelInfo.increasePunCount();
					}
					return;

				}

			}

			// Cap filter
			if (channelInfo.getFilterCaps() && !isSub) {
				String messageNoWS = message.replaceAll("\\s", "");
				int capsNumber = getCapsNumber(messageNoWS);
				double capsPercent = ((double) capsNumber / messageNoWS
						.length()) * 100;
				if (message.length() >= channelInfo
						.getfilterCapsMinCharacters()
						&& capsPercent >= channelInfo.getfilterCapsPercent()
						&& capsNumber >= channelInfo.getfilterCapsMinCapitals()) {
					int warningCount = 0;

					channelInfo.incWarningCount(sender, FilterType.CAPS);
					warningCount = channelInfo.getWarningCount(sender,
							FilterType.CAPS);
					this.secondaryTO(channel, sender,
							this.getTODuration(warningCount, channelInfo),
							FilterType.CAPS, message);

					if (channelInfo.checkSignKicks()) {
						send(channel,
								sender
										+ ", please don't shout or talk in all caps - "
										+ this.getTimeoutText(warningCount,
												channelInfo));
						channelInfo.increasePunCount();
					}
					return;
				}
			}

			// Link filter
			if (channelInfo.getFilterLinks() && !(isRegular)
					&& this.containsLink(message, channelInfo)) {
				permitted = channelInfo.linkPermissionCheck(sender);
				int warningCount = 0;
				if (permitted) {
					send(channel, "Link permitted. (" + sender
							+ ")  i.imgur.com/P09uFKd.gif");
				} else {

					channelInfo.incWarningCount(sender, FilterType.LINK);
					warningCount = channelInfo.getWarningCount(sender,
							FilterType.LINK);
					this.secondaryTO(channel, sender,
							this.getTODuration(warningCount, channelInfo),
							FilterType.LINK, message);

					if (channelInfo.checkSignKicks()) {
						send(channel,
								sender
										+ ", please ask a moderator before posting links - "
										+ this.getTimeoutText(warningCount,
												channelInfo));
						channelInfo.increasePunCount();
					}
					return;
				}

			}

			// Length filter
			if (!(isSub) && (message.length() > channelInfo.getFilterMax())) {
				int warningCount = 0;

				channelInfo.incWarningCount(sender, FilterType.LENGTH);
				warningCount = channelInfo.getWarningCount(sender,
						FilterType.LENGTH);
				this.secondaryTO(channel, sender,
						this.getTODuration(warningCount, channelInfo),
						FilterType.LENGTH, message);

				if (channelInfo.checkSignKicks()) {
					send(channel,
							sender
									+ ", please don't spam long messages - "
									+ this.getTimeoutText(warningCount,
											channelInfo));
					channelInfo.increasePunCount();
				}

				return;
			}

			// Symbols filter
			if (channelInfo.getFilterSymbols() && !(isSub)) {
				String messageNoWS = message.replaceAll("\\s", "");
				int count = getSymbolsNumber(messageNoWS);
				double percent = (double) count / messageNoWS.length();

				if (count > channelInfo.getFilterSymbolsMin()
						&& (percent * 100 > channelInfo
								.getFilterSymbolsPercent())) {
					int warningCount = 0;
					channelInfo.incWarningCount(sender, FilterType.SYMBOLS);
					warningCount = channelInfo.getWarningCount(sender,
							FilterType.SYMBOLS);
					this.secondaryTO(channel, sender,
							this.getTODuration(warningCount, channelInfo),
							FilterType.SYMBOLS, message);

					if (channelInfo.checkSignKicks()) {
						send(channel,
								sender
										+ ", please don't spam symbols - "
										+ this.getTimeoutText(warningCount,
												channelInfo));
						channelInfo.increasePunCount();
					}
					return;

				}
			}

			// Offensive filter
			if (!isSub && channelInfo.getFilterOffensive()) {
				boolean isOffensive = channelInfo.isOffensive(message);
				if (isOffensive) {
					int warningCount = 0;

					channelInfo.incWarningCount(sender, FilterType.OFFENSIVE);
					warningCount = channelInfo.getWarningCount(sender,
							FilterType.OFFENSIVE);
					this.secondaryTO(channel, sender,
							this.getTODuration(warningCount, channelInfo),
							FilterType.OFFENSIVE, message);
					channelInfo.increasePunCount();
					if (channelInfo.checkSignKicks()) {
						send(channel,
								sender
										+ ", disallowed word or phrase - "
										+ this.getTimeoutText(warningCount,
												channelInfo));
						channelInfo.increasePunCount();
					}
					return;
				}
			}

			// Emote filter
			if (!isSub && channelInfo.getFilterEmotes()) {
				if (countEmotes(message) > channelInfo.getFilterEmotesMax()) {
					int warningCount = 0;

					channelInfo.incWarningCount(sender, FilterType.EMOTES);
					warningCount = channelInfo.getWarningCount(sender,
							FilterType.EMOTES);
					this.secondaryTO(channel, sender,
							this.getTODuration(warningCount, channelInfo),
							FilterType.EMOTES, message);

					if (channelInfo.checkSignKicks()) {
						send(channel,
								sender
										+ ", please don't spam emotes - "
										+ this.getTimeoutText(warningCount,
												channelInfo));
						channelInfo.increasePunCount();
					}
					return;

				}

				if (channelInfo.getFilterEmotesSingle()
						&& checkSingleEmote(message)) {
					int warningCount = 0;

					channelInfo.incWarningCount(sender, FilterType.EMOTES);
					warningCount = channelInfo.getWarningCount(sender,
							FilterType.EMOTES);
					this.secondaryTO(channel, sender,
							this.getTODuration(warningCount, channelInfo),
							FilterType.EMOTES, message);

					if (channelInfo.checkSignKicks()) {
						send(channel,
								sender
										+ ", single emote messages are not allowed - "
										+ this.getTimeoutText(warningCount,
												channelInfo));
						channelInfo.increasePunCount();
					}
					return;

				}

			}

		}

		// ignore messages from blacklisted users
		if (!BotManager.getInstance().isAdmin(sender)
				&& channelInfo.getIgnoredUsers().contains(sender.toLowerCase())) {

			return;
		}
		// verbose logging on/off
		if (isAdmin && msg[0].equalsIgnoreCase(prefix + "verboseLogging")) {
			if (msg.length > 1) {
				if (msg[1].equalsIgnoreCase("true")
						|| msg[1].equalsIgnoreCase("on")) {
					this.setVerbose(true);
					send(channel, "Verbose Logging turned on.");
				} else if (msg[1].equalsIgnoreCase("false")
						|| msg[1].equalsIgnoreCase("off")) {
					this.setVerbose(false);
					send(channel, "Verbose Logging turned off.");

				}
			}
		}
		// xkcd
		if (msg[0].equalsIgnoreCase(prefix + "xkcd") && msg.length > 1 && isSub) {
			if (isInteger(msg[1])) {
				int number = Integer.parseInt(msg[1]);
				send(channel,
						"XKCD Comic #" + msg[1] + " Title: "
								+ JSONUtil.getXKCDTitle(number) + "; Image: "
								+ JSONUtil.getXKCDImage(number)
								+ " ; Alt-Text: "
								+ JSONUtil.getXKCDAltText(number));
			} else
				send(channel, "Please enter an integer comic number.");
		}
		// Impersonation command
		if (isAdmin && msg[0].equalsIgnoreCase(prefix + "imp")) {
			if (msg.length >= 3) {
				channelInfo = getChannelObject("#" + msg[1]);
				twitchName = channelInfo.getTwitchName();

				String[] newMsg = new String[msg.length - 2];
				for (int i = 2; i < msg.length; i++) {
					newMsg[i - 2] = msg[i];
				}
				msg = newMsg;

				message = fuseArray(msg, 0);
				send(channel,
						"Impersonating channel " + channelInfo.getChannel()
								+ " with command: " + message);
				System.out.println("IMP: Impersonating channel "
						+ channelInfo.getChannel() + " with command: "
						+ message);
			}

		}

		// ********************************************************************************
		// ***************************** Poll Voting
		// **************************************
		// ********************************************************************************
		if (msg[0].equalsIgnoreCase(prefix + "vote")) {
			log("RB: Matched command !vote (user entry)");
			if (channelInfo.getPoll() != null
					&& channelInfo.getPoll().getStatus() && msg.length > 1) {
				channelInfo.getPoll().vote(sender, msg[1]);
				return;
			}
		}
		// ********************************************************************************
		// ***************************** Giveaway Voting
		// **********************************
		// ********************************************************************************
		if (channelInfo.getGiveaway() != null
				&& channelInfo.getGiveaway().getStatus()) {
			// Giveaway is open and accepting entries.
			channelInfo.getGiveaway().submitEntry(sender, msg[0]);
		}

		// ********************************************************************************
		// ***************************** Raffle Entry
		// *************************************
		// ********************************************************************************
		if (msg[0].equalsIgnoreCase(prefix + "raffle") && msg.length == 1) {
			log("RB: Matched command !raffle (user entry)");
			if (channelInfo.raffle != null) {
				channelInfo.raffle.enter(sender);
				return;
			}
		}
		// ********************************************************************************
		// ******************************* Mode Checks
		// ************************************
		// ********************************************************************************

		// Check channel mode.
		if ((channelInfo.getMode() == 0 || channelInfo.getMode() == -1)
				&& !isOwner)
			return;
		if (channelInfo.getMode() == 1 && !isOp)
			return;

		// ********************************************************************************
		// ********************************* Commands
		// *************************************
		// ********************************************************************************

		// Command cooldown check
		if (msg[0].substring(0, 1).equalsIgnoreCase(prefix)
				&& channelInfo.onCooldown(msg[0])) {
			if (!isOp)
				return;
		}

		// !ping - All
		if (msg[0].equalsIgnoreCase(prefix + "ping") && isOp) {
			log("RB: Matched command !ping");
			String time = new java.util.Date().toString();
			send(channel,
					"Pong sent at " + time + " (" + this.fuseArray(msg, 1)
							+ ")");
			return;
		}

		// !lockouttest - All
		if (msg[0].equalsIgnoreCase(prefix + "lockouttest")) {
			log("RB: Matched command !lockouttest");
			send(channel,
					sender
							+ ", your message was received! You are NOT locked out of chat.");
			return;
		}

		// !bothelp - All
		if (msg[0].equalsIgnoreCase(prefix + "bothelp")) {
			log("RB: Matched command !bothelp");
			send(channel, BotManager.getInstance().bothelpMessage);
			return;
		}

		// !viewers - All
		if ((msg[0].equalsIgnoreCase(prefix + "viewers") || msg[0]
				.equalsIgnoreCase(prefix + "lurkers"))) {
			log("RB: Matched command !viewers");

			try {
				send(channel, JSONUtil.krakenViewers(twitchName) + " viewers.");
			} catch (Exception e) {
				send(channel, "Stream is not live.");
			}

			return;
		}
		if ((msg[0].equalsIgnoreCase(prefix + "chatters"))) {
			log("RB: Matched command !viewers");

			try {
				send(channel, JSONUtil.tmiChattersCount(twitchName)
						+ " people currently connected to chat.");
			} catch (Exception e) {
				send(channel, "Stream is not live.");
			}

			return;
		}
		long newConch = System.currentTimeMillis();

		if ((newConch >= (lastConch + 15 * 1000L)) || isOp) {
			if ((msg[0].equalsIgnoreCase(prefix + "conch") || msg[0]
					.equalsIgnoreCase(prefix + "helix")) && isSub) {
				log("RB: Matched command !conch");
				int rand = (int) Math.round(Math.random() * 14);
				if (msg[1].equalsIgnoreCase("#admin") && isAdmin) {
					rand = Integer.parseInt(msg[2]);
				}
				switch (rand) {
				case 0:
					send(channel, "It is certain.");
					break;
				case 1:
					send(channel, "It is decidedly so.");
					break;
				case 2:
					send(channel, "Better not to tell.");
					break;
				case 3:
					send(channel, "You may rely on it.");
					break;
				case 4:
					send(channel, "Don't count on it.");
					break;
				case 5:
					send(channel, "My reply is no.");
					break;
				case 6:
					send(channel, "Very doubtful.");
					break;
				case 7:
					send(channel, "My sources say no.");
					break;
				case 8:
					send(channel, "Most likely.");
					break;
				case 9:
					send(channel, "Signs point to yes.");
					break;
				case 10:
					send(channel, "Outlook doesn't look good.");
					break;
				case 11:
					send(channel, "The future seems hazy on this.");
					break;
				case 12:
					if (channelInfo.getQuoteSize() > 1) {
						send(channel,
								"Maybe these words of wisdom can guide you: (_QUOTE_)");
					} else
						send(channel,
								"I can provide no help for your situation.");
					break;
				default:
					send(channel, "Unable to discern.");
					break;

				}
				lastConch = newConch;
			}
		}
		if (msg[0].equalsIgnoreCase(prefix + "punishstats") && isOp) {
			log("RB: Matched command !punishstats");

			long timeSince = channelInfo.timeSincePunished();
			int days = (int) (timeSince / 86400);
			int hours = (int) ((timeSince / 3600) % 3600);
			int mins = (int) ((timeSince / 60) % 60);
			int seconds = (int) (timeSince % 60);
			String parsedSince = "";
			if (days > 0) {
				parsedSince = "It has been " + days + " days, " + hours
						+ " hours, " + mins + " minutes, and " + seconds
						+ " seconds since a punishment has been issued.";
			} else if (hours > 0) {
				parsedSince = "It has been " + hours + " hours, " + mins
						+ " minutes, and " + seconds
						+ " seconds since a punishment has been issued.";
			} else if (mins > 0) {
				parsedSince = "It has been " + mins + " minutes, and "
						+ seconds
						+ " seconds since a punishment has been issued.";

			} else {
				parsedSince = "It has been " + seconds
						+ " seconds since a punishment has been issued.";
			}
			send(channel, "The number of punishments doled out is "
					+ channelInfo.getPunCount() + ". " + parsedSince);
		}
		// viewerstats
		// channelInfo.checkViewerStats(twitchName);
		// !viewerstats
		if (msg[0].equalsIgnoreCase(prefix + "viewerstats") && isOp) {
			log("RB: Matched command !viewerstats");
			int average = (int) channelInfo.getAverage();
			send(channel,
					"The all-time high viewer count for " + twitchName + " is "
							+ channelInfo.getViewerStats()
							+ " viewers, achieved at: "
							+ channelInfo.getViewerStatsTime()
							+ ". The average viewer count per stream is "
							+ average + " viewers. ");

		}
		if (msg[0].equalsIgnoreCase(prefix + "setStreamCount") && isAdmin) {
			log("RB: Matched command !setCount");
			channelInfo.setStreamCount(Integer.parseInt(msg[1]));
			send(channel, "Stream count has been adjusted");

		}
		if (msg[0].equalsIgnoreCase(prefix + "resetMaxViewers") & isOwner) {
			if (msg.length > 1) {
				int newMax = Integer.parseInt(msg[1]);
				channelInfo.resetMaxViewers(newMax);

				send(channel, "The maximum viewer count has been reset to: "
						+ channelInfo.getViewerStats());
			}
		}

		if (msg[0].equalsIgnoreCase(prefix + "coetime")) {
			String time = Calendar.getInstance().getTime().toString();
			System.out.println(time);
			int indexColon = time.indexOf(":") - 2;
			int end = time.indexOf(" ", indexColon + 1);
			end = time.indexOf(" ", end + 1);
			time = time.substring(indexColon, end);
			send(channel, "CoeTime is currently: " + time);
		}
		// !hug
		if (msg[0].equalsIgnoreCase(prefix + "hug") && isSub) {
			if (msg.length > 1) {
				send(channel, "(>ಠ_ಠ)> " + fuseArray(msg, 1));
			} else
				send(channel, "Syntax is " + prefix + "hug <single word>");
		}

		// !resolution - All
		if (msg[0].equalsIgnoreCase(prefix + "res")
				|| msg[0].equalsIgnoreCase(prefix + "resolution")) {
			log("RB: Matched command !resolution");

			send(channel,
					prefix
							+ "res is unavailable until Twitch adds parts of justin.tv's API into their current API");
			return;
		}

		// !bitrate - All
		if (msg[0].equalsIgnoreCase(prefix + "bitrate")) {
			log("RB: Matched command !resolution");

			send(channel,
					prefix
							+ "bitrate is unavailable until Twitch adds parts of justin.tv's API into their current API");
			return;
		}

		// !uptime - All

		if (msg[0].equalsIgnoreCase(prefix + "uptime")) {
			log("RB: Matched command !uptime");
			try {
				String uptime = JSONUtil.krakenCreated_at(channelInfo
						.getTwitchName());
				send(channel, this.getTimeStreaming(uptime));
			} catch (Exception e) {
				send(channel, "Error accessing Twitch API.");
			}
			return;
		}
		// // !uptime - All
		// if (msg[0].equalsIgnoreCase(prefix + "uptime")) {
		// log("RB: Matched command !uptime");
		// //String time = channelInfo.getUptime();
		// // if (time != null)
		// // send(channel, twitchName + " has been live for " + time);
		// // else
		// // send(channel, "Stream is not currently online.");
		// }
		//
		// // maintaining if the stream is alive or not
		// try {
		// channelInfo.alive(twitchName);
		// } catch (Exception e) {
		// channelInfo.dead(twitchName);
		//
		// }

		// !music - All
		if (msg[0].equalsIgnoreCase(prefix + "music")) {
			log("RB: Matched command !music");
			String currBullet = bullet[0];
			bullet[0] = "♫";
			send(channel,
					"Now playing: " + JSONUtil.lastFM(channelInfo.getLastfm()));
			bullet[0] = currBullet;

		}
		// updates the song if it has changed
		// if (channelInfo.updateSong()) {
		// String currentSong = JSONUtil.lastFM(channelInfo.getLastfm());
		// if (currentSong.equals("(Nothing)")) {
		// // do nothing
		// } else {
		// String currBullet = bullet[0];
		// bullet[0] = "♫";
		// send(channel, "Now playing: " + currentSong);
		// bullet[0] = currBullet;
		// if (currentSong
		// .equalsIgnoreCase("Credits (Rescue Girl) by Jake Kaufman")) {
		// sendCommand(channel, ".me dances");
		// }
		// }
		// }

		String senderTriggered = "";
		if (msg[0].equalsIgnoreCase(prefix + "whalepenis")) {
			if (msg.length > 1 && isOp) {
				senderTriggered = sender;
				if (msg[1].equalsIgnoreCase("on")) {
					channelInfo.setWp(true);
					send(channel, "Whale penis timer has been turned on");

				} else if (msg[1].equalsIgnoreCase("off")) {
					channelInfo.setWp(false);
					send(channel, "Whale penis timer has been turned off");

				} else if (msg[1].equalsIgnoreCase("stats")) {

					long timeSince = channelInfo.timeSinceNoUpdate();
					int days = (int) (timeSince / 86400);
					int hours = (int) ((timeSince / 3600) % 3600);
					int mins = (int) ((timeSince / 60) % 60);
					int seconds = (int) (timeSince % 60);

					if (days > 0) {
						send(channel,
								"It has been "
										+ days
										+ " days, "
										+ hours
										+ " hours, "
										+ mins
										+ " minutes, and "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					} else if (hours > 0) {
						send(channel,
								"It has been "
										+ hours
										+ " hours, "
										+ mins
										+ " minutes, and "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					} else if (mins > 0) {
						send(channel,
								"It has been "
										+ mins
										+ " minutes, and "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					} else {
						send(channel,
								"It has been "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					}
				} else
					send(channel, "Command syntax: " + prefix
							+ "whalepenis <on/off/stats>");
			}
		}

		// whale penis timer

		String combined = this.fuseArray(msg, 0);
		combined = combined.toLowerCase();

		if (((combined.indexOf("whalepenis") > -1) || (combined
				.indexOf("whale penis") > -1))
				&& channelInfo.getWp()
				&& !sender.equalsIgnoreCase(getNick())
				&& !sender.equalsIgnoreCase(senderTriggered)) {

			channelInfo.increaseWpCount();
			long dummy = channelInfo.timeSinceSaid();

		}

		// !lastfm - All
		if (msg[0].equalsIgnoreCase(prefix + "lastfm")) {
			log("RB: Matched command !lastfm");
			send(channel, "http://www.last.fm/user/" + channelInfo.getLastfm());
		}

		// !steam - All
		// if (msg[0].equalsIgnoreCase(prefix + "steam")) {
		// log("RB: Matched command !steam");
		// if (channelInfo.getSteam().length() > 1) {
		//
		// if (channelInfo.getSteam().length() > 1) {
		// send(channel, JSONUtil.steam(channelInfo.getSteam(), "all"));
		// }
		//
		// } else {
		// send(channel,
		// "Steam ID not set. Do \"!set steam [ID]\" to configure. ID must be in SteamID64 format and profile must be public.");
		// }
		// return;
		// }
		if (msg[0].equalsIgnoreCase(prefix + "songlink")) {
			String songName = JSONUtil.lastFM(channelInfo.getLastfm());
			String url = JSONUtil.lastFMURL(channelInfo.getLastfm());
			send(channel, "You can get " + songName + " at " + url);
		}
		// statusgame
		if (msg[0].equalsIgnoreCase(prefix + "statusgame")
				&& BotManager.getInstance().twitchChannels) {
			log("RB: Matched command !statusgame");
			if (isOp && msg.length > 1) {
				String newStatus = this.fuseArray(msg, 1);
				newStatus.trim();

				try {
					channelInfo.updateStatus(newStatus);
					if (JSONUtil.steam(channelInfo.getSteam(), "game").equals(
							"(unavailable)")) {
						channelInfo.updateGame("");
					} else
						channelInfo.updateGame(JSONUtil.steam(
								channelInfo.getSteam(), "game"));
					send(channel, "Game and Status update sent.");
				} catch (Exception ex) {
					send(channel,
							"Error updating game and/or status. Did you add me as an editor?");
				}

			}

		}
		if (msg[0].equalsIgnoreCase(prefix + "wiki") && isRegular) {
			if (msg.length > 1) {
				String searchTerm = fuseArray(msg, 1);
				String response = JSONUtil.getWiki(searchTerm, 1);
				if (response.length() > 256) {
					response = response.substring(0, 256);
				}
				send(channel, response);
			} else {
				send(channel, "Usage is " + prefix + "wiki <article name>");
			}
		}
		// steamgame
		if (msg[0].equalsIgnoreCase(prefix + "steamgame")
				&& BotManager.getInstance().twitchChannels) {
			log("RB: Matched command !steamgame");
			if (isOp) {

				try {

					if (JSONUtil.steam(channelInfo.getSteam(), "game").equals(
							"(unavailable)")) {
						channelInfo.updateGame("");
						send(channel,
								"Steam game unavailable, changed game to \"Not Playing\" status.");
					} else {
						channelInfo.updateGame(JSONUtil.steam(
								channelInfo.getSteam(), "game"));
						send(channel,
								"Steam game updated to "
										+ JSONUtil.steam(
												channelInfo.getSteam(), "game"));
					}
				} catch (Exception ex) {
					send(channel,
							"Error updating game. Did you add me as an editor?");
				}

			}

		}
		// !me
		if (msg[0].equalsIgnoreCase(prefix + "me") && isOp) {
			if (msg.length > 1) {
				String rest = fuseArray(msg, 1);
				sendCommand(channel, ".me " + rest);
			} else
				send(channel, "Useage is " + prefix + "me <string>");
		}
		// highlight
		// if (msg[0].equalsIgnoreCase(prefix + "highlight")
		// && BotManager.getInstance().twitchChannels) {
		// log("RB: Matched command !highlight");
		// if (isOp && msg.length > 3 && msg[1].equalsIgnoreCase("add")) {
		// int mins = Integer.parseInt(msg[2]);
		// String description = this.fuseArray(msg, 3);
		// description.trim();
		//
		// try {
		// readHighlights("highlight" + channel + ".txt");
		// } catch (Exception e) {
		// send(channel,
		// "Error with the highlight list, it probably hasn't been created yet");
		// }
		// try {
		// String uptime = this.getStreamList("up_time", channelInfo);
		// String timeStreaming = this.getTimeStreaming(uptime, mins);
		// String status = JSONUtil.krakenStatus(twitchName);
		// highlightList.add(timeStreaming + " - " + description
		// + " in stream " + status);
		// save("highlight" + channel + ".txt", highlightList);
		//
		// send(channel, "Highlight marked at " + timeStreaming
		// + " with description: \"" + description
		// + "\" in stream: \"" + status + "\"");
		// } catch (Exception e) {
		// send(channel, "Error accessing Twitch API");
		// }
		//
		// } else if (isOp && msg.length > 2) {
		// if (msg[1].equalsIgnoreCase("delete")
		// || msg[1].equalsIgnoreCase("remove")) {
		// int index = Integer.parseInt(msg[2]);
		// try {
		// readHighlights("highlight" + channel + ".txt");
		// highlightList.remove(index);
		// save("highlight" + channel + ".txt", highlightList);
		//
		// send(channel, "Highlight # " + index + " removed");
		// } catch (Exception e) {
		// send(channel, "Error deleting highlight.");
		// }
		// } else if (msg[1].equalsIgnoreCase("get")) {
		// int index = Integer.parseInt(msg[2]);
		// try {
		// readHighlights("highlight" + channel + ".txt");
		// send(channel,
		// "Highlight Start: " + highlightList.get(index));
		//
		// } catch (Exception e) {
		// send(channel, "Error getting highlight.");
		// }
		// }
		// } else {
		// send(channel,
		// "Highlight syntax is \"!highlight <add> <minutes> <description>\" or \"!highlight <get/delete> <index>\"");
		// }
		//
		// }

		// !game - All

		if (msg[0].equalsIgnoreCase(prefix + "game")
				&& BotManager.getInstance().twitchChannels) {
			log("RB: Matched command !game");
			if (isOp && msg.length > 1) {
				String game = this.fuseArray(msg, 1);
				game.trim();
				if (game.equals("-"))
					game = "";
				try {
					channelInfo.updateGame(game);
					send(channel, "Game update sent.");
				} catch (Exception ex) {
					send(channel,
							"Error updating game. Did you add me as an editor?");
				}

			} else {
				String game = JSONUtil.krakenGame(twitchName);
				if (game.length() > 0) {
					send(channel, "Current game: " + game);
				} else {
					send(channel, "No game set.");
				}
			}
			return;
		}

		// !status - All
		if (msg[0].equalsIgnoreCase(prefix + "status")) {
			log("RB: Matched command !status");
			if (isOp && msg.length > 1
					&& BotManager.getInstance().twitchChannels) {
				String status = this.fuseArray(msg, 1);
				status.trim();
				try {
					channelInfo.updateStatus(status);
					send(channel, "Status update sent.");
				} catch (Exception ex) {
					send(channel,
							"Error updating status. Did you add me as an editor?");
				}
			} else {
				String status = "";

				status = JSONUtil.krakenStatus(twitchName);

				if (status.length() > 0) {
					send(channel, status);
				} else {
					send(channel, "Unable to query API.");
				}
			}
			return;
		}

		// LMGTFY command
		if (msg[0].equalsIgnoreCase(prefix + "google") && isSub) {
			log("RB: Matched command !google");
			if (msg.length > 1) {
				String rawQuery = fuseArray(msg, 1);
				String encodedQuery = "";
				try {
					encodedQuery = URLEncoder.encode(rawQuery, "UTF-8");
				} catch (UnsupportedEncodingException e) {

					e.printStackTrace();
				}
				String url = "http://google.com/#q=" + encodedQuery;
				send(channel, JSONUtil.googURL(url));
			}
		}
		// isLive
		if (msg[0].equalsIgnoreCase(prefix + "islive") && isOp) {
			if (msg.length > 1) {
				if (JSONUtil.krakenIsLive(msg[1].toLowerCase())) {
					send(channel, "Yes, " + msg[1].toLowerCase()
							+ " is streaming " + JSONUtil.krakenGame(msg[1])
							+ " right now.");
				} else
					send(channel, "No, " + msg[1].toLowerCase()
							+ " isn't streaming right now.");
			} else {
				if (JSONUtil.krakenIsLive(channel.substring(1))) {
					send(channel, "Yes, " + channel.substring(1)
							+ " is streaming right now.");
				} else
					send(channel, "No, " + channel.substring(1)
							+ " isn't streaming right now.");
			}
		}

		if (msg[0].equalsIgnoreCase(prefix + "ishere") && isOp) {
			if (msg.length > 1) {
				if (JSONUtil.tmiChatters(channel.substring(1)).contains(
						msg[1].toLowerCase())) {
					send(channel, "Yes, " + msg[1] + " is connected to chat.");
				} else
					send(channel, "No, " + msg[1]
							+ " is not connected to chat.");

			}
		}
		// host
		if (msg[0].equalsIgnoreCase(prefix + "host") && isOwner) {
			if (isOwner && msg.length > 1) {
				if (isOwner && msg[1].equalsIgnoreCase("random")) {

					ArrayList<String> whitelisted = channelInfo
							.getRaidWhitelist();
					int rand = (int) (Math.random() * whitelisted.size() - 1);
					boolean found = false;

					while (whitelisted.size() > 0) {
						if (!JSONUtil.krakenIsLive(whitelisted.get(rand))) {
							whitelisted.remove(rand);
							rand = (int) (Math.random() * whitelisted.size() - 1);
						} else {
							found = true;
							break;
						}
					}
					if (found) {
						send(channel, "Now hosting: " + whitelisted.get(rand));
						sendCommand(channel, ".host " + whitelisted.get(rand));

					} else
						send(channel,
								"None of the whitelisted channels are streaming right now.");

				} else if (msg[1].equalsIgnoreCase("samegame") && isOwner) {
					String response = JSONUtil.getGameChannel(JSONUtil
							.krakenGame(twitchName));
					if (response
							.equalsIgnoreCase("No other channels playing this game")
							|| response.equalsIgnoreCase("Error Querying API")) {
						send(channel, response);
					} else {
						send(channel, "Now hosting: " + response);
						sendCommand(channel, ".host " + response);
					}

				} else {
					if (JSONUtil.krakenIsLive(msg[1])) {
						send(channel, "Now hosting: " + msg[1]);
						sendCommand(channel, ".host " + msg[1]);
					} else
						send(channel, msg[1] + " isn't streaming right now.");
				}

			} else
				send(channel, "Syntax is " + prefix
						+ "host <random/channelName>");
		}
		if (msg[0].equalsIgnoreCase(prefix + "unhost") && isOwner) {
			sendCommand(channel, ".unhost");
		}
		// raid commands
		if (msg[0].equalsIgnoreCase(prefix + "raid") && isOwner) {
			if (isOwner && msg.length > 3) {
				if (msg[1].equalsIgnoreCase("whitelist")) {
					if (msg[2].equalsIgnoreCase("add")) {
						String channelname = msg[3].toLowerCase();
						if (!channelInfo.getRaidWhitelist().contains(
								channelname)) {
							channelInfo.addRaidWhitelist(msg[3]);
							send(channel, msg[3]
									+ " has been added to the raid whitelist.");
						} else
							send(channel, channelname
									+ " is already in the raid whitelist");

					} else if ((msg[2].equalsIgnoreCase("delete") || msg[2]
							.equalsIgnoreCase("remove"))) {
						String channelname = msg[3].toLowerCase();
						if (channelInfo.getRaidWhitelist()
								.contains(channelname)) {
							channelInfo.deleteRaidWhitelist(msg[3]);
							send(channel,
									msg[3]
											+ " has been removed from the raid whitelist.");
						} else
							send(channel, channelname
									+ " wasn't in the raid whitelist.");

					} else
						send(channel, "Syntax is " + prefix
								+ "raid whitelist <add/remove> <channelName>");
				}

			} else if (isOwner && msg.length > 1) {
				if (isOwner && msg[1].equalsIgnoreCase("random")) {

					ArrayList<String> whitelisted = channelInfo
							.getRaidWhitelist();
					int rand = (int) (Math.random() * whitelisted.size() - 1);
					boolean found = false;

					while (whitelisted.size() > 0) {
						if (!JSONUtil.krakenIsLive(whitelisted.get(rand))) {
							whitelisted.remove(rand);
							rand = (int) (Math.random() * whitelisted.size() - 1);
						} else {
							found = true;
							break;
						}
					}
					if (found)
						send(channel, "Go raid " + whitelisted.get(rand)
								+ "! http://twitch.tv/" + whitelisted.get(rand));
					else
						send(channel,
								"None of the whitelisted channels are streaming right now.");
				} else if (isOwner && msg[1].equalsIgnoreCase("list")) {
					ArrayList<String> list = channelInfo.getRaidWhitelist();
					String raidList = "";
					for (int i = 0; i < list.size(); i++) {
						if (i == list.size() - 1) {
							raidList += list.get(i);
						} else
							raidList += list.get(i) + ", ";
					}
					send(channel, raidList);
				} else if (msg[1].equalsIgnoreCase("samegame") && isOwner) {
					String response = JSONUtil.getGameChannel(JSONUtil
							.krakenGame(twitchName));
					if (response
							.equalsIgnoreCase("No other channels playing this game")
							|| response.equalsIgnoreCase("Error Querying API")) {
						send(channel, response);
					} else {
						send(channel, "Go raid " + response
								+ "! http://twitch.tv/" + response);
					}

				} else
					send(channel, "Go raid " + msg[1] + "! http://twitch.tv/"
							+ msg[1]);
			} else
				send(channel, "Syntax is " + prefix
						+ "raid whitelist <add/delete> <channelName> or "
						+ prefix + "raid <list/random/channelName>");
		}
		// !followme - Owner
		if (msg[0].equalsIgnoreCase(prefix + "followme") && isOwner
				&& BotManager.getInstance().twitchChannels) {
			log("RB: Matched command !followme");
			BotManager.getInstance().followChannel(twitchName);
			send(channel, "Follow update sent.");
			return;
		}

		// !properties - Owner
		if (msg[0].equalsIgnoreCase(prefix + "properties") && isOp
				&& BotManager.getInstance().twitchChannels) {
			log("RB: Matched command !properties");
			send(channel,
					JSONUtil.getChatProperties(channelInfo.getTwitchName()));
			return;
		}

		// !commands - Op
		if ((msg[0].equalsIgnoreCase(prefix + "commands") || msg[0]
				.equalsIgnoreCase(prefix + "coemands")) && isOp) {
			log("RB: Matched command !commands");

			ArrayList<String> sorted = channelInfo.getCommandList();
			String sortedList = "";
			for (int i = 0; i < sorted.size(); i++) {
				if (i == sorted.size() - 1) {
					sortedList += sorted.get(i);
				} else
					sortedList += sorted.get(i) + ", ";
			}
			send(channel, "Commands: " + sortedList);
			return;
		}
		String msgs = fuseArray(msg, 0);
		if ((((msgs.indexOf("youtube.com/watch?v=") > -1) || msgs
				.indexOf("youtu.be/") > -1) && (permitted || isRegular))
				&& !msgs.contains("--ignore")) {
			msgs.trim();
			if (msgs.indexOf("youtube.com/watch?v=") > -1) {
				int indexOfId = msgs.indexOf("=") + 1;

				int indexOfSpace = msgs.indexOf(" ", indexOfId);
				int indexOfPound = msgs.indexOf("#", indexOfId);
				String id = "";

				if (indexOfSpace > -1) {
					if (indexOfPound > -1 && indexOfPound <= indexOfSpace) {
						id = msgs.substring(indexOfId, indexOfPound);
					} else {
						id = msgs.substring(indexOfId, indexOfSpace);
					}
				} else {
					if (indexOfPound > -1)
						id = msgs.substring(indexOfId, indexOfPound);
					else
						id = msgs.substring(indexOfId);
				}
				log("youtube id  " + id);
				String title = JSONUtil.youtubeTitle(id);
				send(channel, "Linked Youtube Video: \"" + title + "\"");

			} else {
				int indexOfId = msgs.indexOf(".be/") + 4;
				int indexOfSpace = msgs.indexOf(" ", indexOfId);
				String id = "";
				if (indexOfSpace > -1) {
					id = msgs.substring(indexOfId, indexOfSpace);
				} else {
					id = msgs.substring(indexOfId);
				}
				log("youtube id  " + id);
				String title = JSONUtil.youtubeTitle(id);
				send(channel, "Linked Youtube Video: \"" + title + "\"");
			}
		}
		// !throw - All
		if (msg[0].equalsIgnoreCase(prefix + "throw") && (isSub)) {
			log("RB: Matched command !throw");
			if (msg.length > 1) {

				send(channel, " (╯°□°）╯彡" + fuseArray(msg, 1));
			}

		}

		// !topic
		if (msg[0].equalsIgnoreCase(prefix + "topic") && channelInfo.useTopic) {
			log("RB: Matched command !topic");
			if (msg.length < 2 || !isOp) {
				if (channelInfo.getTopic().equalsIgnoreCase("")) {
					if (BotManager.getInstance().twitchChannels) {
						String status = "";

						status = JSONUtil.krakenStatus(twitchName);

						if (status.length() > 0)
							send(channel, status);
						else
							send(channel, "Unable to query API.");
					} else {
						send(channel, "Topic not set");
					}
				} else {
					send(channel, "Topic: " + channelInfo.getTopic() + " (Set "
							+ channelInfo.getTopicTime() + " ago)");
				}
			} else if (msg.length > 1 && isOp) {
				if (msg[1].equalsIgnoreCase("unset")) {
					channelInfo.setTopic("");
					send(channel, "No topic is set.");
				} else {
					channelInfo.setTopic(message.substring(7));
					send(channel, "Topic: " + channelInfo.getTopic() + " (Set "
							+ channelInfo.getTopicTime() + " ago)");
				}

			}
			return;
		}

		// !link
		if (msg[0].equalsIgnoreCase(prefix + "link") && isSub) {
			log("RB: Matched command !link");
			if (msg.length > 1) {
				String rawQuery = message.substring(6);
				String encodedQuery = "";
				try {
					encodedQuery = URLEncoder.encode(rawQuery, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String url = "http://lmgtfy.com/?q=" + encodedQuery;
				send(channel,
						"Link to \"" + rawQuery + "\": "
								+ JSONUtil.googURL(url));
			}
			return;
		}

		// !commercial
		if ((msg[0].equalsIgnoreCase(prefix + "commercial") || msg[0]
				.equalsIgnoreCase(prefix + "coemercial"))
				&& BotManager.getInstance().twitchChannels && isOp) {
			if (isOp) {
				log("RB: Matched command !commercial");

				if (msg.length > 1) {
					if (Integer.parseInt(msg[1]) % 30 == 0
							&& Integer.parseInt(msg[1]) <= 180) {
						channelInfo
								.scheduleCommercial(Integer.parseInt(msg[1]));

						send(channel,
								"A "
										+ msg[1]
										+ " second commercial will be run in 45 seconds. Thank you for supporting this channel!");
					} else
						send(channel,
								"Commercials must be a multiple of 30 seconds and less than 180 seconds long.");
				} else {
					channelInfo.scheduleCommercial();
					send(channel,
							"A commercial will be run in 45 seconds. Thank you for supporting this channel!");
				}

			}

		}
		if (msg[0].equalsIgnoreCase(prefix + "cancel") && isOp) {
			log("RB: Matched command !cancel");

			channelInfo.cancelCommercial();
			send(channel, "The commercial has been cancelled.");

		}
		if (msg[0].equalsIgnoreCase(prefix + "snooze") && isOp) {
			log("RB: Matched command !snooze");

			channelInfo.snoozeCommercial();
			send(channel, "The commercial has been delayed by five minutes.");

		}

		if (msg[0].equalsIgnoreCase(prefix + "testChannelSend") && isOp) {
			log("RB: Matched command !testChannelSend");

			channelInfo.testChannelSend();

		}
		if ((msg[0].equalsIgnoreCase(prefix + "strawpoll") && isOp && msg.length > 1)) {
			if (msg.length > 3) {
				String newString = fuseArray(msg, 1);
				String[] params = newString.split(";");
				String[] options = params[1].split(",");
				String title = "title=" + URLEncoder.encode(params[0]);
				String multi = "&multi=" + false;
				String permissive = "&permissive=" + false;
				String optionsStr = "";

				for (String s : options) {
					optionsStr += "&options[]=" + URLEncoder.encode(s);
				}
				String urlTestString = title + optionsStr + multi + permissive;

				String id = BotManager.postRemoteDataStrawpoll(urlTestString);
				if (id != null) {
					channelInfo.setLastStrawpoll(Integer.parseInt(id));
					send(channel, "Strawpoll.me/" + id);
				}
			} else if (msg[1].equalsIgnoreCase("results")) {

				String strawpollHtml = BotManager
						.getRemoteContent("http://strawpoll.me/"
								+ channelInfo.getLastStrawpoll() + "/r");

				int pollOptionName = strawpollHtml.indexOf("pollOptionName");
				int end = strawpollHtml.indexOf("<", pollOptionName + 1);
				ArrayList<String> options = new ArrayList<String>();
				int pollOptionStanding = strawpollHtml.indexOf("<span>", end);
				int standingEnd = strawpollHtml.indexOf("vote",
						pollOptionStanding);
				ArrayList<String> votes = new ArrayList<String>();
				System.out.println(pollOptionName + " " + end + " "
						+ pollOptionStanding + " " + standingEnd);
				while (pollOptionName > -1) {
					options.add(strawpollHtml.substring(pollOptionName + 16,
							end));
					votes.add(strawpollHtml.substring(pollOptionStanding + 6,
							standingEnd - 1));
					pollOptionName = strawpollHtml.indexOf("pollOptionName",
							end);
					end = strawpollHtml.indexOf("<", pollOptionName + 1);
					pollOptionStanding = strawpollHtml.indexOf("<span>", end);
					standingEnd = strawpollHtml.indexOf("vote",
							pollOptionStanding);

				}
				String results = "";
				for (int i = 0; i < options.size(); i++) {
					results += options.get(i) + ": " + votes.get(i) + "; ";
				}
				send(channel, results);
			}
		}
		// !command - Ops
		if ((msg[0].equalsIgnoreCase(prefix + "command") || (msg[0]
				.equalsIgnoreCase(prefix + "coemand"))) && isOp) {
			log("RB: Matched command !command");
			if (msg.length < 3) {
				send(channel,
						"Syntax: \"!command add/delete [name] [message]\" - Name is the command trigger without \"!\" and message is the response.");
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					String value = fuseArray(msg, 3);

					channelInfo.setCommand(key, value);
					if (value.contains("(_PURGE_)")
							|| value.contains("(_TIMEOUT_)")
							|| value.contains("(_BAN_)")
							|| value.contains("(_COMMERCIAL_)")) {
						channelInfo.setCommandsRestriction(key, 2);
					} else
						channelInfo.setCommandsRestriction(key, 1);

					// if(channelInfo.checkCommandRestriction(key,3)){
					// channelInfo.setCommandsRestriction(key, 3);
					// }else if(channelInfo.checkCommandRestriction(key,2)){
					// channelInfo.setCommandsRestriction(key, 2);
					// }else if(channelInfo.checkCommandRestriction(key,1)){
					// channelInfo.setCommandsRestriction(key, 1);
					// }else
					// channelInfo.setCommandsRestriction(key, 0);

					send(channel, "Command added/updated.");

				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					String key = msg[2].toLowerCase();
					channelInfo.removeCommand(key);

					channelInfo.removeRepeatCommand(key);
					channelInfo.removeScheduledCommand(key);

					send(channel, "Command " + key + " removed.");

				} else if (msg[1].equalsIgnoreCase("restrict")
						&& msg.length >= 4) {
					String command = msg[2].toLowerCase();
					String levelStr = msg[3].toLowerCase();
					int level = 0;
					if (channelInfo.getCommand(command) != null) {
						if (levelStr.equalsIgnoreCase("owner")
								|| levelStr.equalsIgnoreCase("owners"))
							level = 3;
						if (levelStr.equalsIgnoreCase("mod")
								|| levelStr.equalsIgnoreCase("mods"))
							level = 2;
						if (levelStr.equalsIgnoreCase("regular")
								|| levelStr.equalsIgnoreCase("regulars"))
							level = 1;
						if (levelStr.equalsIgnoreCase("everyone"))
							level = 0;

						if (channelInfo.setCommandsRestriction(command, level))
							send(channel, prefix + command + " restricted to "
									+ levelStr + " only.");
						else
							send(channel, "Error setting restriction.");
					} else {
						send(channel, "Command does not exist.");
					}
				}
			}
			return;
		}

		// !listcommands
		// if (msg[0].equalsIgnoreCase(prefix + "listcommands")) {
		// log("RB: Matched command !listcommands");
		//
		// send(channel, "There are " + commandList.size()/2 + " commands.");
		// for (int i = 0; i < commandList.size(); i+=2){
		//
		// send(channel, commandList.get(i) + " - " + commandList.get(i+1));
		// }
		//
		// }
		//

		// !repeat - Ops
		if (msg[0].equalsIgnoreCase(prefix + "repeat") && isOp) {
			log("RB: Matched command !repeat");
			if (msg.length < 3) {
				if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
					String commandsRepeatKey = "";

					Iterator itr = channelInfo.commandsRepeat.entrySet()
							.iterator();

					while (itr.hasNext()) {
						Map.Entry pairs = (Map.Entry) itr.next();
						RepeatCommand rc = (RepeatCommand) pairs.getValue();
						commandsRepeatKey += pairs.getKey() + " ["
								+ (rc.active == true ? "ON" : "OFF") + "] ("
								+ rc.delay + ")" + ", ";
					}
					send(channel, "Repeating commands: " + commandsRepeatKey);
				} else {
					send(channel,
							"Syntax: \"!repeat add/delete [commandname] [delay in seconds] [message difference - optional]\"");
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String key = msg[2];
					try {
						int delay = Integer.parseInt(msg[3]);
						int difference = 1;
						if (msg.length == 5)
							difference = Integer.parseInt(msg[4]);

						if (channelInfo.getCommand(key).equalsIgnoreCase(
								"invalid")
								|| delay < 30) {
							// Key not found or delay to short
							send(channel,
									"Command not found or delay is less than 30 seconds.");
						} else {
							channelInfo
									.setRepeatCommand(key, delay, difference);
							send(channel, "Command " + key
									+ " will repeat every " + delay
									+ " seconds if " + difference
									+ " messages have passed.");
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}

				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					String key = msg[2];

					channelInfo.removeRepeatCommand(key);
					send(channel, "Command " + key + " will no longer repeat.");

				} else if (msg[1].equalsIgnoreCase("on")
						|| msg[1].equalsIgnoreCase("off")) {
					String key = msg[2];
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setRepeatCommandStatus(key, true);
						send(channel, "Repeat command " + key
								+ " has been enabled.");
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setRepeatCommandStatus(key, false);
						send(channel, "Repeat command " + key
								+ " has been disabled.");
					}

				}
			}
			return;
		}

		// !schedule - Ops
		if (msg[0].equalsIgnoreCase(prefix + "schedule") && isOp) {
			log("RB: Matched command !schedule");
			if (msg.length < 3) {
				if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
					String commandsScheduleKey = "";

					Iterator itr = channelInfo.commandsSchedule.entrySet()
							.iterator();

					while (itr.hasNext()) {
						Map.Entry pairs = (Map.Entry) itr.next();
						ScheduledCommand sc = (ScheduledCommand) pairs
								.getValue();
						commandsScheduleKey += pairs.getKey() + " ["
								+ (sc.active == true ? "ON" : "OFF") + "]"
								+ ", ";
					}
					send(channel, "Scheduled commands: " + commandsScheduleKey);
				} else {
					send(channel,
							"Syntax: \"!schedule add/delete/on/off [commandname] [pattern] [message difference - optional]\"");
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String key = msg[2];
					try {
						String pattern = msg[3];
						if (pattern.equals("hourly"))
							pattern = "0 * * * *";
						else if (pattern.equals("semihourly"))
							pattern = "0,30 * * * *";
						else
							pattern = pattern.replace("_", " ");

						int difference = 1;
						if (msg.length == 5)
							difference = Integer.parseInt(msg[4]);

						if (channelInfo.getCommand(key).equalsIgnoreCase(
								"invalid")
								|| pattern.contains(",,")) {
							// Key not found or delay to short
							send(channel,
									"Command not found or invalid pattern.");
						} else {
							channelInfo.setScheduledCommand(key, pattern,
									difference);
							send(channel, "Command " + key
									+ " will repeat every " + pattern + " if "
									+ difference + " messages have passed.");
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}

				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					String key = msg[2];
					channelInfo.removeScheduledCommand(key);
					send(channel, "Command " + key + " will no longer repeat.");

				} else if (msg[1].equalsIgnoreCase("on")
						|| msg[1].equalsIgnoreCase("off")) {
					String key = msg[2];
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setScheduledCommandStatus(key, true);
						send(channel, "Scheduled command " + key
								+ " has been enabled.");
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setScheduledCommandStatus(key, false);
						send(channel, "Scheduled command " + key
								+ " has been disabled.");
					}

				}
			}
			return;
		}

		// !autoreply - Ops
		if (msg[0].equalsIgnoreCase(prefix + "autoreply") && isOp) {
			log("RB: Matched command !autoreply");
			if (msg.length < 3) {
				if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
					for (int i = 0; i < channelInfo.autoReplyTrigger.size(); i++) {
						String cleanedTrigger = channelInfo.autoReplyTrigger
								.get(i).toString().replaceAll("\\.\\*", "*")
								.replaceAll("\\\\Q", "")
								.replaceAll("\\\\E", "");
						send(channel,
								"[" + (i + 1) + "] " + cleanedTrigger
										+ " ---> "
										+ channelInfo.autoReplyResponse.get(i));
					}
				} else {
					send(channel,
							"Syntax: \"!autoreply add/delete/list [pattern] [response]\"");
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String pattern = msg[2].replaceAll("_", " ");
					String response = fuseArray(msg, 3);

					channelInfo.addAutoReply(pattern, response);
					send(channel, "Autoreply added.");
				} else if ((msg[1].equalsIgnoreCase("delete") || msg[1]
						.equalsIgnoreCase("remove")) && msg.length > 2) {
					if (Main.isInteger(msg[2])) {
						int pos = Integer.parseInt(msg[2]);

						if (channelInfo.removeAutoReply(pos))
							send(channel, "Autoreply removed.");
						else
							send(channel,
									"Autoreply not found. Are you sure you have the correct number?");
					}
				}
			}
			return;
		}

		// !poll - Ops
		if (msg[0].equalsIgnoreCase(prefix + "poll") && isOp) {
			log("RB: Matched command !poll");
			if (msg.length < 2) {
				send(channel, "Syntax: \"" + prefix
						+ "poll create [option option ... option]\"");
			} else if (msg.length >= 2) {
				if (msg[1].equalsIgnoreCase("create")) {
					String[] options = new String[msg.length - 2];
					int oc = 0;
					for (int c = 2; c < msg.length; c++) {
						options[oc] = msg[c];
						oc++;
					}
					channelInfo.setPoll(new Poll(options));
					send(channel, "Poll created. Do '" + prefix
							+ "poll start' to start voting.");
				} else if (msg[1].equalsIgnoreCase("start")) {
					if (channelInfo.getPoll() != null) {
						if (channelInfo.getPoll().getStatus()) {
							send(channel, "Poll is alreay running.");
						} else {
							channelInfo.getPoll().setStatus(true);
							send(channel, "Poll started. Type: " + prefix
									+ "vote <option> to start voting.");
						}
					}
				} else if (msg[1].equalsIgnoreCase("stop")) {
					if (channelInfo.getPoll() != null) {
						if (channelInfo.getPoll().getStatus()) {
							channelInfo.getPoll().setStatus(false);
							send(channel, "Poll stopped.");
						} else {
							send(channel, "Poll is not running.");
						}
					}
				} else if (msg[1].equalsIgnoreCase("results")) {
					if (channelInfo.getPoll() != null) {
						send(channel, channelInfo.getPoll().getResultsString());
						// String[] results =
						// channelInfo.getPoll().getResults();
						// for(int c=0;c<results.length;c++){
						// send(channel, results[c]);
						// }
					}

				}
			}
			return;
		}

		// !giveaway - Ops
		if ((msg[0].equalsIgnoreCase(prefix + "giveaway") || msg[0]
				.equalsIgnoreCase("!ga")) && isOp) {
			log("RB: Matched command !giveaway");
			if (msg.length < 2) {
				send(channel,
						"Syntax: \"!giveaway create [max number] [time to run in seconds]\". Time is optional.");
			} else if (msg.length >= 2) {
				if (msg[1].equalsIgnoreCase("create")) {
					String max = "" + 100;
					if (msg.length > 2) {
						max = msg[2];
					}
					channelInfo.setGiveaway(new Giveaway(max));
					if (msg.length > 3
							&& channelInfo.getGiveaway().isInteger(msg[3])) {
						this.startGaTimer(Integer.parseInt(msg[3]), channelInfo);
					} else {
						send(channel,
								"Giveaway created. Do !giveaway start' to start."
										+ " Range 1-"
										+ channelInfo.getGiveaway().getMax()
										+ ".");
					}
				} else if (msg[1].equalsIgnoreCase("start")) {
					if (channelInfo.getGiveaway() != null) {
						if (channelInfo.getGiveaway().getStatus()) {
							send(channel, "Giveaway is alreay running.");
						} else {
							channelInfo.getGiveaway().setStatus(true);
							send(channel, "Giveaway started.");
						}
					}
				} else if (msg[1].equalsIgnoreCase("stop")) {
					if (channelInfo.getGiveaway() != null) {
						if (channelInfo.getGiveaway().getStatus()) {
							channelInfo.getGiveaway().setStatus(false);
							send(channel, "Giveaway stopped.");
						} else {
							send(channel, "Giveaway is not running.");
						}
					}
				} else if (msg[1].equalsIgnoreCase("results")) {
					if (channelInfo.getGiveaway() != null) {
						send(channel, channelInfo.getGiveaway()
								.getResultsString());
						// String[] results =
						// channelInfo.getGiveaway().getResults();
						// for(int c=0;c<results.length;c++){
						// send(channel, results[c]);
						// }
					} else {
						send(channel, "No giveaway results.");
					}

				}
			}
			return;
		}

		// !raffle - Ops
		if (msg[0].equalsIgnoreCase(prefix + "raffle") && isOp) {
			log("RB: Matched command !raffle");
			if (msg.length >= 2) {
				if (msg[1].equalsIgnoreCase("enable")) {
					if (channelInfo.raffle == null) {
						channelInfo.raffle = new Raffle();
					}
					channelInfo.raffle.setEnabled(true);

					send(channel, "Raffle enabled. Use \"" + prefix
							+ "raffle\" to enter!");
				} else if (msg[1].equalsIgnoreCase("disable")) {
					if (channelInfo.raffle != null) {
						channelInfo.raffle.setEnabled(false);
					}

					send(channel, "Raffle disabled.");
				} else if (msg[1].equalsIgnoreCase("reset")) {
					if (channelInfo.raffle != null) {
						channelInfo.raffle.reset();
					}

					send(channel, "Raffle entries cleared.");
				} else if (msg[1].equalsIgnoreCase("count")) {
					if (channelInfo.raffle != null) {
						send(channel,
								"Raffle has " + channelInfo.raffle.count()
										+ " entries.");
					} else {
						send(channel, "Raffle has 0 entries.");
					}
				} else if (msg[1].equalsIgnoreCase("winner")) {
					if (channelInfo.raffle != null) {
						send(channel,
								"Winner is " + channelInfo.raffle.pickWinner()
										+ "!");
					} else {
						send(channel, "No raffle history found.");
					}
				}
			} else {
				if (channelInfo.raffle != null) {
					channelInfo.raffle.enter(sender);
				}
			}
			return;
		}

		// !random - Ops
		if (msg[0].equalsIgnoreCase(prefix + "random") && isSub) {
			log("RB: Matched command !random");

			if (msg.length > 1) {
				if (msg[1].equalsIgnoreCase("regular") && isOp) {
					logMain("Matched command random regular");
					ArrayList<String> onlineRegs = new ArrayList<String>();
					ArrayList<String> chatters = JSONUtil
							.tmiChatters(twitchName);
					Set<String> regs = channelInfo.getRegulars();
					for (String s : chatters) {
						if (regs.contains(s.toLowerCase())) {
							onlineRegs.add(s);
						}
					}
					if (onlineRegs.size() > 0) {
						String selected = onlineRegs
								.get((int) (Math.random() * onlineRegs.size()));

						send(channel, selected
								+ " is the lucky random regular!");
					} else
						send(channel,
								"No regulars are connected to chat right now.");
				}
				if (msg[1].equalsIgnoreCase("coin")) {
					Random rand = new Random();
					boolean coin = rand.nextBoolean();
					if (coin == true)
						send(channel, "Heads!");
					else
						send(channel, "Tails!");
				} else if (isInteger(msg[1])) {
					int randMax = Integer.parseInt(msg[1]);
					int randReturn = (int) (Math.random() * (randMax - 1)) + 1;
					send(channel, "You rolled: " + randReturn);
				}
			}
			return;
		}

		if (msg[0].equalsIgnoreCase(prefix + "highlights")) {
			send(channel,
					"You can find all marked highlights at: coebot.tv/highlights/"
							+ channel.substring(1));
		}

		if (msg[0].equalsIgnoreCase(prefix + "highlightthat")
				|| msg[0].equalsIgnoreCase(prefix + "ht")) {
			String result = JSONUtil
					.highlightThat("http://coebot.tv/highlights/add/coebot/"
							+ channel.substring(1) + "/" + sender);
			// send(channel, result.replaceAll("\"", ""));
		}
		// ##########################QUOTES##############################
		if (msg[0].equalsIgnoreCase(prefix + "quote") && isSub) {
			if (msg.length > 1) {

				long newQuoted = System.currentTimeMillis();

				if ((newQuoted >= (lastQuoted + 30 * 1000L)) || isOp) {

					// getQuote
					if (msg[1].equalsIgnoreCase("get")) {
						log("RB: Matched command !getQuote");
						if (isSub && msg.length > 2) {
							int index = Integer.parseInt(msg[2]);
							send(channel, "Quote #" + index + ": "
									+ channelInfo.getQuote(index - 1));
							lastQuoted = System.currentTimeMillis();
						} else {
							send(channel, "Syntax is " + prefix
									+ "quote get <index>");
						}
					}

				}
				// randomquote
				long newQuoted1 = System.currentTimeMillis();
				if ((newQuoted1 >= (lastQuoted1 + 30 * 1000L)) || isOp) {
					// randomquote
					if (msg[1].equalsIgnoreCase("random")) {
						log("RB: Matched command !randomquote");
						if (msg.length > 1) {
							int randQuotes = (int) (Math.random() * channelInfo
									.getQuoteSize());
							if (randQuotes > -1) {
								int tempNum = randQuotes + 1;
								send(channel, "Quote #" + tempNum + ": "
										+ channelInfo.getQuote(randQuotes));
								lastQuoted1 = System.currentTimeMillis();
							} else
								send(channel, "Error, whoops");
						} else {
							send(channel, "Syntax is " + prefix
									+ "quote random");
						}
					}
				}

				// !addQuote
				if (msg[1].equalsIgnoreCase("add")) {
					log("RB: Matched command !addQuote");
					if (isOp && msg.length > 2) {

						String quoteReceived = this.fuseArray(msg, 2);
						if (quoteReceived.contains("&&&")) {
							send(channel, "Quotes cannot contain &&&");
						} else {
							quoteReceived.trim();
							int numQuote = channelInfo.addQuote(quoteReceived);
							if (numQuote > -1) {
								numQuote++;
								send(channel, quoteReceived
										+ " added, this is quote #" + numQuote);
							} else
								send(channel, "Quote already exists.");
						}
					}
				}

				// !getQuoteIndex
				if (msg[1].equalsIgnoreCase("getindex")) {
					log("RB: Matched command !getQuoteIndex");
					if (isSub && msg.length > 2
							&& BotManager.getInstance().twitchChannels) {
						String quoteReceived = this.fuseArray(msg, 2);
						quoteReceived.trim();
						int index = channelInfo.getQuoteIndex(quoteReceived);
						if (index > -1) {
							index++;
							send(channel, "This quote's index is " + index);
						} else {
							send(channel,
									"Quote not found, make sure you have the EXACT quote");
						}
					}
				}
				// search
				if (msg[1].equalsIgnoreCase("search") && isOp) {
					log("RB: Matched command !search");
					String containers = "Quotes containing \""
							+ fuseArray(msg, 2) + "\": ";

					int num = 0;
					for (int i = 0; i < channelInfo.getQuoteSize(); i++) {
						if (channelInfo
								.getQuote(i)
								.toLowerCase()
								.contains(
										fuseArray(msg, 2).toLowerCase().trim())) {

							num++;
							int tempNum = i + 1;
							containers += tempNum + ",";
						}
					}
					if (num > 0) {
						send(channel,
								containers.substring(0, containers.length() - 1)
										+ ".");
					} else
						send(channel, "No quotes contained that phrase, sorry.");

				}
				// delQuote
				if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equals("remove")) {
					log("RB: Matched command !delQuote");
					if (isOp && msg.length > 2) {
						int index = Integer.parseInt(msg[2]);
						if (channelInfo.deleteQuote(index - 1)) {
							send(channel, "Quote #" + index
									+ " deleted successfully.");
						} else {
							send(channel, "Error deleting quote");
						}
					}
				}
			} else {
				send(channel,
						"Your syntax is incorrect, please check the documentation.");
			}
		}

		// ********************************************************************************
		// ***************************** Moderation Commands
		// ******************************
		// ********************************************************************************

		// Moderation commands - Ops
		if (isOp) {
			if (msg[0].equalsIgnoreCase("+m") && msg.length > 1) {
				int time = Integer.parseInt(msg[1]);
				sendCommand(channel, ".slow " + time);
			} else if (msg[0].equalsIgnoreCase("+m")) {
				sendCommand(channel, ".slow ");
			}
			if (msg[0].equalsIgnoreCase("-m")) {
				sendCommand(channel, ".slowoff");
			}
			if (msg[0].equalsIgnoreCase("+s")) {
				sendCommand(channel, ".subscribers");
			}
			if (msg[0].equalsIgnoreCase("-s")) {
				sendCommand(channel, ".subscribersoff");
			}
			if (msg.length > 0) {
				if (msg[0].equalsIgnoreCase("+b")
						|| msg[0].equalsIgnoreCase(prefix + "ban")) {
					sendCommand(channel, ".ban " + msg[1].toLowerCase());
					send(channel, msg[1].toLowerCase() + " was banned.");
					channelInfo.increasePunCount();
				}
				if (msg[0].equalsIgnoreCase("-b")) {
					sendCommand(channel, ".unban " + msg[1].toLowerCase());
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
					send(channel, msg[1].toLowerCase() + " was unbanned.");
				}
				if (msg[0].equalsIgnoreCase("+t")) {
					if (msg.length > 2) {
						sendCommand(channel, ".timeout " + msg[1].toLowerCase()
								+ " " + msg[2]);
						send(channel, msg[1].toLowerCase()
								+ " was timed out for " + msg[2] + " seconds.");
						channelInfo.increasePunCount();
					} else {
						sendCommand(channel, ".timeout " + msg[1].toLowerCase());
						send(channel, msg[1].toLowerCase() + " was timed out.");
						channelInfo.increasePunCount();
					}
				}
				if (msg[0].equalsIgnoreCase("-t")) {
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
					send(channel, msg[1].toLowerCase()
							+ " is no longer timed out.");
				}
				if (msg[0].equalsIgnoreCase("+p")) {
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
					send(channel, msg[1].toLowerCase()
							+ "'s chat history was purged.");
					channelInfo.increasePunCount();
				}
			}

		}
		int tries = 0;
		if (msg[0].equalsIgnoreCase(prefix + "winner") && isOp) {
			log("RB: Matched command !winner");

			ArrayList<String> chatters = JSONUtil.tmiChatters(twitchName);
			if (chatters != null) {
				int randomNum = (int) (Math.random() * chatters.size());

				if (randomNum > -1) {
					send(channel,
							"And the winner is... " + chatters.get(randomNum)
									+ "!");
				}
			} else {
				ArrayList<String> chatters1 = JSONUtil.tmiChatters(twitchName);
				if (chatters1 != null) {
					int randomNum = (int) (Math.random() * chatters1.size());

					if (randomNum > -1) {
						send(channel,
								"And the winner is... "
										+ chatters1.get(randomNum) + "!");
					}
				} else {
					ArrayList<String> chatters2 = JSONUtil
							.tmiChatters(twitchName);
					if (chatters2 != null) {
						int randomNum = (int) (Math.random() * chatters2.size());

						if (randomNum > -1) {
							send(channel,
									"And the winner is... "
											+ chatters2.get(randomNum) + "!");
						}
					} else
						send(channel, "Error accessing API after 3 tries.");
				}
			}

		}
		// if(msg[0].equalsIgnoreCase(prefix+"fixquotes")&&isAdmin){
		// try{
		// read("quotesList"+channel+".txt");
		// for (int i = 0; i<quotesList.size();i++){
		// String quoteReceived = quotesList.get(i);
		// int numQuote = channelInfo.addQuote(quoteReceived);
		// if(numQuote > -1)
		// send(channel, quoteReceived + " added, this is quote #" + numQuote);
		// else
		// send(channel, "Quote already exists.");
		//
		// }
		// }catch(Exception e){
		// send(channel, "failed");
		// }
		//
		// }

		if (msg[0].equalsIgnoreCase(prefix + "whatshouldiplay") && isOwner) {
			String result = JSONUtil.whatShouldIPlay(channelInfo.getSteam());
			send(channel, "You could always play: " + result);
		}
		if (msg[0].equalsIgnoreCase(prefix + "race")) {
			String result = JSONUtil.getRace(channel.substring(1));
			if (result != null)
				send(channel, "You can find the race " + channel.substring(1)
						+ " is currently in at " + result);
			else
				send(channel, channel.substring(1)
						+ " is not currently in a race.");
		}
		// if (msg[0].equalsIgnoreCase(prefix + "define") && isSub) {
		// if (msg.length > 1) {
		// log("RB: Matched command !define");
		// String fused = fuseArray(msg, 1);
		// fused = fused.replaceAll(" ", "+");
		// String result = JSONUtil.defineWord(fused);
		// send(channel, result);
		// if (result.equals("Couldn't find any results, sorry")) {
		// } else
		// send(channel,
		// "Definitions may or may not be accurate or as intended");
		// }
		//
		// }
		if (msg[0].equalsIgnoreCase(prefix + "urban") && isRegular) {
			if (msg.length > 1) {
				log("RB: Matched command !urban");
				String fused = fuseArray(msg, 1);
				fused = fused.replaceAll(" ", "+");
				if (channelInfo.getUrban()) {
					String result = JSONUtil.defineUrban(fused);
					if (result.length() > 140)
						result = result.substring(0, 140);
					send(channel, "\"" + result + "\"");
				} else {
					send(channel, prefix
							+ "urban is currently not enabled on this channel.");
				}

			}
		}

		// !clear - Ops
		if (msg[0].equalsIgnoreCase(prefix + "clear") && isOp) {
			log("RB: Matched command !clear");
			sendCommand(channel, ".clear");
			return;
		}

		// Filters
		if (msg[0].equalsIgnoreCase(prefix + "filter") && isOp) {
			if (msg.length < 2) {
				send(channel,
						"Syntax: !filter <option> [sub options]. Options: on/off, status, me, enablewarnings, timeoutduration, displaywarnings, messagelength, links, pd, banphrase, caps, emotes, and symbols.");
				return;
			}

			// Shift down a notch
			String[] newMsg = new String[msg.length - 1];
			for (int i = 1; i < msg.length; i++) {
				newMsg[i - 1] = msg[i];
			}
			msg = newMsg;

			// Global disable
			if (msg[0].equalsIgnoreCase("on")) {
				channelInfo.setFiltersFeature(true);
				send(channel, "Feature: Filters is on");
				return;
			} else if (msg[0].equalsIgnoreCase("off")) {
				channelInfo.setFiltersFeature(false);
				send(channel, "Feature: Filters is off");
				return;
			}

			if (msg[0].equalsIgnoreCase("status")) {
				send(channel, "Global: " + channelInfo.useFilters);
				send(channel,
						"Enable warnings: " + channelInfo.getEnableWarnings());
				send(channel,
						"Timeout duration: " + channelInfo.getTimeoutDuration());
				send(channel,
						"Display warnings: " + channelInfo.checkSignKicks());
				send(channel,
						"Max message length: " + channelInfo.getFilterMax());
				send(channel, "Me: " + channelInfo.getFilterMe());
				send(channel, "Links: " + channelInfo.getFilterLinks());
				send(channel,
						"Banned phrases: " + channelInfo.getFilterOffensive()
								+ " ~ severity="
								+ channelInfo.config.get("banPhraseSeverity"));
				send(channel,
						"Caps: " + channelInfo.getFilterCaps() + " ~ percent="
								+ channelInfo.getfilterCapsPercent()
								+ ", minchars="
								+ channelInfo.getfilterCapsMinCharacters()
								+ ", mincaps="
								+ channelInfo.getfilterCapsMinCapitals());
				send(channel, "Emotes: " + channelInfo.getFilterEmotes()
						+ " ~ max=" + channelInfo.getFilterEmotesMax()
						+ ", single=" + channelInfo.getFilterEmotesSingle());
				send(channel, "Symbols: " + channelInfo.getFilterSymbols()
						+ " ~ percent=" + channelInfo.getFilterSymbolsPercent()
						+ ", min=" + channelInfo.getFilterSymbolsMin());
			}

			if (msg[0].equalsIgnoreCase("me") && msg.length == 2) {
				if (msg[1].equalsIgnoreCase("on")) {
					channelInfo.setFilterMe(true);
					send(channel, "Feature: /me filter is on");
				} else if (msg[1].equalsIgnoreCase("off")) {
					channelInfo.setFilterMe(false);
					send(channel, "Feature: /me filter is off");
				}
				return;
			}

			if (msg[0].equalsIgnoreCase("enablewarnings") && msg.length == 2) {
				if (msg[1].equalsIgnoreCase("on")) {
					channelInfo.setEnableWarnings(true);
					send(channel, "Feature: Timeout warnings are on");
				} else if (msg[1].equalsIgnoreCase("off")) {
					channelInfo.setEnableWarnings(false);
					send(channel, "Feature: Timeout warnings are off");
				}
			}

			if (msg[0].equalsIgnoreCase("timeoutduration") && msg.length == 2) {
				if (Main.isInteger(msg[1])) {
					int duration = Integer.parseInt(msg[1]);
					channelInfo.setTimeoutDuration(duration);
					send(channel,
							"Timeout duration is "
									+ channelInfo.getTimeoutDuration());
				} else {
					send(channel,
							"You must specify an integer for the duration");
				}
			}

			if (msg[0].equalsIgnoreCase("displaywarnings") && msg.length == 2) {
				if (msg[1].equalsIgnoreCase("on")) {
					channelInfo.setSignKicks(true);
					send(channel, "Feature: Display warnings is on");
				} else if (msg[1].equalsIgnoreCase("off")) {
					channelInfo.setSignKicks(false);
					send(channel, "Feature: Display warnings is off");
				}
			}

			if (msg[0].equalsIgnoreCase("messagelength") && msg.length == 2) {
				if (Main.isInteger(msg[1])) {
					channelInfo.setFilterMax(Integer.parseInt(msg[1]));
					send(channel,
							"Max message length set to "
									+ channelInfo.getFilterMax());
				} else {
					send(channel, "Must be an integer.");
				}
			}

			// !links - Owner
			if (msg[0].equalsIgnoreCase("links")) {
				log("RB: Matched command !links");
				if (msg.length == 1) {
					send(channel, "Syntax: \"!links on/off\"");
				} else if (msg.length == 2) {
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setFilterLinks(true);
						send(channel,
								"Link filter: " + channelInfo.getFilterLinks());
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setFilterLinks(false);
						send(channel,
								"Link filter: " + channelInfo.getFilterLinks());
					}
				}
				return;
			}

			// !pd - Owner
			if (msg[0].equalsIgnoreCase("pd")) {
				log("RB: Matched command !pd");
				if (msg.length == 1) {
					send(channel,
							"Syntax: \"!pd add/delete [domain]\" and \"!pd list\"");
				} else if (msg.length > 2) {
					if (msg[1].equalsIgnoreCase("add")) {
						if (channelInfo.isDomainPermitted(msg[2])) {
							send(channel, "Domain already exists. " + "("
									+ msg[2] + ")");
						} else {
							channelInfo.addPermittedDomain(msg[2]);
							send(channel, "Domain added. " + "(" + msg[2] + ")");
						}
					} else if (msg[1].equalsIgnoreCase("delete")
							|| msg[1].equalsIgnoreCase("remove")) {
						if (channelInfo.isDomainPermitted(msg[2])) {
							channelInfo.removePermittedDomain(msg[2]);
							send(channel, "Domain removed. " + "(" + msg[2]
									+ ")");
						} else {
							send(channel, "Domain does not exist. " + "("
									+ msg[2] + ")");
						}
					}
				} else if (msg.length > 1 && msg[1].equalsIgnoreCase("list")
						&& isOp) {
					String tempList = "Permitted domains: ";
					for (String s : channelInfo.getpermittedDomains()) {
						tempList += s + ", ";
					}
					send(channel, tempList);
				}
				return;
			}

			// !banphrase - Owner
			if (msg[0].equalsIgnoreCase("banphrase")) {
				log("RB: Matched command !banphrase");
				if (isOwner)
					log("RB: Is owner");
				if (msg.length == 1) {
					send(channel,
							"Syntax: \"!banphrase on/off\", \"!banphrase add/delete [string to purge]\", \"!banphrase list\"");
				} else if (msg.length > 1) {
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setFilterOffensive(true);
						send(channel, "Ban phrase filter is on");
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setFilterOffensive(false);
						send(channel, "Ban phrase filter is off");
					} else if (msg[1].equalsIgnoreCase("clear")) {
						channelInfo.clearBannedPhrases();
						send(channel, "Banned phrases cleared.");
					} else if (msg[1].equalsIgnoreCase("list")) {
						String tempList = "Banned phrases words: ";
						for (String s : channelInfo.getOffensive()) {
							tempList += s + ", ";
						}
						send(channel, tempList);
					} else if (msg[1].equalsIgnoreCase("add") && msg.length > 2) {
						String phrase = fuseArray(msg, 2);
						if (phrase.contains(",,")) {
							send(channel, "Cannot contain double commas (,,)");
						} else if (channelInfo
								.isBannedPhrase(fuseArray(msg, 2))) {
							send(channel, "Word already exists. " + "("
									+ phrase + ")");
						} else {
							if (phrase.startsWith("REGEX:") && !isAdmin) {
								send(channel,
										"You must have Admin status to add regex phrases.");
								return;
							}
							channelInfo.addOffensive(phrase);
							send(channel, "Word added. " + "(" + phrase + ")");
						}
					} else if (msg[1].equalsIgnoreCase("severity")) {
						if (msg.length > 2 && Main.isInteger(msg[2])) {
							int severity = Integer.parseInt(msg[2]);
							channelInfo.config.put("banPhraseSeverity",
									severity);

							send(channel,
									"Severity set to "
											+ channelInfo.config
													.get("banPhraseSeverity"));
						} else {
							send(channel,
									"Severity is "
											+ channelInfo.config
													.get("banPhraseSeverity"));
						}
					} else if (msg[1].equalsIgnoreCase("delete")
							|| msg[1].equalsIgnoreCase("remove")
							&& msg.length > 2) {
						String phrase = fuseArray(msg, 2);
						channelInfo.removeOffensive(phrase);
						send(channel, "Word removed. " + "(" + phrase + ")");
					}
				}
				return;
			}

			// !caps - Owner
			if (msg[0].equalsIgnoreCase("caps")) {
				log("RB: Matched command !caps");
				if (msg.length == 1) {
					send(channel,
							"Syntax: \"!caps on/off\", \"!caps percent/minchars/mincaps [value]\", \"!caps status\"");
				} else if (msg.length > 1) {
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setFilterCaps(true);
						send(channel,
								"Caps filter: " + channelInfo.getFilterCaps());
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setFilterCaps(false);
						send(channel,
								"Caps filter: " + channelInfo.getFilterCaps());
					} else if (msg[1].equalsIgnoreCase("percent")) {
						if (msg.length > 2) {
							channelInfo.setfilterCapsPercent(Integer
									.parseInt(msg[2]));
							send(channel,
									"Caps filter percent: "
											+ channelInfo
													.getfilterCapsPercent());
						}
					} else if (msg[1].equalsIgnoreCase("minchars")) {
						if (msg.length > 2 && Main.isInteger(msg[2])) {
							channelInfo.setfilterCapsMinCharacters(Integer
									.parseInt(msg[2]));
							send(channel, "Caps filter min characters: "
									+ channelInfo.getfilterCapsMinCharacters());
						}
					} else if (msg[1].equalsIgnoreCase("mincaps")) {
						if (msg.length > 2 && Main.isInteger(msg[2])) {
							channelInfo.setfilterCapsMinCapitals(Integer
									.parseInt(msg[2]));
							send(channel, "Caps filter min caps: "
									+ channelInfo.getfilterCapsMinCapitals());
						}
					} else if (msg[1].equalsIgnoreCase("status")) {
						send(channel,
								"Caps filter="
										+ channelInfo.getFilterCaps()
										+ ", percent="
										+ channelInfo.getfilterCapsPercent()
										+ ", minchars="
										+ channelInfo
												.getfilterCapsMinCharacters()
										+ ", mincaps="
										+ channelInfo
												.getfilterCapsMinCapitals());
					}
				}
				return;
			}

			// !emotes - Owner
			if (msg[0].equalsIgnoreCase("emotes")) {
				log("RB: Matched command !emotes");
				if (msg.length == 1) {
					send(channel,
							"Syntax: \"!emotes on/off\", \"!emotes max [value]\", \"!emotes single on/off\"");
				} else if (msg.length > 1) {
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setFilterEmotes(true);
						send(channel,
								"Emotes filter: "
										+ channelInfo.getFilterEmotes());
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setFilterEmotes(false);
						send(channel,
								"Emotes filter: "
										+ channelInfo.getFilterEmotes());
					} else if (msg[1].equalsIgnoreCase("max")) {
						if (msg.length > 2 && Main.isInteger(msg[2])) {
							channelInfo.setFilterEmotesMax(Integer
									.parseInt(msg[2]));
							send(channel,
									"Emotes filter max: "
											+ channelInfo.getFilterEmotesMax());
						}
					} else if (msg[1].equalsIgnoreCase("status")) {
						send(channel,
								"Emotes filter="
										+ channelInfo.getFilterEmotes()
										+ ", max="
										+ channelInfo.getFilterEmotesMax()
										+ ", single="
										+ channelInfo.getFilterEmotesSingle());
					} else if (msg[1].equalsIgnoreCase("single")
							&& msg.length > 2) {
						if (msg[2].equalsIgnoreCase("on")) {
							channelInfo.setFilterEmotesSingle(true);
							send(channel,
									"Single Emote filter: "
											+ channelInfo
													.getFilterEmotesSingle());
						} else if (msg[2].equalsIgnoreCase("off")) {
							channelInfo.setFilterEmotesSingle(false);
							send(channel,
									"Single Emote filter: "
											+ channelInfo
													.getFilterEmotesSingle());
						}
					}
				}
				return;
			}

			// !symbols - Owner
			if (msg[0].equalsIgnoreCase("symbols")) {
				log("RB: Matched command !symbols");
				if (msg.length == 1) {
					send(channel,
							"Syntax: \"!symbols on/off\", \"!symbols percent/min [value]\", \"!symbols status\"");
				} else if (msg.length > 1) {
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setFilterSymbols(true);
						send(channel,
								"Symbols filter: "
										+ channelInfo.getFilterSymbols());
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setFilterSymbols(false);
						send(channel,
								"Symbols filter: "
										+ channelInfo.getFilterSymbols());
					} else if (msg[1].equalsIgnoreCase("percent")) {
						if (msg.length > 2 && Main.isInteger(msg[2])) {
							channelInfo.setFilterSymbolsPercent(Integer
									.parseInt(msg[2]));
							send(channel, "Symbols filter percent: "
									+ channelInfo.getFilterSymbolsPercent());
						}
					} else if (msg[1].equalsIgnoreCase("min")) {
						if (msg.length > 2 && Main.isInteger(msg[2])) {
							channelInfo.setFilterSymbolsMin(Integer
									.parseInt(msg[2]));
							send(channel, "Symbols filter min symbols: "
									+ channelInfo.getFilterSymbolsMin());
						}
					} else if (msg[1].equalsIgnoreCase("status")) {
						send(channel,
								"Symbols filter="
										+ channelInfo.getFilterSymbols()
										+ ", percent="
										+ channelInfo.getFilterSymbolsPercent()
										+ ", min="
										+ channelInfo.getFilterSymbolsMin());
					}
				}
				return;
			}

			return;
		}
		
		if(msg[0].equalsIgnoreCase(prefix+"boi")&&isSub){
			if(msg.length > 1){
				String searchTerms = fuseArray(msg,1);
				String itemName = JSONUtil.BOIItemName(searchTerms);
				
				if(!itemName.equals("")){
					String itemDesc = JSONUtil.BOIItem(searchTerms);
					send(channel, "Item info for "+itemName+": "+itemDesc);
				}else
					send(channel, "Couldn't find anything for those search terms, sorry.");
			}else
				send(channel, "Usage is "+prefix+"boi <item name>");
		}
		
		
		
		// coebot ignores

		if (msg[0].equalsIgnoreCase(prefix + "ignore") && isOp) {
			if (msg.length == 2) {
				if (msg[1].equalsIgnoreCase("list")) {
					String tempList = "Ignored users: ";
					ArrayList<String> ignored = channelInfo.getIgnoredUsers();
					java.util.Collections.sort(ignored);
					for (int i = 0; i < ignored.size(); i++) {
						if (i == ignored.size() - 1) {
							tempList += (ignored.get(i));
						} else
							tempList += (ignored.get(i) + ", ");
					}
					send(channel, tempList);
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (channelInfo.addIgnoredUser(msg[2].toLowerCase())) {
						send(channel,
								msg[2].toLowerCase()
										+ " has been added to the ignore list for this channel.");

					} else
						send(channel,
								msg[2].toLowerCase()
										+ " is already on the ignore list for this channel.");

				} else if (msg[1].equalsIgnoreCase("remove")
						|| msg[1].equalsIgnoreCase("delete")) {
					if (channelInfo.removeIgnoredUser(msg[2].toLowerCase())) {
						send(channel,
								msg[2].toLowerCase()
										+ " was successfully removed from the ignore list for this channel.");
					} else
						send(channel, msg[2].toLowerCase()
								+ " was not on this channel's ignore list.");
				}
			}
		}

		// !permit - Allows users to post 1 link
		if ((msg[0].equalsIgnoreCase(prefix + "permit") || msg[0]
				.equalsIgnoreCase(prefix + "allow"))
				&& channelInfo.getFilterLinks()
				&& channelInfo.useFilters
				&& isOp) {
			log("RB: Matched command !permit");
			if (msg.length == 1) {
				send(channel, "Syntax: \"!permit [username]\"");
			} else if (msg.length > 1) {
				if (!channelInfo.isRegular(msg[1])) {
					channelInfo.permitUser(msg[1]);
					send(channel, msg[1] + " may now post 1 link.");
				} else {
					send(channel,
							msg[1]
									+ " is a regular and does not need to be permitted.");
				}
			}
			return;
		}
		// if (msg[0].equalsIgnoreCase(prefix + "amIReg")) {
		// if (isRegular)
		// send(channel, sender + " is a regular.");
		// else
		// send(channel, sender + " is not a regular.");
		// }
		// !regular - Owner
		if ((msg[0].equalsIgnoreCase(prefix + "regular") || msg[0]
				.equalsIgnoreCase(prefix + "regulars")) && isOp) {
			log("RB: Matched command !regular");
			if (msg.length < 2) {
				send(channel,
						"Syntax: \"!regular add/delete [name]\", \"!regular list\"");
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (channelInfo.isRegular(msg[2])) {
						send(channel, "User already exists." + "(" + msg[2]
								+ ")");
					} else {
						channelInfo.addRegular(msg[2]);
						send(channel, "User added. " + "(" + msg[2] + ")");
					}
				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					if (channelInfo.isRegular(msg[2])) {
						channelInfo.removeRegular(msg[2]);
						send(channel, "User removed." + "(" + msg[2] + ")");
					} else {
						send(channel, "User does not exist. " + "(" + msg[2]
								+ ")");
					}
				}
			} else if (msg.length > 1 && msg[1].equalsIgnoreCase("list")
					&& isOp) {
				String tempList = "Regulars: ";
				ArrayList<String> arrRegs = new ArrayList<String>();
				for (String s : channelInfo.getRegulars()) {
					arrRegs.add(s);
				}
				java.util.Collections.sort(arrRegs);
				for (int i = 0; i < arrRegs.size(); i++) {
					if (i == arrRegs.size() - 1) {
						tempList += (arrRegs.get(i));
					} else
						tempList += (arrRegs.get(i) + ", ");
				}
				send(channel, tempList);
			}
			return;
		}

		// !mod - Owner
		if (msg[0].equalsIgnoreCase(prefix + "mod") && isOwner) {
			log("RB: Matched command !mod");
			if (msg.length < 2) {
				send(channel,
						"Syntax: \"!mod add/delete [name]\", \"!mod list\"");
			}
			if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (channelInfo.isModerator(msg[2])) {
						send(channel, "User already exists. " + "(" + msg[2]
								+ ")");
					} else {
						channelInfo.addModerator(msg[2]);
						send(channel, "User added. " + "(" + msg[2] + ")");
					}
				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					if (channelInfo.isModerator(msg[2])) {
						channelInfo.removeModerator(msg[2]);
						send(channel, "User removed. " + "(" + msg[2] + ")");
					} else {
						send(channel, "User does not exist. " + "(" + msg[2]
								+ ")");
					}
				}
			} else if (msg.length > 1 && msg[1].equalsIgnoreCase("list")
					&& isOwner) {
				String tempList = "Moderators: ";
				ArrayList<String> arrRegs = new ArrayList<String>();
				for (String s : channelInfo.getModerators()) {
					arrRegs.add(s);
				}
				java.util.Collections.sort(arrRegs);
				for (int i = 0; i < arrRegs.size(); i++) {
					if (i == arrRegs.size() - 1) {
						tempList += (arrRegs.get(i));
					} else
						tempList += (arrRegs.get(i) + ", ");
				}
				send(channel, tempList);
			}
			return;
		}

		// !owner - Owner
		if (msg[0].equalsIgnoreCase(prefix + "owner") && isOwner) {
			log("RB: Matched command !owner");
			if (msg.length < 2) {
				send(channel,
						"Syntax: \"!owner add/delete [name]\", \"!owner list\"");
			}
			if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (channelInfo.isOwner(msg[2])) {
						send(channel, "User already exists. " + "(" + msg[2]
								+ ")");
					} else {
						channelInfo.addOwner(msg[2]);
						send(channel, "User added. " + "(" + msg[2] + ")");
					}
				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					if (channelInfo.isOwner(msg[2])) {
						channelInfo.removeOwner(msg[2]);
						send(channel, "User removed. " + "(" + msg[2] + ")");
					} else {
						send(channel, "User does not exist. " + "(" + msg[2]
								+ ")");
					}
				}
			} else if (msg.length > 1 && msg[1].equalsIgnoreCase("list")
					&& isOwner) {
				String tempList = "Owners: ";
				ArrayList<String> arrRegs = new ArrayList<String>();
				for (String s : channelInfo.getOwners()) {
					arrRegs.add(s);
				}
				java.util.Collections.sort(arrRegs);
				for (int i = 0; i < arrRegs.size(); i++) {
					if (i == arrRegs.size() - 1) {
						tempList += (arrRegs.get(i));
					} else
						tempList += (arrRegs.get(i) + ", ");
				}
				send(channel, tempList);
			}
			return;
		}

		// xboxGame
		if (msg[0].equalsIgnoreCase(prefix + "xboxgame") && isOwner) {
			String gamerTag = channelInfo.getGamerTag();
			String lastGame = JSONUtil.xboxLastGame(gamerTag);
			try {
				if (lastGame.equals("(unavailable)")) {
					channelInfo.updateGame("");
					send(channel,
							"Xbox Live game unavailable, changed game to \"Not Playing\" status.");
				} else {
					channelInfo.updateGame(lastGame);
					send(channel,
							"Xbox Live game updated to the game you last played on Xbox Live, "
									+ lastGame);
				}
			} catch (Exception ex) {
				send(channel,
						"Error updating game. Did you add me as an editor?");
			}

		}

		if (msg[0].startsWith(prefix + "#") && isOwner && msg[0].contains("/")) {

			String otherChannel = msg[0].substring(1, msg[0].indexOf("/"));
			Channel newChannelInfo = getChannelObject(otherChannel);

			logMain(otherChannel);
			String command = msg[0].substring(msg[0].indexOf("/") + 1);
			logMain(command);
			String value = newChannelInfo.getCommand(command);
			logMain(value);
			if (value != null) {
				log("RB: Matched command " + command);

				if (value.contains("(_PURGE_)")) {
					value = value.replace("(_PURGE_)", msg[1].toLowerCase());
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
				} else if (value.contains("(_TIMEOUT_)")) {
					value = value.replace("(_TIMEOUT_)", msg[1].toLowerCase());
					sendCommand(channel, ".timeout " + msg[1].toLowerCase());

				} else if (value.contains("(_BAN_)")) {
					value = value.replace("(_BAN_)", msg[1].toLowerCase());
					sendCommand(channel, ".ban " + msg[1].toLowerCase());
				}
				if (value.contains("(_PARAMETER_)")) {

					String[] parts = fuseArray(msg, 1).split(";");
					if (parts.length > 1) {
						for (String s : parts) {
							value = value.replaceFirst("\\(_PARAMETER_\\)",
									s.trim());
						}
					} else
						value = value.replace("(_PARAMETER_)", parts[0]);
					// value = value.replace("(_PARAMETER_)",
					// fuseArray(msg, 1));

				}

				send(channel, sender, value);

			}
		}

		// !set - Owner
		if (msg[0].equalsIgnoreCase(prefix + "set") && isOp) {
			log("RB: Matched command !set");
			if (msg.length == 1) {
				send(channel,
						"Syntax: \"!set [option] [value]\". Options: topic, filters, throw, signedkicks, joinsparts, lastfm, steam, mode, chatlogging, maxlength");
			} else if (msg[1].equalsIgnoreCase("topic")) {
				if (msg[2].equalsIgnoreCase("on")) {
					channelInfo.setTopicFeature(true);
					send(channel, "Feature: Topic is on");
				} else if (msg[2].equalsIgnoreCase("off")) {
					channelInfo.setTopicFeature(false);
					send(channel, "Feature: Topic is off");
				}

			} else if (msg[1].equalsIgnoreCase("extralifeid") && isOwner) {
				if (msg.length > 2) {
					if (isInteger(msg[2])) {
						channelInfo.setExtraLifeID(msg[2]);
						send(channel, "Your Extra Life ID has been set to: "
								+ msg[2]);
					} else
						send(channel, "Your Extra Life ID must be a number.");
				}
			}
			// enable/disable urban
			else if (msg[1].equalsIgnoreCase("urban") && isOwner) {
				if (msg.length > 2) {
					boolean enabled = false;
					if (msg[2].equalsIgnoreCase("on")
							|| msg[2].equalsIgnoreCase("enabled")) {
						enabled = true;
						send(channel, "The use of " + prefix
								+ "urban is now enabled.");
					} else if (msg[2].equalsIgnoreCase("off")
							|| msg[2].equalsIgnoreCase("disabled")) {
						send(channel, "The use of " + prefix
								+ "urban is now disabled.");
					}
					channelInfo.setUrban(enabled);

				}
			}
			// setgamertag
			else if (msg[1].equalsIgnoreCase("gamertag")) {
				if (msg.length > 2) {
					String gamerTag = this.fuseArray(msg, 2).trim();
					channelInfo.setGamertag(gamerTag);
					send(channel, "Xbox Live Gamertag has been set to "
							+ gamerTag);
				}
			}// setbullet
			else if (msg[1].equalsIgnoreCase("bullet")) {
				if (msg.length > 2) {
					if (!msg[2].startsWith("/") && !msg[2].startsWith(".")) {
						bullet[0] = msg[2];
						channelInfo.setBullet(msg[2]);
						send(channel, "Bullet is now set to \"" + bullet[0]
								+ "\"");
					} else
						send(channel, "Bullet cannot start with \"/\" or \".\"");

				} else
					send(channel, "Usage is " + prefix
							+ "set bullet <new bullet>");
			}// setSubsRegsMinusLinks
			else if (msg[1].equalsIgnoreCase("subsRegsMinusLinks")) {
				if (msg.length > 2) {
					if (msg[2].equalsIgnoreCase("on")) {
						channelInfo.setSubsRegsMinusLinks(true);
						send(channel,
								"Subscribers are now considered regulars except for the ability to post links.");
					} else if (msg[2].equalsIgnoreCase("off")) {
						channelInfo.setSubsRegsMinusLinks(false);
						send(channel,
								"Subscribers are now considered non-regulars.");
					} else
						send(channel, "Syntax is " + prefix
								+ "set subsRegsMinusLinks <on/off>");
				} else
					send(channel, "Syntax is " + prefix
							+ "set subsRegsMinusLinks <on/off>");
			}
			// setupdateDelay
			else if (msg[1].equalsIgnoreCase("updatedelay")) {
				if (msg.length > 2) {
					int newDelay = Integer.parseInt(msg[2]);
					channelInfo.setUpdateDelay(newDelay);
					send(channel, "Delay between the song updating is now "
							+ newDelay + " seconds.");
				} else
					send(channel, "Usage is " + prefix
							+ "set updatedelay <seconds>");
			}
			// set cooldown for custom commands
			else if (msg[1].equalsIgnoreCase("cooldown")) {
				if (msg.length > 2) {
					int newCooldown = Integer.parseInt(msg[2]);
					channelInfo.setCooldown(newCooldown);
					send(channel, "Cooldown for custom commands is now "
							+ newCooldown + " seconds.");
				} else
					send(channel, "Usage is " + prefix
							+ "setbullet <new bullet>");
			} else if (msg[1].equalsIgnoreCase("throw")) {
				if (msg[2].equalsIgnoreCase("on")) {
					channelInfo.setThrow(true);
					send(channel, "Feature: !throw is on");
				} else if (msg[2].equalsIgnoreCase("off")) {
					channelInfo.setThrow(false);
					send(channel, "Feature: !throw is off");
				}
			} else if (msg[1].equalsIgnoreCase("lastfm")) {
				if (msg[2].equalsIgnoreCase("off")) {
					channelInfo.setLastfm("");
					send(channel, "Feature: Lastfm is off.");
				} else {
					channelInfo.setLastfm(msg[2]);
					send(channel, "Feature: Lastfm user set to " + msg[2]);
				}
			} else if (msg[1].equalsIgnoreCase("steam")) {
				if (msg[2].equalsIgnoreCase("off")) {
					channelInfo.setSteam("");
					send(channel, "Feature: Steam is off.");
				} else {
					channelInfo.setSteam(msg[2]);
					send(channel, "Feature: Steam id set to " + msg[2]);
				}
			} else if (msg[1].equalsIgnoreCase("mode")) {
				if (msg.length < 3) {
					send(channel, "Mode set to " + channelInfo.getMode() + "");
				} else if ((msg[2].equalsIgnoreCase("0") || msg[2]
						.equalsIgnoreCase("owner")) && isOwner) {
					channelInfo.setMode(0);
					send(channel, "Mode set to admin/owner only.");
				} else if (msg[2].equalsIgnoreCase("1")
						|| msg[2].equalsIgnoreCase("mod")) {
					channelInfo.setMode(1);
					send(channel, "Mode set to admin/owner/mod only.");
				} else if (msg[2].equalsIgnoreCase("2")
						|| msg[2].equalsIgnoreCase("everyone")) {
					channelInfo.setMode(2);
					send(channel, "Mode set to everyone.");
				} else if (msg[2].equalsIgnoreCase("-1")
						|| msg[2].equalsIgnoreCase("admin")) {
					channelInfo.setMode(-1);
					send(channel, "Special moderation mode activated.");
				}
			} else if (msg[1].equalsIgnoreCase("commerciallength")) {
				if (msg.length > 2) {
					int cLength = Integer.parseInt(msg[2]);
					if (cLength == 30 || cLength == 60 || cLength == 90
							|| cLength == 120 || cLength == 150
							|| cLength == 180) {
						channelInfo.setCommercialLength(cLength);
						send(channel, "Commercial length is set to "
								+ channelInfo.getCommercialLength()
								+ " seconds.");
					}
				} else {
					send(channel,
							"Commercial length is "
									+ channelInfo.getCommercialLength()
									+ " seconds.");
				}
			} else if (msg[1].equalsIgnoreCase("tweet")) {
				if (msg.length < 3) {
					send(channel,
							"ClickToTweet format: "
									+ channelInfo.getClickToTweetFormat());
				} else {
					String format = fuseArray(msg, 2);
					if (!format.contains("(_TWEET_URL_)")) {
						channelInfo.setClickToTweetFormat(format);
						send(channel,
								"ClickToTweet format: "
										+ channelInfo.getClickToTweetFormat());
					} else {
						send(channel, "_TWEET_URL_ is not allowed.");
					}

				}
			} else if (msg[1].equalsIgnoreCase("prefix")) {
				if (msg.length > 2) {
					if (msg[2].length() > 1) {
						send(channel, "Prefix may only be 1 character.");
					} else {
						channelInfo.setPrefix(msg[2]);
						send(channel,
								"Command prefix is " + channelInfo.getPrefix());
					}
				} else {
					send(channel,
							"Command prefix is " + channelInfo.getPrefix());
				}
			} else if (msg[1].equalsIgnoreCase("emoteset") && msg.length > 2) {
				channelInfo.setEmoteSet(msg[2]);
				send(channel,
						"Emote set ID set to " + channelInfo.getEmoteSet());
			} else if (msg[1].equalsIgnoreCase("subscriberregulars")) {
				if (msg[2].equalsIgnoreCase("on")) {
					channelInfo.setSubscriberRegulars(true);
					send(channel,
							"Subscribers will now be treated as regulars.");
				} else if (msg[2].equalsIgnoreCase("off")) {
					channelInfo.setSubscriberRegulars(false);
					send(channel,
							"Subscribers will no longer be treated as regulars.");
				}
			} else if (msg[1].equalsIgnoreCase("subscriberalerts")) {
				if (msg.length < 3) {
					send(channel,
							"Subscriber alerts: "
									+ channelInfo.config.get("subscriberAlert"));
					send(channel, "Subscriber alert message: "
							+ channelInfo.config.get("subMessage"));
				} else if (msg[2].equalsIgnoreCase("on")) {
					channelInfo.config.put("subscriberAlert", true);
					send(channel, "Subscriber alerts enabled.");
				} else if (msg[2].equalsIgnoreCase("off")) {
					channelInfo.config.put("subscriberAlert", false);
					send(channel, "Subscriber alerts disabled.");
				} else if (msg[2].equalsIgnoreCase("message") && msg.length > 3) {
					channelInfo.config.put("subMessage", fuseArray(msg, 3));
					send(channel, "Subscriber alert message set to: "
							+ channelInfo.config.get("subMessage"));
				}
			}
			return;
		}

		// !modchan - Mod
		if (msg[0].equalsIgnoreCase(prefix + "modchan") && isOwner) {
			log("RB: Matched command !modchan");
			if (channelInfo.getMode() == 2) {
				channelInfo.setMode(1);
				send(channel, "Mode set to admin/owner/mod only.");
			} else if (channelInfo.getMode() == 1) {
				channelInfo.setMode(2);
				send(channel, "Mode set to everyone.");
			} else {
				send(channel, "Mode can only be changed by bot admin.");
			}
			return;
		}

		// !join
		if (msg[0].equalsIgnoreCase(prefix + "join")
				&& channel.equalsIgnoreCase("#" + getNick())) {
			log("RB: Matched command !join");

			if (!BotManager.getInstance().publicJoin) {
				send(channel, "Public joining is disabled at this time.");
				return;
			}

			if (JSONUtil.krakenChannelExist(sender)) {
				send(channel, "Joining channel #" + sender + ".");
				boolean joinStatus = BotManager.getInstance().addChannel(
						"#" + sender, 2);
				if (joinStatus) {
					send(channel, "Channel #" + sender + " joined.");
				} else {
					send(channel, "Already in channel #" + sender + ".");
				}
			} else {
				send(channel,
						"Unable to join "
								+ sender
								+ ". This could be because your channel is on Justin.tv and not Twitch. If you are sure your channel is on Twitch, try again later.");
			}
			return;
		}

		if (msg[0].equalsIgnoreCase(prefix + "rejoin")) {
			log("RB: Matched command !rejoin");
			if (msg.length > 1 && isAdmin) {
				if (msg[1].contains("#")) {
					send(channel, "Rejoining channel " + msg[1] + ".");
					boolean joinStatus = BotManager.getInstance()
							.rejoinChannel(msg[1]);
					if (joinStatus) {
						send(channel, "Channel " + msg[1] + " rejoined.");
					} else {
						send(channel, "Bot is not assigned to channel "
								+ msg[1] + ".");
					}

				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
			} else {
				send(channel, "Rejoining channel #" + sender + ".");
				boolean joinStatus = BotManager.getInstance().rejoinChannel(
						"#" + sender);
				if (joinStatus) {
					send(channel, "Channel #" + sender + " rejoined.");
				} else {
					send(channel, "Bot is not assigned to channel #" + sender
							+ ".");
				}
			}
			return;
		}

		// ********************************************************************************
		// **************************** Administration Commands
		// ***************************
		// ********************************************************************************

		if (msg[0].equalsIgnoreCase(prefix + "admin") && isAdmin
				&& msg.length > 1) {
			if (msg[1].equalsIgnoreCase("channels")) {
				send(channel, "Currently in "
						+ BotManager.getInstance().channelList.size()
						+ " channels.");
				String channelString = "";
				for (Map.Entry<String, Channel> entry : BotManager
						.getInstance().channelList.entrySet()) {
					channelString += entry.getValue().getChannel() + ", ";
				}
				send(channel, "Channels: " + channelString);
				return;
			} else if (msg[1].equalsIgnoreCase("join") && msg.length > 2) {
				if (msg[2].contains("#")) {
					String toJoin = msg[2];
					int mode = -1;
					if (msg.length > 3 && Main.isInteger(msg[3]))
						mode = Integer.parseInt(msg[3]);
					send(channel, "Joining channel " + toJoin + " with mode ("
							+ mode + ").");
					boolean joinStatus = BotManager.getInstance().addChannel(
							toJoin, mode);
					if (joinStatus) {
						send(channel, "Channel " + toJoin + " joined.");
					} else {
						send(channel, "Already in channel " + toJoin + ".");
					}

				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
				return;
			} else if (msg[1].equalsIgnoreCase("part") && msg.length > 2) {
				if (msg[2].contains("#")) {
					String toPart = msg[2];
					send(channel, "Channel " + toPart + " parting...");
					BotManager.getInstance().removeChannel(toPart);
					send(channel, "Channel " + toPart + " parted.");
				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
				return;
			} else if (msg[1].equalsIgnoreCase("reconnect")) {
				send(channel, "Reconnecting all servers.");
				BotManager.getInstance().reconnectAllBotsSoft();
				return;
			} else if (msg[1].equalsIgnoreCase("reload") && msg.length > 2) {
				if (msg[2].contains("#")) {
					String toReload = msg[2];
					send(channel, "Reloading channel " + toReload);
					BotManager.getInstance().reloadChannel(toReload);
					send(channel, "Channel " + toReload + " reloaded.");
				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
				return;
			} else if (msg[1].equalsIgnoreCase("color") && msg.length > 2) {
				sendCommand(channel, ".color " + msg[2]);
				send(channel, "Color set to " + msg[2]);
				return;
			} else if (msg[1].equalsIgnoreCase("loadfilter")) {
				BotManager.getInstance().loadGlobalBannedWords();
				BotManager.getInstance().loadBanPhraseList();
				send(channel, "Global banned filter reloaded.");
				return;
			} else if (msg[1].equalsIgnoreCase("spam")) {
				if (msg.length > 3 && Main.isInteger(msg[2])) {
					String toSpam = fuseArray(msg, 3);
					for (int i = 0; i < Integer.parseInt(msg[2]); i++)
						send(channel, toSpam + " " + (i + 1));
					return;
				}
			} else if (msg[1].startsWith("#")) {
				if (msg.length > 2) {

					onChannelMessage(channel, msg[1], sender, fuseArray(msg, 2));
				}
			}
		}
		// ********************************************************************************
		// ***************************** Info/Catch-all Command
		// ***************************
		// ********************************************************************************

		long cooldown = channelInfo.getCooldown() * 1L;

		if (msg[0].substring(0, 1).equalsIgnoreCase(prefix)) {
			String command = msg[0].substring(1).toLowerCase();
			String value = channelInfo.getCommand(command);
			if (value != null) {
				log("RB: Matched command " + msg[0]);
				// String[] arguments = new String[msg.length -2];
				// int tempI = 0;
				// for(int i = 2; i<msg.length; i++){
				// arguments[tempI] = msg[i];
				// tempI++;
				// }
				// if (msg.length > 1 && isOwner) {
				// String updatedMessage = fuseArray(msg, 1);
				// if (!updatedMessage.contains(",,")) {
				// channelInfo.setCommand(command, updatedMessage);
				// send(channel, "Command updated.");
				// } else {
				// send(channel,
				// "Command cannot contain double commas (\",,\").");
				// }s
				// } else {
				if (channelInfo.checkCommandRestriction(command, accessLevel)) {
					long currentTime = System.currentTimeMillis();
					if (currentTime > (lastCommand + cooldown * 1000L) || isOp) {
						lastCommand = currentTime;

						if (value.contains("(_PURGE_)")) {
							value = value.replace("(_PURGE_)",
									msg[1].toLowerCase());
							sendCommand(channel,
									".timeout " + msg[1].toLowerCase() + " 1");
						} else if (value.contains("(_TIMEOUT_)")) {
							value = value.replace("(_TIMEOUT_)",
									msg[1].toLowerCase());
							sendCommand(channel,
									".timeout " + msg[1].toLowerCase());

						} else if (value.contains("(_BAN_)")) {
							value = value.replace("(_BAN_)",
									msg[1].toLowerCase());
							sendCommand(channel, ".ban " + msg[1].toLowerCase());
						}
						if (value.contains("(_PARAMETER_)")) {

							String[] parts = fuseArray(msg, 1).split(";");
							if (parts.length > 1) {
								for (String s : parts) {
									value = value.replaceFirst(
											"\\(_PARAMETER_\\)", s.trim());
								}
							} else
								value = value
										.replace("(_PARAMETER_)", parts[0]);
							// value = value.replace("(_PARAMETER_)",
							// fuseArray(msg, 1));
						}
						channelInfo.increaseCommandCount(command);
						send(channel, sender, value);

					}
				}
				// }

			}

		}

		// ********************************************************************************
		// *********************************** Auto Reply
		// *********************************
		// ********************************************************************************
		boolean matched = false;
		for (int i = 0; i < channelInfo.autoReplyTrigger.size(); i++) {
			Matcher m = channelInfo.autoReplyTrigger.get(i).matcher(message);
			if (m.matches()) {
				if (matched) {
					matched = false;
					break;
				}
				matched = true;
				if (!channelInfo.onCooldown(channelInfo.autoReplyTrigger.get(i)
						.toString())) {
					String value = channelInfo.autoReplyResponse.get(i);
					if (value.contains("(_REGULARS_ONLY_)")) {
						if (isSub) {
							value = value.replace("(_REGULARS_ONLY_)", "");
							if (value.contains("(_PURGE_)")) {
								value = value.replace("(_PURGE_)", sender);
								sendCommand(channel, ".timeout " + sender
										+ " 1");
							} else if (value.contains("(_TIMEOUT_)")) {
								value = value.replace("(_TIMEOUT_)", sender);
								sendCommand(channel, ".timeout " + sender);

							} else if (value.contains("(_BAN_)")) {
								value = value.replace("(_BAN_)", sender);
								sendCommand(channel, ".ban " + sender);
							}
							send(channel, sender, value);
							channelInfo
									.registerCommandUsage(channelInfo.autoReplyTrigger
											.get(i).toString());
						}
					} else {

						if (value.contains("(_PURGE_)")) {
							value = value.replace("(_PURGE_)", sender);
							sendCommand(channel, ".timeout " + sender + " 1");
						} else if (value.contains("(_TIMEOUT_)")) {
							value = value.replace("(_TIMEOUT_)", sender);
							sendCommand(channel, ".timeout " + sender);

						} else if (value.contains("(_BAN_)")) {
							value = value.replace("(_BAN_)", sender);
							sendCommand(channel, ".ban " + sender);
						}

						send(channel, sender, value);
						channelInfo
								.registerCommandUsage(channelInfo.autoReplyTrigger
										.get(i).toString());
					}
				}
			}
		}
	}

	protected void onAdministrativeMessage(String message, Channel channelInfo) {
		String[] msg = message.trim().split(" ");

		if (msg.length > 0) {
			if (msg[0].equalsIgnoreCase("SPECIALUSER")) {
				String user = msg[1];
				String tag = msg[2];

				if (tag.equalsIgnoreCase("admin")
						|| tag.equalsIgnoreCase("staff"))
					BotManager.getInstance().addTagAdmin(user);
				if (tag.equalsIgnoreCase("staff"))
					BotManager.getInstance().addTagStaff(user);
				if (tag.equalsIgnoreCase("subscriber") && channelInfo != null) {
					if (!user.equalsIgnoreCase("Coebot")) {
						privMsgSub = true;
					}

				}
				if (tag.equalsIgnoreCase("subscriber") && channelInfo == null) {
					if (!user.equalsIgnoreCase("Coebot")) {
						privMsgSub = true;
					}
				}

			} else if (msg[0].equalsIgnoreCase("USERCOLOR")) {
				String user = msg[1];
				String color = msg[2];
			} else if (msg[0].equalsIgnoreCase("CLEARCHAT")) {
				if (msg.length > 1) {
					String user = msg[1];
					if (!BotManager.getInstance().verboseLogging)
						System.out.println("RAW: CLEARCHAT " + user);
				} else {
					if (!BotManager.getInstance().verboseLogging)
						System.out.println("RAW: CLEARCHAT");
				}
			} else if (msg[0].equalsIgnoreCase("HISTORYEND")) {
				String channel = msg[1];
				Channel ci = BotManager.getInstance().getChannel("#" + channel);
				ci.active = true;
				// System.out.println("DEBUG: Channel " + ci.getChannel()
				// + " marked active.");
			} else if (msg[0].equalsIgnoreCase("EMOTESET")) {
				String user = msg[1];
				String setsList = msg[2].replaceAll("(\\[|\\])", "");
				String[] sets = setsList.split(",");
				for (String s : sets)
					BotManager.getInstance().addSubBySet(user, s);
			}
		}
	}

	protected void onNewSubscriber(Channel channel, String username) {
		System.out.println("RB: New subscriber in " + channel.getTwitchName()
				+ " " + username);
		logMain("New subscriber to " + channel + ": " + username);
		if (Boolean
				.parseBoolean((String) channel.config.get("subscriberAlert"))) {
			String msgFormat = (String) channel.config.get("subMessage");
			send(channel.getChannel(), null, msgFormat,
					new String[] { username });
		}
	}

	private void setRandomNickColor() {
		if (!BotManager.getInstance().randomNickColor)
			return;

		countToNewColor--;

		if (countToNewColor == 0) {
			countToNewColor = BotManager.getInstance().randomNickColorDiff;
			Color newColor = new Color(Color.HSBtoRGB(random.nextFloat(), 1.0f,
					0.65f));
			System.out.println(newColor.toString());
			String hexColor = String.format("#%06X",
					(0xFFFFFF & newColor.getRGB()));
			System.out.println("New color: " + hexColor);
			sendCommand("#" + getNick(), ".color " + hexColor);
		}

	}

	// private Color generateRandomColor() {
	// Color mix = new Color(0, 0, 0);
	// Random random = new Random();
	// int red = random.nextInt(256);
	// int green = random.nextInt(256);
	// int blue = random.nextInt(256);
	//
	// // mix the color
	// if (mix != null) {
	// red = (red + mix.getRed()) / 2;
	// green = (green + mix.getGreen()) / 2;
	// blue = (blue + mix.getBlue()) / 2;
	// }
	//
	// Color color = new Color(red, green, blue);
	// return color;
	// }

	@Override
	public void onDisconnect() {
		lastPing = -1;
		try {
			System.out.println("INFO: Internal reconnection: "
					+ this.getServer());
			String[] channels = this.getChannels();
			try {
				System.out
						.println("Sleeping for 15 seconds to allow for more JOINs");
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.reconnect();
			for (int i = 0; i < channels.length; i++) {
				this.joinChannel(channels[i]);
				try {
					Thread.sleep(350);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (NickAlreadyInUseException e) {
			logMain("RB: [ERROR] Nickname already in use - " + this.getNick()
					+ " " + this.getServer());
		} catch (IOException e) {
			logMain("RB: [ERROR] Unable to connect to server - "
					+ this.getNick() + " " + this.getServer());
		} catch (IrcException e) {
			logMain("RB: [ERROR] Error connecting to server - "
					+ this.getNick() + " " + this.getServer());
		}

	}

	public void onJoin(String channel, String sender, String login,
			String hostname) {

		Channel channelInfo = getChannelObject(channel);

		if (channelInfo == null)
			return;

		if (this.getNick().equalsIgnoreCase(sender)) {
			log("RB: Got self join for " + channel);
			if (BotManager.getInstance().ignoreHistory) {
				System.out.println("DEBUG: Marking " + channel
						+ " as inactive.");
				channelInfo.active = false;
			}
		}
	}

	public void onPart(String channel, String sender, String login,
			String hostname) {

		Channel channelInfo = getChannelObject(channel);

		if (channelInfo == null)
			return;
	}

	public void send(String target, String sender, String message) {
		send(target, sender, message, null);
	}

	public void send(String target, String message) {
		send(target, null, message, null);
	}

	public void send(String target, String sender, String message, String[] args) {
		if (msgTimer.size() > 19) {

			msgTimer.add(System.currentTimeMillis());
			System.out.println(msgTimer.size());

			long diff = 0;
			try {
				diff = msgTimer.get(20) - msgTimer.get(0);
			} catch (Exception e) {
				logMain("RESETTING THE MSGTIMER QUEUE");
				e.printStackTrace();
				msgTimer = new ArrayList<Long>();
			}
			log("RB: There are " + msgTimer.size()
					+ " times in msgTimer. Diff = " + diff);
			if (diff > 30 * 1000L) {

				msgTimer.remove(0);
				Channel channelInfo = getChannelObject(target);

				if (!BotManager.getInstance().verboseLogging)
					logMain("SEND: " + target + " " + getNick() + " : "
							+ message);

				message = MessageReplaceParser.parseMessage(target, sender,
						message, args);
				boolean useBullet = true;

				if (message.startsWith("/me "))
					useBullet = false;

				// Split if message > X characters
				if (message.length() > 0) {
					List<String> chunks = Main.splitEqually(message, 500);
					int c = 1;
					if (target == null) {
						sendMessage(target, "The bullet is null.");
					}
					for (String chunk : chunks) {
						sendMessage(target,
								(useBullet ? channelInfo.getChannelBullet()
										+ " " : "")
										+ (chunks.size() > 1 ? "[" + c + "] "
												: "") + chunk);
						c++;
						useBullet = true;
					}
				}
				// sendMessage(target,message);
				// setRandomNickColor();
				if (tried) {
					delete = true;
					tried = false;
				}
				checkQueued();
			} else {
				msgTimer.remove(20);
				log("RB: Prevented overflow of messages that would result in a ban.");
				QueuedMessage qm = new QueuedMessage(target, sender, message,
						args);
				boolean matchesAny = false;
				for (int i = 0; i < queuedMessages.size(); i++) {
					if (queuedMessages.get(i).getMessage().equals(message)
							&& queuedMessages.get(i).getTarget().equals(target))
						matchesAny = true;
				}
				if (!matchesAny) {
					queuedMessages.add(qm);
				}

			}
		} else {
			log("RB: seeding msgTimer list");
			for (int i = 0; i < (20); i++) {
				msgTimer.add(System.currentTimeMillis() - 31000L);
			}
			QueuedMessage qm = new QueuedMessage(target, sender, message, args);
			queuedMessages.add(qm);
		}

	}

	public void checkQueued() {
		log("There are " + queuedMessages.size() + " queued messages");
		if (delete) {
			queuedMessages.remove(0);
			delete = false;
		}
		if (queuedMessages.size() > 0) {
			tried = true;
			QueuedMessage qm = queuedMessages.get(0);
			if (qm.isCommand()) {

				sendCommand(qm.getTarget(), qm.getMessage());
			} else {
				send(qm.getTarget(), qm.getSender(), qm.getMessage(),
						qm.getArgs());

			}
		}

	}

	public void sendCommand(String target, String message) {
		if (msgTimer.size() > 19) {
			System.out.println(msgTimer.size());
			msgTimer.add(System.currentTimeMillis());

			long diff = 0;
			try {
				diff = msgTimer.get(20) - msgTimer.get(0);
			} catch (Exception e) {
				logMain("RESETTING THE MSGTIMER QUEUE");
				e.printStackTrace();
				msgTimer = new ArrayList<Long>();
			}

			log("RB: There are " + msgTimer.size()
					+ " times in msgTimer. Diff = " + diff);

			if (diff > 30 * 1000L) {
				msgTimer.remove(0);

				sendMessage(target, message);
				if (tried) {
					delete = true;
					tried = false;
				}
				checkQueued();

			} else {
				msgTimer.remove(20);
				log("RB: Prevented overflow of messages that would result in a ban.");
				QueuedMessage qm = new QueuedMessage(target, message, true);
				boolean matchesAny = false;
				for (int i = 0; i < queuedMessages.size(); i++) {
					if (queuedMessages.get(i).getMessage().equals(message)
							&& queuedMessages.get(i).getTarget().equals(target))
						matchesAny = true;
				}
				if (!matchesAny)
					queuedMessages.add(qm);

			}
		} else {
			log("RB: seeding msgTimer list");
			for (int i = 0; i < (20); i++) {
				msgTimer.add(System.currentTimeMillis() - 31000L);
			}

		}
	}

	@Override
	public void onServerPing(String response) {
		super.onServerPing(response);
		lastPing = (int) (System.currentTimeMillis() / 1000);
	}

	private User matchUser(String nick, String channel) {
		User[] userList = this.getUsers(channel);

		for (int i = 0; i < userList.length; i++) {
			if (userList[i].equals(nick)) {
				return userList[i];
			}
		}
		return null;
	}

	public void log(String line) {
		if (this.getVerbose()) {
			logMain(System.currentTimeMillis() + " " + line);
		}
	}

	public void logMain(String line) {
		BotManager.getInstance().log(line);
	}

	private void startJoinCheck() {

		joinCheck = new Timer();

		int delay = 60000;

		joinCheck.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				String[] currentChanList = ReceiverBot.this.getChannels();
				for (Map.Entry<String, Channel> entry : BotManager
						.getInstance().channelList.entrySet()) {
					boolean inList = false;
					for (String c : currentChanList) {
						if (entry.getValue().getChannel().equals(c))
							inList = true;
					}

					if (!inList) {
						log("RB: " + entry.getValue().getChannel()
								+ " is not in the joined list.");
						ReceiverBot.this.joinChannel(entry.getValue()
								.getChannel());
						try {
							Thread.sleep(350);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}
		}, delay, delay);

	}

	private int getSymbolsNumber(String s) {
		int symbols = 0;
		for (Pattern p : symbolsPatterns) {
			Matcher m = p.matcher(s);
			while (m.find())
				symbols += 1;
		}
		return symbols;
	}

	private int getCapsNumber(String s) {
		int caps = 0;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isUpperCase(s.charAt(i))) {
				caps++;
			}
		}

		return caps;
	}

	private int countConsecutiveCapitals(String s) {
		int caps = 0;
		int max = 0;
		// boolean con = true;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isUpperCase(s.charAt(i))) {
				caps++;
			} else {
				if (caps > 0 && caps > max)
					max = caps;
				caps = 0;
			}
		}
		if (caps > max)
			return caps;
		else
			return max;
	}

	private boolean containsLink(String message, Channel ch) {
		String[] splitMessage = message.toLowerCase().split(" ");
		for (String m : splitMessage) {
			for (Pattern pattern : linkPatterns) {
				// System.out.println("Checking " + m + " against " +
				// pattern.pattern());
				Matcher match = pattern.matcher(m);
				if (match.matches()) {
					log("RB: Link match on " + pattern.pattern());
					if (ch.checkPermittedDomain(m))
						return false;
					else
						return true;
				}
			}
		}
		// for (Pattern pattern : linkPatterns) {
		// System.out.println("Checking " + message + " against " +
		// pattern.pattern());
		// Matcher match = pattern.matcher(message);
		// if (match.matches()) {
		// System.out.println("Bypass match");
		// return true;
		// }
		// }
		return false;
	}

	private boolean containsSymbol(String message, Channel ch) {

		for (Pattern pattern : symbolsPatterns) {
			Matcher match = pattern.matcher(message);
			if (match.find()) {
				log("RB: Symbol match on " + pattern.pattern());
				return true;
			}

		}

		return false;
	}

	private int countEmotes(String message) {
		String str = message;
		int count = 0;
		for (String findStr : BotManager.getInstance().emoteSet) {
			int lastIndex = 0;
			while (lastIndex != -1) {

				lastIndex = str.indexOf(findStr, lastIndex);

				if (lastIndex != -1) {
					count++;
					lastIndex += findStr.length();
				}
			}
		}
		return count;
	}

	private boolean checkSingleEmote(String message) {
		for (String emote : BotManager.getInstance().emoteSet) {
			if (emote.equals(message))
				return true;
		}
		return false;
	}

	public boolean isGlobalBannedWord(String message) {
		for (Pattern reg : BotManager.getInstance().globalBannedWords) {
			Matcher match = reg.matcher(message.toLowerCase());
			if (match.matches()) {
				log("RB: Global banned word matched: " + reg.toString());
				return true;
			}
		}
		return false;
	}

	private String getTimeoutText(int count, Channel channel) {
		if (channel.getEnableWarnings()) {
			if (count > 1) {
				return "timeout";
			} else {
				return "warning";
			}
		} else {
			return "timeout";
		}
	}

	private int getTODuration(int count, Channel channel) {
		if (channel.getEnableWarnings()) {
			if (count > 1) {
				return channel.getTimeoutDuration();
			} else {
				return 10;
			}
		} else {
			return channel.getTimeoutDuration();
		}
	}

	private void secondaryTO(final String channel, final String name,
			final int duration, FilterType type, String message) {

		String line = "FILTER: Issuing a timeout on " + name + " in " + channel
				+ " for " + type.toString() + " (" + duration + ")";
		logMain(line);
		line = "FILTER: Affected Message: " + message;
		logMain(line);

		int iterations = BotManager.getInstance().multipleTimeout;

		for (int i = 0; i < iterations; i++) {
			Timer timer = new Timer();
			int delay = 1000 * i;
			timer.schedule(new TimerTask() {
				public void run() {
					ReceiverBot.this.sendCommand(channel, ".timeout " + name
							+ " " + duration);
				}
			}, delay);
		}

		// Send to subscribers
		Channel channelInfo = getChannelObject(channel);
		if (BotManager.getInstance().wsEnabled)
			BotManager.getInstance().ws.sendToSubscribers(line, channelInfo);

	}

	private void secondaryBan(final String channel, final String name,
			FilterType type) {

		String line = "RB: Issuing a ban on " + name + " in " + channel
				+ " for " + type.toString();
		logMain(line);

		int iterations = BotManager.getInstance().multipleTimeout;
		for (int i = 0; i < iterations; i++) {
			Timer timer = new Timer();
			int delay = 1000 * i;
			System.out.println("Delay: " + delay);
			timer.schedule(new TimerTask() {
				public void run() {
					ReceiverBot.this.sendCommand(channel, ".ban " + name);
				}
			}, delay);
		}

		// Send to subscribers
		Channel channelInfo = getChannelObject(channel);
		if (BotManager.getInstance().wsEnabled)
			BotManager.getInstance().ws.sendToSubscribers(line, channelInfo);

	}

	private void startGaTimer(int seconds, Channel channelInfo) {
		if (channelInfo.getGiveaway() != null) {
			channelInfo.getGiveaway().setTimer(new Timer());
			int delay = seconds * 1000;

			if (!channelInfo.getGiveaway().getStatus()) {
				channelInfo.getGiveaway().setStatus(true);
				send(channelInfo.getChannel(), "> Giveaway started. ("
						+ seconds + " seconds)");
			}

			channelInfo.getGiveaway().getTimer()
					.schedule(new giveawayTimer(channelInfo), delay);
		}
	}

	public String getTimeStreaming(String uptime) {
		uptime = uptime.replace("Z", "UTC");
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
		try {
			Date then = format.parse(uptime);
			return "Streaming for " + this.getTimeTilNow(then) + ".";
		} catch (ParseException e) {
			e.printStackTrace();

		}

		return "An error occurred or stream is offline.";
	}

	public boolean checkStalePing() {
		if (lastPing == -1)
			return false;

		int difference = ((int) (System.currentTimeMillis() / 1000)) - lastPing;

		if (difference > BotManager.getInstance().pingInterval) {
			log("RB: Ping is stale. Last ping= " + lastPing + " Difference= "
					+ difference);
			lastPing = -1;
			return true;
		}

		return false;
	}

	private String fuseArray(String[] array, int start) {
		String fused = "";
		for (int c = start; c < array.length; c++)
			fused += array[c] + " ";

		return fused.trim();

	}

	public String getTimeTilNow(Date date) {
		long difference = (long) (System.currentTimeMillis() / 1000)
				- (date.getTime() / 1000);
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

	public void logGlobalBan(String channel, String sender, String message) {
		String line = sender + "," + channel + ",\"" + message + "\"\n";

		// System.out.print(line);
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("globalbans.csv", true), "UTF-8"));
			out.write(line);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	private class giveawayTimer extends TimerTask {
		private Channel channelInfo;

		public giveawayTimer(Channel channelInfo2) {
			super();
			channelInfo = channelInfo2;
		}

		public void run() {
			if (channelInfo.getGiveaway() != null) {
				if (channelInfo.getGiveaway().getStatus()) {
					channelInfo.getGiveaway().setStatus(false);
					ReceiverBot.this.send(channelInfo.getChannel(),
							"> Giveaway over.");
				}
			}
		}
	}
}
