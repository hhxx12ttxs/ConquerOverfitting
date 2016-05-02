<<<<<<< HEAD
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.webservices.wsdl;

import java.util.HashMap;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;

import org.w3c.dom.Element;

/**
 * Represents a map of all named complex types in the WSDL.
 */
public final class WsdlComplexTypes implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private HashMap<String, ComplexType> _complexTypes = new HashMap<String, ComplexType>();

  /**
   * Create a new instance, parse the WSDL file for named complex types.
   *
   * @param wsdlTypes
   *          Name space resolver.
   */
  protected WsdlComplexTypes( WsdlTypes wsdlTypes ) {

    List<ExtensibilityElement> schemas = wsdlTypes.getSchemas();
    for ( ExtensibilityElement schema : schemas ) {
      Element schemaRoot = ( (Schema) schema ).getElement();

      List<Element> types = DomUtils.getChildElementsByName( schemaRoot, WsdlUtils.COMPLEX_TYPE_NAME );
      for ( Element t : types ) {
        String schemaTypeName = t.getAttribute( WsdlUtils.NAME_ATTR );
        _complexTypes.put( schemaTypeName, new ComplexType( t, wsdlTypes ) );
      }
    }
  }

  /**
   * Get the complex type specified by complexTypeName.
   *
   * @param complexTypeName
   *          Name of complex type.
   * @return ComplexType instance, null if complex type was not defined in the wsdl file.
   */
  public ComplexType getComplexType( String complexTypeName ) {
    return _complexTypes.get( complexTypeName );
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
