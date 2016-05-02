<<<<<<< HEAD
/*
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

package org.apache.wink.json4j.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.JSONString;
import org.apache.wink.json4j.OrderedJSONObject;

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
package org.apache.lucene.util.collections;

import org.junit.Test;

import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.collections.DoubleIterator;
import org.apache.lucene.util.collections.IntIterator;
import org.apache.lucene.util.collections.IntToDoubleMap;

import java.util.HashSet;
import java.util.Random;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class IntToDoubleMapTest extends LuceneTestCase {
  private static void assertGround(double value) {
    assertEquals(IntToDoubleMap.GROUND, value, Double.MAX_VALUE);
  }
  
  @Test
  public void test0() {
    IntToDoubleMap map = new IntToDoubleMap();

    assertGround(map.get(0));
    
    for (int i = 0; i < 100; ++i) {
      int value = 100 + i;
      assertFalse(map.containsValue(value));
      map.put(i, value);
      assertTrue(map.containsValue(value));
      assertNotNull(map.get(i));
    }

    assertEquals(100, map.size());
    for (int i = 0; i < 100; ++i) {
      assertTrue(map.containsKey(i));
      assertEquals(100 + i, map.get(i), Double.MAX_VALUE);

    }

    for (int i = 10; i < 90; ++i) {
      map.remove(i);
      assertGround(map.get(i));
    }

    assertEquals(20, map.size());
    for (int i = 0; i < 100; ++i) {
      assertEquals(map.containsKey(i), !(i >= 10 && i < 90));
    }

    for (int i = 5; i < 85; ++i) {
      map.put(i, Integer.valueOf(5 + i));
    }
    assertEquals(95, map.size());
    for (int i = 0; i < 100; ++i) {
      assertEquals(map.containsKey(i), !(i >= 85 && i < 90));
    }
    for (int i = 0; i < 5; ++i) {
      assertEquals(map.get(i), (100 + i), Double.MAX_VALUE);
    }
    for (int i = 5; i < 85; ++i) {
      assertEquals(map.get(i), (5 + i), Double.MAX_VALUE);
    }
    for (int i = 90; i < 100; ++i) {
      assertEquals(map.get(i), (100 + i), Double.MAX_VALUE);
    }
  }

  @Test
  public void test1() {
    IntToDoubleMap map = new IntToDoubleMap();

    for (int i = 0; i < 100; ++i) {
      map.put(i, Integer.valueOf(100 + i));
    }
    
    HashSet<Double> set = new HashSet<Double>();
    
    for (DoubleIterator iterator = map.iterator(); iterator.hasNext();) {
      set.add(iterator.next());
    }

    assertEquals(set.size(), map.size());
    for (int i = 0; i < 100; ++i) {
      assertTrue(set.contains(Double.valueOf(100+i)));
    }

    set.clear();
    for (DoubleIterator iterator = map.iterator(); iterator.hasNext();) {
      double d = iterator.next();
      if (d % 2 == 1) {
        iterator.remove();
        continue;
      }
      set.add(d);
    }
    assertEquals(set.size(), map.size());
    for (int i = 0; i < 100; i+=2) {
      assertTrue(set.contains(Double.valueOf(100+i)));
    }
  }
  
  @Test
  public void test2() {
    IntToDoubleMap map = new IntToDoubleMap();

    assertTrue(map.isEmpty());
    assertGround(map.get(0));
    for (int i = 0; i < 128; ++i) {
      int value = i * 4096;
      assertFalse(map.containsValue(value));
      map.put(i, value);
      assertTrue(map.containsValue(value));
      assertNotNull(map.get(i));
      assertFalse(map.isEmpty());
    }

    assertEquals(128, map.size());
    for (int i = 0; i < 128; ++i) {
      assertTrue(map.containsKey(i));
      assertEquals(i * 4096, map.get(i), Double.MAX_VALUE);
    }
    
    for (int i = 0 ; i < 200; i+=2) {
      map.remove(i);
    }
    assertEquals(64, map.size());
    for (int i = 1; i < 128; i+=2) {
      assertTrue(map.containsKey(i));
      assertEquals(i * 4096, map.get(i), Double.MAX_VALUE);
      map.remove(i);
    }
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void test3() {
    IntToDoubleMap map = new IntToDoubleMap();
    int length = 100;
    for (int i = 0; i < length; ++i) {
      map.put(i*64, 100 + i);
    }
    HashSet<Integer> keySet = new HashSet<Integer>();
    for (IntIterator iit = map.keyIterator(); iit.hasNext(); ) {
      keySet.add(iit.next());
    }
    assertEquals(length, keySet.size());
    for (int i = 0; i < length; ++i) {
      assertTrue(keySet.contains(i * 64));
    }
    
    HashSet<Double> valueSet = new HashSet<Double>();
    for (DoubleIterator iit = map.iterator(); iit.hasNext(); ) {
      valueSet.add(iit.next());
    }
    assertEquals(length, valueSet.size());
    double[] array = map.toArray();
    assertEquals(length, array.length);
    for (double value: array) {
      assertTrue(valueSet.contains(value));
    }
    
    double[] array2 = new double[80];
    array2 = map.toArray(array2);
    assertEquals(length, array2.length);
    for (double value: array2) {
      assertTrue(valueSet.contains(value));
    }
    
    double[] array3 = new double[120];
    array3 = map.toArray(array3);
    for (int i = 0 ;i < length; ++i) {
      assertTrue(valueSet.contains(array3[i]));
    }
    
    for (int i = 0; i < length; ++i) {
      assertTrue(map.containsValue(i + 100));
      assertTrue(map.containsKey(i*64));
    }
    
    for (IntIterator iit = map.keyIterator(); iit.hasNext(); ) {
      iit.next();
      iit.remove();
    }
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
    
  }

  // now with random data.. and lots of it
  @Test
  public void test4() {
    IntToDoubleMap map = new IntToDoubleMap();
    int length = ArrayHashMapTest.RANDOM_TEST_NUM_ITERATIONS;
    // for a repeatable random sequence
    long seed = random.nextLong();
    Random random = new Random(seed);
    
    for (int i = 0; i < length; ++i) {
      int value = random.nextInt(Integer.MAX_VALUE);
      map.put(i*128, value);
    }

    assertEquals(length, map.size());

    // now repeat
    random.setSeed(seed);

    for (int i = 0; i < length; ++i) {
      int value = random.nextInt(Integer.MAX_VALUE);
      assertTrue(map.containsValue(value));
      assertTrue(map.containsKey(i*128));
      assertEquals(0, Double.compare(value, map.remove(i*128)));
    }
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void testEquals() {
    IntToDoubleMap map1 = new IntToDoubleMap(100);
    IntToDoubleMap map2 = new IntToDoubleMap(100);
    assertEquals("Empty maps should be equal", map1, map2);
    assertEquals("hashCode() for empty maps should be equal", 
        map1.hashCode(), map2.hashCode());
    
    for (int i = 0; i < 100; ++i) {
      map1.put(i, Float.valueOf(1f/i));
      map2.put(i, Float.valueOf(1f/i));
    }
    assertEquals("Identical maps should be equal", map1, map2);
    assertEquals("hashCode() for identical maps should be equal", 
        map1.hashCode(), map2.hashCode());

    for (int i = 10; i < 20; i++) {
      map1.remove(i);
    }
    assertFalse("Different maps should not be equal", map1.equals(map2));
    
    for (int i = 19; i >=10; --i) {
      map2.remove(i);
    }
    assertEquals("Identical maps should be equal", map1, map2);
    assertEquals("hashCode() for identical maps should be equal", 
        map1.hashCode(), map2.hashCode());
    
    map1.put(-1,-1f);
    map2.put(-1,-1.1f);
    assertFalse("Different maps should not be equal", map1.equals(map2));
    
    map2.put(-1,-1f);
    assertEquals("Identical maps should be equal", map1, map2);
    assertEquals("hashCode() for identical maps should be equal", 
        map1.hashCode(), map2.hashCode());
  }
  
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

