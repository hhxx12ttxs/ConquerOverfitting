package net.sf.colossus.ai;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.colossus.client.Client;
import net.sf.colossus.client.LegionClientSide;
import net.sf.colossus.client.PlayerClientSide;
import net.sf.colossus.game.Legion;
import net.sf.colossus.server.Constants;
import net.sf.colossus.util.MultiSet;
import net.sf.colossus.variant.CreatureType;
import net.sf.colossus.variant.MasterBoardTerrain;
import net.sf.colossus.variant.MasterHex;
import net.sf.colossus.xmlparser.TerrainRecruitLoader;


/**
 * Simple implementation of a Titan AI
 * @version $Id: RationalAI.java 3202 2008-02-04 12:07:39Z peterbecker $
 * @author Bruce Sherrod, David Ripton
 * @author Romain Dolbeau
 * @author Corwin Joy, extensively rewritten on 02-Oct-2003
 */
public class RationalAI extends SimpleAI
{
    private static final Logger logger = Logger.getLogger(RationalAI.class
        .getName());

    boolean I_HATE_HUMANS = false;
    private final LinkedList<Legion> legionsToSplit = new LinkedList<Legion>();
    private Map<MasterHex, List<Legion>>[] enemyAttackMap;
    private final Map<String, Integer> evaluateMoveMap = new HashMap<String, Integer>();
    private List<LegionBoardMove> bestMoveList;
    private Iterator<LegionBoardMove> bestMoveListIter;

    public RationalAI(Client client)
    {
        super(client);
    }

    private static double r3(double value)
    {
        return Math.round(1000. * value) / 1000.;
    }

    @Override
    public boolean split()
    {
        // Refresh these once per turn.
        enemyAttackMap = buildEnemyAttackMap(client.getOwningPlayer());
        evaluateMoveMap.clear();

        legionsToSplit.clear();

        PlayerClientSide player = client.getOwningPlayer();
        for (Legion legion : player.getLegions())
        {
            legionsToSplit.add(legion);
        }
        return fireSplits();
    }

    /** Return true if done with all splits and callbacks */
    private boolean fireSplits()
    {
        logger.log(Level.FINEST, "RationalAI.fireSplits " + legionsToSplit);

        //safe to cache the player out of the loop, ref to mutable object
        PlayerClientSide player = client.getOwningPlayer();

        while (!legionsToSplit.isEmpty())
        {
            if (player.getNumMarkersAvailable() == 0)
            {
                logger.log(Level.FINEST, "No more splits. No markers left.");
                return true; //early exit, out of markers
            }

            Legion legion = legionsToSplit.removeFirst();
            if (!splitOneLegion(player, legion))
            {
                return false; //early exit, we've decided we won't finish
            }
        }

        return true; //successfully looked at all splits
    }

    // TODO Undoing a split could release the marker needed to do another
    // split, so we need to synchronize access.
    /** If parentId and childId are null, this is a callback to
     * an undo split */
    @Override
    public boolean splitCallback(Legion parent, Legion child)
    {
        logger.log(Level.FINEST, "RationalAI.splitCallback " + parent + " "
            + child);

        return splitOneLegionCallback((LegionClientSide)parent,
            (LegionClientSide)child)
            && fireSplits();
    }

    // Compute the expected value of a split legion
    // If we want to compute just a single legion, pass null for
    // the child_legion
    private double expectedValueSplitLegion(LegionClientSide legion,
        LegionClientSide child_legion)
    {
        double split_value = 0.0;

        // Compute value of staying for each split stack
        double stay_here_value1 = hexRisk(legion, legion.getCurrentHex(),
            false);
        double stay_here_value2;

        if (child_legion != null)
        {
            stay_here_value2 = hexRisk(child_legion, legion.getCurrentHex(),
                false);
            logger.log(Level.FINEST, "expectedValueSplitLegion(), value of "
                + "staying here for split legion1 " + stay_here_value1);
            logger.log(Level.FINEST, "expectedValueSplitLegion(), value of "
                + "staying here for split legion1 " + stay_here_value2);
        }
        else
        {
            logger.log(Level.FINEST, "expectedValueSplitLegion(), value of "
                + "staying here for unsplit legion " + stay_here_value1);
            stay_here_value2 = 0.0;
        }

        for (int roll = 1; roll <= 6; roll++)
        {
            logger.log(Level.FINEST, "expectedValueSplitLegion: Roll " + roll);
            Set<MasterHex> moves = client.getMovement().listAllMoves(legion,
                legion.getCurrentHex(), roll);

            int size1 = moves.size() + 1;
            int size2;

            if (child_legion != null)
            {
                size2 = moves.size() + 1;
            }
            else
            {
                size2 = 2;
            }

            double[] valueStack1 = new double[size1];
            double[] valueStack2 = new double[size2];
            int move_i = 0;

            for (MasterHex hex : moves)
            {
                double risk_payoff1 = evaluateMove(legion, hex, RECRUIT_TRUE,
                    1, true);

                valueStack1[move_i] = risk_payoff1;
                if (child_legion != null)
                {
                    double risk_payoff2 = evaluateMove(child_legion, hex,
                        RECRUIT_TRUE, 1, true);

                    valueStack2[move_i] = risk_payoff2;
                }
                move_i++;
            }
            // add no-move as an option for each stack
            valueStack1[move_i] = stay_here_value1;
            if (child_legion != null)
            {
                valueStack2[move_i] = stay_here_value2;
            }
            else
            {
                // for loop below we need 2 available "stay here" values
                valueStack2[0] = 0.0;
                valueStack2[0] = 0.0;
            }

            // find optimal move for this roll
            // iterate through move combinations of stack1 and stack2 to
            // find max
            double max_split;

            max_split = Integer.MIN_VALUE;

            for (int i = 0; i < size1; i++)
            {
                double val_i = valueStack1[i];

                for (int j = 0; j < size2; j++)
                {
                    if (i == j)
                    {
                        continue;
                    } // split stacks can't move to same hex
                    double val_j = valueStack2[j];
                    double val = val_i + val_j;

                    if (val > max_split)
                    {
                        max_split = val;
                    }
                }
            }
            split_value += max_split;
        } // end for roll

        return split_value;
    }

    /** Return true if done, false if waiting for callback. */
    boolean splitOneLegion(PlayerClientSide player, Legion legion)
    {
        logger.log(Level.FINEST, "splitOneLegion()");

        // Allow aggressive splits - especially early in the game it is better
        // to split more often -- this should get toned down later in the
        // game by the scooby snack factor
        if (legion.getHeight() < 6)
        {
            logger.log(Level.FINEST, "No split: height < 6");
            return true;
        }

        double stay_here_risk = hexRisk(legion, legion.getCurrentHex(), false);

        boolean at_risk = false;
        if (stay_here_risk > 0 || legion.hasTitan())
        {
            at_risk = true;
        }

        if (at_risk && legion.getHeight() < 7)
        {
            logger
                .log(Level.FINEST, "No split: height < 7 and legion at risk");
            return true;
        }

        StringBuffer results = new StringBuffer();
        boolean hasMustered = false;

        MusteredCreatures mc = chooseCreaturesToSplitOut(legion, at_risk);
        List<CreatureType> creatures = mc.creatures;

        hasMustered = mc.mustered;
        Iterator<CreatureType> it = creatures.iterator();
        int child_value = 0;

        while (it.hasNext())
        {
            CreatureType creature = it.next();

            child_value += (creature).getPointValue();
            results.append(creature.getName());
            if (it.hasNext())
            {
                results.append(",");
            }
        }

        // don't split a stack if it has not mustered
        if (legion.getHeight() < 7 && !hasMustered)
        {
            logger.log(Level.FINEST, "No split: height < 7 and not mustered");
            return true;
        }

        // Do the split.  If we don't like the result we will undo it.
        String newMarkerId = pickMarker(player.getMarkersAvailable(), player
            .getShortColor());

        if (newMarkerId == null)
        {
            //should never happen now that we check in fireSplits
            logger.log(Level.FINEST, "No split.  No markers available.");
            return true;
        }

        logger.log(Level.FINEST, "Wait for split callback");
        client.doSplit(legion, newMarkerId, results.toString());
        return false;
    }

    /** Return true if done, false if waiting for undo split */
    private boolean splitOneLegionCallback(LegionClientSide parent,
        LegionClientSide child)
    {
        if (parent == null && child == null)
        {
            return true; //nothing to do. splitCallback() depends on this exit
        }

        logger.log(Level.FINEST, "Split complete");

        if (client.getTurnNumber() == 1)
        { // first turn
            logger.log(Level.FINEST, "First turn split");
            return true;
        }

        // Compute split value
        logger
            .log(Level.FINEST, "splitOneLegion(): Expected value with split");
        double split_value = expectedValueSplitLegion(parent, child);
        // expected value of split

        // find expected value of no split
        logger.log(Level.FINEST,
            "splitOneLegionCallback(): Expected value with no split");
        double no_split_value = 0.0;
        if (client.getTurnNumber() > 1)
        {
            no_split_value = expectedValueSplitLegion(parent, null);
        }

        // For Titan group, try to only split when at 7
        // The only exception should be if we are under
        // severe attack and splitting can save us

        // For now, just don't split titans under 7 tall no matter what
        /*
         if (legion.hasTitan() &&
         (legion.getHeight() + child_legion.getHeight()) < 7)
         {
         split_value -= 10000;
         no_split_value += 10000;
         }
         */

        // If expected value of split + 5 <= no split, do not split.
        // This gives tendency to split if not under attack.
        // If no_split_value is < -20 , we are under attack and trapped.
        // Do not split.
        logger.log(Level.FINEST, "no split value: " + no_split_value);
        logger.log(Level.FINEST, "split value: " + split_value);
        // Inequality needs to be < here.  I_HATE_HUMANS will causes
        // split_value = 0 on both sides.
        if (split_value * 1.02 < no_split_value || split_value < -20)
        {
            // Undo the split
            client.undoSplit(child);
            logger.log(Level.FINEST,
                "undo split - better to keep stack together");
            return false;
        }
        else
        {
            logger.log(Level.FINEST, "keep the split");
            return true;
        }
    }

    /** Find value of recruiting, including possibly attacking an enemy
     set enemy = null to indicate no enemy */
    double recruitValue(Legion legion, MasterHex hex, Legion enemy,
        MasterBoardTerrain terrain)
    {
        int value = 0;

        // Allow recruits even at 7 high for purposes of calculating
        // the mobility value of a particular hex in evaluateMove

        // Consider recruiting.
        List<CreatureType> recruits = client.findEligibleRecruits(legion, hex);

        if (!recruits.isEmpty())
        {
            CreatureType bestRecruit = recruits.get(recruits.size() - 1);

            value = getHintedRecruitmentValue(bestRecruit, legion,
                hintSectionUsed);
        }

        // Consider acquiring angels.
        if (enemy != null)
        {
            int pointValue = ((LegionClientSide)enemy).getPointValue();
            boolean wouldFlee = flee(enemy, legion);

            if (wouldFlee)
            {
                pointValue /= 2;
            }

            // should work with all variants
            int currentScore = ((PlayerClientSide)legion.getPlayer())
                .getScore();
            int arv = TerrainRecruitLoader.getAcquirableRecruitmentsValue();
            int nextScore = ((currentScore / arv) + 1) * arv;

            CreatureType bestRecruit = null;

            while ((currentScore + pointValue) >= nextScore)
            {
                List<String> ral = TerrainRecruitLoader
                    .getRecruitableAcquirableList(terrain, nextScore);
                Iterator<String> it = ral.iterator();

                while (it.hasNext())
                {
                    CreatureType tempRecruit = client.getGame().getVariant()
                        .getCreatureByName(it.next());

                    if ((bestRecruit == null)
                        || (getHintedRecruitmentValue(tempRecruit, legion,
                            hintSectionUsed) >= getHintedRecruitmentValue(
                            bestRecruit, legion, hintSectionUsed)))
                    {
                        bestRecruit = tempRecruit;
                    }
                }
                nextScore += arv;
            }

            // add value of any angels
            if (bestRecruit != null)
            {
                value += getHintedRecruitmentValue(bestRecruit, legion,
                    hintSectionUsed);
            }
        }

        return value;
    }

    // Given a list of creatures sorted by value, figure out which ones are
    // redundant / have mustered.
    // Removes any creatures that have mustered from the original sorted list
    List<CreatureType> removeMustered(List<CreatureType> sortedCreatures)
    {
        // Look at 4 lowest valued creatures
        // Try to pull out pair that has already mustered.
        List<CreatureType> creaturesThatHaveMustered = new ArrayList<CreatureType>();

        outer: for (int index1 = 0; index1 < 4
            && index1 < sortedCreatures.size(); index1++)
        {
            CreatureType critter1 = sortedCreatures.get(index1);

            for (int index2 = index1 + 1; index2 < sortedCreatures.size(); index2++)
            {
                CreatureType critter2 = sortedCreatures.get(index2);

                if (critter1.equals(critter2))
                { // mustering yourself does not count
                    continue;
                }
                if (TerrainRecruitLoader.getRecruitGraph()
                    .isRecruitDistanceLessThan(critter1.getName(),
                        critter2.getName(), 2))
                {// this creature has mustered
                    creaturesThatHaveMustered.add(sortedCreatures.get(index1));
                    sortedCreatures.remove(index1);
                    index1--; // adjust index to account for removal
                    continue outer;
                }
            }
        }

        return creaturesThatHaveMustered;
    }

    public class CompCreaturesByValueName implements Comparator<CreatureType>
    {
        private final Legion legion;

        public CompCreaturesByValueName(Legion legion)
        {
            this.legion = legion;
        }

        public final int compare(CreatureType creature1, CreatureType creature2)
        {
            int val1 = getHintedRecruitmentValue(creature1, legion,
                hintSectionUsed);
            int val2 = getHintedRecruitmentValue(creature2, legion,
                hintSectionUsed);

            if (val1 < val2)
            {
                return -1;
            }
            if (val1 > val2)
            {
                return 1;
            }
            // val1 == val2, compare string
            return (creature1).getName().compareTo((creature2).getName());
        }
    }

    // Sort creatures first by value then by name.
    // Exclude titan.
    List<CreatureType> sortCreaturesByValueName(List<String> Creatures,
        Legion legion)
    {
        List<CreatureType> sortedCreatures = new ArrayList<CreatureType>();
        Iterator<String> critterIt = Creatures.iterator();

        // copy list excluding titan
        while (critterIt.hasNext())
        {
            String name = critterIt.next();
            CreatureType critter = client.getGame().getVariant()
                .getCreatureByName(name);

            // Never split out the titan.
            if (critter.isTitan())
            {
                continue;
            }
            sortedCreatures.add(critter);
        }
        Collections
            .sort(sortedCreatures, new CompCreaturesByValueName(legion));
        return sortedCreatures;
    }

    // Count number of creatures in the stack that have mustered
    int countMustered(LegionClientSide legion)
    {
        List<CreatureType> sortedCreatures = sortCreaturesByValueName(legion
            .getContents(), legion);
        List<CreatureType> creaturesThatHaveMustered = removeMustered(sortedCreatures);

        return creaturesThatHaveMustered.size();
    }

    class MusteredCreatures
    {
        public boolean mustered;
        public List<CreatureType> creatures;

        MusteredCreatures(boolean m, List<CreatureType> c)
        {
            mustered = m;
            creatures = c;
        }
    }

    /** Decide how to split this legion, and return a list of
     *  Creatures to remove + status flag indicating if these
     creatures have mustered or not*/
    MusteredCreatures chooseCreaturesToSplitOut(Legion legion, boolean at_risk)
    {
        //
        // split a 5 to 8 high legion
        //
        // idea: pick the 2 weakest creatures and kick them
        // out. if there are more than 2 weakest creatures,
        // try to split out ones that have already mustered.
        // return split + status flag to indicate if these
        // creatures have mustered or not
        //
        // Also: when splitting, in the case of cyclops, ogres
        // or centaurs try to split out three rather than 2 if
        // these have already mustered
        //
        if (legion.getHeight() == 8)
        {
            List<CreatureType> creatures = doInitialGameSplit(legion
                .getCurrentHex());

            return new MusteredCreatures(true, creatures);
        }

        logger.log(Level.FINEST,
            "sortCreaturesByValueName() in chooseCreaturesToSplitOut");

        List<CreatureType> sortedCreatures = sortCreaturesByValueName(
            ((LegionClientSide)legion).getContents(), legion);

        logger.log(Level.FINEST, "Sorted stack - minus titan: "
            + sortedCreatures);
        // Look at lowest valued creatures
        // Try to pull out pair that has already mustered.
        logger.log(Level.FINEST,
            "removeMustered() in chooseCreaturesToSplitOut");
        List<CreatureType> creaturesThatHaveMustered = removeMustered(sortedCreatures);

        logger.log(Level.FINEST, "List of mustered creatures: "
            + creaturesThatHaveMustered);
        boolean hasMustered = false;

        if (creaturesThatHaveMustered.size() < 1)
        {
            hasMustered = false;
        }
        else
        {
            hasMustered = true;
        }

        List<CreatureType> creaturesToRemove = new ArrayList<CreatureType>();

        // Try to pull out pair that has already mustered.
        logger.log(Level.FINEST,
            "build final split list in chooseCreaturesToSplitOut");
        Iterator<CreatureType> sortIt = creaturesThatHaveMustered.iterator();
        boolean split_all_mustered = false;

        /*
         if (!at_risk)
         {
         split_all_mustered = true;
         }
         **/

        while (sortIt.hasNext()
            && (creaturesToRemove.size() < 2 || split_all_mustered)
            && creaturesToRemove.size() < 4)
        {
            CreatureType critter = sortIt.next();
            creaturesToRemove.add(critter);
        }

        // If we have 3 mustered creatures, check if we have 3
        // Centaur, Ogre, Cyclops, Troll, Lion.  If so, try to keep the
        // 3 together for maximum mustering potential
        // it is a bit aggressive to keep trying to do 3/4 splits
        // but it seems to give a better result
        if (sortIt.hasNext() && !at_risk)
        {
            CreatureType first_remove = creaturesToRemove.get(0);
            CreatureType critter = sortIt.next();
            String s_first = first_remove.getName();
            String s_critter = critter.getName();

            if (s_first.compareTo(s_critter) == 0) // 3 identical, due to sort
            {
                if (s_first.compareTo("Centaur") == 0
                    || s_first.compareTo("Ogre") == 0
                    || s_first.compareTo("Cyclops") == 0
                    || s_first.compareTo("Troll") == 0
                    || s_first.compareTo("Lion") == 0)
                {
                    // remove the 3rd creature
                    creaturesToRemove.add(critter);
                    logger.log(Level.FINEST, "Triple found!");
                    logger.log(Level.FINEST, "Creatures to remove: "
                        + creaturesToRemove);
                    return new MusteredCreatures(hasMustered,
                        creaturesToRemove);
                }

            }

        }
        // If mustered creatures don't come up to 2
        // start pulling out lowest valued un-mustered creatures
        sortIt = sortedCreatures.iterator();
        while (sortIt.hasNext() && creaturesToRemove.size() < 2)
        {
            CreatureType critter = sortIt.next();

            creaturesToRemove.add(critter);
        }
        logger.log(Level.FINEST, "Creatures to remove: " + creaturesToRemove);

        return new MusteredCreatures(hasMustered, creaturesToRemove);
    }

    // little helper class to store possible moves by legion
    private class LegionBoardMove
    {
        final LegionClientSide legion;
        final MasterHex fromHex;
        final MasterHex toHex;
        final double val;
        final boolean noMove;

        LegionBoardMove(LegionClientSide legion, MasterHex fromHex,
            MasterHex toHex, double val, boolean noMove)
        {
            this.legion = legion;
            this.fromHex = fromHex;
            this.toHex = toHex;
            this.val = val;
            this.noMove = noMove;
        }

        @Override
        public String toString()
        {
            return legion + " to " + toHex;
        }
    }

    /** Return true if we need to run this method again after the server
     *  updates the client with the results of a move or mulligan. */
    @Override
    public boolean masterMove()
    {
        logger.log(Level.FINEST, "This is RationalAI.");
        PlayerClientSide player = client.getOwningPlayer();

        if (enemyAttackMap == null)
        {
            // special code to allow game to reload prperly if saved
            // during AI move
            enemyAttackMap = buildEnemyAttackMap(client.getOwningPlayer());
        }

        // consider mulligans
        if (handleMulligans(player))
        {
            return true;
        }

        boolean telePort = false;
        if (bestMoveList == null)
        {
            bestMoveList = new ArrayList<LegionBoardMove>();
            telePort = handleVoluntaryMoves(player);
            bestMoveListIter = bestMoveList.iterator();
        }

        if (!bestMoveListIter.hasNext())
        {
            bestMoveList = null;

            // ForcedSplit and ForcedSingle implementations here are perhaps 
            // quite poor solutions, but better than getting NAKs...

            boolean moved = handleForcedSplitMoves(player);
            if (moved)
            {
                return true;
            }

            if (!player.hasMoved())
            {
                moved = handleForcedSingleMove(player);

                // Earlier here was a comment: 
                // "always need to retry" and hardcoded returned true.
                // In [ 1748718 ] Game halt in Abyssal9 this lead to a deadlock;
                // - so, if here is returned "false" as for "I won't do any more
                // move", that problem does not occur (server recognizes that 
                // there is no legal move and accepts it)
                // -- does this cause negative side effects elsewhere?? 
                // Let's try ;-)

                return moved;

            }

            return false;
        }

        LegionBoardMove lm = bestMoveListIter.next();
        boolean wentOk = client.doMove(lm.legion, lm.toHex);
        if (!wentOk)
        {
            logger.log(Level.WARNING, "RationalAI.masterMove: client.doMove "
                + "returned false!!");
        }

        if (telePort)
        {
            bestMoveList = null;
        }
        return true;
    }

    private boolean findMoveList(List<LegionClientSide> legions,
        List<List<LegionBoardMove>> all_legionMoves,
        MultiSet<MasterHex> occupiedHexes, boolean teleportsOnly)
    {
        boolean moved = false;
        for (LegionClientSide legion : legions)
        {
            if (legion.hasMoved())
            {
                moved = true;
                continue;
            }

            logger.finest("consider marker " + legion);

            // compute the value of sitting still
            List<LegionBoardMove> legionMoves = new ArrayList<LegionBoardMove>();
            MasterHex hex = legion.getCurrentHex();
            double value = evaluateMove(legion, hex, RECRUIT_FALSE, 2, true);
            LegionBoardMove lmove = new LegionBoardMove(legion, hex, hex,
                value, true);

            if (!teleportsOnly)
            {
                legionMoves.add(lmove);
                occupiedHexes.add(hex);

                logger.log(Level.FINEST, "value of sitting still at hex "
                    + hex + " : " + value);
            }

            // find the expected value of all moves for this legion
            Set<MasterHex> set;
            if (!teleportsOnly)
            {
                // exclude teleport moves
                set = client.getMovement().listNormalMoves(legion, hex,
                    client.getMovementRoll());
            }
            else
            {
                // only teleport moves
                set = client.getMovement().listTeleportMoves(legion, hex,
                    client.getMovementRoll());
            }

            for (MasterHex masterHex : set)
            {
                value = evaluateMove(legion, masterHex, RECRUIT_TRUE, 2, true);

                logger.log(Level.FINEST, "value hex " + masterHex + " value: "
                    + r3(value));

                lmove = new LegionBoardMove(legion, legion.getCurrentHex(),
                    masterHex, value, false);
                legionMoves.add(lmove);
            }

            // Sort moves in the order of descending value.
            Collections.sort(legionMoves, new Comparator<LegionBoardMove>()
            {
                public int compare(LegionBoardMove o1, LegionBoardMove o2)
                {
                    return (int)o2.val - (int)o1.val; // want descending order
                }
            });

            all_legionMoves.add(legionMoves);
        }
        return moved;
    }

    /** Return true if we moved something and need to be called again. */
    private boolean handleVoluntaryMoves(PlayerClientSide player)
    {
        logger.log(Level.FINEST, "handleVoluntaryMoves()");

        boolean moved = false;
        List<LegionClientSide> legions = player.getLegions();
        List<List<LegionBoardMove>> all_legionMoves = new ArrayList<List<LegionBoardMove>>();

        MultiSet<MasterHex> occupiedHexes = new MultiSet<MasterHex>();

        moved = findMoveList(legions, all_legionMoves, occupiedHexes, false);

        logger.log(Level.FINEST, "done computing move values for legions");

        logger.log(Level.FINEST, "raw best moves:");
        for (List<LegionBoardMove> moves : all_legionMoves)
        {
            LegionBoardMove lbm = moves.get(0);
            logger.log(Level.FINEST, lbm.legion + " to " + lbm.toHex
                + " value " + lbm.val);
        }

        // handle teleports
        // XXX
        // just take the best teleport.  this is not quite right
        // since it may stick the legion that does not get to
        // teleport with a really bad move.  it is not easy
        // to figure this out though.
        if (client.getMovementRoll() == 6)
        {
            List<List<LegionBoardMove>> teleport_legionMoves = new ArrayList<List<LegionBoardMove>>();
            findMoveList(legions, teleport_legionMoves,
                new MultiSet<MasterHex>(), true);

            Iterator<List<LegionBoardMove>> legit = teleport_legionMoves
                .iterator();
            LegionBoardMove best_move = new LegionBoardMove(null, null, null,
                0, true);
            double best_value = 0;
            while (legit.hasNext())
            {
                List<LegionBoardMove> legionMoves = legit.next();
                if (legionMoves.isEmpty())
                {
                    continue; // not a teleporting legion
                }
                LegionBoardMove lm = legionMoves.get(0);
                if (lm.val > best_value)
                {
                    logger.log(Level.FINEST, "found new teleport best move "
                        + lm.legion + " to " + lm.toHex + " value " + lm.val);
                    best_value = lm.val;
                    best_move = lm;
                }
            }

            if (best_value > 0)
            {
                if (!best_move.noMove)
                {
                    logger.log(Level.FINEST, "found teleport:  "
                        + best_move.legion + " to " + best_move.toHex
                        + " value " + best_move.val);
                    bestMoveList.add(best_move);
                    return true;
                }
            }
        }

        MoveFinder opt = new MoveFinder();
        bestMoveList = opt.findOptimalMove(all_legionMoves, !moved);

        return false;
    }

    /*
     * Returns true if it did one move, i.e. client needs to call us again
     * (after that move was done), to check whether there is still something 
     * to do.
     * Returns false also if something goes wrong (should never happen, but...)
     * so that it does not endlessly redoes that and hangs forever.
     * TODO: not very fancy or clever. All it does is:
     * if there is a hex remaining with forced moves after split,
     * pick the smallest one and move it to the hex with best value.
     * 
     * However, I still prefer this over the SimpleAI approach, which chooses
     * based on "Legionvalue * ChangeValue", because I'd rather sacrifice weaker
     * legions and spare good ones, and not make "unnecessary" damage to good
     * one and spare weak ones...  
     */

    private boolean handleForcedSplitMoves(PlayerClientSide player)
    {
        int roll = client.getMovementRoll();
        ArrayList<MasterHex> unsplitHexes = new ArrayList<MasterHex>();

        /* Consider one hex after another. It is not necessary to look at
         * the individual legions, because
         * a) when looking at one hex, either in "this round" there is no valid
         *    move, or otherwise we move one.
         * b) it does not matter whether there are lords or not, because teleport
         *    moves are not mandatory
         * c) Once we did move one, we move, return true, get called again,
         *    then the list of labels is re-considered again.
         */
        for (Legion legion : player.getLegions())
        {
            List<LegionClientSide> friendlyLegions = client
                .getFriendlyLegions(legion.getCurrentHex(), player);

            if (friendlyLegions.size() > 1)
            {
                unsplitHexes.add(legion.getCurrentHex());
            }
        }
        for (MasterHex hex : unsplitHexes)
        {
            List<LegionClientSide> friendlyLegions = client
                .getFriendlyLegions(hex, player);

            // pick just any legion for asking the getMovement
            LegionClientSide anyLegion = friendlyLegions.get(0);

            if (!client.getMovement().listNormalMoves(anyLegion,
                anyLegion.getCurrentHex(), roll).isEmpty())
            {
                // Easiest solution: just move the smallest of them,
                // usually that is the one with less valuable stuff split off
                //
                // If split would be that Titan is in smallest stack, or 2+2+2
                // and Titan in one of them (and this gets picked by random)
                // well, ... bad luck :-(
                // But this happens very rarely, this whole forcedSplitMoves
                // in games with 6 AIs perhaps one out of 100 games...

                LegionClientSide minLegion = anyLegion;
                int minSize = minLegion.getHeight();

                for (LegionClientSide l : friendlyLegions)
                {
                    int size = l.getHeight();
                    if (size < minSize)
                    {
                        minSize = size;
                        minLegion = l;
                    }
                }

                Set<MasterHex> set = client.getMovement().listNormalMoves(
                    minLegion, minLegion.getCurrentHex(), roll);

                if (set.size() == 0)
                {
                    // This should never happen. Most likely we get then a NAK...
                    logger.log(Level.SEVERE, "Split legion " + minLegion
                        + " in hexlabel " + hex
                        + " was supposed to have forced moves left, "
                        + " but normal moves list is empty?");
                    // anyway keep on going loop for checking next split legion
                }
                else
                {
                    int bestValue = -1;
                    MasterHex bestHex = null;

                    for (MasterHex targetHex : set)
                    {
                        // The set of moves includes still hexes occupied by our own legions. 
                        List<LegionClientSide> targetOwnLegions = client
                            .getFriendlyLegions(targetHex, player);
                        if (targetOwnLegions.size() == 0)
                        {
                            int value = evaluateMove(minLegion, targetHex,
                                RECRUIT_TRUE, 2, true);
                            if (value > bestValue || bestValue == -1)
                            {
                                bestValue = value;
                                bestHex = targetHex;
                            }
                        }
                    }

                    if (bestHex == null)
                    {
                        // well, no legal move for this one. So, leave it as it is.
                        logger.log(Level.FINEST, "Forced split moves remain "
                            + "(no legal move for legion " + minLegion + ")");
                    }
                    else
                    {
                        boolean wentOk = client.doMove(minLegion, bestHex);
                        if (wentOk)
                        {
                            // ok, lets get called again to check if there are more.
                            return true;
                        }
                        else
                        {
                            // This should never happen. Most likely we get then a NAK...
                            logger.log(Level.SEVERE,
                                "Forced split moves remain, "
                                    + "but client rejects moving marker "
                                    + minLegion + " from " + hex + " to "
                                    + bestHex);
                        }
                    }

                }
            }
        }
        // No forced move remaining
        return false;
    }

    /*
     * Simply move the legion which has the lowest value 
     * (except Titan legion) to the place which is best for it.
     * Moves Titan legion only if no other choice.
     */
    private boolean handleForcedSingleMove(PlayerClientSide player)
    {
        int roll = client.getMovementRoll();

        // first we have to find out those that can move at all:

        ArrayList<LegionClientSide> movableLegions = new ArrayList<LegionClientSide>();

        for (LegionClientSide legion : player.getLegions())
        {
            Set<MasterHex> set = client.getMovement().listNormalMoves(legion,
                legion.getCurrentHex(), roll);

            if (set.size() > 0)
            {
                boolean couldMove = false;
                for (MasterHex targetHex : set)
                {
                    // The set of moves includes still hexes occupied by our own legions. 
                    List<LegionClientSide> targetOwnLegions = client
                        .getFriendlyLegions(targetHex, player);
                    if (targetOwnLegions.size() == 0)
                    {
                        couldMove = true;
                    }
                }
                if (couldMove)
                {
                    movableLegions.add(legion);
                }
            }
        }

        if (movableLegions.size() == 0)
        {
            // No valid move for any legion. Fine.
            return false;
        }

        // OK, now decide which of them to move - the smallest one.

        int minValue = 0;
        LegionClientSide minValueLegion = null;
        LegionClientSide titanLegion = null;

        for (LegionClientSide legion : movableLegions)
        {
            int value = legion.getPointValue();

            if (legion.hasTitan())
            {
                titanLegion = legion;
            }

            else if (value < minValue || minValueLegion == null)
            {
                Set<MasterHex> set = client.getMovement().listNormalMoves(
                    legion, legion.getCurrentHex(), roll);
                if (set.size() > 0)
                {
                    minValue = value;
                    minValueLegion = legion;
                }
            }
        }

        // Arrrgggh. Have to move Titan legion :-(
        if (minValueLegion == null && titanLegion != null)
        {
            logger.log(Level.FINER, "Rational AI, forced single move: "
                + " have to move Titan legion :-(");
            minValueLegion = titanLegion;
        }

        assert minValueLegion != null : "There should be at least one legion we can move";

        // Now decide where we move this unlucky one to:
        Set<MasterHex> minValueMoves = client.getMovement().listNormalMoves(
            minValueLegion, minValueLegion.getCurrentHex(), roll);

        int bestValue = -1;
        MasterHex bestHex = null;
        for (MasterHex targetHex : minValueMoves)
        {
            List<LegionClientSide> targetOwnLegions = client
                .getFriendlyLegions(targetHex, player);
            if (targetOwnLegions.size() == 0)
            {
                int value = evaluateMove(minValueLegion, targetHex,
                    RECRUIT_TRUE, 2, true);
                if (value > bestValue || bestValue == -1)
                {
                    bestValue = value;
                    bestHex = targetHex;
                }
            }
        }

        if (bestHex == null)
        {
            logger.log(Level.SEVERE, "Forced single moves remain, "
                + "moveIterator left bestHex for minValueLegion"
                + minValueLegion + " null ?");
            return false;
        }

        boolean wentOk = client.doMove(minValueLegion, bestHex);
        if (wentOk)
        {
            return true;
        }
        else
        {
            // This should never happen. Most likely we get then a NAK...
            logger.log(Level.SEVERE, "Forced single moves remain, "
                + "but client rejects moving legion " + minValueLegion
                + " to " + bestHex);
            return false;
        }
    }

    private class MoveFinder
    {
        private List<LegionBoardMove> bestMove = null;
        private double bestScore;
        private boolean mustMove;
        private long nodesExplored = 0;

        // initial score is some value that should be smaller that the
        // worst move
        private final static double INITIAL_SCORE = -1000000;
        private final static double NO_MOVE_EXISTS = 2 * INITIAL_SCORE;

        public List<LegionBoardMove> findOptimalMove(
            List<List<LegionBoardMove>> all_legionMoves, boolean mustMove)
        {
            bestMove = new ArrayList<LegionBoardMove>(); // just in case there is no legal move
            bestScore = INITIAL_SCORE;
            this.mustMove = mustMove;
            nodesExplored = 0;

            logger.log(Level.FINEST, "Starting computing the best move");

            Timer fomTimer = setupTimer();

            Collections.shuffle(all_legionMoves, random);

            branchAndBound(new ArrayList<LegionBoardMove>(), all_legionMoves,
                0);

            fomTimer.cancel();

            logger
                .log(Level.FINEST, "Total nodes explored = " + nodesExplored);

            for (Iterator<LegionBoardMove> it = bestMove.iterator(); it
                .hasNext();)
            {
                if (it.next().noMove)
                {
                    it.remove();
                }
            }

            return bestMove;
        }

        private double moveValueBound(
            List<List<LegionBoardMove>> availableMoves)
        {
            double ret = 0;
            for (List<LegionBoardMove> moves : availableMoves)
            {
                if (moves.isEmpty())
                {
                    // at least one peice has no legal moves
                    return NO_MOVE_EXISTS;
                }
                // each move list is assmed t be sorted, so just use the first
                ret += (moves.get(0)).val;
            }
            return ret;
        }

        /**
         * checks if a move is valid, and if so returns the moves in
         * an executeable sequence. The legios not moving are not part
         * of bestMove. Returns null if the move is not valid.
         * @param performedMoves
         * @return
         */
        private List<LegionBoardMove> getValidMove(
            List<LegionBoardMove> performedMoves)
        {
            if (mustMove)
            {
                boolean moved = false;
                for (LegionBoardMove lm : performedMoves)
                {
                    if (!lm.noMove)
                    {
                        moved = true;
                        break;
                    }
                }
                if (!moved)
                {
                    return null;
                }
            }

            Map<MasterHex, List<Legion>> occupiedHexes = new Hashtable<MasterHex, List<Legion>>();
            Set<MasterHex> newOccupiedHexes = new HashSet<MasterHex>();
            List<LegionBoardMove> newBestMove = new ArrayList<LegionBoardMove>();
            for (LegionBoardMove lm : performedMoves)
            {
                List<Legion> markers = occupiedHexes.get(lm.fromHex);
                if (markers == null)
                {
                    markers = new ArrayList<Legion>();
                    occupiedHexes.put(lm.fromHex, markers);
                }
                markers.add(lm.legion);
            }

            boolean moved = true;
            while (moved)
            {
                moved = false;
                // move all pieces that has an open move
                for (Iterator<LegionBoardMove> it = performedMoves.iterator(); it
                    .hasNext();)
                {
                    LegionBoardMove lm = it.next();
                    List<Legion> destConflicts = occupiedHexes.get(lm.toHex);
                    if (destConflicts == null
                        || destConflicts.size() == 0
                        || (destConflicts.size() == 1 && lm.legion
                            .equals(destConflicts.get(0))))
                    { // this piece has an open move
                        List<Legion> markers = occupiedHexes.get(lm.fromHex);
                        markers.remove(lm.legion);
                        if (!newOccupiedHexes.add(lm.toHex))
                        { // two or more pieces are moving to the same spot.
                            return null;
                        }
                        newBestMove.add(lm);
                        it.remove();
                        moved = true;
                    }
                }
            }

            // if there are moves left in perfornmedMoves
            // check if there is a cycle or a split did not separate
            for (LegionBoardMove lm : performedMoves)
            {
                if (!lm.noMove)
                {
                    // A marker that cant move at this point
                    // means a cycle exists
                    return null;
                }
                // now we know we have a split legion not moving since
                // that is the only way to have a noMove conflict
                Set<MasterHex> moves = client.getMovement().listNormalMoves(
                    lm.legion, lm.legion.getCurrentHex(),
                    client.getMovementRoll());
                for (MasterHex dest : moves)
                {
                    if (!(newOccupiedHexes.contains(dest) || occupiedHexes
                        .containsKey(dest)))
                    {
                        // this legion has an open move it should have taken
                        return null;
                    }
                }

            }

            return newBestMove;
        }

        private void branchAndBound(List<LegionBoardMove> performedMoves,
            List<List<LegionBoardMove>> availableMoves, double currentValue)
        {
            nodesExplored++;
            if (timeIsUp)
            {
                if (bestMove == null)
                {
                    // logger.log(Level.FINEST, "no legal move found yet, not able to time out");
                }
                else
                {
                    logger.log(Level.FINEST,
                        "handleVoluntaryMoves() time up after "
                            + nodesExplored + " Nodes Explored");
                    return;
                }
            }

            // bounding step
            if (currentValue + moveValueBound(availableMoves) <= bestScore)
            {
                return;
            }

            if (availableMoves.isEmpty())
            { // this is a leaf check valdity of move
                // could be moved to a function
                List<LegionBoardMove> newBestMove = getValidMove(performedMoves);
                if (newBestMove != null)
                {
                    bestMove = newBestMove;
                    bestScore = currentValue;
                    logger.log(Level.FINEST, "New best move found: ("
                        + currentValue + ") " + bestMove);
                }
                else
                {

                    /*                logger.log(Level.FINEST, 
                     "Illigal move: ("
                     + currentValue
                     + " : "
                     + bestScore
                     + ") "
                     + performedMoves);*/
                }
                return;
            }

            List<LegionBoardMove> nextMoves = availableMoves.get(0);
            for (LegionBoardMove lm : nextMoves)
            {
                if (!lm.noMove
                    && checkNewCycle(lm.fromHex, lm.toHex, performedMoves))
                {
                    continue;
                }
                List<LegionBoardMove> newPerformedMoves = new ArrayList<LegionBoardMove>(
                    performedMoves);
                newPerformedMoves.add(lm);
                branchAndBound(newPerformedMoves, removeHeadAndConflicts(
                    availableMoves, lm), currentValue + lm.val);
            }

        }

        /**
         * checkes if there is a path from 'from' to target, using
         * the moves in the list. This is used to see if there are cycles
         * in the moves, when you ad a move from 'target' to 'from'
         * @param target
         * @param from
         * @param moves
         * @return
         */
        private boolean checkNewCycle(MasterHex target, MasterHex from,
            List<LegionBoardMove> moves)
        {
            for (LegionBoardMove lm : moves)
            {
                if (lm.fromHex.equals(from))
                {
                    if (lm.toHex.equals(target))
                    {
                        return true;
                    }
                    if (checkNewCycle(target, lm.toHex, moves))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        private List<List<LegionBoardMove>> removeHeadAndConflicts(
            List<List<LegionBoardMove>> availableMoves, LegionBoardMove lm)
        {
            List<List<LegionBoardMove>> newAvailableMoves = new ArrayList<List<LegionBoardMove>>();

            for (Iterator<List<LegionBoardMove>> it = availableMoves
                .listIterator(1); it.hasNext();)
            {
                List<LegionBoardMove> moves = it.next();
                List<LegionBoardMove> newMoves = new ArrayList<LegionBoardMove>();
                if (lm.noMove && !moves.isEmpty()
                    && lm.fromHex.equals((moves.get(0)).fromHex))
                {
                    // special case, these two legions are split, make
                    // sure to try the move off moves first
                    LegionBoardMove stayMove = null;
                    for (LegionBoardMove move : moves)
                    {
                        if (move.noMove)
                        {
                            stayMove = move;
                        }
                        else
                        {
                            newMoves.add(move);
                        }
                    }
                    if (stayMove != null)
                    { // there should be one, but just checking
                        newMoves.add(stayMove);
                        // make sure staymove is explored last
                    }
                }
                else
                {
                    for (LegionBoardMove move : moves)
                    {
                        if (!lm.toHex.equals(move.toHex))
                        {
                            newMoves.add(move);
                        }
                    }
                }
                if (newMoves.size() == 1)
                {
                    // if it only has one possible then consider it first
                    newAvailableMoves.add(0, newMoves);
                }
                else
                {
                    newAvailableMoves.add(newMoves);
                }
            }

            return newAvailableMoves;
        }
    }

    final int TITAN_SURVIVAL = 20; // safety margin for Titan

    // Compute risk of being attacked
    // Value returned is expected point value cost
    double hexRisk(Legion legion, MasterHex hex, boolean invert)
    {
        double risk = 0.0;

        // ignore all fear of attack on first turn
        if (client.getTurnNumber() < 2)
        {
            return 0.0;
        }

        // logger.log(Level.FINEST, "considering risk of " + legion + " in " + hex);
        int roll;
        int result = 0;

        for (roll = 1; roll <= 6; roll++)
        {
            List<Legion> enemies = enemyAttackMap[roll].get(hex);

            if (enemies == null)
            {
                continue;
            }

            double worst_result_this_roll = 0.0;

            for (Legion enemy : enemies)
            {
                result = evaluateCombat(enemy, legion, hex);

                if (invert)
                {
                    result = -result;
                }

                if (result > worst_result_this_roll)
                {
                    worst_result_this_roll = result;
                }
            }
            risk -= worst_result_this_roll;
        }
        risk /= 6.0;

        if (invert)
        {
            risk = -risk;
        }

        // logger.log(Level.FINEST, "compute final attack risk as " + r3(risk));
        return risk;
    }

    int evaluateCombat(Legion attacker, Legion defender, MasterHex hex)
    {
        if (attacker.getPlayer().equals(defender.getPlayer()))
        {
            return 0;
        }

        final int defenderPointValue = ((LegionClientSide)defender)
            .getPointValue();
        final int attackerPointValue = ((LegionClientSide)attacker)
            .getPointValue();
        final BattleResults result = estimateBattleResults(attacker, defender,
            hex);

        int value = (int)result.getExpectedValue();

        if (!I_HATE_HUMANS)
        {
            // In rational AI mode do not reward early titan attacks
            if (client.getTurnNumber() < 5)
            {
                return value - 100;
            }
        }

        boolean defenderTitan = defender.hasTitan();

        if (result.getExpectedValue() > 0)
        {
            if (attacker.hasTitan())
            {
                // unless we can win the game with this attack
                if (defenderTitan)
                {
                    if (I_HATE_HUMANS)
                    {
                        // do it and win the game, there is only 1 human
                        value = 1000 + (int)result.getExpectedValue() * 1000;
                    }
                    else if (client.getNumLivingPlayers() == 2
                        && (attackerPointValue - result.getAttackerDead()) > TITAN_SURVIVAL)
                    {
                        // do it and win the game
                        value = 1000 + (int)result.getExpectedValue() * 1000;
                    }
                    else if (result.getAttackerDead() < attackerPointValue / 2
                        && (attackerPointValue - result.getAttackerDead()) > TITAN_SURVIVAL)
                    {
                        // our titan stack will be badly damaged
                        // but it is worth it
                        value = 100 + (int)result.getExpectedValue() * 100;
                    }
                    else
                    {
                        // ack! we'll fuck up our titan group
                        // use metric below so that if we have no choice
                        // but to attack we pick the least losing battle
                        value = result.getAttackerDead() * -100;
                    }
                }
                else if (result.getAttackerDead() > attackerPointValue / 2)
                // (1/4) will usually be about 3 pieces since titan
                // will be large part of value
                {
                    // ack! we'll fuck up our titan group
                    // use metric below so that if we have no choice but to
                    // attack we pick the least losing battle
                    value = -100 + result.getAttackerDead() * -100;
                }
                else
                // win with minimal loss
                {
                    // value = result.getExpectedValue(); // default value
                }
            }
            else if (defenderTitan)
            {
                value = (1000 + (int)result.getExpectedValue() * 1000)
                    / client.getNumLivingPlayers();
            }
        }
        else
        // we expect to lose on this battle.
        // but if the enemy is a titan stack it may be worth it
        {
            if (!attacker.hasTitan() && defenderTitan)
            {
                // gun for the titan stack if we can knock out
                // more than 80% of the value
                if (result.getDefenderDead() > defenderPointValue * .5)
                {
                    // value should be proportional to amount of Titan stack
                    // killed since we may be able to attack with more
                    // than one legion
                    value = result.getDefenderDead() * 100
                        / client.getNumLivingPlayers() / 2;
                }
            }
            else if (attacker.hasTitan())
            {
                // ack! we'll kill our titan group
                // use metric below so that if we have no choice but to attack
                // we pick the least losing battle

                value = (-1000 + (int)result.getExpectedValue() * 1000)
                    / client.getNumLivingPlayers();
            }
        }

        // apply penalty to attacks if we have few legions
        // Don't reward titan attacks with few stacks
        int attackerLegions = ((PlayerClientSide)attacker.getPlayer())
            .getNumLegions();
        if (attackerLegions < 5 && !I_HATE_HUMANS)
        {
            return value - (result.getAttackerDead() / attackerPointValue)
                * 1000;
        }

        return value;
    }

    // evaluate the attack payoff of moving into the hex given by 'hex'.
    // This will typically be negative to indicate that we might lose,
    // zero if the hex is empty, or positive if the hex is occupied
    // by a weak legion
    static final int RECRUIT_FALSE = 0; // don't allow recruiting by attacker
    static final int RECRUIT_TRUE = 1; // allow recruiting by attacker
    static final int RECRUIT_AT_7 = 2; // allow recruiting by attacker 7 high

    int evaluateHexAttack(Legion attacker, MasterHex hex, int canRecruitHere)
    {
        int value = 0;
        // consider making an attack
        final Legion defender = client.getFirstEnemyLegion(hex, attacker
            .getPlayer());

        if (defender != null)
        {
            if (!attacker.getPlayer().equals(defender.getPlayer()))
            {
                value = evaluateCombat(attacker, defender, hex);
            }

            if (I_HATE_HUMANS && !isHumanLegion(defender)
                && !isHumanLegion(attacker))
            {
                // try not to attack other AIs
                if (value > -50)
                {
                    value = -50;
                }

                if ((attacker).hasTitan())
                {
                    value -= 100;
                }

                if ((defender).hasTitan())
                {
                    value -= 100;
                }
            }

            return value;
        }

        if ((canRecruitHere == RECRUIT_TRUE && (attacker).getHeight() < 7)
            || canRecruitHere == RECRUIT_AT_7)
        {
            if (!(attacker).hasTitan())
            {
                value += recruitValue(attacker, hex, null, hex.getTerrain());
            }
            else
            {
                // prefer recruiting with Titan legion
                value += recruitValue(attacker, hex, null, hex.getTerrain()) * 1.1;
            }
        }

        return value;
    }

    /** Memoizing wrapper for evaluateMoveInner */
    private int evaluateMove(LegionClientSide legion, MasterHex hex,
        int canRecruitHere, int depth, boolean addHexRisk)
    {
        String sep = "~";
        String key = "" + legion + sep + hex + sep + canRecruitHere + sep
            + depth + sep + addHexRisk;
        int score;
        Integer val;
        if (evaluateMoveMap.containsKey(key))
        {
            val = evaluateMoveMap.get(key);
            score = val.intValue();
        }
        else
        {
            score = evaluateMoveInner(legion, hex, canRecruitHere, depth,
                addHexRisk);
            val = Integer.valueOf(score);
            evaluateMoveMap.put(key, val);
        }
        return score;
    }

    // cheap, inaccurate evaluation function.  Returns an expected value for
    // moving this legion to this hex.  The value defines a distance
    // metric over the set of all possible moves.
    private int evaluateMoveInner(LegionClientSide legion, MasterHex hex,
        int canRecruitHere, int depth, boolean normalHexRisk)
    {

        // evaluateHexAttack includes recruit value
        double value = evaluateHexAttack(legion, hex, canRecruitHere);

        // if we get killed at this hex there can be no further musters
        if (value < 0)
        {
            return (int)value;
        }

        // consider what we might be able to recruit next turn, from here
        double nextTurnValue = 0.0;
        double stay_at_hex = 0.0;

        boolean invert = false;

        if (!normalHexRisk)
        {
            // invert = true;
        }

        // value of staying at hex we move to
        // i.e. what is risk we will be attacked if we stay at this hex
        // (if invert = true, this becomes value of potentially
        // attacking something at this hex)
        stay_at_hex = hexRisk(legion, hex, invert);

        // when we move to this hex we may get attacked and not have
        // a next turn
        value += stay_at_hex;

        // if we are very likely to be attacked and die here
        // then just return value
        if (value < -10)
        {
            return (int)value;
        }

        if (depth == 0)
        {
            return (int)value;
        }

        // squares that are further away are more likely to be blocked
        double DISC_FACTOR = 1.0;
        double discount = DISC_FACTOR;

        // value of next turn
        for (int roll = 1; roll <= 6; roll++)
        {
            Set<MasterHex> normal_moves = client.getMovement()
                .listNormalMoves(legion, hex, roll);
            Set<MasterHex> tele_moves = client.getMovement()
                .listTeleportMoves(legion, hex, roll);
            double bestMoveVal = stay_at_hex; // can always stay here
            boolean no_attack = false;

            for (int i = 0; i < 2; i++)
            {
                Iterator<MasterHex> nextMoveIt;
                if (i == 0)
                {
                    nextMoveIt = normal_moves.iterator();
                    no_attack = false;
                }
                else
                {
                    nextMoveIt = tele_moves.iterator();
                    no_attack = true;
                }

                while (nextMoveIt.hasNext())
                {
                    MasterHex nextHex = nextMoveIt.next();

                    double nextMoveVal = evaluateMove(legion, nextHex,
                        RECRUIT_AT_7, depth - 1, no_attack);

                    if (nextMoveVal > bestMoveVal)
                    {
                        bestMoveVal = nextMoveVal;
                    }
                }
            }
            bestMoveVal *= discount;
            nextTurnValue += bestMoveVal;
            // squares that are further away are more likely to be blocked
            discount *= DISC_FACTOR;
        }

        nextTurnValue /= 6.0; // 1/6 chance of each happening
        value += 0.9 * nextTurnValue; // discount future moves some

        //logger.log(Level.FINEST, "depth " + depth + " EVAL " + legion +
        //    (canRecruitHere != RECRUIT_FALSE ? " move to " : " stay in ") +
        //    hex + " = " + r3(value));

        return (int)value;
    }

    boolean isHumanLegion(Legion legion)
    {
        return !((PlayerClientSide)legion.getPlayer()).isAI();
    }

    static class BattleResults
    {
        private final double ev; // expected value of attack
        private final int att_dead;
        private final int def_dead;
        private List<String> log = new ArrayList<Str
