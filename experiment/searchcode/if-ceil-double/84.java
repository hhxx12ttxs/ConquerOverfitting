/*
 * HumanTime.java Created on 06.10.2008 Copyright (c) 2008 Johann Burkard
 * (<mailto:jb@eaio.com>) <http://eaio.com> Permission is hereby granted, free
 * of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to
 * the following conditions: The above copyright notice and this permission
 * notice shall be included in all copies or substantial portions of the
 * Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.imdeity.deityapi.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Mainly HumanTime however there are some other functions
 * 
 * @see <a
 *      href="http://johannburkard.de/blog/programming/java/date-formatting-parsing-humans-humantime.html">Date
 *      Formatting and Parsing for Humans in Java with HumanTime</a>
 */
public class HumanTime implements Externalizable, Comparable<HumanTime>, Cloneable {
    
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 5179328390732826722L;
    
    /**
     * One second.
     */
    private static final long SECOND = 1000;
    
    /**
     * One minute.
     */
    private static final long MINUTE = SECOND * 60;
    
    /**
     * One hour.
     */
    private static final long HOUR = MINUTE * 60;
    
    /**
     * One day.
     */
    private static final long DAY = HOUR * 24;
    
    /**
     * One year.
     */
    private static final long YEAR = DAY * 365;
    
    /**
     * Percentage of what is round up or down.
     */
    private static final int CEILING_PERCENTAGE = 15;
    
    /**
     * Parsing state.
     */
    static enum State {
        
        NUMBER, IGNORED, UNIT
        
    }
    
    static State getState(char c) {
        State out;
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                out = State.NUMBER;
                break;
            case 's':
            case 'm':
            case 'h':
            case 'd':
            case 'y':
            case 'S':
            case 'M':
            case 'H':
            case 'D':
            case 'Y':
                out = State.UNIT;
                break;
            default:
                out = State.IGNORED;
        }
        return out;
    }
    
    /**
     * Parses a {@link CharSequence} argument and returns a {@link HumanTime}
     * instance.
     * 
     * @param s
     *            the char sequence, may not be <code>null</code>
     * @return an instance, never <code>null</code>
     */
    public HumanTime eval(final CharSequence s) {
        HumanTime out = new HumanTime(0L);
        
        int num = 0;
        
        int start = 0;
        int end = 0;
        
        State oldState = State.IGNORED;
        
        for (char c : new Iterable<Character>() {
            
            /**
             * @see java.lang.Iterable#iterator()
             */
            public Iterator<Character> iterator() {
                return new Iterator<Character>() {
                    
                    private int p = 0;
                    
                    /**
                     * @see java.util.Iterator#hasNext()
                     */
                    public boolean hasNext() {
                        return p < s.length();
                    }
                    
                    /**
                     * @see java.util.Iterator#next()
                     */
                    public Character next() {
                        return s.charAt(p++);
                    }
                    
                    /**
                     * @see java.util.Iterator#remove()
                     */
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                };
            }
            
        }) {
            State newState = getState(c);
            if (oldState != newState) {
                if (oldState == State.NUMBER && (newState == State.IGNORED || newState == State.UNIT)) {
                    num = Integer.parseInt(s.subSequence(start, end).toString());
                } else if (oldState == State.UNIT && (newState == State.IGNORED || newState == State.NUMBER)) {
                    out.nTimes(s.subSequence(start, end).toString(), num);
                    num = 0;
                }
                start = end;
            }
            ++end;
            oldState = newState;
        }
        if (oldState == State.UNIT) {
            out.nTimes(s.subSequence(start, end).toString(), num);
        }
        
        return out;
    }
    
    /**
     * Parses and formats the given char sequence, preserving all data.
     * <p>
     * Equivalent to <code>eval(in).getExactly()</code>
     * 
     * @param in
     *            the char sequence, may not be <code>null</code>
     * @return a formatted String, never <code>null</code>
     */
    public String exactly(CharSequence in) {
        return eval(in).getExactly();
    }
    
    /**
     * Formats the given time delta, preserving all data.
     * <p>
     * Equivalent to <code>new HumanTime(in).getExactly()</code>
     * 
     * @param l
     *            the time delta
     * @return a formatted String, never <code>null</code>
     */
    public String exactly(long l) {
        return new HumanTime(l).getExactly();
    }
    
    /**
     * Parses and formats the given char sequence, potentially removing some
     * data to make the output easier to understand.
     * <p>
     * Equivalent to <code>eval(in).getApproximately()</code>
     * 
     * @param in
     *            the char sequence, may not be <code>null</code>
     * @return a formatted String, never <code>null</code>
     */
    public String approximately(CharSequence in) {
        return eval(in).getApproximately();
    }
    
    /**
     * Formats the given time delta, preserving all data.
     * <p>
     * Equivalent to <code>new HumanTime(l).getApproximately()</code>
     * 
     * @param l
     *            the time delta
     * @return a formatted String, never <code>null</code>
     */
    public String approximately(long l) {
        return new HumanTime(l).getApproximately();
    }
    
    /**
     * The time delta.
     */
    private long delta;
    
    /**
     * No-argument Constructor for HumanTime.
     * <p>
     * Equivalent to calling <code>new HumanTime(0L)</code>.
     */
    public HumanTime() {
        this(0L);
    }
    
    /**
     * Constructor for HumanTime.
     * 
     * @param delta
     *            the initial time delta, interpreted as a positive number
     */
    private HumanTime(long delta) {
        super();
        this.delta = Math.abs(delta);
    }
    
    private void nTimes(String unit, int n) {
        if ("ms".equalsIgnoreCase(unit)) {
            ms(n);
        } else if ("s".equalsIgnoreCase(unit)) {
            s(n);
        } else if ("m".equalsIgnoreCase(unit)) {
            m(n);
        } else if ("h".equalsIgnoreCase(unit)) {
            h(n);
        } else if ("d".equalsIgnoreCase(unit)) {
            d(n);
        } else if ("y".equalsIgnoreCase(unit)) {
            y(n);
        }
    }
    
    private long upperCeiling(long x) {
        return (x / 100) * (100 - CEILING_PERCENTAGE);
    }
    
    private long lowerCeiling(long x) {
        return (x / 100) * CEILING_PERCENTAGE;
    }
    
    private String ceil(long d, long n) {
        return Integer.toString((int) Math.ceil((double) d / n));
    }
    
    private String floor(long d, long n) {
        return Integer.toString((int) Math.floor((double) d / n));
    }
    
    /**
     * Adds one year to the time delta.
     * 
     * @return this HumanTime object
     */
    public HumanTime y() {
        return y(1);
    }
    
    /**
     * Adds n years to the time delta.
     * 
     * @param n
     *            n
     * @return this HumanTime object
     */
    private HumanTime y(int n) {
        delta += YEAR * Math.abs(n);
        return this;
    }
    
    /**
     * Adds n days to the time delta.
     * 
     * @param n
     *            n
     * @return this HumanTime object
     */
    private HumanTime d(int n) {
        delta += DAY * Math.abs(n);
        return this;
    }
    
    /**
     * Adds n hours to the time delta.
     * 
     * @param n
     *            n
     * @return this HumanTime object
     */
    private HumanTime h(int n) {
        delta += HOUR * Math.abs(n);
        return this;
    }
    
    /**
     * Adds n months to the time delta.
     * 
     * @param n
     *            n
     * @return this HumanTime object
     */
    private HumanTime m(int n) {
        delta += MINUTE * Math.abs(n);
        return this;
    }
    
    /**
     * Adds n seconds to the time delta.
     * 
     * @param n
     *            seconds
     * @return this HumanTime object
     */
    private HumanTime s(int n) {
        delta += SECOND * Math.abs(n);
        return this;
    }
    
    /**
     * Adds n milliseconds to the time delta.
     * 
     * @param n
     *            n
     * @return this HumanTime object
     */
    private HumanTime ms(int n) {
        delta += Math.abs(n);
        return this;
    }
    
    /**
     * Returns a human-formatted representation of the time delta.
     * 
     * @return a formatted representation of the time delta, never
     *         <code>null</code>
     */
    private String getExactly() {
        return getExactly(new StringBuilder()).toString();
    }
    
    /**
     * Appends a human-formatted representation of the time delta to the given
     * {@link Appendable} object.
     * 
     * @param <T>
     *            the return type
     * @param a
     *            the Appendable object, may not be <code>null</code>
     * @return the given Appendable object, never <code>null</code>
     */
    private <T extends Appendable> T getExactly(T a) {
        try {
            boolean prependBlank = false;
            long d = delta;
            if (d >= YEAR) {
                a.append(floor(d, YEAR));
                a.append(' ');
                a.append('y');
                prependBlank = true;
            }
            d %= YEAR;
            if (d >= DAY) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, DAY));
                a.append(' ');
                a.append('d');
                prependBlank = true;
            }
            d %= DAY;
            if (d >= HOUR) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, HOUR));
                a.append(' ');
                a.append('h');
                prependBlank = true;
            }
            d %= HOUR;
            if (d >= MINUTE) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, MINUTE));
                a.append(' ');
                a.append('m');
                prependBlank = true;
            }
            d %= MINUTE;
            if (d >= SECOND) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, SECOND));
                a.append(' ');
                a.append('s');
                prependBlank = true;
            }
            d %= SECOND;
            if (d > 0) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(Integer.toString((int) d));
                a.append(' ');
                a.append('m');
                a.append('s');
            }
        } catch (IOException ex) {
            // What were they thinking...
        }
        return a;
    }
    
    /**
     * Returns an approximate, human-formatted representation of the time delta.
     * 
     * @return a formatted representation of the time delta, never
     *         <code>null</code>
     */
    private String getApproximately() {
        return getApproximately(new StringBuilder()).toString();
    }
    
    /**
     * Appends an approximate, human-formatted representation of the time delta
     * to the given {@link Appendable} object.
     * 
     * @param <T>
     *            the return type
     * @param a
     *            the Appendable object, may not be <code>null</code>
     * @return the given Appendable object, never <code>null</code>
     */
    private <T extends Appendable> T getApproximately(T a) {
        
        try {
            int parts = 0;
            boolean rounded = false;
            boolean prependBlank = false;
            long d = delta;
            long mod = d % YEAR;
            
            if (mod >= upperCeiling(YEAR)) {
                a.append(ceil(d, YEAR));
                a.append(' ');
                a.append('y');
                ++parts;
                rounded = true;
                prependBlank = true;
            } else if (d >= YEAR) {
                a.append(floor(d, YEAR));
                a.append(' ');
                a.append('y');
                ++parts;
                rounded = mod <= lowerCeiling(YEAR);
                prependBlank = true;
            }
            
            if (!rounded) {
                d %= YEAR;
                mod = d % DAY;
                
                if (mod >= upperCeiling(DAY)) {
                    if (prependBlank) {
                        a.append(' ');
                    }
                    a.append(ceil(d, DAY));
                    a.append(' ');
                    a.append('d');
                    ++parts;
                    rounded = true;
                    prependBlank = true;
                } else if (d >= DAY) {
                    if (prependBlank) {
                        a.append(' ');
                    }
                    a.append(floor(d, DAY));
                    a.append(' ');
                    a.append('d');
                    ++parts;
                    rounded = mod <= lowerCeiling(DAY);
                    prependBlank = true;
                }
                
                if (parts < 2) {
                    d %= DAY;
                    mod = d % HOUR;
                    
                    if (mod >= upperCeiling(HOUR)) {
                        if (prependBlank) {
                            a.append(' ');
                        }
                        a.append(ceil(d, HOUR));
                        a.append(' ');
                        a.append('h');
                        ++parts;
                        rounded = true;
                        prependBlank = true;
                    } else if (d >= HOUR && !rounded) {
                        if (prependBlank) {
                            a.append(' ');
                        }
                        a.append(floor(d, HOUR));
                        a.append(' ');
                        a.append('h');
                        ++parts;
                        rounded = mod <= lowerCeiling(HOUR);
                        prependBlank = true;
                    }
                    
                    if (parts < 2) {
                        d %= HOUR;
                        mod = d % MINUTE;
                        
                        if (mod >= upperCeiling(MINUTE)) {
                            if (prependBlank) {
                                a.append(' ');
                            }
                            a.append(ceil(d, MINUTE));
                            a.append(' ');
                            a.append('m');
                            ++parts;
                            rounded = true;
                            prependBlank = true;
                        } else if (d >= MINUTE && !rounded) {
                            if (prependBlank) {
                                a.append(' ');
                            }
                            a.append(floor(d, MINUTE));
                            a.append(' ');
                            a.append('m');
                            ++parts;
                            rounded = mod <= lowerCeiling(MINUTE);
                            prependBlank = true;
                        }
                        
                        if (parts < 2) {
                            d %= MINUTE;
                            mod = d % SECOND;
                            
                            if (mod >= upperCeiling(SECOND)) {
                                if (prependBlank) {
                                    a.append(' ');
                                }
                                a.append(ceil(d, SECOND));
                                a.append(' ');
                                a.append('s');
                                ++parts;
                                rounded = true;
                                prependBlank = true;
                            } else if (d >= SECOND && !rounded) {
                                if (prependBlank) {
                                    a.append(' ');
                                }
                                a.append(floor(d, SECOND));
                                a.append(' ');
                                a.append('s');
                                ++parts;
                                rounded = mod <= lowerCeiling(SECOND);
                                prependBlank = true;
                            }
                            
                            if (parts < 2) {
                                d %= SECOND;
                                
                                if (d > 0 && !rounded) {
                                    if (prependBlank) {
                                        a.append(' ');
                                    }
                                    a.append(Integer.toString((int) d));
                                    a.append(' ');
                                    a.append('m');
                                    a.append('s');
                                }
                            }
                            
                        }
                        
                    }
                    
                }
            }
        } catch (IOException ex) {
            // What were they thinking...
        }
        
        return a;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (!(obj instanceof HumanTime)) { return false; }
        return delta == ((HumanTime) obj).delta;
    }
    
    /**
     * Returns a 32-bit representation of the time delta.
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) (delta ^ (delta >> 32));
    }
    
    /**
     * Returns a String representation of this.
     * <p>
     * The output is identical to {@link #getExactly()}.
     * 
     * @see java.lang.Object#toString()
     * @see #getExactly()
     * @return a String, never <code>null</code>
     */
    public String toString() {
        return getExactly();
    }
    
    /**
     * Compares this HumanTime to another HumanTime.
     * 
     * @param t
     *            the other instance, may not be <code>null</code>
     * @return which one is greater
     */
    public int compareTo(HumanTime t) {
        return delta == t.delta ? 0 : (delta < t.delta ? -1 : 1);
    }
    
    /**
     * Deep-clones this object.
     * 
     * @see java.lang.Object#clone()
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException {
        delta = in.readLong();
    }
    
    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(delta);
    }
    
    /**
     * Prints the time
     * 
     * @param theDate
     * @return
     */
    public String printTime(Date theDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(theDate);
        return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
    }
    
    /**
     * Converts a time to a string
     * 
     * @param date
     * @return
     */
    public String timeApproxToDate(Date date) {
        long relativeTime = date.getTime() - Calendar.getInstance().getTimeInMillis();
        
        return approximately(relativeTime) + " ago [" + this.printTime(date) + "]";
    }
    
    /**
     * Converts a time to a friendly string
     * 
     * @param time
     * @return
     */
    public String timeConvert(long time) {
        String retTime = approximately(time * (long) 60000);
        if (retTime.isEmpty()) {
            retTime = "None";
        }
        return retTime;
    }
    
    /**
     * Returns a user friendly string from a date
     * 
     * @param theDate
     * @param verbose
     * @return
     */
    public String getFriendlyDate(Calendar theDate, boolean verbose) {
        int year = theDate.get(Calendar.YEAR);
        int month = theDate.get(Calendar.MONTH);
        int dayOfMonth = theDate.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = theDate.get(Calendar.DAY_OF_WEEK);
        
        // Get the day of the week as a String.
        // Note: The Calendar DAY_OF_WEEK property is NOT zero-based, and Sunday
        // is the first day of week.
        String friendly = "";
        switch (dayOfWeek) {
            case 1:
                friendly = "Sunday";
                break;
            case 2:
                friendly = "Monday";
                break;
            case 3:
                friendly = "Tuesday";
                break;
            case 4:
                friendly = "Wednesday";
                break;
            case 5:
                friendly = "Thursday";
                break;
            case 6:
                friendly = "Friday";
                break;
            case 7:
                friendly = "Saturday";
                break;
            default:
                friendly = "BadDayValue";
                break;
        }
        
        // Add padding and the prefix to the day of month
        if (verbose == true) {
            friendly += " the " + dayOfMonth;
        } else {
            friendly += ", " + dayOfMonth;
        }
        
        String dayString = String.valueOf(dayOfMonth); // Convert dayOfMonth to
                                                       // String using valueOf
        
        // Suffix is "th" for day of day of month values ending in 0, 4, 5, 6,
        // 7, 8, and 9
        if (dayString.endsWith("0") || dayString.endsWith("4") || dayString.endsWith("5") || dayString.endsWith("6") || dayString.endsWith("7") || dayString.endsWith("8") || dayString.endsWith("9")) {
            friendly += "th ";
        }
        
        // Suffix is "st" for day of month values ending in 1
        if (dayString.endsWith("1")) {
            friendly += "st ";
        }
        
        // Suffix is "nd" for day of month values ending in 2
        if (dayString.endsWith("2")) {
            friendly += "nd ";
        }
        
        // Suffix is "rd" for day of month values ending in 3
        if (dayString.endsWith("3")) {
            friendly += "rd ";
        }
        
        // Add more padding if we've been asked to be verbose
        if (verbose == true) {
            friendly += "of ";
        }
        
        // Get a friendly version of the month.
        // Note: The Calendar MONTH property is zero-based to increase the
        // chance of developers making mistakes.
        switch (month) {
            case 0:
                friendly += "January";
                break;
            case 1:
                friendly += "February";
                break;
            case 2:
                friendly += "March";
                break;
            case 3:
                friendly += "April";
                break;
            case 4:
                friendly += "May";
                break;
            case 5:
                friendly += "June";
                break;
            case 6:
                friendly += "July";
                break;
            case 7:
                friendly += "August";
                break;
            case 8:
                friendly += "September";
                break;
            case 9:
                friendly += "October";
                break;
            case 10:
                friendly += "November";
                break;
            case 11:
                friendly += "December";
                break;
            default:
                friendly += "BadMonthValue";
                break;
        }
        
        // Tack on the year and we're done. Phew!
        friendly += " " + year;
        
        return friendly;
        
    }
    
    /**
     * Gets a user friendly date
     * 
     * @param theDate
     * @param verbose
     * @return
     */
    public String getFriendlyDate(Date theDate, boolean verbose) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(theDate);
        return this.getFriendlyDate(cal, verbose);
    }
    
}

