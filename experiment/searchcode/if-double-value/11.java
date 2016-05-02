/*
<<<<<<< HEAD
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package com.granule.json.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.granule.json.JSONArray;
import com.granule.json.JSONObject;
import com.granule.json.OrderedJSONObject;
import com.granule.json.JSONString;

/**
 * Class to handle serialization of a JSON object to a JSON string.
 */
public class Serializer {

    /**
     * The writer to use when writing this JSON object.
     */
    private Writer writer;

    /**
     * Create a serializer on the specified output stream writer.
     */
    public Serializer(Writer writer) {
        super();

        this.writer = writer;
    }

    /**
     * Method to flush the current writer.
     * @throws IOException Thrown if an error occurs during writer flush.
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Method to close the current writer.
     * @throws IOException Thrown if an error occurs during writer close.
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Method to write a raw string to the writer.
     * @param s The String to write.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeRawString(String s) throws IOException {
        writer.write(s);

        return this;
    }

    /**
     * Method to write the text string 'null' to the output stream (null JSON object).
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeNull() throws IOException {
        writeRawString("null");
        return this;
    }

    /**
     * Method to write a number to the current writer.
     * @param value The number to write to the JSON output string.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeNumber(Number value) throws IOException {
        if (null == value) return writeNull();

        if (value instanceof Float) {
            if (((Float)value).isNaN()) return writeNull();
            if (Float.NEGATIVE_INFINITY == value.floatValue()) return writeNull();
            if (Float.POSITIVE_INFINITY == value.floatValue()) return writeNull();
        }

        if (value instanceof Double) {
            if (((Double)value).isNaN()) return writeNull();
            if (Double.NEGATIVE_INFINITY == value.doubleValue()) return writeNull();
            if (Double.POSITIVE_INFINITY == value.doubleValue()) return writeNull();
        }

        writeRawString(value.toString());

        return this;
    }

    /**
     * Method to write a boolean value to the output stream.
     * @param value The Boolean object to write out as a JSON boolean.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeBoolean(Boolean value) throws IOException {
        if (null == value) return writeNull();

        writeRawString(value.toString());

        return this;
    }

    /**
     * Method to generate a string with a particular width.  Alignment is done using zeroes if it does not meet the width requirements.
     * @param s The string to write
     * @param len The minimum length it should be, and to align with zeroes if length is smaller.
     * @return A string properly aligned/correct width.
     */
    private String rightAlignedZero(String s, int len) {
        if (len == s.length()) return s;

        StringBuffer sb = new StringBuffer(s);

        while (sb.length() < len) {
            sb.insert(0, '0');
        }

        return sb.toString();
    }

    /**
     * Method to write a String out to the writer, encoding special characters and unicode characters properly.
     * @param value The string to write out.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeString(String value) throws IOException {
        if (null == value) return writeNull();

        writer.write('"');

        char[] chars = value.toCharArray();

        for (int i=0; i<chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case  '"': writer.write("\\\""); break;
                case '\\': writer.write("\\\\"); break;
                case    0: writer.write("\\0"); break;
                case '\b': writer.write("\\b"); break;
                case '\t': writer.write("\\t"); break;
                case '\n': writer.write("\\n"); break;
                case '\f': writer.write("\\f"); break;
                case '\r': writer.write("\\r"); break;
                case '/': writer.write("\\/"); break;
                default:
                    if ((c >= 32) && (c <= 126)) {
                        writer.write(c);
                    } else {
                        writer.write("\\u");
                        writer.write(rightAlignedZero(Integer.toHexString(c),4));
                    }
            }
        }

        writer.write('"');

        return this;
    }

    /**
     * Method to write out a generic JSON type.
     * @param object The JSON compatible object to serialize.
     * @throws IOException Thrown if an error occurs during write, or if a nonJSON compatible Java object is passed..
     */
    private Serializer write(Object object) throws IOException {
        if (null == object) return writeNull();
        
        // Serialize the various types!
        Class clazz = object.getClass();
        if (Number.class.isAssignableFrom(clazz)) return writeNumber((Number) object);
        if (Boolean.class.isAssignableFrom(clazz)) return writeBoolean((Boolean) object);
        if (JSONObject.class.isAssignableFrom(clazz)) return writeObject((JSONObject) object);
        if (JSONArray.class.isAssignableFrom(clazz)) return writeArray((JSONArray) object);
        if (JSONString.class.isAssignableFrom(clazz)) return writeRawString(((JSONString) object).toJSONString());
        if (String.class.isAssignableFrom(clazz)) return writeString((String) object);

        throw new IOException("Attempting to serialize unserializable object: '" + object + "'");
    }

    /**
     * Method to write a complete JSON object to the stream.
     * @param object The JSON object to write out.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeObject(JSONObject object) throws IOException {
        if (null == object) return writeNull();

        // write header
        writeRawString("{");
        indentPush();

        Iterator iter = null;
        if (object instanceof OrderedJSONObject) {
            iter = ((OrderedJSONObject)object).getOrder();
        } else {
            List propertyNames = getPropertyNames(object);
            iter = propertyNames.iterator();
        }

        while ( iter.hasNext() ) {
            Object key = iter.next();
            if (!(key instanceof String)) throw new IOException("attempting to serialize object with an invalid property name: '" + key + "'" );

            Object value = object.get(key);
            if (!JSONObject.isValidObject(value)) throw new IOException("attempting to serialize object with an invalid property value: '" + value + "'");

            newLine();
            indent();
            writeString((String)key);
            writeRawString(":");
            space();
            write(value);

            if (iter.hasNext()) writeRawString(",");
        }

        // write trailer
        indentPop();
        newLine();
        indent();
        writeRawString("}");

        return this;
    }

    /**
     * Method to write a JSON array out to the stream.
     * @param value The JSON array to write out.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeArray(JSONArray value) throws IOException {
        if (null == value) return writeNull();

        // write header
        writeRawString("[");
        indentPush();

        for (Iterator iter=value.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (!JSONObject.isValidObject(element)) throw new IOException("attempting to serialize array with an invalid element: '" + value + "'");

            newLine();
            indent();
            write(element);

            if (iter.hasNext()) writeRawString(",");
        }

        // write trailer
        indentPop();
        newLine();
        indent();
        writeRawString("]");

        return this;
    }

    //---------------------------------------------------------------
    // pretty printing overridables
    //---------------------------------------------------------------

    /**
     * Method to write a space to the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void space() throws IOException {
    }

    /**
     * Method to write a newline to the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void newLine() throws IOException {
    }

    /**
     * Method to write an indent to the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void indent() throws IOException {
    }

    /**
     * Method to increase the indent depth of the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void indentPush() {
    }

    /**
     * Method to reduce the indent depth of the output writer.
     */
    public void indentPop() {
    }

    /**
     * Method to get a list of all the property names stored in a map.
     */
    public List getPropertyNames(Map map) {
        return new ArrayList(map.keySet());
    }

=======
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.primitives;

import static java.lang.Double.NaN;
import static org.truth0.Truth.ASSERT;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.testing.Helpers;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Unit test for {@link Doubles}.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(emulated = true)
@SuppressWarnings("cast") // redundant casts are intentional and harmless
public class DoublesTest extends TestCase {
  private static final double[] EMPTY = {};
  private static final double[] ARRAY1 = {(double) 1};
  private static final double[] ARRAY234
      = {(double) 2, (double) 3, (double) 4};

  private static final double LEAST = Double.NEGATIVE_INFINITY;
  private static final double GREATEST = Double.POSITIVE_INFINITY;

  private static final double[] NUMBERS = new double[] {
      LEAST, -Double.MAX_VALUE, -1.0, -0.5, -0.1, -0.0, 0.0, 0.1, 0.5, 1.0,
      Double.MAX_VALUE, GREATEST, Double.MIN_NORMAL, -Double.MIN_NORMAL,
      Double.MIN_VALUE, -Double.MIN_VALUE, Integer.MIN_VALUE,
      Integer.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE
  };

  private static final double[] VALUES
      = Doubles.concat(NUMBERS, new double[] {NaN});

  public void testHashCode() {
    for (double value : VALUES) {
      assertEquals(((Double) value).hashCode(), Doubles.hashCode(value));
    }
  }

  public void testIsFinite() {
    for (double value : NUMBERS) {
      assertEquals(!(Double.isNaN(value) || Double.isInfinite(value)), Doubles.isFinite(value));
    }
  }

  public void testCompare() {
    for (double x : VALUES) {
      for (double y : VALUES) {
        // note: spec requires only that the sign is the same
        assertEquals(x + ", " + y,
                     Double.valueOf(x).compareTo(y),
                     Doubles.compare(x, y));
      }
    }
  }

  public void testContains() {
    assertFalse(Doubles.contains(EMPTY, (double) 1));
    assertFalse(Doubles.contains(ARRAY1, (double) 2));
    assertFalse(Doubles.contains(ARRAY234, (double) 1));
    assertTrue(Doubles.contains(new double[] {(double) -1}, (double) -1));
    assertTrue(Doubles.contains(ARRAY234, (double) 2));
    assertTrue(Doubles.contains(ARRAY234, (double) 3));
    assertTrue(Doubles.contains(ARRAY234, (double) 4));

    for (double value : NUMBERS) {
      assertTrue("" + value,
          Doubles.contains(new double[] {5.0, value}, value));
    }
    assertFalse(Doubles.contains(new double[] {5.0, NaN}, NaN));
  }

  public void testIndexOf() {
    assertEquals(-1, Doubles.indexOf(EMPTY, (double) 1));
    assertEquals(-1, Doubles.indexOf(ARRAY1, (double) 2));
    assertEquals(-1, Doubles.indexOf(ARRAY234, (double) 1));
    assertEquals(0, Doubles.indexOf(
        new double[] {(double) -1}, (double) -1));
    assertEquals(0, Doubles.indexOf(ARRAY234, (double) 2));
    assertEquals(1, Doubles.indexOf(ARRAY234, (double) 3));
    assertEquals(2, Doubles.indexOf(ARRAY234, (double) 4));
    assertEquals(1, Doubles.indexOf(
        new double[] { (double) 2, (double) 3, (double) 2, (double) 3 },
        (double) 3));

    for (double value : NUMBERS) {
      assertEquals("" + value,
          1, Doubles.indexOf(new double[] {5.0, value}, value));
    }
    assertEquals(-1, Doubles.indexOf(new double[] {5.0, NaN}, NaN));
  }

  public void testIndexOf_arrayTarget() {
    assertEquals(0, Doubles.indexOf(EMPTY, EMPTY));
    assertEquals(0, Doubles.indexOf(ARRAY234, EMPTY));
    assertEquals(-1, Doubles.indexOf(EMPTY, ARRAY234));
    assertEquals(-1, Doubles.indexOf(ARRAY234, ARRAY1));
    assertEquals(-1, Doubles.indexOf(ARRAY1, ARRAY234));
    assertEquals(0, Doubles.indexOf(ARRAY1, ARRAY1));
    assertEquals(0, Doubles.indexOf(ARRAY234, ARRAY234));
    assertEquals(0, Doubles.indexOf(
        ARRAY234, new double[] { (double) 2, (double) 3 }));
    assertEquals(1, Doubles.indexOf(
        ARRAY234, new double[] { (double) 3, (double) 4 }));
    assertEquals(1, Doubles.indexOf(ARRAY234, new double[] { (double) 3 }));
    assertEquals(2, Doubles.indexOf(ARRAY234, new double[] { (double) 4 }));
    assertEquals(1, Doubles.indexOf(new double[] { (double) 2, (double) 3,
        (double) 3, (double) 3, (double) 3 },
        new double[] { (double) 3 }
    ));
    assertEquals(2, Doubles.indexOf(
        new double[] { (double) 2, (double) 3, (double) 2,
            (double) 3, (double) 4, (double) 2, (double) 3},
        new double[] { (double) 2, (double) 3, (double) 4}
    ));
    assertEquals(1, Doubles.indexOf(
        new double[] { (double) 2, (double) 2, (double) 3,
            (double) 4, (double) 2, (double) 3, (double) 4},
        new double[] { (double) 2, (double) 3, (double) 4}
    ));
    assertEquals(-1, Doubles.indexOf(
        new double[] { (double) 4, (double) 3, (double) 2},
        new double[] { (double) 2, (double) 3, (double) 4}
    ));

    for (double value : NUMBERS) {
      assertEquals("" + value, 1, Doubles.indexOf(
          new double[] {5.0, value, value, 5.0}, new double[] {value, value}));
    }
    assertEquals(-1, Doubles.indexOf(
        new double[] {5.0, NaN, NaN, 5.0}, new double[] {NaN, NaN}));
  }

  public void testLastIndexOf() {
    assertEquals(-1, Doubles.lastIndexOf(EMPTY, (double) 1));
    assertEquals(-1, Doubles.lastIndexOf(ARRAY1, (double) 2));
    assertEquals(-1, Doubles.lastIndexOf(ARRAY234, (double) 1));
    assertEquals(0, Doubles.lastIndexOf(
        new double[] {(double) -1}, (double) -1));
    assertEquals(0, Doubles.lastIndexOf(ARRAY234, (double) 2));
    assertEquals(1, Doubles.lastIndexOf(ARRAY234, (double) 3));
    assertEquals(2, Doubles.lastIndexOf(ARRAY234, (double) 4));
    assertEquals(3, Doubles.lastIndexOf(
        new double[] { (double) 2, (double) 3, (double) 2, (double) 3 },
        (double) 3));

    for (double value : NUMBERS) {
      assertEquals("" + value,
          0, Doubles.lastIndexOf(new double[] {value, 5.0}, value));
    }
    assertEquals(-1, Doubles.lastIndexOf(new double[] {NaN, 5.0}, NaN));
  }

  public void testMax_noArgs() {
    try {
      Doubles.max();
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testMax() {
    assertEquals(LEAST, Doubles.max(LEAST));
    assertEquals(GREATEST, Doubles.max(GREATEST));
    assertEquals((double) 9, Doubles.max(
        (double) 8, (double) 6, (double) 7,
        (double) 5, (double) 3, (double) 0, (double) 9));

    assertEquals(0.0, Doubles.max(-0.0, 0.0));
    assertEquals(0.0, Doubles.max(0.0, -0.0));
    assertEquals(GREATEST, Doubles.max(NUMBERS));
    assertTrue(Double.isNaN(Doubles.max(VALUES)));
  }

  public void testMin_noArgs() {
    try {
      Doubles.min();
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testMin() {
    assertEquals(LEAST, Doubles.min(LEAST));
    assertEquals(GREATEST, Doubles.min(GREATEST));
    assertEquals((double) 0, Doubles.min(
        (double) 8, (double) 6, (double) 7,
        (double) 5, (double) 3, (double) 0, (double) 9));

    assertEquals(-0.0, Doubles.min(-0.0, 0.0));
    assertEquals(-0.0, Doubles.min(0.0, -0.0));
    assertEquals(LEAST, Doubles.min(NUMBERS));
    assertTrue(Double.isNaN(Doubles.min(VALUES)));
  }

  public void testConcat() {
    assertTrue(Arrays.equals(EMPTY, Doubles.concat()));
    assertTrue(Arrays.equals(EMPTY, Doubles.concat(EMPTY)));
    assertTrue(Arrays.equals(EMPTY, Doubles.concat(EMPTY, EMPTY, EMPTY)));
    assertTrue(Arrays.equals(ARRAY1, Doubles.concat(ARRAY1)));
    assertNotSame(ARRAY1, Doubles.concat(ARRAY1));
    assertTrue(Arrays.equals(ARRAY1, Doubles.concat(EMPTY, ARRAY1, EMPTY)));
    assertTrue(Arrays.equals(
        new double[] {(double) 1, (double) 1, (double) 1},
        Doubles.concat(ARRAY1, ARRAY1, ARRAY1)));
    assertTrue(Arrays.equals(
        new double[] {(double) 1, (double) 2, (double) 3, (double) 4},
        Doubles.concat(ARRAY1, ARRAY234)));
  }

  public void testEnsureCapacity() {
    assertSame(EMPTY, Doubles.ensureCapacity(EMPTY, 0, 1));
    assertSame(ARRAY1, Doubles.ensureCapacity(ARRAY1, 0, 1));
    assertSame(ARRAY1, Doubles.ensureCapacity(ARRAY1, 1, 1));
    assertTrue(Arrays.equals(
        new double[] {(double) 1, (double) 0, (double) 0},
        Doubles.ensureCapacity(ARRAY1, 2, 1)));
  }

  public void testEnsureCapacity_fail() {
    try {
      Doubles.ensureCapacity(ARRAY1, -1, 1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      // notice that this should even fail when no growth was needed
      Doubles.ensureCapacity(ARRAY1, 1, -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @GwtIncompatible("Double.toString returns different value in GWT.")
  public void testJoin() {
    assertEquals("", Doubles.join(",", EMPTY));
    assertEquals("1.0", Doubles.join(",", ARRAY1));
    assertEquals("1.0,2.0", Doubles.join(",", (double) 1, (double) 2));
    assertEquals("1.02.03.0",
        Doubles.join("", (double) 1, (double) 2, (double) 3));
  }

  public void testJoinNonTrivialDoubles() {
    assertEquals("", Doubles.join(",", EMPTY));
    assertEquals("1.2", Doubles.join(",", 1.2));
    assertEquals("1.3,2.4", Doubles.join(",", 1.3, 2.4));
    assertEquals("1.42.53.6", Doubles.join("", 1.4, 2.5, 3.6));
  }

  public void testLexicographicalComparator() {
    List<double[]> ordered = Arrays.asList(
        new double[] {},
        new double[] {LEAST},
        new double[] {LEAST, LEAST},
        new double[] {LEAST, (double) 1},
        new double[] {(double) 1},
        new double[] {(double) 1, LEAST},
        new double[] {GREATEST, Double.MAX_VALUE},
        new double[] {GREATEST, GREATEST},
        new double[] {GREATEST, GREATEST, GREATEST});

    Comparator<double[]> comparator = Doubles.lexicographicalComparator();
    Helpers.testComparator(comparator, ordered);
  }

  @GwtIncompatible("SerializableTester")
  public void testLexicographicalComparatorSerializable() {
    Comparator<double[]> comparator = Doubles.lexicographicalComparator();
    assertSame(comparator, SerializableTester.reserialize(comparator));
  }

  public void testToArray() {
    // need explicit type parameter to avoid javac warning!?
    List<Double> none = Arrays.<Double>asList();
    assertTrue(Arrays.equals(EMPTY, Doubles.toArray(none)));

    List<Double> one = Arrays.asList((double) 1);
    assertTrue(Arrays.equals(ARRAY1, Doubles.toArray(one)));

    double[] array = {(double) 0, (double) 1, Math.PI};

    List<Double> three = Arrays.asList((double) 0, (double) 1, Math.PI);
    assertTrue(Arrays.equals(array, Doubles.toArray(three)));

    assertTrue(Arrays.equals(array, Doubles.toArray(Doubles.asList(array))));
  }

  public void testToArray_threadSafe() {
    for (int delta : new int[] { +1, 0, -1 }) {
      for (int i = 0; i < VALUES.length; i++) {
        List<Double> list = Doubles.asList(VALUES).subList(0, i);
        Collection<Double> misleadingSize =
            Helpers.misleadingSizeCollection(delta);
        misleadingSize.addAll(list);
        double[] arr = Doubles.toArray(misleadingSize);
        assertEquals(i, arr.length);
        for (int j = 0; j < i; j++) {
          assertEquals(VALUES[j], arr[j]);
        }
      }
    }
  }

  public void testToArray_withNull() {
    List<Double> list = Arrays.asList((double) 0, (double) 1, null);
    try {
      Doubles.toArray(list);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testToArray_withConversion() {
    double[] array = {(double) 0, (double) 1, (double) 2};

    List<Byte> bytes = Arrays.asList((byte) 0, (byte) 1, (byte) 2);
    List<Short> shorts = Arrays.asList((short) 0, (short) 1, (short) 2);
    List<Integer> ints = Arrays.asList(0, 1, 2);
    List<Float> floats = Arrays.asList((float) 0, (float) 1, (float) 2);
    List<Long> longs = Arrays.asList((long) 0, (long) 1, (long) 2);
    List<Double> doubles = Arrays.asList((double) 0, (double) 1, (double) 2);

    assertTrue(Arrays.equals(array, Doubles.toArray(bytes)));
    assertTrue(Arrays.equals(array, Doubles.toArray(shorts)));
    assertTrue(Arrays.equals(array, Doubles.toArray(ints)));
    assertTrue(Arrays.equals(array, Doubles.toArray(floats)));
    assertTrue(Arrays.equals(array, Doubles.toArray(longs)));
    assertTrue(Arrays.equals(array, Doubles.toArray(doubles)));
  }

  public void testAsList_isAView() {
    double[] array = {(double) 0, (double) 1};
    List<Double> list = Doubles.asList(array);
    list.set(0, (double) 2);
    assertTrue(Arrays.equals(new double[] {(double) 2, (double) 1}, array));
    array[1] = (double) 3;
    ASSERT.that(list).has().allOf((double) 2, (double) 3).inOrder();
  }

  public void testAsList_toArray_roundTrip() {
    double[] array = { (double) 0, (double) 1, (double) 2 };
    List<Double> list = Doubles.asList(array);
    double[] newArray = Doubles.toArray(list);

    // Make sure it returned a copy
    list.set(0, (double) 4);
    assertTrue(Arrays.equals(
        new double[] { (double) 0, (double) 1, (double) 2 }, newArray));
    newArray[1] = (double) 5;
    assertEquals((double) 1, (double) list.get(1));
  }

  // This test stems from a real bug found by andrewk
  public void testAsList_subList_toArray_roundTrip() {
    double[] array = { (double) 0, (double) 1, (double) 2, (double) 3 };
    List<Double> list = Doubles.asList(array);
    assertTrue(Arrays.equals(new double[] { (double) 1, (double) 2 },
        Doubles.toArray(list.subList(1, 3))));
    assertTrue(Arrays.equals(new double[] {},
        Doubles.toArray(list.subList(2, 2))));
  }

  public void testAsListEmpty() {
    assertSame(Collections.emptyList(), Doubles.asList(EMPTY));
  }

  /**
   * A reference implementation for {@code tryParse} that just catches the exception from
   * {@link Double#valueOf}.
   */
  private static Double referenceTryParse(String input) {
    if (input.trim().length() < input.length()) {
      return null;
    }
    try {
      return Double.valueOf(input);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @GwtIncompatible("Doubles.tryParse")
  private static void checkTryParse(String input) {
    Double expected = referenceTryParse(input);
    assertEquals(expected, Doubles.tryParse(input));
    assertEquals(expected != null,
        Doubles.FLOATING_POINT_PATTERN.matcher(input).matches());
  }

  @GwtIncompatible("Doubles.tryParse")
  private static void checkTryParse(double expected, String input) {
    assertEquals(Double.valueOf(expected), Doubles.tryParse(input));
    assertTrue(Doubles.FLOATING_POINT_PATTERN.matcher(input).matches());
  }

  @GwtIncompatible("Doubles.tryParse")
  public void testTryParseHex() {
    for (String signChar : ImmutableList.of("", "+", "-")) {
      for (String hexPrefix : ImmutableList.of("0x", "0X")) {
        for (String iPart : ImmutableList.of("", "0", "1", "F", "f", "c4", "CE")) {
          for (String fPart : ImmutableList.of("", ".", ".F", ".52", ".a")) {
            for (String expMarker : ImmutableList.of("p", "P")) {
              for (String exponent : ImmutableList.of("0", "-5", "+20", "52")) {
                for (String typePart : ImmutableList.of("", "D", "F", "d", "f")) {
                  checkTryParse(
                      signChar + hexPrefix + iPart + fPart + expMarker + exponent + typePart);
                }
              }
            }
          }
        }
      }
    }
  }

  @GwtIncompatible("Doubles.tryParse")
  public void testTryParseAllCodePoints() {
    // Exercise non-ASCII digit test cases and the like.
    char[] tmp = new char[2];
    for (int i = Character.MIN_CODE_POINT; i < Character.MAX_CODE_POINT; i++) {
      Character.toChars(i, tmp, 0);
      checkTryParse(String.copyValueOf(tmp, 0, Character.charCount(i)));
    }
  }

  @GwtIncompatible("Doubles.tryParse")
  public void testTryParseOfToStringIsOriginal() {
    for (double d : NUMBERS) {
      checkTryParse(d, Double.toString(d));
    }
  }

  @GwtIncompatible("Doubles.tryParse")
  public void testTryParseOfToHexStringIsOriginal() {
    for (double d : NUMBERS) {
      checkTryParse(d, Double.toHexString(d));
    }
  }

  @GwtIncompatible("Doubles.tryParse")
  public void testTryParseNaN() {
    checkTryParse("NaN");
    checkTryParse("+NaN");
    checkTryParse("-NaN");
  }

  @GwtIncompatible("Doubles.tryParse")
  public void testTryParseInfinity() {
    checkTryParse(Double.POSITIVE_INFINITY, "Infinity");
    checkTryParse(Double.POSITIVE_INFINITY, "+Infinity");
    checkTryParse(Double.NEGATIVE_INFINITY, "-Infinity");
  }

  private static final String[] BAD_TRY_PARSE_INPUTS =
    { "", "+-", "+-0", " 5", "32 ", " 55 ", "infinity", "POSITIVE_INFINITY", "0x9A", "0x9A.bE-5",
      ".", ".e5", "NaNd", "InfinityF" };

  @GwtIncompatible("Doubles.tryParse")
  public void testTryParseFailures() {
    for (String badInput : BAD_TRY_PARSE_INPUTS) {
      assertFalse(Doubles.FLOATING_POINT_PATTERN.matcher(badInput).matches());
      assertEquals(referenceTryParse(badInput), Doubles.tryParse(badInput));
      assertNull(Doubles.tryParse(badInput));
    }
  }

  @GwtIncompatible("NullPointerTester")
  public void testNulls() {
    new NullPointerTester().testAllPublicStaticMethods(Doubles.class);
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

