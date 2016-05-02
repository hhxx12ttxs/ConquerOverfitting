package cz.fhsoft.poker.league.shared.persistence.compare;

import java.util.Comparator;

import cz.fhsoft.poker.league.shared.model.v1.Competition;
import cz.fhsoft.poker.league.shared.model.v1.DescribedEntity;
import cz.fhsoft.poker.league.shared.model.v1.Game;
import cz.fhsoft.poker.league.shared.model.v1.IdentifiableEntity;
import cz.fhsoft.poker.league.shared.model.v1.Invitation;
import cz.fhsoft.poker.league.shared.model.v1.Player;
import cz.fhsoft.poker.league.shared.model.v1.PlayerInGame;
import cz.fhsoft.poker.league.shared.model.v1.PrizeMoneyFormula;
import cz.fhsoft.poker.league.shared.model.v1.PrizeMoneyRule;
import cz.fhsoft.poker.league.shared.model.v1.PrizeMoneyRuleSet;
import cz.fhsoft.poker.league.shared.model.v1.Tournament;
import cz.fhsoft.poker.league.shared.services.RankingRecord;
import cz.fhsoft.poker.league.shared.util.StringUtil;

public class Comparators {

	public static final IdentifiableEntityComparator<IdentifiableEntity> IDENTIFIABLE_ENTITY_COMPARATOR = new IdentifiableEntityComparator<IdentifiableEntity>(); 

	public static final DescribedEntityComparator<DescribedEntity> DESCRIBED_ENTITY_COMPARATOR = new DescribedEntityComparator<DescribedEntity>(); 

	public static final Comparator<Competition> COMPETITIONS_COMPARATOR = new DescribedEntityComparator<Competition>() {

		@Override
		public int compare(Competition c1, Competition c2) {
			int result = - c1.getStartDate().compareTo(c2.getStartDate());
			if(result != 0)
				return result;

			result = c1.getEndDate().compareTo(c2.getEndDate());
		
			if(result != 0)
				return result;

			return super.compare(c1, c2);
		}
		
	};

	public static final Comparator<Tournament> TOURNAMENTS_COMPARATOR = new DescribedEntityComparator<Tournament>() {

		@Override
		public int compare(Tournament t1, Tournament t2) {
			int result = - t1.getTournamentStart().compareTo(t2.getTournamentStart());
			
			if(result != 0)
				return result;

			return super.compare(t1, t2);
		}
		
	};

	public static final Comparator<Invitation> INVITATIONS_COMPARATOR = new IdentifiableEntityComparator<Invitation>() {

		@Override
		public int compare(Invitation i1, Invitation i2) {
			// invitations must be resolved (or, in the worst case, proxied for being stale)
			int result = i1.getOrdinal() == 0
					? (i2.getOrdinal() != 0 ? 1 : 0)
					: (i2.getOrdinal() == 0
					? (i1.getOrdinal() != 0 ? -1 : 0)
					: 0);

			if(result != 0)
				return result;

			result = i1.getOrdinal() > i2.getOrdinal()
					? 1
					: (i1.getOrdinal() < i2.getOrdinal()
							? -1
							:0);
			
			if(result != 0)
				return result;
			
			result = i1.getReply().ordinal() > i2.getReply().ordinal()
					? 1
					: (i1.getReply().ordinal() < i2.getReply().ordinal()
							? -1
							: 0);

			if(result != 0)
				return result;
			
			result = i1.getPlayer().getNick().compareTo(i2.getPlayer().getNick());
			
			if(result != 0)
				return result;

			return super.compare(i1, i2);
		}
		
	};

	public static final Comparator<Game> GAMES_COMPARATOR = new IdentifiableEntityComparator<Game>() {

		@Override
		public int compare(Game g1, Game g2) {
			int n1 = g1.getOrdinal();
			int n2 = g2.getOrdinal();
			
			int result = n1 > n2
					? 1
					: (n1 < n2
							? -1
							: 0);
			
			
			if(result != 0)
				return result;

			return super.compare(g2, g2);
		}
		
	};

	public static final Comparator<Player> PLAYERS_COMPARATOR = new IdentifiableEntityComparator<Player>() {

		@Override
		public int compare(Player p1, Player p2) {
			int result = StringUtil.nonNullString(p1.getNick()).compareTo(StringUtil.nonNullString(p2.getNick()));
			if(result != 0)
				return result;

			result = StringUtil.nonNullString(p1.getLastName()).compareTo(StringUtil.nonNullString(p2.getLastName()));
			if(result != 0)
				return result;

			result = StringUtil.nonNullString(p1.getFirstName()).compareTo(StringUtil.nonNullString(p2.getFirstName()));
			if(result != 0)
				return result;

			result = StringUtil.nonNullString(p1.getEmailAddress()).compareTo(StringUtil.nonNullString(p2.getEmailAddress()));

			if(result != 0)
				return result;

			return super.compare(p1, p2);
		}
		
	};

	public static final Comparator<PlayerInGame> PLAYERS_IN_GAME_COMPARATOR = new IdentifiableEntityComparator<PlayerInGame>() {
		
		@Override
		public int compare(PlayerInGame pig1, PlayerInGame pig2) {
			int result = pig1.getRank() > pig2.getRank()
					? 1
					: (pig1.getRank() < pig2.getRank()
							? -1
							:0);
			
			if(result != 0)
				return result;

			result = PLAYERS_COMPARATOR.compare(pig1.getPlayer(), pig2.getPlayer());
			
			if(result != 0)
				return result;

			return super.compare(pig1, pig2);
		}
		
	};

	public static final Comparator<PrizeMoneyRuleSet> PRIZE_MONEY_RULE_SET_COMPARATOR = new DescribedEntityComparator<PrizeMoneyRuleSet>() {

		@Override
		public int compare(PrizeMoneyRuleSet s1, PrizeMoneyRuleSet s2) {
			int result = s1.getName().compareTo(s2.getName());
			if(result != 0)
				return result;

			return super.compare(s1, s2);
		}
		
	};

	public static final Comparator<PrizeMoneyRule> PRIZE_MONEY_RULE_COMPARATOR = new IdentifiableEntityComparator<PrizeMoneyRule>() {

		@Override
		public int compare(PrizeMoneyRule r1, PrizeMoneyRule r2) {
			int n1 = r1.getNumberOfPlayers();
			int n2 = r2.getNumberOfPlayers();
			
			int result = n1 > n2
					? 1
					: (n1 < n2
							? -1
							: 0);
			
			if(result != 0)
				return result;

			return super.compare(r1, r2);
		}
		
	};

	public static final Comparator<PrizeMoneyFormula> PRIZE_MONEY_FORMULA_COMPARATOR = new IdentifiableEntityComparator<PrizeMoneyFormula>() {

		@Override
		public int compare(PrizeMoneyFormula f1, PrizeMoneyFormula f2) {
			int n1 = f1.getRank();
			int n2 = f2.getRank();
			
			int result = n1 > n2
					? 1
					: (n1 < n2
							? -1
							: 0);
			
			if(result != 0)
				return result;

			return super.compare(f1, f2);
		}
		
	};


	public static final Comparator<RankingRecord> RANKING_RECORD_COMPARATOR_WITH_SPLIT = new Comparator<RankingRecord>() {

		@Override
		public int compare(RankingRecord r1, RankingRecord r2) {
			int result = r1.getInGameFlag() > r2.getInGameFlag()
					? 1
					: (r1.getInGameFlag() < r2.getInGameFlag()
							? -1
							: 0);
			
			if(result != 0)
				return result;

			double attendance1 = (double) r1.getGamesPlayed() / (double) r1.getTotalGames();
			double attendance2 = (double) r2.getGamesPlayed() / (double) r2.getTotalGames();

			boolean attendanceOk1 = attendance1*100 >= r1.getMinimalAttendance();
			boolean attendanceOk2 = attendance2*100 >= r2.getMinimalAttendance();

			if(attendanceOk1 != attendanceOk2)
				return attendanceOk1
						? -1
						: 1;

			result = r1.getRelativePrizeMoney() > r2.getRelativePrizeMoney()
					? -1
					: (r1.getRelativePrizeMoney() < r2.getRelativePrizeMoney()
							? 1
							: 0);
			
			if(result != 0)
				return result;

			result = r1.getRelativePoints() > r2.getRelativePoints()
					? -1
					: (r1.getRelativePoints() < r2.getRelativePoints()
							? 1
							: 0);
			
			if(result != 0)
				return result;

			result = attendance1 > attendance2
					? -1
					: (attendance1 < attendance2
							? 1
							: 0);

			return result;
		}
		
	};

	public static final Comparator<RankingRecord> RANKING_RECORD_COMPARATOR_STRICT = new Comparator<RankingRecord>() {

		@Override
		public int compare(RankingRecord r1, RankingRecord r2) {
			int result = RANKING_RECORD_COMPARATOR_WITH_SPLIT.compare(r1, r2);

			if(result != 0)
				return result;

			return r1.getPlayerNick().compareTo(r2.getPlayerNick()); // name decides :-)
		}
		
	};
}

