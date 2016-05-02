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
=======
package electricUtils;

import mathUtils.Complex;
import mathUtils.MathUtils;

public class ElectricUtils {
	public static Complex getEquivalentResistance(Complex resistance1, Complex resistance2, 
			boolean areAlongside) {
		if (areAlongside) {
			Complex numerator = MathUtils.ComplexUtils.multiplyComplex(resistance1, resistance2);
			Complex denominator = MathUtils.ComplexUtils.sumComplex(resistance1, resistance2);
			
			return MathUtils.ComplexUtils.divideComplex(numerator, denominator);
		} else {
			return MathUtils.ComplexUtils.sumComplex(resistance1, resistance2);
		}
	}
	
	public static Complex getElectricityByTevenen(Complex voltage, Complex equivalentResistance, 
			Complex currentResistance) {
		Complex denominator = MathUtils.ComplexUtils.sumComplex(equivalentResistance, currentResistance);
		
		return MathUtils.ComplexUtils.divideComplex(voltage, denominator);
	}
	
	public static Complex getElectricityByNorton(Complex shortCircuitElectricity, Complex equivalentResistance, 
			Complex currentResistance) {
		Complex numerator = MathUtils.ComplexUtils.multiplyComplex(shortCircuitElectricity, equivalentResistance);
		Complex denominator = MathUtils.ComplexUtils.sumComplex(equivalentResistance, currentResistance);
		
		return MathUtils.ComplexUtils.divideComplex(numerator, denominator);
	}
	
	public static Complex getVoltageFromElectricityAndResistance(Complex electricity, Complex resistance) {
		return MathUtils.ComplexUtils.multiplyComplex(electricity, resistance);
	}
	
	public static Complex getResistanceFromElectricityVoltageAndPower(
			Complex electricity, Complex voltage, Complex power) {
		double angle = Math.acos(
				power.getModule() / 
				(electricity.getModule() * voltage.getModule()));
		double cosineValue = Math.cos(angle);
		double sineValue = Math.sin(angle);
		double resistance = MathUtils.ComplexUtils.divideComplex(voltage, electricity).getModule();
		
		return new Complex(resistance * cosineValue, resistance * sineValue);
	}
	
	public static Complex getPowerFromVoltageAndElectricity(Complex voltage, Complex electricity) {
		return MathUtils.ComplexUtils.multiplyComplex(voltage, electricity.getComplexConjugate());
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

