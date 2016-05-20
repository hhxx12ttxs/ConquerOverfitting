package stratego2.model.Player.AI;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import stratego2.model.Color;
import stratego2.model.FriendlyPiece;
import stratego2.model.GameState;
import stratego2.model.Move;
import stratego2.model.Piece;
import stratego2.model.Rank;
import stratego2.model.Simulator;
import stratego2.model.Square;
import stratego2.model.StrategoRules;

/**
 *
 * @author roussew
 */
public class UTCPlayer extends AIPlayer {

    private static final double EPSILON = 0.0001;
    private static final double UTC_CONSTANT = 0.5;
    private MCSTNode actions;
    private MCSTNode root;

    private Piece tempCaptured;

    public UTCPlayer(Color color) {
        this.color = color;
    }

    public UTCPlayer(Color color, StrategoRules rules) {
        super(color, rules);
    }

    @Override
    public Move getMove() {
        GameState sampleState = getSampleState();
        actions = new MCSTNode(sampleState, actions);
        System.out.println(actions.getState());
        System.out.println(state);
        actions.expand();
        for (int i = 0; i < 5; i++) {
            root = new MCSTNode(getSampleState());
            root.expand();
            for (int j = 0; j < 50; j++) {
                if (j > 20) {
                    System.out.println();
                }
                MCSTNode next = treePolicy(root);
                double payout = defaultPolicy(next);
                backup(next, payout);
            }
            updateActionScores(i);
        }
        return getBestMove();
    }

    private void updateActionScores(int count) {
        List<MCSTNode> possibleActions = actions.getChildren();
        List<MCSTNode> treeActions = root.getChildren();

        for (int i = 0; i < possibleActions.size(); i++) {
            possibleActions.get(i).updateUtility(treeActions.get(i).getQvalue());
        }
    }

    private Move getBestMove() {
        List<MCSTNode> possibleActions = actions.getChildren();
        MCSTNode bestChild = possibleActions.get(0);
        for (MCSTNode node : possibleActions) {
            if (bestChild.getUtility() < node.getUtility()) {
                bestChild = node;
            }
        }
        return bestChild.getMove();
    }

    private GameState getSampleState() {
        ArrayList<EnemyPiece> enemyPieces = getArmies();
        ArrayList<FriendlyPiece> convertedPieces = convertPieces(enemyPieces);
        if (color == color.RED) {
            return new GameState(state.getToMove(), convertedPieces, army);
        } else {
            return new GameState(state.getToMove(), army, convertedPieces);
        }
    }

    private ArrayList<EnemyPiece> getArmies() {
        ArrayList<EnemyPiece> enemyPieces = new ArrayList<>();
        army.clear();
        for (Square s : state) {
            if (s.isOccupied()) {

                if (s.getOccupier() instanceof EnemyPiece) {
                    EnemyPiece ep = (EnemyPiece) s.getOccupier();
                    enemyPieces.add(ep);

                } else {
                    army.add((FriendlyPiece) s.getOccupier());
                }

            }
        }
        return enemyPieces;
    }

    private ArrayList<FriendlyPiece> convertPieces(ArrayList<EnemyPiece> enemy) {
        ArrayList<FriendlyPiece> convertedPieces = new ArrayList<>();
        for (EnemyPiece p : enemy) {
            ProbabilityDistribution dist = p.getDistribution();
            Rank rank = determineRank(dist);
            revealRank(dist, rank);
            convertedPieces.add(
                    new FriendlyPiece(p.getColor(), p.getRow(), p.getColumn(), rank));
        }
        return convertedPieces;
    }

    private void revealRank(ProbabilityDistribution changed, Rank rank) {
        changed.setProb(rank, 1);
        for (Rank r : Rank.values()) {
            if (r != rank) {
                changed.setProb(r, 0);
            }
        }        
        ArrayList<EnemyPiece> enemyArmy = getArmies();
        ArrayList<ProbabilityDistribution>distributions = getDistributions(enemyArmy);
        reCalibrateBeliefState(rank, distributions, changed);
    }

    private void reCalibrateBeliefState(Rank rank,
            List<ProbabilityDistribution> distributions,
            ProbabilityDistribution changed) {

        double scale;
        if (Math.abs(changed.getProb(rank) - 1) < EPSILON) {
            scale = getScaleUp(rank, distributions);
        } else {
            scale = getScaleDown(rank, distributions);
        }
        for (ProbabilityDistribution dist : distributions) {
            if (dist == changed || Math.abs(dist.getProb(rank) - 1) < EPSILON) {
                continue;
            }

            dist.setProb(rank, scale * dist.getProb(rank));
            try {
                normalize(dist, rank);
            } catch (Exception ex) {
                Logger.getLogger(UTCPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private double getScaleUp(Rank rank, List<ProbabilityDistribution> distributions) {
        int numAvailable = rank.getCount();
        for (ProbabilityDistribution dist : distributions) {
            if (Math.abs(dist.getProb(rank) - 1) < EPSILON) {
                numAvailable--;
            }
        }
        System.out.println("rank: " + rank + "numLeft: " + numAvailable);
        double scale = 0;
        if (numAvailable > 0) {
            scale = (double) numAvailable / (numAvailable + 1);
        }
        return scale;
    }

    private double getScaleDown(Rank rank, List<ProbabilityDistribution> distributions) {
        int count = 0;
        for (ProbabilityDistribution dist : distributions) {
            if (Math.abs(dist.getProb(rank)) > EPSILON) {
                count++;
            }
        }
        return (count + 1) / count;
    }

    private void normalize(ProbabilityDistribution dist, Rank modified) throws Exception {
        double total = 0;

        for (Rank r : Rank.values()) {
            if (r != modified) {
                total += dist.getProb(r);
            }
        }
        if (Math.abs(total) < EPSILON) {
            return;
        }

        double scale = (1 - dist.getProb(modified)) / total;

        for (Rank r : Rank.values()) {
            if (r != modified) {
                dist.setProb(r, scale * dist.getProb(r));
            }
        }

        total = 0;
        for (Rank r : Rank.values()) {
            total += dist.getProb(r);
        }
        if (Math.abs(total - 1) > EPSILON) {
            throw new IllegalStateException("normilization falure " + total);
        }
    }

    private Rank determineRank(ProbabilityDistribution dist) {
        double n = Math.random();
        double upper = 0;
        Rank returnVal = null;
        for (Rank r : Rank.values()) {
            upper += dist.getProb(r);
            if (Double.isNaN(upper)) {
                System.out.print("this is it " + r);
            }
            if (n <= upper) {
                returnVal = r;
                break;
            }
        }
        if (returnVal == null) {
            System.out.println();
        }
        return returnVal;
    }

    private MCSTNode treePolicy(MCSTNode node) {
        if (node.isLeaf()) {
            node.expand();
            return node;
        } else {
            double bestValue = Double.NEGATIVE_INFINITY;
            MCSTNode bestNode = null;
            for (MCSTNode child : node.getChildren()) {
                double explorationFactor
                        = (child.getCount() == 0) ? Double.POSITIVE_INFINITY
                        : UTC_CONSTANT * Math.sqrt(Math.log(node.getCount()) / child.getCount());
                double val = child.getQvalue() + explorationFactor;
                if (val > bestValue) {
                    bestValue = val;
                    bestNode = child;
                }
            }
            return treePolicy(bestNode);
        }
    }

    private double defaultPolicy(MCSTNode node) {
        Simulator simulator = new Simulator(node.getState(),
                new DefaultPlayer(Color.RED, rules, node.getState()),
                new DefaultPlayer(Color.BLUE, rules, node.getState()));
        try {
            Thread t = new Thread(simulator);
            t.start();
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(UTCPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(simulator.getState());
        return (simulator.getResult() == color) ? 1 : -1;
    }

    private void backup(MCSTNode node, double result) {
        if (node.getState().getToMove() != color) {
            result = -result;
        }
        do {
//            if (node.getState().getToMove() != color) {
            node.incrementCount();
//            }
            node.updateQValue(result);
            if (!node.isRoot()) {
                node = node.getParent();
            }
            result = -result;
        } while (!node.isRoot());
        node.incrementCount();
    }

    @Override
    public void reportMove(Move move) {
        Square endSquare = state.getSquare(move.getDestinationRow(), move.getDestinationColumn());
        Square startSquare = state.getSquare(move.getStartRow(), move.getStartColumn());

        if (startSquare.getOccupier() instanceof EnemyPiece) {
            EnemyPiece piece = (EnemyPiece) startSquare.getOccupier();
            ProbabilityDistribution dist = piece.getDistribution();
            state = state.placePiece(move.getStartRow(), move.getStartColumn(), piece);
            ArrayList<EnemyPiece> enemyArmy = null;
            ArrayList<ProbabilityDistribution> distributions;

            if (isScoutMove(move)) {
                try {
                    dist.setProb(Rank.SCOUT, 1);
                    normalize(dist, Rank.SCOUT);
                    enemyArmy = getArmies();
                    distributions = getDistributions(enemyArmy);
                    reCalibrateBeliefState(Rank.FLAG, distributions, dist);
                    reCalibrateBeliefState(Rank.BOMB, distributions, dist);
                } catch (Exception ex) {
                    Logger.getLogger(UTCPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                try {
                    dist.setProb(Rank.FLAG, 0);
                    normalize(dist, Rank.FLAG);
                    dist.setProb(Rank.BOMB, 0);
                    normalize(dist, Rank.BOMB);
                    enemyArmy = getArmies();
                    distributions = getDistributions(enemyArmy);
                    reCalibrateBeliefState(Rank.FLAG, distributions, dist);
                    reCalibrateBeliefState(Rank.BOMB, distributions, dist);
                } catch (Exception ex) {
                    Logger.getLogger(UTCPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ArrayList<? extends Piece> blueArmy, redArmy;
            if (color == color.BLUE) {
                blueArmy = army;
                redArmy = enemyArmy;
            } else {
                blueArmy = enemyArmy;
                redArmy = army;
            }
            state = new GameState(state.getToMove(), blueArmy, redArmy);
        }
        System.out.println("before Move: \n" + state);
        if (endSquare.isOccupied()) {
            tempCaptured = endSquare.getOccupier();
        }
        //if()
        super.reportMove(move);
        System.out.println("After move: \n" + state);

    }

    private boolean isScoutMove(Move move) {
        int xdist = Math.abs(move.getDestinationColumn() - move.getStartColumn());
        int ydist = Math.abs(move.getDestinationRow() - move.getStartRow());
        return (xdist > 1 || ydist > 1);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void revealSquare(Square square, Move move) {
        System.out.println(state);
        int row = move.getDestinationRow();
        int column = move.getDestinationColumn();
        if (!square.isOccupied()) {
            state = state.placePiece(row, column, null);
        } else {
            FriendlyPiece winner = (FriendlyPiece) square.getOccupier();

            if (tempCaptured.getColor() == winner.getColor()) {

                if (winner.getColor() != color) {
                    state = state.placePiece(row, column, tempCaptured);
                    ProbabilityDistribution winnerDist;
                    winnerDist = ((EnemyPiece) tempCaptured).getDistribution();
                    revealRank(winnerDist, winner.getRank());
                    ArrayList<EnemyPiece> enemyArmy = getArmies();
                    ArrayList<? extends Piece> blueArmy;
                    ArrayList<? extends Piece> redArmy;
                    if (color == color.BLUE) {
                        blueArmy = army;
                        redArmy = enemyArmy;
                    } else {
                        blueArmy = enemyArmy;
                        redArmy = army;
                    }
                    state = new GameState(state.getToMove(), blueArmy, redArmy);
                } else {
                    state = state.placePiece(row, column, tempCaptured);
                }

            } else if (winner.getColor() != color) {
                ProbabilityDistribution winnerDist;
                EnemyPiece ep;
                ep = ((EnemyPiece) state.getSquare(row, column).getOccupier());
                winnerDist = ep.getDistribution();
                state = state.placePiece(row, column, ep);
                revealRank(winnerDist, winner.getRank());
                ArrayList<EnemyPiece> enemyArmy = getArmies();
                ArrayList<? extends Piece> blueArmy;
                ArrayList<? extends Piece> redArmy;
                if (color == color.BLUE) {
                        blueArmy = army;
                        redArmy = enemyArmy;
                    } else {
                        blueArmy = enemyArmy;
                        redArmy = army;
                    }
                    state = new GameState(state.getToMove(), blueArmy, redArmy);
            }   

        }

        System.out.println(state);
    }

    private ArrayList<ProbabilityDistribution> getDistributions(ArrayList<EnemyPiece> enemyArmy) {
        ArrayList<ProbabilityDistribution> distributions = new ArrayList<>();
        for (EnemyPiece ep : enemyArmy) {
            distributions.add(ep.getDistribution());
        }
        return distributions;
    }

    @Override
    public List<FriendlyPiece> getSetup() {
        List<FriendlyPiece> friendlyArmy = super.getSetup();
        Color enemyColor = (this.color == Color.BLUE) ? Color.RED : Color.BLUE;
        StartPossitionMaker maker = new StartPossitionMaker(enemyColor);
        List<EnemyPiece> enemyArmy = maker.getBeliefState();
        List<? extends Piece> blueArmy, redArmy;
        if (color == Color.BLUE) {
            blueArmy = friendlyArmy;
            redArmy = enemyArmy;
        } else {
            blueArmy = enemyArmy;
            redArmy = friendlyArmy;
        }
        state = new GameState(Color.BLUE, blueArmy, redArmy);
        return friendlyArmy;
    }

    @Override
    public void reportResult(Color color) {
        //do nothing for now
    }

    class Location {

        int row;
        int column;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + this.row;
            hash = 17 * hash + this.column;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Location other = (Location) obj;
            if (this.row != other.row) {
                return false;
            }
            if (this.column != other.column) {
                return false;
            }
            return true;
        }

    }
}

