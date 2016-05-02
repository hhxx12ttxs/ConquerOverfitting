<<<<<<< HEAD
/*
 *  Copyright (C) 2010 GSyC/LibreSoft, Universidad Rey Juan Carlos.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/. 
 *
 *  Author : Raúl Román López <rroman@gsyc.es>
 *
 */
package com.libresoft.apps.ARviewer.Utils;


public class CompassController{
	
	private static final float STDEV = 4.3351f;
	private static final int MAX_RANGE = 20;
	
	private float Kp;
	private float last_value = 0; 
	private double last_error = 0;
	private boolean complex = false;
	
	
	public CompassController(float Kp, boolean complex){
		this.Kp = Kp;
		this.complex = complex;
	}
	
	public float getValue(float new_value){
		if(Math.abs(new_value) > 360)
			return last_value;
		
		float error = new_value - last_value;
		if (error > 180)
			error = error - 360;
		else if (error < (-180))
			error = error + 360;
		
		
//		integral += (error*DT);
//		float derivative = (error - last_error)/DT;
//		float value = Kp * error + Kd * derivative + Ki * integral;
//		last_error = error;
		
		float K = Kp;
		if (complex){
			if(Math.abs(error) < MAX_RANGE){
				double calc = Math.pow(MAX_RANGE + 1 - Math.abs(error), 2);
				K = (float) (((calc-1)/Math.pow(2, last_error) + 1) * 1/calc) * Kp;
				if(Math.abs(error) < STDEV)
					last_error = Math.min(20, last_error + 1);
				else
					last_error = Math.max(0, last_error - 1);
			}else
				last_error = 0;
		}
		
		float value = K * error;
		
		value += last_value;
		if(value > 360)
			value = value - 360;
		else if(value < 0)
			value = 360 + value;
		
		last_value = value;
		return value;
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
>>>>>>> 76aa07461566a5976980e6696204781271955163
}
