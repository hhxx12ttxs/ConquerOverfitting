/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
 * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
 */
package mu.nu.nullpo.game.net;

import java.io.Serializable;
import java.util.LinkedList;

import mu.nu.nullpo.game.component.RuleOptions;

/**
 * ?????
 */
public class NetRoomInfo implements Serializable {
	/** Serial version */
	private static final long serialVersionUID = 1L;

	/** ?? number */
	public int roomID = -1;

	/** ???? */
	public String strName = "";

	/** ?????Maximum?count */
	public int maxPlayers = 6;

	/** ????????? time */
	public int autoStartSeconds = 0;

	/** ????(??) */
	public int gravity = 1;

	/** ????(??) */
	public int denominator = 60;

	/** ARE */
	public int are = 30;

	/** ARE after line clear */
	public int areLine = 30;

	/** Line clear time */
	public int lineDelay = 40;

	/** ?? time */
	public int lockDelay = 30;

	/** DAS */
	public int das = 14;

	/** Flag for types of T-Spins allowed (0=none, 1=normal, 2=all spin) */
	public int tspinEnableType = 1;

	/** Spin detection type */
	public static final int SPINTYPE_4POINT = 0, SPINTYPE_IMMOBILE = 1;

	public int spinCheckType = SPINTYPE_4POINT;

	/** Allow EZ-spins in spinCheckType 2 */
	public boolean tspinEnableEZ = false;

	/** Flag for enabling B2B */
	public boolean b2b = true;

	/** b2b adds as a separate garbage chunk */
	public boolean b2bChunk;

	/** Flag for enabling combos */
	public boolean combo = true;

	/** Allow Rensa/Combo Block */
	public boolean rensaBlock = true;

	/** Allow garbage countering */
	public boolean counter = true;

	/** Enable bravo bonus */
	public boolean bravo = true;

	/** ????? flag */
	public boolean ruleLock = false;

	/** Rule name */
	public String ruleName = "";

	/** ??? */
	public RuleOptions ruleOpt = null;

	/** ??????Number of players */
	public int playerSeatedCount = 0;

	/** ??????count */
	public int spectatorCount = 0;

	/** ??????????????(???+???) */
	public int playerListCount = 0;

	/** ???? flag */
	public boolean playing = false;

	/** Start game???Number of players */
	public int startPlayers = 0;

	/** ?????? */
	public int deadCount = 0;

	/** Automatically start timer?????????true */
	public boolean autoStartActive = false;

	/** ??OK????????Cancel???true */
	public boolean isSomeoneCancelled = false;

	/** 3??????????? Attack ????? */
	public boolean reduceLineSend = false;

	/** Rate of change of garbage holes */
	public int garbagePercent = 100;

	/** Hole change style (false=line true=attack) */
	public boolean garbageChangePerAttack = true;

	/** Divide change rate by number of live players/teams to mimic feel of 1v1 */
	public boolean divideChangeRateByPlayers = false;

	/** Garbage send type (false=Send to all, true=Target) */
	public boolean isTarget = false;

	/** Targeting time */
	public int targetTimer = 60;

	//public boolean useTankMode = false;

	/** Hurryup??????count(-1?Hurryup??) */
	public int hurryupSeconds = -1;

	/** Hurryup????Block?????????????? */
	public int hurryupInterval = 5;

	/** Automatically start timer type(false=NullpoMino true=TNET2) */
	public boolean autoStartTNET2 = false;

	/** ??OK????????Cancel???Timer??? */
	public boolean disableTimerAfterSomeoneCancelled = false;

	/** Map is enabled */
	public boolean useMap = false;

	/** ???Map */
	public int mapPrevious = -1;

	/** ??????garbage block??????? */
	public boolean useFractionalGarbage = false;

	/** Mode name */
	public String strMode = "";

	/** Single player flag */
	public boolean singleplayer = false;

	/** Rated-game flag */
	public boolean rated = false;

	/** Custom rated-game flag */
	public boolean customRated = false;

	/** Game style */
	public int style = 0;

	/** ?????? */
	public LinkedList<String> mapList = new LinkedList<String>();

	/** ??????????? */
	public LinkedList<NetPlayerInfo> playerList = new LinkedList<NetPlayerInfo>();

	/** ???? */
	public LinkedList<NetPlayerInfo> playerSeat = new LinkedList<NetPlayerInfo>();

	/** ????(Start game????????????????????????????????????) */
	public LinkedList<NetPlayerInfo> playerSeatNowPlaying = new LinkedList<NetPlayerInfo>();

	/** ???? */
	public LinkedList<NetPlayerInfo> playerQueue = new LinkedList<NetPlayerInfo>();

	/** Dead player list (Pushed from front, winner will be the first entry) */
	public LinkedList<NetPlayerInfo> playerSeatDead = new LinkedList<NetPlayerInfo>();

	/** Chat messages */
	public LinkedList<NetChatMessage> chatList = new LinkedList<NetChatMessage>();

	/**
	 * Constructor
	 */
	public NetRoomInfo() {
	}

	/**
	 * Copy constructor
	 *
	 * @param n
	 *            Copy source
	 */
	public NetRoomInfo(NetRoomInfo n) {
		copy(n);
	}

	/**
	 * String????? data????Constructor
	 *
	 * @param rdata
	 *            String???(String[7])
	 */
	public NetRoomInfo(String[] rdata) {
		importStringArray(rdata);
	}

	/**
	 * String?? data????Constructor
	 *
	 * @param str
	 *            String
	 */
	public NetRoomInfo(String str) {
		importString(str);
	}

	/**
	 * ??NetRoomInfo?????
	 *
	 * @param n
	 *            Copy source
	 */
	public void copy(NetRoomInfo n) {
		roomID = n.roomID;
		strName = n.strName;
		maxPlayers = n.maxPlayers;
		autoStartSeconds = n.autoStartSeconds;
		gravity = n.gravity;
		denominator = n.denominator;
		are = n.are;
		areLine = n.areLine;
		lineDelay = n.lineDelay;
		lockDelay = n.lockDelay;
		das = n.das;
		tspinEnableType = n.tspinEnableType;
		spinCheckType = n.spinCheckType;
		tspinEnableEZ = n.tspinEnableEZ;
		b2b = n.b2b;
		b2bChunk = n.b2bChunk;
		combo = n.combo;
		rensaBlock = n.rensaBlock;
		counter = n.counter;
		bravo = n.bravo;

		ruleLock = n.ruleLock;
		ruleName = n.ruleName;
		if (n.ruleOpt != null) {
			ruleOpt = new RuleOptions(n.ruleOpt);
		} else {
			ruleOpt = null;
		}

		playerSeatedCount = n.playerSeatedCount;
		spectatorCount = n.spectatorCount;
		playerListCount = n.playerListCount;
		playing = n.playing;
		startPlayers = n.startPlayers;
		deadCount = n.deadCount;
		autoStartActive = n.autoStartActive;
		isSomeoneCancelled = n.isSomeoneCancelled;
		reduceLineSend = n.reduceLineSend;
		hurryupSeconds = n.hurryupSeconds;
		hurryupInterval = n.hurryupInterval;
		autoStartTNET2 = n.autoStartTNET2;
		disableTimerAfterSomeoneCancelled = n.disableTimerAfterSomeoneCancelled;
		useMap = n.useMap;
		mapPrevious = n.mapPrevious;
		useFractionalGarbage = n.useFractionalGarbage;
		garbageChangePerAttack = n.garbageChangePerAttack;
		garbagePercent = n.garbagePercent;
		divideChangeRateByPlayers = n.divideChangeRateByPlayers;
		isTarget = n.isTarget;
		targetTimer = n.targetTimer;
		//useTankMode = n.useTankMode;
		strMode = n.strMode;
		singleplayer = n.singleplayer;
		rated = n.rated;
		customRated = n.customRated;
		style = n.style;

		mapList.clear();
		mapList.addAll(n.mapList);
		playerList.clear();
		playerList.addAll(n.playerList);
		playerSeat.clear();
		playerSeat.addAll(n.playerSeat);
		playerSeatNowPlaying.clear();
		playerSeatNowPlaying.addAll(n.playerSeatNowPlaying);
		playerQueue.clear();
		playerQueue.addAll(n.playerQueue);
		playerSeatDead.clear();
		playerSeatDead.addAll(n.playerSeatDead);
		chatList.clear();
		chatList.addAll(n.chatList);
	}

	/**
	 * String????? data??(Player?????)
	 *
	 * @param rdata
	 *            String???(String[43])
	 */
	public void importStringArray(String[] rdata) {
		roomID = Integer.parseInt(rdata[0]);
		strName = NetUtil.urlDecode(rdata[1]);
		maxPlayers = Integer.parseInt(rdata[2]);
		playerSeatedCount = Integer.parseInt(rdata[3]);
		spectatorCount = Integer.parseInt(rdata[4]);
		playerListCount = Integer.parseInt(rdata[5]);
		playing = Boolean.parseBoolean(rdata[6]);
		ruleLock = Boolean.parseBoolean(rdata[7]);
		ruleName = NetUtil.urlDecode(rdata[8]);
		autoStartSeconds = Integer.parseInt(rdata[9]);
		gravity = Integer.parseInt(rdata[10]);
		denominator = Integer.parseInt(rdata[11]);
		are = Integer.parseInt(rdata[12]);
		areLine = Integer.parseInt(rdata[13]);
		lineDelay = Integer.parseInt(rdata[14]);
		lockDelay = Integer.parseInt(rdata[15]);
		das = Integer.parseInt(rdata[16]);
		tspinEnableType = Integer.parseInt(rdata[17]);
		b2b = Boolean.parseBoolean(rdata[18]);
		combo = Boolean.parseBoolean(rdata[19]);
		rensaBlock = Boolean.parseBoolean(rdata[20]);
		counter = Boolean.parseBoolean(rdata[21]);
		bravo = Boolean.parseBoolean(rdata[22]);
		reduceLineSend = Boolean.parseBoolean(rdata[23]);
		hurryupSeconds = Integer.parseInt(rdata[24]);
		hurryupInterval = Integer.parseInt(rdata[25]);
		autoStartTNET2 = Boolean.parseBoolean(rdata[26]);
		disableTimerAfterSomeoneCancelled = Boolean.parseBoolean(rdata[27]);
		useMap = Boolean.parseBoolean(rdata[28]);
		useFractionalGarbage = Boolean.parseBoolean(rdata[29]);
		garbageChangePerAttack = Boolean.parseBoolean(rdata[30]);
		garbagePercent = Integer.parseInt(rdata[31]);
		spinCheckType = Integer.parseInt(rdata[32]);
		tspinEnableEZ = Boolean.parseBoolean(rdata[33]);
		b2bChunk = Boolean.parseBoolean(rdata[34]);
		strMode = NetUtil.urlDecode(rdata[35]);
		singleplayer = Boolean.parseBoolean(rdata[36]);
		rated = Boolean.parseBoolean(rdata[37]);
		customRated = Boolean.parseBoolean(rdata[38]);
		style = Integer.parseInt(rdata[39]);
		divideChangeRateByPlayers = Boolean.parseBoolean(rdata[40]);
		if(rdata.length > 41) isTarget = Boolean.parseBoolean(rdata[41]);
		if(rdata.length > 42) targetTimer = Integer.parseInt(rdata[42]);
		//useTankMode = Boolean.parseBoolean(rdata[43]);
	}

	/**
	 * String(;????)?? data??(Player?????)
	 *
	 * @param str
	 *            String
	 */
	public void importString(String str) {
		importStringArray(str.split(";"));
	}

	/**
	 * String??????(Player?????)
	 *
	 * @return String???(String[43])
	 */
	public String[] exportStringArray() {
		String[] rdata = new String[43];
		rdata[0] = Integer.toString(roomID);
		rdata[1] = NetUtil.urlEncode(strName);
		rdata[2] = Integer.toString(maxPlayers);
		rdata[3] = Integer.toString(playerSeatedCount);
		rdata[4] = Integer.toString(spectatorCount);
		rdata[5] = Integer.toString(playerListCount);
		rdata[6] = Boolean.toString(playing);
		rdata[7] = Boolean.toString(ruleLock);
		rdata[8] = NetUtil.urlEncode(ruleName);
		rdata[9] = Integer.toString(autoStartSeconds);
		rdata[10] = Integer.toString(gravity);
		rdata[11] = Integer.toString(denominator);
		rdata[12] = Integer.toString(are);
		rdata[13] = Integer.toString(areLine);
		rdata[14] = Integer.toString(lineDelay);
		rdata[15] = Integer.toString(lockDelay);
		rdata[16] = Integer.toString(das);
		rdata[17] = Integer.toString(tspinEnableType);
		rdata[18] = Boolean.toString(b2b);
		rdata[19] = Boolean.toString(combo);
		rdata[20] = Boolean.toString(rensaBlock);
		rdata[21] = Boolean.toString(counter);
		rdata[22] = Boolean.toString(bravo);
		rdata[23] = Boolean.toString(reduceLineSend);
		rdata[24] = Integer.toString(hurryupSeconds);
		rdata[25] = Integer.toString(hurryupInterval);
		rdata[26] = Boolean.toString(autoStartTNET2);
		rdata[27] = Boolean.toString(disableTimerAfterSomeoneCancelled);
		rdata[28] = Boolean.toString(useMap);
		rdata[29] = Boolean.toString(useFractionalGarbage);
		rdata[30] = Boolean.toString(garbageChangePerAttack);
		rdata[31] = Integer.toString(garbagePercent);
		rdata[32] = Integer.toString(spinCheckType);
		rdata[33] = Boolean.toString(tspinEnableEZ);
		rdata[34] = Boolean.toString(b2bChunk);
		rdata[35] = NetUtil.urlEncode(strMode);
		rdata[36] = Boolean.toString(singleplayer);
		rdata[37] = Boolean.toString(rated);
		rdata[38] = Boolean.toString(customRated);
		rdata[39] = Integer.toString(style);
		rdata[40] = Boolean.toString(divideChangeRateByPlayers);
		rdata[41] = Boolean.toString(isTarget);
		rdata[42] = Integer.toString(targetTimer);
		//rdata[43] = Boolean.toString(useTankMode);

		return rdata;
	}

	/**
	 * String???(;????)(Player?????)
	 *
	 * @return String
	 */
	public String exportString() {
		String[] data = exportStringArray();
		String strResult = "";

		for (int i = 0; i < data.length; i++) {
			strResult += data[i];
			if (i < data.length - 1)
				strResult += ";";
		}

		return strResult;
	}

	/**
	 * Number of players???????
	 */
	public void updatePlayerCount() {
		playerSeatedCount = getNumberOfPlayerSeated();
		playerListCount = playerList.size();
		spectatorCount = playerListCount - playerSeatedCount;
	}

	/**
	 * ??????????count?count??(null?????????)
	 *
	 * @return ??????????count
	 */
	public int getNumberOfPlayerSeated() {
		int count = 0;
		for (int i = 0; i < playerSeat.size(); i++) {
			if (playerSeat.get(i) != null)
				count++;
		}
		return count;
	}

	/**
	 * ????Player???????????????
	 *
	 * @param pInfo
	 *            Player
	 * @return ????Player??????????true
	 */
	public boolean isPlayerInSeat(NetPlayerInfo pInfo) {
		return playerSeat.contains(pInfo);
	}

	/**
	 * ????Player??? number????????????
	 *
	 * @param pInfo
	 *            Player
	 * @return ???? number(?????-1)
	 */
	public int getPlayerSeatNumber(NetPlayerInfo pInfo) {
		for (int i = 0; i < playerSeat.size(); i++) {
			if (playerSeat.get(i) == pInfo) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return ????????????????????true
	 */
	public boolean canJoinSeat() {
		return (getNumberOfPlayerSeated() < maxPlayers);
	}

	/**
	 * ???????
	 *
	 * @param pInfo
	 *            Player
	 * @return ????? number(??????-1)
	 */
	public int joinSeat(NetPlayerInfo pInfo) {
		if (canJoinSeat()) {
			exitQueue(pInfo);

			for (int i = 0; i < playerSeat.size(); i++) {
				if (playerSeat.get(i) == null) {
					playerSeat.set(i, pInfo);
					return i;
				}
			}

			playerSeat.add(pInfo);
			return playerSeat.size() - 1;
		}
		return -1;
	}

	/**
	 * ????Player?????????
	 *
	 * @param pInfo
	 *            Player
	 */
	public void exitSeat(NetPlayerInfo pInfo) {
		for (int i = 0; i < playerSeat.size(); i++) {
			if (playerSeat.get(i) == pInfo) {
				playerSeat.set(i, null);
			}
		}
	}

	/**
	 * ???????
	 *
	 * @param pInfo
	 *            Player
	 * @return ???? number
	 */
	public int joinQueue(NetPlayerInfo pInfo) {
		if (playerQueue.contains(pInfo)) {
			return playerQueue.indexOf(pInfo);
		}
		playerQueue.add(pInfo);
		return playerQueue.size() - 1;
	}

	/**
	 * ????Player?????????
	 *
	 * @param pInfo
	 *            Player
	 */
	public void exitQueue(NetPlayerInfo pInfo) {
		playerQueue.remove(pInfo);
	}

	/**
	 * ???Player????????count??
	 *
	 * @return ??????Number of players
	 */
	public int getHowManyPlayersReady() {
		int count = 0;
		for (NetPlayerInfo pInfo : playerSeat) {
			if (pInfo != null) {
				if (pInfo.ready)
					count++;
			}
		}
		return count;
	}

	/**
	 * ???Player??????count??(??????????????????????)
	 *
	 * @return ?????Number of players
	 */
	public int getHowManyPlayersPlaying() {
		int count = 0;
		for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
			if (pInfo != null) {
				if (pInfo.playing && playerSeat.contains(pInfo))
					count++;
			}
		}
		return count;
	}

	/**
	 * ????????Player??????
	 *
	 * @return ????????Player???(??2???????????, ??????????????????null)
	 */
	public NetPlayerInfo getWinner() {
		if ((startPlayers >= 2) && (getHowManyPlayersPlaying() < 2)
				&& (playing == true)) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if (pInfo != null) {
					if (pInfo.playing && pInfo.connected
							&& playerSeat.contains(pInfo))
						return pInfo;
				}
			}
		}
		return null;
	}

	/**
	 * ????????Team name???
	 *
	 * @return ????????Team name
	 */
	public String getWinnerTeam() {
		if ((startPlayers >= 2) && (getHowManyPlayersPlaying() >= 2)
				&& (playing == true)) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && pInfo.playing && pInfo.connected
						&& playerSeat.contains(pInfo)) {
					if (pInfo.strTeam.length() <= 0) {
						return null;
					} else {
						return pInfo.strTeam;
					}
				}
			}
		}

		return null;
	}

	/**
	 * @return 1??????????????????true
	 */
	public boolean isTeamWin() {
		String teamname = null;

		if ((startPlayers >= 2) && (getHowManyPlayersPlaying() >= 2)
				&& (playing == true)) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && pInfo.playing && pInfo.connected
						&& playerSeat.contains(pInfo)) {
					if (pInfo.strTeam.length() <= 0) {
						return false;
					} else if (teamname == null) {
						teamname = pInfo.strTeam;
					} else if (!teamname.equals(pInfo.strTeam)) {
						return false;
					}
				}
			}
		}

		return (teamname != null);
	}

	/**
	 * @return true if it's a team game
	 */
	public boolean isTeamGame() {
		LinkedList<String> teamList = new LinkedList<String>();

		if (startPlayers >= 2) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && (pInfo.strTeam.length() > 0)) {
					if (teamList.contains(pInfo.strTeam)) {
						return true;
					} else {
						teamList.add(pInfo.strTeam);
					}
				}
			}
		}

		return false;
	}

	/**
	 * @return true if 2 or more people have same IP
	 */
	public boolean hasSameIPPlayers() {
		LinkedList<String> ipList = new LinkedList<String>();

		if (startPlayers >= 2) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && (pInfo.strRealIP.length() > 0)) {
					if (ipList.contains(pInfo.strRealIP)) {
						return true;
					} else {
						ipList.add(pInfo.strRealIP);
					}
				}
			}
		}

		return false;
	}

	/**
	 * Start game????????
	 */
	public void gameStart() {
		updatePlayerCount();
		playerSeatNowPlaying.clear();
		playerSeatNowPlaying.addAll(playerSeat);
		playerSeatDead.clear();
		chatList.clear();
		startPlayers = playerSeatedCount;
		deadCount = 0;
		autoStartActive = false;
		isSomeoneCancelled = false;
	}

	/**
	 * ?????????
	 */
	public void delete() {
		ruleOpt = null;
		mapList.clear();
		playerList.clear();
		playerSeat.clear();
		playerSeatNowPlaying.clear();
		playerQueue.clear();
		playerSeatDead.clear();
		chatList.clear();
	}
}

