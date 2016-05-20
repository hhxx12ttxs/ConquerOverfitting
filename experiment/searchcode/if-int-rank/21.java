package net.jacobandreas.steph.strategy;

import net.jacobandreas.steph.event.PairingEventListener;
import net.jacobandreas.steph.tournament.*;
import java.util.*;

/**
 * Defines common behaviors and useful helper methods for all pairing
 * strategies.
 *
 * @author jacob
 */
public abstract class BasePairingStrategy implements PairingStrategy {

    /**
     * A list of registered PairingEventListeners
     */
    ArrayList<PairingEventListener> pairingEventListeners;

    /**
     * Sets up the BasePairingStrategy
     */
    protected BasePairingStrategy() {
        pairingEventListeners = new ArrayList<PairingEventListener>();
    }

    /**
     * For avoiding roundoff errors in floating-point arithmetic.
     */
    public static final double TOLERANCE = 0.01;

    /**
     * Stub for round-specific comparator for ranking potential swaps according
     * to their desirability
     * @return a comparator for ranking swaps
     */
    public abstract Comparator<Swap> swapComparator();

    /**
     * Registers a new PairingEventListener
     * @param l the listener to register
     */
    public void addPairingEventListener(PairingEventListener l) {
        pairingEventListeners.add(l);
    }

    /**
     * Gets the list of registered PairingEventListeners
     * @return an array of registered listeners
     */
    public PairingEventListener[] getPairingEventListeners() {
        return pairingEventListeners.toArray(new PairingEventListener[0]);
    }

    /**
     * Dispatches a new pairing event to all registered listeners
     * @param event a description of the event that occurred
     * @param data an object with additional data about the pairing event
     */
    protected void reportPairingEvent(String event, Object data) {
        for(PairingEventListener l : getPairingEventListeners()) {
            l.pairingEventOccurred(event, data);
        }
    }

    /**
     * Determines whether a new match between the given teams is permissible.
     * @param team1 The first team in the proposed match
     * @param team2 The second team in the proposed match
     * @return true if the teams can hit each other in the next round, and false
     * otherwise.
     */
    protected boolean isPermitted(Team team1, Team team2) {
        // make sure the teams agree about the permissibility of the match
        if(team1.canHit(team2) && !team2.canHit(team1)) {
            assert false: "Sanity check failed: teams are not mutually impermissible";
        }
        if(team2.canHit(team1) && !team1.canHit(team2)) {
            assert false: "Sanity check failed: teams are not mutually impermissible";
        }
        return team1.canHit(team2);
    }

    /**
     * Utility method to randomly shuffle a list of teams (does not modify the
     * original list).
     * @param teams the list of teams to shuffle
     * @return a shuffled list of teams
     */
    protected ArrayList<Team> shuffle(ArrayList<Team> teams) {
        teams = (ArrayList<Team>)teams.clone();
        ArrayList<Team> newstack = new ArrayList<Team>();
        int len = teams.size();
        while(len > 0) {
            newstack.add(teams.remove((int)(len * Math.random())));
            len--;
        }
        return newstack;
    }

    /**
     * Automatically resolves all impermissible matches to find the legal
     * pairing for a side-constrained round. NOTE: This method will mutate
     * the input lists in the process of resolving impermissibles.
     *
     * This method is designed to simulate, as closely as possible, the
     * procedure carried out by human tabulators with stacks of tab cards.
     * Each Team object represents a card, and the pi and delta ArrayLists
     * represent the card stacks.
     *
     * @param pi The list of teams who need to go Prosecution/Plaintiff in the
     * next round
     * @param delta The list of teams who need to go Defense in the next round
     * @return The pairer's list
     */
    protected List<Swap> sideConstrainedResolveImpermissibles(ArrayList<Team> pi, ArrayList<Team> delta) {

        // Make sure both stacks are the same size
        assert pi.size() == delta.size() : "Sanity check failed: lists are not the same length";

        // Assign P and D ranks to the pi and delta stacks respectively
        assignRanks(pi);
        assignRanks(delta);

        // prepare the Pairer's list
        ArrayList<Swap> swaps = new ArrayList<Swap>();

        for(int i = 0; i < pi.size(); i++) {
        // for every pair of cards in the stack

            Team t1 = pi.get(i);
            Team t2 = delta.get(i);

            if(isPermitted(t1, t2)) {
                // keep looking farther down
                continue;
            }

            // Otherwise, this match is impermissible. The resolution may generate
            // another impermissible match higher up, so we have to go back to
            // the top of the stack on the next turn.
            i = -1;

            ArrayList<Swap> swapCandidates;
            int reach = 1; // The distance out we will look for candidate swaps
            do {

                if(reach > pi.size()) {
                // we have tried every possible swap: give up. (N.B. this has
                // never happened in a real round)
                    reportPairingEvent(PairingEventListener.UNPAIRABLE, null);
                    return swaps;
                }
                // get swap candidates in the pi column
                swapCandidates = getSwapCandidates(t1, pi, swaps, reach, Match.PI);
                // and the delta column
                swapCandidates.addAll(getSwapCandidates(t2, delta, swaps, reach, Match.DELTA));
                // increment the reach in case there were no candidates
                reach++;
            } while(swapCandidates.size() == 0);

            // rank the available swaps by desirability
            Collections.sort(swapCandidates, swapComparator());
            // pick the most desirable
            Swap chosenSwap = swapCandidates.get(0);
            ArrayList<Team> workingList;

            // choose which list we're modifying on this step
            if(chosenSwap.contains(t1)) {
                workingList = pi;
            } else if(chosenSwap.contains(t2)) {
                workingList = delta;
            } else {
                assert false : "Sanity check failed: neither impermissible team is team1";
                workingList = null;
            }

            Team badTeam = chosenSwap.getTeam1(); // the team that caused the impermissible
            Team goodTeam = chosenSwap.getTeam2(); // the team used to resolve it

            // swap the rank numbers of the two teams
            int oldBadRank = badTeam.getRank();
            badTeam.setRank(goodTeam.getRank());
            goodTeam.setRank(oldBadRank);

            // swap the stack positions of the two teams
            int oldBadIndex = workingList.indexOf(badTeam);
            int oldGoodIndex = workingList.indexOf(goodTeam);
            workingList.remove(badTeam);
            workingList.remove(goodTeam);

            if(oldBadIndex < oldGoodIndex) {
                workingList.add(oldBadIndex, goodTeam);
                workingList.add(oldGoodIndex, badTeam);
            } else if (oldGoodIndex < oldBadIndex) {
                workingList.add(oldGoodIndex, badTeam);
                workingList.add(oldBadIndex, goodTeam);
            } else {
                assert false : "Sanity check failed: bad team and good team have the same index!";
            }

            // remember this swap in the pairer's list
            swaps.add(chosenSwap);
        }

        return swaps;

    }

    /**
     * Automatically resolves all impermissible matches to find the legal
     * pairing for an unconstrained round. NOTE: This method will mutate
     * the input lists in the process of resolving impermissibles.
     *
     * This method is designed to simulate, as closely as possible, the
     * procedure carried out by human tabulators with stacks of tab cards.
     * Each Team object represents a card, and the teams ArrayList represents
     * the team stack
     *
     * @param teams The list of teams to be paired
     * @return The pairer's list
     */
    protected List<Swap> resolveImpermissibles(ArrayList<Team> teams) {

        // Assign ranks to the stack
        assignRanks(teams);

        // Prepare the pairer's list
        ArrayList<Swap> swaps = new ArrayList<Swap>();

        for(int i = 0; i < teams.size() - 1; i += 2) {
        // for every pair of teams in the pairer's list

            Team t1 = teams.get(i);
            Team t2 = teams.get(i + 1);

            if(isPermitted(t1, t2)) {
                // this match is allowed, look farther down
                continue;
            }

            // Otherwise, the pairing is impermissible. The resolution at this
            // step may create another impermissible higher up, so we'll need
            // to go back to the top of the stack on the next turn.
            i = -2;

            ArrayList<Swap> swapCandidates;
            int reach = 1; // the distance out we will look for candidate swaps

            do {

                if(reach > teams.size()) {
                    // We have tried every possible swap: give up.
                    reportPairingEvent(PairingEventListener.UNPAIRABLE, null);
                    return swaps;
                }

                // get the candidate swaps for each team
                swapCandidates = getSwapCandidates(t1, teams, swaps, reach, Match.NO_SIDE);
                swapCandidates.addAll(getSwapCandidates(t2, teams, swaps, reach, Match.NO_SIDE));

                // make sure we don't attempt to swap the teams with each other
                Swap forbidden = new Swap(t1, t2, Match.NO_SIDE);
                if(swapCandidates.contains(forbidden)) {
                    swapCandidates.remove(forbidden);
                    swapCandidates.remove(forbidden);
                    // symmetric, so there have to be two copies
                }
                // extend our reach, in case we didn't find anything
                reach++;

            } while(swapCandidates.size() == 0);

            // rank the swaps by desirability, and
            Collections.sort(swapCandidates, swapComparator());
            // choose the most desirable
            Swap chosenSwap = swapCandidates.get(0);

            // make sure the chosen swap is valid
            if(chosenSwap.contains(t1) && chosenSwap.contains(t2)) {
                System.err.println("Sanity check failed: swapping two impermissibly paired teams");
            }
            if(!(chosenSwap.contains(t1) || chosenSwap.contains(t2)))  {
                System.err.println("Sanity check failed: swap contains neither impermissibly paired team");
            }

            Team badTeam = chosenSwap.getTeam1(); // the team that caused the impermissible
            Team goodTeam = chosenSwap.getTeam2(); // the team that will fix it

            // swap ranks
            int oldBadRank = badTeam.getRank();
            badTeam.setRank(goodTeam.getRank());
            goodTeam.setRank(oldBadRank);

            // and list positions
            int oldBadIndex = teams.indexOf(badTeam);
            int oldGoodIndex = teams.indexOf(goodTeam);
            teams.remove(badTeam);
            teams.remove(goodTeam);

            if(oldBadIndex < oldGoodIndex) {
                teams.add(oldBadIndex, goodTeam);
                teams.add(oldGoodIndex, badTeam);
            } else if (oldGoodIndex < oldBadIndex) {
                teams.add(oldGoodIndex, badTeam);
                teams.add(oldBadIndex, goodTeam);
            } else {
                assert false : "Sanity check failed: bad team and good team have the same index!";
            }

            // add swap to the pairer's list
            swaps.add(chosenSwap);

        }


        return swaps;
    }

    /**
     * Gets potential swap partners for the given team from the given list
     * @param t The team we're trying to swap out
     * @param teams The list of teams we can swap with
     * @param madeSwaps The list of swaps we've already made
     * @param reach The distance out to look for swaps
     * @param side The side of the case we're making this swap on
     * @return A list of candidate swaps
     */
    protected ArrayList<Swap> getSwapCandidates(Team t, ArrayList<Team> teams, ArrayList<Swap> madeSwaps, int reach, int side) {
        // Each team has at most two candidate swaps
        ArrayList<Swap> candidates = new ArrayList<Swap>(2);

        int rank = t.getRank();

        if(rank > reach) {
            // we're high enough up in the list to look below us for candidates
            // get the candidate, and
            Team cand = getWithRank(rank - reach, teams);
            Swap candSwap = new Swap(t, cand, side);
            // add it to the list if we haven't already tried it
            if(!madeSwaps.contains(candSwap)) {
                candidates.add(candSwap);
            }
        }

        if(rank < teams.size() + 1 - reach) {
            // we're low enough in the list that we can look above us for candidates
            // get the candidate
            Team cand = getWithRank(rank + reach, teams);
            Swap candSwap = new Swap(t, cand, side);
            // add it to the list if we haven't already tried it
            if(!madeSwaps.contains(candSwap)) {
                candidates.add(candSwap);
            }
        }

        return candidates;
    }

    /**
     * Numbers the teams in a list according to their position.
     * @param toRank The list of teams to rank
     */
    protected void assignRanks(ArrayList<Team> toRank) {
        int i = 1;
        for(Team team : toRank) {
            team.setRank(i);
            i++;
        }
    }

    /**
     * Gets the Team with the given rank from the given list
     * @param rank The rank we're looking for
     * @param teams The teams we're looking in
     * @return The requested team
     */
    protected Team getWithRank(int rank, ArrayList<Team> teams) {
        for(Team t : teams) {
            if(t.getRank() == rank) {
                return t;
            }
        }
        assert false : "Sanity check failed: no team with rank " + rank;
        return null;
    }

}

