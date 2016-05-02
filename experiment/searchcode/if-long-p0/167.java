package wisematches.playground.tourney.regular.impl;

import wisematches.core.Personality;
import wisematches.core.PersonalityManager;
import wisematches.playground.*;
import wisematches.playground.tourney.TourneyGameResolution;
import wisematches.playground.tourney.regular.TourneyGroup;
import wisematches.playground.tourney.regular.TourneyRelationship;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

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
	private long player0;

	@Column(name = "player2")
	private long player1;

	@Column(name = "player3")
	private long player2;

	@Column(name = "player4")
	private long player3;

	@Column(name = "game1")
	private long game0;

	@Column(name = "game2")
	private long game1;

	@Column(name = "game3")
	private long game2;

	@Column(name = "game4")
	private long game3;

	@Column(name = "game5")
	private long game4;

	@Column(name = "game6")
	private long game5;


	@Column(name = "result1")
	@Enumerated(EnumType.ORDINAL)
	private TourneyGameResolution result0;

	@Column(name = "result2")
	@Enumerated(EnumType.ORDINAL)
	private TourneyGameResolution result1;

	@Column(name = "result3")
	@Enumerated(EnumType.ORDINAL)
	private TourneyGameResolution result2;

	@Column(name = "result4")
	@Enumerated(EnumType.ORDINAL)
	private TourneyGameResolution result3;

	@Column(name = "result5")
	@Enumerated(EnumType.ORDINAL)
	private TourneyGameResolution result4;

	@Column(name = "result6")
	@Enumerated(EnumType.ORDINAL)
	private TourneyGameResolution result5;


	@Column(name = "playersCount")
	private byte playersCount;

	@Column(name = "totalGamesCount")
	private byte totalGamesCount;

	@Column(name = "finishedGamesCount")
	private byte finishedGamesCount;

	HibernateTourneyGroup() {
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

		player0 = players[0];
		player1 = players[1];
		if (playersCount > 2) {
			player2 = players[2];
		}
		if (playersCount > 3) {
			player3 = players[3];
		}
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
	public long[] getPlayers() {
		return Arrays.copyOf(new long[]{player0, player1, player2, player3}, playersCount);
	}

	@Override
	public int getPlayersCount() {
		return playersCount;
	}

	@Override
	public int getTotalGamesCount() {
		return totalGamesCount;
	}

	@Override
	public int getFinishedGamesCount() {
		return finishedGamesCount;
	}

	@Override
	public int getPlayerScores(long player) {
		int res = 0;
		final long[] players = getPlayers();
		for (long p : players) {
			final TourneyGameResolution success = getPlayerSuccess(player, p);
			if (success != null) {
				res += success.getPoints();
			}
		}
		return res;
	}

	@Override
	public long getGameId(long p1, long p2) {
		return getGameByIndex(getGameIndex(getPlayerIndex(p1), getPlayerIndex(p2)));
	}

	@Override
	public boolean isFinished() {
		return finishedDate != null;
	}

	@Override
	public TourneyGameResolution getPlayerSuccess(long p1, long p2) {
		if (p1 == p2) {
			return null;
		}
		final int playerIndex1 = getPlayerIndex(p1);
		final int playerIndex2 = getPlayerIndex(p2);

		final int gameIndex = getGameIndex(playerIndex1, playerIndex2);

		TourneyGameResolution res = getResultByIndex(gameIndex);
		if (res == null) {
			return null;
		}
		return playerIndex1 < playerIndex2 ? res : res.getOpposite(); // less index - master
	}

	@Override
	public Id getId() {
		return new Id(round.getId(), group);
	}

	@Override
	public Date getStartedDate() {
		return startedDate;
	}

	@Override
	public Date getFinishedDate() {
		return finishedDate;
	}

	<S extends GameSettings> int initializeGames(GamePlayManager<S, ?> gamePlayManager, GameSettingsProvider<S, TourneyGroup> settingsProvider, PersonalityManager personalityManager) throws BoardCreationException {
		if (totalGamesCount != 0) {
			throw new IllegalStateException("Group already initialized");
		}

		final S settings = settingsProvider.createGameSettings(this);
		final TourneyRelationship relationship = new TourneyRelationship(getRound().getDivision().getTourney().getNumber());
		final Personality p0 = personalityManager.getPerson(player0);
		final Personality p1 = personalityManager.getPerson(player1);
		if (playersCount == 2) {
			game0 = gamePlayManager.createBoard(settings, Arrays.asList(p0, p1), relationship).getBoardId();
			totalGamesCount = 1;
		} else if (playersCount == 3) {
			final Personality p2 = personalityManager.getPerson(player2);
			game0 = gamePlayManager.createBoard(settings, Arrays.asList(p0, p1), relationship).getBoardId();
			game1 = gamePlayManager.createBoard(settings, Arrays.asList(p0, p2), relationship).getBoardId();
			game2 = gamePlayManager.createBoard(settings, Arrays.asList(p1, p2), relationship).getBoardId();
			totalGamesCount = 3;
		} else if (playersCount == 4) {
			final Personality p2 = personalityManager.getPerson(player2);
			final Personality p3 = personalityManager.getPerson(player3);
			game0 = gamePlayManager.createBoard(settings, Arrays.asList(p0, p1), relationship).getBoardId();
			game1 = gamePlayManager.createBoard(settings, Arrays.asList(p0, p2), relationship).getBoardId();
			game2 = gamePlayManager.createBoard(settings, Arrays.asList(p0, p3), relationship).getBoardId();
			game3 = gamePlayManager.createBoard(settings, Arrays.asList(p1, p2), relationship).getBoardId();
			game4 = gamePlayManager.createBoard(settings, Arrays.asList(p1, p3), relationship).getBoardId();
			game5 = gamePlayManager.createBoard(settings, Arrays.asList(p2, p3), relationship).getBoardId();
			totalGamesCount = 6;
		}
		startedDate = new Date();
		return totalGamesCount;
	}

	void finalizeGame(GameBoard<?, ?, ?> board) {
		if (finishedDate != null) {
			throw new IllegalStateException("Group already finished");
		}

		final Collection<Personality> wonPlayers = board.getWonPlayers();
		final long boardId = board.getBoardId();
		if (wonPlayers == null) {
			throw new IllegalStateException("Game is not finished: " + boardId);
		}

		if (wonPlayers.size() == 0) {
			setGameResult(boardId, TourneyGameResolution.DRAW);
		} else {
			final Personality winner = wonPlayers.iterator().next();
			setGameResult(boardId, winner.getId() == getGameMaster(boardId) ? TourneyGameResolution.WON : TourneyGameResolution.LOST);
		}
		finishedGamesCount += 1;

		if (finishedGamesCount == totalGamesCount) {
			finishedDate = new Date();
		}
	}

	private void setGameResult(long boardId, TourneyGameResolution result) {
		switch (getGameIndex(boardId)) {
			case 0:
				result0 = result;
				break;
			case 1:
				result1 = result;
				break;
			case 2:
				result2 = result;
				break;
			case 3:
				result3 = result;
				break;
			case 4:
				result4 = result;
				break;
			case 5:
				result5 = result;
				break;
		}
	}

	int getPlayerIndex(long player) {
		if (player == 0) {
			throw new IllegalArgumentException("Incorrect player id");
		}
		if (player == player0) {
			return 0;
		} else if (player == player1) {
			return 1;
		} else if (player == player2) {
			return 2;
		} else if (player == player3) {
			return 3;
		}
		throw new IllegalArgumentException("Incorrect player id");
	}

	int getGameIndex(long boardId) {
		if (game0 == boardId) {
			return 0;
		} else if (game1 == boardId) {
			return 1;
		} else if (game2 == boardId) {
			return 2;
		} else if (game3 == boardId) {
			return 3;
		} else if (game4 == boardId) {
			return 4;
		} else if (game5 == boardId) {
			return 5;
		}
		throw new IllegalArgumentException("Incorrect board id: " + boardId);
	}

	int getGameIndex(int playerIndex1, int playerIndex2) {
		final int i1 = Math.min(playerIndex1, playerIndex2);
		final int i2 = Math.max(playerIndex1, playerIndex2);

		if (totalGamesCount == 3) {
			return (i1 == 0 ? i2 - 1 : i1 + i2 - 1);
		} else {
			return (i1 == 0 ? i2 - 1 : i1 + i2);
		}
	}

	long getGameByIndex(int gameIndex) {
		switch (gameIndex) {
			case 0:
				return game0;
			case 1:
				return game1;
			case 2:
				return game2;
			case 3:
				return game3;
			case 4:
				return game4;
			case 5:
				return game5;
		}
		throw new IndexOutOfBoundsException("Incorrect game index");
	}

	long getGameMaster(long game) {
		final int index = getGameIndex(game);
		if (totalGamesCount == 1) {
			return player0;
		} else if (totalGamesCount == 3) {
			if (index == 2) {
				return player1;
			}
			return player0;
		} else if (totalGamesCount == 6) {
			if (index == 5) {
				return player2;
			}
			if (index < 3) {
				return player0;
			}
			return player1;
		}
		throw new IllegalStateException("Incorrect total games count");
	}

	TourneyGameResolution getResultByIndex(int gameIndex) {
		switch (gameIndex) {
			case 0:
				return result0;
			case 1:
				return result1;
			case 2:
				return result2;
			case 3:
				return result3;
			case 4:
				return result4;
			case 5:
				return result5;
		}
		throw new IndexOutOfBoundsException("Incorrect game index");
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("HibernateTourneyGroup");
		sb.append("{internalId=").append(internalId);
		sb.append(", group=").append(group);
		sb.append(", round=").append(round);
		sb.append(", startedDate=").append(startedDate);
		sb.append(", finishedDate=").append(finishedDate);
		sb.append(", player1=").append(player0);
		sb.append(", player2=").append(player1);
		sb.append(", player3=").append(player2);
		sb.append(", player4=").append(player3);
		sb.append(", game1=").append(game0);
		sb.append(", game2=").append(game1);
		sb.append(", game3=").append(game2);
		sb.append(", game4=").append(game3);
		sb.append(", game5=").append(game4);
		sb.append(", game6=").append(game5);
		sb.append(", result1=").append(result0);
		sb.append(", result2=").append(result1);
		sb.append(", result3=").append(result2);
		sb.append(", result4=").append(result3);
		sb.append(", result5=").append(result4);
		sb.append(", result6=").append(result5);
		sb.append(", playersCount=").append(playersCount);
		sb.append(", totalGamesCount=").append(totalGamesCount);
		sb.append(", finishedGamesCount=").append(finishedGamesCount);
		sb.append('}');
		return sb.toString();
	}
}

