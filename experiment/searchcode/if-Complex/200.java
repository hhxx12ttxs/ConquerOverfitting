/*
    fracgen-c : Fractal Generator (using concurrency)
    Copyright (C) 2010 Arpit Sud, Sri Teja Basava & Sidartha Gracias

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package util;


public class Complex 
{
	private double r;
	private double i;
	
	public Complex()
	{
		r = 0.0;
		i = 0.0;
	}
	
	public Complex(double real, double imaginary) 
	{
		 r = real;
		 i = imaginary;
	}
	
	public double real() 
	{
		return r;
	}
	
	public double imaginary() 
	{
		return i;
	}
	
	public Complex add(Complex c) 
	{
		return new Complex(r + c.r, i + c.i);
	}
	
	public Complex multiply(Complex c) 
	{
		return new Complex((r*c.r - i*c.i), (i*c.r +r*c.i));
	}
	
	public double magnitude() 
	{
		return Math.sqrt(r*r + i*i);
	}
	
	public String toString()
	{
		if(i>=0)
			return (r +"+" + Math.abs(i) +"j");
		else
			return (r +"-" + Math.abs(i) +"j");
	}
}
