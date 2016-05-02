/*
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.time.calendrical.CalendricalFormatter;
import javax.time.calendrical.CalendricalObject;
import javax.time.calendrical.DateAdjuster;
import javax.time.calendrical.DateAdjusters;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.LocalDateTimeField;
import javax.time.calendrical.LocalDateTimeUnit;
import javax.time.calendrical.MockFieldNoValue;
import javax.time.calendrical.MockZoneResolverReturnsNull;
import javax.time.calendrical.PeriodUnit;
import javax.time.calendrical.TimeAdjuster;
import javax.time.calendrical.ZoneResolver;
import javax.time.calendrical.ZoneResolvers;
import javax.time.extended.Year;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test LocalDateTime.
 */
@Test
public class TestLocalDateTime extends AbstractTest {

    private static final ZoneOffset OFFSET_PONE = ZoneOffset.ofHours(1);
    private static final ZoneOffset OFFSET_PTWO = ZoneOffset.ofHours(2);
    private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");
    private static final ZoneId ZONE_GAZA = ZoneId.of("Asia/Gaza");

    private LocalDateTime TEST_2007_07_15_12_30_40_987654321 = LocalDateTime.of(2007, 7, 15, 12, 30, 40, 987654321);
    private LocalDateTime MAX_DATE_TIME;
    private LocalDateTime MIN_DATE_TIME;
    private Instant MAX_INSTANT;
    private Instant MIN_INSTANT;

    @BeforeMethod(groups={"implementation","tck"})
    public void setUp() {
        MAX_DATE_TIME = LocalDateTime.MAX_DATE_TIME;
        MIN_DATE_TIME = LocalDateTime.MIN_DATE_TIME;
        MAX_INSTANT = MAX_DATE_TIME.atOffset(ZoneOffset.UTC).toInstant();
        MIN_INSTANT = MIN_DATE_TIME.atOffset(ZoneOffset.UTC).toInstant();
    }


    //-----------------------------------------------------------------------
    private void check(LocalDateTime dateTime, int y, int m, int d, int h, int mi, int s, int n) {
        assertEquals(dateTime.getYear(), y);
        assertEquals(dateTime.getMonthOfYear().getValue(), m);
        assertEquals(dateTime.getDayOfMonth(), d);
        assertEquals(dateTime.getHourOfDay(), h);
        assertEquals(dateTime.getMinuteOfHour(), mi);
        assertEquals(dateTime.getSecondOfMinute(), s);
        assertEquals(dateTime.getNanoOfSecond(), n);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_interfaces() {
        Object obj = TEST_2007_07_15_12_30_40_987654321;
        assertTrue(obj instanceof CalendricalObject);
        assertTrue(obj instanceof Serializable);
        assertTrue(obj instanceof Comparable<?>);
    }

    @Test(groups={"tck"})
    public void test_serialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(TEST_2007_07_15_12_30_40_987654321);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                baos.toByteArray()));
        assertEquals(ois.readObject(), TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(groups={"tck"})
    public void test_immutable() {
        Class<LocalDateTime> cls = LocalDateTime.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().contains("$") == false) {
                if (Modifier.isStatic(field.getModifiers())) {
                    assertTrue(Modifier.isFinal(field.getModifiers()), "Field:" + field.getName());
                } else {
                    assertTrue(Modifier.isPrivate(field.getModifiers()), "Field:" + field.getName());
                    assertTrue(Modifier.isFinal(field.getModifiers()), "Field:" + field.getName());
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void constant_MIN_DATE_TIME() {
        check(LocalDateTime.MIN_DATE_TIME, Year.MIN_YEAR, 1, 1, 0, 0, 0, 0);
    }

    @Test(groups={"implementation"})
    public void constant_MAX_DATE_TIME() {
        check(LocalDateTime.MAX_DATE_TIME, Year.MAX_YEAR, 12, 31,  23, 59, 59, 999999999);
    }

    //-----------------------------------------------------------------------
    // now()
    //-----------------------------------------------------------------------
    @Test(timeOut=30000, groups={"tck"})  // TODO: remove when time zone loading is faster
    public void now() {
        LocalDateTime expected = LocalDateTime.now(Clock.systemDefaultZone());
        LocalDateTime test = LocalDateTime.now();
        long diff = Math.abs(test.toLocalTime().toNanoOfDay() - expected.toLocalTime().toNanoOfDay());
        if (diff >= 100000000) {
            // may be date change
            expected = LocalDateTime.now(Clock.systemDefaultZone());
            test = LocalDateTime.now();
            diff = Math.abs(test.toLocalTime().toNanoOfDay() - expected.toLocalTime().toNanoOfDay());
        }
        assertTrue(diff < 100000000);  // less than 0.1 secs
    }

    //-----------------------------------------------------------------------
    // now(Clock)
    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void now_Clock_nullClock() {
        LocalDateTime.now(null);
    }

    @Test(groups={"tck"})
    public void now_Clock_allSecsInDay_utc() {
        for (int i = 0; i < (2 * 24 * 60 * 60); i++) {
            Instant instant = Instant.ofEpochSecond(i).plusNanos(123456789L);
            Clock clock = Clock.fixed(instant, ZoneId.UTC);
            LocalDateTime test = LocalDateTime.now(clock);
            assertEquals(test.getYear(), 1970);
            assertEquals(test.getMonthOfYear(), MonthOfYear.JANUARY);
            assertEquals(test.getDayOfMonth(), (i < 24 * 60 * 60 ? 1 : 2));
            assertEquals(test.getHourOfDay(), (i / (60 * 60)) % 24);
            assertEquals(test.getMinuteOfHour(), (i / 60) % 60);
            assertEquals(test.getSecondOfMinute(), i % 60);
            assertEquals(test.getNanoOfSecond(), 123456789);
        }
    }

    @Test(groups={"tck"})
    public void now_Clock_allSecsInDay_offset() {
        for (int i = 0; i < (2 * 24 * 60 * 60); i++) {
            Instant instant = Instant.ofEpochSecond(i).plusNanos(123456789L);
            Clock clock = Clock.fixed(instant.minusSeconds(OFFSET_PONE.getTotalSeconds()), ZoneId.of(OFFSET_PONE));
            LocalDateTime test = LocalDateTime.now(clock);
            assertEquals(test.getYear(), 1970);
            assertEquals(test.getMonthOfYear(), MonthOfYear.JANUARY);
            assertEquals(test.getDayOfMonth(), (i < 24 * 60 * 60) ? 1 : 2);
            assertEquals(test.getHourOfDay(), (i / (60 * 60)) % 24);
            assertEquals(test.getMinuteOfHour(), (i / 60) % 60);
            assertEquals(test.getSecondOfMinute(), i % 60);
            assertEquals(test.getNanoOfSecond(), 123456789);
        }
    }

    @Test(groups={"tck"})
    public void now_Clock_allSecsInDay_beforeEpoch() {
        LocalTime expected = LocalTime.MIDNIGHT.plusNanos(123456789L);
        for (int i =-1; i >= -(24 * 60 * 60); i--) {
            Instant instant = Instant.ofEpochSecond(i).plusNanos(123456789L);
            Clock clock = Clock.fixed(instant, ZoneId.UTC);
            LocalDateTime test = LocalDateTime.now(clock);
            assertEquals(test.getYear(), 1969);
            assertEquals(test.getMonthOfYear(), MonthOfYear.DECEMBER);
            assertEquals(test.getDayOfMonth(), 31);
            expected = expected.minusSeconds(1);
            assertEquals(test.toLocalTime(), expected);
        }
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void now_Clock_maxYear() {
        Clock clock = Clock.fixed(MAX_INSTANT, ZoneId.UTC);
        LocalDateTime test = LocalDateTime.now(clock);
        assertEquals(test, MAX_DATE_TIME);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void now_Clock_tooBig() {
        Clock clock = Clock.fixed(MAX_INSTANT.plusSeconds(24 * 60 * 60), ZoneId.UTC);
        LocalDateTime.now(clock);
    }

    @Test(groups={"tck"})
    public void now_Clock_minYear() {
        Clock clock = Clock.fixed(MIN_INSTANT, ZoneId.UTC);
        LocalDateTime test = LocalDateTime.now(clock);
        assertEquals(test, MIN_DATE_TIME);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void now_Clock_tooLow() {
        Clock clock = Clock.fixed(MIN_INSTANT.minusNanos(1), ZoneId.UTC);
        LocalDateTime.now(clock);
    }

    //-----------------------------------------------------------------------
    // of() factories
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_ofMidnight_intsMonth() {
        LocalDateTime dateTime = LocalDateTime.ofMidnight(2008, MonthOfYear.FEBRUARY, 29);
        check(dateTime, 2008, 2, 29, 0, 0, 0, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_intsMonth_yearTooLow() {
        LocalDateTime.ofMidnight(Integer.MIN_VALUE, MonthOfYear.FEBRUARY, 29);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_ofMidnight_intsMonth_nullMonth() {
        LocalDateTime.ofMidnight(2008, null, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_intsMonth_dayTooLow() {
        LocalDateTime.ofMidnight(2008, MonthOfYear.FEBRUARY, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_intsMonth_dayTooHigh() {
        LocalDateTime.ofMidnight(2008, MonthOfYear.MARCH, 32);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_ofMidnight_ints() {
        LocalDateTime dateTime = LocalDateTime.ofMidnight(2008, 2, 29);
        check(dateTime, 2008, 2, 29, 0, 0, 0, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_ints_yearTooLow() {
        LocalDateTime.ofMidnight(Integer.MIN_VALUE, 2, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_ints_monthTooLow() {
        LocalDateTime.ofMidnight(2008, 0, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_ints_monthTooHigh() {
        LocalDateTime.ofMidnight(2008, 13, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_ints_dayTooLow() {
        LocalDateTime.ofMidnight(2008, 2, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofMidnight_ints_dayTooHigh() {
        LocalDateTime.ofMidnight(2008, 3, 32);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_ofMidnight_LocalDate() {
        LocalDateTime dateTime = LocalDateTime.ofMidnight(LocalDate.of(2008, 2, 29));
        check(dateTime, 2008, 2, 29, 0, 0, 0, 0);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_ofMidnight_LocalDate_null() {
        LocalDateTime.ofMidnight((LocalDate) null);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_4intsMonth() {
        LocalDateTime dateTime = LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30);
        check(dateTime, 2007, 7, 15, 12, 30, 0, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_4intsMonth_yearTooLow() {
        LocalDateTime.of(Integer.MIN_VALUE, MonthOfYear.JULY, 15, 12, 30);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_4intsMonth_nullMonth() {
        LocalDateTime.of(2007, null, 15, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_4intsMonth_dayTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, -1, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_4intsMonth_dayTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 32, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_4intsMonth_hourTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, -1, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_4intsMonth_hourTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 24, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_4intsMonth_minuteTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_4intsMonth_minuteTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 60);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_5intsMonth() {
        LocalDateTime dateTime = LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, 40);
        check(dateTime, 2007, 7, 15, 12, 30, 40, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_yearTooLow() {
        LocalDateTime.of(Integer.MIN_VALUE, MonthOfYear.JULY, 15, 12, 30, 40);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_5intsMonth_nullMonth() {
        LocalDateTime.of(2007, null, 15, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_dayTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, -1, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_dayTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 32, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_hourTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, -1, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_hourTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 24, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_minuteTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, -1, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_minuteTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 60, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_secondTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5intsMonth_secondTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, 60);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_6intsMonth() {
        LocalDateTime dateTime = LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, 40, 987654321);
        check(dateTime, 2007, 7, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_yearTooLow() {
        LocalDateTime.of(Integer.MIN_VALUE, MonthOfYear.JULY, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_6intsMonth_nullMonth() {
        LocalDateTime.of(2007, null, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_dayTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, -1, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_dayTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 32, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_hourTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, -1, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_hourTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 24, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_minuteTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, -1, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_minuteTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 60, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_secondTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, -1, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_secondTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, 60, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_nanoTooLow() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, 40, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6intsMonth_nanoTooHigh() {
        LocalDateTime.of(2007, MonthOfYear.JULY, 15, 12, 30, 40, 1000000000);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_5ints() {
        LocalDateTime dateTime = LocalDateTime.of(2007, 7, 15, 12, 30);
        check(dateTime, 2007, 7, 15, 12, 30, 0, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_yearTooLow() {
        LocalDateTime.of(Integer.MIN_VALUE, 7, 15, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_monthTooLow() {
        LocalDateTime.of(2007, 0, 15, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_monthTooHigh() {
        LocalDateTime.of(2007, 13, 15, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_dayTooLow() {
        LocalDateTime.of(2007, 7, -1, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_dayTooHigh() {
        LocalDateTime.of(2007, 7, 32, 12, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_hourTooLow() {
        LocalDateTime.of(2007, 7, 15, -1, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_hourTooHigh() {
        LocalDateTime.of(2007, 7, 15, 24, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_minuteTooLow() {
        LocalDateTime.of(2007, 7, 15, 12, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_5ints_minuteTooHigh() {
        LocalDateTime.of(2007, 7, 15, 12, 60);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_6ints() {
        LocalDateTime dateTime = LocalDateTime.of(2007, 7, 15, 12, 30, 40);
        check(dateTime, 2007, 7, 15, 12, 30, 40, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_yearTooLow() {
        LocalDateTime.of(Integer.MIN_VALUE, 7, 15, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_monthTooLow() {
        LocalDateTime.of(2007, 0, 15, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_monthTooHigh() {
        LocalDateTime.of(2007, 13, 15, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_dayTooLow() {
        LocalDateTime.of(2007, 7, -1, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_dayTooHigh() {
        LocalDateTime.of(2007, 7, 32, 12, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_hourTooLow() {
        LocalDateTime.of(2007, 7, 15, -1, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_hourTooHigh() {
        LocalDateTime.of(2007, 7, 15, 24, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_minuteTooLow() {
        LocalDateTime.of(2007, 7, 15, 12, -1, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_minuteTooHigh() {
        LocalDateTime.of(2007, 7, 15, 12, 60, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_secondTooLow() {
        LocalDateTime.of(2007, 7, 15, 12, 30, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_6ints_secondTooHigh() {
        LocalDateTime.of(2007, 7, 15, 12, 30, 60);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_7ints() {
        LocalDateTime dateTime = LocalDateTime.of(2007, 7, 15, 12, 30, 40, 987654321);
        check(dateTime, 2007, 7, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_yearTooLow() {
        LocalDateTime.of(Integer.MIN_VALUE, 7, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_monthTooLow() {
        LocalDateTime.of(2007, 0, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_monthTooHigh() {
        LocalDateTime.of(2007, 13, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_dayTooLow() {
        LocalDateTime.of(2007, 7, -1, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_dayTooHigh() {
        LocalDateTime.of(2007, 7, 32, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_hourTooLow() {
        LocalDateTime.of(2007, 7, 15, -1, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_hourTooHigh() {
        LocalDateTime.of(2007, 7, 15, 24, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_minuteTooLow() {
        LocalDateTime.of(2007, 7, 15, 12, -1, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_minuteTooHigh() {
        LocalDateTime.of(2007, 7, 15, 12, 60, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_secondTooLow() {
        LocalDateTime.of(2007, 7, 15, 12, 30, -1, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_secondTooHigh() {
        LocalDateTime.of(2007, 7, 15, 12, 30, 60, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_nanoTooLow() {
        LocalDateTime.of(2007, 7, 15, 12, 30, 40, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_7ints_nanoTooHigh() {
        LocalDateTime.of(2007, 7, 15, 12, 30, 40, 1000000000);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_LocalDate_LocalTime() {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2007, 7, 15), LocalTime.of(12, 30, 40, 987654321));
        check(dateTime, 2007, 7, 15, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_LocalDate_LocalTime_nullLocalDate() {
        LocalDateTime.of(null, LocalTime.of(12, 30, 40, 987654321));
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_LocalDate_LocalTime_nullLocalTime() {
        LocalDateTime.of(LocalDate.of(2007, 7, 15), null);
    }

    //-----------------------------------------------------------------------
    // from()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_factory_CalendricalObject() {
        assertEquals(LocalDateTime.from(LocalDateTime.of(2007, 7, 15, 17, 30)), LocalDateTime.of(2007, 7, 15, 17, 30));
        assertEquals(LocalDateTime.from(OffsetDateTime.of(2007, 7, 15, 17, 30, ZoneOffset.ofHours(2))), LocalDateTime.of(2007, 7, 15, 17, 30));
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_factory_CalendricalObject_invalid_noDerive() {
        LocalDateTime.from(LocalTime.of(12, 30));
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_factory_CalendricalObject_null() {
        LocalDateTime.from((CalendricalObject) null);
    }

    //-----------------------------------------------------------------------
    // parse()
    //-----------------------------------------------------------------------
//    @Test(dataProvider="sampleToString", groups={"tck"})
//    public void test_parse(int y, int month, int d, int h, int m, int s, int n, String text) {
//        LocalDateTime t = LocalDateTime.parse(text);
//        assertEquals(t.getYear(), y);
//        assertEquals(t.getMonthOfYear().getValue(), month);
//        assertEquals(t.getDayOfMonth(), d);
//        assertEquals(t.getHourOfDay(), h);
//        assertEquals(t.getMinuteOfHour(), m);
//        assertEquals(t.getSecondOfMinute(), s);
//        assertEquals(t.getNanoOfSecond(), n);
//    }
//
//    @Test(expectedExceptions=CalendricalParseException.class, groups={"tck"})
//    public void factory_parse_illegalValue() {
//        LocalDateTime.parse("2008-06-32T11:15");
//    }
//
//    @Test(expectedExceptions=CalendricalParseException.class, groups={"tck"})
//    public void factory_parse_invalidValue() {
//        LocalDateTime.parse("2008-06-31T11:15");
//    }
//
//    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
//    public void factory_parse_nullText() {
//        LocalDateTime.parse((String) null);
//    }

    //-----------------------------------------------------------------------
    // parse(CalendricalFormatter)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_parse_formatter() {
        final LocalDateTime dateTime = LocalDateTime.of(2010, 12, 3, 11, 30, 45);
        CalendricalFormatter f = new CalendricalFormatter() {
            @Override
            public String print(CalendricalObject calendrical) {
                throw new AssertionError();
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Object parse(String text, Class type) {
                return dateTime;
            }
        };
        LocalDateTime test = LocalDateTime.parse("ANY", f);
        assertEquals(test, dateTime);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_parse_formatter_nullText() {
        CalendricalFormatter f = new CalendricalFormatter() {
            @Override
            public String print(CalendricalObject calendrical) {
                throw new AssertionError();
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Object parse(String text, Class type) {
                assertEquals(text, null);
                throw new NullPointerException();
            }
        };
        LocalDateTime.parse((String) null, f);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_parse_formatter_nullFormatter() {
        LocalDateTime.parse("ANY", null);
    }

    //-----------------------------------------------------------------------
    // get(DateTimeField)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_get_DateTimeField() {
        LocalDateTime test = LocalDateTime.of(2008, 6, 30, 12, 30, 40, 987654321);
        assertEquals(test.get(LocalDateTimeField.YEAR), 2008);
        assertEquals(test.get(LocalDateTimeField.MONTH_OF_YEAR), 6);
        assertEquals(test.get(LocalDateTimeField.DAY_OF_MONTH), 30);
        assertEquals(test.get(LocalDateTimeField.DAY_OF_WEEK), 1);
        assertEquals(test.get(LocalDateTimeField.DAY_OF_YEAR), 182);
        
        assertEquals(test.get(LocalDateTimeField.HOUR_OF_DAY), 12);
        assertEquals(test.get(LocalDateTimeField.MINUTE_OF_HOUR), 30);
        assertEquals(test.get(LocalDateTimeField.SECOND_OF_MINUTE), 40);
        assertEquals(test.get(LocalDateTimeField.NANO_OF_SECOND), 987654321);
        assertEquals(test.get(LocalDateTimeField.HOUR_OF_AMPM), 0);
        assertEquals(test.get(LocalDateTimeField.AMPM_OF_DAY), AmPmOfDay.PM.getValue());
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"} )
    public void test_get_DateTimeField_null() {
        LocalDateTime test = LocalDateTime.of(2008, 6, 30, 12, 30, 40, 987654321);
        test.get((DateTimeField) null);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"} )
    public void test_get_DateTimeField_invalidField() {
        TEST_2007_07_15_12_30_40_987654321.get(MockFieldNoValue.INSTANCE);
    }

    //-----------------------------------------------------------------------
    // extract(Class)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_extract_Class() {
        LocalDateTime test = LocalDateTime.of(2008, 6, 30, 12, 30, 40, 987654321);
        assertEquals(test.extract(LocalDate.class), test.toLocalDate());
        assertEquals(test.extract(LocalTime.class), test.toLocalTime());
        assertEquals(test.extract(LocalDateTime.class), test);
        assertEquals(test.extract(OffsetDate.class), null);
        assertEquals(test.extract(OffsetTime.class), null);
        assertEquals(test.extract(OffsetDateTime.class), null);
        assertEquals(test.extract(ZonedDateTime.class), null);
        assertEquals(test.extract(ZoneOffset.class), null);
        assertEquals(test.extract(ZoneId.class), null);
        assertEquals(test.extract(Instant.class), null);
        assertEquals(test.extract(Class.class), LocalDateTime.class);
        assertEquals(test.extract(String.class), null);
        assertEquals(test.extract(BigDecimal.class), null);
        assertEquals(test.extract(null), null);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="sampleDates")
    Object[][] provider_sampleDates() {
        return new Object[][] {
            {2008, 7, 5},
            {2007, 7, 5},
            {2006, 7, 5},
            {2005, 7, 5},
            {2004, 1, 1},
            {-1, 1, 2},
        };
    }

    @DataProvider(name="sampleTimes")
    Object[][] provider_sampleTimes() {
        return new Object[][] {
            {0, 0, 0, 0},
            {0, 0, 0, 1},
            {0, 0, 1, 0},
            {0, 0, 1, 1},
            {0, 1, 0, 0},
            {0, 1, 0, 1},
            {0, 1, 1, 0},
            {0, 1, 1, 1},
            {1, 0, 0, 0},
            {1, 0, 0, 1},
            {1, 0, 1, 0},
            {1, 0, 1, 1},
            {1, 1, 0, 0},
            {1, 1, 0, 1},
            {1, 1, 1, 0},
            {1, 1, 1, 1},
        };
    }

    //-----------------------------------------------------------------------
    // get*()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates", groups={"tck"})
    public void test_get_dates(int y, int m, int d) {
        LocalDateTime a = LocalDateTime.of(y, m, d, 12, 30);
        assertEquals(a.getYear(), y);
        assertEquals(a.getMonthOfYear(), MonthOfYear.of(m));
        assertEquals(a.getDayOfMonth(), d);
    }

    @Test(dataProvider="sampleDates", groups={"tck"})
    public void test_getDOY(int y, int m, int d) {
        LocalDateTime a = LocalDateTime.of(y, m, d, 12 ,30);
        int total = 0;
        for (int i = 1; i < m; i++) {
            total += MonthOfYear.of(i).lengthInDays(isIsoLeap(y));
        }
        int doy = total + d;
        assertEquals(a.getDayOfYear(), doy);
    }

    @Test(dataProvider="sampleTimes", groups={"tck"})
    public void test_get_times(int h, int m, int s, int ns) {
        LocalDateTime a = LocalDateTime.of(TEST_2007_07_15_12_30_40_987654321.toLocalDate(), LocalTime.of(h, m, s, ns));
        assertEquals(a.getHourOfDay(), h);
        assertEquals(a.getMinuteOfHour(), m);
        assertEquals(a.getSecondOfMinute(), s);
        assertEquals(a.getNanoOfSecond(), ns);
    }

    //-----------------------------------------------------------------------
    // getDayOfWeek()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_getDayOfWeek() {
        DayOfWeek dow = DayOfWeek.MONDAY;
        for (MonthOfYear month : MonthOfYear.values()) {
            int length = month.lengthInDays(false);
            for (int i = 1; i <= length; i++) {
                LocalDateTime d = LocalDateTime.of(LocalDate.of(2007, month, i),
                        TEST_2007_07_15_12_30_40_987654321.toLocalTime());
                assertSame(d.getDayOfWeek(), dow);
                dow = dow.next();
            }
        }
    }

    //-----------------------------------------------------------------------
    // isLeapYear()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_isLeapYear() {
        assertEquals(LocalDateTime.of(1999, 1, 1, 0, 0).isLeapYear(), false);
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0).isLeapYear(), true);
        assertEquals(LocalDateTime.of(2001, 1, 1, 0, 0).isLeapYear(), false);
        assertEquals(LocalDateTime.of(2002, 1, 1, 0, 0).isLeapYear(), false);
        assertEquals(LocalDateTime.of(2003, 1, 1, 0, 0).isLeapYear(), false);
        assertEquals(LocalDateTime.of(2004, 1, 1, 0, 0).isLeapYear(), true);
        assertEquals(LocalDateTime.of(2005, 1, 1, 0, 0).isLeapYear(), false);
    }

    //-----------------------------------------------------------------------
    // with()
    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_same_with_DateAdjuster() {
        DateAdjuster dateAdjuster = DateAdjusters.lastDayOfMonth();
        LocalDateTime adjusted = TEST_2007_07_15_12_30_40_987654321.with(dateAdjuster);
        assertSame(adjusted.toLocalTime(), TEST_2007_07_15_12_30_40_987654321.toLocalTime());
    }
    
    @Test(groups={"tck"})
    public void test_equals_with_DateAdjuster() {
        DateAdjuster dateAdjuster = DateAdjusters.lastDayOfMonth();
        LocalDateTime adjusted = TEST_2007_07_15_12_30_40_987654321.with(dateAdjuster);
        assertEquals(adjusted.toLocalTime(), TEST_2007_07_15_12_30_40_987654321.toLocalTime());
    }

    @Test(groups={"tck"})
    public void test_with_DateAdjuster() {
        DateAdjuster dateAdjuster = DateAdjusters.lastDayOfMonth();
        LocalDateTime adjusted = TEST_2007_07_15_12_30_40_987654321.with(dateAdjuster);
        assertEquals(adjusted.toLocalDate(), dateAdjuster.adjustDate(TEST_2007_07_15_12_30_40_987654321.toLocalDate()));
    }
    
    
    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_same_with_TimeAdjuster() {
        TimeAdjuster timeAdjuster = new TimeAdjuster() {
            public LocalTime adjustTime(LocalTime time) {
                return LocalTime.of(23, 5);
            }
        };
        LocalDateTime adjusted = TEST_2007_07_15_12_30_40_987654321.with(timeAdjuster);
        assertSame(adjusted.toLocalDate(), TEST_2007_07_15_12_30_40_987654321.toLocalDate());
    }

    @Test(groups={"tck"})
    public void test_with_TimeAdjuster() {
        TimeAdjuster timeAdjuster = new TimeAdjuster() {
            public LocalTime adjustTime(LocalTime time) {
                return LocalTime.of(23, 5);
            }
        };
        LocalDateTime adjusted = TEST_2007_07_15_12_30_40_987654321.with(timeAdjuster);
        assertEquals(adjusted.toLocalTime(), LocalTime.of(23, 5));
    }
    
    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_with_null() {
        TEST_2007_07_15_12_30_40_987654321.with(null);
    }

    //-----------------------------------------------------------------------
    // withYear()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withYear_int_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withYear(2008);
        check(t, 2008, 7, 15, 12, 30, 40, 987654321);
    }

    @Test(groups={"implementation"})
    public void test_withYear_int_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withYear(2007);
        assertSame(t.toLocalDate(), TEST_2007_07_15_12_30_40_987654321.toLocalDate());
        assertSame(t.toLocalTime(), TEST_2007_07_15_12_30_40_987654321.toLocalTime());
    }
    
    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withYear_int_invalid() {
        TEST_2007_07_15_12_30_40_987654321.withYear(Year.MIN_YEAR - 1);
    }

    @Test(groups={"tck"})
    public void test_withYear_int_adjustDay() {
        LocalDateTime t = LocalDateTime.of(2008, 2, 29, 12, 30).withYear(2007);
        LocalDateTime expected = LocalDateTime.of(2007, 2, 28, 12, 30);
        assertEquals(t, expected);
    }

    //-----------------------------------------------------------------------
    // withMonthOfYear()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withMonthOfYear_int_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withMonthOfYear(1);
        check(t, 2007, 1, 15, 12, 30, 40, 987654321);
    }

    @Test(groups={"implementation"})
    public void test_withMonthOfYear_int_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withMonthOfYear(7);
        assertSame(t.toLocalDate(), TEST_2007_07_15_12_30_40_987654321.toLocalDate());
        assertSame(t.toLocalTime(), TEST_2007_07_15_12_30_40_987654321.toLocalTime());
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withMonthOfYear_int_invalid() {
        TEST_2007_07_15_12_30_40_987654321.withMonthOfYear(13);
    }

    @Test(groups={"tck"})
    public void test_withMonthOfYear_int_adjustDay() {
        LocalDateTime t = LocalDateTime.of(2007, 12, 31, 12, 30).withMonthOfYear(11);
        LocalDateTime expected = LocalDateTime.of(2007, 11, 30, 12, 30);
        assertEquals(t, expected);
    }

    //-----------------------------------------------------------------------
    // withDayOfMonth()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withDayOfMonth_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDayOfMonth(1);
        check(t, 2007, 7, 1, 12, 30, 40, 987654321);
    }

    @Test(groups={"implementation"})
    public void test_withDayOfMonth_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDayOfMonth(15);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfMonth_invalid() {
        LocalDateTime.of(2007, 11, 30, 12, 30).withDayOfMonth(32);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfMonth_invalidCombination() {
        LocalDateTime.of(2007, 11, 30, 12, 30).withDayOfMonth(31);
    }

    //-----------------------------------------------------------------------
    // withDayOfYear(int)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withDayOfYear_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDayOfYear(33);
        assertEquals(t, LocalDateTime.of(2007, 2, 2, 12, 30, 40, 987654321));
    }

    @Test(groups={"implementation"})
    public void test_withDayOfYear_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDayOfYear(31 + 28 + 31 + 30 + 31 + 30 + 15);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfYear_illegal() {
        TEST_2007_07_15_12_30_40_987654321.withDayOfYear(367);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfYear_invalid() {
        TEST_2007_07_15_12_30_40_987654321.withDayOfYear(366);
    }

    //-----------------------------------------------------------------------
    // withDate(int,MonthOfYear,int)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withDate_iMi() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2008, MonthOfYear.FEBRUARY, 29);
        check(t, 2008, 2, 29, 12, 30, 40, 987654321);
    }

    @Test(groups={"implementation"})
    public void test_withDate_iMi_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2007, MonthOfYear.JULY, 15);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_iMi_sameYear() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2007, MonthOfYear.JUNE, 14);
        check(t, 2007, 6, 14, 12, 30, 40, 987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_iMi_sameMonth() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2006, MonthOfYear.JULY, 14);
        check(t, 2006, 7, 14, 12, 30, 40, 987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_iMi_sameDay() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2006, MonthOfYear.JUNE, 15);
        check(t, 2006, 6, 15, 12, 30, 40, 987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_iMi_dayChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2007, MonthOfYear.JULY, 16);
        check(t, 2007, 7, 16, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_iMi_yearTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withDate(Integer.MIN_VALUE, MonthOfYear.FEBRUARY, 29);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_withDate_iMi_monthNull() {
        TEST_2007_07_15_12_30_40_987654321.withDate(2008, null, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_iMi_dayTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withDate(2008, 2, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_iMi_dayTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withDate(2008, 3, 32);
    }

    //-----------------------------------------------------------------------
    // withDate()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withDate() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2008, 2, 29);
        check(t, 2008, 2, 29, 12, 30, 40, 987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2007, 7, 15);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_sameYear() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2007, 6, 14);
        check(t, 2007, 6, 14, 12, 30, 40, 987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_sameMonth() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2006, 7, 14);
        check(t, 2006, 7, 14, 12, 30, 40, 987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_sameDay() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2006, 6, 15);
        check(t, 2006, 6, 15, 12, 30, 40, 987654321);
    }

    @Test(groups={"tck"})
    public void test_withDate_dayChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withDate(2007, 7, 16);
        check(t, 2007, 7, 16, 12, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_yearTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withDate(Integer.MIN_VALUE, 2, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_monthTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withDate(2008, 0, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_monthTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withDate(2008, 13, 29);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_dayTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withDate(2008, 2, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDate_dayTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withDate(2008, 3, 32);
    }

    //-----------------------------------------------------------------------
    // withHourOfDay()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withHourOfDay_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321;
        for (int i = 0; i < 24; i++) {
            t = t.withHourOfDay(i);
            assertEquals(t.getHourOfDay(), i);
        }
    }

    @Test(groups={"implementation"})
    public void test_withHourOfDay_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withHourOfDay(12);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(groups={"implementation"})
    public void test_withHourOfDay_toMidnight() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(1, 0)).withHourOfDay(0);
        assertSame(t.toLocalTime(), LocalTime.MIDNIGHT);
    }

    @Test(groups={"implementation"})
    public void test_withHourOfDay_toMidday() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(1, 0)).withHourOfDay(12);
        assertSame(t.toLocalTime(), LocalTime.MIDDAY);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withHourOfDay_hourTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withHourOfDay(-1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withHourOfDay_hourTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withHourOfDay(24);
    }

    //-----------------------------------------------------------------------
    // withMinuteOfHour()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withMinuteOfHour_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321;
        for (int i = 0; i < 60; i++) {
            t = t.withMinuteOfHour(i);
            assertEquals(t.getMinuteOfHour(), i);
        }
    }

    @Test(groups={"implementation"})
    public void test_withMinuteOfHour_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withMinuteOfHour(30);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(groups={"implementation"})
    public void test_withMinuteOfHour_toMidnight() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(0, 1)).withMinuteOfHour(0);
        assertSame(t.toLocalTime(), LocalTime.MIDNIGHT);
    }

    @Test(groups={"implementation"})
    public void test_withMinuteOfHour_toMidday() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 1)).withMinuteOfHour(0);
        assertSame(t.toLocalTime(), LocalTime.MIDDAY);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withMinuteOfHour_minuteTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withMinuteOfHour(-1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withMinuteOfHour_minuteTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withMinuteOfHour(60);
    }

    //-----------------------------------------------------------------------
    // withSecondOfMinute()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withSecondOfMinute_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321;
        for (int i = 0; i < 60; i++) {
            t = t.withSecondOfMinute(i);
            assertEquals(t.getSecondOfMinute(), i);
        }
    }

    @Test(groups={"implementation"})
    public void test_withSecondOfMinute_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withSecondOfMinute(40);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(groups={"implementation"})
    public void test_withSecondOfMinute_toMidnight() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(0, 0, 1)).withSecondOfMinute(0);
        assertSame(t.toLocalTime(), LocalTime.MIDNIGHT);
    }

    @Test(groups={"implementation"})
    public void test_withSecondOfMinute_toMidday() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 0, 1)).withSecondOfMinute(0);
        assertSame(t.toLocalTime(), LocalTime.MIDDAY);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withSecondOfMinute_secondTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withSecondOfMinute(-1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withSecondOfMinute_secondTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withSecondOfMinute(60);
    }

    //-----------------------------------------------------------------------
    // withNanoOfSecond()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withNanoOfSecond_normal() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321;
        t = t.withNanoOfSecond(1);
        assertEquals(t.getNanoOfSecond(), 1);
        t = t.withNanoOfSecond(10);
        assertEquals(t.getNanoOfSecond(), 10);
        t = t.withNanoOfSecond(100);
        assertEquals(t.getNanoOfSecond(), 100);
        t = t.withNanoOfSecond(999999999);
        assertEquals(t.getNanoOfSecond(), 999999999);
    }

    @Test(groups={"implementation"})
    public void test_withNanoOfSecond_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withNanoOfSecond(987654321);
        assertSame(t, TEST_2007_07_15_12_30_40_987654321);
    }

    @Test(groups={"implementation"})
    public void test_withNanoOfSecond_toMidnight() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(0, 0, 0, 1)).withNanoOfSecond(0);
        assertSame(t.toLocalTime(), LocalTime.MIDNIGHT);
    }

    @Test(groups={"implementation"})
    public void test_withNanoOfSecond_toMidday() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 0, 0, 1)).withNanoOfSecond(0);
        assertSame(t.toLocalTime(), LocalTime.MIDDAY);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withNanoOfSecond_nanoTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withNanoOfSecond(-1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withNanoOfSecond_nanoTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withNanoOfSecond(1000000000);
    }

    //-----------------------------------------------------------------------
    // withTime()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withTime_2ints() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withTime(13, 40);
        check(t, 2007, 7, 15, 13, 40, 0, 0);
    }

    @Test(groups={"implementation"})
    public void test_withTime_2ints_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30));
        LocalDateTime wt = t.withTime(12, 30);
        assertSame(t, wt);
    }

    @Test(groups={"tck"})
    public void test_withTime_2ints_sameHour() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(12, 20);
        check(t, 2007, 7, 15, 12, 20, 0, 0);
    }

    @Test(groups={"tck"})
    public void test_withTime_2ints_sameMinute() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(11, 30);
        check(t, 2007, 7, 15, 11, 30, 0, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_2ints_hourTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(-1, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_2ints_hourTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(24, 30);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_2ints_minuteTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_2ints_minuteTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 60);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withTime_3ints() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withTime(13, 40, 50);
        check(t, 2007, 7, 15, 13, 40, 50, 0);
    }

    @Test(groups={"implementation"})
    public void test_withTime_3ints_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40));
        LocalDateTime wt = t.withTime(12, 30, 40);
        assertSame(t, wt);
    }

    @Test(groups={"tck"})
    public void test_withTime_3ints_sameHour() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(12, 20, 30);
        check(t, 2007, 7, 15, 12, 20, 30, 0);
    }

    @Test(groups={"tck"})
    public void test_withTime_3ints_sameMinute() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(11, 30, 30);
        check(t, 2007, 7, 15, 11, 30, 30, 0);
    }

    @Test(groups={"tck"})
    public void test_withTime_3ints_sameSecond() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(11, 20, 40);
        check(t, 2007, 7, 15, 11, 20, 40, 0);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_3ints_hourTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(-1, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_3ints_hourTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(24, 30, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_3ints_minuteTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, -1, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_3ints_minuteTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 60, 40);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_3ints_secondTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 30, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_3ints_secondTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 30, 60);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withTime_4ints() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.withTime(13, 40, 50, 987654322);
        check(t, 2007, 7, 15, 13, 40, 50, 987654322);
    }

    @Test(groups={"implementation"})
    public void test_withTime_4ints_noChange() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40, 987654321));
        LocalDateTime wt = t.withTime(12, 30, 40, 987654321);
        assertSame(t, wt);
    }

    @Test(groups={"tck"})
    public void test_withTime_4ints_sameHour() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40, 987654321));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(12, 20, 30, 987654320);
        check(t, 2007, 7, 15, 12, 20, 30, 987654320);
    }

    @Test(groups={"tck"})
    public void test_withTime_4ints_sameMinute() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40, 987654321));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(11, 30, 30, 987654320);
        check(t, 2007, 7, 15, 11, 30, 30, 987654320);
    }

    @Test(groups={"tck"})
    public void test_withTime_4ints_sameSecond() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40, 987654321));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(11, 20, 40, 987654320);
        check(t, 2007, 7, 15, 11, 20, 40, 987654320);
    }

    @Test(groups={"tck"})
    public void test_withTime_4ints_sameNano() {
        LocalDateTime t = TEST_2007_07_15_12_30_40_987654321.with(LocalTime.of(12, 30, 40, 987654321));
        t = TEST_2007_07_15_12_30_40_987654321.withTime(11, 20, 30, 987654321);
        check(t, 2007, 7, 15, 11, 20, 30, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_4ints_hourTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(-1, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_4ints_hourTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(24, 30, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_4ints_minuteTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, -1, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_4ints_minuteTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 60, 40, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_4ints_secondTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 30, -1, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_4ints_secondTooHigh() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 30, 60, 987654321);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withTime_4ints_nanoTooLow() {
        TEST_2007_07_15_12_30_40_987654321.withTime(12, 30, 40, -1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_with
