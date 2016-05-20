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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import jsl.utilities.random.distributions.StudentT;

/** The Statistic class allows the collection of summary statistics
 * on data via the collect() methods.  Both weighted and unweighted
 * statistical summaries are supported.  The primary statistical
 * summary is for the statistical moments.
 */
public class Statistic extends AbstractStatistic {

    /** Holds the minimum of the observed data.
     */
    protected double min;

    /** Holds the maximum of the observed data
     */
    protected double max;

    /** Holds the weighted sum of squares of the data.
     */
    protected double wsumsq;

    /** Holds the number of observations observed
     */
    protected double num;

    /** Holds the weighted sum of the data.
     */
    protected double wsum;

    /** Holds the sum of the weights observed.
     */
    protected double sumw;

    /** Holds the last value observed
     */
    protected double myValue;

    /** Holds the last weight observed
     */
    protected double myWeight;

    /** Holds the sum the lag-1 data, i.e. from the second data point on
     * variable for collecting lag1 covariance
     */
    protected double sumxx = 0.0;

    /** Holds the first observed data point, needed for von-neuman statistic
     */
    protected double firstx = 0.0;

    /** Holds the first 4 statistical moments
     */
    protected double[] moments;

    /** Holds sum = sum + j*x
     */
    protected double myJsum;

    /** Creates a Statistic with name "null"
     */
    public Statistic() {
        this(null, null);
    }

    /** Creates a Statistic with the given name
     * @param name A String representing the name of the statistic
     */
    public Statistic(String name) {
        this(name, null);
    }

    /** Creates a Statistic \based on the provided array
     * @param values an array of values to collect statistics on
     */
    public Statistic(double[] values) {
        this(null, values);
    }

    /** Creates a Statistic with the given name based on the provided array
     * @param name A String representing the name of the statistic
     * @param values an array of values to collect statistics on
     */
    public Statistic(String name, double[] values) {
        super(name);
        moments = new double[5];
        reset();
        if (values != null) {
            collect(values);
        }
    }

    /** Returns a statistic that summarizes the passed in array of values
     *
     * @param x the values to compute statistics for
     * @return a Statistic summarizing the data
     */
    public static Statistic collectStatistics(double[] x) {
        Statistic s = new Statistic();
        s.collect(x);
        return (s);
    }

    /** Returns a statistic that summarizes the passed in arrays
     *  The lengths of the arrays must be the same.
     *
     * @param x the values
     * @param w the weights
     * @return a Statistic summarizing the data
     */
    public static Statistic collectStatistics(double[] x, double[] w) {
        if (x.length != w.length) {
            throw new IllegalArgumentException("The supplied arrays are not of equal length");
        }

        Statistic s = new Statistic();
        s.collect(x, w);

        return (s);
    }

    /** Creates a instance of Statistic that is a copy of the supplied Statistic
     *  All internal state is the same except for the id of the returned Statistic
     *
     * @param stat
     * @return a copy of the supplied Statistic
     */
    public static Statistic newInstance(Statistic stat) {
        Statistic s = new Statistic();
        s.myNumMissing = stat.myNumMissing;
        s.firstx = stat.firstx;
        s.max = stat.max;
        s.min = stat.min;
        s.myConfidenceLevel = stat.myConfidenceLevel;
        s.myJsum = stat.myJsum;
        s.myValue = stat.myValue;
        s.myName = stat.myName;
        s.myWeight = stat.myWeight;
        s.num = stat.num;
        s.sumw = stat.sumw;
        s.wsum = stat.wsum;
        s.wsumsq = stat.wsumsq;
        s.sumxx = stat.sumxx;
        System.arraycopy(stat.moments, 0, s.moments, 0, s.moments.length);
        return (s);
    }

    /** Creates a instance of Statistic that is a copy of this Statistic
     *  All internal state is the same except for the id of the returned Statistic
     *
     * @return a copy of the supplied Statistic
     */
    public final Statistic newInstance() {
        Statistic s = new Statistic();
        s.myNumMissing = myNumMissing;
        s.firstx = firstx;
        s.max = max;
        s.min = min;
        s.myConfidenceLevel = myConfidenceLevel;
        s.myJsum = myJsum;
        s.myValue = myValue;
        s.myName = myName;
        s.myWeight = myWeight;
        s.num = num;
        s.sumw = sumw;
        s.wsum = wsum;
        s.wsumsq = wsumsq;
        s.sumxx = sumxx;
        System.arraycopy(moments, 0, s.moments, 0, s.moments.length);
        return (s);
    }

    /** Returns the index associated with the minimum element in the array
     *  For ties, this returns the first found
     * 
     * @param x
     * @return the index associated with the minimum element
     */
    public static int getIndexOfMin(double[] x) {
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * 
     * @param x
     * @return the minimum value in the array
     */
    public static double getMin(double[] x) {
        return x[getIndexOfMin(x)];
    }

    /** Returns the index associated with the maximum element in the array
     *  For ties, this returns the first found
     * 
     * @param x
     * @return the index associated with the maximum element
     */
    public static int getIndexOfMax(double[] x) {
        int index = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * 
     * @param x
     * @return the maximum value in the array
     */
    public static double getMax(double[] x) {
        return x[getIndexOfMax(x)];
    }

    /** Returns the median of the data.  The array is sorted
     *
     * @param data
     * @return the median of the data
     */
    public static double getMedian(double[] data) {
        Arrays.sort(data);
        int size = data.length;
        double median = -1;
        if (size % 2 == 0) {//even
            int firstIndex = (size / 2) - 1;
            int secondIndex = firstIndex + 1;
            double firstValue = data[firstIndex];
            double secondValue = data[secondIndex];
            median = (firstValue + secondValue) / 2;
        } else {//odd
            int index = (int) Math.ceil(size / 2);
            median = data[index];
        }
        return median;
    }

    @Override
    public final double getCount() {
        return (moments[0]);
    }

    @Override
    public final double getSum() {
        return (moments[1] * moments[0]);
    }

    @Override
    public final double getWeightedSum() {
        return (wsum);
    }

    @Override
    public final double getWeightedSumOfSquares() {
        return (wsumsq);
    }

    @Override
    public final double getAverage() {
        if (moments[0] < 1.0) {
            return Double.NaN;
        }

        return (moments[1]);
    }

    /** Returns the 2nd statistical central moment
     * 
     * @return the 2nd statistical central moment
     */
    public final double get2ndCentralMoment() {
        return moments[2];
    }

    /** Returns the 3rd statistical central moment
     * 
     * @return the 3rd statistical central moment
     */
    public final double get3rdCentralMoment() {
        return moments[3];
    }

    /** Returns the 4th statistical central moment
     * 
     * @return the 4th statistical central moment
     */
    public final double get4thCentralMoment() {
        return moments[4];
    }

    /** Returns the 2nd statistical raw moment (about zero)
     * 
     * @return the 2nd statistical raw moment (about zero)
     */
    public final double get2ndRawMoment() {
        double mu = getAverage();
        return moments[2] + mu * mu;
    }

    /** Returns the 3rd statistical raw moment (about zero)
     * 
     * @return the 3rd statistical raw moment (about zero)
     */
    public final double get3rdRawMoment() {
        double m3 = get3rdCentralMoment();
        double mr2 = get2ndRawMoment();
        double mu = getAverage();
        return m3 + 3.0 * mu * mr2 - 2.0 * mu * mu * mu;
    }

    /** Returns the 4th statistical raw moment (about zero)
     * 
     * @return the 4th statistical raw moment (about zero)
     */
    public final double get4thRawMoment() {
        double m4 = get4thCentralMoment();
        double mr3 = get3rdRawMoment();
        double mr2 = get2ndRawMoment();
        double mu = getAverage();
        return m4 + 4.0 * mu * mr3 - 6.0 * mu * mu * mr2 + 3.0 * mu * mu * mu * mu;
    }

    @Override
    public final double getSumOfWeights() {
        return (sumw);
    }

    @Override
    public final double getWeightedAverage() {
        if (sumw <= 0.0) {
            return Double.NaN;
        }

        return (wsum / sumw);
    }

    @Override
    public final double getDeviationSumOfSquares() {
        return (moments[2] * moments[0]);
    }

    @Override
    public final double getVariance() {
        if (moments[0] < 2) {
            return Double.NaN;
        }

        return (getDeviationSumOfSquares() / (moments[0] - 1.0));
    }

    @Override
    public final double getStandardDeviation() {
        return (Math.sqrt(getVariance()));
    }

    @Override
    public final double getMin() {
        return (min);
    }

    @Override
    public final double getMax() {
        return (max);
    }

    @Override
    public final double getLastValue() {
        return (myValue);
    }

    @Override
    public final double getLastWeight() {
        return (myWeight);
    }

    @Override
    public final double getKurtosis() {
        if (moments[0] < 4) {
            return (Double.NaN);
        }

        double n = moments[0];
        double n1 = n - 1.0;
        double v = getVariance();
        double d = (n - 1.0) * (n - 2.0) * (n - 3.0) * v * v;
        double t = n * (n + 1.0) * n * moments[4] - 3.0 * n1 * n1 * n1 * v * v;
        double k = (double) t / (double) d;
        return (k);
    }

    @Override
    public final double getSkewness() {
        if (moments[0] < 3) {
            return (Double.NaN);
        }

        double n = moments[0];
        double v = getVariance();
        double s = Math.sqrt(v);
        double d = (n - 1.0) * (n - 2.0) * v * s;
        double t = n * n * moments[3];

        double k = (double) t / (double) d;
        return (k);
    }

    /** Checks if the supplied value falls within
     *  getAverage() +/- getHalfWidth()
     *
     * @param mean
     * @return true if the supplied value falls within getAverage() +/- getHalfWidth()
     */
    public final boolean checkMean(double mean) {

        double a = getAverage();
        double hw = getHalfWidth();
        double ll = a - hw;
        double ul = a + hw;

        if ((ll <= mean) && (mean <= ul)) {
            return true;
        } else {
            return false;
        }
    }

    /** Returns the half-width for a confidence interval on the mean
     *  with confidence level alpha based on StudentT
     *  distribution
     * 
     * @param level
     * @return 
     */
    @Override
    public double getHalfWidth(double level) {
        if (getCount() <= 1.0) {
            return (Double.NaN);
        }
        double dof = getCount() - 1.0;
        double alpha = 1.0 - level;
        double p = 1.0 - alpha/2.0;
        double t = StudentT.getInvCDF(dof, p);
        double hw = t * getStandardError();
//        System.out.println("dof = " + dof);
//        System.out.println("1.0 - alpha/2.0 = " + p);
//        System.out.println("t = " + t);
//        System.out.println("hw " + hw);
        return hw;
    }

    @Override
    public final double getStandardError() {
        if (moments[0] < 1.0) {
            return (Double.NaN);
        }

        return (getStandardDeviation() / Math.sqrt(moments[0]));
    }

    @Override
    public final int getLeadingDigitRule(double a) {
        return (int) Math.floor(Math.log10(a * getStandardError()));
    }

    @Override
    public final double getLag1Covariance() {
        if (num > 2.0) {
            double c1 = sumxx - (num + 1.0) * moments[1] * moments[1] + moments[1] * (firstx + myValue);
            return (c1 / num);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getLag1Correlation() {
        if (num > 2.0) {
            return (getLag1Covariance() / moments[2]);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getVonNeumannLag1TestStatistic() {
        if (num > 2.0) {
            double r1 = getLag1Correlation();
            double t = (firstx - moments[1]) * (firstx - moments[1]) + (myValue - moments[1]) * (myValue - moments[1]);
            double b = 2.0 * num * moments[2];
            double v = Math.sqrt((num * num - 1.0) / (num - 2.0)) * (r1 + (t / b));
            return (v);
        } else {
            return (Double.NaN);
        }
    }

    /** Returns the observation weighted sum of the data
     *  i.e. sum = sum + j*x
     *  where j is the observation number and x is jth observation
     *
     * @return the observation weighted sum of the data
     */
    public final double getObsWeightedSum() {
        return (myJsum);
    }

    @Override
    public void collect(double x, double weight) {
        if (Double.isNaN(x)) {
            myNumMissing++;
            return;
        }

        if (getSaveDataOption()) {
            saveData(x, weight);
        }

        double n, n1, n2, delta, d2, d3, r1;

        // update moments
        num = num + 1;
        sumw = sumw + weight;
        wsum = wsum + x * weight;
        wsumsq = wsumsq + x * x * weight;
        myJsum = myJsum + num * x;

        n = moments[0];
        n1 = n + 1.0;
        n2 = n * n;
        delta = (moments[1] - x) / n1;
        d2 = delta * delta;
        d3 = delta * d2;
        r1 = n / n1;
        moments[4] = (1.0 + n * n2) * d2 * d2 + 6.0 * moments[2] * d2 + 4.0 * moments[3] * delta + moments[4];
        moments[4] *= r1;

        moments[3] = (1.0 - n2) * d3 + 3.0 * moments[2] * delta + moments[3];
        moments[3] *= r1;

        moments[2] = (1.0 + n) * d2 + moments[2];
        moments[2] *= r1;

        moments[1] = moments[1] - delta;
        moments[0] = n1;

        // to collect lag 1 cov, we need x(1)

        if (num == 1.0) {
            firstx = x;
        }

        // to collect lag 1 cov, we must provide new x and previous x
        // to collect lag 1 cov, we must sum x(i) and x(i+1)
        if (num >= 2.0) {
            sumxx = sumxx + x * myValue;
        }

        // update min, max, current value, current weight
        if (x > max) {
            max = x;
        }
        if (x < min) {
            min = x;
        }

        myValue = x;
        myWeight = weight;

    }

    @Override
    public void reset() {
        myNumMissing = 0.0;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        wsumsq = 0.0;
        num = 0.0;
        wsum = 0.0;
        sumw = 0.0;
        myJsum = 0.0;
        sumxx = 0.0;

        for (int i = 0; i < moments.length; i++) {
            moments[i] = 0.0;
        }

        clearSavedData();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID ");
        sb.append(getId());
        sb.append("\n");
        sb.append("Name ");
        sb.append(getName());
        sb.append("\n");
        sb.append("Number ");
        sb.append(getCount());
        sb.append("\n");
        sb.append("Average ");
        sb.append(getAverage());
        sb.append("\n");
        sb.append("Standard Deviation ");
        sb.append(getStandardDeviation());
        sb.append("\n");
        sb.append("Standard Error ");
        sb.append(getStandardError());
        sb.append("\n");
        sb.append("Half-width ");
        sb.append(getHalfWidth());
        sb.append("\n");
        sb.append("Confidence Level ");
        sb.append(getConfidenceLevel());
        sb.append("\n");
        sb.append("Minimum ");
        sb.append(getMin());
        sb.append("\n");
        sb.append("Maximum ");
        sb.append(getMax());
        sb.append("\n");
        sb.append("Sum ");
        sb.append(getSum());
        sb.append("\n");
        sb.append("Variance ");
        sb.append(getVariance());
        sb.append("\n");
        sb.append("Weighted Average ");
        sb.append(getWeightedAverage());
        sb.append("\n");
        sb.append("Weighted Sum ");
        sb.append(getWeightedSum());
        sb.append("\n");
        sb.append("Sum of Weights ");
        sb.append(getSumOfWeights());
        sb.append("\n");
        sb.append("Weighted Sum of Squares ");
        sb.append(getWeightedSumOfSquares());
        sb.append("\n");
        sb.append("Deviation Sum of Squares ");
        sb.append(getDeviationSumOfSquares());
        sb.append("\n");
        sb.append("Last value collected ");
        sb.append(getLastValue());
        sb.append("\n");
        sb.append("Last weighted collected ");
        sb.append(getLastWeight());
        sb.append("\n");
        sb.append("Kurtosis ");
        sb.append(getKurtosis());
        sb.append("\n");
        sb.append("Skewness ");
        sb.append(getSkewness());
        sb.append("\n");
        sb.append("Lag 1 Covariance ");
        sb.append(getLag1Covariance());
        sb.append("\n");
        sb.append("Lag 1 Correlation ");
        sb.append(getLag1Correlation());
        sb.append("\n");
        sb.append("Von Neumann Lag 1 Test Statistic ");
        sb.append(getVonNeumannLag1TestStatistic());
        sb.append("\n");
        sb.append("Number of missing observations ");
        sb.append(getNumberMissing());
        sb.append("\n");
        sb.append("Lead-Digit Rule(1) ");
        sb.append(getLeadingDigitRule(1.0));
        sb.append("\n");
        return (sb.toString());
    }

    /** Writes a "pretty" representation of the statistics in the list in column
     *  form
     * 
     *  Name    Count   Average     Std. Dev.
     * 
     * @param out
     * @param stats
     */
    public static void writeSummaryStatistics(PrintWriter out, List<Statistic> stats) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        if (stats == null) {
            throw new IllegalArgumentException("The List was null");
        }
        String hline = "-----------------------------------------------------------------------------------------------------";
        out.println(hline);
        out.println();
        out.println("Statistical Summary Report");
        out.println(new Date());
        String format = "%-50s \t %12d \t %12f \t %12f %n";

        if (!stats.isEmpty()) {
            out.println(hline);
            out.println();
            out.println(hline);
            out.printf("%-50s \t %12s \t %12s \t %12s %n", "Name", "Count", "Average", "Std. Dev.");
            out.println(hline);

            for (Statistic stat : stats) {
                int n = (int) stat.getCount();
                double avg = stat.getAverage();
                double std = stat.getStandardDeviation();
                String name = stat.getName();
                out.printf(format, name, n, avg, std);

            }
            out.println(hline);
        }
    }
}

