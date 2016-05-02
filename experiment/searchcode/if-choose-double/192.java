/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.utilities.statistic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jsl.utilities.rootfinding.Interval;

/** Holds data to perform multiple comparisons
 *  Performs pairwise comparisons and computes pairwise differences and
 *  variances
 *
 * @author rossetti
 */
public class MultipleComparisonAnalyzer {

    private LinkedHashMap<String, double[]> myDataMap;

    private int myDataSize;

    private LinkedHashMap<String, LinkedHashMap<String, double[]>> myPairDiffs;

    private LinkedHashMap<String, LinkedHashMap<String, Statistic>> myPairDiffStats;

    public MultipleComparisonAnalyzer(Map<String, double[]> dataMap) {
        setDataMap(dataMap);
    }

    /** The names of items being compared as an array of strings
     * 
     * @return 
     */
    public String[] getDataNames() {
        String[] names = new String[myDataMap.keySet().size()];
        int i = 0;
        for (String s : myDataMap.keySet()) {
            names[i] = s;
            i++;
        }
        return (names);
    }

    /** Returns true if the analyzer has data for the name
     * 
     * @param dataName
     * @return 
     */
    public boolean contains(String dataName) {
        return myDataMap.containsKey(dataName);
    }

    /** Sets the underlying data map.  Any data already in the analyzer
     *  will be replaced.
     *  The supplied dataMap must not be null.
     *  There needs to be at least 2 data arrays
     *  The length of each data array must be the same.
     * 
     * @param dataMap 
     */
    public final void setDataMap(Map<String, double[]> dataMap) {
        if (dataMap == null) {
            throw new IllegalArgumentException("The supplied data map was null");
        }

        if (dataMap.keySet().size() <= 1) {
            throw new IllegalArgumentException("There must be 2 or more data arrays");
        }

        if (checkLengths(dataMap) == false) {
            throw new IllegalArgumentException("The data arrays do not have all the same lengths");
        }

        myDataMap = new LinkedHashMap<String, double[]>();
        for (String s : dataMap.keySet()) {
            double[] x = dataMap.get(s);
            myDataSize = x.length;
            double[] d = new double[x.length];
            System.arraycopy(x, 0, d, 0, x.length);
            myDataMap.put(s, d);
        }

        myPairDiffs = computePairedDifferences();
        myPairDiffStats = computePairedDifferenceStatistics();
    }

    /** The key to each LinkedHashMap is the name of the data
     *  The Statistic is based on the paired differences
     * 
     * @return 
     */
    public LinkedHashMap<String, LinkedHashMap<String, Statistic>> computePairedDifferenceStatistics() {
        LinkedHashMap<String, LinkedHashMap<String, Statistic>> pd =
                new LinkedHashMap<String, LinkedHashMap<String, Statistic>>();
        int i = 1;
        for (String fn : myDataMap.keySet()) {
            int j = 1;
            if (i < myDataMap.keySet().size()) {
                LinkedHashMap<String, Statistic> m = new LinkedHashMap<String, Statistic>();
                pd.put(fn, m);
                for (String sn : myDataMap.keySet()) {
                    if (i < j) {
                        double[] fd = myDataMap.get(fn);
                        double[] sd = myDataMap.get(sn);
                        double[] d = computeDifference(fd, sd);
                        m.put(sn, new Statistic(fn + " - " + sn, d));
                    }
                    j++;
                }
            }
            i++;
        }
        return pd;
    }

    /** The key to each LinkedHashMap is the name of the data
     *  The array contains the paired differences
     * 
     * @return 
     */
    public LinkedHashMap<String, LinkedHashMap<String, double[]>> computePairedDifferences() {
        LinkedHashMap<String, LinkedHashMap<String, double[]>> pd =
                new LinkedHashMap<String, LinkedHashMap<String, double[]>>();
        int i = 1;
        for (String fn : myDataMap.keySet()) {
            int j = 1;
            if (i < myDataMap.keySet().size()) {
                LinkedHashMap<String, double[]> m = new LinkedHashMap<String, double[]>();
                pd.put(fn, m);
                for (String sn : myDataMap.keySet()) {
                    if (i < j) {
                        double[] fd = myDataMap.get(fn);
                        double[] sd = myDataMap.get(sn);
                        double[] d = computeDifference(fd, sd);
                        m.put(sn, d);
                    }
                    j++;
                }
            }
            i++;
        }
        return pd;
    }

    /** The paired differences as a array for the pair of data names
     *  given by the strings. If the data names don't exist a
     *  null pointer exception will occur
     * 
     * @param s1
     * @param s2
     * @return 
     */
    public double[] getPairedDifference(String s1, String s2) {
        LinkedHashMap<String, double[]> g = myPairDiffs.get(s1);
        double[] x = g.get(s2);
        double[] d = new double[x.length];
        System.arraycopy(x, 0, d, 0, x.length);
        return d;
    }

    /** A list holding the statistics for all of the pairwise
     *  differences is returned
     * 
     * @return 
     */
    public List<Statistic> getPairedDifferenceStatistics() {
        List<Statistic> list = new ArrayList<Statistic>();
        for (String f : myPairDiffStats.keySet()) {
            LinkedHashMap<String, Statistic> g = myPairDiffStats.get(f);
            for (String s : g.keySet()) {
                list.add(getPairedDifferenceStatistic(f, s));
            }
        }
        return list;
    }

    /** The statistics for the pair of data names
     *  given by the strings. If the data names don't exist a
     *  null pointer exception will occur
     * 
     * @param s1
     * @param s2
     * @return 
     */
    public Statistic getPairedDifferenceStatistic(String s1, String s2) {
        LinkedHashMap<String, Statistic> g = myPairDiffStats.get(s1);
        Statistic stat = g.get(s2);
        return stat.newInstance();
    }

    /** Each paired difference is labeled with 
     *  data name i - data name j for all i, j
     *  The returns the names as an array of strings
     * 
     * @return 
     */
    public String[] getNamesOfPairedDifferences() {
        List<Statistic> list = getPairedDifferenceStatistics();
        String[] names = new String[list.size()];
        int i = 0;
        for (Statistic s : list) {
            names[i] = s.getName();
            i++;
        }
        return names;
    }

    /** The name of the maximum average difference
     * 
     * @return 
     */
    public String getNameOfMaximumAverageOfDifferences() {
        return getNamesOfPairedDifferences()[getIndexOfMaximumOfAveragesOfDifferences()];
    }

    /** The name of the minimum average difference
     * 
     * @return 
     */
    public String getNameOfMinumumAverageOfDifferences() {
        return getNamesOfPairedDifferences()[getIndexOfMinimumOfAveragesOfDifferences()];
    }

    /** The actual maximum average of the differences
     * 
     * @return 
     */
    public double getMaximumOfAveragesOfDifferences() {
        return Statistic.getMax(getAveragesOfDifferences());
    }

    /** Suppose there are n data names. Then there are n(n-1)/2 pairwise
     *  differences.  This method returns the index of the 
     *  maximum of the array given by getAveragesOfDifferences()
     * 
     * @return 
     */
    public int getIndexOfMaximumOfAveragesOfDifferences() {
        return Statistic.getIndexOfMax(getAveragesOfDifferences());
    }

    public double getMinimumOfAveragesOfDifferences() {
        return Statistic.getMin(getAveragesOfDifferences());
    }

    /** The actual minimum average of the differences
     * 
     * @return 
     */
    public int getIndexOfMinimumOfAveragesOfDifferences() {
        return Statistic.getIndexOfMin(getAveragesOfDifferences());
    }

    /** Suppose there are n data names. Then there are n(n-1)/2 pairwise
     *  differences.  This method returns averages of the differences
     *  in an array.  The elements of the array have correspondence
     *  to the array of strings returned by getNamesOfPairedDifferences()
     * 
     * @return 
     */
    public double[] getAveragesOfDifferences() {
        List<Double> list = new ArrayList<Double>();
        for (String f : myPairDiffStats.keySet()) {
            LinkedHashMap<String, Statistic> g = myPairDiffStats.get(f);
            for (String s : g.keySet()) {
                list.add(getAverageDifference(f, s));
            }
        }
        double[] x = new double[list.size()];
        int i = 0;
        for (Double d : list) {
            x[i] = d.doubleValue();
            i++;
        }
        return x;
    }

    /** Suppose there are n data names. Then there are n(n-1)/2 pairwise
     *  differences.  This method returns variances of the differences
     *  in an array.  The elements of the array have correspondence
     *  to the array of strings returned by getNamesOfPairedDifferences()
     * 
     * @return 
     */
    public double[] getVariancesOfDifferences() {
        List<Double> list = new ArrayList<Double>();
        for (String f : myPairDiffStats.keySet()) {
            LinkedHashMap<String, Statistic> g = myPairDiffStats.get(f);
            for (String s : g.keySet()) {
                list.add(getVarianceOfDifference(f, s));
            }
        }
        double[] x = new double[list.size()];
        int i = 0;
        for (Double d : list) {
            x[i] = d.doubleValue();
            i++;
        }
        return x;
    }

    /** Suppose there are n data names. Then there are n(n-1)/2 pairwise
     *  differences.  This method returns a list of confidence intervals 
     *  of the differences in an array.  The elements of the array have correspondence
     *  to the array of strings returned by getNamesOfPairedDifferences()
     * 
     * @return 
     */
    public List<Interval> getConfidenceIntervalsOfDifferenceData(double alpha) {
        List<Statistic> list = getPairedDifferenceStatistics();
        List<Interval> ilist = new ArrayList<Interval>();
        for (Statistic s : list) {
            ilist.add(s.getConfidenceInterval(alpha));
        }
        return ilist;
    }

    /** The maximum variance of the differences
     * 
     * @return 
     */
    public double getMaxVarianceOfDifferences() {
        double[] v = getVariancesOfDifferences();
        int indexOfMax = Statistic.getIndexOfMax(v);
        return v[indexOfMax];

    }

    /** The average for the pair of data names
     *  given by the strings. If the data names don't exist a
     *  null pointer exception will occur
     * 
     * @param s1
     * @param s2
     * @return 
     */
    public double getAverageDifference(String s1, String s2) {
        LinkedHashMap<String, Statistic> g = myPairDiffStats.get(s1);
        Statistic stat = g.get(s2);
        return stat.getAverage();
    }

    /** The variance for the pair of data names
     *  given by the strings. If the data names don't exist a
     *  null pointer exception will occur
     * 
     * @param s1
     * @param s2
     * @return 
     */
    public double getVarianceOfDifference(String s1, String s2) {
        LinkedHashMap<String, Statistic> g = myPairDiffStats.get(s1);
        Statistic stat = g.get(s2);
        return stat.getVariance();
    }

    /** A helper method to compute the difference between
     *  the two arrays
     * 
     * @param f
     * @param s
     * @return 
     */
    public static double[] computeDifference(double[] f, double[] s) {
        if (f.length != s.length) {
            throw new IllegalArgumentException("The array lengths were not equal");
        }
        double[] r = new double[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i] = f[i] - s[i];
        }
        return r;
    }

    /** Checks if each double[] in the map has the same length
     * 
     * @param dataMap
     * @return 
     */
    public final boolean checkLengths(Map<String, double[]> dataMap) {
        if (dataMap.keySet().size() <= 1) {
            throw new IllegalArgumentException("There must be 2 or more data arrays");
        }

        int[] lengths = new int[dataMap.keySet().size()];

        int i = 0;
        for (String s : dataMap.keySet()) {
            lengths[i] = dataMap.get(s).length;
            if (i > 0) {
                if (lengths[i - 1] != lengths[i]) {
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    /** Get statistics on the data associated with the name.
     *  If the name is not in the analyzer, null is returned
     * 
     * @param name
     * @return 
     */
    public Statistic getStatistic(String name) {
        double[] data = myDataMap.get(name);
        if (data == null) {
            return null;
        }
        return new Statistic(name, data);
    }

    /** A list of statistics for all the data
     * 
     * @return 
     */
    public List<Statistic> getStatistics() {
        List<Statistic> list = new ArrayList<Statistic>();
        for (String s : myDataMap.keySet()) {
            list.add(getStatistic(s));
        }
        return list;
    }

    /** The average for the named data or Double.NaN if
     *  the name is not in the collector
     * 
     * @param name
     * @return 
     */
    public double getAverage(String name) {
        Statistic s = getStatistic(name);
        if (s == null) {
            return Double.NaN;
        }
        return s.getAverage();
    }

    /** The variance for the named data or Double.NaN if
     *  the name is not in the collector
     * 
     * @param name
     * @return 
     */
    public double getVariance(String name) {
        Statistic s = getStatistic(name);
        if (s == null) {
            return Double.NaN;
        }
        return s.getVariance();
    }

    /** The maximum of the average of all the data
     * 
     * @return 
     */
    public double getMaximumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getMax(avgs);
    }

    /** The index of the maximum average
     * 
     * @return 
     */
    public int getIndexOfMaximumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getIndexOfMax(avgs);
    }

    /** The name of the maximum average
     * 
     * @return 
     */
    public String getNameOfMaximumAverageOfData() {
        String[] names = getDataNames();
        return names[getIndexOfMaximumAverageOfData()];
    }

    /** The minimum of the average of all the data
     * 
     * @return 
     */
    public double getMinimumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getMin(avgs);
    }

    /** The index of the minimum of the average of all the data
     * 
     * @return 
     */
    public int getIndexOfMinimumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getIndexOfMin(avgs);
    }

    /** The name of the minimum of the average of all the data
     * 
     * @return 
     */
    public String getNameOfMinimumAverageOfData() {
        String[] names = getDataNames();
        return names[getIndexOfMinimumAverageOfData()];
    }

    /** An array of all the averages of the data
     * 
     * @return 
     */
    public double[] getAveragesOfData() {
        List<Statistic> list = getStatistics();
        double[] avg = new double[list.size()];
        int i = 0;
        for (Statistic s : list) {
            avg[i] = s.getAverage();
            i++;
        }
        return avg;
    }

    /** An array of all the variances of the data
     * 
     * @return 
     */
    public double[] getVariancesOfData() {
        List<Statistic> list = getStatistics();
        double[] var = new double[list.size()];
        int i = 0;
        for (Statistic s : list) {
            var[i] = s.getVariance();
            i++;
        }
        return var;
    }

    /** A list of confidence intervals for the data based on the
     *  supplied confidence level
     * 
     * @param level
     * @return 
     */
    public List<Interval> getConfidenceIntervalsOfData(double level) {
        List<Statistic> list = getStatistics();
        List<Interval> ilist = new ArrayList<Interval>();
        for (Statistic s : list) {
            ilist.add(s.getConfidenceInterval(level));
        }
        return ilist;
    }

    /** A 2-Dim array of the data
     *  each row represents the across replication average for each  
     *  configuration (column)
     * 
     * @return 
     */
    public final double[][] getAllDataAsArray() {
        int c = myDataMap.keySet().size();
        int r = myDataSize;
        double[][] x = new double[r][c];
        int j = 0;
        for (String s : myDataMap.keySet()) {
            // get the column data
            double[] d = getData(s);
            // copy column data into the array
            for (int i = 0; i < r; i++) {
                x[i][j] = d[i];
            }
            // index to next column
            j++;
        }
        return x;
    }

    /** The data associated with the name. If the name is not
     *  in the map, the array will be null
     * 
     * @param name
     * @return 
     */
    public double[] getData(String name) {
        double[] x = myDataMap.get(name);
        if (x == null){
            return null;
        }
        double[] d = new double[x.length];
        System.arraycopy(x, 0, d, 0, x.length);
        return d;
    }

    /** Write a statistical summary of the data in the analyzer
     * 
     * @param out 
     */
    public final void writeSummaryStatistics(PrintWriter out) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        String hline = "--------------------------------------------------------------------------------------";
        out.println(hline);
        out.println();
        out.println("Statistical Summary Report");
        out.println(new Date());
        out.print("Sample Size: ");
        out.println(myDataSize);

        List<Statistic> stats = getStatistics();
        String format = "%-50s \t %12f \t %12f %n";

        if (!stats.isEmpty()) {
            out.println(hline);
            out.println();
            out.println(hline);
            out.printf("%-50s \t %12s \t %12s %n", "Name", "Average", "Std. Dev.");
            out.println(hline);

            for (Statistic stat : stats) {
                double avg = stat.getAverage();
                double std = stat.getStandardDeviation();
                String name = stat.getName();
                out.printf(format, name, avg, std);

            }
            out.println(hline);
        }
    }

        /** Write a statistical summary of the difference data in the analyzer
     * 
     * @param out 
     */
    public final void writeSummaryDifferenceStatistics(PrintWriter out) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        String hline = "--------------------------------------------------------------------------------------";
        out.println(hline);
        out.println();
        out.println("Statistical Summary Report for Differences");
        out.println(new Date());
        out.print("Sample Size: ");
        out.println(myDataSize);

        List<Statistic> stats = getPairedDifferenceStatistics();
        String format = "%-50s \t %12f \t %12f %n";

        if (!stats.isEmpty()) {
            out.println(hline);
            out.println();
            out.println(hline);
            out.printf("%-50s \t %12s \t %12s %n", "Name", "Average", "Std. Dev.");
            out.println(hline);

            for (Statistic stat : stats) {
                double avg = stat.getAverage();
                double std = stat.getStandardDeviation();
                String name = stat.getName();
                out.printf(format, name, avg, std);

            }
            out.println(hline);
        }
    }

    /** Write the data as a csv file
     * 
     * @param out 
     */
    public void writeDataAsCSVFile(PrintWriter out) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        int c = myDataMap.keySet().size();
        int r = 1;
        for (String s : myDataMap.keySet()) {
            out.print(s);
            if (r < c) {
                out.print(",");
            }
            r++;
        }
        out.println();
        double[][] data = getAllDataAsArray();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                out.print(data[i][j]);
                if (j < data[i].length - 1) {
                    out.print(",");
                }
            }
            out.println();
        }
    }

    public static void main(String args[]) {
        LinkedHashMap<String, double[]> data = new LinkedHashMap<String, double[]>();
        double[] d1 = {63.72, 32.24, 40.28, 36.94, 36.29, 56.94, 34.10, 63.36, 49.29, 87.20};
        double[] d2 = {63.06, 31.78, 40.32, 37.71, 36.79, 57.93, 33.39, 62.92, 47.67, 80.79};
        double[] d3 = {57.74, 29.65, 36.52, 35.71, 33.81, 51.54, 31.39, 57.24, 42.63, 67.27};
        double[] d4 = {62.63, 31.56, 39.87, 37.35, 36.65, 57.15, 33.30, 62.21, 47.46, 79.60};
        data.put("One", d1);
        data.put("Two", d2);
        data.put("Three", d3);
        data.put("Four", d4);

        MultipleComparisonAnalyzer mca = new MultipleComparisonAnalyzer(data);

        PrintWriter out = new PrintWriter(System.out, true);
//        PrintWriter out2 = JSL.makePrintWriter("test", "csv");
        mca.writeDataAsCSVFile(out);
        out.println();
        mca.writeSummaryStatistics(out);
        out.println();

        List<Interval> intervals = mca.getConfidenceIntervalsOfData(0.95);
        out.println("Confidence Intervals on Data");
        for (Interval i : intervals) {
            out.println(i);
        }

//        System.out.println(mca.computePairedDifferences());
//
//        double[] xd = mca.getPairedDifference("One", "Two");
//        for (double x : xd) {
//            System.out.println(x);
//        }

//        Statistic stat = mca.getPairedDifferenceStatistic("One", "Two");
//        List<Statistic> dstats = mca.getPairedDifferenceStatistics();
//        System.out.println(dstats);

        mca.writeSummaryDifferenceStatistics(out);

        List<Interval> intervals2 = mca.getConfidenceIntervalsOfDifferenceData(0.95);
        out.println("Confidence Intervals on Differences");
        for (Interval i : intervals2) {
            out.println(i);
        }
        out.println();
        out.println("Max variance = " + mca.getMaxVarianceOfDifferences());

        out.println("Min performer = " + mca.getNameOfMinimumAverageOfData());
        out.println("Min performance = " + mca.getMinimumAverageOfData());

        out.println("Max performer = " + mca.getNameOfMaximumAverageOfData());
        out.println("Max performance = " + mca.getMaximumAverageOfData());

        out.println("Min difference = " + mca.getNameOfMinumumAverageOfDifferences());
        out.println("Min difference value = " + mca.getMinimumOfAveragesOfDifferences());

        out.println("Max difference = " + mca.getNameOfMaximumAverageOfDifferences());
        out.println("Max difference value = " + mca.getMaximumOfAveragesOfDifferences());

    }
}

