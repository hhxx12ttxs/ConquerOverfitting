/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
<<<<<<< HEAD
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
=======
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
>>>>>>> 76aa07461566a5976980e6696204781271955163
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<<<<<<< HEAD

package org.apache.mahout.math;

import org.apache.mahout.math.function.Functions;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public final class VectorTest extends MahoutTestCase {

  @Test
  public void testSparseVector()  {
    Vector vec1 = new RandomAccessSparseVector(3);
    Vector vec2 = new RandomAccessSparseVector(3);
    doTestVectors(vec1, vec2);
  }

  @Test
  public void testSparseVectorFullIteration() {
    int[] index = {0, 1, 2, 3, 4, 5};
    double[] values = {1, 2, 3, 4, 5, 6};

    assertEquals(index.length, values.length);

    int n = index.length;

    Vector vector = new SequentialAccessSparseVector(n);
    for (int i = 0; i < n; i++) {
      vector.set(index[i], values[i]);
    }

    for (int i = 0; i < n; i++) {
      assertEquals(vector.get(i), values[i], EPSILON);
    }

    int elements = 0;
    for (Vector.Element e : vector) {
      elements++;
    }
    assertEquals(n, elements);

    assertFalse(new SequentialAccessSparseVector(0).iterator().hasNext());
  }

  @Test
  public void testSparseVectorSparseIteration() {
    int[] index = {0, 1, 2, 3, 4, 5};
    double[] values = {1, 2, 3, 4, 5, 6};

    assertEquals(index.length, values.length);

    int n = index.length;

    Vector vector = new SequentialAccessSparseVector(n);
    for (int i = 0; i < n; i++) {
      vector.set(index[i], values[i]);
    }

    for (int i = 0; i < n; i++) {
      assertEquals(vector.get(i), values[i], EPSILON);
    }

    int elements = 0;
    Iterator<Vector.Element> it = vector.iterateNonZero();
    while (it.hasNext()) {
      it.next();
      elements++;
    }
    assertEquals(n, elements);

    Vector empty = new SequentialAccessSparseVector(0);
    assertFalse(empty.iterateNonZero().hasNext());
  }

  @Test
  public void testEquivalent()  {
    //names are not used for equivalent
    RandomAccessSparseVector randomAccessLeft = new RandomAccessSparseVector(3);
    Vector sequentialAccessLeft = new SequentialAccessSparseVector(3);
    Vector right = new DenseVector(3);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    randomAccessLeft.setQuick(2, 3);
    sequentialAccessLeft.setQuick(0,1);
    sequentialAccessLeft.setQuick(1,2);
    sequentialAccessLeft.setQuick(2,3);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    assertEquals(randomAccessLeft, right);
    assertEquals(sequentialAccessLeft, right);
    assertEquals(sequentialAccessLeft, randomAccessLeft);

    Vector leftBar = new DenseVector(3);
    leftBar.setQuick(0, 1);
    leftBar.setQuick(1, 2);
    leftBar.setQuick(2, 3);
    assertEquals(leftBar, right);
    assertEquals(randomAccessLeft, right);
    assertEquals(sequentialAccessLeft, right);

    Vector rightBar = new RandomAccessSparseVector(3);
    rightBar.setQuick(0, 1);
    rightBar.setQuick(1, 2);
    rightBar.setQuick(2, 3);
    assertEquals(randomAccessLeft, rightBar);

    right.setQuick(2, 4);
    assertFalse(randomAccessLeft.equals(right));
    right = new DenseVector(4);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    right.setQuick(3, 3);
    assertFalse(randomAccessLeft.equals(right));
    randomAccessLeft = new RandomAccessSparseVector(2);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    assertFalse(randomAccessLeft.equals(right));

    Vector dense = new DenseVector(3);
    right = new DenseVector(3);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    dense.setQuick(0, 1);
    dense.setQuick(1, 2);
    dense.setQuick(2, 3);
    assertEquals(dense, right);

    RandomAccessSparseVector sparse = new RandomAccessSparseVector(3);
    randomAccessLeft = new RandomAccessSparseVector(3);
    sparse.setQuick(0, 1);
    sparse.setQuick(1, 2);
    sparse.setQuick(2, 3);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    randomAccessLeft.setQuick(2, 3);
    assertEquals(randomAccessLeft, sparse);

    Vector v1 = new VectorView(randomAccessLeft, 0, 2);
    Vector v2 = new VectorView(right, 0, 2);
    assertEquals(v1, v2);
    sparse = new RandomAccessSparseVector(2);
    sparse.setQuick(0, 1);
    sparse.setQuick(1, 2);
    assertEquals(v1, sparse);
  }

  private static void doTestVectors(Vector left, Vector right) {
    left.setQuick(0, 1);
    left.setQuick(1, 2);
    left.setQuick(2, 3);
    right.setQuick(0, 4);
    right.setQuick(1, 5);
    right.setQuick(2, 6);
    double result = left.dot(right);
    assertEquals(32.0, result, EPSILON);
  }

  @Test
  public void testGetDistanceSquared()  {
    Vector v = new DenseVector(5);
    Vector w = new DenseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);

    v = new RandomAccessSparseVector(5);
    w = new RandomAccessSparseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);

    v = new SequentialAccessSparseVector(5);
    w = new SequentialAccessSparseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);
    
  }

  @Test
  public void testAddTo() throws Exception {
    Vector v = new DenseVector(4);
    Vector w = new DenseVector(4);
    v.setQuick(0, 1);
    v.setQuick(1, 2);
    v.setQuick(2, 0);
    v.setQuick(3, 4);

    w.setQuick(0, 1);
    w.setQuick(1, 1);
    w.setQuick(2, 1);
    w.setQuick(3, 1);

    w.assign(v, Functions.PLUS);
    Vector gold = new DenseVector(new double[]{2, 3, 1, 5});
    assertEquals(w, gold);
    assertFalse(v.equals(gold));
  }


  private static void setUpV(Vector v) {
    v.setQuick(1, 2);
    v.setQuick(2, -4);
    v.setQuick(3, -9);
  }

  private static void setUpW(Vector w) {
    w.setQuick(0, -5);
    w.setQuick(1, -1);
    w.setQuick(2, 9);
    w.setQuick(3, 0.1);
    w.setQuick(4, 2.1);
  }

  private static void doTestGetDistanceSquared(Vector v, Vector w) {
    double expected = v.minus(w).getLengthSquared();
    assertEquals(expected, v.getDistanceSquared(w), 1.0e-6);
  }

  @Test
  public void testGetLengthSquared()  {
    Vector v = new DenseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
    v = new RandomAccessSparseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
    v = new SequentialAccessSparseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
  }

  public static double lengthSquaredSlowly(Vector v) {
    double d = 0.0;
    for (int i = 0; i < v.size(); i++) {
      double value = v.get(i);
      d += value * value;
    }
    return d;
  }

  private static void doTestGetLengthSquared(Vector v) {
    double expected = lengthSquaredSlowly(v);
    assertEquals("v.getLengthSquared() != sum_of_squared_elements(v)", expected, v.getLengthSquared(), 0.0);

    v.set(v.size()/2, v.get(v.size()/2) + 1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via set() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.setQuick(v.size()/5, v.get(v.size()/5) + 1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via setQuick() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    Iterator<Vector.Element> it = v.iterator();
    while (it.hasNext()) {
      Vector.Element e = it.next();
      if (e.index() == v.size() - 2) {
        e.set(e.get() - 5.0);
      }
    }
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via dense iterator.set fails to change lengthSquared",
                 expected, v.getLengthSquared(), EPSILON);

    it = v.iterateNonZero();
    int i = 0;
    while (it.hasNext()) {
      i++;
      Vector.Element e = it.next();
      if (i == v.getNumNondefaultElements() - 1) {
        e.set(e.get() - 5.0);
      }
    }
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via sparse iterator.set fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(3.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(Functions.SQUARE);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(square) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(new double[v.size()]);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(double[]) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.getElement(v.size()/2).set(2.5);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via v.getElement().set() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.normalize();
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via normalize() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.set(0, 1.5);
    v.normalize(1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via normalize(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.times(2.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via times(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.times(v);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via times(vector) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(Functions.POW, 3.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(pow, 3.0) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(v, Functions.PLUS);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(v,plus) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);
  }

  @Test
  public void testIterator() {

    Collection<Integer> expectedIndices = new HashSet<Integer>();
    int i = 1;
    while (i <= 20) {
      expectedIndices.add(i * (i + 1) / 2);
      i++;
    }

    Vector denseVector = new DenseVector(i * i);
    for (int index : expectedIndices) {
      denseVector.set(index, (double) 2 * index);
    }
    doTestIterators(denseVector, expectedIndices);

    Vector randomAccessVector = new RandomAccessSparseVector(i * i);
    for (int index : expectedIndices) {
      randomAccessVector.set(index, (double) 2 * index);
    }
    doTestIterators(randomAccessVector, expectedIndices);

    Vector sequentialVector = new SequentialAccessSparseVector(i * i);
    for (int index : expectedIndices) {
      sequentialVector.set(index, (double) 2 * index);
    }
    doTestIterators(sequentialVector, expectedIndices);
  }

  private static void doTestIterators(Vector vector, Collection<Integer> expectedIndices) {
    expectedIndices = new HashSet<Integer>(expectedIndices);
    Iterator<Vector.Element> allIterator = vector.iterator();
    int index = 0;
    while (allIterator.hasNext()) {
      Vector.Element element = allIterator.next();
      assertEquals(index, element.index());
      if (expectedIndices.contains(index)) {
        assertEquals((double) index * 2, element.get(), EPSILON);
      } else {
        assertEquals(0.0, element.get(), EPSILON);
      }
      index++;
    }

    Iterator<Vector.Element> nonZeroIterator = vector.iterateNonZero();
    while (nonZeroIterator.hasNext()) {
      Vector.Element element = nonZeroIterator.next();
      index = element.index();
      assertTrue(expectedIndices.contains(index));
      assertEquals((double) index * 2, element.get(), EPSILON);
      expectedIndices.remove(index);
    }
    assertTrue(expectedIndices.isEmpty());
  }

  @Test
  public void testNormalize()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, 1);
    vec1.setQuick(1, 2);
    vec1.setQuick(2, 3);
    Vector norm = vec1.normalize();
    assertNotNull("norm1 is null and it shouldn't be", norm);

    Vector vec2 = new SequentialAccessSparseVector(3);

    vec2.setQuick(0, 1);
    vec2.setQuick(1, 2);
    vec2.setQuick(2, 3);
    Vector norm2 = vec2.normalize();
    assertNotNull("norm1 is null and it shouldn't be", norm2);

    Vector expected = new RandomAccessSparseVector(3);

    expected.setQuick(0, 0.2672612419124244);
    expected.setQuick(1, 0.5345224838248488);
    expected.setQuick(2, 0.8017837257372732);

    assertEquals(expected, norm);

    norm = vec1.normalize(2);
    assertEquals(expected, norm);

    norm2 = vec2.normalize(2);
    assertEquals(expected, norm2);

    norm = vec1.normalize(1);
    norm2 = vec2.normalize(1);
    expected.setQuick(0, 1.0 / 6);
    expected.setQuick(1, 2.0 / 6);
    expected.setQuick(2, 3.0 / 6);
    assertEquals(expected, norm);
    assertEquals(expected, norm2);
    norm = vec1.normalize(3);
    //expected = vec1.times(vec1).times(vec1);

    // double sum = expected.zSum();
    // cube = Math.pow(sum, 1.0/3);
    double cube = Math.pow(36, 1.0 / 3);
    expected = vec1.divide(cube);

    assertEquals(norm, expected);

    norm = vec1.normalize(Double.POSITIVE_INFINITY);
    norm2 = vec2.normalize(Double.POSITIVE_INFINITY);
    // The max is 3, so we divide by that.
    expected.setQuick(0, 1.0 / 3);
    expected.setQuick(1, 2.0 / 3);
    expected.setQuick(2, 3.0 / 3);
    assertEquals(norm, expected);
    assertEquals(norm2, expected);

    norm = vec1.normalize(0);
    // The max is 3, so we divide by that.
    expected.setQuick(0, 1.0 / 3);
    expected.setQuick(1, 2.0 / 3);
    expected.setQuick(2, 3.0 / 3);
    assertEquals(norm, expected);

    try {
      vec1.normalize(-1);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      vec2.normalize(-1);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
  
  @Test
  public void testLogNormalize() {
    Vector vec1 = new RandomAccessSparseVector(3);
    
    vec1.setQuick(0, 1);
    vec1.setQuick(1, 2);
    vec1.setQuick(2, 3);
    Vector norm = vec1.logNormalize();
    assertNotNull("norm1 is null and it shouldn't be", norm);
    
    Vector vec2 = new SequentialAccessSparseVector(3);
    
    vec2.setQuick(0, 1);
    vec2.setQuick(1, 2);
    vec2.setQuick(2, 3);
    Vector norm2 = vec2.logNormalize();
    assertNotNull("norm1 is null and it shouldn't be", norm2);

    Vector expected = new DenseVector(new double[]{
      0.2672612419124244, 0.4235990463273581, 0.5345224838248488
    });

    assertVectorEquals(expected, norm, 1.0e-15);
    assertVectorEquals(expected, norm2, 1.0e-15);

    norm = vec1.logNormalize(2);
    assertVectorEquals(expected, norm, 1.0e-15);
    
    norm2 = vec2.logNormalize(2);
    assertVectorEquals(expected, norm2, 1.0e-15);
    
    try {
      vec1.logNormalize(1);
      fail("Should fail with power == 1");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      vec1.logNormalize(-1);
      fail("Should fail with negative power");
    } catch (IllegalArgumentException e) {
      // expected
    }
    
    try {
      vec2.logNormalize(Double.POSITIVE_INFINITY);
      fail("Should fail with positive infinity norm");
    } catch (IllegalArgumentException e) {
      // expected
    }  
  }

  private static void assertVectorEquals(Vector expected, Vector actual, double epsilon) {
    assertEquals(expected.size(), actual.size());
    for (Vector.Element x : expected) {
      assertEquals(x.get(), actual.get(x.index()), epsilon);
    }
  }

  @Test
  public void testMax()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(1, -3);
    vec1.setQuick(2, -2);

    double max = vec1.maxValue();
    assertEquals(-1.0, max, 0.0);

    int idx = vec1.maxValueIndex();
    assertEquals(0, idx);

    vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);
    
    vec1 = new SequentialAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);
    
    vec1 = new DenseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);
    
    vec1 = new RandomAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new DenseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new RandomAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new DenseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

  }

  @Test
  public void testMin()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, 1);
    vec1.setQuick(1, 3);
    vec1.setQuick(2, 2);

    double max = vec1.minValue();
    assertEquals(1.0, max, 0.0);

    int idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new SequentialAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new DenseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new RandomAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new DenseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new RandomAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new DenseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

  }

  @Test
  public void testDenseVector()  {
    Vector vec1 = new DenseVector(3);
    Vector vec2 = new DenseVector(3);
    doTestVectors(vec1, vec2);
  }

  @Test
  public void testVectorView()  {
    RandomAccessSparseVector vec1 = new RandomAccessSparseVector(3);
    RandomAccessSparseVector vec2 = new RandomAccessSparseVector(6);
    SequentialAccessSparseVector vec3 = new SequentialAccessSparseVector(3);
    SequentialAccessSparseVector vec4 = new SequentialAccessSparseVector(6);
    Vector vecV1 = new VectorView(vec1, 0, 3);
    Vector vecV2 = new VectorView(vec2, 2, 3);
    Vector vecV3 = new VectorView(vec3, 0, 3);
    Vector vecV4 = new VectorView(vec4, 2, 3);
    doTestVectors(vecV1, vecV2);
    doTestVectors(vecV3, vecV4);
  }

  /** Asserts a vector using enumeration equals a given dense vector */
  private static void doTestEnumeration(double[] apriori, Vector vector) {
    double[] test = new double[apriori.length];
    Iterator<Vector.Element> iter = vector.iterateNonZero();
    while (iter.hasNext()) {
      Vector.Element e = iter.next();
      test[e.index()] = e.get();
    }

    for (int i = 0; i < test.length; i++) {
      assertEquals(apriori[i], test[i], EPSILON);
    }
  }

  @Test
  public void testEnumeration()  {
    double[] apriori = {0, 1, 2, 3, 4};

    doTestEnumeration(apriori, new VectorView(new DenseVector(new double[]{
        -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}), 2, 5));

    doTestEnumeration(apriori, new DenseVector(new double[]{0, 1, 2, 3, 4}));

    Vector sparse = new RandomAccessSparseVector(5);
    sparse.set(0, 0);
    sparse.set(1, 1);
    sparse.set(2, 2);
    sparse.set(3, 3);
    sparse.set(4, 4);
    doTestEnumeration(apriori, sparse);

    sparse = new SequentialAccessSparseVector(5);
    sparse.set(0, 0);
    sparse.set(1, 1);
    sparse.set(2, 2);
    sparse.set(3, 3);
    sparse.set(4, 4);
    doTestEnumeration(apriori, sparse);

  }

  @Test
  public void testAggregation()  {
    Vector v = new DenseVector(5);
    Vector w = new DenseVector(5);
    setUpFirstVector(v);
    setUpSecondVector(w);
    doTestAggregation(v, w);
    v = new RandomAccessSparseVector(5);
    w = new RandomAccessSparseVector(5);
    setUpFirstVector(v);
    doTestAggregation(v, w);
    setUpSecondVector(w);
    doTestAggregation(w, v);
    v = new SequentialAccessSparseVector(5);
    w = new SequentialAccessSparseVector(5);
    setUpFirstVector(v);
    doTestAggregation(v, w);
    setUpSecondVector(w);
    doTestAggregation(w, v);
  }

  private static void doTestAggregation(Vector v, Vector w) {
    assertEquals("aggregate(plus, pow(2)) not equal to " + v.getLengthSquared(),
        v.getLengthSquared(),
        v.aggregate(Functions.PLUS, Functions.pow(2)), EPSILON);
    assertEquals("aggregate(plus, abs) not equal to " + v.norm(1),
        v.norm(1),
        v.aggregate(Functions.PLUS, Functions.ABS), EPSILON);
    assertEquals("aggregate(max, abs) not equal to " + v.norm(Double.POSITIVE_INFINITY),
        v.norm(Double.POSITIVE_INFINITY),
        v.aggregate(Functions.MAX, Functions.ABS), EPSILON);

    assertEquals("v.dot(w) != v.aggregate(w, plus, mult)",
        v.dot(w),
        v.aggregate(w, Functions.PLUS, Functions.MULT), EPSILON);
    assertEquals("|(v-w)|^2 != v.aggregate(w, plus, chain(pow(2), minus))",
        v.minus(w).dot(v.minus(w)),
        v.aggregate(w, Functions.PLUS, Functions.chain(Functions.pow(2), Functions.MINUS)), EPSILON);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAggregate1() {
    assertEquals(1.0, new DenseVector(new double[]{1}).aggregate(Functions.MIN, Functions.IDENTITY), EPSILON);
    assertEquals(1.0, new DenseVector(new double[]{2, 1}).aggregate(Functions.MIN, Functions.IDENTITY), EPSILON);
    new DenseVector(new double[0]).aggregate(Functions.MIN, Functions.IDENTITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAggregate2() {
    assertEquals(3.0, new DenseVector(new double[]{1}).aggregate(
        new DenseVector(new double[]{2}),Functions.MIN, Functions.PLUS), EPSILON);
    new DenseVector(new double[0]).aggregate(new DenseVector(new double[0]), Functions.MIN, Functions.PLUS);
  }

  private static void setUpFirstVector(Vector v) {
    v.setQuick(1, 2);
    v.setQuick(2, 0.5);
    v.setQuick(3, -5);
  }

  private static void setUpSecondVector(Vector v) {
    v.setQuick(0, 3);
    v.setQuick(1, -1.5);
    v.setQuick(2, -5);
    v.setQuick(3, 2);
  }

  @Test
  public void testHashCodeEquivalence() {
    // Hash codes must be equal if the vectors are considered equal
    Vector sparseLeft = new RandomAccessSparseVector(3);
    Vector denseRight = new DenseVector(3);
    sparseLeft.setQuick(0, 1);
    sparseLeft.setQuick(1, 2);
    sparseLeft.setQuick(2, 3);
    denseRight.setQuick(0, 1);
    denseRight.setQuick(1, 2);
    denseRight.setQuick(2, 3);
    assertEquals(sparseLeft, denseRight);
    assertEquals(sparseLeft.hashCode(), denseRight.hashCode());

    sparseLeft = new SequentialAccessSparseVector(3);
    sparseLeft.setQuick(0, 1);
    sparseLeft.setQuick(1, 2);
    sparseLeft.setQuick(2, 3);
    assertEquals(sparseLeft, denseRight);
    assertEquals(sparseLeft.hashCode(), denseRight.hashCode());

    Vector denseLeft = new DenseVector(3);
    denseLeft.setQuick(0, 1);
    denseLeft.setQuick(1, 2);
    denseLeft.setQuick(2, 3);
    assertEquals(denseLeft, denseRight);
    assertEquals(denseLeft.hashCode(), denseRight.hashCode());

    Vector sparseRight = new SequentialAccessSparseVector(3);
    sparseRight.setQuick(0, 1);
    sparseRight.setQuick(1, 2);
    sparseRight.setQuick(2, 3);
    assertEquals(sparseLeft, sparseRight);
    assertEquals(sparseLeft.hashCode(), sparseRight.hashCode());

    DenseVector emptyLeft = new DenseVector(0);
    Vector emptyRight = new SequentialAccessSparseVector(0);
    assertEquals(emptyLeft, emptyRight);
    assertEquals(emptyLeft.hashCode(), emptyRight.hashCode());
    emptyRight = new RandomAccessSparseVector(0);
    assertEquals(emptyLeft, emptyRight);
    assertEquals(emptyLeft.hashCode(), emptyRight.hashCode());
  }

  @Test
  public void testHashCode() {
    // Make sure that hash([1,0,2]) != hash([1,2,0])
    Vector left = new SequentialAccessSparseVector(3);
    Vector right = new SequentialAccessSparseVector(3);
    left.setQuick(0, 1);
    left.setQuick(2, 2);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    left = new RandomAccessSparseVector(3);
    right = new RandomAccessSparseVector(3);
    left.setQuick(0, 1);
    left.setQuick(2, 2);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    // Make sure that hash([1,0,2,0,0,0]) != hash([1,0,2])
    right = new SequentialAccessSparseVector(5);
    right.setQuick(0, 1);
    right.setQuick(2, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    right = new RandomAccessSparseVector(5);
    right.setQuick(0, 1);
    right.setQuick(2, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());
  }
=======
package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Provides extra functionality for Java Number classes.</p>
 *
 * @since 2.0
 * @version $Id$
 */
public class NumberUtils {
    
    /** Reusable Long constant for zero. */
    public static final Long LONG_ZERO = Long.valueOf(0L);
    /** Reusable Long constant for one. */
    public static final Long LONG_ONE = Long.valueOf(1L);
    /** Reusable Long constant for minus one. */
    public static final Long LONG_MINUS_ONE = Long.valueOf(-1L);
    /** Reusable Integer constant for zero. */
    public static final Integer INTEGER_ZERO = Integer.valueOf(0);
    /** Reusable Integer constant for one. */
    public static final Integer INTEGER_ONE = Integer.valueOf(1);
    /** Reusable Integer constant for minus one. */
    public static final Integer INTEGER_MINUS_ONE = Integer.valueOf(-1);
    /** Reusable Short constant for zero. */
    public static final Short SHORT_ZERO = Short.valueOf((short) 0);
    /** Reusable Short constant for one. */
    public static final Short SHORT_ONE = Short.valueOf((short) 1);
    /** Reusable Short constant for minus one. */
    public static final Short SHORT_MINUS_ONE = Short.valueOf((short) -1);
    /** Reusable Byte constant for zero. */
    public static final Byte BYTE_ZERO = Byte.valueOf((byte) 0);
    /** Reusable Byte constant for one. */
    public static final Byte BYTE_ONE = Byte.valueOf((byte) 1);
    /** Reusable Byte constant for minus one. */
    public static final Byte BYTE_MINUS_ONE = Byte.valueOf((byte) -1);
    /** Reusable Double constant for zero. */
    public static final Double DOUBLE_ZERO = Double.valueOf(0.0d);
    /** Reusable Double constant for one. */
    public static final Double DOUBLE_ONE = Double.valueOf(1.0d);
    /** Reusable Double constant for minus one. */
    public static final Double DOUBLE_MINUS_ONE = Double.valueOf(-1.0d);
    /** Reusable Float constant for zero. */
    public static final Float FLOAT_ZERO = Float.valueOf(0.0f);
    /** Reusable Float constant for one. */
    public static final Float FLOAT_ONE = Float.valueOf(1.0f);
    /** Reusable Float constant for minus one. */
    public static final Float FLOAT_MINUS_ONE = Float.valueOf(-1.0f);

    /**
     * <p><code>NumberUtils</code> instances should NOT be constructed in standard programming.
     * Instead, the class should be used as <code>NumberUtils.toInt("6");</code>.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public NumberUtils() {
        super();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Convert a <code>String</code> to an <code>int</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toInt(null) = 0
     *   NumberUtils.toInt("")   = 0
     *   NumberUtils.toInt("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the int represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.1
     */
    public static int toInt(String str) {
        return toInt(str, 0);
    }

    /**
     * <p>Convert a <code>String</code> to an <code>int</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toInt(null, 1) = 1
     *   NumberUtils.toInt("", 1)   = 1
     *   NumberUtils.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the int represented by the string, or the default if conversion fails
     * @since 2.1
     */
    public static int toInt(String str, int defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>long</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toLong(null) = 0L
     *   NumberUtils.toLong("")   = 0L
     *   NumberUtils.toLong("1")  = 1L
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the long represented by the string, or <code>0</code> if
     *  conversion fails
     * @since 2.1
     */
    public static long toLong(String str) {
        return toLong(str, 0L);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>long</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toLong(null, 1L) = 1L
     *   NumberUtils.toLong("", 1L)   = 1L
     *   NumberUtils.toLong("1", 0L)  = 1L
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the long represented by the string, or the default if conversion fails
     * @since 2.1
     */
    public static long toLong(String str, long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>float</code>, returning
     * <code>0.0f</code> if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>,
     * <code>0.0f</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toFloat(null)   = 0.0f
     *   NumberUtils.toFloat("")     = 0.0f
     *   NumberUtils.toFloat("1.5")  = 1.5f
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @return the float represented by the string, or <code>0.0f</code>
     *  if conversion fails
     * @since 2.1
     */
    public static float toFloat(String str) {
        return toFloat(str, 0.0f);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>float</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>, the default
     * value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toFloat(null, 1.1f)   = 1.0f
     *   NumberUtils.toFloat("", 1.1f)     = 1.1f
     *   NumberUtils.toFloat("1.5", 0.0f)  = 1.5f
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @param defaultValue the default value
     * @return the float represented by the string, or defaultValue
     *  if conversion fails
     * @since 2.1
     */
    public static float toFloat(String str, float defaultValue) {
      if (str == null) {
          return defaultValue;
      }     
      try {
          return Float.parseFloat(str);
      } catch (NumberFormatException nfe) {
          return defaultValue;
      }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>double</code>, returning
     * <code>0.0d</code> if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>,
     * <code>0.0d</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null)   = 0.0d
     *   NumberUtils.toDouble("")     = 0.0d
     *   NumberUtils.toDouble("1.5")  = 1.5d
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @return the double represented by the string, or <code>0.0d</code>
     *  if conversion fails
     * @since 2.1
     */
    public static double toDouble(String str) {
        return toDouble(str, 0.0d);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>double</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>, the default
     * value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null, 1.1d)   = 1.1d
     *   NumberUtils.toDouble("", 1.1d)     = 1.1d
     *   NumberUtils.toDouble("1.5", 0.0d)  = 1.5d
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @param defaultValue the default value
     * @return the double represented by the string, or defaultValue
     *  if conversion fails
     * @since 2.1
     */
    public static double toDouble(String str, double defaultValue) {
      if (str == null) {
          return defaultValue;
      }
      try {
          return Double.parseDouble(str);
      } catch (NumberFormatException nfe) {
          return defaultValue;
      }
    }

     //-----------------------------------------------------------------------
     /**
     * <p>Convert a <code>String</code> to a <code>byte</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toByte(null) = 0
     *   NumberUtils.toByte("")   = 0
     *   NumberUtils.toByte("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the byte represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.5
     */
    public static byte toByte(String str) {
        return toByte(str, (byte) 0);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>byte</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toByte(null, 1) = 1
     *   NumberUtils.toByte("", 1)   = 1
     *   NumberUtils.toByte("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the byte represented by the string, or the default if conversion fails
     * @since 2.5
     */
    public static byte toByte(String str, byte defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>short</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toShort(null) = 0
     *   NumberUtils.toShort("")   = 0
     *   NumberUtils.toShort("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the short represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.5
     */
    public static short toShort(String str) {
        return toShort(str, (short) 0);
    }

    /**
     * <p>Convert a <code>String</code> to an <code>short</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toShort(null, 1) = 1
     *   NumberUtils.toShort("", 1)   = 1
     *   NumberUtils.toShort("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the short represented by the string, or the default if conversion fails
     * @since 2.5
     */
    public static short toShort(String str, short defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    //-----------------------------------------------------------------------
    // must handle Long, Float, Integer, Float, Short,
    //                  BigDecimal, BigInteger and Byte
    // useful methods:
    // Byte.decode(String)
    // Byte.valueOf(String,int radix)
    // Byte.valueOf(String)
    // Double.valueOf(String)
    // Float.valueOf(String)
    // Float.valueOf(String)
    // Integer.valueOf(String,int radix)
    // Integer.valueOf(String)
    // Integer.decode(String)
    // Integer.getInteger(String)
    // Integer.getInteger(String,int val)
    // Integer.getInteger(String,Integer val)
    // Integer.valueOf(String)
    // Double.valueOf(String)
    // new Byte(String)
    // Long.valueOf(String)
    // Long.getLong(String)
    // Long.getLong(String,int)
    // Long.getLong(String,Integer)
    // Long.valueOf(String,int)
    // Long.valueOf(String)
    // Short.valueOf(String)
    // Short.decode(String)
    // Short.valueOf(String,int)
    // Short.valueOf(String)
    // new BigDecimal(String)
    // new BigInteger(String)
    // new BigInteger(String,int radix)
    // Possible inputs:
    // 45 45.5 45E7 4.5E7 Hex Oct Binary xxxF xxxD xxxf xxxd
    // plus minus everything. Prolly more. A lot are not separable.

    /**
     * <p>Turns a string value into a java.lang.Number.</p>
     *
     * <p>First, the value is examined for a type qualifier on the end
     * (<code>'f','F','d','D','l','L'</code>).  If it is found, it starts 
     * trying to create successively larger types from the type specified
     * until one is found that can represent the value.</p>
     *
     * <p>If a type specifier is not found, it will check for a decimal point
     * and then try successively larger types from <code>Integer</code> to
     * <code>BigInteger</code> and from <code>Float</code> to
     * <code>BigDecimal</code>.</p>
     *
     * <p>If the string starts with <code>0x</code> or <code>-0x</code> (lower or upper case), it
     * will be interpreted as a hexadecimal integer.  Values with leading
     * <code>0</code>'s will not be interpreted as octal.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * <p>This method does not trim the input string, i.e., strings with leading
     * or trailing spaces will generate NumberFormatExceptions.</p>
     *
     * @param str  String containing a number, may be null
     * @return Number created from the string (or null if the input is null)
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Number createNumber(String str) throws NumberFormatException {
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }  
        if (str.startsWith("--")) {
            // this is protection for poorness in java.lang.BigDecimal.
            // it accepts this as a legal value, but it does not appear 
            // to be in specification of class. OS X Java parses it to 
            // a wrong value.
            return null;
        }
        if (str.startsWith("0x") || str.startsWith("-0x") || str.startsWith("0X") || str.startsWith("-0X")) {
            return createInteger(str);
        }   
        char lastChar = str.charAt(str.length() - 1);
        String mant;
        String dec;
        String exp;
        int decPos = str.indexOf('.');
        int expPos = str.indexOf('e') + str.indexOf('E') + 1;

        if (decPos > -1) {

            if (expPos > -1) {
                if (expPos < decPos || expPos > str.length()) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                dec = str.substring(decPos + 1, expPos);
            } else {
                dec = str.substring(decPos + 1);
            }
            mant = str.substring(0, decPos);
        } else {
            if (expPos > -1) {
                if (expPos > str.length()) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                mant = str.substring(0, expPos);
            } else {
                mant = str;
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar) && lastChar != '.') {
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length() - 1);
            } else {
                exp = null;
            }
            //Requesting a specific type..
            String numeric = str.substring(0, str.length() - 1);
            boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
            switch (lastChar) {
                case 'l' :
                case 'L' :
                    if (dec == null
                        && exp == null
                        && (numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric))) {
                        try {
                            return createLong(numeric);
                        } catch (NumberFormatException nfe) { // NOPMD
                            // Too big for a long
                        }
                        return createBigInteger(numeric);

                    }
                    throw new NumberFormatException(str + " is not a valid number.");
                case 'f' :
                case 'F' :
                    try {
                        Float f = NumberUtils.createFloat(numeric);
                        if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                            //If it's too big for a float or the float value = 0 and the string
                            //has non-zeros in it, then float does not have the precision we want
                            return f;
                        }

                    } catch (NumberFormatException nfe) { // NOPMD
                        // ignore the bad number
                    }
                    //$FALL-THROUGH$
                case 'd' :
                case 'D' :
                    try {
                        Double d = NumberUtils.createDouble(numeric);
                        if (!(d.isInfinite() || (d.floatValue() == 0.0D && !allZeros))) {
                            return d;
                        }
                    } catch (NumberFormatException nfe) { // NOPMD
                        // ignore the bad number
                    }
                    try {
                        return createBigDecimal(numeric);
                    } catch (NumberFormatException e) { // NOPMD
                        // ignore the bad number
                    }
                    //$FALL-THROUGH$
                default :
                    throw new NumberFormatException(str + " is not a valid number.");

            }
        } else {
            //User doesn't have a preference on the return type, so let's start
            //small and go from there...
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length());
            } else {
                exp = null;
            }
            if (dec == null && exp == null) {
                //Must be an int,long,bigint
                try {
                    return createInteger(str);
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }
                try {
                    return createLong(str);
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }
                return createBigInteger(str);

            } else {
                //Must be a float,double,BigDec
                boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
                try {
                    Float f = createFloat(str);
                    if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                        return f;
                    }
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }
                try {
                    Double d = createDouble(str);
                    if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                        return d;
                    }
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }

                return createBigDecimal(str);

            }
        }
    }

    /**
     * <p>Utility method for {@link #createNumber(java.lang.String)}.</p>
     *
     * <p>Returns <code>true</code> if s is <code>null</code>.</p>
     * 
     * @param str  the String to check
     * @return if it is all zeros or <code>null</code>
     */
    private static boolean isAllZeros(String str) {
        if (str == null) {
            return true;
        }
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != '0') {
                return false;
            }
        }
        return str.length() > 0;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Convert a <code>String</code> to a <code>Float</code>.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     * 
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Float</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Float createFloat(String str) {
        if (str == null) {
            return null;
        }
        return Float.valueOf(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>Double</code>.</p>
     * 
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Double</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Double createDouble(String str) {
        if (str == null) {
            return null;
        }
        return Double.valueOf(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>Integer</code>, handling
     * hex and octal notations.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     * 
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Integer</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Integer createInteger(String str) {
        if (str == null) {
            return null;
        }
        // decode() handles 0xAABD and 0777 (hex and octal) as well.
        return Integer.decode(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>Long</code>; 
     * since 3.1 it handles hex and octal notations.</p>
     * 
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Long</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Long createLong(String str) {
        if (str == null) {
            return null;
        }
        return Long.decode(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>BigInteger</code>.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     * 
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>BigInteger</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static BigInteger createBigInteger(String str) {
        if (str == null) {
            return null;
        }
        return new BigInteger(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>BigDecimal</code>.</p>
     * 
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>BigDecimal</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static BigDecimal createBigDecimal(String str) {
        if (str == null) {
            return null;
        }
        // handle JDK1.3.1 bug where "" throws IndexOutOfBoundsException
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }  
        return new BigDecimal(str);
    }

    // Min in array
    //--------------------------------------------------------------------
    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static long min(long[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        long min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static int min(int[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        int min = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] < min) {
                min = array[j];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static short min(short[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        short min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static byte min(byte[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        byte min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

     /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#min(double[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static double min(double[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Double.isNaN(array[i])) {
                return Double.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#min(float[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static float min(float[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        float min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Float.isNaN(array[i])) {
                return Float.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    // Max in array
    //--------------------------------------------------------------------
    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static long max(long[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        long max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static int max(int[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        int max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static short max(short[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        short max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static byte max(byte[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        byte max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#max(double[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static double max(double[] array) {
        // Validates input
        if (array== null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        double max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#max(float[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static float max(float[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        float max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Float.isNaN(array[j])) {
                return Float.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }
     
    // 3 param min
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the minimum of three <code>long</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static long min(long a, long b, long c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>int</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static int min(int a, int b, int c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>short</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static short min(short a, short b, short c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>byte</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static byte min(byte a, byte b, byte c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>double</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     * @see IEEE754rUtils#min(double, double, double) for a version of this method that handles NaN differently
     */
    public static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * <p>Gets the minimum of three <code>float</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     * @see IEEE754rUtils#min(float, float, float) for a version of this method that handles NaN differently
     */
    public static float min(float a, float b, float c) {
        return Math.min(Math.min(a, b), c);
    }

    // 3 param max
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the maximum of three <code>long</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static long max(long a, long b, long c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>int</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static int max(int a, int b, int c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>short</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static short max(short a, short b, short c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>byte</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static byte max(byte a, byte b, byte c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>double</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     * @see IEEE754rUtils#max(double, double, double) for a version of this method that handles NaN differently
     */
    public static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    /**
     * <p>Gets the maximum of three <code>float</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     * @see IEEE754rUtils#max(float, float, float) for a version of this method that handles NaN differently
     */
    public static float max(float a, float b, float c) {
        return Math.max(Math.max(a, b), c);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Checks whether the <code>String</code> contains only
     * digit characters.</p>
     *
     * <p><code>Null</code> and empty String will return
     * <code>false</code>.</p>
     *
     * @param str  the <code>String</code> to check
     * @return <code>true</code> if str contains only Unicode numeric
     */
    public static boolean isDigits(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks whether the String a valid Java number.</p>
     *
     * <p>Valid numbers include hexadecimal marked with the <code>0x</code>
     * qualifier, scientific notation and numbers marked with a type
     * qualifier (e.g. 123L).</p>
     *
     * <p><code>Null</code> and empty String will return
     * <code>false</code>.</p>
     *
     * @param str  the <code>String</code> to check
     * @return <code>true</code> if the string is a correctly formatted number
     */
    public static boolean isNumber(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0' && chars[start + 1] == 'x') {
            int i = start + 2;
            if (i == sz) {
                return false; // str == "0x"
            }
            // checking hex (it can't be anything else)
            for (; i < chars.length; i++) {
                if ((chars[i] < '0' || chars[i] > '9')
                    && (chars[i] < 'a' || chars[i] > 'f')
                    && (chars[i] < 'A' || chars[i] > 'F')) {
                    return false;
                }
            }
            return true;
        }
        sz--; // don't want to loop to the last char, check it afterwords
              // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent   
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163

}

