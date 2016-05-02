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
 * @(#)NameMatcherTester.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.batchext;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

import com.sun.jbi.internationalization.Messages;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *
 * 
 * 
 * 
 * @author Harry Liu (harry.liu@sun.com)
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved.
 */
public class NameMatcherTester {
    /** Creates a new instance of NameMatcherTester */
    public NameMatcherTester() {
    }

    public static void main(String[] args) throws Exception {
        String pattern;
        String formattedString;
        NameMatcher nm;
        Date currentDateTime = new Date();
        SimpleDateFormat simpleFormatter;

        // test "this" class
        nm = new NameMatcher("file%d%d%d%d");
        String input = "file0031";
        System.out.println("Pattern: " + nm.getRawPattern() + ", input: " + input + "   matches: " + nm.matches(input));
        System.out.println("Pattern: " + nm.getRawPattern() + ", input: " + input + "   find: " + nm.find(input));
        System.out.println("Pattern: " + nm.getRawPattern() + ", input: " + input + "   lookingAt: " + nm.lookingAt(input));

        // for escape special chars
        pattern = "*[]()|+{}:.^$?\\";
        nm = new NameMatcher(pattern);
        String escapedString = nm.getRegexPattern().pattern();
        System.out.println("escape: " + pattern + " ==> " + escapedString);
        pattern = escapedString;
        escapedString = nm.getRegexPattern().pattern();
        System.out.println("escape again: " + pattern + " ==> " + escapedString);

        if (1>0) return;
        
        // for Locale
        System.out.println("Locale.getDefault is: " + Locale.getDefault()); // en_US or Locale.US
        Locale[] locale = Locale.getAvailableLocales();
        for (int i = 0; i < locale.length; i++) {
            System.out.println("Locale.getAvailableLocales (" + i + ") is " + locale[i]);
        }
        
        // for TimeZone
        System.out.println("TimeZone.getDefault is: " + TimeZone.getDefault()); // en_US or Locale.US
        System.out.println("TimeZone.getDisplayName is: " + TimeZone.getDefault().getDisplayName());
        String[] zone = TimeZone.getAvailableIDs();
        for (int i = 0; i < zone.length; i++) {
            System.out.println("TimeZone.getAvailableIDs (" + i + ") is " + zone[i]);
        }
        
        // for Symbols
        DateFormatSymbols symbols = new DateFormatSymbols(DateFormatLocale.LOCALE_IN_USE);
        String localPatternChars = symbols.getLocalPatternChars();
        String[] amPmStrings = symbols.getAmPmStrings();
        String[] eras = symbols.getEras();
        String[] months = symbols.getMonths();
        String[] shortMonths = symbols.getShortMonths();
        String[] shortWeekdays = symbols.getShortWeekdays();
        String[] weekday = symbols.getWeekdays();
        String[][] zoneStrings = symbols.getZoneStrings();
        
        System.out.println("DateFormatSymbols->localPatternChars [" + localPatternChars + "]");
        for (int i = 0; i < amPmStrings.length; i++) {
            System.out.println("DateFormatSymbols->amPmStrings (" + i + ") is [" + amPmStrings[i] + "]");
        }
        
        for (int i = 0; i < eras.length; i++) {
            System.out.println("DateFormatSymbols->eras (" + i + ") is [" + eras[i] + "]");
        }
        
        for (int i = 0; i < months.length; i++) {
            System.out.println("DateFormatSymbols->months (" + i + ") is [" + months[i] + "]");
        }
        
        for (int i = 0; i < shortMonths.length; i++) {
            System.out.println("DateFormatSymbols->shortMonths (" + i + ") is [" + shortMonths[i] + "]");
        }
        
        for (int i = 0; i < shortWeekdays.length; i++) {
            System.out.println("DateFormatSymbols->shortWeekdays (" + i + ") is [" + shortWeekdays[i] + "]");
        }
        
        for (int i = 0; i < weekday.length; i++) {
            System.out.println("DateFormatSymbols->weekday (" + i + ") is [" + weekday[i] + "]");
        }
        
        for (int i = 0; i < zoneStrings.length; i++) {
            for (int j = 0; j < zoneStrings[i].length; j++) {
                System.out.println("DateFormatSymbols->zoneStrings (" + i + ", " + j + ") is [" + zoneStrings[i][j] + "]");
            }
        }
        
        
        /*
        // just for temp test
        String regexUnion = "(" + mapping.get("D") + "|" + mapping.get("DD") + "|" + mapping.get("DDD") + "|" + mapping.get("DDDDDD") + ")";
        Matcher m = Pattern.compile(regexUnion).matcher("123.456.789.0123.0345.0789.00123.00456.00789.000123.000456.000789.123.456.789");
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        m.find();
        System.out.println("end: " + m.end() + " start: " + m.start() + " group: " + m.group() + " groupCount: " + m.groupCount());
        */
        
        // G
        pattern = "%G"; // AD
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        /*
        expected = "AD" + " is expected Match for pattern %" + pattern;
        regexMatcher = regexPattern.matcher(expected);
        System.out.println(expected + ": " + regexMatcher.find());
        expected = "BC" + " is expected Match for pattern %" + pattern;
        regexMatcher = regexPattern.matcher(expected);
        System.out.println(expected + ": " + regexMatcher.find());
        expected = "AB" + " is expected NotMatch for pattern %" + pattern;
        regexMatcher = regexPattern.matcher(expected);
        System.out.println(expected + ": " + !regexMatcher.find());
        expected = "ad" + " is expected NotMatch for pattern %" + pattern;
        regexMatcher = regexPattern.matcher(expected);
        System.out.println(expected + ": " + !regexMatcher.find());
         **/

        pattern = "%G%G%G%G%G%G"; // AD
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%y"; // 06
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%y%y"; // 06
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%y%y%y"; // 06
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%y%y%y%y"; // 2006
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%y%y%y%y%y%y"; // 002006
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%M"; // 3
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%M%M"; // 03
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%M%M%M"; // Mar
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%M%M%M%M%M%M"; // March
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%d"; // 9
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%d%d"; // 09
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%d%d%d%d%d%d"; // 000009
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%w"; // 45
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%w%w"; // 45
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%w%w%w%w%w%w"; // 000045
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%W"; // 2
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%W%W%W%W%W%W"; // 000002
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%D"; // 315
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%D%D"; // 315
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%D%D%D"; // 315
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%D%D%D%D%D%D"; // 000315
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%F"; // 2
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%F%F%F%F%F%F"; // 000002
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%E"; // Sat
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%E%E"; // Sat
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%E%E%E"; // Sat
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%E%E%E%E%E%E"; // Saturday
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%a"; // PM
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%a%a%a%a%a%a"; // PM
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%H"; // 21
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%H%H"; // 21
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%H%H%H%H%H%H"; // 000021
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%k"; // 21
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%k%k"; // 21
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%k%k%k%k%k%k"; // 000021
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%K"; // 9
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%K%K"; // 09
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%K%K%K%K%K%K"; // 000009
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%h"; // 8
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%h%h"; // 08
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%h%h%h%h%h%h"; // 000008
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%m"; // 2
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%m%m"; // 02
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%m%m%m%m%m%m"; // 000002
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%s"; // 2
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%s%s"; // 02
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%s%s%s%s%s%s"; // 000002
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%S"; // 825
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%S%S"; // 825
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%S%S%S"; // 825
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%S%S%S%S%S%S"; // 000825
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%z"; // PST
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%z%z"; // PST
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%z%z%z"; // PST
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%z%z%z%z%z%z"; // Pacific Standard Time
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        pattern = "%Z"; // -0800
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        pattern = "%Z%Z%Z%Z%Z%Z"; // -0800
        simpleFormatter = new SimpleDateFormat(pattern.replaceAll("%", ""));
        formattedString = simpleFormatter.format(currentDateTime);
        System.out.println(pattern + " ==> " + formattedString);
        System.out.println(formattedString + " matches back with pattern " + pattern + ": " + new NameMatcher(pattern).matches(formattedString));
        
        
        return;
    }
}

