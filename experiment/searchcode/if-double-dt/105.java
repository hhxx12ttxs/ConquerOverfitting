// Copyright (c) 1999 Frank Gerard
//    
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//    
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//    
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

package net.sf.hulp.profiler;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import net.sf.hulp.util.Markup;
import net.sf.hulp.util.StringTools;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A profiler implementation that accumulates data with millisecond or better accuracy.
 */
public class RealProfiler extends Profiler {
    private HashMap<Totaler, Totaler> mTotalers = new HashMap<Totaler, Totaler>(); // Key=Totaler, Value=Totaler
    public static final double[] HISTOGRAM_BINS = new double[30];
    private static final double HISTOGRAM_DENOMINATOR = 100.0d;
    
    static {
        // Initialize the histogram array
        for (int i = 0; i < HISTOGRAM_BINS.length; i++) {
            HISTOGRAM_BINS[i] = Math.pow(2.0d, (double) i) / HISTOGRAM_DENOMINATOR;
        }
        HISTOGRAM_BINS[HISTOGRAM_BINS.length - 1] = Double.MAX_VALUE;
    }
    
    /**
     * Returns the bin index following bin[i - 1] <= d < bin[i]
     * 
     * @param d value to find
     * @return index into bin array
     */
    public static final int histoIndex(double d) {
        if (d < 0.0d) {
            d = 0.0d;
        }
        if (d > RealProfiler.HISTOGRAM_BINS[RealProfiler.HISTOGRAM_BINS.length - 2]) {
            return RealProfiler.HISTOGRAM_BINS.length - 1;
        }
        
        // Bin equation:
        long k = (long) (HISTOGRAM_DENOMINATOR * d);
        
        int i = 0;
        while (k != 0) {
            k = (k >> 1);
            i++;
        }
        
        return i;
    }

    /**
     * One single measurement.
     */
    private class RealMeasurementV1 extends net.sf.hulp.measure.Measurement {
        private String  mTopic;
        private String  mSubTopic;
        private double  m_tStart;
        private double  m_dt;
        private boolean mDead;
        private Totaler mTotaler;

        /**
         * Constructor
         */
        public RealMeasurementV1(String topic, String subTopic, Totaler a) {
            mTopic = topic;
            mSubTopic = subTopic;
            m_tStart = Chrono.start();
            mTotaler = a;
        }

        /**
         * Resets the name as specified
         */
        public void setTopic(String topic) {
            mTopic = topic;
            mTotaler.decActive();
            mTotaler = getTotaler(this);
            mTotaler.incActive();
        }

        /**
         * Extends the name with a subtopic
         */
        public void setSubtopic(String subTopic) {
            mSubTopic = subTopic;
            mTotaler.decActive();
            mTotaler = getTotaler(this);
            mTotaler.incActive();
        }

        /**
         * Ends the time interval and adds the measurement to the profiler list.
         */
        public void end() {
            if (mDead) {
                return;
            }
            mDead = true;
            m_dt = Chrono.stop(m_tStart);
            
            if (mTotaler == null) {
                mTotaler = getTotaler(this);
            }
            
            mTotaler.add(this);
        }

        /**
         * @see java.lang.Object#hashCode()
         * 
         * Note: needs to be identical to Totaler.hashCode so that a lookup can be done
         * in the map to find a Totaler using a RealMeasurement as an argument; avoiding
         * an extra class (= object) saves on object instantiations
         */
        public int hashCode() {
            if (mSubTopic != null) {
                return mTopic.hashCode() + mSubTopic.hashCode();
            }
            else if (mTopic != null) {
                return mTopic.hashCode();
            } else {
                return 0;
            }
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         * 
         * Note: needs to be identical to Totaler.equals so that a lookup can be done
         * in the map to find a Totaler using a RealMeasurement as an argument; avoiding
         * an extra class (= object) saves on object instantiations
         */
        public boolean equals(Object o) {
            if (o instanceof RealMeasurementV1) {
                RealMeasurementV1 rhs = (RealMeasurementV1) o;
                return (mTopic == rhs.mTopic || (mTopic != null && mTopic.equals(rhs.mTopic)))
                && (mSubTopic == rhs.mSubTopic 
                    || (mSubTopic != null && rhs.mSubTopic != null && mSubTopic.equals(rhs.mSubTopic)));
                
            } else if (o instanceof Totaler) {
                Totaler rhs = (Totaler) o;
                return "".equals(rhs.mSource)
                && (mTopic == rhs.mName1 || (mTopic!= null && mTopic.equals(rhs.mName1)))
                && (mSubTopic == rhs.mSubname
                    || (mSubTopic != null && rhs.mSubname != null && mSubTopic.equals(rhs.mSubname)));
            } else {
                return false;
            }
        }
    }

    /**
     * One single measurement.
     */
    private class RealMeasurementV2 extends net.java.hulp.measure.Probe {
        private String  mSource;
        private String  mTopic1;
        private String  mSubTopic;
        private double  m_tStart;
        private double  m_dt;
        private boolean mDead;
        private Totaler mTotaler;

        /**
         * Constructor
         */
        public RealMeasurementV2(Class source, String topic, String subTopic, Totaler a) {
            mSource = source == null ? "" : source.getName();
            mTopic1 = topic;
            mSubTopic = subTopic;
            m_tStart = Chrono.start();
            mTotaler = a;
        }

        /**
         * Resets the name as specified
         */
        public void setTopic(String topic) {
            mTopic1 = topic;
            mTotaler.decActive();
            mTotaler = getTotaler(this);
            mTotaler.incActive();
        }

        /**
         * Extends the name with a subtopic
         */
        public void setSubtopic(String subTopic) {
            mSubTopic = subTopic;
            mTotaler.decActive();
            mTotaler = getTotaler(this);
            mTotaler.incActive();
        }

        /**
         * Ends the time interval and adds the measurement to the profiler list.
         */
        public void end() {
            if (mDead) {
                return;
            }
            mDead = true;
            m_dt = Chrono.stop(m_tStart);
            
            if (mTotaler == null) {
                mTotaler = getTotaler(this);
            }
            
            mTotaler.add(this);
        }

        /**
         * @see java.lang.Object#hashCode()
         * 
         * Note: needs to be identical to Totaler.hashCode so that a lookup can be done
         * in the map to find a Totaler using a RealMeasurement as an argument; avoiding
         * an extra class (= object) saves on object instantiations
         */
        public int hashCode() {
            if (mSubTopic != null) {
                return mSource.hashCode() + mTopic1.hashCode() + mSubTopic.hashCode();
            }
            else if (mTopic1 != null) {
                return mSource.hashCode() + mTopic1.hashCode();
            } else {
                return mSource.hashCode();
            }
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         * 
         * Note: needs to be identical to Totaler.equals so that a lookup can be done
         * in the map to find a Totaler using a RealMeasurement as an argument; avoiding
         * an extra class (= object) saves on object instantiations
         */
        public boolean equals(Object o) {
            if (o instanceof RealMeasurementV2) {
                RealMeasurementV2 rhs = (RealMeasurementV2) o;
                return mSource.equals(rhs.mSource) 
                && (mTopic1 == rhs.mTopic1 || (mTopic1 != null && mTopic1.equals(rhs.mTopic1)))
                && (mSubTopic == rhs.mSubTopic 
                    || (mSubTopic != null && rhs.mSubTopic != null && mSubTopic.equals(rhs.mSubTopic)));
                
            } else if (o instanceof Totaler) {
                Totaler rhs = (Totaler) o;
                return mSource.equals(rhs.mSource) 
                && (mTopic1 == rhs.mName1 || (mTopic1 != null && mTopic1.equals(rhs.mName1)))
                && (mSubTopic == rhs.mSubname
                    || (mSubTopic != null && rhs.mSubname != null && mSubTopic.equals(rhs.mSubname)));
            } else {
                return false;
            }
        }
    }

    /**
     * Accumulates measurements and keeps statistics
     */
    private static class Totaler implements Comparable, Cloneable {
        private String mSource;
        private String mName1;
        private String mSubname;
        private int    mN;
        private double m_dtSum;
        private double m_tFirst; // Start time of first measurement 
        private double m_dtFirst; // duration of first measurement
        private double m_tLast; // Start time of most recent measurement 
        private double m_dtLast; // duration of most recent measurement
        private int    m_nActive;
        private int[]  mHistogram;
        
        /**
         * Constructs a new Totaler
         */
        public Totaler(String source, String name, String subname) {
            mSource = source;
            mName1 = name;
            mSubname = subname;
            mHistogram = new int[HISTOGRAM_BINS.length];
        }
        
        /**
         * @see java.lang.Object#clone()
         */
        public synchronized Object clone() {
            Totaler ret = new Totaler(mSource, mName1, mSubname);
            ret.mN = mN;
            ret.m_dtSum = m_dtSum;
            ret.m_tFirst = m_tFirst;
            ret.m_dtFirst = m_dtFirst;
            ret.m_tLast = m_tLast;
            ret.m_dtLast = m_dtLast;
            ret.m_nActive = m_nActive;
            ret.mHistogram = new int[mHistogram.length];
            System.arraycopy(mHistogram, 0, ret.mHistogram, 0, mHistogram.length);
            return ret;
        }

        private synchronized void incActive() {
            m_nActive++;
        }

        private synchronized void decActive() {
            m_nActive--;
        }

        /**
         * Adds a duration to accumulate
         */
        private synchronized void add(RealMeasurementV1 d) {
            if (mN == 0) {
                m_dtFirst = d.m_dt;
                m_tFirst = d.m_tStart;
            }

            mN++;
            m_tLast = d.m_tStart;
            m_dtLast = d.m_dt;
            m_dtSum += d.m_dt;
            m_nActive--;
            mHistogram[RealProfiler.histoIndex(d.m_dt)]++;
        }
        
        /**
         * Adds a duration to accumulate
         */
        private synchronized void add(RealMeasurementV2 d) {
            if (mN == 0) {
                m_dtFirst = d.m_dt;
                m_tFirst = d.m_tStart;
            }

            mN++;
            m_tLast = d.m_tStart;
            m_dtLast = d.m_dt;
            m_dtSum += d.m_dt;
            m_nActive--;
            mHistogram[RealProfiler.histoIndex(d.m_dt)]++;
        }
        
        public synchronized double getAverage() {
            return m_dtSum / (mN == 0 ? 1 : mN);
        }
        
        public synchronized double getAveragePrime() {
            return mN <= 1 ? m_dtSum :  (m_dtSum - m_dtFirst) / (mN - 1);
        }
        
        public synchronized double getLoad() {
            double load = Double.NaN;
            if (getTimespan() > 1E-8 && getTimespan() != Double.NaN) {
                if (mN > 1) {
                    load = mN * getAverage() / getTimespan();
                }
            }
            return load;
        }
        
        public synchronized double getTimespan() {
            double dt = Double.NaN;
            if (mN >= 1) {
                double dtLast  = Chrono.stop(m_tLast);
                double dtFirst = Chrono.stop(m_tFirst);
                dt = dtFirst - dtLast + m_dtLast;
            }
            return dt;
        }

        public synchronized double getThroughput() {
            double throughput = Double.NaN;
            if (getTimespan() > 1E-8 && getTimespan() != Double.NaN) {
                throughput = mN / getTimespan() * 1000.0;
            }
            return throughput;
        }
        
        public synchronized double getMedian() {
            // Special case: all in first bin
            if (mHistogram[0] == mN) {
                return 0.0;
            }

            // Special case: all in last bin
            if (mHistogram[HISTOGRAM_BINS.length - 1] == mN) {
                return mHistogram[HISTOGRAM_BINS.length - 1];
            }
            
            // Find the half point
            double half = mN * 0.5d;
            int sum = 0;
            
            // Find the x and y coordinates of the point before and after the half
            // point so that we can use linear interpolation to find an estimate for
            // the half point. X is moving sum, Y = bin number
            int x1 = 0;
            int y1 = 0;
            int x2 = 0;
            int y2 = 0;

            for (int i = 0; i < mHistogram.length; i++) {
                sum += mHistogram[i];
                
                // Special case: exact match
                if (sum == half) {
                    return HISTOGRAM_BINS[i];
                }
                
                if (sum <= half) {
                    x1 = sum;
                    y1 = i;
                } else {
                    x2 = sum;
                    y2 = i;
                    break;
                }
            }
            
            // Use linear interpolation in the bin number domain to estimate the 
            // half point
            double y = (double) y1 
                + ((double) y2 - (double) y1) 
                / ((double) x2 - (double) x1) 
                * (half - (double) x1);
            
            // y now is a fractional bin number. Convert to a time quantity using the 
            // bin equation
            return Math.pow(2.0d, y) / HISTOGRAM_DENOMINATOR;
        }
        
        public String getName() {
            return mSource + (mSource.length() == 0 ? "" : ".") + mName1;
        }

        /**
         * Prints out a row
         */
        synchronized private void dump(final PrintWriter out, final Markup f)
                throws IOException {
            out.print(f.beginRow()
              + getName()
              + f.tab() + mN
              + f.tab() + Chrono.format(m_dtSum)
              + f.tab() + Chrono.format(getAveragePrime())
              + f.tab() + Chrono.format(getMedian())
              + f.tab() + m_nActive
              + f.tab() + f.fine(mSubname)
              + f.tab() + f.fine(Chrono.format(m_dtFirst))
              + f.tab() + f.fine(Chrono.format(getAverage()))
              + f.tab() + Chrono.format(getThroughput())
              + f.tab() + f.fine(Chrono.format(getTimespan()))
              + f.tab() + f.fine(Chrono.format(getLoad()))
              );
            for (int i = 0; i < mHistogram.length; i++) {
                out.print(f.tab() + mHistogram[i]);
            }
            out.print(f.endRow());
        }
        
        /**
         * @see java.lang.Object#hashCode()
         * 
         * Note: needs to be identical to RealMeasurement.hashCode so that a lookup can be done
         * in the map to find a Totaler using a RealMeasurement as an argument; avoiding
         * an extra class (= object) saves on object instantiations
         */
        public int hashCode() {
            if (mSubname != null) {
                return mSource.hashCode() + mName1.hashCode() + mSubname.hashCode();
            }
            else if (mName1 != null) {
                return mSource.hashCode() + mName1.hashCode();
            } else {
                return mSource.hashCode();
            }
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         * 
         * Note: needs to be identical to RealMeasurement.equals so that a lookup can be done
         * in the map to find a Totaler using a RealMeasurement as an argument; avoiding
         * an extra class (= object) saves on object instantiations
         */
        public boolean equals(Object o) {
            if (o instanceof RealMeasurementV1) {
                RealMeasurementV1 rhs = (RealMeasurementV1) o;
                return "".equals(mSource)
                && (mName1 == rhs.mTopic || (mName1 != null && mName1.equals(rhs.mTopic)))
                && (mSubname == rhs.mSubTopic 
                    || (mSubname != null && rhs.mSubTopic != null && mSubname.equals(rhs.mSubTopic)));
            } else if (o instanceof RealMeasurementV2) {
                RealMeasurementV2 rhs = (RealMeasurementV2) o;
                return mSource.equals(rhs.mSource) 
                && (mName1 == rhs.mTopic1 || (mName1 != null && mName1.equals(rhs.mTopic1)))
                && (mSubname == rhs.mSubTopic 
                    || (mSubname != null && rhs.mSubTopic != null && mSubname.equals(rhs.mSubTopic)));
            } else if (o instanceof Totaler) {
                Totaler rhs = (Totaler) o;
                return mSource.equals(rhs.mSource) 
                && (mName1 == rhs.mName1 || (mName1 != null && mName1.equals(rhs.mName1)))
                && (mSubname == rhs.mSubname
                    || (mSubname != null && rhs.mSubname != null && mSubname.equals(rhs.mSubname)));
            } else {
                return false;
            }
        }

        /**
         * @param rhs_ object to compare to
         * @return lexical order
         */
        public int compareTo(Object rhs_) {
            Totaler lhs = this;
            Totaler rhs = ((Totaler) rhs_);
            
            int ret = lhs.mSource.compareTo(rhs.mSource);
            
            if (ret == 0) {
                if (lhs.mName1 != rhs.mName1) {
                    if (lhs.mName1 == null) {
                        ret = -1;
                    } else if (rhs.mName1 == null) {
                        ret = 1;
                    } else {
                        ret = lhs.mName1.compareTo(rhs.mName1); 
                    }
                }
            }
            
            if (ret == 0) {
                // same... check subtype
                if (lhs.mSubname != null && rhs.mSubname != null) {
                    ret = lhs.mSubname.compareTo(rhs.mSubname);
                }
                else {
                    if (lhs.mSubname == null && rhs.mSubname == null) {
                        ret = 0;
                    }
                    if (lhs.mSubname == null) {
                        ret = 1;
                    }
                    else if (rhs.mSubname == null) {
                        return -1;
                    }
                }
            }
            return ret;
        }
    }

    private static void header(final PrintWriter out, final Markup f, double overhead)
    throws IOException {
        // Super header
        out.print(f.beginRow(12) + "Profiler data ("
            + StringTools.timeString(System.currentTimeMillis())
            + "; measurement overhead: " + Chrono.format(overhead) + " ms)");
        out.print(f.tab(HISTOGRAM_BINS.length) + "Histogram data; bins in ms, counts in n(t)/N");
        out.print(f.endRow());

        // Column header
        out.print(f.beginHRow()
            + H_TOPIC
            + f.tab() + H_N
            + f.tab() + H_TOTALTIME
            + f.tab() + H_AVERAGEPRIME
            + f.tab() + H_MEDIAN
            + f.tab() + H_ACT
            + f.tab() + H_SUBTOPIC
            + f.tab() + f.fine(H_FIRST)
            + f.tab() + f.fine(H_AVERAGE)
            + f.tab() + H_THROUGHPUT
            + f.tab() + f.fine(H_TIMESPAN)
            + f.tab() + f.fine(H_LOAD)
        );

        for (int i = 0; i < HISTOGRAM_BINS.length; i++) {
            out.print(f.tab());
            if (HISTOGRAM_BINS[i] == Double.MAX_VALUE) {
                out.print("oo");
            } else {
                out.print(HISTOGRAM_BINS[i]);
            }
        }
        
        out.print(f.endRow());
    }

    /**
     * Returns an existing or newly created Totaler
     */
    private Totaler getTotaler(RealMeasurementV1 m) {
        Totaler ret = null;
        synchronized (mTotalers) {
            ret = (Totaler) mTotalers.get(m);
            if (ret == null) {
                ret = new Totaler("", m.mTopic, m.mSubTopic);
                mTotalers.put(ret, ret);
            }
        }
        return ret;
    }

    /**
     * Returns an existing or newly created Totaler
     */
    private Totaler getTotaler(RealMeasurementV2 m) {
        Totaler ret = null;
        synchronized (mTotalers) {
            ret = (Totaler) mTotalers.get(m);
            if (ret == null) {
                ret = new Totaler(m.mSource, m.mTopic1, m.mSubTopic);
                mTotalers.put(ret, ret);
            }
        }
        return ret;
    }

    /**
     * @see net.sf.hulp.measure.Measurement.Factory#create(java.lang.String, java.lang.String)
     */
    public net.sf.hulp.measure.Measurement create(String topic, String subTopic) {
        RealMeasurementV1 ret = new RealMeasurementV1(topic, subTopic, null);
        Totaler a = getTotaler(ret);
        a.incActive();
        ret.mTotaler = a;
        return ret;
    }
    
    /**
     * @see net.sf.hulp.measure.Measurement.Factory#getData(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public TabularData getData(List<Pattern[]> criteria) {
        try {
            Map<Totaler, Totaler> m;
            synchronized (mTotalers) {
                m = (Map<Totaler, Totaler>) mTotalers.clone();
            }

            int N = 13;
            List<CompositeData> ret = new ArrayList<CompositeData>();
            String[] names = new String[N + HISTOGRAM_BINS.length];
            SimpleType[] types = new SimpleType[N + HISTOGRAM_BINS.length];
            String[] descrs = new String[N + HISTOGRAM_BINS.length];
            CompositeType type = null;

            for (Totaler t: m.values()) {
                boolean matches = false;
                for (Pattern[] p: criteria) {
                    if (p[0].matcher(t.mSource).matches()
                        && p[1].matcher(t.mName1).matches() 
                        && p[2].matcher(t.mSubname == null ? "" : t.mSubname).matches()) {
                        matches = true;
                        break;
                    }
                }

                if (matches) {
                    Object[] values = new Object[N + HISTOGRAM_BINS.length];
                    int j = 0;

                    names[j] = H_SOURCE;
                    types[j] = SimpleType.STRING;
                    descrs[j] = H_SOURCE;
                    values[j++] = t.mSource; 

                    names[j] = H_TOPIC;
                    types[j] = SimpleType.STRING;
                    descrs[j] = H_TOPIC;
                    values[j++] = t.mName1; 

                    names[j] = H_N;
                    types[j] = SimpleType.INTEGER;
                    descrs[j] = H_N;
                    values[j++] = t.mN;

                    names[j] = H_TOTALTIME;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_TOTALTIME;
                    values[j++] = t.m_dtSum;

                    names[j] = H_AVERAGEPRIME;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_AVERAGEPRIME;
                    values[j++] = t.getAveragePrime();

                    names[j] = H_MEDIAN;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_MEDIAN;
                    values[j++] = t.getMedian();

                    names[j] = H_ACT;
                    types[j] = SimpleType.INTEGER;
                    descrs[j] = H_ACT;
                    values[j++] = t.m_nActive;

                    names[j] = H_SUBTOPIC;
                    types[j] = SimpleType.STRING;
                    descrs[j] = H_SUBTOPIC;
                    values[j++] = t.mSubname == null ? "" : t.mSubname;

                    names[j] = H_FIRST;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_FIRST;
                    values[j++] = t.m_dtFirst;

                    names[j] = H_AVERAGE;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_AVERAGE;
                    values[j++] = t.getAverage();

                    names[j] = H_THROUGHPUT;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_THROUGHPUT;
                    values[j++] = t.getThroughput();

                    names[j] = H_TIMESPAN;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_TIMESPAN;
                    values[j++] = t.getTimespan();

                    names[j] = H_LOAD;
                    types[j] = SimpleType.DOUBLE;
                    descrs[j] = H_LOAD;
                    values[j++] = t.getLoad();
                    
                    assertTrue(N == j);

                    for (int i = 0; i < HISTOGRAM_BINS.length; i++) {
                        names[j] = HISTOGRAM_BINS[i] == Double.MAX_VALUE ? "oo" : "h_" + Double.toString(HISTOGRAM_BINS[i]);
                        types[j] = SimpleType.INTEGER;
                        descrs[j] = "Histogram data; bins in ms";
                        values[j++] = t.mHistogram[i];
                    }
                    assertTrue(j == values.length);

                    if (type == null) {
                        type = new CompositeType("Performance metrics row", "S", names, descrs, types);
                    }

                    CompositeDataSupport row = new CompositeDataSupport(type, names, values);
                    ret.add(row);
                }
            }
            
            if (ret.isEmpty()) {
                return null;
            }

            // Calculate overhead
            int nTotal = 0;
            for (Totaler t: m.values()) {
                nTotal += t.mN;
            }
            double overhead = (double) nTotal * Chrono.getOverheadPerMeasurement();

            // Header
            String descr = "Profiler data ("
                + StringTools.timeString(System.currentTimeMillis())
                + "; measurement overhead: " + Chrono.format(overhead) + " ms)";
            
            TabularType tabletype = new TabularType(
                "Profiler results", 
                descr, 
                type, new String[] {H_SOURCE, H_TOPIC, H_SUBTOPIC}
            );
            
            TabularData t = new TabularDataSupport(tabletype);
            t.putAll(ret.toArray(new CompositeData[0]));
            
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Dumps out all measurements; clones the table so that measurements can continue.
     */
    @SuppressWarnings("unchecked")
    public void dump(final PrintWriter out, final Markup f) throws IOException {
        Map m = null;
        synchronized (mTotalers) {
            m = (Map) mTotalers.clone();
        }
        SortedSet<Totaler> s = new TreeSet<Totaler>(m.values());
        
        // Calculate overhead
        int nTotal = 0;
        for (Iterator i = s.iterator(); i.hasNext(); ) {
            Totaler a = (Totaler) i.next();
            nTotal += a.mN;
        }
        double overhead = (double) nTotal * Chrono.getOverheadPerMeasurement();

        // Header
        header(out, f, overhead);
        
        // Real data
        for (Iterator i = s.iterator(); i.hasNext(); ) {
            Totaler a = (Totaler) i.next();
            a.dump(out, f);
        }
    }

    /**
     * Clears all accumulated data
     */
    public void clear() {
        synchronized (mTotalers) {
            mTotalers.clear();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void clearData(List<Pattern[]> criteria) {
        // Make a list of all objects matching the criteria
        List<Totaler> toclear = new ArrayList<Totaler>();

        Map<Totaler, Totaler> m;
        synchronized (mTotalers) {
            m = (Map<Totaler, Totaler>) mTotalers.clone();
        }

        for (Totaler t: m.values()) {
            for (Pattern[] p: criteria) {
                if (p[0].matcher(t.mSource).matches()
                    && p[1].matcher(t.mName1).matches() 
                    && p[2].matcher(t.mSubname == null ? "" : t.mSubname).matches()) {
                    toclear.add(t);
                    break;
                }
            }
        }
        
        // Remove all objects matching the criteria
        synchronized (mTotalers) {
            for (Totaler t : toclear) {
                mTotalers.remove(t);
            }
        }
    }

    /**
     * @see net.sf.hulp.profiler.Profiler#dump()
     */
    public Map[] dump() {
        Map m = null;
        synchronized (mTotalers) {
            m = (Map) mTotalers.clone();
        }
        
        Map[] ret = new Map[m.size()];
        int i = 0;
        for (Iterator iter = m.values().iterator(); iter.hasNext();) {
            Totaler t = (Totaler) iter.next();
            Map<String, Comparable> e = new HashMap<String, Comparable>();
            ret[i++] = e;
            
            e.put(H_TOPIC, t.getName());
            e.put(H_SUBTOPIC, t.mSubname);
            e.put(H_N, new Integer(t.mN));
            e.put(H_TOTALTIME, new Double(t.m_dtSum));
            e.put(H_AVERAGEPRIME, new Double(t.getAveragePrime()));
            e.put(H_ACT, new Integer(t.m_nActive));
            e.put(H_THROUGHPUT, new Double(t.getThroughput()));
            
            e.put(H_FIRST, new Double(t.m_dtFirst));
            e.put(H_AVERAGE, new Double(t.getAverage()));
            e.put(H_TIMESPAN, new Double(t.getTimespan()));
            e.put(H_LOAD, new Double(t.getLoad()));
        }
        
        return ret;
    }
    
    /**
     * @see net.sf.hulp.profiler.Profiler#dump(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Map dump(String topicSubtopicSeparator) {
        Map<String, Map<String, Object>> ret = new HashMap();
        Map[] entries = dump();
        
        for (int i = 0; i < entries.length; i++) {
            String key = entries[i].get(H_TOPIC) + topicSubtopicSeparator + entries[i].get(H_SUBTOPIC);
            ret.put(key, entries[i]);
        }
        return ret;
    }
    
    /**
     * For testing
     */
    private static void assertTrue(boolean b) {
        if (!b) {
            throw new RuntimeException();
        }
    }
    
    /**
     * For testing 
     */
    public void testCompare() {
        RealMeasurementV1 a = new RealMeasurementV1(null, null, null);
        Totaler b = new Totaler("", null, null);
       
        assertTrue(a.equals(b));
        
        a.mTopic = "a";  //a=a,null; b=null,null
        assertTrue(!a.equals(b));
        assertTrue(!b.equals(a));
        assertTrue(a.hashCode() != b.hashCode());
        a.mSubTopic = "b"; //a=a,b; b=null,null
        assertTrue(!a.equals(b));
        assertTrue(!b.equals(a));
        assertTrue(a.hashCode() != b.hashCode());
        b.mName1 = "a"; //a=a,b; b=a,null
        assertTrue(!a.equals(b));
        assertTrue(!b.equals(a));
        assertTrue(a.hashCode() != b.hashCode());
        b.mSubname = "b"; //a=a,b; b=a,b
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());
        a.mSubTopic = null; //a=a,null; b=a,b
        assertTrue(!a.equals(b));
        assertTrue(!b.equals(a));
        assertTrue(a.hashCode() != b.hashCode());
    }

    public void help(PrintWriter out, Markup f) {
        if (!net.sf.hulp.measure.Measurement.isInstalled()) {
            out.println("All classes are available but the profiler is not enabled." + f.br());
            out.println("Set the following argument to the JVM:<br>");
            out.println(f.beginArea());
            out.println(f.beginPre());
            out.println("-Dnet.sf.hulp.profiler=1");
            out.println(f.endPre() +  f.br());
            out.println(f.endArea());
        } else {
            out.println(f.beginTable(Markup.TABLESTYLE_SIMPLE));
            out.println(f.beginRow() + "Profiler enabled" + f.tab() + "true" + f.endRow());
            out.println(f.beginRow() + "High resolution timer available" + f.tab() + Chrono.isHighResTimerAvailable() + f.endRow());
            out.println(f.beginRow() + "Timer overhead" + f.tab() + Chrono.getOverheadPerMeasurement() + " ms per measurement" + f.endRow());
            out.println(f.endTable());
            
            out.println(f.br());
            out.println(f.br());
            
            out.println("The profiler is based on time probes that are placed strategically in");
            out.println("the code base. The probles have a name (topic) and an optional sub-name");
            out.println("(subtopic). A probe essentially measures the time difference between a");
            out.println("start and stop time, just like a stopwatch. The profiler aggregates");
            out.println("these measured time differences <span style=\"font-family: monospace;\">dt</span>.<br>");
            out.println("<br>");
            out.println("Click on <span style=\"font-weight: bold;\">dump</span> to get a listing");
            out.println("of all aggregated results of all probes in HTML format.<br>");
            out.println("Click on <span style=\"font-weight: bold;\">dump text</span> to get the");
            out.println("same data in tab-delimited text; this can be used in a spreadsheet.<br>");
            out.println("Click on <span style=\"font-weight: bold;\">clear</span> to reset all");
            out.println("probes and remove them from memory<br>");
            out.println("<br>");
            out.println("The following columns are displayed in the dump:<br>");
            out.println("<br>");
            out.println("<table style=\"width: 893px; height: 28px;\" bordercolordark=\"#FFFFFF\"");
            out.println("bordercolorlight=\"#FFFFFF\" border=\"0\" bordercolor=\"#ffffff\"");
            out.println("cellpadding=\"2\" cellspacing=\"2\">");
            out.println("<tbody>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">n</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">Number of measurements (or <tt>N</tt>), i.e. the number");
            out.println("of <tt>dt</tt>-s, i.e. the number of times that <span");
            out.println("style=\"font-family: monospace;\">Measurement.begin()</span> - <span");
            out.println("style=\"font-family: monospace;\">end()</span> was called. <br>");
            out.println("</td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">total");
            out.println("time (ms)</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">the sum of all <tt>dt</tt>-s&nbsp; </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">average'");
            out.println("(ms)</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">(the sum of all <tt>dt</tt>-s minus the first <tt>dt</tt>)");
            out.println("divided by <tt>N</tt>.");
            out.println("The first measurement is discounted because it typically includes");
            out.println("classloading times and distorts the results considerably. If there's");
            out.println("only one measurement, the first measurement is not discounted and the");
            out.println("value should be equal to total time </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">act</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">the number of measurement objects on which <tt>begin()</tt>");
            out.println("was called but not <tt>end()</tt>.");
            out.println("This indicates the number of active measurements&nbsp; </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">sub");
            out.println("topic</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">the name of the measurement specified in the second");
            out.println("argument of <tt>begin()</tt> or in <tt>setSubTopic()</tt>&nbsp; </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">first</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">the first <tt>dt</tt>. This is interesting because it");
            out.println("may include special initializations <br>");
            out.println("</td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">average</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">sum of all <tt>dt</tt>-s divided by N; this does not");
            out.println("discount the first measurement&nbsp; </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">throughput</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\"><span style=\"font-family: monospace;\">N</span> divided by");
            out.println("(<tt>tlast</tt> - <tt>tfirst</tt>); this is the average throughput.");
            out.println("This number is meaningful if there were no long pauses in");
            out.println("processing.&nbsp; </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\"><tt>tlast</tt>");
            out.println("- <tt>tfirst</tt></td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">the wallclock time of the first measurement's begin()");
            out.println("method is tracked as <tt>tfirst</tt> and the wallclock time of the");
            out.println("last measurement's end() method is tracked as <tt>tlast</tt>&nbsp; </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">Load</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">The sum of all <tt>dt</tt>-s divided by (<tt>tlast</tt>");
            out.println("- <tt>tfirst</tt>).");
            out.println("This is a measure of concurrency: the higher the number, the greater");
            out.println("the concurrency. In a single threaded scenario this number can never");
            out.println("exceed 1.&nbsp; </td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td class=\"tbrwlt\"");
            out.println("style=\"vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);\">Histograms</td>");
            out.println("<td style=\"color: rgb(0, 0, 0);\" class=\"tbrwlt\" bgcolor=\"#e5eaed\"");
            out.println("valign=\"top\">See below<br>");
            out.println("</td>");
            out.println("</tr>");
            out.println("</tbody>");
            out.println("</table>");
            out.println("<br>");
            out.println("Data is tracked for histograms. There are 30 bins (0 - 29). The times");
            out.println("are on an exponential scale: 0.01 * 2<sup>bin</sup>&nbsp; where <span");
            out.println("style=\"font-family: monospace;\">bin</span> is the bin number.");
            out.println("");        
        }
    }

    public net.java.hulp.measure.Probe createV2(int level, Class source, String topic, String subtopic) {
        RealMeasurementV2 ret = new RealMeasurementV2(source, topic, subtopic, null);
        Totaler a = getTotaler(ret);
        a.incActive();
        ret.mTotaler = a;
        return ret;
    }

//The profiler is based on time probes that are placed strategically in
//the code base. The probles have a name (topic) and an optional sub-name
//(subtopic). A probe essentially measures the time difference between a
//start and stop time, just like a stopwatch. The profiler aggregates
//these measured time differences <span style="font-family: monospace;">dt</span>.<br>
//<br>
//Click on <span style="font-weight: bold;">dump</span> to get a listing
//of all aggregated results of all probes in HTML format.<br>
//Click on <span style="font-weight: bold;">dump text</span> to get the
//same data in tab-delimited text; this can be used in a spreadsheet.<br>
//Click on <span style="font-weight: bold;">clear</span> to reset all
//probes and remove them from memory<br>
//<br>
//The following columns are displayed in the dump:<br>
//<br>
//<table style="width: 893px; height: 28px;" bordercolordark="#FFFFFF"
//bordercolorlight="#FFFFFF" border="0" bordercolor="#ffffff"
//cellpadding="2" cellspacing="2">
//<tbody>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">n</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">Number of measurements (or <tt>N</tt>), i.e. the number
//of <tt>dt</tt>-s, i.e. the number of times that <span
//style="font-family: monospace;">Measurement.begin()</span> - <span
//style="font-family: monospace;">end()</span> was called. <br>
//</td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">total
//time (ms)</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">the sum of all <tt>dt</tt>-s&nbsp; </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">average'
//(ms)</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">(the sum of all <tt>dt</tt>-s minus the first <tt>dt</tt>)
//divided by <tt>N</tt>.
//The first measurement is discounted because it typically includes
//classloading times and distorts the results considerably. If there's
//only one measurement, the first measurement is not discounted and the
//value should be equal to total time </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">act</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">the number of measurement objects on which <tt>begin()</tt>
//was called but not <tt>end()</tt>.
//This indicates the number of active measurements&nbsp; </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">sub
//topic</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">the name of the measurement specified in the second
//argument of <tt>begin()</tt> or in <tt>setSubTopic()</tt>&nbsp; </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">first</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">the first <tt>dt</tt>. This is interesting because it
//may include special initializations <br>
//</td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">average</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">sum of all <tt>dt</tt>-s divided by N; this does not
//discount the first measurement&nbsp; </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">throughput</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top"><span style="font-family: monospace;">N</span> divided by
//(<tt>tlast</tt> - <tt>tfirst</tt>); this is the average throughput.
//This number is meaningful if there were no long pauses in
//processing.&nbsp; </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);"><tt>tlast</tt>
//- <tt>tfirst</tt></td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">the wallclock time of the first measurement's begin()
//method is tracked as <tt>tfirst</tt> and the wallclock time of the
//last measurement's end() method is tracked as <tt>tlast</tt>&nbsp; </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">Load</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">The sum of all <tt>dt</tt>-s divided by (<tt>tlast</tt>
//- <tt>tfirst</tt>).
//This is a measure of concurrency: the higher the number, the greater
//the concurrency. In a single threaded scenario this number can never
//exceed 1.&nbsp; </td>
//</tr>
//<tr>
//<td class="tbrwlt"
//style="vertical-align: top; background-color: rgb(229, 234, 237); color: rgb(0, 0, 0);">Histograms</td>
//<td style="color: rgb(0, 0, 0);" class="tbrwlt" bgcolor="#e5eaed"
//valign="top">See below<br>
//</td>
//</tr>
//</tbody>
//</table>
//<br>
//Data is tracked for histograms. There are 30 bins (0 - 29). The times
//are on an exponential scale: 0.01 * 2<sup>bin</sup>&nbsp; where <span
//style="font-family: monospace;">bin</span> is the bin number.

}

