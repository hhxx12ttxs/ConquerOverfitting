package net.jacobandreas.steph.strategy;

import net.jacobandreas.steph.tournament.*;
import java.util.*;

/**
 * Strategy to pair round 3 of a tournament. This pairing is NOT side constrained,
 * and uses CS before PD in rankings.
 *
 * @author jacob
 */
public class Round3PairingStrategy extends BasePairingStrategy {

    private final boolean higherNumberComesFirst, piInLeftColumn;

    /**
     * Creates a new Round3PairingStrategy, with the given preferences about
     * team ordering and the pi column
     * @param higherNumberComesFirst
     * @param piInLeftColumn
     */
    public Round3PairingStrategy(boolean higherNumberComesFirst, boolean piInLeftColumn) {
        this.higherNumberComesFirst = higherNumberComesFirst;
        this.piInLeftColumn = piInLeftColumn;
    }

    /**
     * Pairs the round, using record, CS and PD to determine swap closeness
     * @param teams the teams to pair
     * @return A paired round
     */
    public Round pair(ArrayList<Team> teams) {

        Round round = new Round();

        // sort the stack
        Collections.sort(teams, getRound3Comparator());

        // resolve impermissibles
        resolveImpermissibles(teams);

        // double check our pairings, and add them to the round
        for(int i = 0; i < teams.size()-1; i += 2) {
            Team t1 = teams.get(i);
            Team t2 = teams.get(i+1);
            assert isPermitted(t1, t2) :
               "Sanity check failed: impermissibles paired (3)";
            Match m;
            if(piInLeftColumn) {
                m = new Match(t1, t2);
            } else {
                m = new Match(t2, t1);
            }
            round.addMatch(m);
        }

        return round;

    }

    /**
     * Gets a comparator to rank swaps by desirability. The most desirable swaps
     * minimize the difference in record, then CS, then PD.
     * @return a swap comparator
     */
    public Comparator<Swap> swapComparator() {
        return new Comparator<Swap>() {
            public int compare(Swap s1, Swap s2) {
                double winDD = s1.getWinDifference() - s2.getWinDifference();
                //i.e. Win-Difference-Difference
                if(Math.abs(winDD) > TOLERANCE) {
                    return (int) Math.signum(winDD);
                }
                double csDD = s1.getCSDifference() - s2.getCSDifference();
                if(Math.abs(csDD) > TOLERANCE) {
                    return (int) Math.signum(csDD);
                }
                double pdDD = s1.getPDDifference() - s2.getPDDifference();
                //i.e. Point-Differential-Difference-Difference
                if(Math.abs(pdDD) > TOLERANCE) {
                    return (int) Math.signum(pdDD);
                }
                int rankSumD = s1.getRankSum() - s2.getRankSum();
                if(Math.abs(rankSumD) > TOLERANCE) { // but these are ints
                    return (int) -Math.signum(rankSumD);
                    // higher rank sums come "first"
                }
                if(s1.getSide() == Match.DELTA && s2.getSide() == Match.PI) {
                    return -1;
                } else if(s1.getSide() == Match.PI && s2.getSide() == Match.DELTA) {
                    return 1;
                }
                assert false : "Sanity check failed: unable to rank swaps (3)";
                return 0;
            }
        };
    }

    /**
     * Gets a comparator to order teams in the card stack, using CS as the first-
     * level tiebreaker and PD as the second.
     * @return a team comparator for the card stack
     */
    private Comparator<Team> getRound3Comparator() {
        return new Comparator<Team>() {

            public int compare(Team t1, Team t2) {
                double winD = t2.getWins() - t1.getWins();
                if(Math.abs(winD) > TOLERANCE) {
                    return (int) Math.signum(winD);
                }
                double cs = t2.getCombinedStrength() - t1.getCombinedStrength();
                if(Math.abs(cs) > TOLERANCE) {
                    return (int) Math.signum(cs);
                }
                double pD = t2.getPointDifferential() - t1.getPointDifferential();
                if(Math.abs(pD) > TOLERANCE) {
                    return (int) Math.signum(pD);
                }
                if(higherNumberComesFirst) {
                    return t2.getNumber() - t1.getNumber();
                } else {
                    return t1.getNumber() - t2.getNumber();
                }
            }

        };
    }


}

