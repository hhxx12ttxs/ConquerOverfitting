/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uga.ai.daverob;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author daverob
 */
public class LocusTest {
    public Double epsilon;
    public LocusTest() {
        epsilon = 0.000001;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getLatitude method, of class Locus.
     */
    @Test
    public void testGetLatitude() {
        System.out.println("Testing Locus.getLatitude().");
        Locus myLocus = new Locus(34.0,-84.0,30629,"Atlanta, GA 30606");
        Double expectedResult = 34.0;
        myLocus.setLatitude(expectedResult);
        Double actualResult = myLocus.getLatitude();
        if ((actualResult + epsilon <= expectedResult) || (actualResult - epsilon >= expectedResult))
        {
            fail("Actual result and expected result are of type Double and are not close enough together.");
        }
        return;
    }

    /**
     * Test of setLatitude method, of class Locus.
     */
    @Test
    public void testSetLatitude() {
        System.out.println("Testing Locus.setLatitude().");
        Locus myLocus = new Locus(34.0,-84.0,30629,"Atlanta, GA 30606");
        Double expectedResult = 34.0;
        myLocus.setLatitude(expectedResult);
        Double actualResult = myLocus.getLatitude();
        if ((actualResult + epsilon <= expectedResult) || (actualResult - epsilon >= expectedResult))
        {
            fail("Actual result and expected result are of type Double and are not close enough together.");
        }
        return;
    }

    /**
     * Test of getLongitude method, of class Locus.
     */
    @Test
    public void testGetLongitude() {
        System.out.println("Testing Locus.getLongitude().");
        Locus myLocus = new Locus(34.0,-84.0,30629,"Atlanta, GA 30606");
        Double expectedResult = 34.0;
        myLocus.setLongitude(expectedResult);
        Double actualResult = myLocus.getLongitude();
        if ((actualResult + epsilon <= expectedResult) || (actualResult - epsilon >= expectedResult))
        {
            fail("Actual result and expected result are of type Double and are not close enough together.");
        }
        return;
    }

    /**
     * Test of setLongitude method, of class Locus.
     */
    @Test
    public void testSetLongitude() {
        System.out.println("Testing Locus.setLongitude().");
        Locus myLocus = new Locus(34.0,-84.0,30629,"Atlanta, GA 30606");
        Double expectedResult = 34.0;
        myLocus.setLongitude(expectedResult);
        Double actualResult = myLocus.getLongitude();
        if ((actualResult + epsilon <= expectedResult) || (actualResult - epsilon >= expectedResult))
        {
            fail("Actual result and expected result are of type Double and are not close enough together.");
        }
        return;
    }

    /**
     * Test of getZipCode method, of class Locus.
     */
    @Test
    public void testGetZipCode() {
        System.out.println("Testing Locus.getZipCode()");
        Locus myLocus = new Locus(43.0,35.0,30674,"Great Stuff!");
        Integer expectedResult = new Integer(50005);
        myLocus.setZipCode(expectedResult);
        Integer actualResult = myLocus.getZipCode();
        assertEquals(expectedResult, actualResult);
        return;
    }

    /**
     * Test of setZipCode method, of class Locus.
     */
    @Test
    public void testSetZipCode() {
        System.out.println("Testing Locus.setZipCode()");
        Locus myLocus = new Locus(43.0,35.0,30574,"Great Stuff!");
        Integer expectedResult = new Integer(50009);
        myLocus.setZipCode(expectedResult);
        Integer actualResult = myLocus.getZipCode();
        assertEquals(expectedResult, actualResult);
        return;
    }

    /**
     * Test of getName method, of class Locus.
     */
    @Test
    public void testGetName() {
        System.out.println("Testing Locus.setName().");
        String expectedResult = "Tibee Island, GA";
        Locus myLocus = new Locus(99.9,99.0,99999,"Laguna Beach, CA");
        myLocus.setName(expectedResult);
        String actualResult = myLocus.getName();
        assertEquals(expectedResult, actualResult);
        return;
    }

    /**
     * Test of setName method, of class Locus.
     */
    @Test
    public void testSetName() {
        System.out.println("Testing Locus.setName().");
        String expectedResult = "The Beach, CA";
        Locus myLocus = new Locus(99.9,99.0,99999,"Laguna Beach, CA");
        myLocus.setName(expectedResult);
        String actualResult = myLocus.getName();
        assertEquals(expectedResult, actualResult);
        return;
    }

    /**
     * Test of isZipCodeValid method, of class Locus.
     */
    @Test
    public void testIsZipCodeValid() {
        System.out.println("Testing Locus.isZipCodeValid().");
        Integer zip = new Integer(99999);
        Locus myLocus = new Locus(99.0,99.0,99999,"What's up?");
        Boolean expectedResult = true;
        Boolean actualResult = myLocus.isZipCodeValid(zip);
        assertEquals(expectedResult, actualResult);
        return;
    }
}

