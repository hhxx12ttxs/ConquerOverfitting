/*
 * Copyright (C) 2014 Marco
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.masteriti.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marco
 */
public class CalcTest {

  private static final double EPSILON = Calc.EPSILON;

  public CalcTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  /**
   * Test of findSegmentsIntersection method, of class Calc.
   */
//  @Test
  public void testGetIntersection_LineSegment2D_LineSegment2D() {
    System.out.println("findSegmentsIntersection");
    LineSegment2D[] test = getTestLineSegments();
    LineSegment2D s1 = null;
    LineSegment2D s2 = null;
    Point expResult = null;
    assertEquals("FAIL - Expected Point(4,4)", new Point(4d, 4d),
            Calc.getIntersection(test[0], test[1]));
    assertEquals("FAIL - Expected Point(0,-4)", new Point(0d, -4d),
            Calc.getIntersection(test[2], test[3]));
    assertEquals("FAIL - Expected Point(-5,0)", new Point(-5d, 0d),
            Calc.getIntersection(test[4], test[5]));
    assertEquals("FAIL - Expected null", null,
            Calc.getIntersection(test[6], test[7]));
    assertEquals("FAIL - Expected Point(4,-6)", new Point(4d, -6d),
            Calc.getIntersection(test[8], test[9]));
    assertEquals("FAIL - Expected Point(-4,-4)", new Point(-4d, -4d),
            Calc.getIntersection(test[10], test[11]));
    assertEquals("FAIL - Expected LineSegment(-4,6,-6,7)", new LineSegment2D(-4d, 6d, -6d, 7),
            Calc.getIntersection(test[12], test[13]));
    assertEquals("FAIL - Expected Point(5,8)", new Point(5d, 8d),
            Calc.getIntersection(test[14], test[15]));
    
    try{
      Calc.getIntersection(null, test[1]);
      fail("Did not throw exception when Linesegment s1 is null");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
        try{
      Calc.getIntersection(test[1], null);
      fail("Did not throw exception when Linesegment s2 is null");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  }
  
  private LineSegment2D[] getTestLineSegments() {
    LineSegment2D s1, s2, s3, s4, s5, s6, s7, s8,
            s9, s10, s11, s12, s13, s14, s15, s16;

    // Result is point (4,4)
    s1 = new LineSegment2D(1d, 1d, 7d, 7d);
    s2 = new LineSegment2D(6d, 2d, 2d, 6d);

    // Resuls in point (0,-4)
    s3 = new LineSegment2D(0d, -1d, 0d, -5d);
    s4 = new LineSegment2D(1d, -2d, -1d, -6d);

    // Result in point(-5,0)
    s5 = new LineSegment2D(-2d, 0d, -7d, 0d);
    s6 = new LineSegment2D(-2d, 3d, -7d, -2d);

    // Result in null
    s7 = new LineSegment2D(2d, -1d, 3d, -3d);
    s8 = new LineSegment2D(3d, -1d, 4d, -3d);

    // Result in point(4,-6)
    s9 = new LineSegment2D(4d, -4d, 4d, -8d);
    s10 = new LineSegment2D(2d, -6d, 6d, -6d);

    // Result in point (-4,-4)
    s11 = new LineSegment2D(-2d, -2d, -5d, -5d);
    s12 = new LineSegment2D(-4d, -4d, -8d, -4d);

    // Result in segment(-4, 6, -6, 7)
    s13 = new LineSegment2D(-6d, 7d, -2d, 5d);
    s14 = new LineSegment2D(-8d, 8d, -4d, 6d);

    // Result in point(5,8)
    s15 = new LineSegment2D(2d, 8d, 5d, 8d);
    s16 = new LineSegment2D(5d, 8d, 8d, 9d);

    return new LineSegment2D[]{s1, s2, s3, s4, s5, s6, s7, s8,
      s9, s10, s11, s12, s13, s14, s15, s16};
  }

  /**
   * Test of getVector method, of class Calc.
   */
//  @Test
  public void testGetVector() {
    System.out.println("getVector");
    Point p, pO, p1, p2, p3, pNull;
    pO = new Point(0,0);
    p1 = new Point(1,1);
    p2 = new Point(1,5);
    p3 = new Point(5,1);
    pNull = null;
    
    try {
      p = Calc.getVector(pNull, pO);
      fail("Should have thrown Null pointer exception for passing null argument");
    } catch (RuntimeException e) {
      assertTrue(true); // We're good.
    }
    
    assertEquals("Expecting (0,4) with (p1,p2) ",new Point(0, 4), Calc.getVector(p1, p2));
    assertEquals("Expecting (4,0) with (p1,p3) ",new Point(4, 0), Calc.getVector(p1, p3));
    assertEquals("Expecting (0,-4) with (p2,p1) ",new Point(0,-4), Calc.getVector(p2, p1));
    assertEquals("Expecting (-4,0) with (p3,p1) ",new Point(-4, 0), Calc.getVector(p3, p1));
    assertEquals("Expecting (sqrt(2), sqrt(2)) with (pO,p1) ",new Point(Math.sqrt(2), Math.sqrt(2)), Calc.getVector(pO, p1));
    assertEquals("Expecting (-sqrt2,-sqrt2) with (p1,pO) ",new Point(-Math.sqrt(2),-Math.sqrt(2)), Calc.getVector(p1, pO));
    assertEquals("Expecting (+inf,0) with (+inf,1)(p1) ",new Point(Double.POSITIVE_INFINITY, 0), Calc.getVector(new Point(Double.POSITIVE_INFINITY, 0),p1));
    assertEquals("Expecting (+inf,-inf) with (-inf,-inf)(+inf,0) ",
                  new Point(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
                  Calc.getVector(new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), new Point(Double.NEGATIVE_INFINITY, 0)));

    try {
      Calc.getVector(new Point(Double.POSITIVE_INFINITY, 0), new Point(Double.POSITIVE_INFINITY, 0));
      fail("Should thrown exception due to NaN for x coordinate");
    } catch (RuntimeException e) {
      assertTrue(true); // We good.
    }
    
  }

  @Test
  public void testMagnitude() {
    System.out.println("magnitude");
    double expResult;
    double result;
    Point p; 

    p = new Point(0, 4);
    expResult = 4;
    result = Calc.magnitude(p);
    assertEquals("Test 1 Fail: ", expResult, result, EPSILON);

    p = new Point(4, 0);
    expResult = 4;
    result = Calc.magnitude(p);
    assertEquals("Test 1 Fail: ", expResult, result, EPSILON);
    
    
    p = new Point(4, -3);
    expResult = 5;
    result = Calc.magnitude(p);
    assertEquals("Test 1 Fail: ", expResult, result, EPSILON);
    
  }

  /**
   * Test of dotProduct method, of class Calc.
   */
  @Test
  public void testDotProduct() {
    System.out.println("dotProduct");
    Point v1;
    Point v2;
    double expResult;
    double result;

    v1 = new Point(0, 4);
    v2 = new Point(4, 0);
    expResult = 0;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 1 Fail: ", expResult, result, EPSILON);

    v1 = new Point(0, 4);
    v2 = new Point(0, 4);
    expResult = 16;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 2 Fail: ", expResult, result, EPSILON);

    v1 = new Point(4, 0);
    v2 = new Point(4, 0);
    expResult = 16;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 3 Fail: ", expResult, result, EPSILON);

    v1 = new Point(0, 4);
    v2 = new Point(0, -4);
    expResult = -16;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 4 Fail: ", expResult, result, EPSILON);

    v1 = new Point(4, 4);
    v2 = new Point(0, -4);
    expResult = -16;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 5 Fail: ", expResult, result, EPSILON);

    v1 = new Point(4, 4);
    v2 = new Point(-4, 0);
    expResult = -16;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 6 Fail: ", expResult, result, EPSILON);

    v1 = new Point(4, 4);
    v2 = new Point(4, 0);
    expResult = 16;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 7 Fail: ", expResult, result, EPSILON);
    
    v1 = new Point(4, 4);
    v2 = new Point(-3, -3);
    expResult = -24;
    result = Calc.dotProduct(v1, v2);
    assertEquals("Test 7 Fail: ", expResult, result, EPSILON);    
  }

  /**
   * Test of getAngleRadians method, of class Calc.
   */
  @Test
  public void testGetAngleRadians() {
    System.out.println("getAngleRadians");
    Point a;
    Point b;
    Point c;
    Double expResult;
    Double result;

    a = new Point(0d, 4d);
    b = new Point(0d, 0d);
    c = new Point(4d, 0d);
    result = Calc.getAngleRadians(a, b, c);
    expResult = 0.5d;
    assertEquals("Fail Test1: ", expResult, result);

    a = new Point(4d, 3d);
    b = new Point(4d, 3d);
    c = new Point(0d, 4d);
    result = Calc.getAngleRadians(a, b, c);
    expResult = Double.NaN;
    assertTrue("Fail Test2, expected NaN, but got " + expResult + ".", result.isNaN());

    a = new Point(4d, 0d);
    b = new Point(4d, 0d);
    c = new Point(4d, 0d);
    result = Calc.getAngleRadians(a, b, c);
    expResult = Double.NaN;
    assertTrue("Fail Test3, expected NaN, but got " + expResult + ".", result.isNaN());

    a = new Point(4d, 0d);
    b = new Point(0d, 0d);
    c = new Point(0d, 4d);
    result = Calc.getAngleRadians(a, b, c);
    expResult = 1.5d;
    assertEquals("Fail Test4: ", expResult, result);

    a = new Point(4, 4);
    b = new Point(0, 0);
    c = new Point(-3, -3);
    result = Calc.getAngleRadians(a, b, c);
    expResult = 1d;
    assertEquals("Fail Test5: ", expResult, result, EPSILON);

    a = new Point(4d, 4d);
    b = new Point(1d, 1d);
    c = new Point(1d, 2d);
    result = Calc.getAngleRadians(a, b, c);
    expResult = 1.75d;
    assertEquals("Fail Test6: ", expResult, result);

    a = new Point(1d, 4d);
    b = new Point(1d, 1d);
    c = new Point(4d, 4d);
    result = Calc.getAngleRadians(a, b, c);
    expResult = 0.25d;
    assertEquals("Fail Test7: ", expResult, result);

    a = new Point(-4d, -4d);
    b = new Point(0d, 0d);
    c = new Point(0d, 2d);
    result = Calc.getAngleRadians(a, b, c);
    expResult = 0.75d;
    assertEquals("Fail Test8: ", expResult, result);

  }

  /**
   * Test of testIfColinearTo method, of class Calc.
   */
  @Test
  public void testTestIfColinearTo() {
    System.out.println("testIfColinearTo");
    Point p0, px, py, p1, p2, p3, p4;
    p0 = new Point(0,0);
    px = new Point(10, 0);
    py = new Point(0, 10);
    p1 = new Point(10, 10);
    p2 = new Point(10,-10);
    p3 = new Point(-10,-10);
    p4 = new Point(-10, 10);
    assertTrue("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(p3, p0, p1));
    assertTrue("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(p1, p0, p3));
    assertTrue("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(p2, p0, p4));
    assertTrue("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(p4, p0, p2));
    assertTrue("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(px, p2, p1));
    assertTrue("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(py, p4, p1));
    assertTrue("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(p3, p3, p1));
    assertFalse("p3 should be colinear to p0 and p1", Calc.testIfColinearTo(p3, p0, p2));
        
    try {
      Calc.testIfColinearTo(p0, p4, p4);
      fail("Should have thrown IllegalArgumentException for having Point a and b coincide!");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    
  }

  /**
   * Test of isPointOn2DSegment method, of class Calc.
   */
  @Test
  public void testIsPointOn2DSegment() {
    System.out.println("isPointOn2DSegment");
    Point p0, px, py, p1, p2, p3, p4;
    p0 = new Point(0,0);
    px = new Point(10, 0);
    py = new Point(0, 10);
    p1 = new Point(10, 10);
    p2 = new Point(10,-10);
    p3 = new Point(-10,-10);
    p4 = new Point(-10, 10);
    
    assertTrue("px should be part of segment p1-p2", Calc.isPointOn2DSegment(px, new LineSegment2D(p1, p2)));
    assertTrue("px should be part of segment p1-p2", Calc.isPointOn2DSegment(py, new LineSegment2D(p1, p4)));
    assertFalse("p2 should not be part of segment p1-p2", Calc.isPointOn2DSegment(p1, new LineSegment2D(px, p2)));
    assertFalse("p4 should not be part of segment p1-p2", Calc.isPointOn2DSegment(p1, new LineSegment2D(py, p4)));
    assertTrue("p0 should be part of segment p1-p3", Calc.isPointOn2DSegment(p0, new LineSegment2D(p1, p3)));
    assertFalse("p3 should not be part of segment p1-p0", Calc.isPointOn2DSegment(p1, new LineSegment2D(p0, p3)));
  }

 

  
  @Test
  public void testIsLeft() {
    System.out.println("isLeft");
    
    Point[] pt = { new Point( 0, 0), // 0
                   new Point(-1,-1), // 1
                   new Point( 1,-1), // 2
                   new Point( 1,-1), // 3
                   new Point( 1, 0), // 4
                   new Point( 1, 1), // 5
                   new Point( 1, 1), // 6
                   new Point( 1, 1), // 7
                   new Point( 2, 0), // 8
                   new Point( 0, 1), // 9
                   new Point(-1, 1), // 10
                   new Point( 0, 2) // 11
    };
    
    // Test colinear points
    assertTrue("Horizontal set of colinear Points", Calc.isLeft(pt[10], pt[9], pt[5])==0);
    assertTrue("Vertical set of colinear Points", Calc.isLeft(pt[5], pt[4], pt[3])==0);
    assertTrue("Diagonal set of colinear Points", Calc.isLeft(pt[1], pt[0], pt[7])==0);

    // Test point to left
    assertTrue("pt9 should be left of pts1-0", Calc.isLeft(pt[1], pt[0], pt[9]) == -1);
    assertTrue("pt9 should be left of pts4-7", Calc.isLeft(pt[4], pt[7], pt[9]) == -1);
    assertTrue("pt9 should be left of pts11-10", Calc.isLeft(pt[11], pt[10], pt[9]) == -1);
    assertTrue("pt8 should be left of pts9-10", Calc.isLeft(pt[9], pt[10], pt[8]) == -1);
    assertTrue("pt0 should be left of pts9-1", Calc.isLeft(pt[9], pt[1], pt[0]) == -1);
    
    // Test point to right (invert a and b from above)
    assertTrue("pt9 should be left of pts1-0", Calc.isLeft(pt[0], pt[1], pt[9]) == 1);
    assertTrue("pt9 should be left of pts4-7", Calc.isLeft(pt[7], pt[4], pt[9]) == 1);
    assertTrue("pt9 should be left of pts11-10", Calc.isLeft(pt[10], pt[11], pt[9]) == 1);
    assertTrue("pt8 should be left of pts9-10", Calc.isLeft(pt[10], pt[9], pt[8]) == 1);
    assertTrue("pt0 should be left of pts9-1", Calc.isLeft(pt[1], pt[9], pt[0]) == 1);
    // test where a and b coincide
    assertTrue("pt2 and pt3 coincide, result should be NaN",  Double.isNaN(Calc.isLeft(pt[2], pt[3], pt[0])));
    
    // test where a and c coincide
    assertTrue("pt3 is coincides with pt2 and is colinear to pts2-4", Calc.isLeft(pt[2], pt[4], pt[3]) == 0);
    // test where b and c coincide
    assertTrue("pt6 is coincides with pt5 and is colinear to pts4-5", Calc.isLeft(pt[4], pt[5], pt[6]) == 0);

    // test where one of the points are null
    try {
      Calc.isLeft(pt[1], pt[2], null);
      fail("Should have thrown exception for null parameters");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    try {
      Calc.isLeft(pt[3], null, pt[4]);
      fail("Should have thrown exception for null parameters");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    try {
      Calc.isLeft(null, pt[5], pt[6]);
      fail("Should have thrown exception for null parameters");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  }

  /**
   * Test of sqr method, of class Calc.
   */
  @Test
  public void testSqr() {
    System.out.println("sqr");
    double d = -Math.PI;
    double expResult = 9.8696044010893;
    double result = Calc.sqr(d);
    assertEquals(expResult, result, Calc.EPSILON);
  }

  /**
   * Test of getIntersection method, of class Calc.
   */
//  @Test
  public void testGetIntersection_4args() {
    System.out.println("getIntersection");
  }

  /**
   * Test of getOverlap method, of class Calc.
   */
  @Test
  public void testGetOverlap_Ray_Ray() {
    System.out.println("getOverlap");
    Point[] p = getTestPoints();
    Ray[] r = getTestRays();
    LineSegment2D[] s = getTestSegments();
    LineSegment2D expSeg;
    Point expPt;
    Ray expRay;
    Object result;
    
    int h,j,k;
    for(int i = 0; i < 8; i++) {
      h = (i+4)%8; // convergent rays opposite points from p0
      j = h+8;     // same direction as r[i], opposite of p0
      k = i+8;     // same origin point, opposite direction
      
      // test converging rays from opposite sides.
      result = r[i].getOverlap(r[h]);
      expSeg = s[(i%4)+16];
      assertEquals("Overlap "+r[i].toString()+" & "+r[h]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);
      
      // test as above but same direction
      result = r[i].getOverlap(r[j]);
      expRay = r[j];
      assertEquals("Overlap "+r[i].toString()+" & "+r[j]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expRay, result);
      
      // test diverging rays with same point of origin
      result = r[i].getOverlap(r[k]);
      expPt = p[i+1];
      assertEquals("Overlap "+r[i].toString()+" & "+r[k]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expPt, result);
      
      // test diverging rays with separate
      result = r[j].getOverlap(r[k]);
      assertNull("Overlap "+r[i].toString()+" & "+r[h]+" Expected: null for test "
              +i, result);
    }
  }

  /**
   * Test of getOverlap method, of class Calc.
   */
  @Test
  public void testGetOverlap_LineSegment2D_Ray() {
    System.out.println("getOverlap");
    Point[] p = getTestPoints();
    Ray[] r = getTestRays();
    LineSegment2D[] s = getTestSegments();
    LineSegment2D expSeg;
    Point expPt;
    Object result;
    
    int h;
    for(int i = 0; i < 8; i++) {
      h = (i+4)%8;
      
      // test for outside ray completely overlapping segment
      result = r[i].getOverlap(s[h]);
      expSeg = s[h];
      assertEquals("Overlap "+r[i].toString()+" & "+s[h]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);
      
      // test for ray coinciding with segment endpoint and overlapping entire segment
      result = r[i].getOverlap(s[i]);
      expSeg = s[i];
      assertEquals("Overlap "+r[i].toString()+" & "+s[i]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);

      // test ray partially overlapping segment
      result = r[i+16].getOverlap(s[i+8]);
      expSeg = s[i+32];
      assertEquals("Overlap "+r[i+16].toString()+" & "+s[i+8]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);

      // test for ray coinciding on segment endpoint and overlapping on single point.
      result = r[i+8].getOverlap(s[i]);
      expPt = p[i+1];
      assertEquals("Overlap "+r[i+8].toString()+" & "+s[i]+" Expected:"
              +expPt.toString()+", Result:"+result.toString(), expPt, result);

      // test for non overlapping ray over segment
      result = r[h+8].getOverlap(s[i]);
      assertNull("Overlap "+r[i+8].toString()+" & "+s[i]+" Expected: null for test "
              +i, result);
    }
  }

  /**
   * Test of getOverlap method, of class Calc.
   */
  @Test
  public void testGetOverlap_LineSegment2D_LineSegment2D() {
    System.out.println("getOverlap");
    Point[] p = getTestPoints();
    Ray[] r = getTestRays();
    LineSegment2D[] s = getTestSegments();
    LineSegment2D expSeg;
    Point expPt;
    Object result;
    
    int h;
    for(int i = 0; i < 8; i++) {
      h = (i+4)%8;
      
      // Test non overlapping segments
      result = s[i+24].getOverlap(s[h+24]);
      assertNull("Overlap "+s[i+24].toString()+" & "+s[h+24]+" Expected: null for test"
              +i, result);
      
      // test adjacent segments that should return only the end point in common.
      result = s[i].getOverlap(s[h]);
      expPt = p[0];
      assertEquals("Overlap "+s[i].toString()+" & "+s[h]+" Expected:"
              +expPt.toString()+", Result:"+result.toString(), expPt, result);
      
      // test partially overlapping segments, no end points in common
      result = s[i].getOverlap(s[i+8]);
      expSeg = s[i+32];
      assertEquals(i+") Overlap "+s[i].toString()+" & "+s[i+8]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);
      
      // test fully overlapped smaller segment, one end point in common
      result = s[i+32].getOverlap(s[i]);
      expSeg = s[i+32];
      assertEquals("Overlap "+s[i+32].toString()+" & "+s[i]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);
      
      // test fully overlapped segment of equal size (both end points in common.
      result = s[i*4].getOverlap(s[i*4]);
      expSeg = s[i*4];
      assertEquals("Overlap "+s[i*4].toString()+" & "+s[i*4]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);
      
      // test fully overlapped smaller segment, no end points in common
      result = s[i+8].getOverlap(s[i+16]);
      expSeg = s[i+8];
      assertEquals("Overlap "+s[i+8].toString()+" & "+s[i+16]+" Expected:"
              +expSeg.toString()+", Result:"+result.toString(), expSeg, result);
    }
  }
  
  private Point[] getTestPoints() {
    Point[] pts = { new Point( 0, 0),
                    new Point( 2, 0),
                    new Point( 2, 2),
                    new Point( 0, 2),
                    new Point(-2, 2),
                    new Point(-2, 0),
                    new Point(-2,-2),
                    new Point( 0,-2),
                    new Point( 2,-2),
                    new Point( 1, 0),
                    new Point( 1, 1),
                    new Point( 0, 1),
                    new Point(-1, 1),
                    new Point(-1, 0),
                    new Point(-1,-1),
                    new Point( 0,-1),
                    new Point( 1,-1)
    };
    return pts;
  }
  
  private Vector2d[] getTestVectors() {
    Point[] p = getTestPoints();
    Vector2d[] v = {  new Vector2d(p[0], p[1]), // 0
                      new Vector2d(p[0], p[2]), // 1
                      new Vector2d(p[0], p[3]), // 2
                      new Vector2d(p[0], p[4]), // 3
                      new Vector2d(p[0], p[5]), // 4
                      new Vector2d(p[0], p[6]), // 5
                      new Vector2d(p[0], p[7]), // 6
                      new Vector2d(p[0], p[8])  // 7
    };
    return v;
  }
  
  private Ray[] getTestRays() {
    Point[] p = getTestPoints();
    Vector2d[] v = getTestVectors();
    Ray[] r = { new Ray(p[1], v[4]),  //  0
                new Ray(p[2], v[5]),  //  1
                new Ray(p[3], v[6]),  //  2
                new Ray(p[4], v[7]),  //  3
                new Ray(p[5], v[0]),  //  4
                new Ray(p[6], v[1]),  //  5
                new Ray(p[7], v[2]),  //  6
                new Ray(p[8], v[3]),  //  7
                new Ray(p[1], v[0]),  //  8 - same origin as previous 8
                new Ray(p[2], v[1]),  //  9   but opposite direction.
                new Ray(p[3], v[2]),  // 10
                new Ray(p[4], v[3]),  // 11
                new Ray(p[5], v[4]),  // 12
                new Ray(p[6], v[5]),  // 13
                new Ray(p[7], v[6]),  // 14
                new Ray(p[8], v[7]),  // 15
                new Ray(p[0], v[0]),  // 16 - origin at 0
                new Ray(p[0], v[1]),  // 17
                new Ray(p[0], v[2]),  // 18
                new Ray(p[0], v[3]),  // 19
                new Ray(p[0], v[4]),  // 20
                new Ray(p[0], v[5]),  // 21
                new Ray(p[0], v[6]),  // 22
                new Ray(p[0], v[7])   // 23
    };
    return r;
  }
  
  private LineSegment2D[] getTestSegments() {
    Point[] p = getTestPoints();
    LineSegment2D[] s = { new LineSegment2D(p[0], p[1]),    //  0 - from origin to external point
                          new LineSegment2D(p[0], p[2]),    //  1
                          new LineSegment2D(p[0], p[3]),    //  2
                          new LineSegment2D(p[0], p[4]),    //  3
                          new LineSegment2D(p[0], p[5]),    //  4
                          new LineSegment2D(p[0], p[6]),    //  5
                          new LineSegment2D(p[0], p[7]),    //  6
                          new LineSegment2D(p[0], p[8]),    //  7
                          new LineSegment2D(p[13], p[ 9]),  //  8 - from mid point to opposite mid point
                          new LineSegment2D(p[14], p[10]),  //  9
                          new LineSegment2D(p[15], p[11]),  // 10
                          new LineSegment2D(p[16], p[12]),  // 11
                          new LineSegment2D(p[ 9], p[13]),  // 12
                          new LineSegment2D(p[10], p[14]),  // 13
                          new LineSegment2D(p[11], p[15]),  // 14
                          new LineSegment2D(p[12], p[16]),  // 15
                          new LineSegment2D(p[5], p[1]),    // 16 - from external point to opposite external point
                          new LineSegment2D(p[6], p[2]),    // 17
                          new LineSegment2D(p[7], p[3]),    // 18
                          new LineSegment2D(p[8], p[4]),    // 19
                          new LineSegment2D(p[1], p[5]),    // 20
                          new LineSegment2D(p[2], p[6]),    // 21
                          new LineSegment2D(p[3], p[7]),    // 22
                          new LineSegment2D(p[4], p[8]),    // 23
                          new LineSegment2D(p[ 9], p[1]),   // 24 - from mid point to external point
                          new LineSegment2D(p[10], p[2]),   // 25
                          new LineSegment2D(p[11], p[3]),   // 26
                          new LineSegment2D(p[12], p[4]),   // 27
                          new LineSegment2D(p[13], p[5]),   // 28
                          new LineSegment2D(p[14], p[6]),   // 29
                          new LineSegment2D(p[15], p[7]),   // 30
                          new LineSegment2D(p[16], p[8]),   // 31
                          new LineSegment2D(p[0], p[ 9]),   // 32 - from origin to mid point
                          new LineSegment2D(p[0], p[10]),   // 33
                          new LineSegment2D(p[0], p[11]),   // 34
                          new LineSegment2D(p[0], p[12]),   // 35
                          new LineSegment2D(p[0], p[13]),   // 36
                          new LineSegment2D(p[0], p[14]),   // 37
                          new LineSegment2D(p[0], p[15]),   // 38
                          new LineSegment2D(p[0], p[16])    // 39
    };
    return s;
  }
  
  @Test
  public void testIsRayCrossingBounds() {
    System.out.println("isRayCrossingBounds");
    Rectangle rect = getTestBounds();
    Point[] pt = {  new Point(0,0),       // 0
                    new Point(100,100),   // 1
                    new Point(-200,-100), // 2
                    new Point(1,0),       // 3
                    new Point(1,1),       // 4
                    new Point(0,1),       // 5
                    new Point(-1,1),      // 6
                    new Point(-1,0),      // 7
                    new Point(-1,-1),     // 8
                    new Point(0,-1),      // 9
                    new Point(1,-1)       // 10
    };
    Vector2d[] dir = {  new Vector2d(pt[0],pt[3]),
                        new Vector2d(pt[0],pt[4]).normalized(),
                        new Vector2d(pt[0],pt[5]),
                        new Vector2d(pt[0],pt[6]).normalized(),
                        new Vector2d(pt[0],pt[7]),
                        new Vector2d(pt[0],pt[8]).normalized(),
                        new Vector2d(pt[0],pt[9]),
                        new Vector2d(pt[0],pt[10]).normalized()                        
    };
    ArrayList<Ray> ray = new ArrayList();
    Arrays.asList(dir).stream().forEach(d ->{
      ray.add(new Ray(pt[1], d));
    });
    Arrays.asList(dir).stream().forEach(d->{
      ray.add(new Ray(pt[2],d));
    });
    
    final boolean[] result = {  true,
                                true,
                                true,
                                true,
                                true,
                                true,
                                true,
                                true,
                                false,
                                true,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false
    };
    
    ray.stream().forEach(r->{
      int i = ray.indexOf(r);
      assertEquals("Test "+i, result[i], Calc.isLineIntersectingRect(r, rect));
    });
  } // end of isRayCrossingBounds //////////////////////////////
  
  private Rectangle getTestBounds() {
    return new Rectangle(0, 0, 800, 600);
  }  
}

