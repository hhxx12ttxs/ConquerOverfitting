package net.sf.colossus.client;


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

import net.sf.colossus.server.Constants;
import net.sf.colossus.server.Creature;
import net.sf.colossus.util.MultiSet;
import net.sf.colossus.xmlparser.TerrainRecruitLoader;


/**
 * Simple implementation of a Titan AI
 * @version $Id: RationalAI.java 2820 2007-12-05 19:23:51Z cleka $
 * @author Bruce Sherrod, David Ripton
 * @author Romain Dolbeau
 * @author Corwin Joy, extensively rewritten on 02-Oct-2003
 */
public class RationalAI extends SimpleAI implements AI
{
    private static final Logger logger =
        Logger.getLogger(RationalAI.class.getName());

    boolean I_HATE_HUMANS = false;
    private LinkedList legionsToSplit = new LinkedList();
    private Map[] enemyAttackMap;
    private Map evaluateMoveMap = new HashMap();
    private List bestMoveList;
    private Iterator bestMoveListIter;

    public RationalAI(Client client)
    {
        super(client);
    }

    private static double r3(double value)
    {
        return Math.round(1000. * value) / 1000.;
    }

    public boolean split()
    {
        // Refresh these once per turn.
        enemyAttackMap = buildEnemyAttackMap(client.getPlayerInfo());
        evaluateMoveMap.clear();

        legionsToSplit.clear();

        PlayerInfo player = client.getPlayerInfo();
        Iterator it = player.getLegionIds().iterator();
        while (it.hasNext())
        {
            String markerId = (String)it.next();
            legionsToSplit.add(markerId);
        }
        return fireSplits();
    }

    /** Return true if done with all splits and callbacks */
    private boolean fireSplits()
    {
        logger.log(Level.FINEST, "RationalAI.fireSplits " + legionsToSplit);

        //safe to cache the player out of the loop, ref to mutable object
        PlayerInfo player = client.getPlayerInfo();

        while (!legionsToSplit.isEmpty())
        {
            if (player.getNumMarkers() == 0)
            {
                logger.log(Level.FINEST, "No more splits. No markers left.");
                return true; //early exit, out of markers
            }

            String markerId = (String)legionsToSplit.removeFirst();
            if (!splitOneLegion(player, markerId))
            {
                return false; //early exit, we've decided we won't finish
            }
        }

        return true; //succesfully looked at all splits
    }

    // XXX Undoing a split could release the marker needed to do another
    // split, so we need to synchronize access.
    /** If parentId and childId are null, this is a callback to
     * an undo split */
    public boolean splitCallback(String parentId, String childId)
    {
        logger.log(Level.FINEST,
            "RationalAI.splitCallback " + parentId + " " + childId);

        return splitOneLegionCallback(parentId, childId) && fireSplits();
    }

    // Compute the expected value of a split legion
    // If we want to compute just a single legion, pass null for
    // the child_legion
    private double expectedValueSplitLegion(LegionInfo legion,
        LegionInfo child_legion)
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
            logger.log(Level.FINEST, "expectedValueSplitLegion(), value of " +
                "staying here for split legion1 " + stay_here_value1);
            logger.log(Level.FINEST, "expectedValueSplitLegion(), value of " +
                "staying here for split legion1 " + stay_here_value2);
        }
        else
        {
            logger.log(Level.FINEST, "expectedValueSplitLegion(), value of " +
                "staying here for unsplit legion " + stay_here_value1);
            stay_here_value2 = 0.0;
        }

        for (int roll = 1; roll <= 6; roll++)
        {
            logger.log(Level.FINEST, "expectedValueSplitLegion: Roll " + roll);
            Set moves = client.getMovement().listAllMoves(legion,
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
            Iterator moveIt = moves.iterator();
            int move_i = 0;

            while (moveIt.hasNext())
            {
                String hexLabel = (String)moveIt.next();
                MasterHex hex = MasterBoard.getHexByLabel(hexLabel);
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
    boolean splitOneLegion(PlayerInfo player, String markerId)
    {
        logger.log(Level.FINEST, "splitOneLegion()");

        LegionInfo legion = client.getLegionInfo(markerId);

        // Allow aggressive splits - especially early in the game it is better
        // to split more often -- this should get toned down later in the
        // game by the scooby snack factor
        if (legion.getHeight() < 6)
        {
            logger.log(Level.FINEST, "No split: height < 6");
            return true;
        }

        double stay_here_risk = hexRisk(legion, legion.getCurrentHex(),
            false);

        boolean at_risk = false;
        if (stay_here_risk > 0 || legion.hasTitan())
        {
            at_risk = true;
        }

        if (at_risk && legion.getHeight() < 7)
        {
            logger.log(Level.FINEST, "No split: height < 7 and legion at risk");
            return true;
        }

        StringBuffer results = new StringBuffer();
        boolean hasMustered = false;

        MusteredCreatures mc = chooseCreaturesToSplitOut(legion, at_risk);
        List creatures = mc.creatures;

        hasMustered = mc.mustered;
        Iterator it = creatures.iterator();
        int child_value = 0;

        while (it.hasNext())
        {
            Creature creature = (Creature)it.next();

            child_value += creature.getPointValue();
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
        String newMarkerId = pickMarker(player.getMarkersAvailable(),
            player.getShortColor());

        if (newMarkerId == null)
        {
            //should never happen now that we check in fireSplits
            logger.log(Level.FINEST, "No split.  No markers available.");
            return true;
        }

        logger.log(Level.FINEST, "Wait for split callback");
        client.doSplit(legion.getMarkerId(), newMarkerId, results.toString());
        return false;
    }

    /** Return true if done, false if waiting for undo split */
    private boolean splitOneLegionCallback(String markerId, String newMarkerId)
    {
        if (markerId == null && newMarkerId == null)
        {
            return true; //nothing to do. splitCallback() depends on this exit
        }

        LegionInfo legion = client.getLegionInfo(markerId);
        logger.log(Level.FINEST, "Split complete");

        if (client.getTurnNumber() == 1)
        { // first turn
            logger.log(Level.FINEST, "First turn split");
            return true;
        }

        LegionInfo child_legion = client.getLegionInfo(newMarkerId);

        // Compute split value
        logger.log(Level.FINEST, "splitOneLegion(): Expected value with split");
        double split_value = expectedValueSplitLegion(legion, child_legion);
        // expected value of split

        // find expected value of no split
        logger.log(Level.FINEST,
            "splitOneLegionCallback(): Expected value with no split");
        double no_split_value = 0.0;
        if (client.getTurnNumber() > 1)
        {
            no_split_value = expectedValueSplitLegion(legion, null);
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
        if (split_value * 1.02 < no_split_value ||
            split_value < -20)
        {
            // Undo the split
            client.undoSplit(newMarkerId);
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
    double recruitValue(LegionInfo legion, String hexLabel,
        LegionInfo enemy, String terrain)
    {
        int value = 0;

        // Allow recruits even at 7 high for purposes of calculating
        // the mobility value of a particular hex in evaluateMove

        // Consider recruiting.
        List recruits = client.findEligibleRecruits(legion.getMarkerId(),
            hexLabel);

        if (!recruits.isEmpty())
        {
            Creature bestRecruit = (Creature)recruits.get(recruits.size() - 1);

            value = ghrv(bestRecruit, legion, hintSectionUsed);
        }

        // Consider acquiring angels.
        if (enemy != null)
        {
            int pointValue = enemy.getPointValue();
            boolean wouldFlee = flee(enemy, legion);

            if (wouldFlee)
            {
                pointValue /= 2;
            }

            // should work with all variants
            int currentScore = legion.getPlayerInfo().getScore();
            int arv = TerrainRecruitLoader.getAcquirableRecruitmentsValue();
            int nextScore = ((currentScore / arv) + 1) * arv;

            Creature bestRecruit = null;

            while ((currentScore + pointValue) >= nextScore)
            {
                java.util.List ral =
                    TerrainRecruitLoader.getRecruitableAcquirableList(terrain,
                    nextScore);
                java.util.Iterator it = ral.iterator();

                while (it.hasNext())
                {
                    Creature tempRecruit = Creature.getCreatureByName(
                        (String)it.next());

                    if ((bestRecruit == null) ||
                        (ghrv(tempRecruit, legion, hintSectionUsed) >=
                        ghrv(bestRecruit, legion, hintSectionUsed)))
                    {
                        bestRecruit = tempRecruit;
                    }
                }
                nextScore += arv;
            }

            // add value of any angels
            if (bestRecruit != null)
            {
                value += ghrv(bestRecruit, legion, hintSectionUsed);
            }
        }

        return value;
    }

    // Given a list of creatures sorted by value, figure out which ones are
    // redundant / have mustered.
    // Removes any creatures that have mustered from the original sorted list
    List removeMustered(List sortedCreatures)
    {
        // Look at 4 lowest valued creatures
        // Try to pull out pair that has already mustered.
        List creaturesThatHaveMustered = new ArrayList();

        outer:
        for (int index1 = 0; index1 < 4 && index1 < sortedCreatures.size();
            index1++)
        {
            String critter1 = ((Creature)sortedCreatures.get(
                index1)).getName();

            for (int index2 = index1 + 1; index2 < sortedCreatures.size();
                index2++)
            {
                String critter2 = ((Creature)sortedCreatures.get(
                    index2)).getName();

                if (critter1 == critter2)
                { // mustering yourself does not count
                    continue;
                }
                if (TerrainRecruitLoader.getRecruitGraph().
                    isRecruitDistanceLessThan(critter1, critter2, 2))
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

    public class CompCreaturesByValueName implements Comparator
    {
        private LegionInfo legion;

        public CompCreaturesByValueName(LegionInfo l)
        {
            legion = l;
        }

        public final int compare(Object o1, Object o2)
        {
            int val1 = ghrv((Creature)o1, legion, hintSectionUsed);
            int val2 = ghrv((Creature)o2, legion, hintSectionUsed);

            if (val1 < val2)
            {
                return -1;
            }
            if (val1 > val2)
            {
                return 1;
            }
            // val1 == val2, compare string
            return ((Creature)o1).getName().compareTo((
                (Creature)o2).getName());
        }
    }

    // Sort creatures first by value then by name.
    // Exclude titan.
    List sortCreaturesByValueName(List Creatures, LegionInfo legion)
    {
        List sortedCreatures = new ArrayList();
        Iterator critterIt = Creatures.iterator();

        // copy list excluding titan
        while (critterIt.hasNext())
        {
            String name = (String)critterIt.next();
            Creature critter = Creature.getCreatureByName(name);

            // Never split out the titan.
            if (critter.isTitan())
            {
                continue;
            }
            sortedCreatures.add(critter);
        }
        Collections.sort(sortedCreatures, new CompCreaturesByValueName(
            legion));
        return sortedCreatures;
    }

    // Count number of creatures in the stack that have mustered
    int countMustered(LegionInfo legion)
    {
        List sortedCreatures = sortCreaturesByValueName(legion.getContents(),
            legion);
        List creaturesThatHaveMustered = removeMustered(sortedCreatures);

        return creaturesThatHaveMustered.size();
    }

    class MusteredCreatures
    {
        public boolean mustered;
        public List creatures;
        MusteredCreatures(boolean m, List c)
        {
            mustered = m;
            creatures = c;
        }
    }

    /** Decide how to split this legion, and return a list of
     *  Creatures to remove + status flag indicating if these
     creatures have mustered or not*/
    MusteredCreatures chooseCreaturesToSplitOut(LegionInfo legion,
        boolean at_risk)
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
            List creatures = doInitialGameSplit(legion.getHexLabel());

            return new MusteredCreatures(true, creatures);
        }

        logger.log(Level.FINEST,
            "sortCreaturesByValueName() in chooseCreaturesToSplitOut");

        List sortedCreatures = sortCreaturesByValueName(legion.getContents(),
            legion);

        logger.log(Level.FINEST,
            "Sorted stack - minus titan: " + sortedCreatures);
        // Look at lowest valued creatures
        // Try to pull out pair that has already mustered.
        logger.log(Level.FINEST,
            "removeMustered() in chooseCreaturesToSplitOut");
        List creaturesThatHaveMustered = removeMustered(sortedCreatures);

        logger.log(Level.FINEST,
            "List of mustered creatures: " + creaturesThatHaveMustered);
        boolean hasMustered = false;

        if (creaturesThatHaveMustered.size() < 1)
        {
            hasMustered = false;
        }
        else
        {
            hasMustered = true;
        }

        List creaturesToRemove = new ArrayList();

        // Try to pull out pair that has already mustered.
        logger.log(Level.FINEST,
            "build final split list in chooseCreaturesToSplitOut");
        Iterator sortIt = creaturesThatHaveMustered.iterator();
        boolean split_all_mustered = false;

        /*
         if (!at_risk)
         {
         split_all_mustered = true;
         }
         **/

        while (sortIt.hasNext() &&
            (creaturesToRemove.size() < 2 || split_all_mustered) &&
            creaturesToRemove.size() < 4)
        {
            Creature critter = (Creature)sortIt.next();
            creaturesToRemove.add(critter);
        }

        // If we have 3 mustered creatures, check if we have 3
        // Centaur, Ogre, Cyclops, Troll, Lion.  If so, try to keep the
        // 3 together for maximum mustering potential
        // it is a bit aggressive to keep trying to do 3/4 splits
        // but it seems to give a better result
        if (sortIt.hasNext() && !at_risk)
        {
            Creature first_remove = (Creature)creaturesToRemove.get(0);
            Creature critter = (Creature)sortIt.next();
            String s_first = first_remove.getName();
            String s_critter = critter.getName();

            if (s_first.compareTo(s_critter) == 0) // 3 identical, due to sort
            {
                if (s_first.compareTo("Centaur") == 0 ||
                    s_first.compareTo("Ogre") == 0 ||
                    s_first.compareTo("Cyclops") == 0 ||
                    s_first.compareTo("Troll") == 0 ||
                    s_first.compareTo("Lion") == 0
                    )
                {
                    // remove the 3rd creature
                    creaturesToRemove.add(critter);
                    logger.log(Level.FINEST, "Triple found!");
                    logger.log(Level.FINEST,
                        "Creatures to remove: " + creaturesToRemove);
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
            Creature critter = (Creature)sortIt.next();

            creaturesToRemove.add(critter);
        }
        logger.log(Level.FINEST, "Creatures to remove: " + creaturesToRemove);

        return  new MusteredCreatures(hasMustered, creaturesToRemove);
    }

    // little helper class to store possible moves by legion
    private class LegionBoardMove
    {
        final String markerId;
        final String fromHex;
        final String toHex;
        final double val;
        final boolean noMove;

        LegionBoardMove(String markerId, String fromHex, String toHex,
            double val, boolean noMove)
        {
            this.markerId = markerId;
            this.fromHex = fromHex;
            this.toHex = toHex;
            this.val = val;
            this.noMove = noMove;
        }

        public String toString()
        {
            return markerId + " to " + toHex;
        }
    }

    /** Return true if we need to run this method again after the server
     *  updates the client with the results of a move or mulligan. */
    public boolean masterMove()
    {
        logger.log(Level.FINEST, "This is RationalAI.");
        PlayerInfo player = client.getPlayerInfo();

        if (enemyAttackMap==null)
        {
            // special code to allow game to reload prperly if saved
            // during AI move
            enemyAttackMap = buildEnemyAttackMap(client.getPlayerInfo());
        }

        // consider mulligans
        if (handleMulligans(player))
        {
            return true;
        }

        boolean telePort = false;
        if (bestMoveList == null)
        {
            bestMoveList = new ArrayList();
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

            if (player.numLegionsMoved() == 0)
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

        LegionBoardMove lm = (LegionBoardMove)bestMoveListIter.next();
        boolean wentOk = client.doMove(lm.markerId, lm.toHex);
        if (!wentOk)
        {
            logger.log(Level.WARNING, "RationalAI.masterMove: client.doMove " +
                "returned false!!");
        }

        if (telePort)
        {
            bestMoveList = null;
        }
        return true;
    }

    private boolean findMoveList(List markerIds, List all_legionMoves,
        MultiSet occupiedHexes, boolean teleportsOnly)
    {
        boolean moved = false;
        Iterator it = markerIds.iterator();
        while (it.hasNext())
        {
            String markerId = (String)it.next();
            LegionInfo legion = client.getLegionInfo(markerId);
            if (legion.hasMoved())
            {
                moved = true;
                continue;
            }

            logger.log(Level.FINEST, "consider marker " + markerId);

            // compute the value of sitting still
            List legionMoves = new ArrayList();
            MasterHex hex = MasterBoard.getHexByLabel(
                legion.getCurrentHex().getLabel());
            double value = evaluateMove(legion, hex, RECRUIT_FALSE, 2, true);
            LegionBoardMove lmove = new LegionBoardMove(markerId,
                legion.getCurrentHex().getLabel(),
                legion.getCurrentHex().getLabel(), value, true);

            if (!teleportsOnly)
            {
                legionMoves.add(lmove);
                occupiedHexes.add(legion.getCurrentHex().getLabel());

                logger.log(Level.FINEST,
                    "value of sitting still at hex " + hex.getLabel() +
                    " : " + value);
            }

            // find the expected value of all moves for this legion
            Set set;
            if (!teleportsOnly)
            {
                // exclude teleport moves
                set = client.getMovement().listNormalMoves(legion,
                    legion.getCurrentHex(), client.getMovementRoll());
            }
            else
            {
                // only teleport moves
                set = client.getMovement().listTeleportMoves(legion,
                    legion.getCurrentHex(), client.getMovementRoll());
            }

            Iterator moveIterator = set.iterator();
            while (moveIterator.hasNext())
            {
                final String hexLabel = (String)moveIterator.next();
                hex = MasterBoard.getHexByLabel(hexLabel);
                value = evaluateMove(legion, hex, RECRUIT_TRUE, 2, true);

                logger.log(Level.FINEST,
                    "value hex " + hexLabel + " value: " + r3(value));

                lmove = new LegionBoardMove(markerId,
                    legion.getCurrentHex().getLabel(),
                    hexLabel, value, false);
                legionMoves.add(lmove);
            }

            // Sort moves in the order of descending value.
            Collections.sort(legionMoves, new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    LegionBoardMove L1 = (LegionBoardMove)o1;
                    LegionBoardMove L2 = (LegionBoardMove)o2;
                    return (int)L2.val - (int)L1.val; // want descending order
                }
            }
            );

            all_legionMoves.add(legionMoves);
        }
        return moved;
    }

    /** Return true if we moved something and need to be called again. */
    private boolean handleVoluntaryMoves(PlayerInfo player)
    {
        logger.log(Level.FINEST, "handleVoluntaryMoves()");

        boolean moved = false;
        List markerIds = player.getLegionIds();
        List all_legionMoves = new ArrayList();

        MultiSet occupiedHexes = new MultiSet();

        moved = findMoveList(markerIds, all_legionMoves, occupiedHexes, false);

        logger.log(Level.FINEST, "done computing move values for legions");

        logger.log(Level.FINEST, "raw best moves:");
        for (Iterator it = all_legionMoves.iterator(); it.hasNext();)
        {
            List moves = ((List)it.next());
            LegionBoardMove lbm = (LegionBoardMove)moves.get(0);
            logger.log(Level.FINEST,
                lbm.markerId + " to " + lbm.toHex + " value " + lbm.val);
        }

        // handle teleports
        // XXX
        // just take the best teleport.  this is not quite right
        // since it may stick the legion that does not get to
        // teleport with a really bad move.  it is not easy
        // to figure this out though.
        if (client.getMovementRoll() == 6)
        {
            List teleport_legionMoves = new ArrayList();
            MultiSet dummy = new MultiSet();
            findMoveList(markerIds, teleport_legionMoves, dummy, true);

            ListIterator legit = teleport_legionMoves.listIterator();
            LegionBoardMove best_move = new LegionBoardMove(
                "", "", "", 0, true);
            double best_value = 0;
            while (legit.hasNext())
            {
                List legionMoves = (List)legit.next();
                if (legionMoves.isEmpty())
                {
                    continue;  // not a teleporting legion
                }
                LegionBoardMove lm = (LegionBoardMove)legionMoves.get(0);
                if (lm.val > best_value)
                {
                    logger.log(Level.FINEST,
                        "found new teleport best move " + lm.markerId +
                        " to " + lm.toHex + " value " + lm.val);
                    best_value = lm.val;
                    best_move = lm;
                }
            }

            if (best_value > 0)
            {
                if (!best_move.noMove)
                {
                    logger.log(Level.FINEST,
                        "found teleport:  " + best_move.markerId +
                        " to " + best_move.toHex + " value " + best_move.val);
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

    private boolean handleForcedSplitMoves(PlayerInfo player)
    {
        int roll = client.getMovementRoll();
        ArrayList unsplitHexes = new ArrayList();

        /* Consider one hex after another. It is not necessary to look at
         * the individual legions, because
         * a) when looking at one hex, either in "this round" there is no valid
         *    move, or otherwise we move one.
         * b) it does not matter whether there are lords or not, because teleport
         *    moves are not mandatory
         * c) Once we did move one, we move, return true, get called again,
         *    then the list of labels is re-considered again.
         */
        Iterator it = player.getLegionIds().iterator();
        while (it.hasNext())
        {
            String markerId = (String)it.next();
            LegionInfo legion = client.getLegionInfo(markerId);
            String hexLabel = legion.getHexLabel();
            List friendlyLegions = client.getFriendlyLegions(hexLabel,
                player.getName());

            if (friendlyLegions.size() > 1)
            {
                unsplitHexes.add(hexLabel);
            }
        }
        Iterator itHexes = unsplitHexes.iterator();
        while (itHexes.hasNext())
        {
            String hexLabel = (String)itHexes.next();
            List friendlyLegions = client.getFriendlyLegions(hexLabel,
                player.getName());

            // pick just any legion for asking the getMovement
            Object[] legions = friendlyLegions.toArray();
            String anyLegionId = (String)legions[0];
            LegionInfo anyLegion = client.getLegionInfo(anyLegionId);

            if ( !client.getMovement().listNormalMoves(anyLegion,
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

                LegionInfo minLegion = anyLegion;
                int minSize = minLegion.getHeight();

                Iterator it2 = friendlyLegions.iterator();
                while (it2.hasNext())
                {
                    LegionInfo l = client.getLegionInfo((String)it2.next());
                    int size = l.getHeight();
                    if (size < minSize)
                    {
                        minSize = size;
                        minLegion = l;
                    }
                }

                String minMarkerId = minLegion.getMarkerId();
                Set set = client.getMovement().listNormalMoves(minLegion,
                    minLegion.getCurrentHex(), roll);

                if (set.size() == 0)
                {
                    // This should never happen. Most likely we get then a NAK...
                    logger.log(Level.SEVERE,
                        "Split legion " + minMarkerId +
                        " in hexlabel " + hexLabel +
                        " was supposed to have forced moves left, " +
                        " but normal moves list is empty?");
                    // anyway keep on going loop for checking next split legion
                }
                else
                {
                    Iterator moveIterator = set.iterator();
                    int bestValue = -1;
                    String bestHex = null;

                    while (moveIterator.hasNext())
                    {
                        String targetHex = (String)moveIterator.next();

                        // The set of moves includes still hexes occupied by our own legions. 
                        List targetOwnLegions = client.getFriendlyLegions(targetHex,
                            player.getName());
                        if (targetOwnLegions.size() == 0 )
                        {
                            MasterHex hex = MasterBoard.getHexByLabel(targetHex);
                            int value = evaluateMove(minLegion, hex,
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
                        logger.log(Level.FINEST,
                            "Forced split moves remain " +
                            "(no legal move for legion " + minMarkerId + ")");
                    }
                    else
                    {
                        boolean wentOk = client.doMove(minMarkerId, bestHex);
                        if (wentOk)
                        {
                            // ok, lets get called again to check if there are more.
                            return true;
                        }
                        else
                        {
                            // This should never happen. Most likely we get then a NAK...
                            logger.log(Level.SEVERE,
                                "Forced split moves remain, " +
                                "but client rejects moving marker " +
                                minMarkerId +
                                " from " + hexLabel + " to " + bestHex);
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
    private boolean handleForcedSingleMove(PlayerInfo player)
    {
        int roll = client.getMovementRoll();

        // first we have to find out those that can move at all:

        ArrayList movableLegions = new ArrayList();

        Iterator it = player.getLegionIds().iterator();
        while (it.hasNext())
        {
            String markerId = (String)it.next();
            LegionInfo legion = client.getLegionInfo(markerId);

            Set set = client.getMovement().listNormalMoves(legion,
                legion.getCurrentHex(), roll);

            if (set.size() > 0)
            {
                boolean couldMove = false;
                Iterator moveIterator = set.iterator();
                while (moveIterator.hasNext())
                {
                    String targetHex = (String)moveIterator.next();

                    // The set of moves includes still hexes occupied by our own legions. 
                    List targetOwnLegions = client.getFriendlyLegions(targetHex,
                        player.getName());
                    if (targetOwnLegions.size() == 0 )
                    {
                        couldMove = true;
                    }
                }
                if (couldMove)
                {
                    movableLegions.add(markerId);
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
        String minValueMarker = null;
        String titanMarker = null;

        it = movableLegions.iterator();
        while (it.hasNext())
        {
            String markerId = (String)it.next();
            LegionInfo legion = client.getLegionInfo(markerId);
            int value = legion.getPointValue();

            if (legion.hasTitan())
            {
                titanMarker = markerId;
            }

            else if (value < minValue || minValueMarker == null)
            {
                Set set = client.getMovement().listNormalMoves(legion,
                    legion.getCurrentHex(), roll);
                if (set.size() > 0)
                {
                    minValue = value;
                    minValueMarker = markerId;
                }
            }
        }

        // Arrrgggh. Have to move Titan legion :-(
        if (minValueMarker == null && titanMarker != null)
        {
            logger.log(Level.FINEST, "Rational AI, forced single move: " +
                " have to move Titan legion :-(");
            minValueMarker = titanMarker;
        }

        // Now decide where we move this unlucky one to:

        LegionInfo minLegion = client.getLegionInfo(minValueMarker);
        Set minValueMoves = client.getMovement().listNormalMoves(minLegion,
            minLegion.getCurrentHex(), roll);

        Iterator moveIterator = minValueMoves.iterator();
        int bestValue = -1;
        String bestHex = null;
        while (moveIterator.hasNext())
        {
            String targetHex = (String)moveIterator.next();
            MasterHex hex = MasterBoard.getHexByLabel(targetHex);

            List targetOwnLegions = client.getFriendlyLegions(targetHex,
                player.getName());
            if (targetOwnLegions.size() == 0 )
            {
                int value = evaluateMove(minLegion, hex, RECRUIT_TRUE, 2, true);
                if (value > bestValue || bestValue == -1)
                {
                    bestValue = value;
                    bestHex = targetHex;
                }
            }
        }

        if (bestHex == null)
        {
            logger.log(Level.SEVERE, "Forced single moves remain, " +
                "moveIterator left bestHex for minValueMarker" +
                minValueMarker + " null ?");
            return false;
        }

        boolean wentOk = client.doMove(minValueMarker, bestHex);
        if (wentOk)
        {
            return true;
        }
        else
        {
            // This should never happen. Most likely we get then a NAK...
            logger.log(Level.SEVERE, "Forced single moves remain, " +
                "but client rejects moving marker " + minValueMarker +
                " to " + bestHex);
            return false;
        }
    }

    private class MoveFinder
    {
        private List bestMove = null;
        private double bestScore;
        private boolean mustMove;
        private long nodesExplored = 0;

        // initial score is some value that should be smaller that the
        // worst move
        private final static double INITIAL_SCORE = -1000000;
        private final static double NO_MOVE_EXISTS = 2 * INITIAL_SCORE;

        public List findOptimalMove(List all_legionMoves, boolean mustMove)
        {
            bestMove = new ArrayList(); // just in case there is no legal move
            bestScore = INITIAL_SCORE;
            this.mustMove = mustMove;
            nodesExplored = 0;

            logger.log(Level.FINEST, "Starting computing the best move");

            Timer fomTimer = setupTimer();

            Collections.shuffle(all_legionMoves,random);

            branchAndBound(new ArrayList(), all_legionMoves, 0);

            fomTimer.cancel();
            
            logger.log(Level.FINEST, "Total nodes explored = " + nodesExplored);

            for (Iterator it = bestMove.iterator(); it.hasNext();)
            {
                if (((LegionBoardMove)it.next()).noMove)
                {
                    it.remove();
                }
            }

            return bestMove;
        }

        private double moveValueBound(List availableMoves)
        {
            double ret = 0;
            for (Iterator it = availableMoves.iterator(); it.hasNext();)
            {
                List moves = ((List)it.next());
                if (moves.isEmpty())
                {
                    // at least one peice has no legal moves
                    return NO_MOVE_EXISTS;
                }
                // each move list is assmed t be sorted, so just use the first
                ret += ((LegionBoardMove)moves.get(0)).val;
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
        private List getValidMove(List performedMoves)
        {
            if (mustMove)
            {
                boolean moved = false;
                for (Iterator it = performedMoves.iterator(); it.hasNext();)
                {
                    LegionBoardMove lm = (LegionBoardMove)it.next();
                    if (!lm.noMove)
                    {
                        moved = true;
                        break;
                    }
                }
                if (!moved) {
                    return null;
                }
            }

            Map occupiedHexes = new Hashtable();
            Set newOccupiedHexes = new HashSet();
            List newBestMove = new ArrayList();
            for (Iterator it = performedMoves.iterator(); it.hasNext();)
            {
                LegionBoardMove lm = (LegionBoardMove)it.next();
                List markers = (List)occupiedHexes.get(lm.fromHex);
                if (markers == null)
                {
                    markers = new ArrayList();
                    occupiedHexes.put(lm.fromHex, markers);
                }
                markers.add(lm.markerId);
            }

            boolean moved = true;
            while (moved)
            {
                moved = false;
                // move all pieces that has an open move
                for (Iterator it = performedMoves.iterator(); it.hasNext();)
                {
                    LegionBoardMove lm = (LegionBoardMove)it.next();
                    List destConflicts = (List)occupiedHexes.get(lm.toHex);
                    if (destConflicts == null || destConflicts.size() == 0 ||
                        (destConflicts.size() == 1 &&
                        lm.markerId.equals(destConflicts.get(0))))
                    { // this piece has an open move
                        List markers = (List)occupiedHexes.get(lm.fromHex);
                        markers.remove(lm.markerId);
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
            // check if there is a cycle or a split didnt seperate
            for (Iterator it = performedMoves.iterator(); it.hasNext();)
            {
                LegionBoardMove lm = (LegionBoardMove)it.next();
                if (!lm.noMove)
                {
                    // A marker that cant move at this point
                    // means a cycle exists
                    return null;
                }
                // now we know we have a split legion not moving since
                // that is the only way to have a noMove conflict
                LegionInfo legion = client.getLegionInfo(lm.markerId);
                Set moves =
                    client.getMovement().listNormalMoves(
                    legion,
                    legion.getCurrentHex(),
                    client.getMovementRoll());
                for (Iterator it2 = moves.iterator(); it2.hasNext();)
                {
                    // make sure move is blocked
                    String dest = (String)it2.next();
                    if (!(newOccupiedHexes.contains(dest) ||
                        occupiedHexes.containsKey(dest)))
                    {
                        // this legion has an open move it should have taken
                        return null;
                    }
                }

            }

            return newBestMove;
        }

        private void branchAndBound(
            List performedMoves,
            List availableMoves,
            double currentValue)
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
                        "handleVoluntaryMoves() time up after " + nodesExplored +
                        " Nodes Explored");
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
                List newBestMove = getValidMove(performedMoves);
                if (newBestMove != null)
                {
                    bestMove = newBestMove;
                    bestScore = currentValue;
                    logger.log(Level.FINEST,
                        "New best move found: (" + currentValue + ") " +
                        bestMove);
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

            List nextMoves = (List)availableMoves.get(0);
            for (Iterator it = nextMoves.iterator(); it.hasNext();)
            {
                LegionBoardMove lm = (LegionBoardMove)it.next();
                if (!lm.noMove &&
                    checkNewCycle(lm.fromHex, lm.toHex, performedMoves))
                {
                    continue;
                }
                List newPerformedMoves = new ArrayList(performedMoves);
                newPerformedMoves.add(lm);
                branchAndBound(
                    newPerformedMoves,
                    removeHeadAndConflicts(availableMoves, lm),
                    currentValue + lm.val);
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
        private boolean checkNewCycle(String target, String from, List moves)
        {
            for (Iterator it = moves.iterator(); it.hasNext();)
            {
                // note the when we hit a split there can be several
                // paths to explore.
                LegionBoardMove lm = (LegionBoardMove)it.next();
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

        private List removeHeadAndConflicts(
            List availableMoves,
            LegionBoardMove lm)
        {
            List newAvailableMoves = new ArrayList();

            for (Iterator it = availableMoves.listIterator(1); it.hasNext();)
            {
                List moves = (List)it.next();
                List newMoves = new ArrayList();
                if (lm.noMove && !moves.isEmpty() &&
                    lm.fromHex.equals(((LegionBoardMove)moves.get(0)).fromHex))
                {
                    // special case, these two legions are split, make
                    // sure to try the move off moves first
                    LegionBoardMove stayMove = null;
                    for (Iterator it2 = moves.iterator(); it2.hasNext();)
                    {
                        LegionBoardMove move = (LegionBoardMove)it2.next();
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
                        // make shure staymove is explored last
                    }
                }
                else
                {
                    for (Iterator it2 = moves.iterator(); it2.hasNext();)
                    {
                        LegionBoardMove move = (LegionBoardMove)it2.next();
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
    double hexRisk(LegionInfo legion, MasterHex hex, boolean invert)
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
            List enemies = (List)enemyAttackMap[roll].get(hex.getLabel());

            if (enemies == null)
            {
                continue;
            }

            Iterator it = enemies.iterator();
            double worst_result_this_roll = 0.0;

            while (it.hasNext())
            {
                LegionInfo enemy = (LegionInfo)it.next();
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

    int evaluateCombat(LegionInfo attacker, LegionInfo defender, MasterHex hex)
    {
        if (attacker.getPlayerName().equals(defender.getPlayerName()))
        {
            return 0;
        }

        final int defenderPointValue = defender.getPointValue();
        final int attackerPointValue = attacker.getPointValue();
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
                    else if (client.getNumLivingPlayers() == 2  &&
                        (attackerPointValue - result.getAttackerDead()) >
                        TITAN_SURVIVAL)
                    {
                        // do it and win the game
                        value = 1000 + (int)result.getExpectedValue() * 1000;
                    }
                    else if (result.getAttackerDead() <
                        attackerPointValue / 2  &&
                        (attackerPointValue - result.getAttackerDead()) >
                        TITAN_SURVIVAL)
                    {
                        // our titan stack will be badly damaged
                        // but it is worth it
                        value = 100 +
                            (int)result.getExpectedValue() * 100;
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
                else // win with minimal loss
                {
                    // value = result.getExpectedValue(); // default value
                }
            }
            else if (defenderTitan)
            {
                value = (1000 + (int)result.getExpectedValue() * 1000) /
                    client.getNumLivingPlayers();
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
                    value = result.getDefenderDead() * 100 /
                        client.getNumLivingPlayers() / 2;
                }
            }
            else if (attacker.hasTitan())
            {
                // ack! we'll kill our titan group
                // use metric below so that if we have no choice but to attack
                // we pick the least losing battle

                value = (-1000 + (int)result.getExpectedValue() * 1000) /
                    client.getNumLivingPlayers();
            }
        }

        // apply penalty to attacks if we have few legions
        // Don't reward titan attacks with few stacks
        int attackerLegions = attacker.getPlayerInfo().getNumLegions();
        if (attackerLegions < 5 && !I_HATE_HUMANS)
        {
            return value - (result.getAttackerDead() /
                attackerPointValue) * 1000;
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

    int evaluateHexAttack(LegionInfo attacker, MasterHex hex,
        int canRecruitHere)
    {
        int value = 0;
        // consider making an attack
        final String enemyMarkerId = client.getFirstEnemyLegion(hex.getLabel(),
            attacker.getPlayerName());

        if (enemyMarkerId != null)
        {
            LegionInfo defender = client.getLegionInfo(enemyMarkerId);
            if (!attacker.getPlayerName().equals(defender.getPlayerName()))
            {
                value = evaluateCombat(attacker, defender, hex);
            }

            if (I_HATE_HUMANS && !isHumanLegion(defender) &&
                !isHumanLegion(attacker))
            {
                // try not to attack other AIs
                if (value > -50)
                {
                    value = -50;
                }

                if (attacker.hasTitan())
                {
                    value -= 100;
                }

                if (defender.hasTitan())
                {
                    value -= 100;
                }
            }

            return value;
        }

        if ((canRecruitHere == RECRUIT_TRUE && attacker.getHeight() < 7) ||
            canRecruitHere == RECRUIT_AT_7)
        {
            if (!attacker.hasTitan())
            {
                value += recruitValue(attacker, hex.getLabel(), null,
                    hex.getTerrain());
            }
            else
            {
                // prefer recruiting with Titan legion
                value += recruitValue(attacker, hex.getLabel(), null,
                    hex.getTerrain()) *
                    1.1;
            }
        }

        return value;
    }

    /** Memoizing wrapper for evaluateMoveInner */
    private int evaluateMove(LegionInfo legion, MasterHex hex,
        int canRecruitHere, int depth, boolean addHexRisk)
    {
        String sep = "~";
        String key = "" + legion + sep + hex + sep + canRecruitHere + sep +
            depth + sep + addHexRisk;
        int score;
        Integer val;
        if (evaluateMoveMap.containsKey(key))
        {
            val = (Integer)evaluateMoveMap.get(key);
            score = val.intValue();
        }
        else
        {
            score = evaluateMoveInner(legion, hex, canRecruitHere, depth,
                addHexRisk);
            val = new Integer(score);
            evaluateMoveMap.put(key, val);
        }
        return score;
    }

    // cheap, inaccurate evaluation function.  Returns an expected value for
    // moving this legion to this hex.  The value defines a distance
    // metric over the set of all possible moves.
    private int evaluateMoveInner(LegionInfo legion, MasterHex hex,
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
            Set normal_moves = client.getMovement().listNormalMoves(legion, hex,
                roll);
            Set tele_moves = client.getMovement().listTeleportMoves(legion, hex,
                roll);
            double bestMoveVal = stay_at_hex; // can always stay here
            boolean no_attack = false;

            for (int i = 0; i < 2;
