/*
<h2>Copyright</h2>
Copyright (c) 2005-2010 Interworld Transport.  All rights reserved.<br>
--------------------------------------------------------------------------------
<br>
---com.interworldtransport.clados.Complex<br>
--------------------------------------------------------------------------------
<p>
Interworld Transport grants you ("Licensee") a license to this software
under the terms of the GNU General Public License.<br>
A full copy of the license can be found bundled with this package or code file.
<p>
If the license file has become separated from the package, code file, or binary
executable, the Licensee is still expected to read about the license at the
following URL before accepting this material.
<blockquote><code>http://www.opensource.org/gpl-license.html</code></blockquote>
<p>
Use of this code or executable objects derived from it by the Licensee states
their willingness to accept the terms of the license.
<p>
A prospective Licensee unable to find a copy of the license terms should contact
Interworld Transport for a free copy.
<p>
--------------------------------------------------------------------------------
<br>
---com.interworldtransport.clados.Complex<br>
--------------------------------------------------------------------------------
*/
package com.interworldtransport.clados;

/**
 * This class implements the concept of a Complex Field from mathematics.   
 * Field objects within the clados package are used as 'numbers' in the 
 * definition of an algebra.  All CladosObjects use FieldObjects as a result.  
 * 
 * Field Objects are not named.  They do not have any geometric properties.
 * Treat them like you would any other number you could plug into a simple 
 * calculator.
 * 
 * There is no doubt that the overhead related to this class is a waste of 
 * resources.  However, it allows one to plug fields into the algebra classes
 * without having to maintain many different types of monads and nyads.  If 
 * Java came with primitive types for complex and quaternion fields, and other
 * primitives implemented a 'Field' interface, I wouldn't bother writing 
 * this object or any of the other decendents of FieldObject.
 * 
 * @version 0.80, $Date: 2010/09/07 04:22:49 $
 * @author Dr Alfred W Differ
 */
public class Complex extends FieldObject
{
/**
 *  Real part of the complex number.
 */
	public double real;
/**
 *  Imaginary part of the complex number.
 */
	public double img;
	
	
	
/** 
 * Basic Constructor with no values to initialize. 
 */
	public Complex()
	{
		FieldType="Complex";
		real=Double.NaN; 
		img=Double.NaN;
	}
/** 
 * Basic Constructor that initializes the values from another complex number. 
 */
	public Complex(Complex z) 
	{
		FieldType="Complex";
		real = z.real; 
		img = z.img;
	}
/** 
 * Basic Constructor that initializes the values from another complex number. 
 */
	public Complex(FieldObject cvalue) 
	{
		if (cvalue.getFieldType().equals("Complex"))
		{
			FieldType="Complex";
			Complex z=(Complex)cvalue;
			real = z.real; 
			img = z.img;
		}
		//if (cvalue.getFieldType().equals("Real"))
		//{
		//	Real z=(Real)cvalue;
		//	real = z.real; 
		//	img = 0.0;
		//}
	}
/** 
 * Basic Constructor that initializes the real value. 
 */
	public Complex(double pr) 
	{
		FieldType="Complex";
		real = pr; 
		img = 0.0;
	}	
/** 
 * Basic Constructor that initializes the values. 
 */
	public Complex(double pr, double pi) 
	{
		FieldType="Complex";
		real = pr; 
		img = pi;
	}
/** 
 * Basic Constructor that initializes the values. 
 */
	public Complex(String ptype, double pl, double parg) 
	{
		FieldType="Complex";
		if (ptype.equals("polar"))
		{
			real = pl*Math.cos(parg);
			img = pl*Math.sin(parg);
		}
		else
		{
			real = pl; 
			img = parg;
		}
	}
/** 
 * Construct complex on a text representation. 
 * input format  (real_double,imaginary_double) 
 * @param	s	String
 */
	public Complex(String s)
	{
		FieldType="Complex";
		int from = s.indexOf('(');
		int to = s.indexOf(',',from);
		real = Double.parseDouble(s.substring(from+1,to));
		from = to;
		to = s.indexOf(')',from);
		img = Double.parseDouble(s.substring(from+1,to));
	}
	
/** 
 * Get method for real part.
 * @return double 
 */
	public double getReal() 
	{
		return real;
	}
/** 
 * Get method for imaginary part.
 * @return double 
 */
	public double getImg() 
	{
		return img;
	}
/** 
 * Get method for magnitude of the complex number.
 * @return double 
 */
	public double getModulus() 
	{
		return Math.sqrt(real*real+img*img);
	}
/** 
 * Get method for the argument of the complex number.  This function uses the 
 * arctangent function, so its range and domain are the same.
 * @return double 
 */
	public double getArgument() 
	{
		return Math.atan(img/real);
	}
 
/**
 * Check for the equality of this object with that of the argument.
 * @param cvalue				FieldObject
 * @return boolean <tt>true</tt> if both components are the same; <tt>false</tt>, otherwise.
 */
	public boolean equals(FieldObject cvalue) 
	{
		if (this.isTypeMatch(cvalue))
		{
			Complex z = (Complex)cvalue;
			return ( (real == z.real) && (img  == z.img) ) ;
		}
		else return false;
	}
/**
 * Check for equality of this object with that of the argument to within some 
 * tolerance factors.
 * @param cvalue		FieldObject
 * @param rtol			double
 * @param atol			double
 * @return boolean <tt>true</tt> if both components are the same; <tt>false</tt>, otherwise.
 */	
	public boolean equals(FieldObject cvalue, double rtol, double atol) 
	{
		if (this.isTypeMatch(cvalue))
		{
			Complex z = (Complex)cvalue;
			return  (getModulus()-z.getModulus()) <= Math.abs(rtol) && 
					getArgument()-z.getArgument() <= Math.abs(atol);
		}
		else return false;
	}

/**
 * Check to see if this complex number is zero.
 * @return boolean <tt>true</tt> if both components are zero; <tt>false</tt>, otherwise.
 */
	public  boolean isZero()
	{
		return ( (real==0.0) && (img==0.0) );
	}
/**
 * Check to see if this complex number is infinite.
 * @return boolean <tt>true</tt> if either component is infinite; <tt>false</tt>, otherwise.
 */
	public boolean isInfinite () 
	{
        return  ( Double.isInfinite(real) || Double.isInfinite(img) );
    }
/**
 * Check to see if this complex number is infinite.
 * @return boolean <tt>true</tt> if either component is <tt>NaN</tt>; <tt>false</tt>, otherwise.
 */
	public boolean isNaN() 
	{
        return  ( Double.isNaN(real) || Double.isNaN(img) );
    }
	
/**
 * Conjugate method flips the sign on the imaginary part
 */
	public void conjugate()
	{
		img *= -1;
	}
/** 
 * Define a complex add method. 
 * @param 	cvalue				Complex
 * @throws 						FieldBinaryException 
 */
	public void add(FieldObject cvalue)
	{
		Complex z=null;
		if (this.isTypeMatch(cvalue)) z = (Complex)cvalue;
		else z = cvalue.toComplex();
		real += z.real;
		img  += z.img;
		
	}
/** 
 * Add method for tacking on a real number.
 * @param pvalue	double
 */
	public void add(double pvalue) 
	{
		real = real + pvalue;
	}
/** 
 * Define a complex subtract method.
 * @param pvalue	Complex 
 */
	public void subtract(FieldObject pvalue) 
	{
		Complex z=null;
		if (this.isTypeMatch(pvalue)) z = (Complex)pvalue;
		else z = pvalue.toComplex();
		real -= z.real;
		img  -= z.img;
		
	}
/** 
 * Subtract method for taking off a real number.
 * @param pvalue	double
 */
	public void subtract(double pvalue) 
	{
		real = real - pvalue;
	}
/**
 * Multiply this complex object by the complex argument.
 * @param pvalue	Complex 
 */
	public void multiply(FieldObject pvalue) 
	{
		Complex z=null;
		if (this.isTypeMatch(pvalue)) z = (Complex)pvalue;
		else z = pvalue.toComplex();
		double r2 = real * z.real - img * z.img;
		double i2 = real * z.img  + img * z.real;
		real = r2;
		img  = i2;
	}
/**
 * Multiply this complex object by a double.
 * @param pvalue	double 
 */
	public void multiply(double pvalue) 
	{
		real *= pvalue;
		img *= pvalue;
	}
/**
 * Divide this complex object by the complex argument.
 * @param pvalue	Complex 
 */
	public void divide(FieldObject pvalue) 
	{
		Complex z=null;
		if (this.isTypeMatch(pvalue)) z = (Complex)pvalue;
		else z = pvalue.toComplex();
		
		double denom = z.real * z.real + z.img  * z.img;
		double r = real * z.real + img  * z.img;
		double i = img  * z.real - real * z.img;
		real = r/denom;
		img  = i/denom;	
	}
/**
 * Divide this complex object by a double.
 * @param pvalue	double 
 */
	public void divide(double pvalue) 
	{
		real /= pvalue;
		img /= pvalue;			
	}	
	
/**
 * Static method that creates a new Complex with the conjugate of the parameter.
 * @param pvalue	Complex
 */
	public static Complex conjugate(Complex pvalue)
	{
		return new Complex(pvalue.real, -1.0*pvalue.img);
	}
/**
 * Static add method that creates a new Complex with the sum.
 * @param pvalue1	Complex
 * @param pvalue2	Complex
 */
	public static Complex add(Complex pvalue1, Complex pvalue2) 
	{
		double r = pvalue1.real + pvalue2.real;
		double i = pvalue1.img  + pvalue2.img;
		return new Complex(r,i);
	}

/** 
 * Define a static subtract method that createsa new Complex equal to
 * cvalue1 - cvalue2.
 * @param pvalue1	Complex
 * @param pvalue2	Complex  
 */
	public static Complex subtract(Complex pvalue1, Complex pvalue2) 
	{
		double r = pvalue1.real - pvalue2.real;
		double i = pvalue1.img  - pvalue2.img;
		return new Complex(r,i);
	}
 
/** 
 * Define a static multiply method that createsa new Complex with the 
 * product.
 * @param pvalue1	Complex
 * @param pvalue2	Complex 
 */
	public static Complex multiply(Complex pvalue1, Complex pvalue2) 
	{
		double r2 = pvalue1.real * pvalue2.real - pvalue1.img * pvalue2.img;
		double i2 = pvalue1.img  * pvalue2.real + pvalue1.real  * pvalue2.img;
		return new Complex(r2, i2);
	}

/**
 * Define a static divide method that creates a new Complex with the 
 * result of cvalue1/cvalue2.
 * @param pvalue1	Complex
 * @param pvalue2	Complex 
 */
	public static Complex divide(Complex pvalue1, Complex pvalue2) 
	{
		double denom = pvalue2.real * pvalue2.real + pvalue2.img  * pvalue2.img;
		
		double r = pvalue1.real * pvalue2.real + pvalue1.img  * pvalue2.img;
		double i = pvalue1.img * pvalue2.real - pvalue1.real * pvalue2.img;
		
		return new Complex(r/denom, i/denom);
	}

/** 
 * Return a string representation of the complex value. 
 */
	public String toString() 
	{
		String img_sign = (img < 0) ? " - " : " + ";
		return (real +  img_sign + img + "i");
	}
/** 
 * Return a string representation of the complex value. 
 */
	public String toParseableString() 
	{
		return ("("+real+","+img+")");
	}

/**
 * Construct a Real object using the real component of this object.
 * @see com.interworldtransport.clados.FieldObject#toReal()
 */
	public Real toReal()
	{
		return new Real(real);
	}
/**
 * This object is already a Complex, so return it.
 * @see com.interworldtransport.clados.FieldObject#toComplex()
 */
	public Complex toComplex()
	{
		return this;
	}
	
} 
