<<<<<<< HEAD
/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.padaf.xmpbox.type;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.TextType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test MetaData Objects for complex properties
 * 
 * @author a183132
 * 
 */
public class ComplexMetadataPropertyTest {

	protected XMPMetadata metadata;
	protected XMPSchema tmpSchem;

	@Before
	public void resetDocument() throws Exception {
		metadata = new XMPMetadata();
		tmpSchem = metadata.createAndAddDefaultSchema("test",
				"http://www.test.org/test/");

	}

	/**
	 * Check if Array building works (complexproperty)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBuildAndCompareArray() throws Exception {
		// Build a bag with one rdf:li
		ComplexProperty bag = new ComplexProperty(metadata, "test", "TESTBAG",
				ComplexProperty.UNORDERED_ARRAY);
		TextType litmp = new TextType(metadata, "rdf", "li", "TestValue");
		bag.getContainer().addProperty(litmp);
		// bag.getContainer().addProperty(new TextType(metadata.getFuturOwner(),
		// "rdf", "li", "TestValue"));

		Assert
				.assertTrue(bag.getContainer().getAllProperties().contains(
						litmp));
		// Assert.assertEquals(litmp.getElement(),
		// bag.getContainer().getElement().getFirstChild());

		// Build a bag with 2 rdf:li
		ComplexProperty seq = new ComplexProperty(metadata,
				"http://www.test.org/test/", "test", "TESTSEQNS",
				ComplexProperty.ORDERED_ARRAY);
		TextType li1 = new TextType(metadata, "rdf", "li", "TestValue1");
		TextType li2 = new TextType(metadata, "rdf", "li", "TestValue2");
		seq.getContainer().addProperty(li1);
		seq.getContainer().addProperty(li2);

		// Comparing content
		Assert.assertTrue(seq.isSameProperty(seq));
		Assert.assertFalse(seq.isSameProperty(bag));

		ComplexProperty seqBis = new ComplexProperty(metadata,
				"http://www.test.org/test/", "test", "TESTSEQNS",
				ComplexProperty.ORDERED_ARRAY);
		TextType lis1 = new TextType(metadata, "rdf", "li", "TestValue");
		seqBis.getContainer().addProperty(lis1);
		Assert.assertFalse(seq.isSameProperty(seqBis));

		tmpSchem.addProperty(bag);
		tmpSchem.addProperty(seq);
		// SaveMetadataHelper.serialize(metadata, true, System.out);
	}

	/**
	 * Check if Complex property container building works (used directly for
	 * complex rdf:li)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBuildingComplexRDFLi() throws Exception {
		// Build a bag with one rdf:li
		ComplexPropertyContainer complexLi = new ComplexPropertyContainer(
				metadata, "http://www.test.org/test/", "rdf", "li");

		TextType li1 = new TextType(metadata, "test", "value1", "ValueOne");
		TextType li2 = new TextType(metadata, "test", "value2", "ValueTwo");
		TextType li3 = new TextType(metadata, "test", "value3", "ValueThree");

		complexLi.addProperty(li1);
		// Test removing during adding
		complexLi.addProperty(li1);
		complexLi.addProperty(li2);
		complexLi.addProperty(li3);

		// Test contains checking
		Assert.assertTrue(complexLi.containsProperty(li1));
		complexLi.removeProperty(li1);
		Assert.assertFalse(complexLi.containsProperty(li1));

		tmpSchem.addProperty(complexLi);
		// SaveMetadataHelper.serialize(metadata, true, System.out);
	}

	/**
	 * Throw BadFieldValueException
	 * 
	 * @throws BadFieldValueException
	 */
	@Test(expected = BadFieldValueException.class)
	public void testBadFieldValueExceptionWithCause() throws Exception {
		throw new BadFieldValueException("TEST", new Throwable());
	}

	/**
	 * Throw BadFieldValueException
	 * 
	 * @throws BadFieldValueException
	 */
	@Test(expected = BadFieldValueException.class)
	public void badFieldValuetestException() throws Exception {
		throw new BadFieldValueException("TEST");
	}

}

=======
/*
 * Created on Feb 23, 2006
 */
package de.torstennahm.math;


public class Complex {
	private final double r;
	private final double i;
	
	public static final Complex NaN = new Complex(Double.NaN, Double.NaN);
	
	public Complex(double r) {
		this.r = r;
		this.i = 0.0;
	}
	
	public Complex(double r, double i) {
		this.r = r;
		this.i = i;
	}
	
	public Complex add(Complex c) {
		return new Complex(r + c.r, i + c.i);
	}
	
	public Complex sub(Complex c) {
		return new Complex(r - c.r, i - c.i);
	}

	public Complex mul(Complex c) {
		return new Complex(r * c.r - i * c.i, i * c.r + r * c.i);
	}
	
	public Complex mul(double a) {
		return new Complex(r * a, i * a);
	}
	
	public Complex div(Complex c) {
		double d = 1.0 / (c.r * c.r + c.i * c.i);
		return new Complex((r * c.r + i * c.i) * d, (i * c.r - r * c.i) * d);
	}
	
	public Complex div(double a) {
		double inv = 1.0 / a;
		return new Complex(r * inv, i * inv);
	}
	
	public double re() {
		return r;
	}
	
	public double im() {
		return i;
	}
	
	public Complex conjugate() {
		return new Complex(r, -i);
	}
	
	public Complex neg() {
		return new Complex(-r, -i);
	}
	
	public double abs() {
		return Math.sqrt(r*r + i*i);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Complex) {
			Complex c = (Complex) o;
			return c.r == r && c.i == i;
		} else {
			return false;
		}
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
