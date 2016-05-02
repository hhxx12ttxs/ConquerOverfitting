package wisematches.playground.tourney.regular;

import wisematches.personality.Language;
import wisematches.playground.tourney.TourneyEntity;

import java.util.EnumSet;

/**
 * The tournament group is last tournament entity that describes players and games for one group.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public interface TourneyGroup extends RegularTourneyEntity<TourneyGroup, TourneyGroup.Id, TourneyGroup.Context> {
	/**
	 * Returns group number
	 *
	 * @return the group number.
	 */
	int getGroup();

	/**
	 * Returns round that this group is belong to.
	 *
	 * @return the tourney round.
	 */
	TourneyRound getRound();

	/**
	 * Returns all games which take part in this group. The array contains first all games for first player,
	 * when for second player and so on.
	 *
	 * @return all games which take part in this group.
	 */
	long[] getGames();

	/**
	 * All players in this group.
	 *
	 * @return array of all players in this group.
	 */
	long[] getPlayers();

	/**
	 * Returns scores for each player in this group. Index in this array is equals to player's index in {@link #getPlayers()} array.
	 *
	 * @return all scores for players in this group.
	 */
	short[] getScores();

	/**
	 * Returns scores for specified player only.
	 *
	 * @param player the player id
	 * @return player's scores.
	 */
	short getScores(long player);

	/**
	 * Returns game id between two specified players. Order of players are not important.
	 *
	 * @param p1 first player
	 * @param p2 second player.
	 * @return the game id that is played by specified players.
	 * @throws IllegalArgumentException if any player doesn't belong to this group.
	 */
	long getGameId(long p1, long p2);

	boolean isWinner(long player);

	public final class Id extends TourneyEntity.Id<TourneyGroup, Id> {
		private TourneyRound.Id id;
		private int group;

		private Id() {
		}

		public Id(int tourney, Language language, TourneySection section, int round, int group) {
			this(new TourneyRound.Id(tourney, language, section, round), group);
		}

		public Id(TourneyRound.Id id, int group) {
			this.id = id;
			this.group = group;
		}

		public int getGroup() {
			return group;
		}

		public TourneyRound.Id getRoundId() {
			return id;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Id id1 = (Id) o;
			return group == id1.group && id.equals(id1.id);
		}

		@Override
		public int hashCode() {
			int result = id.hashCode();
			result = 31 * result + group;
			return result;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("Id");
			sb.append("{id=").append(id);
			sb.append(", group=").append(group);
			sb.append('}');
			return sb.toString();
		}
	}

	public final class Context extends TourneyEntity.Context<TourneyGroup, Context> {
		private final TourneyRound.Id round;

		public Context(TourneyRound.Id round) {
			this.round = round;
		}

		public Context(TourneyRound.Id round, EnumSet<State> states) {
			super(states);
			this.round = round;
		}

		public TourneyRound.Id getRoundId() {
			return round;
		}
	}
}
