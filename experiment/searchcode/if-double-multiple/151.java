/*
 * Copyright (c) 2007, Manuel D. Rossetti (rossetti@uark.edu)
 *
 * Contact:
 *	Manuel D. Rossetti, Ph.D., P.E.
 *	Department of Industrial Engineering
 *	University of Arkansas
 *	4207 Bell Engineering Center
 *	Fayetteville, AR 72701
 *	Phone: (479) 575-6756
 *	Email: rossetti@uark.edu
 *	Web: www.uark.edu/~rossetti
 *
 * This file is part of the JSL (a Java Simulation Library). The JSL is a framework
 * of Java classes that permit the easy development and execution of discrete event
 * simulation programs.
 *
 * The JSL is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The JSL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the JSL (see file COPYING in the distribution);
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA, or see www.fsf.org
 *
 */
package jsl.utilities.statistic;

/**
 */
public class BatchStatistic extends AbstractStatistic {

    /** the default minimum number of batches
     */
    public static final int MIN_NUM_BATCHES = 20;

    /** the default minimum number of observations
     *  per batch
     */
    public static final int MIN_NUM_OBS_PER_BATCH = 16;

    /** the default multiplier that determines the maximum
     *  number of batches
     */
    public static final int MAX_BATCH_MULTIPLE = 2;

    /** The minimum number of observations per
     *  batch
     */
    private int myMinBatchSize;

    /** The minimum number of batches required
     */
    private int myMinNumBatches;

    /**
     *  The multiple of the minimum number of batches
     *  that determines the maximum number of batches
     *  e.g. if the min. number of batches is 20
     *  and the max number batches multiple is 2,
     *  then we can have at most 40 batches
     */
    private int myMaxNumBatchesMultiple;

    /** The maximum number of batches as determined
     *  by the max num batches multiple
     */
    private int myMaxNumBatches;

    /** holds the batch means
     */
    private double[] bm;

    /** the number of batches
     */
    private int myNumBatches = 0;

    /** the number of time rebatching has occurred
     */
    private int myNumRebatches = 0;

    /** the size of the current batch
     */
    private int myCurrentBatchSize;

    /** collects the within batch statistics
     */
    private Statistic myStatistic;

    /** collects the across batch statistics
     */
    private Statistic myBMStatistic;

    /** the last observed value
     */
    private double myValue;

    /** the weight associated with the value
     */
    private double myWeight;

    /** counts the total number of observations
     */
    private double myTotNumObs = 0.0; //counts total number of observations

    /** Creates a BatchStatistic with defaults:
     * The minimum number of batches = 20
     * The minimum number of observations per batch = 16
     * The maximum number of batches = 40, i.e. max batch multiple is 2
     */
    public BatchStatistic() {
        this(20, 16, 2, null, null);
    }

    /** Creates a BatchStatistic with defaults:
     * The minimum number of batches = 20
     * The minimum number of observations per batch = 16
     * The maximum number of batches = 40, i.e. max batch multiple is 2
     * @param name The name for the statistic
     */
    public BatchStatistic(String name) {
        this(20, 16, 2, name, null);
    }

    /** Creates a BatchStatistic with defaults:
     * The minimum number of batches = 20
     * The minimum number of observations per batch = 16
     * The maximum number of batches = 40, i.e. max batch multiple is 2
     * @param name The name for the statistic
     * @param values An array of values to collect on
     */
    public BatchStatistic(String name, double[] values) {
        this(20, 16, 2, name, values);
    }

    /** Creates a BatchStatistic with defaults:
     * The minimum number of batches = 20
     * The minimum number of observations per batch = 16
     * The maximum number of batches = 40, i.e. max batch multiple is 2
     * @param values An array of values to collect on
     */
    public BatchStatistic(double[] values) {
        this(20, 16, 2, null, values);
    }

    /** Creates a BatchStatistic
     * @param minNumBatches The minimum number of batches, must be >= 2
     * @param minBatchSize The minimum number of observations per batch, must be >= 2
     * @param maxNBMultiple The maximum number of batches as a multiple of the minimum number of batches.
     * For example, if minNumBatches = 20 and maxNBMultiple = 2 then the maximum number of batches allowed will be 40.
     * maxNBMultiple must be 2 or more.
     */
    public BatchStatistic(int minNumBatches, int minBatchSize, int maxNBMultiple) {
        this(minNumBatches, minBatchSize, maxNBMultiple, null, null);
    }

    /** Creates a BatchStatistic
     * For example, if minNumBatches = 20 and maxNBMultiple = 2 then the maximum number of batches allowed will be 40.
     * maxNBMultiple must be 2 or more.
     * @param minNumBatches The minimum number of batches, must be >= 2
     * @param minBatchSize The minimum number of observations per batch, must be >= 2
     * @param maxNBMultiple The maximum number of batches as a multiple of the minimum number of batches.
     * @param values An array of values to collect on
     */
    public BatchStatistic(int minNumBatches, int minBatchSize, int maxNBMultiple, double[] values) {
        this(minNumBatches, minBatchSize, maxNBMultiple, null, values);
    }

    /** Creates a BatchStatistic
     * For example, if minNumBatches = 20 and maxNBMultiple = 2 then the maximum number of batches allowed will be 40.
     * maxNBMultiple must be 2 or more.
     * @param minNumBatches The minimum number of batches, must be >= 2
     * @param minBatchSize The minimum number of observations per batch, must be >= 2
     * @param maxNBMultiple The maximum number of batches as a multiple of the minimum number of batches.
     * @param name A String representing the name of the statistic
     */
    public BatchStatistic(int minNumBatches, int minBatchSize, int maxNBMultiple, String name) {
        this(minNumBatches, minBatchSize, maxNBMultiple, name, null);
    }

    /** Creates a BatchStatistic with the given name
     * For example, if minNumBatches = 20 and maxNBMultiple = 2 then the maximum number of batches allowed will be 40.
     * maxNBMultiple must be 2 or more.
     * @param minNumBatches The minimum number of batches, must be >= 2
     * @param minBatchSize The minimum number of observations per batch, must be >= 2
     * @param maxNBMultiple The maximum number of batches as a multiple of the minimum number of batches.
     * @param name A String representing the name of the statistic
     * @param values An array of values to collect on
     */
    public BatchStatistic(int minNumBatches, int minBatchSize, int maxNBMultiple, String name, double[] values) {
        super(name);

        if (minNumBatches <= 1) {
            throw new IllegalArgumentException("Number of batches must be >= 2");
        }

        if (minBatchSize <= 1) {
            throw new IllegalArgumentException("Batch size must be >= 2");
        }

        if (maxNBMultiple <= 1) {
            throw new IllegalArgumentException("Maximum number of batches multiple must be >= 2");
        }

        myMinNumBatches = minNumBatches;
        myMinBatchSize = minBatchSize;
        myCurrentBatchSize = myMinBatchSize;

        myMaxNumBatchesMultiple = maxNBMultiple;

        myMaxNumBatches = myMinNumBatches * myMaxNumBatchesMultiple;

        bm = new double[myMaxNumBatches + 1];
        myStatistic = new Statistic();
        myBMStatistic = new Statistic(getName());
        if (values != null) {
            for (double x : values) {
                collect(x);
            }
        }
    }

    public static BatchStatistic newInstance(BatchStatistic bStat) {
        BatchStatistic b = new BatchStatistic(bStat.myMinNumBatches, bStat.myMinBatchSize, bStat.myMaxNumBatchesMultiple);

        for (int i = 1; i <= b.myMaxNumBatches; i++) {
            b.bm[i] = bStat.bm[i];
        }

        b.myNumBatches = bStat.myNumBatches;
        b.myNumRebatches = bStat.myNumRebatches;
        b.myCurrentBatchSize = bStat.myCurrentBatchSize;

        b.myStatistic = Statistic.newInstance(bStat.myStatistic);
        b.myBMStatistic = Statistic.newInstance(bStat.myBMStatistic);

        b.myValue = bStat.myValue;
        b.myWeight = bStat.myWeight;
        b.myTotNumObs = bStat.myTotNumObs;

        return (b);
    }

    public final BatchStatistic newInstance() {
        return newInstance(this);
    }

    /** Returns the minimum number of Batches that are needed
     * @return
     */
    public final int getMinNumberOfBatches() {
        return (myMinNumBatches);
    }

    /** Returns the maximum number of batches that is declared
     * @return
     */
    public final int getMaxNumberOfBatchesMultiple() {
        return (myMaxNumBatchesMultiple);
    }

    /** Returns the minimum number of observations needed in a batch
     * @return
     */
    public final int getMinBatchSize() {
        return (myMinBatchSize);
    }

    @Override
    public void setConfidenceLevel(double alpha) {
        myBMStatistic.setConfidenceLevel(alpha);
    }

    @Override
    public final void reset() {
        myNumMissing = 0.0;
        myBMStatistic.reset();
        for (int i = 1; i <= myMaxNumBatches; i++) {
            bm[i] = 0.0;
        }
        myStatistic.reset();
        myNumBatches = 0;
        myTotNumObs = 0.0;
        myCurrentBatchSize = myMinBatchSize;
        clearSavedData();
    }

    @Override
    public final void collect(double value, double weight) {
        if (Double.isNaN(value)) {
            myNumMissing++;
            return;
        }

        if (getSaveDataOption()) {
            saveData(value, weight);
        }

        myTotNumObs = myTotNumObs + 1.0;
        myValue = value;
        myWeight = weight;
        myStatistic.collect(myValue, myWeight);
        if (myStatistic.getCount() == myCurrentBatchSize) {
            collectBatch();
        }
    }

    /** Performs the collection of the batches.
     *
     *
     */
    private void collectBatch() {
        // increment the current number of batches
        myNumBatches = myNumBatches + 1;
        // record the average of the batch
        bm[myNumBatches] = myStatistic.getWeightedAverage();
        // collect running statistics on the batches
        myBMStatistic.collect(bm[myNumBatches]);
        // reset the within batch statistic for next batch
        myStatistic.reset();
        // if the number of batches has reached the maximum then rebatch down to
        // min number of batches
        if (myNumBatches == myMaxNumBatches) {
            myNumRebatches++;
            myCurrentBatchSize = myCurrentBatchSize * myMaxNumBatchesMultiple;
            int j = 0; // within batch counter
            int k = 0; // batch counter
            myBMStatistic.reset(); // clear for collection across new batches
            // loop through all the batches
            for (int i = 1; i <= myNumBatches; i++) {
                myStatistic.collect(bm[i]); // collect across batches old batches
                j++;
                if (j == myMaxNumBatchesMultiple) { // have enough for a batch
                    //collect new batch average
                    myBMStatistic.collect(myStatistic.getAverage());
                    k++; //count the batches
                    bm[k] = myStatistic.getAverage(); // save the new batch average
                    myStatistic.reset(); // reset for next batch
                    j = 0;
                }
            }
            myNumBatches = k; // k should be minNumBatches
            myStatistic.reset(); //reset for use with new data
        }
    }

    /** Returns a StatisticAccessorIfc which has collected statistics after
     *  rebatching the batch means to the supplied number of batches
     *
     * @param numBatches the desired number of batches, must be >=2
     * @return A reference to a Statistic
     */
    public final Statistic rebatchToNumberOfBatches(int numBatches) {
        if (numBatches <= 1) {
            throw new IllegalArgumentException("Number of batches must be >= 2");
        }
        int j = 0;
        Statistic wb = new Statistic();
        Statistic bms = new Statistic(getName());
        int bs = myNumBatches / numBatches;
        // loop through all the batches
        for (int i = 1; i <= myNumBatches; i++) {
            wb.collect(bm[i]);
            j++;
            if (j == bs) {
                bms.collect(wb.getAverage());
                wb.reset();
                j = 0;
            }
        }
        return (bms);
    }

    /** Returns a copy of the batch means array
     *
     * @return An array holding the batch means
     */
    public final double[] getBatchMeanArrayCopy() {
        double[] nbm = new double[bm.length];
        System.arraycopy(bm, 0, nbm, 0, bm.length);
        return (nbm);
    }

    /** Gets the unweighted average over the batches.
     * @return A double representing the average or Double.NaN if no observations.
     */
    @Override
    public final double getAverage() {
        return (myBMStatistic.getAverage());
    }

    /** Gets the count of the number of batches.
     * @return A double representing the count
     */
    @Override
    public final double getCount() {
        return (myBMStatistic.getCount());
    }

    /** Gets the sum of the batch means
     * @return A double representing the sum
     */
    @Override
    public final double getSum() {
        return (myBMStatistic.getSum());
    }

    /** Gets the weighted sum of the batch means
     * @return A double representing the weighted sum
     */
    @Override
    public final double getWeightedSum() {
        return (myBMStatistic.getWeightedSum());
    }

    /** Gets the weighted sum of squares of the batch means
     * @return A double representing the weighted sum of squares
     */
    @Override
    public final double getWeightedSumOfSquares() {
        return (myBMStatistic.getWeightedSumOfSquares());
    }

    /** Gets the sum of the weights for batch means
     * @return A double representing the sum of the weights
     */
    @Override
    public final double getSumOfWeights() {
        return (myBMStatistic.getSumOfWeights());
    }

    /** Gets the weighted average of the batches.
     * @return A double representing the weighted average or Double.NaN if no observations.
     */
    @Override
    public final double getWeightedAverage() {
        return (myBMStatistic.getWeightedAverage());
    }

    /** Gets the sum of squares of the deviations from the average
     *  This is the numerator in the classic sample variance formula
     * @return A double representing the sum of squares of the deviations from the average
     */
    @Override
    public final double getDeviationSumOfSquares() {
        return (myBMStatistic.getDeviationSumOfSquares());
    }

    /** Gets the unweighted sample variance of the batches.
     * @return A double representing the sample variance or Double.NaN if 1 or less batch.
     */
    @Override
    public final double getVariance() {
        return (myBMStatistic.getVariance());
    }

    /** Gets the unweighted sample standard deviation of the batches. Simply
     *  the square root of getVariance()
     * @return A double representing the sample standard deviation or Double.NaN if 1 or less observations.
     */
    @Override
    public final double getStandardDeviation() {
        return (myBMStatistic.getStandardDeviation());
    }

    /** Gets the confidence interval half-width for the batches.  Simply the
     * sample standard error times the confidence coefficient
     * @param alpha 
     * @return A double representing the half-width or Double.NaN if < 1 observation
     */
    @Override
    public final double getHalfWidth(double alpha) {
        return (myBMStatistic.getHalfWidth(alpha));
    }

    /** Gets the minimum of the batch means.
     * @return A double representing the minimum
     */
    @Override
    public final double getMin() {
        return (myBMStatistic.getMin());
    }

    /** Gets the maximum of the batch means.
     * @return A double representing the maximum
     */
    @Override
    public final double getMax() {
        return (myBMStatistic.getMax());
    }

    /** Gets the last observed batch mean
     * @return A double representing the last batch mean
     */
    @Override
    public final double getLastValue() {
        return (myBMStatistic.getLastValue());
    }

    /** Gets the last observed weight
     * @return A double representing the last weight
     */
    @Override
    public final double getLastWeight() {
        return (myBMStatistic.getLastWeight());
    }

    /** Gets the kurtosis of the batch means
     * @return A double representing the kurtosis
     */
    @Override
    public final double getKurtosis() {
        return (myBMStatistic.getKurtosis());
    }

    /** Gets the skewness of the batch means
     * @return A double representing the skewness
     */
    @Override
    public final double getSkewness() {
        return (myBMStatistic.getSkewness());
    }

    /** Gets the standard error of the batch means.  Simply the
     * sample standard deviation divided by the square root of the number of batches
     * @return A double representing the standard error or Double.NaN if < 1 batches
     */
    @Override
    public final double getStandardError() {
        return (myBMStatistic.getStandardError());
    }

    /** Gets the lag-1 sample correlation of the unweighted batch means.
     *  Note: See Box, Jenkins, Reinsel, Time Series Analysis, 3rd edition, Prentice-Hall, pg 31
     * @return A double representing the sample correlation or Double.NaN if <=2 batches
     */
    @Override
    public final double getLag1Correlation() {
        return (myBMStatistic.getLag1Correlation());
    }

    /** Gets the lag-1 sample covariance of the unweighted batch means.
     *  Note: See Box, Jenkins, Reinsel, Time Series Analysis, 3rd edition, Prentice-Hall, pg 31
     * @return A double representing the sample correlation or Double.NaN if <=2 batches
     */
    @Override
    public final double getLag1Covariance() {
        return (myBMStatistic.getLag1Covariance());
    }

    /** Gets the Von Neumann Lag 1 test statistic for checking the hypothesis that the batches are uncorrelated
     *  Note: See Handbook of Simulation, Jerry Banks editor, McGraw-Hill, pg 253.
     * @return A double representing the Von Neumann test statistic
     */
    @Override
    public final double getVonNeumannLag1TestStatistic() {
        return (myBMStatistic.getVonNeumannLag1TestStatistic());
    }

    @Override
    public final int getLeadingDigitRule(double a) {
        return myBMStatistic.getLeadingDigitRule(a);
    }

    @Override
    public final String toString() {
        String s = myBMStatistic.toString();
        s = s + "Minimum batch size = " + myMinBatchSize + "\n" + "Minimum number of batches = " + myMinNumBatches + "\n" + "Maximum number of batches multiple = " + myMaxNumBatchesMultiple + "\n" + "Maximum number of batches = " + myMaxNumBatches + "\n" + "Number of rebatches = " + myNumRebatches + "\n" + "Current batch size = " + myCurrentBatchSize + "\n" + "Amount left unbatched = " + myStatistic.getCount() + "\n" + "Total number observed = " + myTotNumObs + "\n";
        return (s);
    }

    /** Gets the total number of observations observed
     *
     * @return a double representing the total number of observations
     */
    public final double getTotalNumberOfObservations() {
        return (myTotNumObs);
    }

    /** Returns a reference to the StatisticAccessorIfc that is tabulating
     *  the current batch
     *
     * @return
     */
    public final StatisticAccessorIfc getCurrentBatchStatistic() {
        return (myStatistic);
    }

    /** Returns the current number of observations per batch
     *  This value varies during the execution. After all data have been
     *  collected, this method returns the number of observations per batch
     *
     * @return Returns the currentBatchSize.
     */
    public final int getCurrentBatchSize() {
        return myCurrentBatchSize;
    }

}

