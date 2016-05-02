<<<<<<< HEAD
/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link DrugOrderValidator} class.
 */
public class DrugOrderValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if prn is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPrnIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setPrn(null);
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("prn"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if complex is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfComplexIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setComplex(null);
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("complex"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should not fail validation if drug is null", method = "validate(Object,Errors)")
	public void validate_shouldNotFailValidationIfDrugIsNull() throws Exception {
		DrugOrder order = new DrugOrder();
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("drug"));
	}
	
	/**
	 * @see {@link DrugOrderValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		DrugOrder order = new DrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderType(Context.getOrderService().getOrderType(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setStartDate(cal.getTime());
		order.setDiscontinuedDate(new Date());
		order.setAutoExpireDate(new Date());
		order.setDrug(Context.getConceptService().getDrug(3));
		
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
=======

package com.snuggy.nr.util;

import static java.lang.Math.*;

import com.snuggy.nr.refs.*;

public class Complex implements ByValue<Complex> {
    
    private double real, imag;

    public Complex() {
        this.real = 0.0;
        this.imag = 0.0;
    }
    
    private Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }
    
    public static boolean equal(Complex x, Complex y) {
        return x.real == y.real && x.imag == y.imag;
    }
    
    public static double norm2(Complex x) {
        return x.real * x.real + x.imag * x.imag;
    }
    
    public static double norm(Complex x) {
        return java.lang.Math.sqrt(norm2(x));
    }
    
    public static double real(Complex x) {
        return x.real;
    }
    
    public static double imag(Complex x) {
        return x.imag;
    }
    
    public static Complex complex() {
        return complex(0.0, 0.0);
    }
    
    public static Complex complex(double real) {
        return complex(real, 0.0);
    }

    public static Complex complex(double real, double imag) {
        Complex r = new Complex(real, imag);
        return r;
    }
    
    public static Complex complex(Complex x) {
        if (x == null)
            System.out.println("moo");
        return complex(x.real, x.imag);
    }
    
    public static double abs(Complex z) {
        double x = z.real;
        double y = z.imag;
        
        double absX =  Math.abs(x);
        double absY =  Math.abs(y);

        if (absX == 0.0 && absY == 0.0) {                                      // !!! Numerical Recipes, mmm?
            return  0.0;
        } else if (absX >= absY) {
            double d =  y / x;
            return  absX*Math.sqrt(1.0 + d*d);
        } else {
            double d =  x / y;
            return  absY*Math.sqrt(1.0 + d*d);
        }
    }
    
    public static Complex plus(Complex x, double y) {
        return complex(x.real + y, x.imag);
    }
    
    public static Complex plus(Complex x, Complex y) {
        return complex(x.real + y.real, x.imag + y.imag);
    }
    
    public static Complex plus(Complex x, Complex y, Complex z) {
        return complex(x.real + y.real + z.real, x.imag + y.imag + z.imag);
    }
    
    public static Complex plus(Complex x, Complex y, double z) {
        return complex(x.real + y.real + z, x.imag + y.imag);
    }
    
    public static Complex conj(Complex x) {
        return complex(x.real, -x.imag);
    }
    
    public static Complex minus(Complex x) {
        return complex(-x.real, -x.imag);
    }
    
    public static double minus(double x) {
        return -x;
    }
    
    public static Complex minus(double x, Complex y) {
        return complex(x - y.real, -y.imag);
    }
    
    public static Complex minus(Complex x, double y) {
        return complex(x.real - y, x.imag);
    }
    
    public static Complex minus(Complex x, Complex y) {
        return complex(x.real - y.real, x.imag - y.imag);
    }
    
    public static Complex times(Complex x, Complex y, Complex z) {
        return times(x, times(y, z));
    }
    
    public static Complex times(Complex x, Complex y) {
        double a = x.real;
        double b = x.imag;
        double c = y.real;
        double d = y.imag;
        return complex(a * c - b * d, b * c + a * d);
    }

    public static Complex times(double x, Complex y) {
        double a = x;
        double c = y.real;
        double d = y.imag;
        return complex(a * c, a * d);
    }

    public static double times(double x, double y) {
        return x * y;
    }

    public static Complex times(Complex x, double y) {
        double a = x.real;
        double b = x.imag;
        double c = y;
        return complex(a * c, b * c);
    }

    public static Complex divide(Complex x, Complex y) {
        double a = x.real;
        double b = x.imag;
        double c = y.real;
        double d = y.imag;
        double h2 = c * c + d * d;
        double real = (a * c + b * d) / h2;
        double imag = (b * c - a * d) / h2;
        return complex(real, imag);
    }

    public static Complex divide(double x, Complex y) {
        double a = x;
        double c = y.real;
        double d = y.imag;
        double h2 = c * c + d * d;
        double real = (a * c) / h2;
        double imag = (-a * d) / h2;
        return complex(real, imag);
    }

    public static double divide(double x, double y) {
        return x / y;
    }

    public static Complex divide(Complex x, double y) {
        double a = x.real;
        double b = x.imag;
        double c = y;
        double h2 = c * c;
        double real = (a * c) / h2;
        double imag = (b * c) / h2;
        return complex(real, imag);
    }

    public static double divide(int x, double y) {
        return x / y;
    }

    public static double divide(double x, int y) {
        return x / y;
    }

    public static Complex sqrt(Complex z) {
        z = complex(z);
        double mag =  abs(z);
        if (mag > 0.0) {
            if (z.real > 0.0) {
                double temp =  Math.sqrt(0.5 * (mag + z.real));
                z.real =  temp;
                z.imag =  0.5 * z.imag / temp;
            } else {
                double temp =  Math.sqrt(0.5 * (mag - z.real));
                if (z.imag < 0.0) {
                    temp =  -temp;
                }
                z.real =  0.5 * z.imag / temp;
                z.imag =  temp;
            }
        } else {
            z.real =  0.0;
            z.imag =  0.0;
	    }
        return z;
    }
    
    public static Complex polar(final double _Rho, final double _Theta)
    {   // return _Rho * exp(i * _Theta) as complex
	    return complex(
	            _Rho * cos(_Theta),
		        _Rho * sin(_Theta)
		       );
    }

    @Override
    public void copyIn(Complex t) {
        this.real = t.real;
        this.imag = t.imag;
    }

    @Override
    public Complex copyOut() {
        Complex r = new Complex(this.real, this.imag);
        return r;
    }
    
    @Override
    public String toString() {
        return "(" + real + " + " + imag + "i)";
    }

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

