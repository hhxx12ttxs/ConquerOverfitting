// Copyright (c) 2007 Sun Microsystems
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

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A timing facility that provides access to a microsecond timing facility on Win32;
 * this relies on the JChrono.dll to be in the path, otherwise the normal
 * System.currentMillis() will be used.<br><br>
 *
 * The typical use is as follows:<br><code>
 * double t0 = Chrono.start();
 * ... do the stuff to measure
 * double dt = Chrono.stop(t0);
 * </code>
 *
 * @author JavaPro (Aug 2001)
 */
public class Chrono {
    /** Formatter to display the doubles in reasonable precision */
    private static NumberFormat sFormatter = null;
    
    private static double sSpeed = -1.0d;

    static {
        sFormatter = DecimalFormat.getInstance();
        sFormatter.setMaximumFractionDigits(3);
    }

    /**
     * Starts the timer
     *
     * @return value in arbitrary units
     */
    public static double start() {
        return System.nanoTime();
    }

    /**
     * Returns the time difference in milli seconds between the specified time obtained
     * using start() and the current time.
     *
     * @param t time obtained through start()
     * @return
     */
    public static double stop(double dStart) {
        long now = System.nanoTime();
        if (now < dStart) {
            // On some virtualization platforms, the clock is messed up
            return 0d;
        }
        return (now - dStart) / (double) 1000000.0;
    }

    /**
     * Checks if a high resolution timer is available
     */
    public static boolean isHighResTimerAvailable() {
        return true;
    }

    /**
     * Formats an elapsed time with a reasonable number of fractional digits.
     */
    public static String format(double t) {
        return sFormatter.format(t);
    }
    
    /**
     * @return overhead in ms for each start/stop
     */
    public static double getOverheadPerMeasurement() {
        double speed = 0.0d;
        synchronized (Chrono.class) {
            speed = sSpeed;
        }

        if (speed < 0.0d) {
            // Warmup
            for (int k = 0; k < 1000; k++) {
                double ts = start();
                stop(ts);
            }
            
            // Measure for 500 ms
            double ts = start();
            double dt = stop(ts);
            int n = 0;
            for (;;) {
                start();
                dt = stop(ts);
                n++;
                if (dt > 500.0d) {
                    break;
                }
            }
            speed = (double) n / dt;

            synchronized (Chrono.class) {
                sSpeed = speed;
            }
        }
        
        return speed > 0.0d ? (double) 1.0d / speed : 0.0d;
    }
}
