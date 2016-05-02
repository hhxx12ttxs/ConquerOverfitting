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
// 
// Copyright (c) 2007 Sun Microsystems

package net.sf.hulp.test;

import net.sf.hulp.measure.Measurement;
import net.sf.hulp.profiler.Profiler;
import net.sf.hulp.profiler.RealProfiler;
import net.sf.hulp.profiler.Chrono;
import net.sf.hulp.util.Markup;
import net.sf.hulp.util.MarkupHtml;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import junit.framework.TestCase;

public class Test extends TestCase {
    
    public void testAcc() throws Throwable {
        double d0 = Chrono.start();
        long t0 = System.currentTimeMillis();
        Thread.sleep(500);
        double dd = Chrono.stop(d0);
        long dt = System.currentTimeMillis() - t0;
        System.out.println("milli: " + dt);
        System.out.println("nano: " + dd);
        
        assertTrue(dt < 650 && dt > 400);
        assertTrue(dd < 520 && dt > 480);
    }
    
    public void testMeas() throws Throwable {
        System.setProperty("net.sf.hulp.profiler", "1");
        Profiler.get().clear();
        Measurement m1 = Measurement.begin("m1");
        Measurement m1a = Measurement.begin("m1");
        Measurement m2 = Measurement.begin("m2");
        Thread.sleep(500);
        m1.end();

        m1 = Measurement.begin("m1");
        Thread.sleep(100);
        m2.end();
        m1.end();
        m1a.end();

        Markup f = new Markup();
        f.beginTable(MarkupHtml.TABLESTYLE_BARE);
        PrintWriter p = new PrintWriter(System.out);
        Profiler.get().dump(p, f);
        p.flush();

        System.out.println();
        
        Map m = Profiler.get().dump("::");
        Map m1result = (Map) m.get("m1" + "::" + null);
        assertTrue(((Integer) m1result.get(Profiler.H_N)).intValue() == 3);
        assertTrue(((Integer) m1result.get(Profiler.H_ACT)).intValue() == 0);
    }
    
    public void testHistogram() throws Throwable {
 //       System.setProperty("net.sf.hulp.profiler", "1");
        Profiler.get().clear();

        for (int i = 0; i < 10000; i++) {
            Measurement m1 = Measurement.begin("sleep(1)");
            Thread.sleep(1);
            m1.end();
        }

        for (int i = 0; i < 10000; i++) {
            Measurement m1 = Measurement.begin("yield");
            Thread.sleep(1);
            m1.end();
        }

        for (int i = 0; i < 250000; i++) {
            Measurement m1 = Measurement.begin("alloc long[100]");
            long[] buf = new long[100];
            if (buf.length == 0) {
                throw new RuntimeException();
            }
            m1.end();
        }

        for (int i = 0; i < 25000; i++) {
            Measurement m1 = Measurement.begin("alloc long[10000]");
            long[] buf = new long[10000];
            if (buf.length == 0) {
                throw new RuntimeException();
            }
            m1.end();
        }

        for (int i = 0; i < 250; i++) {
            Measurement m1 = Measurement.begin("alloc long[1000000]");
            long[] buf = new long[1000000];
            if (buf.length == 0) {
                throw new RuntimeException();
            }
            m1.end();
        }

        Markup f = new Markup();
        f.beginTable(MarkupHtml.TABLESTYLE_BARE);
        PrintWriter p = new PrintWriter(System.out);
        Profiler.get().dump(p, f);
        p.flush();
    }
    
    public void testActBug() throws Throwable {
        System.setProperty("net.sf.hulp.profiler", "1");
        Profiler.get().clear();
        Measurement m1 = Measurement.begin("m1");
        m1.end();

        {
            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(((Integer) m1result.get(Profiler.H_ACT)).intValue() == 0);
        }
        
        m1 = Measurement.begin("m1");
        m1.setSubtopic("sub");
        m1.end();

        {
            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(((Integer) m1result.get(Profiler.H_ACT)).intValue() == 0);
        }
        {
            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + "sub");
            assertTrue(((Integer) m1result.get(Profiler.H_ACT)).intValue() == 0);
        }
        
        
        m1 = Measurement.begin(null);
        Measurement m2 = Measurement.begin(null);
        m2.end();
        m1.setTopic("m1");
        m1.setSubtopic("sub");
        m1.end();

        {
            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(((Integer) m1result.get(Profiler.H_ACT)).intValue() == 0);
        }
        {
            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + "sub");
            assertTrue(((Integer) m1result.get(Profiler.H_ACT)).intValue() == 0);
        }

        Markup f = new Markup();
        f.beginTable(MarkupHtml.TABLESTYLE_BARE);
        PrintWriter p = new PrintWriter(System.out);
        Profiler.get().dump(p, f);
        p.flush();
    }
    
    public void testSpeed() throws Throwable {
        System.out.println("Speed test");
        String s = "abcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabc";
        s = "/" + s + s + s;
        System.out.println("String length: " + s.length());

        // No timing
        {
            double d0 = Chrono.start();
            int N = 1000000;
            for (int i = 0; i < N; i++) {
                int k = s.lastIndexOf('/');
                if (k < 0) {
                    throw new Throwable();
                }
            }
            double dd = Chrono.stop(d0);
            System.out.println("no timing: " + dd + " ms total; " + (dd / (double) N * (double) 1000000.0) + " ns per loop");
        }

//        // Nanotime
//        {
//            double d0 = Chrono.start();
//            long sum = 0;
//            int N = 1000000;
//            for (int i = 0; i < N; i++) {
//                long d1 = System.nanoTime();
//                int k = s.lastIndexOf('/');
//                if (k < 0) {
//                    throw new Throwable();
//                }
//                d1 = System.nanoTime() - d1;
//                sum += d1;
//            }
//            double dd = Chrono.stop(d0);
//            System.out.println("sys nano : " + dd + " ms total; " + (dd / (double) N * (double) 1000000.0) + " ns per loop; sum=" + sum);
//        }
//
        // Chrono time
        {
            double d0 = Chrono.start();
            double sum = 0.0;
            int N = 1000000;
            for (int i = 0; i < N; i++) {
                double d1 = Chrono.start();
                int k = s.lastIndexOf('/');
                if (k < 0) {
                    throw new Throwable();
                }
                d1 = Chrono.stop(d1);
                sum += d1;
            }
            double dd = Chrono.stop(d0);
            System.out.println("chrono   : " + dd + " ms total; " + (dd / (double) N * (double) 1000000.0) + " ns per loop; sum=" + sum);
        }

        // Measurement time
        {
            System.setProperty("net.sf.hulp.profiler", "1");
            Profiler.get().clear();
            Measurement m0 = Measurement.begin("void");
            m0.end();
            
            double d0 = Chrono.start();
            int N = 1000000;
            for (int i = 0; i < N; i++) {
                Measurement m1 = Measurement.begin("m1");
                int k = s.lastIndexOf('/');
                if (k < 0) {
                    throw new Throwable();
                }
                m1.end();
            }
            double dd = Chrono.stop(d0);
            System.out.println("meas     : " + dd + " ms total; " + (dd / (double) N * (double) 1000000.0) + " ns per loop");
        }
    }
    
    public void testSpeed2() throws Throwable {
        System.out.println("Speed test");
        String s = "abcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabc";
        s = "/" + s + s + s;
        System.out.println("String length: " + s.length());

        // No timing
        {
            double d0 = Chrono.start();
            int N = 1000000;
            for (int i = 0; i < N; i++) {
                int k = s.lastIndexOf('/');
                if (k < 0) {
                    throw new Throwable();
                }
            }
            double dd = Chrono.stop(d0);
            System.out.println("no timing: " + dd + " ms total; " + (dd / (double) N * (double) 1000000.0) + " ns per loop");
        }

        // Measurement time
        {
            System.setProperty("net.sf.hulp.profiler", "0");
            Measurement m0 = Measurement.begin("void");
            m0.end();
            
            double d0 = Chrono.start();
            int N = 1000000;
            for (int i = 0; i < N; i++) {
                Measurement m1 = Measurement.begin("m1");
                int k = s.lastIndexOf('/');
                if (k < 0) {
                    throw new Throwable();
                }
                m1.end();
            }
            double dd = Chrono.stop(d0);
            System.out.println("void meas: " + dd + " ms total; " + (dd / (double) N * (double) 1000000.0) + " ns per loop");
        }
    }
    
    public void testCompare() throws Throwable {
        new RealProfiler().testCompare();
    }
    
//    private static final long histoIndex1(double d) {
//        if (d > RealProfiler.HISTOGRAM[RealProfiler.HISTOGRAM.length - 2]) {
//            return RealProfiler.HISTOGRAM.length - 1;
//        }
//        
//        // Bin equation:
//        long i = (long) (100.0d * d);
//
//        // From Long.highestOneBit()
//        i |= (i >>  1);
//        i |= (i >>  2);
//        i |= (i >>  4);
//        i |= (i >>  8);
//        i |= (i >> 16);
//        i |= (i >> 32);
//        i = i - (i >>> 1);
//        return i;
//    }

    private static final int histoIndex2(double d) {
        if (Double.isNaN(d)) {
            return 0;
        }
        int i = Arrays.binarySearch(RealProfiler.HISTOGRAM_BINS, d);
        if (i < 0) {
            i = (-(i + 1));
        }
        if (i >= RealProfiler.HISTOGRAM_BINS.length) {
            i = RealProfiler.HISTOGRAM_BINS.length - 1;
        }
        return i;
    }
    
    public void checkHist(double d) {
        int idx2 = histoIndex2(d);
        int idx1 = RealProfiler.histoIndex(d);
        System.out.println("d=" + d + "; 100*d=" + (long) (100.0d * d) + " search: " + Arrays.binarySearch(RealProfiler.HISTOGRAM_BINS, d)
            + "; idx2=" + idx2 + " (" + RealProfiler.HISTOGRAM_BINS[idx2] + "); idx1=" + idx1 + " (" + RealProfiler.HISTOGRAM_BINS[idx1] + ");");

        assertTrue("" + idx1 + "<>" + idx2, idx1 == idx2);
    }
    
    public void testHist() throws Throwable {
        double[] h = RealProfiler.HISTOGRAM_BINS;
        for (int i = 0; i < h.length; i++) {
            System.out.println(i + ": " + h[i]);
        }
        
        checkHist(0.0d);
        checkHist(0.0001d);
        checkHist(0.01001d);
        checkHist(0.02001d);
        checkHist(0.14d);
        checkHist(0.1600001d);
        checkHist(0.17d);
        checkHist(1.0E15d);
        checkHist(1.0E308d);
        checkHist(Double.MAX_VALUE);
        checkHist(Double.MIN_VALUE);
        checkHist(-0.000001d);
        checkHist(-1d);
        checkHist(-1.0E308d);
        checkHist(Double.NEGATIVE_INFINITY);
        checkHist(Double.POSITIVE_INFINITY);
        checkHist(Double.NaN);
    }

}

