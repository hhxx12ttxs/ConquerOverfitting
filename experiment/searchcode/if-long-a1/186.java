package com.n9works.bukkit.pvp;

import com.n9works.bukkit.Account;
import com.n9works.bukkit.TheArtifact;
import com.n9works.bukkit.bungee.models.BattlegroundStatus;
import com.n9works.bukkit.bungee.models.Match;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Logger;

import static com.n9works.bukkit.utils.TimeUtil.secondsSince;
import static java.util.Arrays.asList;
import static org.bukkit.ChatColor.*;

public class Matchmaking implements Runnable {
    private final Logger log = Logger.getLogger("Artifact-Arena");
    private final TheArtifact plugin;

    public boolean arenasAvailable = true;

    private static final int minBGPlayers = 4;
    private static final int PvPMatchupTimer = 15;

    public final Map<String, BukkitTask> inviteTasks = new HashMap<>();

    private final List<String> imbalancedWarnedPlayers = new ArrayList<>();
    private final List<String> ineligibleSkyvalePlayersWarned = new ArrayList<>();

    private final Queue queue;

    public Matchmaking(final TheArtifact plugin, Queue queue) {
        this.plugin = plugin;
        this.queue = queue;

        plugin.scheduler.scheduleSyncRepeatingTask(plugin, this, 100, 20 * PvPMatchupTimer);
    }

    @Override
    public void run() {
        //For each available arena, setup the matches
        if (!plugin.bungee.pvpOnline) {
            log.info("[Matchmaking] PvP Server offline, not checking for matches.");
            return;
        }

        get2v2ArenaMatchup();
        getArenaMatchup();
        getChallengeMatchup();
        getBGMatchup(plugin.vota);
        getBGMatchup(plugin.overload);
        getSkyvaleMatchup(plugin.skyvale);
        getSpectatorMatchup();
    }

    void getArenaMatchup() {
        if (queue.queued1v1Players.size() > 1) {
            if (!arenasAvailable) return;

            PvPPlayer p1 = null;
            PvPPlayer p2 = null;

            //Determine closest matchup
            Map<String, Map<String, Long>> closestOpponents = new HashMap<>();

            //Get closest matchup for each player
            for (String player1 : queue.queued1v1Players) {
                int minDifference = Integer.MAX_VALUE;
                for (String player2 : queue.queued1v1Players) {
                    if (!player1.equals(player2)) {
                        PvPPlayer a1 = plugin.getArenaAccount(player1);
                        PvPPlayer a2 = plugin.getArenaAccount(player2);
                        int p1Rating = a1.arenarating;
                        int p2Rating = a2.arenarating;
                        final int diff = Math.abs(p2Rating - p1Rating);

                        updateMatchupTries(a1, a2);

                        int matchupTries = 0;
                        if (a1.recentOpponents.containsKey(a2.name)) {
                            matchupTries = a1.recentOpponents.get(a2.name);
                        }

                        log.info(a1.name + " vs " + a2.name + " offset = " + matchupTries + ".");

                        if (matchupTries <= 0) {
                            if (diff < minDifference) {
                                minDifference = diff;
                                Long combinedTimeInQueue = ((System.currentTimeMillis() - a1.timeInQueue) +
                                        (System.currentTimeMillis() - a2.timeInQueue));
                                Map<String, Long> opponentMap = new HashMap<>();
                                opponentMap.put(player2, combinedTimeInQueue);
                                if (!closestOpponents.containsKey(player2)) {
                                    closestOpponents.put(player1, opponentMap);
                                }
                            }
                        }
                    }
                }
            }

            if (closestOpponents.size() > 0) {
                //Determine match pair with longest time in queue
                Long minTime = Long.MIN_VALUE;
                for (Map.Entry<String, Map<String, Long>> entry : closestOpponents.entrySet()) {
                    String p = entry.getKey();
                    String opponent = entry.getValue().entrySet().iterator().next().getKey();
                    Long queueTime = entry.getValue().entrySet().iterator().next().getValue();

                    if ((opponent != null) && (queueTime > minTime)) {
                        p1 = plugin.getArenaAccount(p);
                        p2 = plugin.getArenaAccount(opponent);
                        minTime = queueTime;
                    }
                }

                p1.hasArena = true;
                p2.hasArena = true;

                queue.queued1v1Players.remove(p1.name);
                queue.queued1v1Players.remove(p2.name);

                Match match = new Match("1v1");
                match.p1 = p1.name;
                match.p2 = p2.name;

                plugin.bungee.sendMatch(match);
                notifyMatchReady(p1.name);
                notifyMatchReady(p2.name);
            }
        }
    }

    public void getChallengeMatchup() {
        if (queue.challengePlayers.size() > 1) {
            if (!arenasAvailable) return;

            PvPPlayer p1 = null;
            PvPPlayer p2 = null;

            //Get closest matchup for each player
            for (String player1 : queue.challengePlayers) {
                for (String player2 : queue.challengePlayers) {
                    PvPPlayer a1 = plugin.getArenaAccount(player1);
                    PvPPlayer a2 = plugin.getArenaAccount(player2);
                    if (!player1.equals(player2) && a1.opponent != null && a2.opponent != null) {
                        if (a1.opponent.equals(a2.name) && a2.opponent.equals(a1.name)) {
                            p1 = a1;
                            p2 = a2;
                            break;
                        }
                    }
                }
            }
            if (p1 != null) {
                p1.hasArena = true;
                p2.hasArena = true;

                queue.challengePlayers.remove(p1.name);
                queue.challengePlayers.remove(p2.name);

                Match match = new Match("challenge");
                match.p1 = p1.name;
                match.p2 = p2.name;

                plugin.bungee.sendMatch(match);
                notifyMatchReady(p1.name);
                notifyMatchReady(p2.name);
            }
        }

        //Expire old Challenges
        List<String> expiredChallengers = new ArrayList<>();
        for (String p : queue.challengePlayers) {
            PvPPlayer challenger = plugin.getArenaAccount(p);
            if (secondsSince(challenger.challengeTime) > 120) {
                expiredChallengers.add(p);
            }
        }

        for (String p : expiredChallengers) {
            queue.dropQueue(p);
            queue.dropQueue(plugin.getArenaAccount(p).opponent);
        }
    }

    void notifyMatchReady(String player) {
        Player p = plugin.server.getPlayerExact(player);

        //They're not moved yet so ensure they're not stuck in transit somehow.
        if (plugin.bungee.playersInTransit.contains(player)) {
            plugin.bungee.playersInTransit.remove(player);
        }

        try {
            new FancyMessage("Your arena battle is ready! ")
                    .color(AQUA)
                    .then(" [Click here to join]")
                    .color(GOLD)
                    .style(UNDERLINE)
                    .style(BOLD)
                    .tooltip("Click to join!")
                    .command("/join")
                    .send(p);
        } catch (Exception e) {
            log.info("[FancyText] Broke");
            p.sendMessage(AQUA + "Your arena battle is ready!" + AQUA + "Please type " + YELLOW + "/join" +
                    AQUA + "to join!");
        }

        p.sendMessage(GRAY + "You have 45 seconds to join or you will automatically forfeit the match.");

//        p.sendMessage(ChatColor.AQUA + "Your arena battle is ready! Type " +
//                ChatColor.GOLD + "/join" + ChatColor.AQUA + " to join. You have 45" +
//                " seconds to join or you will automatically forfeit the match.");
        p.playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 1);

        startInviteTimer(player);
    }

    void startInviteTimer(final String player) {
        //Start join timeout
        BukkitTask inviteTask = plugin.scheduler.runTaskLater(plugin,
                () -> {
                    Player p = plugin.server.getPlayerExact(player);
                    if (p != null && p.isOnline()) {
                        p.sendMessage(ChatColor.AQUA + "You failed to join your match.");
                        inviteTasks.remove(p.getName());
                        plugin.getArenaAccount(player).hasArena = false;
                        p.playSound(p.getLocation(), Sound.BLAZE_DEATH, 1, 1);
                    }
                }, 20 * 45
        );

        inviteTasks.put(player, inviteTask);
    }

    void get2v2ArenaMatchup() {
        if (plugin.queue.queued2v2Players.size() >= 4) {
            if (!arenasAvailable) return;

            List<ArenaParty> arenaTeams = plugin.queue.getQueuedArenaTeams();

            ArenaParty t1;
            ArenaParty t2;

            log.info("[ARENA] --- 2v2 Queued Teams ---");
            int count = 1;
            for (ArenaParty arenaTeam : arenaTeams) {
                log.info("[ARENA] " + count + " " + arenaTeam.player1 + " & " + arenaTeam.player2
                        + " [" + arenaTeam.arenarating + "]");
                count++;
            }

            if (arenaTeams.size() < 2) {
                //We need to create two teams
                arenaTeams = build2v2Teams(arenaTeams, plugin.queue.queued2v2Players);
            }

            t1 = arenaTeams.get(0);
            t2 = arenaTeams.get(1);

            //Determine closest matchup
            //Get closest matchup for each player
//            for (ArenaParty team1 : arenaTeams) {
//                int minDifference = Integer.MAX_VALUE;
//                for (ArenaParty team2 : arenaTeams) {
//                    if (team1 != team2) {
//                        team1.setRating();
//                        team2.setRating();
//                        final int diff = Math.abs(team1.arenarating - team2.arenarating);
//
//                        update2v2MatchupTries(team1, team2);
//
//                        if (get2v2MatchupTries(team1, team2) == 0) {
//                            if (diff < minDifference) {
//                                minDifference = diff;
//                                Long combinedTimeInQueue = ((System.currentTimeMillis() - team1.timeInQueue) +
//                                        (System.currentTimeMillis() - team2.timeInQueue));
//                                Map<ArenaParty, Long> opponentMap = new HashMap<>();
//                                opponentMap.put(team2, combinedTimeInQueue);
//                                if (!closestOpponents.containsKey(team2)) {
//                                    closestOpponents.put(team1, opponentMap);
//                                }
//                            }
//                        }
//                    }
//                }
//            }

            if (t1 == null || t2 == null) return;

            //Remove players queued for 2s and assign them to the arena player slots
            PvPPlayer p1 = plugin.getArenaAccount(t1.player1);
            PvPPlayer p2 = plugin.getArenaAccount(t1.player2);
            PvPPlayer p3 = plugin.getArenaAccount(t2.player1);
            PvPPlayer p4 = plugin.getArenaAccount(t2.player2);

            p1.hasArena = true;
            p2.hasArena = true;
            p3.hasArena = true;
            p4.hasArena = true;

            plugin.queue.queued2v2Players.remove(p1.name);
            plugin.queue.queued2v2Players.remove(p2.name);
            plugin.queue.queued2v2Players.remove(p3.name);
            plugin.queue.queued2v2Players.remove(p4.name);

            Match match = new Match("2v2");
            match.p1 = p1.name;
            match.p2 = p2.name;
            match.p3 = p3.name;
            match.p4 = p4.name;

            plugin.bungee.sendMatch(match);
            notifyMatchReady(p1.name);
            notifyMatchReady(p2.name);
            notifyMatchReady(p3.name);
            notifyMatchReady(p4.name);
        }
    }

    private List<ArenaParty> build2v2Teams(List<ArenaParty> existingTeams, List<String> queued2v2Players) {

        //We need to remove players in existing teams to the list of the queued 2v2 players. Only drawing from
        //un-teamed queuers.
        for (ArenaParty party : existingTeams) {
            queued2v2Players.remove(party.player1);
            queued2v2Players.remove(party.player2);
        }

        for (String player : queued2v2Players) {
            for (String player2 : queued2v2Players) {
                if (player.equals(player2)) continue;

                ArenaParty party = new ArenaParty(plugin, player, player2);
                existingTeams.add(party);
            }
        }

        ArenaParty team1 = null;
        ArenaParty team2 = null;

        //Get closest team matchup.
        for (ArenaParty party1 : existingTeams) {
            int minDifference = Integer.MAX_VALUE;
            for (ArenaParty party2 : existingTeams) {
                //Make sure parties aren't the same and that we don't have a player on both teams, cause, that was fun.
                if (!party1.equals(party2) && !party2.contains(party1.player1) && !party2.contains(party1.player2)) {
                    final int diff = Math.abs(party1.arenarating - party2.arenarating);
//                    log.info(party1 + " vs " + party2 + " diff: " + diff);

                    if (diff < minDifference) {
                        minDifference = diff;
                        team1 = party1;
                        team2 = party2;
                    }
                }
            }
        }
        if (team1 == null) return null;

        log.info("Pairing is: " + team1 + " vs " + team2);
        return new ArrayList<>(asList(team1, team2));
    }


    void getSpectatorMatchup() {
        if (plugin.arenaManager.matches.size() == 0) {
            return;
        }

        messageQueuedSpectators(AQUA + "--Current Watchable Matches--");

        for (Match m : plugin.arenaManager.matches) {
            messageQueuedSpectators(m.getMatchMessage(plugin));
        }

        for (String spec : plugin.queue.spectators) {
            PvPPlayer p = plugin.getArenaAccount(spec);
            p.spectator = true;
            p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.WITHER_SPAWN, 1, 1);
        }

        plugin.queue.spectators.clear();
    }

    void messageQueuedSpectators(String msg) {
        for (String spec : plugin.queue.spectators) {
            plugin.server.getPlayerExact(spec).sendMessage(msg);
        }
    }

    void messageQueuedSpectators(FancyMessage msg) {
        for (String spec : plugin.queue.spectators) {
            msg.send(plugin.server.getPlayerExact(spec));
        }
    }

    void sendBGInvites(List<String> players, String bg) {
        Match match = new Match(bg);
        match.bgplayers.addAll(players);

        //Handle spectators
        if (bg.equalsIgnoreCase("Valley of the Artifacts")) {
            match.spectators.addAll(queue.queuedVotASpectators);
            players.addAll(queue.queuedVotASpectators);
            queue.queuedVotASpectators.clear();
        } else if (bg.equalsIgnoreCase("Vale of Conquest")) {
            match.spectators = queue.queuedSkyvaleSpectators;
            players.addAll(queue.queuedSkyvaleSpectators);
            queue.queuedSkyvaleSpectators.clear();
        }

        plugin.bungee.sendMatch(match);

        for (String o : players) {
            Player player = plugin.server.getPlayerExact(o);
            if (player != null) {
                PvPPlayer p = plugin.getArenaAccount(player.getName());

                try {
                    new FancyMessage(bg)
                            .color(YELLOW)
                            .then(" is ready!")
                            .color(AQUA)
                            .then(" [Click here to join]")
                            .color(GOLD)
                            .style(UNDERLINE)
                            .style(BOLD)
                            .tooltip("Click to join!")
                            .command("/join")
                            .send(player);
                } catch (Exception e) {
                    log.info("[FancyText] Broke");
                    p.sendMessage(YELLOW + bg + AQUA + " is ready!" + AQUA + "Please type " + YELLOW + "/join" +
                            AQUA + "to join!");
                }

                p.sendMessage(GRAY + "You have 45 seconds to join or you will automatically forfeit the match.");


                player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 1);
                switch (bg) {
                    case "VotA":
                        player.setScoreboard(queue.queueScoreboard.board_ready_vota);
                        p.hasVotA = true;
                        break;
                    case "Overload":
                        player.setScoreboard(queue.queueScoreboard.board_ready_overload);
                        p.hasOverload = true;
                        break;
                    default:
                        player.setScoreboard(queue.queueScoreboard.board_ready_skyvale);
                        p.hasSkyvale = true;
                        break;
                }
                log.info(p.bgRating + ": " + o);
                startInviteTimer(o);

                imbalancedWarnedPlayers.remove(p.name);
                ineligibleSkyvalePlayersWarned.remove(p.name);
            }
        }
    }

    PvPPlayer getLastQueued(List<String> players) {
        //Odd number of players. Last one doesn't get to go.
        return plugin.getArenaAccount(players.get(players.size() - 1));
    }

    void getBGMatchup(BattlegroundStatus battleground) {
        List<String> queuedPlayers;
        String bgName;
        if (battleground.game.equals("overload")) {
            queuedPlayers = queue.queuedOverloadPlayers;
            bgName = "Overload";
        } else {
            queuedPlayers = queue.queuedVotAPlayers;
            bgName = "Valley of the Artifacts";
        }

        if (queuedPlayers.size() >= 1) {
            if (!battleground.active && queuedPlayers.size() >= minBGPlayers) {
                PvPPlayer p = getLastQueued(queuedPlayers);
                Boolean trim = false;
                if (queuedPlayers.size() % 2 != 0) {
                    queuedPlayers.remove(p.name);
                    trim = true;

                    if (!imbalancedWarnedPlayers.contains(p.name)) {
                        p.sendMessage(ChatColor.AQUA + "Due to balance restrictions, you cannot " +
                                "join " + bgName + " at this time. You are now at the front of the queue and will " +
                                "join when more players queue.");
                        imbalancedWarnedPlayers.add(p.name);
                    }
                }

                //Let's sort our BG queue based on arena ratings. This might help balance out matches.
                Collections.sort(queuedPlayers, (p1, p2) -> Integer.valueOf(plugin.getArenaAccount(p1).bgRating).compareTo(plugin.getArenaAccount(p2).bgRating));

                sendBGInvites(queuedPlayers, bgName);

                try {
                    //Server-wide announcement of the game
                    FancyMessage gameAnnouncement = new FancyMessage("A game of ")
                            .color(AQUA)
                            .then(bgName)
                            .then(GREEN)
                            .then(" has begun! ")
                            .color(AQUA)
                            .then("[Click to Join!]")
                            .style(BOLD)
                            .color(GOLD);
                    if (bgName.equalsIgnoreCase("Overload")) {
                        gameAnnouncement.command("/bg2 queue")
                                .tooltip("Click to queue for Overload.");
                    } else {
                        gameAnnouncement.command("/bg queue")
                                .tooltip("Click to queue for VotA.");
                    }
                    plugin.globalFancyMessage(gameAnnouncement, queuedPlayers);
                } catch (Exception e) {
                    log.info("[FancyText] Broke");
                    p.sendMessage(AQUA + "A game of " + YELLOW + bgName + AQUA + " has begun!");
                }

                queuedPlayers.clear();
                if (trim) {
                    queuedPlayers.add(p.name);
                }
                log.info("[" + battleground + "] " + bgName + " is ready.");

                battleground.active = true;

            } else if (battleground.active) {
                if (isBGImbalanced(battleground)) {
                    //Teams aren't balanced, requires an odd number of joins.
                    Boolean trim = false;
                    PvPPlayer p = getLastQueued(queuedPlayers);
                    if (queuedPlayers.size() % 2 == 0) {
                        //If even, trim the last player so it's even in the BG.
                        queuedPlayers.remove(p.name);
                        trim = true;

                        if (!imbalancedWarnedPlayers.contains(p.name)) {
                            p.sendMessage(ChatColor.AQUA + "Due to balance restrictions, you cannot " +
                                    "join " + bgName + " at this time. You are now at the front of the queue and will " +
                                    "join when more players queue.");
                            imbalancedWarnedPlayers.add(p.name);
                        }
                    }

                    //Let's sort our BG queue based on arena ratings. This might help balance out matches.
                    Collections.sort(queuedPlayers, (p1, p2) -> Integer.valueOf(plugin.getArenaAccount(p1).bgRating).compareTo(plugin.getArenaAccount(p2).bgRating));

                    //Need to put the highest rating people first so they
                    //get put in the lower-rated 'team'
                    Collections.reverse(queuedPlayers);

                    sendBGInvites(queuedPlayers, bgName);
                    queuedPlayers.clear();
                    //If there was a trim, add that player back to the queue, otherwise
                    //there was only 1 player in the queue, so they made it in.
                    if (trim) {
                        queuedPlayers.add(p.name);
                    }
                } else {
                    //Teams are balanced, need an even number of joins.

                    PvPPlayer p = getLastQueued(queuedPlayers);
                    Boolean trim = false;
                    if (queuedPlayers.size() % 2 != 0) {
                        //If even, trim the last player so it's even in the BG.
                        queuedPlayers.remove(p.name);
                        trim = true;
                        if (!imbalancedWarnedPlayers.contains(p.name)) {
                            p.sendMessage(ChatColor.AQUA + "Due to balance restrictions, you cannot " +
                                    "join " + bgName + " at this time. You are now at the front of the queue and will " +
                                    "join when more players queue.");
                            imbalancedWarnedPlayers.add(p.name);
                        }
                    }

                    //Let's sort our BG queue based on arena ratings. This might help balance out matches.
                    Collections.sort(queuedPlayers, (p1, p2) -> Integer.valueOf(plugin.getArenaAccount(p1).bgRating).compareTo(plugin.getArenaAccount(p2).bgRating));

                    //Need to put the highest rating people first so they
                    //get put in the lower-rated 'team'
                    Collections.reverse(queuedPlayers);

                    sendBGInvites(queuedPlayers, bgName);
                    queuedPlayers.clear();
                    if (trim) {
                        queuedPlayers.add(p.name);
                    }
                }
            }
        }

        if (battleground.active && queue.queuedVotASpectators.size() > 0) {
            sendBGInvites(new ArrayList<>(), bgName);
        }
    }

    void getSkyvaleMatchup(BattlegroundStatus battleground) {
        if (queue.queuedSkyvalePlayers.size() >= 1) {
            if (!battleground.active && enoughSkyvalePlayers()) {

                List<String> validPlayers = getValidSkyvalePlayers();

                sendBGInvites(validPlayers, "Vale of Conquest");

                try {
                    //Server-wide announcement of the game
                    FancyMessage gameAnnouncement = new FancyMessage("A game of ")
                            .color(AQUA)
                            .then("Vale of Conquest")
                            .then(GREEN)
                            .then(" has begun! ")
                            .color(AQUA)
                            .then("[Click to Join!]")
                            .style(BOLD)
                            .color(GOLD)
                            .command("/bg3 queue")
                            .tooltip("Click to queue for Vale of Conquest.");
                    plugin.globalFancyMessage(gameAnnouncement, queue.queuedSkyvalePlayers);
                } catch (Exception e) {
                    log.info("[FancyText] Broke");
                }

                queue.queuedSkyvalePlayers.removeAll(validPlayers);
                log.info("[Vale of Conquest] is ready.");

                battleground.active = true;
            } else if (battleground.active) {
                List<String> validPlayers = getValidSkyvalePlayers();

                sendBGInvites(validPlayers, "Vale of Conquest");
                queue.queuedSkyvalePlayers.removeAll(validPlayers);
            }
        } else if (queue.queuedSkyvaleSpectators.size() > 0) {
            sendBGInvites(queue.queuedSkyvaleSpectators, "Vale of Conquest");
        }
    }

    private List<String> getValidSkyvalePlayers() {
        Map<String, Integer> invalidPlayers = new HashMap<>();
        List<String> validPlayers = new ArrayList<>();

        //Get players queued by themselves, and thus not eligible to play
        for (String p : queue.queuedSkyvalePlayers) {
            if (meetsTownQueueRequirement(p)) {
                if (!plugin.skyvale.active) {
                    validPlayers.add(p);
                } else {
                    if (meetsTownBalanceRequirement(p)) {
                        validPlayers.add(p);
                    } else {
                        invalidPlayers.put(p, 1);
                    }
                }
            } else {
                invalidPlayers.put(p, 0);
            }
        }

        //Inform ineligible players they cannot join.
        for (String p : invalidPlayers.keySet()) {
            if (ineligibleSkyvalePlayersWarned.contains(p)) continue;

            if (invalidPlayers.get(p) == 0) {
                //0 is because of minimum town requirements
                plugin.server.getPlayerExact(p).sendMessage(AQUA
                        + "A minimum of two players are required to queue from a town in order to participate in Vale of Conquest.");
            } else {
                //1 is because of maximum town size cap requirements
                plugin.server.getPlayerExact(p).sendMessage(AQUA
                        + "You are unable to join Vale of Conquest due to Town Balance restrictions at this time. You will be notified if you can join.");
            }

            ineligibleSkyvalePlayersWarned.add(p);
        }

        return validPlayers;
    }

    private boolean enoughSkyvalePlayers() {
        return queue.getQueuedTowns() >= 3;
    }

    private boolean meetsTownQueueRequirement(String player) {
        Account a = plugin.getAccount(player);
        if (a.town == null) return false;

        //Check queued players
        for (String p : queue.queuedSkyvalePlayers) {
            Account pAccount = plugin.getAccount(p);
            if (pAccount.town == null) continue;

            if (!p.equals(player) && pAccount.town.equals(a.town)) return true;
        }

        //Otherwise, check players in the active game.
        for (String town : plugin.skyvale.players.keySet()) {
            for (String p : plugin.skyvale.players.get(town)) {
                Account pAccount = plugin.getAccount(p);
                if (pAccount.town == null) continue;

                if (!p.equals(player) && pAccount.town.equals(a.town)) return true;
            }
        }

        return false;
    }

    private boolean meetsTownBalanceRequirement(String player) {
        String town = plugin.getAccount(player).town;
        if (town == null) return false;
        if (plugin.skyvale.players.isEmpty()
                || !plugin.skyvale.players.containsKey(town)) return true;

        //return Dellsmite(4) <= smallest team (2) + 1;
        return plugin.skyvale.players.get(town).size() <= getSmallestTeam() + 1;
    }

    private Integer getSmallestTeam() {
        int teamSize = Integer.MAX_VALUE;

        for (String town : plugin.skyvale.players.keySet()) {
            int townSize = plugin.skyvale.players.get(town).size();
            if (townSize < teamSize) teamSize = townSize;
        }

        //Smallest possible team size has to be 2, can't be 1, that'd be unfair.
        if (teamSize == 1) teamSize++;

        log.info("[Queue] Smallest skyvale team is " + teamSize);
        return teamSize;
    }

    Boolean isBGImbalanced(BattlegroundStatus bg) {
        return bg.redSize != bg.blueSize;
    }

    void updateMatchupTries(PvPPlayer p1, PvPPlayer p2) {
        if (p1.recentOpponents.containsKey(p2.name)) {
            p1.recentOpponents.put(p2.name, p1.recentOpponents.get(p2.name) - 1);
        } else {
            //For every 100 rating difference, add a 'try' so vastly differently skilled players can't play each other often
            int offset = Math.abs(p2.arenarating - p1.arenarating);
            log.info(p1.name + " offset vs " + p2.name + " is " + (offset / 100));
            p1.recentOpponents.put(p2.name, offset / 100);
        }
    }

    public void processMatchPlayed(ArenaMatch match) {
        PvPPlayer winner = plugin.getArenaAccount(match.winner);
        PvPPlayer loser = plugin.getArenaAccount(match.loser);

        winner.updateRecentOpponents(match.loser);
        loser.updateRecentOpponents(match.winner);
    }
}

