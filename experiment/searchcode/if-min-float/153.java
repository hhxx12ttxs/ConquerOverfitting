package org.hontracker;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Random;

import org.hontracker.Global.State;
import org.hontracker.DbTypes.DbBoolean;
import org.hontracker.DbTypes.DbByteArray;
import org.hontracker.DbTypes.DbFloat;
import org.hontracker.DbTypes.DbInteger;
import org.hontracker.DbTypes.DbString;
import org.hontracker.DbTypes.DbTimestamp;
import org.hontracker.DbTypes.DbType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.lorecraft.phparser.SerializedPhpParser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

public class Player {
    static final private String TableName = "Players";
    static final private String server = "masterserver.hon.s2games.com";
    static final private String userAgent1 = "S2 Games/Heroes of Newerth/";
    static final private String userAgent2 = "/wac/i686";
    static private String gameVersion = "2.5.3.0"; // Fallback version

    private SQLiteDatabase db = null;

    public enum DBState {
        Unknown, DoesNotExist, Exists
    }

    private State state = State.Idle;
    private DBState dbState = DBState.Unknown;
    private boolean retrievedUpdate = false;
    private LinkedHashMap<String, Integer> changedProperties = new LinkedHashMap<String, Integer>();
    private boolean isRandomPlayer = false;
    private static final HashSet<String> randomPlayers = new HashSet<String>(
            Arrays.asList("z x y", "a c"));

    private LinkedHashMap<String, DbType<?>> properties = null;

    // Common properties - independent of game mode.
    public static final String Name = "Name";
    public static final String Id = "Id";
    public static final String ClanName = "ClanName";
    public static final String ClanTag = "ClanTag";
    public static final String Populated = "Populated";
    public static final String Timestamp = "Timestamp";
    public static final String ExpandedView = "ExpandedView";
    public static final String Starred = "Starred";

    public enum GameMode {
        MM, CM, Pub
    }

    // Properties per game mode.
    public static String Data = "Data";
    public static String Rating = "Rating";
    public static String Wins = "Wins";
    public static String Losses = "Losses";
    public static String Disconnects = "Disconnects";
    public static String Kills = "Kills";
    public static String Deaths = "Deaths";
    public static String Assists = "Assists";
    public static String TSR = "TSR";
    public static String Games = "Games";
    public static String APM = "APM";
    public static String XPPM = "XPPM";
    public static String GPM = "GPM";
    public static String WPM = "WPM";

    // Calculated values - not stored in the database
    public static String WinsPercentage = "WinsPercentage";
    public static String LossesPercentage = "LossesPercentage";
    public static String DisconnectsPercentage = "DisconnectsPercentage";
    public static String KDR = "KDR";
    public static String KADR = "KADR";

    private LinkedHashMap<GameMode, Float> currentWinsPercentage = new LinkedHashMap<Player.GameMode, Float>();
    private LinkedHashMap<GameMode, Float> currentLossesPercentage = new LinkedHashMap<Player.GameMode, Float>();
    private LinkedHashMap<GameMode, Float> currentDisconnectsPercentage = new LinkedHashMap<Player.GameMode, Float>();
    private LinkedHashMap<GameMode, Float> currentKDR = new LinkedHashMap<Player.GameMode, Float>();
    private LinkedHashMap<GameMode, Float> currentKADR = new LinkedHashMap<Player.GameMode, Float>();

    Player(String name, SQLiteDatabase db) {
        this.properties = getProperties(name);
        this.db = db;

        if (isRandomPlayer(name)) {
            isRandomPlayer = true;
        }

        setStateIdle();
    }

    static public boolean isRandomPlayer(String name) {
        return randomPlayers.contains(name);
    }

    static public class PlayerComparator implements Comparator<Player> {
        @Override
        public int compare(Player player1, Player player2) {
            return player1.getName().compareTo(player2.getName());
        }
    }

    static private Iterator<GameMode> gameModeIter() {
        return Arrays.asList(GameMode.MM, GameMode.CM, GameMode.Pub).iterator();
    }

    static public LinkedHashMap<String, DbType<?>> getProperties(String name) {
        LinkedHashMap<String, DbType<?>> properties = new LinkedHashMap<String, DbType<?>>();

        properties.put(Name, new DbString(Name, name, "PRIMARY KEY"));
        properties.put(Id, new DbInteger(Id));
        properties.put(ClanName, new DbString(ClanName));
        properties.put(ClanTag, new DbString(ClanTag));
        properties.put(Populated, new DbBoolean(Populated));
        properties.put(Timestamp, new DbTimestamp(Timestamp));
        properties.put(ExpandedView, new DbBoolean(ExpandedView));
        properties.put(Starred, new DbBoolean(Starred));

        // Matchmaking statistics
        Iterator<GameMode> gameModeIter = gameModeIter();
        while (gameModeIter.hasNext()) {
            String gameMode = gameModeIter.next().toString();
            properties.put(gameMode + "_" + Data, new DbByteArray(gameMode
                    + "_" + Data));
            properties.put(gameMode + "_" + Rating, new DbFloat(gameMode + "_"
                    + Rating));
            properties.put(gameMode + "_" + Wins, new DbInteger(gameMode + "_"
                    + Wins));
            properties.put(gameMode + "_" + Losses, new DbInteger(gameMode
                    + "_" + Losses));
            properties.put(gameMode + "_" + Disconnects, new DbInteger(gameMode
                    + "_" + Disconnects));
            properties.put(gameMode + "_" + Kills, new DbInteger(gameMode + "_"
                    + Kills));
            properties.put(gameMode + "_" + Deaths, new DbInteger(gameMode
                    + "_" + Deaths));
            properties.put(gameMode + "_" + Assists, new DbInteger(gameMode
                    + "_" + Assists));
            properties.put(gameMode + "_" + TSR, new DbFloat(gameMode + "_"
                    + TSR));
            properties.put(gameMode + "_" + Games, new DbInteger(gameMode + "_"
                    + Games));
            properties.put(gameMode + "_" + APM, new DbFloat(gameMode + "_"
                    + APM));
            properties.put(gameMode + "_" + XPPM, new DbFloat(gameMode + "_"
                    + XPPM));
            properties.put(gameMode + "_" + GPM, new DbFloat(gameMode + "_"
                    + GPM));
            properties.put(gameMode + "_" + WPM, new DbFloat(gameMode + "_"
                    + WPM));
        }

        return properties;
    }

    public static void dbOnCreate(SQLiteDatabase db) {
        Log.i(Global.TAG, "Player.dbOnCreate - Creating database.");
        StringBuilder query = new StringBuilder("CREATE TABLE " + TableName
                + " (");
        Iterator<DbType<?>> propertiesIterator = getProperties("").values()
                .iterator();
        while (propertiesIterator.hasNext()) {
            DbType<?> property = propertiesIterator.next();
            query.append(property.getColumnDefinition());
            if (propertiesIterator.hasNext()) {
                query.append(", ");
            }
        }
        query.append(")");
        db.execSQL(query.toString());
    }

    public static void dbOnUpdate(SQLiteDatabase db, int oldVersion,
            int newVersion) {
        Log.i(Global.TAG,
                "Player.dbOnUpdate - Updating database. oldVersion = '"
                        + new Integer(oldVersion).toString()
                        + "', newVersion = '"
                        + new Integer(newVersion).toString() + "'.");
        db.execSQL("DROP TABLE " + TableName);
        dbOnCreate(db);
    }

    public synchronized void dbAdd() {
        long result = db.insertOrThrow(TableName, null, generateValues());
        Log.d(Global.TAG, "Player.dbAdd - Adding '" + getName()
                + "' to database, result = '" + result + "'.");
        dbState = DBState.Exists;
    }

    public synchronized void dbRemove() {
        long result = db.delete(TableName, "Name = '" + getName() + "'", null);
        Log.d(Global.TAG, "Player.dbRemove - Removing '" + getName()
                + "' from database, result = '" + result + "'.");
        dbState = DBState.DoesNotExist;
    }

    public synchronized void dbUpdate() {
        boolean retryQuery = false;
        do {
            try {
                if (dbState == DBState.DoesNotExist) {
                    dbAdd();
                } else if (dbState == DBState.Exists) {
                    long result = db.update(TableName, generateValues(),
                            "Name = '" + getName() + "'", null);
                    Log.d(Global.TAG, "Player.dbUpdate - Updating '"
                            + getName() + "' in database, result = '" + result
                            + "'.");
                } else {
                    Log.e(Global.TAG,
                            "Player.dbUpdate - '" + getName()
                                    + "' Ignored request, dbState = '"
                                    + dbState.toString() + "'");
                }
                retryQuery = false;
            } catch (Exception e) {
                // Recreate table.
                dbOnUpdate(db, 0, db.getVersion());
                retryQuery = !retryQuery;
                Log.w(Global.TAG, "Player.loadData - Failed to load '"
                        + getName() + "' from database. " + e.toString());
            }
        } while (retryQuery == true);
    }

    public synchronized void loadData() {
        boolean recordExists = false;
        boolean retryQuery = false;
        do {
            try {
                Cursor cursor = db.query(TableName, null, "Name = '"
                        + getName() + "' ", null, null, null, "Name asc");
                if (cursor.moveToFirst()) {
                    Iterator<DbType<?>> propertiesIterator = properties
                            .values().iterator();
                    while (propertiesIterator.hasNext()) {
                        DbType<?> property = propertiesIterator.next();
                        property.retrieveValue(cursor);
                    }
                    recordExists = true;
                }
                cursor.close();
                retryQuery = false;
            } catch (Exception e) {
                // Recreate table
                dbOnUpdate(db, 0, db.getVersion());
                retryQuery = !retryQuery;
                Log.w(Global.TAG, "Player.loadData - Failed to load '"
                        + getName() + "' from database. " + e.toString());
            }
        } while (retryQuery == true);

        if (recordExists == true) {
            Iterator<GameMode> gameModeIter = gameModeIter();
            while (gameModeIter.hasNext()) {
                calculateStats(gameModeIter.next(), false);
            }
            dbState = DBState.Exists;
            Log.v(Global.TAG, "Player.loadData - '" + getName()
                    + "' loaded from database, populated = "
                    + getPopulated().toString() + ".");
        } else {
            dbState = DBState.DoesNotExist;
            Log.v(Global.TAG, "Player.loadData - '" + getName()
                    + "' not found.");
        }
    }

    private ContentValues generateValues() {
        ContentValues values = new ContentValues();
        Iterator<DbType<?>> propertiesIterator = properties.values().iterator();
        while (propertiesIterator.hasNext()) {
            propertiesIterator.next().fillContentValue(values);
        }

        return values;
    }

    public synchronized boolean retrieveData() throws Exception {
        boolean handled = false;
        if (dbState != DBState.Unknown) {
            Log.i(Global.TAG, "Player.retrieveData - Fetching '" + getName()
                    + "'.");
            try {
                setStateInProgress();
                changedProperties.clear();

                parseMatchMaking();
                parseCasualMode();
                parsePublicGames();
                parseClan();

                Log.i(Global.TAG,
                        "Player.retrieveData - Data retrieval succesful for '"
                                + getName() + "'.");

                // Record the time when this player was updated.
                setTimestamp(new Long(new Date().getTime()).toString());
                // Mark this player as being updated
                setPopulated(true);
                // Update database
                dbUpdate();
                Log.i(Global.TAG,
                        "Player.retrieveData - Database updated for '"
                                + getName() + "'.");

                if (!retrievedUpdate) {
                    retrievedUpdate = true;
                }
                handled = true;
            } catch (Exception e) {
                setStateIncomplete();
                throw e;
            }
        } else {
            Log.e(Global.TAG, "Player.retrieveData - '" + getName()
                    + "' Ignored request, dbState = '" + dbState.toString()
                    + "'");
        }
        setStateIdle();
        return handled;
    }

    private synchronized void parseBasics(
            LinkedHashMap<Object, Object> playerData) {
        setId(getInteger(playerData, "account_id"));
        setClanName(getString(playerData, "name"));
        setClanTag(""); // Need to clear the clanTag as it will be reloaded.
    }

    @SuppressWarnings("unchecked")
    private synchronized void parseMatchMaking() throws Exception {
        Log.v(Global.TAG,
                "Player.parseMatchMaking - Fetching Match Making stats for '"
                        + getName() + "'.");

        GameMode gameMode = GameMode.MM;
        if (isRandomPlayer == true) {
            Random randomGenerator = new Random();

            byte[] compressed = Utils.compress("");
            setData(gameMode, compressed);

            setRating(gameMode, randomGenerator.nextFloat() * 2000);
            setWins(gameMode, randomGenerator.nextInt(300));
            setLosses(gameMode, randomGenerator.nextInt(300));
            setDisconnects(gameMode, randomGenerator.nextInt(15));
            setKills(gameMode, randomGenerator.nextInt(1000));
            setDeaths(gameMode, randomGenerator.nextInt(1000));
            setAssists(gameMode, randomGenerator.nextInt(3000));
            setTSR(gameMode, randomGenerator.nextFloat() * 10);
            setGames(gameMode, randomGenerator.nextInt(1000));
            setAPM(gameMode, randomGenerator.nextFloat() * 200);
            setXPPM(gameMode, randomGenerator.nextFloat() * 500);
            setGPM(gameMode, randomGenerator.nextFloat() * 700);
            setWPM(gameMode, randomGenerator.nextFloat() * 5);
        } else {
            LinkedHashMap<Object, Object> result = null;
            int retryCount = Global.RETRY_COUNT;
            while (retryCount > 0) {
                try {
                    String url = "http://" + server + "/client_requester.php";
                    Document document = Jsoup
                            .connect(url)
                            .data("f", "show_stats", "nickname", getName(),
                                    "table", "ranked")
                            .userAgent(getUserAgent()).post();
                    retryCount = 0; // Fetch successful

                    String data = document.body().text();
                    byte[] compressed = Utils.compress(data);
                    setData(gameMode, compressed);

                    SerializedPhpParser serializedPhpParser = new SerializedPhpParser(
                            data);
                    result = (LinkedHashMap<Object, Object>) serializedPhpParser
                            .parse();

                    parseBasics(result);
                    setRating(gameMode, getFloat(result, "rnk_amm_team_rating"));
                    setWins(gameMode, getInteger(result, "rnk_wins"));
                    setLosses(gameMode, getInteger(result, "rnk_losses"));
                    setDisconnects(gameMode, getInteger(result, "rnk_discos"));
                    setKills(gameMode, getInteger(result, "rnk_herokills"));
                    setDeaths(gameMode, getInteger(result, "rnk_deaths"));
                    setAssists(gameMode, getInteger(result, "rnk_heroassists"));
                    setTSR(gameMode, calculateMMTSR(result));
                    setGames(gameMode, getInteger(result, "rnk_games_played"));
                    setAPM(gameMode, getFloat(result, "avgActions_min"));
                    setXPPM(gameMode, getFloat(result, "avgXP_min"));
                    setGPM(gameMode, validateFloat(getFloat(result, "rnk_gold")
                            * 60f / getFloat(result, "rnk_time_earning_exp")));
                    setWPM(gameMode, getFloat(result, "avgWardsUsed"));

                    break;
                } catch (Exception e) {
                    if (--retryCount <= 0) {
                        Log.e(Global.TAG,
                                "Player.parseMatchMaking - Failed to retrieve Match Making stats for '"
                                        + getName() + "'. " + e.toString());
                        throw e;
                    }
                    Log.w(Global.TAG,
                            "Player.parseMatchMaking - Retrying Match Making stats for '"
                                    + getName() + "'. " + e.toString());
                }
            }
        }
        calculateStats(gameMode, true);
    }

    @SuppressWarnings("unchecked")
    private synchronized void parseCasualMode() throws Exception {
        Log.v(Global.TAG,
                "Player.parseCasualMode - Fetching Casual Mode stats for '"
                        + getName() + "'.");

        GameMode gameMode = GameMode.CM;
        if (isRandomPlayer == true) {
            Random randomGenerator = new Random();

            byte[] compressed = Utils.compress("");
            setData(gameMode, compressed);

            setRating(gameMode, randomGenerator.nextFloat() * 2000);
            setWins(gameMode, randomGenerator.nextInt(300));
            setLosses(gameMode, randomGenerator.nextInt(300));
            setDisconnects(gameMode, randomGenerator.nextInt(15));
            setKills(gameMode, randomGenerator.nextInt(1000));
            setDeaths(gameMode, randomGenerator.nextInt(1000));
            setAssists(gameMode, randomGenerator.nextInt(3000));
            setTSR(gameMode, randomGenerator.nextFloat() * 10);
            setGames(gameMode, randomGenerator.nextInt(1000));
            setAPM(gameMode, randomGenerator.nextFloat() * 200);
            setXPPM(gameMode, randomGenerator.nextFloat() * 500);
            setGPM(gameMode, randomGenerator.nextFloat() * 700);
            setWPM(gameMode, randomGenerator.nextFloat() * 5);
        } else {
            LinkedHashMap<Object, Object> result = null;
            int retryCount = Global.RETRY_COUNT;
            while (retryCount > 0) {
                try {
                    String url = "http://" + server + "/client_requester.php";
                    Document document = Jsoup
                            .connect(url)
                            .data("f", "show_stats", "nickname", getName(),
                                    "table", "casual")
                            .userAgent(getUserAgent()).post();
                    retryCount = 0; // Fetch successful

                    String data = document.body().text();
                    byte[] compressed = Utils.compress(data);
                    setData(gameMode, compressed);

                    SerializedPhpParser serializedPhpParser = new SerializedPhpParser(
                            data);
                    result = (LinkedHashMap<Object, Object>) serializedPhpParser
                            .parse();

                    parseBasics(result);
                    setRating(gameMode, getFloat(result, "cs_amm_team_rating"));
                    setWins(gameMode, getInteger(result, "cs_wins"));
                    setLosses(gameMode, getInteger(result, "cs_losses"));
                    setDisconnects(gameMode, getInteger(result, "cs_discos"));
                    setKills(gameMode, getInteger(result, "cs_herokills"));
                    setDeaths(gameMode, getInteger(result, "cs_deaths"));
                    setAssists(gameMode, getInteger(result, "cs_heroassists"));
                    setTSR(gameMode, calculateCMTSR(result));
                    setGames(gameMode, getInteger(result, "cs_games_played"));
                    setAPM(gameMode, getFloat(result, "avgActions_min"));
                    setXPPM(gameMode, getFloat(result, "avgXP_min"));
                    setGPM(gameMode, validateFloat(getFloat(result, "cs_gold")
                            * 60f / getFloat(result, "cs_time_earning_exp")));
                    setWPM(gameMode, getFloat(result, "avgWardsUsed"));

                    break;
                } catch (Exception e) {
                    if (--retryCount <= 0) {
                        Log.e(Global.TAG,
                                "Player.parseCasualMode - Failed to retrieve Casual Mode stats for '"
                                        + getName() + "'. " + e.toString());
                        throw e;
                    }
                    Log.w(Global.TAG,
                            "Player.parseCasualMode - Retrying Casual Mode stats for '"
                                    + getName() + "'. " + e.toString());
                }
            }
        }
        calculateStats(gameMode, true);
    }

    @SuppressWarnings("unchecked")
    private synchronized void parsePublicGames() throws Exception {
        Log.v(Global.TAG,
                "Player.parsePublicGames - Fetching Public Games stats for '"
                        + getName() + "'.");

        GameMode gameMode = GameMode.Pub;
        if (isRandomPlayer == true) {
            Random randomGenerator = new Random();

            byte[] compressed = Utils.compress("");
            setData(gameMode, compressed);

            setRating(gameMode, randomGenerator.nextFloat() * 2000);
            setWins(gameMode, randomGenerator.nextInt(300));
            setLosses(gameMode, randomGenerator.nextInt(300));
            setDisconnects(gameMode, randomGenerator.nextInt(15));
            setKills(gameMode, randomGenerator.nextInt(1000));
            setDeaths(gameMode, randomGenerator.nextInt(1000));
            setAssists(gameMode, randomGenerator.nextInt(3000));
            setTSR(gameMode, randomGenerator.nextFloat() * 10);
            setGames(gameMode, randomGenerator.nextInt(1000));
            setAPM(gameMode, randomGenerator.nextFloat() * 200);
            setXPPM(gameMode, randomGenerator.nextFloat() * 500);
            setGPM(gameMode, randomGenerator.nextFloat() * 700);
            setWPM(gameMode, randomGenerator.nextFloat() * 5);
        } else {
            LinkedHashMap<Object, Object> result = null;
            int retryCount = Global.RETRY_COUNT;
            while (retryCount > 0) {
                try {
                    String url = "http://" + server + "/client_requester.php";
                    Document document = Jsoup
                            .connect(url)
                            .data("f", "show_stats", "nickname", getName(),
                                    "table", "player")
                            .userAgent(getUserAgent()).post();
                    retryCount = 0; // Fetch successful

                    String data = document.body().text();
                    byte[] compressed = Utils.compress(data);
                    setData(gameMode, compressed);

                    SerializedPhpParser serializedPhpParser = new SerializedPhpParser(
                            data);
                    result = (LinkedHashMap<Object, Object>) serializedPhpParser
                            .parse();

                    parseBasics(result);
                    setRating(gameMode, getFloat(result, "acc_pub_skill"));
                    setWins(gameMode, getInteger(result, "acc_wins"));
                    setLosses(gameMode, getInteger(result, "acc_losses"));
                    setDisconnects(gameMode, getInteger(result, "acc_discos"));
                    setKills(gameMode, getInteger(result, "acc_herokills"));
                    setDeaths(gameMode, getInteger(result, "acc_deaths"));
                    setAssists(gameMode, getInteger(result, "acc_heroassists"));
                    setTSR(gameMode, calculatePubTSR(result));
                    setGames(gameMode, getInteger(result, "acc_games_played"));
                    setAPM(gameMode, getFloat(result, "avgActions_min"));
                    setXPPM(gameMode, getFloat(result, "avgXP_min"));
                    setGPM(gameMode, validateFloat(getFloat(result, "acc_gold")
                            * 60f / getFloat(result, "acc_time_earning_exp")));
                    setWPM(gameMode, getFloat(result, "avgWardsUsed"));

                    break;
                } catch (Exception e) {
                    if (--retryCount <= 0) {
                        Log.e(Global.TAG,
                                "Player.parsePublicGames - Failed to retrieve Public Game stats for '"
                                        + getName() + "'. " + e.toString());
                        throw e;
                    }
                    Log.w(Global.TAG,
                            "Player.parsePublicGames - Retrying Public Game stats for '"
                                    + getName() + "'. " + e.toString());
                }
            }
        }
        calculateStats(gameMode, true);
    }

    private synchronized void parseClan() throws Exception {
        String actualClanName = getClanName();
        if (actualClanName.length() > 0) {
            String searchClanName = new String(actualClanName);
            int retryCount = Global.RETRY_COUNT;
            while (retryCount > 0) {
                try {
                    Log.v(Global.TAG,
                            "Player.parseClan - Fetching Clan information for '"
                                    + searchClanName + "'.");
                    String urlClanName = URLEncoder.encode(searchClanName,
                            "UTF-8");
                    String url = "http://clans.heroesofnewerth.com/ajaxClanGrid.php?perPage=&type=name&q="
                            + urlClanName;
                    Document document = Jsoup.connect(url).post();

                    String clanTag = "";
                    Element clans = document.body().child(1);
                    Iterator<Element> iterator = clans.children().iterator();
                    // Find the matching clan name and grab it's tag.
                    while (iterator.hasNext()) {
                        Element clanText = iterator.next();
                        String name = clanText.child(0).text();
                        if (name.compareToIgnoreCase(actualClanName) == 0) {
                            clanTag = clanText.child(1).ownText();
                            break;
                        }
                    }
                    if (clanTag.length() != 0) {
                        setClanTag(clanTag);
                        retryCount = 0; // Fetch successful
                    } else {
                        Log.v(Global.TAG,
                                "Player.parseClan - Clan information not found.");
                        // Retry by searching name minus the last word in a
                        // multi-word clan name. This could still fail if the
                        // clan is not on the first results page.
                        int spaceIndex = searchClanName.lastIndexOf(' ');
                        if (spaceIndex != -1) {
                            searchClanName = searchClanName.substring(0,
                                    spaceIndex);
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    if (--retryCount <= 0) {
                        Log.e(Global.TAG,
                                "Player.parseClan - Failed to retrieve Clan information for '"
                                        + getName() + "'. " + e.toString());
                        throw e;
                    }
                    Log.w(Global.TAG,
                            "Player.parseClan - Retrying Clan information for '"
                                    + getName() + "'. " + e.toString());
                }
            }
        }
    }

    private void calculateStats(GameMode gameMode, boolean recordChanges) {
        Integer wins = getWins(gameMode);
        Integer losses = getLosses(gameMode);
        Integer disconnects = getDisconnects(gameMode);
        Integer totalMatches = getWins(gameMode) + getLosses(gameMode)
                + getDisconnects(gameMode);

        Float winsPercentage = totalMatches != 0 ? (100f * wins / totalMatches) + 0.5f
                : 0f;
        int winsPercentageComparison = 0;
        if (currentWinsPercentage.containsKey(gameMode)) {
            winsPercentageComparison = currentWinsPercentage.get(gameMode)
                    .compareTo(winsPercentage);
        }
        currentWinsPercentage.put(gameMode, winsPercentage);

        Float lossesPercentage = totalMatches != 0 ? (100f * losses / totalMatches) + 0.5f
                : 0f;
        int lossesPercentageComparison = 0;
        if (currentLossesPercentage.containsKey(gameMode)) {
            lossesPercentageComparison = currentLossesPercentage.get(gameMode)
                    .compareTo(lossesPercentage);
        }
        currentLossesPercentage.put(gameMode, lossesPercentage);

        Float disconnectsPercentage = totalMatches != 0 ? (100f * disconnects / totalMatches) + 0.5f
                : 0f;
        int disconnectsPercentageComparison = 0;
        if (currentDisconnectsPercentage.containsKey(gameMode)) {
            disconnectsPercentageComparison = currentDisconnectsPercentage.get(
                    gameMode).compareTo(disconnectsPercentage);
        }
        currentDisconnectsPercentage.put(gameMode, disconnectsPercentage);

        Integer kills = getKills(gameMode);
        Integer deaths = getDeaths(gameMode);
        Integer assists = getAssists(gameMode);

        Float kdr = deaths != 0 ? kills.floatValue() / deaths.floatValue() : 0;
        int kdrComparison = 0;
        if (currentKDR.containsKey(gameMode)) {
            kdrComparison = currentKDR.get(gameMode).compareTo(kdr);
        }
        currentKDR.put(gameMode, kdr);

        Float kadr = deaths != 0 ? (kills.floatValue() + assists.floatValue())
                / deaths.floatValue() : 0;
        int kadrComparison = 0;
        if (currentKADR.containsKey(gameMode)) {
            kadrComparison = currentKADR.get(gameMode).compareTo(kadr);
        }
        currentKADR.put(gameMode, kadr);

        if (recordChanges) {
            changedProperties.put(gameMode + "_" + WinsPercentage,
                    winsPercentageComparison);
            changedProperties.put(gameMode + "_" + LossesPercentage,
                    lossesPercentageComparison);
            changedProperties.put(gameMode + "_" + DisconnectsPercentage,
                    disconnectsPercentageComparison);
            changedProperties.put(gameMode + "_" + KDR, kdrComparison);
            changedProperties.put(gameMode + "_" + KADR, kadrComparison);
        }
    }

    private Float calculateMMTSR(LinkedHashMap<Object, Object> data) {
        Float tsr = new Float(0);
        if (data != null) {
            Float param5 = getFloat(data, "rnk_games_played");
            Float param6 = getFloat(data, "rnk_wins");
            Float param7 = getFloat(data, "rnk_losses");
            Float param20 = getFloat(data, "rnk_herokills");
            Float param24 = getFloat(data, "rnk_heroassists");
            Float param25 = getFloat(data, "rnk_deaths");
            Float param42 = getFloat(data, "rnk_gold");
            Float param66 = getFloat(data, "percentEM");
            Float param68 = getFloat(data, "avgGameLength");
            Float param69 = getFloat(data, "avgXP_min");
            Float param70 = getFloat(data, "avgDenies");
            Float param71 = getFloat(data, "avgCreepKills");
            Float param74 = getFloat(data, "avgWardsUsed");

            tsr = calculateRankedTSR(param5, param6, param7, param20, param24,
                    param25, param42, param66, param68, param69, param70,
                    param71, param74);
        }
        return tsr;
    }

    private Float calculateCMTSR(LinkedHashMap<Object, Object> data) {
        Float tsr = new Float(0);
        if (data != null) {
            Float param5 = getFloat(data, "cs_games_played");
            Float param6 = getFloat(data, "cs_wins");
            Float param7 = getFloat(data, "cs_losses");
            Float param20 = getFloat(data, "cs_herokills");
            Float param24 = getFloat(data, "cs_heroassists");
            Float param25 = getFloat(data, "cs_deaths");
            Float param42 = getFloat(data, "cs_gold");
            Float param66 = getFloat(data, "percentEM");
            Float param68 = getFloat(data, "avgGameLength");
            Float param69 = getFloat(data, "avgXP_min");
            Float param70 = getFloat(data, "avgDenies");
            Float param71 = getFloat(data, "avgCreepKills");
            Float param74 = getFloat(data, "avgWardsUsed");

            tsr = calculateRankedTSR(param5, param6, param7, param20, param24,
                    param25, param42, param66, param68, param69, param70,
                    param71, param74);
        }
        return tsr;
    }

    private Float calculatePubTSR(LinkedHashMap<Object, Object> data) {
        Float tsr = new Float(0);
        if (data != null) {
            Float param5 = getFloat(data, "acc_games_played");
            Float param6 = getFloat(data, "acc_wins");
            Float param7 = getFloat(data, "acc_losses");
            Float param20 = getFloat(data, "acc_herokills");
            Float param24 = getFloat(data, "acc_heroassists");
            Float param25 = getFloat(data, "acc_deaths");
            Float param42 = getFloat(data, "acc_gold");
            Float param66 = getFloat(data, "percentEM");
            Float param68 = getFloat(data, "avgGameLength");
            Float param69 = getFloat(data, "avgXP_min");
            Float param70 = getFloat(data, "avgDenies");
            Float param71 = getFloat(data, "avgCreepKills");
            Float param74 = getFloat(data, "avgWardsUsed");

            tsr = calculatePubTSR(param5, param6, param7, param20, param24,
                    param25, param42, param66, param68, param69, param70,
                    param71, param74);
        }
        return tsr;
    }

    // TSR http://forums.heroesofnewerth.com/showthread.php?t=126454
    // Using algorithm from version 4.3 (2011-12-13) using .honmod code.
    private Float calculateRankedTSR(Float param5, Float param6, Float param7,
            Float param20, Float param24, Float param25, Float param42,
            Float param66, Float param68, Float param69, Float param70,
            Float param71, Float param74) {
        if (param5 > 0) {
            // From ranked_stats.package
            return Max(
                    Min(((param20 / param25 / 1.15f) * 0.65f)
                            + ((param24 / param25 / 1.55f) * 1.20f)
                            + (((param6 / (param6 + param7)) / 0.55f) * 0.9f)
                            + (((param42 / (param5 * param68 / 60f) / 230f) * (1f - ((230f / 195f) * (param66 / 100f)))) * 0.35f)
                            + (((param69 / 380f) * (1f - ((380f / 565f) * (param66 / 100f)))) * 0.40f)
                            + (((((Min(param70, 30f) / 12f) * (1f - ((4.5f / 8.5f) * (param66 / 100f)))) * 0.70f)
                                    + (((Min(param71, 200f) / 93f) * (1f - ((63f / 81f) * (param66 / 100f)))) * 0.50f) + (Min(
                                    param74, 5.0f) / 1.45f * 0.30f)) * (37.5f / (param68 / 60f))),
                            10f), 0f);
        }
        return 0f;
    }

    private Float calculatePubTSR(Float param5, Float param6, Float param7,
            Float param20, Float param24, Float param25, Float param42,
            Float param66, Float param68, Float param69, Float param70,
            Float param71, Float param74) {
        if (param5 > 0) {
            // From halisa_stats.package
            return Max(
                    Min(((param20 / param25 / 1.1f) * 0.65f)
                            + ((param24 / param25 / 1.5f) * 1.20f)
                            + (((param6 / (param6 + param7)) / 0.55f) * 0.9f)
                            + (((param42 / (param5 * param68 / 60f) / 190f) * (1f - ((190f / 195f) * (param66 / 100f)))) * 0.35f)
                            + (((param69 / 420f) * (1f - ((420f / 565f) * (param66 / 100f)))) * 0.40f)
                            + (((((Min(param70, 30f) / 12f) * (1f - ((4.5f / 8.5f) * (param66 / 100f)))) * 0.70f)
                                    + (((Min(param71, 200f) / 93f) * (1f - ((63f / 81f) * (param66 / 100f)))) * 0.50f) + (Min(
                                    param74, 3.5f) / 0.55f * 0.30f)) * (38.5f / (param68 / 60f))),
                            10f), 0f);
        }
        return 0f;
    }

    private Float Max(Float val1, Float val2) {
        Float validVal1 = validateFloat(val1);
        Float validVal2 = validateFloat(val2);
        if (Float.compare(validVal1, validVal2) >= 0) {
            return validVal1;
        }
        return validVal2;
    }

    private Float Min(Float val1, Float val2) {
        Float validVal1 = validateFloat(val1);
        Float validVal2 = validateFloat(val2);
        if (Float.compare(validVal1, validVal2) < 0) {
            return validVal1;
        }
        return validVal2;
    }

    static public Integer getTsrColour(Context context, Float tsr,
            Integer numberOfGames) {
        Integer colour = Color.WHITE;
        if (numberOfGames < 50) {
            colour = context.getResources().getColor(R.color.newbie);
        } else if (tsr < 4.0) {
            colour = context.getResources().getColor(R.color.skill_low);
        } else if (tsr < 6.0) {
            colour = context.getResources().getColor(R.color.skill_med);
        } else {
            colour = context.getResources().getColor(R.color.skill_high);
        }
        return colour;
    }

    static private String getString(LinkedHashMap<Object, Object> map,
            String key) {
        String result = new String();
        if (map != null) {
            Object value = map.get(key);
            if (value == null) {
                result = new String("");
            } else if (value instanceof String) {
                result = (String) value;
            } else {
                result = value.toString();
                if (result == null) {
                    result = new String("");
                }
            }
        }
        return result;
    }

    static private Integer getInteger(LinkedHashMap<Object, Object> map,
            String key) {
        Integer result = new Integer(0);
        if (map != null) {
            NumberFormat format = NumberFormat.getInstance(Locale.US);
            String value = getString(map, key);
            if (value != null && value.length() > 0) {
                try {
                    result = format.parse(value).intValue();
                } catch (Exception e) {
                    Log.e(Global.TAG, "Player.getInteger - '" + key + "' - "
                            + e.toString());
                    // Return 0 on any failure.
                    result = 0;
                }
            }
        }
        return result;
    }

    static private Float getFloat(LinkedHashMap<Object, Object> map, String key) {
        Float result = 0f;
        if (map != null) {
            String value = getString(map, key);
            if (value != null && value.length() > 0) {
                try {
                    result = validateFloat(Float.valueOf(value).floatValue());
                } catch (Exception e) {
                    Log.e(Global.TAG,
                            "Player.getFloat - '" + key + "' - " + e.toString());
                    // Return 0 on any failure.
                    result = 0f;
                }
            }
        }
        return result;
    }

    static private Float validateFloat(Float value) {
        Float result = value;
        if (value.isInfinite() == true || value.isNaN() == true) {
            result = 0f;
        }
        return result;
    }

    public boolean isIdle() {
        boolean result = false;
        if (getState() == State.Idle || getState() == State.Incomplete) {
            result = true;
        }
        return result;
    }

    public State getState() {
        return state;
    }

    public boolean retrievedUpdate() {
        return retrievedUpdate;
    }

    public void setStateQueued() {
        if (state != State.InProgress) {
            state = State.Queued;
        }
    }

    private void setStateIdle() {
        state = State.Idle;
    }

    private void setStateInProgress() {
        state = State.InProgress;
    }

    private void setStateIncomplete() {
        state = State.Incomplete;
    }

    public DBState getDBState() {
        return dbState;
    }
    
    public static String getServer() {
        return server;
    }

    public static String getUserAgent() {
        return userAgent1 + gameVersion + userAgent2;
    }
    
    public static void setGameVersion(String gameVersion) {
        Player.gameVersion = gameVersion;
    }

    private void setProperty(String name, Boolean value) {
        changedProperties
                .put(name,
                        new Integer(((DbBoolean) properties.get(name))
                                .setValue(value)));
    }

    private void setProperty(String name, byte[] value) {
        changedProperties.put(
                name,
                new Integer(((DbByteArray) properties.get(name))
                        .setValue(value)));
    }

    private void setProperty(String name, Float value) {
        changedProperties.put(name, new Integer(
                ((DbFloat) properties.get(name)).setValue(value)));
    }

    private void setProperty(String name, Integer value) {
        changedProperties
                .put(name,
                        new Integer(((DbInteger) properties.get(name))
                                .setValue(value)));
    }

    private void setProperty(String name, String value) {
        changedProperties.put(name,
                new Integer(((DbString) properties.get(name)).setValue(value)));
    }

    private void setPropertyTimestamp(String name, String value) {
        changedProperties.put(
                name,
                new Integer(((DbTimestamp) properties.get(name))
                        .setValue(value)));
    }

    public Integer isChanged(GameMode gameMode, String name) {
        Integer comparison = changedProperties.get(gameMode + "_" + name);
        if (comparison == null) {
            comparison = 0;
        }
        return comparison;
    }

    @SuppressWarnings("unused")
    private void setName(String value) {
        setProperty(Name, value);
    }

    public String getName() {
        return (String) properties.get(Name).getValue();
    }

    private void setId(Integer value) {
        setProperty(Id, value);
    }

    public Integer getId() {
        return (Integer) properties.get(Id).getValue();
    }

    private void setClanName(String value) {
        setProperty(ClanName, value);
    }

    public String getClanName() {
        return (String) properties.get(ClanName).getValue();
    }

    private void setClanTag(String value) {
        setProperty(ClanTag, value);
    }

    public String getClanTag() {
        return (String) properties.get(ClanTag).getValue();
    }

    public String getFullName() {
        String fullName = getName();
        String clanTag = getClanTag();
        if (clanTag.length() > 0) {
            fullName = "[" + clanTag + "] " + fullName;
        }
        return fullName;
    }

    public boolean togglePopulated() {
        setPopulated(!getPopulated());
        return getPopulated();
    }

    private void setPopulated(Boolean value) {
        setProperty(Populated, value);
    }

    public Boolean getPopulated() {
        // All database values need to be loaded before we return the stored
        // Populated value. Otherwise we could have read the populated value but
        // not the rest and return it prematurely.
        if (dbState == DBState.Exists) {
            return (Boolean) properties.get(Populated).getValue();
        }
        return new Boolean(false);
    }

    private void setTimestamp(String value) {
        setPropertyTimestamp(Timestamp, value);
    }

    public String getTimestamp() {
        return (String) properties.get(Timestamp).getValue();
    }

    public void setExpandedView(boolean value) {
        setProperty(ExpandedView, value);
    }

    public Boolean getExpandedView() {
        return (Boolean) properties.get(ExpandedView).getValue();
    }

    public void setStarred(boolean value) {
        setProperty(Starred, value);
        dbUpdate();
    }

    public Boolean getStarred() {
        return (Boolean) properties.get(Starred).getValue();
    }

    private void setData(GameMode gameMode, byte[] value) {
        setProperty(gameMode + "_" + Data, value);
    }

    public byte[] getData(GameMode gameMode) {
        return (byte[]) properties.get(gameMode + "_" + Data).getValue();
    }

    private void setRating(GameMode gameMode, Float value) {
        setProperty(gameMode + "_" + Rating, value);
    }

    public Float getRating(GameMode gameMode) {
        return (Float) properties.get(gameMode + "_" + Rating).getValue();
    }

    private void setWins(GameMode gameMode, Integer value) {
        setProperty(gameMode + "_" + Wins, value);
    }

    public Integer getWins(GameMode gameMode) {
        return (Integer) properties.get(gameMode + "_" + Wins).getValue();
    }

    private void setLosses(GameMode gameMode, Integer value) {
        setProperty(gameMode + "_" + Losses, value);
    }

    public Integer getLosses(GameMode gameMode) {
        return (Integer) properties.get(gameMode + "_" + Losses).getValue();
    }

    private void setDisconnects(GameMode gameMode, Integer value) {
        setProperty(gameMode + "_" + Disconnects, value);
    }

    public Integer getDisconnects(GameMode gameMode) {
        return (Integer) properties.get(gameMode + "_" + Disconnects)
                .getValue();
    }

    private void setKills(GameMode gameMode, Integer value) {
        setProperty(gameMode + "_" + Kills, value);
    }

    public Integer getKills(GameMode gameMode) {
        return (Integer) properties.get(gameMode + "_" + Kills).getValue();
    }

    private void setDeaths(GameMode gameMode, Integer value) {
        setProperty(gameMode + "_" + Deaths, value);
    }

    public Integer getDeaths(GameMode gameMode) {
        return (Integer) properties.get(gameMode + "_" + Deaths).getValue();
    }

    private void setAssists(GameMode gameMode, Integer value) {
        setProperty(gameMode + "_" + Assists, value);
    }

    public Integer getAssists(GameMode gameMode) {
        return (Integer) properties.get(gameMode + "_" + Assists).getValue();
    }

    private void setTSR(GameMode gameMode, Float value) {
        setProperty(gameMode + "_" + TSR, value);
    }

    public Float getTSR(GameMode gameMode) {
        return (Float) properties.get(gameMode + "_" + TSR).getValue();
    }

    private void setGames(GameMode gameMode, Integer value) {
        setProperty(gameMode + "_" + Games, value);
    }

    public Integer getGames(GameMode gameMode) {
        return (Integer) properties.get(gameMode + "_" + Games).getValue();
    }

    private void setAPM(GameMode gameMode, Float value) {
        setProperty(gameMode + "_" + APM, value);
    }

    public Float getAPM(GameMode gameMode) {
        return (Float) properties.get(gameMode + "_" + APM).getValue();
    }

    private void setXPPM(GameMode gameMode, Float value) {
        setProperty(gameMode + "_" + XPPM, value);
    }

    public Float getXPPM(GameMode gameMode) {
        return (Float) properties.get(gameMode + "_" + XPPM).getValue();
    }

    private void setGPM(GameMode gameMode, Float value) {
        setProperty(gameMode + "_" + GPM, value);
    }

    public Float getGPM(GameMode gameMode) {
        return (Float) properties.get(gameMode + "_" + GPM).getValue();
    }

    private void setWPM(GameMode gameMode, Float value) {
        setProperty(gameMode + "_" + WPM, value);
    }

    public Float getWPM(GameMode gameMode) {
        return (Float) properties.get(gameMode + "_" + WPM).getValue();
    }

    public Float getWinsPercentage(GameMode gameMode) {
        Float rc = 0f;
        if (currentWinsPercentage.containsKey(gameMode)) {
            rc = currentWinsPercentage.get(gameMode);
        }
        return rc;
    }

    public Float getLossesPercentage(GameMode gameMode) {
        Float rc = 0f;
        if (currentLossesPercentage.containsKey(gameMode)) {
            rc = currentLossesPercentage.get(gameMode);
        }
        return rc;
    }

    public Float getDisconnectsPercentage(GameMode gameMode) {
        Float rc = 0f;
        if (currentDisconnectsPercentage.containsKey(gameMode)) {
            rc = currentDisconnectsPercentage.get(gameMode);
        }
        return rc;
    }

    public Float getKDR(GameMode gameMode) {
        Float rc = 0f;
        if (currentKDR.containsKey(gameMode)) {
            rc = currentKDR.get(gameMode);
        }
        return rc;
    }

    public Float getKADR(GameMode gameMode) {
        Float rc = 0f;
        if (currentKADR.containsKey(gameMode)) {
            rc = currentKADR.get(gameMode);
        }
        return rc;
    }
}
