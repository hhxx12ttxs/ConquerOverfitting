/**
 * Copyright (c) 2006-2013 Berlin Brown. All Rights Reserved
 *
 * http://www.opensource.org/licenses/bsd-license.php

 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * * Neither the name of the Botnode.com (Berlin Brown) nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * File : 
 * Date: 4/25/2012
 * bbrown
 * Contact: Berlin Brown <berlin dot brown at gmail.com>
 * 
 * Description - Google App Engine Proof of Concept Appliations, Mathservices and Wicket And Spring/JQuery, 
 * 
 * https://github.com/berlinbrown
 * http://code.google.com/p/javanotebook/
 * http://code.google.com/p/jvmnotebook/ 
 */

/**
 * For source:
 * 
 * https://github.com/berlinbrown/physicsforprogrammers/tree/master/projects/MathServices
 * 
 * Also see:
 * 
 * https://github.com/berlinbrown
 * http://berlinbrown.github.com/
 * http://berlinbrown.github.com/applets.html
 */
package org.berlin.math.demo;

/*
 * Pngj PNG writer used with Google App Engine.
 * 
 * From pngj:
 * http://code.google.com/p/pngj/
 * 
 */

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.PngFilterType;
import ar.com.hjg.pngj.PngWriter;

/**
 * Basic math utilities for google app engine.
 * Render the mandelbrot fractal.
 * 
 * @author bbrown (berlin.brown at gmail.com)
 *
 */
public class ImageDemoMandelbrotServlet extends HttpServlet {
	 
	/**
	 * Serial version id. 
	 */
	private static final long serialVersionUID = 1L;
	
	public static class Complex {
	    private final double re;
	    private final double im;
	    public Complex(double real, double imag) {
	        re = real;
	        im = imag;
	    }
	    public String toString() {
	        if (im == 0) return re + "";
	        if (re == 0) return im + "i";
	        if (im <  0) return re + " - " + (-im) + "i";
	        return re + " + " + im + "i";
	    }	  
	    public double abs()   { return Math.hypot(re, im); }  // Math.sqrt(re*re + im*im)
	    public double phase() { return Math.atan2(im, re); }
	    public Complex plus(Complex b) {
	        Complex a = this;             // invoking object
	        double real = a.re + b.re;
	        double imag = a.im + b.im;
	        return new Complex(real, imag);
	    }
	    public Complex minus(Complex b) {
	        Complex a = this;
	        double real = a.re - b.re;
	        double imag = a.im - b.im;
	        return new Complex(real, imag);
	    }
	    public Complex times(Complex b) {
	        Complex a = this;
	        double real = a.re * b.re - a.im * b.im;
	        double imag = a.re * b.im + a.im * b.re;
	        return new Complex(real, imag);
	    }
	    public Complex times(double alpha) {
	        return new Complex(alpha * re, alpha * im);
	    }
	    public Complex conjugate() {  return new Complex(re, -im); }
	    public Complex reciprocal() {
	        double scale = re*re + im*im;
	        return new Complex(re / scale, -im / scale);
	    }
	    public double re() { return re; }
	    public double im() { return im; }
	    public Complex divides(Complex b) {
	        Complex a = this;
	        return a.times(b.reciprocal());
	    }
	    public Complex exp() {
	        return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
	    }
	    public Complex sin() {
	        return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
	    } 
	    public Complex cos() {
	        return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
	    }
	    public Complex tan() {
	        return sin().divides(cos());
	    }	
	    public static Complex plus(Complex a, Complex b) {
	        double real = a.re + b.re;
	        double imag = a.im + b.im;
	        Complex sum = new Complex(real, imag);
	        return sum;
	    }
	    public static void test(String[] args) {
	        Complex a = new Complex(5.0, 6.0);
	        Complex b = new Complex(-3.0, 4.0);

	        System.out.println("a            = " + a);
	        System.out.println("b            = " + b);
	        System.out.println("Re(a)        = " + a.re());
	        System.out.println("Im(a)        = " + a.im());
	        System.out.println("b + a        = " + b.plus(a));
	        System.out.println("a - b        = " + a.minus(b));
	        System.out.println("a * b        = " + a.times(b));
	        System.out.println("b * a        = " + b.times(a));
	        System.out.println("a / b        = " + a.divides(b));
	        System.out.println("(a / b) * b  = " + a.divides(b).times(b));
	        System.out.println("conj(a)      = " + a.conjugate());
	        System.out.println("|a|          = " + a.abs());
	        System.out.println("tan(a)       = " + a.tan());
	    }

	} // End of complex class //
	
    public static int mandelbrot(final Complex z0, final int max) {
        Complex z = z0;
        for (int t = 0; t < max; t++) {
            if (z.abs() > 2.0) {
            	return t;
            }
            z = z.times(z).plus(z0);
        }
        return max;
    }	
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {	
		doPost(request, response);
	} // End of the class //
	
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {				
		response.setContentType("image/png");		
				
		final int cols = 380;
		final int rows = 380;		
		final int max = 78;
		final int N = cols;
		
		final OutputStream os = response.getOutputStream();
		final PngWriter png = new PngWriter(os, new ImageInfo(cols, rows, 8, false));
		png.setFilterType(PngFilterType.FILTER_NONE);
		final ImageLine iline1 = new ImageLine(png.imgInfo);		
        final double xc   = Double.parseDouble("-0.5");
        final double yc   = Double.parseDouble("0");
        final double size = Double.parseDouble("2");	
		for (int i = 0; i < rows; i++) {						
			for (int j = 0; j < cols; j++) {			
				final double x0 = xc - size / 2 + size*i/N;
	            double y0 = yc - size / 2 + size*j/N;
	            Complex z0 = new Complex(x0, y0);
	            final int grayx = (max - mandelbrot(z0, max));
	            final int gray = (int)Math.min(Math.floor(grayx * 2.0), 254);
	            ImageLineHelper.setPixelRGB8(iline1, j, gray, gray, gray);						
			} // End of the for //			
			iline1.setRown(i);
			png.writeRow(iline1);
		}
		png.end();	
	} // End of the class //

} // End of the class //
