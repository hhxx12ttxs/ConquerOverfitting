/*
9 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

import javax.time.calendrical.CalendricalFormatter;
import javax.time.calendrical.CalendricalObject;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.LocalDateTimeField;
import javax.time.calendrical.LocalDateTimeUnit;
import javax.time.calendrical.MockFieldNoValue;
import javax.time.extended.Year;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test OffsetDate.
 */
@Test
public class TestOffsetDate extends AbstractTest {
    private static final ZoneOffset OFFSET_PONE = ZoneOffset.ofHours(1);
    private static final ZoneOffset OFFSET_PTWO = ZoneOffset.ofHours(2);
    private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");
    private static final ZoneId ZONE_GAZA = ZoneId.of("Asia/Gaza");
    
    private OffsetDate TEST_2007_07_15_PONE;
    private OffsetDate MAX_DATE;
    private OffsetDate MIN_DATE;
    private Instant MAX_INSTANT;
    private Instant MIN_INSTANT;

    @BeforeMethod(groups={"tck","implementation"})
    public void setUp() {
        TEST_2007_07_15_PONE = OffsetDate.of(2007, 7, 15, OFFSET_PONE);
        
        OffsetDateTime max = OffsetDateTime.ofMidnight(Year.MAX_YEAR, 12, 31, ZoneOffset.UTC);
        OffsetDateTime min = OffsetDateTime.ofMidnight(Year.MIN_YEAR, 1, 1, ZoneOffset.UTC);
        MAX_DATE = max.toOffsetDate();
        MIN_DATE = min.toOffsetDate();
        MAX_INSTANT = max.toInstant();
        MIN_INSTANT = min.toInstant();
    }

    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_interfaces() {
        Object obj = TEST_2007_07_15_PONE;
        assertTrue(obj instanceof CalendricalObject);
        assertTrue(obj instanceof Serializable);
        assertTrue(obj instanceof Comparable<?>);
    }

    @Test(groups={"tck"})
    public void test_serialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(TEST_2007_07_15_PONE);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                baos.toByteArray()));
        assertEquals(ois.readObject(), TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_immutable() {
        Class<OffsetDate> cls = OffsetDate.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().contains("$") == false) {
                assertTrue(Modifier.isPrivate(field.getModifiers()));
                assertTrue(Modifier.isFinal(field.getModifiers()));
            }
        }
    }

    //-----------------------------------------------------------------------
    // now()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void now() {
        OffsetDate expected = OffsetDate.now(Clock.systemDefaultZone());
        OffsetDate test = OffsetDate.now();
        for (int i = 0; i < 100; i++) {
            if (expected.equals(test)) {
                return;
            }
            expected = OffsetDate.now(Clock.systemDefaultZone());
            test = OffsetDate.now();
        }
        assertEquals(test, expected);
    }

    //-----------------------------------------------------------------------
    // now(Clock)
    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class, groups={"implementation"})
    public void now_Clock_nullClock() {
        OffsetDate.now(null);
    }

    @Test(groups={"tck"})
    public void now_Clock_allSecsInDay_utc() {
        for (int i = 0; i < (2 * 24 * 60 * 60); i++) {
            Instant instant = Instant.ofEpochSecond(i);
            Clock clock = Clock.fixed(instant, ZoneId.UTC);
            OffsetDate test = OffsetDate.now(clock);
            assertEquals(test.getYear(), 1970);
            assertEquals(test.getMonthOfYear(), MonthOfYear.JANUARY);
            assertEquals(test.getDayOfMonth(), (i < 24 * 60 * 60 ? 1 : 2));
            assertEquals(test.getOffset(), ZoneOffset.UTC);
        }
    }

    @Test(groups={"tck"})
    public void now_Clock_allSecsInDay_beforeEpoch() {
        for (int i =-1; i >= -(2 * 24 * 60 * 60); i--) {
            Instant instant = Instant.ofEpochSecond(i);
            Clock clock = Clock.fixed(instant, ZoneId.UTC);
            OffsetDate test = OffsetDate.now(clock);
            assertEquals(test.getYear(), 1969);
            assertEquals(test.getMonthOfYear(), MonthOfYear.DECEMBER);
            assertEquals(test.getDayOfMonth(), (i >= -24 * 60 * 60 ? 31 : 30));
            assertEquals(test.getOffset(), ZoneOffset.UTC);
        }
    }

    @Test(groups={"tck"})
    public void now_Clock_offsets() {
        OffsetDateTime base = OffsetDateTime.of(1970, 1, 1, 12, 0, ZoneOffset.UTC);
        for (int i = -9; i < 15; i++) {
            ZoneOffset offset = ZoneOffset.ofHours(i);
            Clock clock = Clock.fixed(base.toInstant(), ZoneId.of(offset));
            OffsetDate test = OffsetDate.now(clock);
            assertEquals(test.getYear(), 1970);
            assertEquals(test.getMonthOfYear(), MonthOfYear.JANUARY);
            assertEquals(test.getDayOfMonth(), i >= 12 ? 2 : 1);
            assertEquals(test.getOffset(), offset);
        }
    }

    //-----------------------------------------------------------------------
    // factories
    //-----------------------------------------------------------------------
    void check(OffsetDate test, int y, int mo, int d, ZoneOffset offset) {
        assertEquals(test.getYear(), y);
        assertEquals(test.getMonthOfYear().getValue(), mo);
        assertEquals(test.getDayOfMonth(), d);
        assertEquals(test.getOffset(), offset);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_intMonthInt() {
        OffsetDate test = OffsetDate.of(2007, MonthOfYear.JULY, 15, OFFSET_PONE);
        check(test, 2007, 7, 15, OFFSET_PONE);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_ints() {
        OffsetDate test = OffsetDate.of(2007, 7, 15, OFFSET_PONE);
        check(test, 2007, 7, 15, OFFSET_PONE);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_intsMonthOffset() {
        assertEquals(TEST_2007_07_15_PONE, OffsetDate.of(2007, MonthOfYear.JULY, 15, OFFSET_PONE));
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_intsMonthOffset_dayTooLow() {
        OffsetDate.of(2007, MonthOfYear.JANUARY, 0, OFFSET_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_intsMonthOffset_dayTooHigh() {
        OffsetDate.of(2007, MonthOfYear.JANUARY, 32, OFFSET_PONE);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_intsMonthOffset_nullMonth() {
        OffsetDate.of(2007, null, 30, OFFSET_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_intsMonthOffset_yearTooLow() {
        OffsetDate.of(Integer.MIN_VALUE, MonthOfYear.JANUARY, 1, OFFSET_PONE);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_intsMonthOffset_nullOffset() {
        OffsetDate.of(2007, MonthOfYear.JANUARY, 30, null);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_intsOffset() {
        OffsetDate test = OffsetDate.of(2007, 7, 15, OFFSET_PONE);
        check(test, 2007, 7, 15, OFFSET_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_ints_dayTooLow() {
        OffsetDate.of(2007, 1, 0, OFFSET_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_ints_dayTooHigh() {
        OffsetDate.of(2007, 1, 32, OFFSET_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_ints_monthTooLow() {
        OffsetDate.of(2007, 0, 1, OFFSET_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_ints_monthTooHigh() {
        OffsetDate.of(2007, 13, 1, OFFSET_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_of_ints_yearTooLow() {
        OffsetDate.of(Integer.MIN_VALUE, 1, 1, OFFSET_PONE);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_ints_nullOffset() {
        OffsetDate.of(2007, 1, 1, (ZoneOffset) null);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})    
    public void factory_of_LocalDateZoneOffset() {
        LocalDate localDate = LocalDate.of(2008, 6, 30);
        OffsetDate test = OffsetDate.of(localDate, OFFSET_PONE);
        check(test, 2008, 6, 30, OFFSET_PONE);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_LocalDateZoneOffset_nullDate() {
        OffsetDate.of((LocalDate) null, OFFSET_PONE);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_LocalDateZoneOffset_nullOffset() {
        LocalDate localDate = LocalDate.of(2008, 6, 30);
        OffsetDate.of(localDate, (ZoneOffset) null);
    }

    //-----------------------------------------------------------------------
    // ofInstant()
    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_ofInstant_nullInstant() {
        OffsetDate.ofInstant((Instant) null, ZoneOffset.UTC);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_ofInstant_nullOffset() {
        Instant instant = Instant.ofEpochSecond(0L);
        OffsetDate.ofInstant(instant, (ZoneOffset) null);
    }

    @Test(groups={"tck"})
    public void factory_ofInstant_allSecsInDay_utc() {
        for (int i = 0; i < (2 * 24 * 60 * 60); i++) {
            Instant instant = Instant.ofEpochSecond(i);
            OffsetDate test = OffsetDate.ofInstant(instant, ZoneOffset.UTC);
            assertEquals(test.getYear(), 1970);
            assertEquals(test.getMonthOfYear(), MonthOfYear.JANUARY);
            assertEquals(test.getDayOfMonth(), (i < 24 * 60 * 60 ? 1 : 2));
        }
    }

    @Test(groups={"tck"})
    public void factory_ofInstant_allSecsInDay_offset() {
        for (int i = 0; i < (2 * 24 * 60 * 60); i++) {
            Instant instant = Instant.ofEpochSecond(i);
            OffsetDate test = OffsetDate.ofInstant(instant.minusSeconds(OFFSET_PONE.getTotalSeconds()), OFFSET_PONE);
            assertEquals(test.getYear(), 1970);
            assertEquals(test.getMonthOfYear(), MonthOfYear.JANUARY);
            assertEquals(test.getDayOfMonth(), (i < 24 * 60 * 60) ? 1 : 2);
        }
    }

    @Test(groups={"tck"})
    public void factory_ofInstant_beforeEpoch() {
        for (int i =-1; i >= -(24 * 60 * 60); i--) {
            Instant instant = Instant.ofEpochSecond(i);
            OffsetDate test = OffsetDate.ofInstant(instant, ZoneOffset.UTC);
            assertEquals(test.getYear(), 1969);
            assertEquals(test.getMonthOfYear(), MonthOfYear.DECEMBER);
            assertEquals(test.getDayOfMonth(), 31);
        }
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_ofInstant_maxYear() {
        OffsetDate test = OffsetDate.ofInstant(MAX_INSTANT, ZoneOffset.UTC);
        assertEquals(test, MAX_DATE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofInstant_tooBig() {
        OffsetDate.ofInstant(MAX_INSTANT.plusSeconds(24 * 60 * 60), ZoneOffset.UTC);
    }
    
    @Test(groups={"tck"})
    public void factory_ofInstant_minYear() {
        OffsetDate test = OffsetDate.ofInstant(MIN_INSTANT, ZoneOffset.UTC);
        assertEquals(test, MIN_DATE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void factory_ofInstant_tooLow() {
        OffsetDate.ofInstant(MIN_INSTANT.minusNanos(1), ZoneOffset.UTC);
    }

    //-----------------------------------------------------------------------
    // from()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_factory_CalendricalObject() {
        assertEquals(OffsetDate.from(OffsetDate.of(2007, 7, 15, OFFSET_PONE)), OffsetDate.of(2007, 7, 15, OFFSET_PONE));
        assertEquals(OffsetDate.from(OffsetDateTime.of(2007, 7, 15, 17, 30, OFFSET_PONE)), OffsetDate.of(2007, 7, 15, OFFSET_PONE));
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_factory_CalendricalObject_invalid_noDerive() {
        OffsetDate.from(LocalTime.of(12, 30));
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_factory_CalendricalObject_null() {
        OffsetDate.from((CalendricalObject) null);
    }

    //-----------------------------------------------------------------------
    // parse()
    //-----------------------------------------------------------------------
//    @Test(dataProvider="sampleToString", groups={"tck"})
//    public void factory_parse_validText(int y, int m, int d, String offsetId, String parsable) {
//        OffsetDate t = OffsetDate.parse(parsable);
//        assertNotNull(t, parsable);
//        assertEquals(t.getYear(), y, parsable);
//        assertEquals(t.getMonthOfYear().getValue(), m, parsable);
//        assertEquals(t.getDayOfMonth(), d, parsable);
//        assertEquals(t.getOffset(), ZoneOffset.of(offsetId));
//    }

    @DataProvider(name="sampleBadParse")
    Object[][] provider_sampleBadParse() {
        return new Object[][]{
                {"2008/07/05"},
                {"10000-01-01"},
                {"2008-1-1"},
                {"2008--01"},
                {"ABCD-02-01"},
                {"2008-AB-01"},
                {"2008-02-AB"},
                {"-0000-02-01"},
                {"2008-02-01Y"},
                {"2008-02-01+19:00"},
                {"2008-02-01+01/00"},
                {"2008-02-01+1900"},
                {"2008-02-01+01:60"},
                {"2008-02-01+01:30:123"},
                {"2008-02-01"},
                {"2008-02-01+01:00[Europe/Paris]"},
        };
    }

//    @Test(dataProvider="sampleBadParse", expectedExceptions=CalendricalParseException.class, groups={"tck"})
//    public void factory_parse_invalidText(String unparsable) {
//        OffsetDate.parse(unparsable);
//    }
//
//    @Test(expectedExceptions=CalendricalParseException.class, groups={"tck"})
//    public void factory_parse_illegalValue() {
//        OffsetDate.parse("2008-06-32+01:00");
//    }
//
//    @Test(expectedExceptions=CalendricalParseException.class, groups={"tck"})
//    public void factory_parse_invalidValue() {
//        OffsetDate.parse("2008-06-31+01:00");
//    }
//
//    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
//    public void factory_parse_nullText() {
//        OffsetDate.parse((String) null);
//    }

    //-----------------------------------------------------------------------
    // parse(CalendricalFormatter)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_parse_formatter() {
        final OffsetDate date = OffsetDate.of(2010, 12, 3, ZoneOffset.ofHours(1));
        CalendricalFormatter f = new CalendricalFormatter() {
            @Override
            public String print(CalendricalObject calendrical) {
                throw new AssertionError();
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Object parse(String text, Class type) {
                return date;
            }
        };
        OffsetDate test = OffsetDate.parse("ANY", f);
        assertEquals(test, date);
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
        OffsetDate.parse((String) null, f);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_parse_formatter_nullFormatter() {
        OffsetDate.parse("ANY", null);
    }

    //-----------------------------------------------------------------------
    // constructor
    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void constructor_nullDate() throws Throwable  {
        Constructor<OffsetDate> con = OffsetDate.class.getDeclaredConstructor(LocalDate.class, ZoneOffset.class);
        con.setAccessible(true);
        try {
            con.newInstance(null, OFFSET_PONE);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void constructor_nullOffset() throws Throwable  {
        Constructor<OffsetDate> con = OffsetDate.class.getDeclaredConstructor(LocalDate.class, ZoneOffset.class);
        con.setAccessible(true);
        try {
            con.newInstance(LocalDate.of(2008, 6, 30), null);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    //-----------------------------------------------------------------------
    // basics
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleDates")
    Object[][] provider_sampleDates() {
        return new Object[][] {
            {2008, 7, 5, OFFSET_PTWO},
            {2007, 7, 5, OFFSET_PONE},
            {2006, 7, 5, OFFSET_PTWO},
            {2005, 7, 5, OFFSET_PONE},
            {2004, 1, 1, OFFSET_PTWO},
            {-1, 1, 2, OFFSET_PONE},
            {999999, 11, 20, ZoneOffset.ofHoursMinutesSeconds(6, 9, 12)},
        };
    }

    @Test(dataProvider="sampleDates", groups={"tck"})
    public void test_get_OffsetDate(int y, int m, int d, ZoneOffset offset) {
        LocalDate localDate = LocalDate.of(y, m, d);
        OffsetDate a = OffsetDate.of(localDate, offset);
        
        assertEquals(a.getYear(), localDate.getYear());
        assertEquals(a.getMonthOfYear(), localDate.getMonthOfYear());
        assertEquals(a.getDayOfMonth(), localDate.getDayOfMonth());
        assertEquals(a.getDayOfYear(), localDate.getDayOfYear());
        assertEquals(a.getDayOfWeek(), localDate.getDayOfWeek());
        assertEquals(a.isLeapYear(), isIsoLeap(a.getYear()));
        
        assertEquals(a.toString(), localDate.toString() + offset.toString());
        assertEquals(a.getOffset(), offset);
    }
    
    @Test(dataProvider="sampleDates", groups={"implementation"})
    public void test_get_Offset_LocalDate(int y, int m, int d, ZoneOffset offset) {
        LocalDate localDate = LocalDate.of(y, m, d);
        OffsetDate a = OffsetDate.of(localDate, offset);
        
        assertSame(a.getOffset(), offset);
        assertSame(a.toLocalDate(), localDate);
    }
    

    @Test(dataProvider="sampleDates", groups={"tck"})
    public void test_getDOY(int y, int m, int d, ZoneOffset offset) {
        OffsetDate a = OffsetDate.of(y, m, d, offset);
        int total = 0;
        for (int i = 1; i < m; i++) {
            total += MonthOfYear.of(i).lengthInDays(isIsoLeap(y));
        }
        int doy = total + d;
        assertEquals(a.getDayOfYear(), doy);
    }

    //-----------------------------------------------------------------------
    // get(DateTimeField)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_get_DateTimeField() {
        OffsetDate test = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        assertEquals(test.get(LocalDateTimeField.YEAR), 2008);
        assertEquals(test.get(LocalDateTimeField.MONTH_OF_YEAR), 6);
        assertEquals(test.get(LocalDateTimeField.DAY_OF_MONTH), 30);
        assertEquals(test.get(LocalDateTimeField.DAY_OF_WEEK), 1);
        assertEquals(test.get(LocalDateTimeField.DAY_OF_YEAR), 182);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"} )
    public void test_get_DateTimeField_null() {
        OffsetDate test = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        test.get((DateTimeField) null);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"} )
    public void test_get_DateTimeField_invalidField() {
        TEST_2007_07_15_PONE.get(MockFieldNoValue.INSTANCE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"} )
    public void test_get_DateTimeField_timeField() {
        TEST_2007_07_15_PONE.get(LocalDateTimeField.AMPM_OF_DAY);
    }

    //-----------------------------------------------------------------------
    // extract(Class)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_extract_Class() {
        OffsetDate test = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        assertEquals(test.extract(LocalDate.class), test.toLocalDate());
        assertEquals(test.extract(LocalTime.class), null);
        assertEquals(test.extract(LocalDateTime.class), null);
        assertEquals(test.extract(OffsetDate.class), test);
        assertEquals(test.extract(OffsetTime.class), null);
        assertEquals(test.extract(OffsetDateTime.class), null);
        assertEquals(test.extract(ZonedDateTime.class), null);
        assertEquals(test.extract(ZoneOffset.class), test.getOffset());
        assertEquals(test.extract(ZoneId.class), null);
        assertEquals(test.extract(Instant.class), null);
        assertEquals(test.extract(Class.class), OffsetDate.class);
        assertEquals(test.extract(String.class), null);
        assertEquals(test.extract(BigDecimal.class), null);
        assertEquals(test.extract(null), null);
    }

    //-----------------------------------------------------------------------
    // getDayOfWeek()
    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_getDayOfWeek() {
        DayOfWeek dow = DayOfWeek.MONDAY;
        ZoneOffset[] offsets = new ZoneOffset[] {OFFSET_PONE, OFFSET_PTWO};

        for (MonthOfYear month : MonthOfYear.values()) {
            int length = month.lengthInDays(false);
            for (int i = 1; i <= length; i++) {
                OffsetDate d = OffsetDate.of(2007, month, i, offsets[i % 2]);
                assertSame(d.getDayOfWeek(), dow);
                dow = dow.next();
            }
        }
    }

    //-----------------------------------------------------------------------
    // withOffset()
    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_withOffset() {
        OffsetDate base = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        OffsetDate test = base.withOffset(OFFSET_PTWO);
        assertSame(test.toLocalDate(), base.toLocalDate());
        assertSame(test.getOffset(), OFFSET_PTWO);
    }

    @Test(groups={"implementation"})
    public void test_withOffset_noChange() {
        OffsetDate base = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        OffsetDate test = base.withOffset(OFFSET_PONE);
        assertSame(test, base);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_withOffset_null() {
        OffsetDate base = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        base.withOffset(null);
    }

    //-----------------------------------------------------------------------
    // with(DateAdjuster)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_with_LocalDate() {
        OffsetDate base = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        OffsetDate test = base.with(Year.of(2007));
        assertEquals(test.toLocalDate(), LocalDate.of(2007, 6, 30));
    }

    @Test(groups={"implementation"})
    public void test_with_Offset() {
        OffsetDate base = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        OffsetDate test = base.with(Year.of(2007));
        assertSame(test.getOffset(), base.getOffset());
    }
    
    @Test(groups={"implementation"})
    public void test_with_noChange() {
        LocalDate date = LocalDate.of(2008, 6, 30);
        OffsetDate base = OffsetDate.of(date, OFFSET_PONE);
        OffsetDate test = base.with(date);
        assertSame(test, base);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_with_null() {
        OffsetDate base = OffsetDate.of(2008, 6, 30, OFFSET_PONE);
        base.with(null);
    }

    //-----------------------------------------------------------------------
    // withYear()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withYear_int_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.withYear(2008);
        assertEquals(t, OffsetDate.of(2008, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_withYear_int_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.withYear(2007);
        assertSame(t, TEST_2007_07_15_PONE);
    }
    
    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withYear_int_invalid() {
        TEST_2007_07_15_PONE.withYear(Year.MIN_YEAR - 1);
    }

    @Test(groups={"tck"})
    public void test_withYear_int_adjustDay() {
        OffsetDate t = OffsetDate.of(2008, 2, 29, OFFSET_PONE).withYear(2007);
        OffsetDate expected = OffsetDate.of(2007, 2, 28, OFFSET_PONE);
        assertEquals(t, expected);
    }

    //-----------------------------------------------------------------------
    // withMonthOfYear()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withMonthOfYear_int_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.withMonthOfYear(1);
        assertEquals(t, OffsetDate.of(2007, 1, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_withMonthOfYear_int_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.withMonthOfYear(7);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withMonthOfYear_int_invalid() {
        TEST_2007_07_15_PONE.withMonthOfYear(13);
    }

    @Test(groups={"tck"})
    public void test_withMonthOfYear_int_adjustDay() {
        OffsetDate t = OffsetDate.of(2007, 12, 31, OFFSET_PONE).withMonthOfYear(11);
        OffsetDate expected = OffsetDate.of(2007, 11, 30, OFFSET_PONE);
        assertEquals(t, expected);
    }

    //-----------------------------------------------------------------------
    // withDayOfMonth()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withDayOfMonth_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.withDayOfMonth(1);
        assertEquals(t, OffsetDate.of(2007, 7, 1, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_withDayOfMonth_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.withDayOfMonth(15);
        assertEquals(t, OffsetDate.of(2007, 7, 15, OFFSET_PONE));
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfMonth_invalidForMonth() {
        OffsetDate.of(2007, 11, 30, OFFSET_PONE).withDayOfMonth(31);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfMonth_invalidAlways() {
        OffsetDate.of(2007, 11, 30, OFFSET_PONE).withDayOfMonth(32);
    }

    //-----------------------------------------------------------------------
    // withDayOfYear(int)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_withDayOfYear_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.withDayOfYear(33);
        assertEquals(t, OffsetDate.of(2007, 2, 2, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_withDayOfYear_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.withDayOfYear(31 + 28 + 31 + 30 + 31 + 30 + 15);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfYear_illegal() {
        TEST_2007_07_15_PONE.withDayOfYear(367);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_withDayOfYear_invalid() {
        TEST_2007_07_15_PONE.withDayOfYear(366);
    }

    //-----------------------------------------------------------------------
    // plus(Period)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_plus_Period() {
        Period period = Period.of(7, LocalDateTimeUnit.MONTHS);
        OffsetDate t = TEST_2007_07_15_PONE.plus(period);
        assertEquals(t, OffsetDate.of(2008, 2, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_plus_Period_zero() {
        OffsetDate t = TEST_2007_07_15_PONE.plus(Period.ZERO_DAYS);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    //-----------------------------------------------------------------------
    // plusYears()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_plusYears_long_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.plusYears(1);
        assertEquals(t, OffsetDate.of(2008, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_plusYears_long_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.plusYears(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_plusYears_long_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.plusYears(-1);
        assertEquals(t, OffsetDate.of(2006, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusYears_long_adjustDay() {
        OffsetDate t = OffsetDate.of(2008, 2, 29, OFFSET_PONE).plusYears(1);
        OffsetDate expected = OffsetDate.of(2009, 2, 28, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_plusYears_long_big() {
        long years = 20L + Year.MAX_YEAR;
        OffsetDate test = OffsetDate.of(-40, 6, 1, OFFSET_PONE).plusYears(years);
        assertEquals(test, OffsetDate.of((int) (-40L + years), 6, 1, OFFSET_PONE));
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_plusYears_long_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 1, 1, OFFSET_PONE).plusYears(1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_plusYears_long_invalidTooLargeMaxAddMax() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.plusYears(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_plusYears_long_invalidTooLargeMaxAddMin() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.plusYears(Long.MIN_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_plusYears_long_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE).plusYears(-1);
    }

    //-----------------------------------------------------------------------
    // plusMonths()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_plusMonths_long_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.plusMonths(1);
        assertEquals(t, OffsetDate.of(2007, 8, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_plusMonths_long_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.plusMonths(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_plusMonths_long_overYears() {
        OffsetDate t = TEST_2007_07_15_PONE.plusMonths(25);
        assertEquals(t, OffsetDate.of(2009, 8, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusMonths_long_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.plusMonths(-1);
        assertEquals(t, OffsetDate.of(2007, 6, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusMonths_long_negativeAcrossYear() {
        OffsetDate t = TEST_2007_07_15_PONE.plusMonths(-7);
        assertEquals(t, OffsetDate.of(2006, 12, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusMonths_long_negativeOverYears() {
        OffsetDate t = TEST_2007_07_15_PONE.plusMonths(-31);
        assertEquals(t, OffsetDate.of(2004, 12, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusMonths_long_adjustDayFromLeapYear() {
        OffsetDate t = OffsetDate.of(2008, 2, 29, OFFSET_PONE).plusMonths(12);
        OffsetDate expected = OffsetDate.of(2009, 2, 28, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_plusMonths_long_adjustDayFromMonthLength() {
        OffsetDate t = OffsetDate.of(2007, 3, 31, OFFSET_PONE).plusMonths(1);
        OffsetDate expected = OffsetDate.of(2007, 4, 30, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_plusMonths_long_big() {
        long months = 20L + Integer.MAX_VALUE;
        OffsetDate test = OffsetDate.of(-40, 6, 1, OFFSET_PONE).plusMonths(months);
        assertEquals(test, OffsetDate.of((int) (-40L + months / 12), 6 + (int) (months % 12), 1, OFFSET_PONE));
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_plusMonths_long_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE).plusMonths(1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_plusMonths_long_invalidTooLargeMaxAddMax() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.plusMonths(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_plusMonths_long_invalidTooLargeMaxAddMin() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.plusMonths(Long.MIN_VALUE);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_plusMonths_long_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE).plusMonths(-1);
    }

    //-----------------------------------------------------------------------
    // plusWeeks()
    //-----------------------------------------------------------------------
    @DataProvider(name="samplePlusWeeksSymmetry")
    Object[][] provider_samplePlusWeeksSymmetry() {
        return new Object[][] {
            {OffsetDate.of(-1, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(-1, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(0, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(0, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(0, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2007, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2007, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2008, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2008, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(2008, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2099, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2099, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2100, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2100, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 12, 31, OFFSET_PTWO)},
        };
    }
    
    @Test(dataProvider="samplePlusWeeksSymmetry", groups={"tck"})
    public void test_plusWeeks_symmetry(OffsetDate reference) {
        for (int weeks = 0; weeks < 365 * 8; weeks++) {
            OffsetDate t = reference.plusWeeks(weeks).plusWeeks(-weeks);
            assertEquals(t, reference);

            t = reference.plusWeeks(-weeks).plusWeeks(weeks);
            assertEquals(t, reference);
        }
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.plusWeeks(1);
        assertEquals(t, OffsetDate.of(2007, 7, 22, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_plusWeeks_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.plusWeeks(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_overMonths() {
        OffsetDate t = TEST_2007_07_15_PONE.plusWeeks(9);
        assertEquals(t, OffsetDate.of(2007, 9, 16, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_overYears() {
        OffsetDate t = OffsetDate.of(2006, 7, 16, OFFSET_PONE).plusWeeks(52);
        assertEquals(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_overLeapYears() {
        OffsetDate t = TEST_2007_07_15_PONE.plusYears(-1).plusWeeks(104);
        assertEquals(t, OffsetDate.of(2008, 7, 12, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.plusWeeks(-1);
        assertEquals(t, OffsetDate.of(2007, 7, 8, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_negativeAcrossYear() {
        OffsetDate t = TEST_2007_07_15_PONE.plusWeeks(-28);
        assertEquals(t, OffsetDate.of(2006, 12, 31, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_negativeOverYears() {
        OffsetDate t = TEST_2007_07_15_PONE.plusWeeks(-104);
        assertEquals(t, OffsetDate.of(2005, 7, 17, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_maximum() {
        OffsetDate t = OffsetDate.of(Year.MAX_YEAR, 12, 24, OFFSET_PONE).plusWeeks(1);
        OffsetDate expected = OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_plusWeeks_minimum() {
        OffsetDate t = OffsetDate.of(Year.MIN_YEAR, 1, 8, OFFSET_PONE).plusWeeks(-1);
        OffsetDate expected = OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_plusWeeks_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 25, OFFSET_PONE).plusWeeks(1);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_plusWeeks_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 7, OFFSET_PONE).plusWeeks(-1);
    }

    @Test(expectedExceptions={ArithmeticException.class}, groups={"tck"})
    public void test_plusWeeks_invalidMaxMinusMax() {
        OffsetDate.of(Year.MAX_YEAR, 12, 25, OFFSET_PONE).plusWeeks(Long.MAX_VALUE);
    }

    @Test(expectedExceptions={ArithmeticException.class}, groups={"tck"})
    public void test_plusWeeks_invalidMaxMinusMin() {
        OffsetDate.of(Year.MAX_YEAR, 12, 25, OFFSET_PONE).plusWeeks(Long.MIN_VALUE);
    }

    //-----------------------------------------------------------------------
    // plusDays()
    //-----------------------------------------------------------------------
    @DataProvider(name="samplePlusDaysSymmetry")
    Object[][] provider_samplePlusDaysSymmetry() {
        return new Object[][] {
            {OffsetDate.of(-1, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(-1, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(0, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(0, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(0, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2007, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2007, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2008, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2008, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(2008, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2099, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2099, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2100, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2100, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 12, 31, OFFSET_PTWO)},
        };
    }
    
    @Test(dataProvider="samplePlusDaysSymmetry", groups={"tck"})
    public void test_plusDays_symmetry(OffsetDate reference) {
        for (int days = 0; days < 365 * 8; days++) {
            OffsetDate t = reference.plusDays(days).plusDays(-days);
            assertEquals(t, reference);

            t = reference.plusDays(-days).plusDays(days);
            assertEquals(t, reference);
        }
    }

    @Test(groups={"tck"})
    public void test_plusDays_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.plusDays(1);
        assertEquals(t, OffsetDate.of(2007, 7, 16, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_plusDays_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.plusDays(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_plusDays_overMonths() {
        OffsetDate t = TEST_2007_07_15_PONE.plusDays(62);
        assertEquals(t, OffsetDate.of(2007, 9, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusDays_overYears() {
        OffsetDate t = OffsetDate.of(2006, 7, 14, OFFSET_PONE).plusDays(366);
        assertEquals(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_plusDays_overLeapYears() {
        OffsetDate t = TEST_2007_07_15_PONE.plusYears(-1).plusDays(365 + 366);
        assertEquals(t, OffsetDate.of(2008, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusDays_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.plusDays(-1);
        assertEquals(t, OffsetDate.of(2007, 7, 14, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusDays_negativeAcrossYear() {
        OffsetDate t = TEST_2007_07_15_PONE.plusDays(-196);
        assertEquals(t, OffsetDate.of(2006, 12, 31, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusDays_negativeOverYears() {
        OffsetDate t = TEST_2007_07_15_PONE.plusDays(-730);
        assertEquals(t, OffsetDate.of(2005, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_plusDays_maximum() {
        OffsetDate t = OffsetDate.of(Year.MAX_YEAR, 12, 30, OFFSET_PONE).plusDays(1);
        OffsetDate expected = OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_plusDays_minimum() {
        OffsetDate t = OffsetDate.of(Year.MIN_YEAR, 1, 2, OFFSET_PONE).plusDays(-1);
        OffsetDate expected = OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_plusDays_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE).plusDays(1);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_plusDays_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE).plusDays(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_plusDays_overflowTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE).plusDays(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_plusDays_overflowTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE).plusDays(Long.MIN_VALUE);
    }

    //-----------------------------------------------------------------------
    // minus(Period)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_minus_PeriodProvider() {
        Period period = Period.of(7, LocalDateTimeUnit.MONTHS);
        OffsetDate t = TEST_2007_07_15_PONE.minus(period);
        assertEquals(t, OffsetDate.of(2006, 12, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_minus_PeriodProvider_zero() {
        OffsetDate t = TEST_2007_07_15_PONE.minus(Period.ZERO_DAYS);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    //-----------------------------------------------------------------------
    // minusYears()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_minusYears_long_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.minusYears(1);
        assertEquals(t, OffsetDate.of(2006, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_minusYears_long_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.minusYears(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_minusYears_long_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.minusYears(-1);
        assertEquals(t, OffsetDate.of(2008, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusYears_long_adjustDay() {
        OffsetDate t = OffsetDate.of(2008, 2, 29, OFFSET_PONE).minusYears(1);
        OffsetDate expected = OffsetDate.of(2007, 2, 28, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_minusYears_long_big() {
        long years = 20L + Year.MAX_YEAR;
        OffsetDate test = OffsetDate.of(40, 6, 1, OFFSET_PONE).minusYears(years);
        assertEquals(test, OffsetDate.of((int) (40L - years), 6, 1, OFFSET_PONE));
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_minusYears_long_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 1, 1, OFFSET_PONE).minusYears(-1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_minusYears_long_invalidTooLargeMaxAddMax() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.minusYears(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_minusYears_long_invalidTooLargeMaxAddMin() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.minusYears(Long.MIN_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_minusYears_long_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE).minusYears(1);
    }

    //-----------------------------------------------------------------------
    // minusMonths()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_minusMonths_long_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.minusMonths(1);
        assertEquals(t, OffsetDate.of(2007, 6, 15, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_minusMonths_long_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.minusMonths(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_minusMonths_long_overYears() {
        OffsetDate t = TEST_2007_07_15_PONE.minusMonths(25);
        assertEquals(t, OffsetDate.of(2005, 6, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusMonths_long_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.minusMonths(-1);
        assertEquals(t, OffsetDate.of(2007, 8, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusMonths_long_negativeAcrossYear() {
        OffsetDate t = TEST_2007_07_15_PONE.minusMonths(-7);
        assertEquals(t, OffsetDate.of(2008, 2, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusMonths_long_negativeOverYears() {
        OffsetDate t = TEST_2007_07_15_PONE.minusMonths(-31);
        assertEquals(t, OffsetDate.of(2010, 2, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusMonths_long_adjustDayFromLeapYear() {
        OffsetDate t = OffsetDate.of(2008, 2, 29, OFFSET_PONE).minusMonths(12);
        OffsetDate expected = OffsetDate.of(2007, 2, 28, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_minusMonths_long_adjustDayFromMonthLength() {
        OffsetDate t = OffsetDate.of(2007, 3, 31, OFFSET_PONE).minusMonths(1);
        OffsetDate expected = OffsetDate.of(2007, 2, 28, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_minusMonths_long_big() {
        long months = 20L + Integer.MAX_VALUE;
        OffsetDate test = OffsetDate.of(40, 6, 1, OFFSET_PONE).minusMonths(months);
        assertEquals(test, OffsetDate.of((int) (40L - months / 12), 6 - (int) (months % 12), 1, OFFSET_PONE));
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_minusMonths_long_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE).minusMonths(-1);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_minusMonths_long_invalidTooLargeMaxAddMax() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.minusMonths(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_minusMonths_long_invalidTooLargeMaxAddMin() {
        OffsetDate test = OffsetDate.of(Year.MAX_YEAR, 12, 1, OFFSET_PONE);
        test.minusMonths(Long.MIN_VALUE);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_minusMonths_long_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE).minusMonths(1);
    }

    //-----------------------------------------------------------------------
    // minusWeeks()
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleMinusWeeksSymmetry")
    Object[][] provider_sampleMinusWeeksSymmetry() {
        return new Object[][] {
            {OffsetDate.of(-1, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(-1, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(0, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(0, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(0, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2007, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2007, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2008, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2008, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(2008, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2099, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2099, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2100, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2100, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 12, 31, OFFSET_PTWO)},
        };
    }
    
    @Test(dataProvider="sampleMinusWeeksSymmetry", groups={"tck"})
    public void test_minusWeeks_symmetry(OffsetDate reference) {
        for (int weeks = 0; weeks < 365 * 8; weeks++) {
            OffsetDate t = reference.minusWeeks(weeks).minusWeeks(-weeks);
            assertEquals(t, reference);

            t = reference.minusWeeks(-weeks).minusWeeks(weeks);
            assertEquals(t, reference);
        }
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.minusWeeks(1);
        assertEquals(t, OffsetDate.of(2007, 7, 8, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_minusWeeks_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.minusWeeks(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_overMonths() {
        OffsetDate t = TEST_2007_07_15_PONE.minusWeeks(9);
        assertEquals(t, OffsetDate.of(2007, 5, 13, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_overYears() {
        OffsetDate t = OffsetDate.of(2008, 7, 13, OFFSET_PONE).minusWeeks(52);
        assertEquals(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_overLeapYears() {
        OffsetDate t = TEST_2007_07_15_PONE.minusYears(-1).minusWeeks(104);
        assertEquals(t, OffsetDate.of(2006, 7, 18, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.minusWeeks(-1);
        assertEquals(t, OffsetDate.of(2007, 7, 22, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_negativeAcrossYear() {
        OffsetDate t = TEST_2007_07_15_PONE.minusWeeks(-28);
        assertEquals(t, OffsetDate.of(2008, 1, 27, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_negativeOverYears() {
        OffsetDate t = TEST_2007_07_15_PONE.minusWeeks(-104);
        assertEquals(t, OffsetDate.of(2009, 7, 12, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_maximum() {
        OffsetDate t = OffsetDate.of(Year.MAX_YEAR, 12, 24, OFFSET_PONE).minusWeeks(-1);
        OffsetDate expected = OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_minusWeeks_minimum() {
        OffsetDate t = OffsetDate.of(Year.MIN_YEAR, 1, 8, OFFSET_PONE).minusWeeks(1);
        OffsetDate expected = OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_minusWeeks_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 25, OFFSET_PONE).minusWeeks(-1);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_minusWeeks_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 7, OFFSET_PONE).minusWeeks(1);
    }

    @Test(expectedExceptions={ArithmeticException.class}, groups={"tck"})
    public void test_minusWeeks_invalidMaxMinusMax() {
        OffsetDate.of(Year.MAX_YEAR, 12, 25, OFFSET_PONE).minusWeeks(Long.MAX_VALUE);
    }

    @Test(expectedExceptions={ArithmeticException.class}, groups={"tck"})
    public void test_minusWeeks_invalidMaxMinusMin() {
        OffsetDate.of(Year.MAX_YEAR, 12, 25, OFFSET_PONE).minusWeeks(Long.MIN_VALUE);
    }

    //-----------------------------------------------------------------------
    // minusDays()
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleMinusDaysSymmetry")
    Object[][] provider_sampleMinusDaysSymmetry() {
        return new Object[][] {
            {OffsetDate.of(-1, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(-1, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(-1, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(0, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(0, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(0, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(0, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2007, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2007, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2007, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2008, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2008, 2, 29, OFFSET_PTWO)},
            {OffsetDate.of(2008, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2008, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2099, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2099, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2099, 12, 31, OFFSET_PTWO)},
            {OffsetDate.of(2100, 1, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 2, 28, OFFSET_PTWO)},
            {OffsetDate.of(2100, 3, 1, OFFSET_PONE)},
            {OffsetDate.of(2100, 12, 31, OFFSET_PTWO)},
        };
    }
    
    @Test(dataProvider="sampleMinusDaysSymmetry", groups={"tck"})
    public void test_minusDays_symmetry(OffsetDate reference) {
        for (int days = 0; days < 365 * 8; days++) {
            OffsetDate t = reference.minusDays(days).minusDays(-days);
            assertEquals(t, reference);

            t = reference.minusDays(-days).minusDays(days);
            assertEquals(t, reference);
        }
    }

    @Test(groups={"tck"})
    public void test_minusDays_normal() {
        OffsetDate t = TEST_2007_07_15_PONE.minusDays(1);
        assertEquals(t, OffsetDate.of(2007, 7, 14, OFFSET_PONE));
    }

    @Test(groups={"implementation"})
    public void test_minusDays_noChange() {
        OffsetDate t = TEST_2007_07_15_PONE.minusDays(0);
        assertSame(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_minusDays_overMonths() {
        OffsetDate t = TEST_2007_07_15_PONE.minusDays(62);
        assertEquals(t, OffsetDate.of(2007, 5, 14, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusDays_overYears() {
        OffsetDate t = OffsetDate.of(2008, 7, 16, OFFSET_PONE).minusDays(367);
        assertEquals(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_minusDays_overLeapYears() {
        OffsetDate t = TEST_2007_07_15_PONE.plusYears(2).minusDays(365 + 366);
        assertEquals(t, TEST_2007_07_15_PONE);
    }

    @Test(groups={"tck"})
    public void test_minusDays_negative() {
        OffsetDate t = TEST_2007_07_15_PONE.minusDays(-1);
        assertEquals(t, OffsetDate.of(2007, 7, 16, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusDays_negativeAcrossYear() {
        OffsetDate t = TEST_2007_07_15_PONE.minusDays(-169);
        assertEquals(t, OffsetDate.of(2007, 12, 31, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusDays_negativeOverYears() {
        OffsetDate t = TEST_2007_07_15_PONE.minusDays(-731);
        assertEquals(t, OffsetDate.of(2009, 7, 15, OFFSET_PONE));
    }

    @Test(groups={"tck"})
    public void test_minusDays_maximum() {
        OffsetDate t = OffsetDate.of(Year.MAX_YEAR, 12, 30, OFFSET_PONE).minusDays(-1);
        OffsetDate expected = OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(groups={"tck"})
    public void test_minusDays_minimum() {
        OffsetDate t = OffsetDate.of(Year.MIN_YEAR, 1, 2, OFFSET_PONE).minusDays(1);
        OffsetDate expected = OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_minusDays_invalidTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE).minusDays(-1);
    }

    @Test(expectedExceptions={CalendricalException.class}, groups={"tck"})
    public void test_minusDays_invalidTooSmall() {
        OffsetDate.of(Year.MIN_YEAR, 1, 1, OFFSET_PONE).minusDays(1);
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_minusDays_overflowTooLarge() {
        OffsetDate.of(Year.MAX_YEAR, 12, 31, OFFSET_PONE).minusDays(Long.MIN_VALUE);
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_minusDays_overflowTooSmall() {
        OffsetDate.of(Yea
