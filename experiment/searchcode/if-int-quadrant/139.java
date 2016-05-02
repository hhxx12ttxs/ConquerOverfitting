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
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Marco
 */
public class LineSegment2DTest {
  
  public LineSegment2DTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Test
  public void testConstructionException() {
    System.out.println("LineSegment constructor");
    try {
      LineSegment2D s = new LineSegment2D(0,0,0,0);
      fail("Should have thrown an exception for coinciding end points");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(20123123123.01234d,10d,20123123123.01234d,10d);
      fail("Should have thrown an exception for coinciding end points");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(-10,0,-10,0);
      fail("Should have thrown an exception for coinciding end points");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(null,new Point(-10,0));
      fail("Should have thrown an exception for null Point a parameter");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(new Point(-10,0), null);
      fail("Should have thrown an exception for null Point b parameter");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(Double.NEGATIVE_INFINITY,1, 2, 3);
      fail("Should have thrown an exception for point a with infinite x parameter");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(0, Double.POSITIVE_INFINITY, 2, 3);
      fail("Should have thrown an exception for point a with infinite y parameter");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(0, 1, Double.POSITIVE_INFINITY, 3);
      fail("Should have thrown an exception for point b with infinite x parameter");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
    
    try {
      LineSegment2D s = new LineSegment2D(0, 1, 2, Double.NEGATIVE_INFINITY);
      fail("Should have thrown an exception for point b with infinite y parameter");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  }
  
  /**
   * Test of getLength method, of class LineSegment2D.
   */
  @Test
  public void testGetLength() {
    System.out.println("getLength");
    ArrayList<LineSegment2D> s = getTestSegments();
    double diagResult = 10*Math.sqrt(2);
    assertEquals("Test 1", 10, s.get(0).getLength(), Calc.EPSILON);
    assertEquals("Test 2", 10, s.get(1).getLength(), Calc.EPSILON);
    assertEquals("Test 3", 10, s.get(2).getLength(), Calc.EPSILON);
    assertEquals("Test 4", 10, s.get(3).getLength(), Calc.EPSILON);
    assertEquals("Test 5", diagResult, s.get(4).getLength(), Calc.EPSILON);
    assertEquals("Test 6", diagResult, s.get(5).getLength(), Calc.EPSILON);
    assertEquals("Test 7", diagResult, s.get(6).getLength(), Calc.EPSILON);
    assertEquals("Test 8", diagResult, s.get(7).getLength(), Calc.EPSILON);
    
  }


  /**
   * Test of reorientSegment method, of class LineSegment2D.
   */
  @Test
  public void testReorientSegment_0args() {
    System.out.println("reorientSegment");
    
    ArrayList<LineSegment2D> s = getTestSegments();

    s.stream().forEach(seg -> {
      LineSegment2D segment = seg.reorientSegment();
      assertTrue("Test Ax >= Bx "+s.indexOf(segment), segment.getPointAx() >= segment.getPointBx());
      if(segment.getPointAx()== segment.getPointBx()) {
        assertTrue("Test Ay > By "+s.indexOf(segment), segment.getPointAy() > segment.getPointBy());
      }
    });
  }

  /**
   * Test of getXatY method, of class LineSegment2D.
   */
  @Test
  public void testGetXatY() {
    System.out.println("getXatY");
    ArrayList<LineSegment2D> s = getTestSegments();
    double[] resultNeg10 = {Double.NaN, 0, Double.NaN, 0,-10, 10,-10, 10};
    double[] resultNeg5 = {Double.NaN, 0, Double.NaN, 0,-5, 5,-5, 5};
    double[] resultOrigin = {Double.NaN, 0, Double.NaN, 0, 0, 0, 0, 0};
    double[] result5 = {Double.NaN, 0, Double.NaN, 0, 5,-5, 5,-5};
    double[] result10 = {Double.NaN, 0, Double.NaN, 0, 10,-10, 10,-10};
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Test x = -10 for Segment "+i, resultNeg10[i], seg.getXatY(-10), Calc.EPSILON);
      assertEquals("Test x = -5 for Segment "+i, resultNeg5[i], seg.getXatY(-5), Calc.EPSILON);
      assertEquals("Test x = 0 for Segment "+i, resultOrigin[i], seg.getXatY(0), Calc.EPSILON);
      assertEquals("Test x = 5 for Segment "+i, result5[i], seg.getXatY(5), Calc.EPSILON);
      assertEquals("Test x = 10 for Segment "+i, result10[i], seg.getXatY(10), Calc.EPSILON);
    }
  }

  /**
   * Test of getYatX method, of class LineSegment2D.
   */
  @Test
  public void testGetYatX() {
    System.out.println("getYatX");
    ArrayList<LineSegment2D> s = getTestSegments();
    double[] resultNeg10 = { 0, Double.NaN, 0, Double.NaN,-10, 10,-10, 10};
    double[] resultNeg5 = { 0,Double.NaN, 0, Double.NaN,-5, 5,-5, 5};
    double[] resultOrigin = { 0, Double.NaN, 0, Double.NaN, 0, 0, 0, 0};
    double[] result5 = { 0,Double.NaN, 0, Double.NaN, 5,-5, 5,-5};
    double[] result10 = { 0,Double.NaN, 0, Double.NaN, 10,-10, 10,-10};
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Test x = -10 for Segment "+seg.printPointsToString(), resultNeg10[i], seg.getYatX(-10), Calc.EPSILON);
      assertEquals("Test x = -5 for Segment "+seg.printPointsToString(), resultNeg5[i], seg.getYatX(-5), Calc.EPSILON);
      assertEquals("Test x = 0 for Segment "+seg.printPointsToString(), resultOrigin[i], seg.getYatX(0), Calc.EPSILON);
      assertEquals("Test x = 5 for Segment "+seg.printPointsToString(), result5[i], seg.getYatX(5), Calc.EPSILON);
      assertEquals("Test x = 10 for Segment "+seg.printPointsToString(), result10[i], seg.getYatX(10), Calc.EPSILON);
    }   
  }

  /**
   * Test of compareTo method, of class LineSegment2D.
   */
  @Test
  public void testCompareTo() {
    System.out.println("compareTo");
    ArrayList<LineSegment2D> s = getTestSegments();
    LineSegment2D justSmallerThan10 = new LineSegment2D(5, 2, (15 - (Calc.EPSILON/2)), 2);
    LineSegment2D seg10 = s.get(0);
    LineSegment2D seg10sqrt2 = s.get(4);
    int[] expectedForSmallerThan10 = {0, 0, 0, 0,1,1,1,1};
    int[] expectedForSeg10 = {0, 0, 0, 0, 1, 1, 1, 1};
    int[] expectedForSeg10sqrt2 = {-1, -1, -1, -1, 0, 0, 0, 0};
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Test comparison with just smaller than 10 with seg-"+i, expectedForSmallerThan10[i], seg.compareTo(justSmallerThan10));
      assertEquals("Test comparison with length 10 with seg-"+i, expectedForSeg10[i], seg.compareTo(seg10));
      assertEquals(i+") Comparing fixed "+seg10sqrt2.printPointsToString()+" of length "+seg10sqrt2.getLength()+" with segment "+seg.printPointsToString()+" of length "
              +seg.getLength()+", ", expectedForSeg10sqrt2[i], seg.compareTo(seg10sqrt2));
    }
  }

  /**
   * Test of equals method, of class LineSegment2D.
   */
  @Test
  public void testEqualsAndHashCode() {
    System.out.println("equals and hashcode");
    ArrayList<LineSegment2D> s, c, d;
    s = getTestSegments();
    c = new ArrayList<>();
    d = new ArrayList<>();
    LineSegment2D diff = new LineSegment2D(new Point(1,2), new Point(-3, -4.56789));
    for(LineSegment2D seg : s) {
      c.add(new LineSegment2D(seg.getPointA().copy(), seg.getPointB().copy()));
      d.add(new LineSegment2D(seg.getPointA().copy(), seg.getPointB().copy()));
    }
    
    LineSegment2D copy;
    for(int i = 0; i < c.size(); i++) {
      copy = c.get(i);
      copy.reorientSegment(); // orientation should not matter.
      assertEquals("Test "+copy.printPointsToString()+" equals "+s.get(i).printPointsToString(),copy, s.get(i));
      assertEquals("Test Equality "+i,copy, d.get(i));
      assertEquals("Test Transitive "+i,d.get(i), s.get(i));
      assertTrue("Test Reflective "+i, s.get(i).equals(s.get(i)));
      assertFalse("Test not equal to null "+i, copy.equals(null));
      assertFalse("Test not equal to different segment "+i, copy.equals(diff));
      assertFalse("Test objects are distinct "+i, copy == s.get(i));
      
      // Test HashCode
      assertEquals("Segment "+copy.printPointsToString()+" doesn't have same hascode as "+s.get(i).printPointsToString(), copy.hashCode(), s.get(i).hashCode());
    }
  }
  
  /**
   * Test of getTopPoint method, of class LineSegment2D.
   */
  @Test
  public void testGetTopPoint() {
    System.out.println("getTopPoint");
    ArrayList<LineSegment2D> s = getTestSegments();
    Point[] p = getTestPoints();
    Point[] expected = {p[0], p[2], p[3], p[0], p[5], p[0], p[0], p[8]};
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      Point pt = seg.getTopPoint();
      assertEquals("Test "+i+" for segment "+seg.printPointsToString()+", expecting "
              +expected[i].print2DLoc()+" but getting "+pt.print2DLoc(), expected[i], pt);
    }
  }
  
  /**
   * Test of getBottomPoint method, of class LineSegment2D.
   */
  @Test
  public void testGetBottomPoint() {
    System.out.println("getBottomPoint");
    ArrayList<LineSegment2D> s = getTestSegments();
    Point[] p = getTestPoints();
    Point[] expected = {p[1], p[0], p[0], p[4], p[0], p[6], p[7], p[0]};
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Test "+i+" for segment "+seg.printPointsToString(), expected[i], seg.getBottomPoint());
    }
  }

  /**
   * Test of getLeftMostXBoundary method, of class LineSegment2D.
   */
  @Test
  public void testGetLeftMostXBoundary() {
    System.out.println("getLeftMostXBoundary");
    ArrayList<LineSegment2D> s = getTestSegments();
    double[] expected = {0, 0, -10, 0, 0, 0, -10, -10};
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Testing"+seg.printPointsToString()+", expected "+expected[i]+" but got "+seg.getLeftMostXBoundary(), expected[i], seg.getLeftMostXBoundary(), Calc.EPSILON);
    }
  }

  /**
   * Test of getRightMostXBoundary method, of class LineSegment2D.
   */
  @Test
  public void testGetRightMostXBoundary() {
    System.out.println("getRightMostXBoundary");
    ArrayList<LineSegment2D> s = getTestSegments();
    double[] expected = {10, 0, 0, 0, 10, 10, 0, 0};
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Test value equals for "+i, expected[i], seg.getRightMostXBoundary(), Calc.EPSILON);
    }
  }


  /**
   * Test of getGreaterYBoundary method, of class LineSegment2D.
   */
  @Test
  public void testGetGreaterYBoundary() {
    System.out.println("getGreaterYBoundary");
    ArrayList<LineSegment2D> s = getTestSegments();
    double[] expected = {0, 10, 0, 0, 10, 0, 0, 10};
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Test value equals for "+i, expected[i], seg.getGreaterYBoundary(), Calc.EPSILON);
    }
  }

  /**
   * Test of getLesserYBoundary method, of class LineSegment2D.
   */
  @Test
  public void testGetLesserYBoundary() {
    System.out.println("getLesserYBoundary");
    ArrayList<LineSegment2D> s = getTestSegments();
    double[] expected = {0, 0, 0,-10, 0,-10,-10, 0};
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertEquals("Test value equals for "+i, expected[i], seg.getLesserYBoundary(), Calc.EPSILON);
    }
  }
  
  /**
   * Test of containsPoint method, of class LineSegment2D.
   */
  @Test
  public void testContainsPoint() {
    System.out.println("containsPoint");
    ArrayList<LineSegment2D> s = getTestSegments();
    Point[] e = getTestPoints();
    Point[] p = {new Point(8, 0),
                 new Point(0, 2),
                 new Point(-4,0),
                 new Point(0,-1),
                 new Point(0.2,0.2),
                 new Point(9,-9),
                 new Point(-1,-1),
                 new Point(-5, 5)};
    
    Point pOut = new Point(Calc.EPSILON*4, -Calc.EPSILON*2);
    
    LineSegment2D seg;
    for(int i = 0; i < s.size(); i++) {
      seg = s.get(i);
      assertFalse("Test outside point "+i, seg.containsPoint(pOut));
      assertTrue("Test endpoint "+i, seg.containsPoint(e[i+1]));
      assertTrue("Test origin point "+i, seg.containsPoint(e[0]));
      assertTrue("Test inner point "+i, seg.containsPoint(p[i]));
    }
  }
  
  private Point[] getTestPoints() {
    Point[] ps = {
              new Point(  0,  0),
              new Point( 10,  0),
              new Point(  0, 10),
              new Point(-10,  0),
              new Point(  0,-10),
              new Point( 10, 10),
              new Point( 10,-10),
              new Point(-10,-10),
              new Point(-10, 10)};
            
    return ps;
  }
  
  private ArrayList<LineSegment2D> getTestSegments() {

    Point[] p = getTestPoints();
    ArrayList<LineSegment2D> segments = new ArrayList<>();
    // All segments have point A at origin.
    segments.add(new LineSegment2D(p[0], p[1])); // horizontal segment positive x
    segments.add(new LineSegment2D(p[0], p[2])); // vertical segment positive y
    segments.add(new LineSegment2D(p[0], p[3])); // horizontal segment negative x
    segments.add(new LineSegment2D(p[0], p[4])); // vertical segment negative y
    segments.add(new LineSegment2D(p[0], p[5])); // diagonal I Quadrant
    segments.add(new LineSegment2D(p[0], p[6])); // diagonal IV Quadrant
    segments.add(new LineSegment2D(p[0], p[7])); // diagonal III Quadrant
    segments.add(new LineSegment2D(p[0], p[8])); // diagonal II Quadrant
    
    return segments;    
  }

  /**
   * Test of contains method, of class LineSegment2D.
   */
//  @Test
  public void testContains() {
    System.out.println("contains");
    Point[] p = getTestPoints();
    Point[] x = { new Point( 5, 0),
                  new Point( 0, 5),
                  new Point(-5, 0),
                  new Point( 0,-5),
                  new Point( 5, 5),
                  new Point(-5, 5),
                  new Point(-5,-5),
                  new Point( 5,-5)
    };
    ArrayList<LineSegment2D> s = getTestSegments();
    boolean[] expected = {  
    };
    
    int k, j;
    int i = 0;
    for(LineSegment2D seg : s) {
      assertTrue("Test seg"+seg.printPointsToString()+" contains point "+p[0].print2DLoc(), seg.contains(p[0]));
      assertTrue("Test seg"+seg.printPointsToString()+" contains point "+p[i+1].print2DLoc(), seg.contains(p[i+1]));
      assertTrue("Test seg"+seg.printPointsToString()+" contains point "+x[i].print2DLoc(), seg.contains(x[i]));
      k = (i + 4) % 8; // Cycle 4 elements ahead on the array
      assertFalse("Test seg"+seg.printPointsToString()+" doesn't contains point "+x[k].print2DLoc(), seg.contains(x[k]));
      j = ((i+2) % 4)+(((i+1)/4)*4); // Cycle 2 elements ahead on the array for the first 4, then again for the next 4...hehe
      assertFalse("Test seg"+seg.printPointsToString()+" doesn't contains point "+x[j].print2DLoc(), seg.contains(x[j]));

    }
  }
  
  
  /**
   * Test of getPointA method, of class LineSegment2D.
   */
//  @Test
  public void testGetPointA() {
  }

  /**
   * Test of getPointB method, of class LineSegment2D.
   */
//  @Test
  public void testGetPointB() {
  }

  /**
   * Test of getPointAx method, of class LineSegment2D.
   */
//  @Test
  public void testGetPointAx() {
  }

  /**
   * Test of getPointAy method, of class LineSegment2D.
   */
//  @Test
  public void testGetPointAy() {
  }

  /**
   * Test of getPointBx method, of class LineSegment2D.
   */
//  @Test
  public void testGetPointBx() {
  }

  /**
   * Test of getPointBy method, of class LineSegment2D.
   */
//  @Test
  public void testGetPointBy() {
  }

  /**
   * Test of reorientSegment method, of class LineSegment2D.
   */
//  @Test
  public void testReorientSegment() {
  }

  /**
   * Test of equals method, of class LineSegment2D.
   */
//  @Test
  public void testEquals() {
  }

  /**
   * Test of printPointsToString method, of class LineSegment2D.
   */
//  @Test
  public void testPrintPointsToString() {
  }


  /**
   * Test of getOverlap method, of class LineSegment2D.
   */
//  @Test
  public void testGetOverlap() {
    System.out.println("getOverlap");
    AbstractLine2D other = null;
    LineSegment2D instance = null;
    Object expResult = null;
    Object result = instance.getOverlap(other);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of hashCode method, of class LineSegment2D.
   */
//  @Test
  public void testHashCode() {
  }
}

