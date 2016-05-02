package wisematches.playground.tourney.regular.impl;

import wisematches.personality.Personality;
import wisematches.playground.*;
import wisematches.playground.tourney.regular.TourneyGroup;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Entity
@Table(name = "tourney_regular_group")
public class HibernateTourneyGroup implements TourneyGroup {
	@Column(name = "id")
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long internalId;

	@Column(name = "groupNumber")
	private int group;

	@OneToOne
	@JoinColumn(name = "roundId")
	private HibernateTourneyRound round;

	@Column(name = "started")
	@Temporal(TemporalType.TIMESTAMP)
	private Date startedDate;

	@Column(name = "finished")
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishedDate;

	@Column(name = "player1")
	private long player1;

	@Column(name = "player2")
	private long player2;

	@Column(name = "player3")
	private long player3;

	@Column(name = "player4")
	private long player4;

	@Column(name = "scores1")
	private short scores1;

	@Column(name = "scores2")
	private short scores2;

	@Column(name = "scores3")
	private short scores3;

	@Column(name = "scores4")
	private short scores4;

	@Column(name = "game1")
	private long game1;

	@Column(name = "game2")
	private long game2;

	@Column(name = "game3")
	private long game3;

	@Column(name = "game4")
	private long game4;

	@Column(name = "game5")
	private long game5;

	@Column(name = "game6")
	private long game6;

	@Column(name = "playersCount")
	private byte playersCount;

	@Column(name = "totalGamesCount")
	private byte totalGamesCount;

	@Column(name = "finishedGamesCount")
	private byte finishedGamesCount;

	@Deprecated
	private HibernateTourneyGroup() {
	}

	public HibernateTourneyGroup(int group, HibernateTourneyRound round, long[] players) {
		this.group = group;
		this.round = round;
		playersCount = (byte) players.length;
		if (playersCount < 2) {
			throw new IllegalArgumentException("Less that two players in group can't be");
		}
		if (playersCount > 4) {
			throw new IllegalArgumentException("More that four players in group can't be");
		}

		player1 = players[0];
		player2 = players[1];
		if (playersCount > 2) {
			player3 = players[2];
		}
		if (playersCount > 3) {
			player4 = players[3];
		}
	}

	long getInternalId() {
		return internalId;
	}

	@Override
	public State getState() {
		return State.getState(startedDate, finishedDate);
	}

	@Override
	public int getGroup() {
		return group;
	}

	@Override
	public HibernateTourneyRound getRound() {
		return round;
	}

	@Override
	public long[] getGames() {
		return Arrays.copyOf(new long[]{game1, game2, game3, game4, game5, game6}, totalGamesCount);
	}

	@Override
	public long[] getPlayers() {
		return Arrays.copyOf(new long[]{player1, player2, player3, player4}, playersCount);
	}

	@Override
	public short[] getScores() {
		return Arrays.copyOf(new short[]{scores1, scores2, scores3, scores4}, playersCount);
	}

	@Override
	public short getScores(long player) {
		return getScores()[getPlayerIndex(player)];
	}

	@Override
	public long getGameId(long p1, long p2) {
		return getGames()[getGameIndex(getPlayerIndex(p1), getPlayerIndex(p2))];
	}

	@Override
	public boolean isWinner(long player) {
		return finishedDate != null && getScores(player) == getMaxScore();
	}

	@Override
	public Id getId() {
		return new Id(round.getId(), group);
	}

	int getPlayerIndex(long player) {
		if (player == 0) {
			throw new IllegalArgumentException("Incorrect player id");
		}
		if (player == player1) {
			return 0;
		} else if (player == player2) {
			return 1;
		} else if (player == player3) {
			return 2;
		} else if (player == player4) {
			return 3;
		}
		throw new IllegalArgumentException("Incorrect player id");
	}

	int getGameIndex(int playerIndex1, int playerIndex2) {
		final int i1 = Math.min(playerIndex1, playerIndex2);
		final int i2 = Math.max(playerIndex1, playerIndex2);
		return (i1 == 0 ? i2 - 1 : i1 + i2);
	}

	@Override
	public Date getStartedDate() {
		return startedDate;
	}

	@Override
	public Date getFinishedDate() {
		return finishedDate;
	}


	<S extends GameSettings> int initializeGames(BoardManager<S, ?> boardManager, GameSettingsProvider<S, TourneyGroup> settingsProvider) throws BoardCreationException {
		if (totalGamesCount != 0) {
			throw new IllegalStateException("Group already initialized");
		}

		final S gameSettings = settingsProvider.createGameSettings(this);
		if (playersCount == 2) {
			game1 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player1), Personality.person(player2))).getBoardId();
			totalGamesCount = 1;
		} else if (playersCount == 3) {
			game1 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player1), Personality.person(player2))).getBoardId();
			game2 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player1), Personality.person(player3))).getBoardId();
			game3 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player2), Personality.person(player3))).getBoardId();
			totalGamesCount = 3;
		} else if (playersCount == 4) {
			game1 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player1), Personality.person(player2))).getBoardId();
			game2 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player1), Personality.person(player3))).getBoardId();
			game3 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player1), Personality.person(player4))).getBoardId();
			game4 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player2), Personality.person(player3))).getBoardId();
			game5 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player2), Personality.person(player4))).getBoardId();
			game6 = boardManager.createBoard(gameSettings, Arrays.asList(Personality.person(player3), Personality.person(player4))).getBoardId();
			totalGamesCount = 6;
		}
		startedDate = new Date();
		return totalGamesCount;
	}

	void finalizeGame(GameBoard<?, ?> board) {
		if (finishedDate != null) {
			throw new IllegalStateException("Group already finished");
		}

		final Collection<? extends GamePlayerHand> wonPlayers = board.getWonPlayers();
		if (wonPlayers == null) {
			throw new IllegalStateException("Game is not finished: " + board.getBoardId());
		}

		if (wonPlayers.size() == 0) {
			final List<? extends GamePlayerHand> playersHands = board.getPlayersHands();
			for (GamePlayerHand hand : playersHands) {
				addScores(hand.getPlayerId(), (short) 1);
			}
		} else {
			for (GamePlayerHand hand : wonPlayers) {
				addScores(hand.getPlayerId(), (short) 2);
			}
		}
		finishedGamesCount += 1;

		if (finishedGamesCount == totalGamesCount) {
			finishedDate = new Date();
		}
	}

	private void addScores(long player, short scores) {
		final int playerIndex = getPlayerIndex(player);
		if (playerIndex == 0) {
			scores1 += scores;
		} else if (playerIndex == 1) {
			scores2 += scores;
		} else if (playerIndex == 2) {
			scores3 += scores;
		} else if (playerIndex == 3) {
			scores4 += scores;
		}
	}

	private short getMaxScore() {
		short max = 0;
		if (scores1 > max) {
			max = scores1;
		}
		if (scores2 > max) {
			max = scores2;
		}
		if (scores3 > max) {
			max = scores3;
		}
		if (scores4 > max) {
			max = scores4;
		}
		return max;
	}

	@Override
	public String toString() {
		return "HibernateTourneyGroup{" +
				"internalId=" + internalId +
				", group=" + group +
				", round=" + round +
				", playersCount=" + playersCount +
				", player1=" + player1 +
				", player2=" + player2 +
				", player3=" + player3 +
				", player4=" + player4 +
				", scores1=" + scores1 +
				", scores2=" + scores2 +
				", scores3=" + scores3 +
				", scores4=" + scores4 +
				", game1=" + game1 +
				", game2=" + game2 +
				", game3=" + game3 +
				", game4=" + game4 +
				", game5=" + game5 +
				", game6=" + game6 +
				", startedDate=" + startedDate +
				", finishedDate=" + finishedDate +
				'}';
	}
}

