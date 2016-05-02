<<<<<<< HEAD
/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
public final class WsdlComplexTypes implements java.io.Serializable
{

	private static final long serialVersionUID = 1L;

	private HashMap<String, ComplexType> _complexTypes = new HashMap<String, ComplexType>();

	/**
	 * Create a new instance, parse the WSDL file for named complex types.
	 *
	 * @param wsdlTypes Name space resolver.
	 */
	protected WsdlComplexTypes(WsdlTypes wsdlTypes)
	{

		List<ExtensibilityElement> schemas = wsdlTypes.getSchemas();
		for (ExtensibilityElement schema : schemas) {
			Element schemaRoot = ((Schema) schema).getElement();

			List<Element> types = DomUtils.getChildElementsByName(schemaRoot, WsdlUtils.COMPLEX_TYPE_NAME);
			for (Element t : types) {
				String schemaTypeName = t.getAttribute(WsdlUtils.NAME_ATTR);
				_complexTypes.put(schemaTypeName, new ComplexType(t, wsdlTypes));
			}
		}
	}

	/**
	 * Get the complex type specified by complexTypeName.
	 *
	 * @param complexTypeName Name of complex type.
	 * @return ComplexType instance, null if complex type was not defined in the wsdl file.
	 */
	public ComplexType getComplexType(String complexTypeName)
	{
		return _complexTypes.get(complexTypeName);
	}
=======
/**
 * Created by Giuseppe on 4/16/2014.
 */

public class TestComplex {

    public static void main(String[] args) {

        double a = 3.5;
        double b = 5.5;
        Complex c1 = new Complex(a, b);


        double c = -3.5;
        double d = 1.0;
        Complex c2 = new Complex(c, d);

        System.out.println("(" + c1 + ")" + " + " + "(" + c2 + ")" + " = " + c1.add(c2));
        System.out.println("(" + c1 + ")" + " - " + "(" + c2 + ")" + " = " + c1.subtract(c2));
        System.out.println("(" + c1 + ")" + " * " + "(" + c2 + ")" + " = " + c1.multiply(c2));
        System.out.println("(" + c1 + ")" + " / " + "(" + c2 + ")" + " = " + c1.divide(c2));
        System.out.println("|" + c1 + "| = " + c1.abs());

    }

    static class Complex  {
        private double a = 0, b = 0;

        public Complex() {
        }

        Complex(double a, double b) {
            this.a = a;
            this.b = b;
        }

        public Complex(double a) {
            this.a = a;
        }

        public double getA() {
            return a;
        }

        public double getB() {
            return b;
        }

        public Complex add(Complex secondComplex) {
            double newA = a + secondComplex.getA();
            double newB = b + secondComplex.getB();
            return new Complex(newA, newB);
        }

        public Complex subtract(Complex secondComplex) {
            double newA = a - secondComplex.getA();
            double newB = b - secondComplex.getB();
            return new Complex(newA, newB);
        }

        public Complex multiply(Complex secondComplex) {
            double newA = a * secondComplex.getA() - b * secondComplex.getB();
            double newB = b * secondComplex.getA() + a * secondComplex.getB();
            return new Complex(newA, newB);
        }

        public Complex divide(Complex secondComplex) {
            double newA = (a * secondComplex.getA() + b * secondComplex.getB())
                    / (Math.pow(secondComplex.getA(), 2.0) + Math.pow(secondComplex.getB(),
                    2.0));
            double newB = (b * secondComplex.getA() - a * secondComplex.getB())
                    / (Math.pow(secondComplex.getA(), 2.0) + Math.pow(secondComplex.getB(),
                    2.0));
            return new Complex(newA, newB);
        }

        public double abs() {
            return Math.sqrt(a * a + b * b);
        }

        @Override
        public String toString() {
            if (b != 0)
                return a + " + " + b + "i";
            return a + "";
        }
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

