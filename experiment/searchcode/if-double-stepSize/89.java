package jiggle;

/*
 * Abstract base class for first-order graph-drawing optimization procedures.
 * Includes concrete method for performing adaptive line search.
 */

public abstract class FirstOrderOptimizationProcedure extends ForceDirectedOptimizationProcedure {

    protected double maxCos = 1;

    FirstOrderOptimizationProcedure(Graph g, ForceModel fm, double accuracy) {
        super(g, fm);
        maxCos = accuracy;
    }

    protected double negativeGradient[][] = null;

    protected double descentDirection[][] = null;

    protected double penaltyVector[][] = null;

    @Override
    public double improveGraph() {
        int n = graph.getNumberOfVertices();
        int d = graph.getDimensions();
        if ((negativeGradient == null) || (negativeGradient.length != n)) {
            negativeGradient = new double[n][d];
            penaltyVector = new double[n][d];
            getNegativeGradient();
        }
        computeDescentDirection();
        return lineSearch();
    }

    public void reset() {
        negativeGradient = null;
    }

    private double computePenaltyFactor() {
        double m1 = l2Norm(negativeGradient);
        double m2 = l2Norm(penaltyVector);
        double penaltyFactor = 0;
        if (m2 == 0)
            penaltyFactor = 0;
        else if (m1 == 0)
            penaltyFactor = 1;
        else {
            /*
             * If the pernalty vector is opposing the negative gradient 
             * (cos=-1) we want the penalty vector to win out, otherwise the
             * closer the two vectors are give more importance to the negative
             * gradient
             * 
             * Therefore if cosine is -1, then penalty factor is strongest, 
             * normalized by the lengths of the two gradients.
             */
            double cos = dotProduct(negativeGradient, penaltyVector) / (m1 * m2);
            penaltyFactor = Math.max(0.00000001, (0.00000001 - cos)) * Math.max(1, (m1 / m2));
        }
        //System.err.println("penaltyFactor: " + penaltyFactor + " negativeGradient: " + m1 + " penaltyVector: " + m2 + " sum: " + (m1+penaltyFactor*m2) + " dif: " + (m1-penaltyFactor*m2));
        return penaltyFactor * 2; // we want this decision to overwhelm the gradient
    }
    
    private void getNegativeGradient() {
        forceModel.getNegativeGradient(negativeGradient);
        if (constrained) {
            forceModel.getPenaltyVector(penaltyVector);
            
            double penaltyFactor = computePenaltyFactor();
            
            int n = graph.getNumberOfVertices();
            int d = graph.getDimensions();
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    negativeGradient[i][j] += penaltyFactor * penaltyVector[i][j];
                }
            }
        }
    }


    protected abstract void computeDescentDirection();

    private double stepSize = 0.1, previousStepSize = 0;

    protected double lineSearch() {
        previousStepSize = 0;
        //int n = graph.numberOfVertices();
        double magDescDir = l2Norm(descentDirection);
        if (magDescDir < 0.0001)
            return 0;
        //double magLo = l2Norm(negativeGradient);
        step();
        getNegativeGradient();
        double magHi = l2Norm(negativeGradient);
        double m = magDescDir * magHi;
        double cos = dotProduct(negativeGradient, descentDirection) / m;
        double lo = 0, hi = Double.MAX_VALUE;
        //int i = 0;
        while (((cos < 0) || (cos > maxCos)) && (hi - lo > 0.00000001)) {
            if (cos < 0) {
                hi = stepSize;
                stepSize = (lo + hi) / 2;
            } else {
                if (hi < Double.MAX_VALUE) {
                    lo = stepSize;
                    stepSize = (lo + hi) / 2;
                } else {
                    lo = stepSize;
                    stepSize *= 2;
                }
            }
            step();
            getNegativeGradient();
            m = magDescDir * l2Norm(negativeGradient);
            cos = dotProduct(negativeGradient, descentDirection) / m;
        }
        return l2Norm(negativeGradient);
    }

    private void step() {
        int n = graph.getNumberOfVertices();
        double s = stepSize - previousStepSize;
        int successCnt = 0;
        for (int i = 0; i < n; i++) {
            boolean success = graph.vertices.get(i).translate(s, descentDirection[i]);
            successCnt += success ? 1 : 0;
        }
        previousStepSize = stepSize;
        if (successCnt != n) {
            System.err.println("ERR: FOOP.step() translation success: " + successCnt + "/" + n);
        }
    }

    protected static double dotProduct(double[][] u, double[][] v) {
        int n = u.length;
        int d = u[0].length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                sum += u[i][j] * v[i][j];
            }
        }
        return sum;
    }

    /*
     * returns sqrt ( cell@ + ... )  
     */
    protected static double l2Norm(double[][] vect) {
        return Math.sqrt(dotProduct(vect, vect));
    }

    protected double lInfinityNorm(double[][] vect) {
        int n = graph.getNumberOfVertices(), d = graph.getDimensions();
        double max = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                max = Math.max(max, Math.abs(vect[i][j]));
            }
        }
        return max;
    }
}
