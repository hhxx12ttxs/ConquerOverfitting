/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)DurationImpl.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.engine.bpel.core.bpel.dt.impl;

import java.text.DecimalFormat;

import com.sun.jbi.engine.bpel.core.bpel.dt.Duration;
import com.sun.jbi.engine.bpel.core.bpel.util.I18n;


/**
 * DOCUMENT ME!
 *
 * @author Sun Microsystems
 * @version 
 */
public class DurationImpl implements Duration {

    /**
     * DOCUMENT ME!
     */
    static final DecimalFormat FMT = new DecimalFormat(".###");

    /**
     * DOCUMENT ME!
     */
    boolean negative;

    /**
     * DOCUMENT ME!
     */
    int years;

    /**
     * DOCUMENT ME!
     */
    int months;

    //int weeks;

    /**
     * DOCUMENT ME!
     */
    int days;

    /**
     * DOCUMENT ME!
     */
    int hours;

    /**
     * DOCUMENT ME!
     */
    int minutes;

    /**
     * DOCUMENT ME!
     */
    int seconds;

    /**
     * DOCUMENT ME!
     */
    int milliseconds;

    /**
     * default constructor
     */
    public DurationImpl() {
    }

    /**
     * Creates a new DurationImpl object.
     *
     * @param copy DOCUMENT ME!
     */
    public DurationImpl(DurationImpl copy) {
        negative = copy.negative;
        years = copy.years;
        months = copy.months;

        //    weeks = copy.weeks;
        days = copy.days;
        hours = copy.hours;
        minutes = copy.minutes;
        seconds = copy.seconds;
        milliseconds = copy.milliseconds;
    }

    private void check(int value) {
        if (value < 0) {
            throw new IllegalArgumentException(I18n.loc("BPCOR-6009: DurationImpl value cannot be negative"));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param years DOCUMENT ME!
     */
    public void setYears(int years) {
        check(years);
        this.years = years;
    }

    /**
     * DOCUMENT ME!
     *
     * @param months DOCUMENT ME!
     */
    public void setMonths(int months) {
        check(months);
        this.months = months;
    }

    //    public void setWeeks(int weeks) {
    //    check(weeks);
    //    this.weeks = weeks;
    //    }

    /**
     * set days
     * @param days number of days
     */
    public void setDays(int days) {
        check(days);
        this.days = days;
    }

    /**
     * DOCUMENT ME!
     *
     * @param hours DOCUMENT ME!
     */
    public void setHours(int hours) {
        check(hours);
        this.hours = hours;
    }

    /**
     * DOCUMENT ME!
     *
     * @param minutes DOCUMENT ME!
     */
    public void setMinutes(int minutes) {
        check(minutes);
        this.minutes = minutes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param seconds DOCUMENT ME!
     */
    public void setSeconds(int seconds) {
        check(seconds);
        this.seconds = seconds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param millis DOCUMENT ME!
     */
    public void setMilliseconds(int millis) {
        check(millis);
        this.milliseconds = millis;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getYears() {
        return years;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMonths() {
        return months;
    }

    //public int getWeeks() {
    //return weeks;
    //}

    /**
     * get days
     * @return number of days
     */
    public int getDays() {
        return days;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getHours() {
        return hours;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMilliseconds() {
        return milliseconds;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isNegative() {
        return negative;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     */
    public void setNegative(boolean value) {
        negative = value;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        String buffer = "";

        if (negative) {
            buffer += "-";
        }

        buffer += "P";

        if (years > 0) {
            buffer += years;
            buffer += "Y";
        }

        if (months > 0) {
            buffer += months;
            buffer += "M";

            //} else if(weeks > 0) {
            //buffer += weeks;
            //buffer += "W";
        }

        if (days > 0) {
            buffer += days;
            buffer += "D";
        }

        if ((hours > 0) || (minutes > 0) || (seconds > 0) ||
                (milliseconds > 0)) {
            buffer += "T";

            if (hours > 0) {
                buffer += hours;
                buffer += "H";
            }

            if (minutes > 0) {
                buffer += minutes;
                buffer += "M";
            }

            if ((seconds > 0) || (milliseconds > 0)) {
                buffer += seconds;

                if (milliseconds > 0) {
                    buffer += FMT.format((double) milliseconds / 1000);
                }

                buffer += "S";
            }
        }

        return buffer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Duration parse(String value) {
        DurationParser parser = new DurationParser(value);

        return parser.parseDuration();
    }

    //P(nY)?(nM)?(nD)?T(nH)?(nM)?(nS("."n)?)?
    static class DurationParser {
        /**
         * DOCUMENT ME!
         */
        Duration result;

        /**
         * DOCUMENT ME!
         */
        char[] buf;

        /**
         * DOCUMENT ME!
         */
        int pos;

        /**
         * Creates a new DurationParser object.
         *
         * @param value DOCUMENT ME!
         */
        DurationParser(String value) {
            buf = value.toCharArray();
            pos = buf.length - 1;
            result = new DurationImpl();
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        int peek() {
            if (pos < 0) {
                throw new RuntimeException(I18n.loc("BPCOR-6010: DurationImpl malformed duration"));
            }

            return buf[pos];
        }

        /**
         * DOCUMENT ME!
         */
        void lex() {
            pos--;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        int parseInt() {
            int mark = pos;

            while ((pos > 0) && Character.isDigit(buf[pos - 1])) {
                pos--;
            }

            int res = Integer.parseInt(new String(buf, pos, (mark + 1) - pos));
            pos--;

            return res;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        boolean parseSeconds() {
            if (peek() == 'S') {
                lex();

                int value = parseInt();

                if (peek() == '.') {
                    result.setMilliseconds((int) (Double.parseDouble("0." +
                            value) * 1000));
                    lex();

                    if ((pos >= 0) && Character.isDigit(buf[pos])) {
                        value = parseInt(); // handle 0.5 and alike
                    } else {
                        value = 0; //  handle .5 and alike
                    }
                }

                result.setSeconds(value);

                return true;
            }

            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        boolean parseMinutes() {
            if (peek() == 'M') {
                lex();
                result.setMinutes(parseInt());

                return true;
            }

            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        boolean parseHours() {
            if (peek() == 'H') {
                lex();
                result.setHours(parseInt());

                return true;
            }

            return false;
        }

        /**
         * DOCUMENT ME!
         */
        void parseT() {
            if (peek() == 'T') {
                lex();
            } else {
                throw new RuntimeException(I18n.loc("BPCOR-6011: DurationImpl Expected T instead of '{0}'", 
                		new Integer(peek())));
            }
        }

        /**
         * DOCUMENT ME!
         */
        void parseP() {
            if (peek() == 'P') {
                lex();
            } else {
                throw new RuntimeException(I18n.loc("BPCOR-6012: DurationImpl Expected P instead of '(0}'",
                		new Integer(peek())));
            }
        }

        /**
         * DOCUMENT ME!
         */
        void parseNegative() {
            if ((pos >= 0) && (peek() == '-')) {
                lex();
                result.setNegative(true);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        boolean parseDay() {
            if (peek() == 'D') {
                lex();
                result.setDays(parseInt());

                return true;
            }

            return false;
        }

        //boolean parseWeek() {
        //if(peek() == 'W') {
        //lex();
        //result.setWeeks(parseInt());
        //return true;
        //}
        //return false;
        //}
        boolean parseMonth() {
            if (peek() == 'M') {
                lex();
                result.setMonths(parseInt());

                return true;
            }

            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        boolean parseYear() {
            if (peek() == 'Y') {
                lex();
                result.setYears(parseInt());

                return true;
            }

            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        Duration parseDuration() {
            boolean some = false;

            if (parseSeconds()) {
                some = true;
            }

            if (parseMinutes()) {
                some = true;
            }

            if (parseHours()) {
                some = true;
            }

            if (some) {
                parseT();
            }

            if (parseDay()) {
                some = true;
            }

            //if(parseWeek()) {
            //some = true;
            //}
            if (parseMonth()) {
                some = true;
            }

            if (parseYear()) {
                some = true;
            }

            parseP();
            parseNegative();
            parseEnd(some);

            return result;
        }

        /**
         * DOCUMENT ME!
         *
         * @param some DOCUMENT ME!
         */
        void parseEnd(boolean some) {
            if (!some) {
                throw new RuntimeException(I18n.loc("BPCOR-6013: DurationImpl No fields specified"));
            }

            if (pos > 0) {
                throw new RuntimeException(I18n.loc("BPCOR-6014: DurationImpl unexpected text: {0}", 
                		new String(buf, 0, pos)));
            }
        }
    }
}

