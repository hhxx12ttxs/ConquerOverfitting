/*
<h2>Copyright</h2>
Copyright (c) 2005 Interworld Transport.  All rights reserved.<br>
--------------------------------------------------------------------------------
<br>
---com.interworldtransport.clados.Real<br>
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
---com.interworldtransport.clados.Real<br>
--------------------------------------------------------------------------------
*/
package com.interworldtransport.clados;

/**
 * This class implements the concept of a Real Field from mathematics.   Field 
 * objects within the clados package are used as 'numbers' in the definition of 
 * an algebra.  All CladosObjects use FieldObjects as a result.  
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
 * Ideally, this would extend java.lang.Double and implement an interface like 
 * FieldObject.  That can't be done, though, because Double is final.
 * 
 * @version 0.80, $Date: 2005/09/29 08:36:20 $
 * @author Dr Alfred W Differ
 */
public class Real extends FieldObject
{
/**
 *  The number!
 */
	public double real;
	
/**
 * Basic Constructor with no values to initialize. 
 */
	public Real() 
	{
		FieldType="Real";
		real = 0.0; 
	}
/**
 * Simple constructor from a double primitive value.
 * @param cvalue	double
 */
	public Real(double cvalue)
	{
		FieldType="Real";
		real = cvalue;
	}
/** 
 * Basic Constructor that initializes the values from another Real number. 
 * @param z	Real
 */
	public Real(Real z) 
	{
		FieldType="Real";
		real = z.real; 
	}
/** 
 * Basic Constructor that initializes the values from another FieldObject.
 * @param cvalue	FieldObject 
 */
	public Real(FieldObject cvalue) 
	{
		FieldType="Real";
		if (cvalue.getFieldType().equals("Real"))
		{
			real = ((Real)cvalue).real; 
		}
		if (cvalue.getFieldType().equals("Complex"))
		{
			real = ((Complex)cvalue).real; 
		}
	}
	
/**
 * For a real number, the modulus is the same as the absolute value of the 
 * number itself. 
 * @see com.interworldtransport.clados.FieldObject#getModulus()
 */
	public double getModulus()
	{
		return Math.abs(real);
	}
/**
 * This method checks for exact equality.
 * @see com.interworldtransport.clados.FieldObject#equals(com.interworldtransport.clados.FieldObject)
 */
	public boolean equals(FieldObject pCvalue)
	{
		if (this.isTypeMatch(pCvalue))
		{
			return (((Real)pCvalue).real==real);
		}
		else
		{
			// TODO Write section that tries to cast pCvalue to Real
			// and try again.
			return false;	
		}	
	}
/**
 * This method checks to see if the number is exactly zero. 
 * @see com.interworldtransport.clados.FieldObject#isZero()
 */
	public boolean isZero()
	{
		if (real==0.0) return true;
		else return false;
	}
/**
 * This method checks to see if the value is infinite.
 * @see com.interworldtransport.clados.FieldObject#isInfinite()
 */
	public boolean isInfinite()
	{
		return  Double.isInfinite(real);
	}
/**
 * This method checks to see if the value is not a number at all. NAN
 * @see com.interworldtransport.clados.FieldObject#isNaN()
 */
	public boolean isNaN()
	{
		return Double.isNaN(real);
	}
/**
 * This method does nothing since the conjugate of a real number is itself.
 * @see com.interworldtransport.clados.FieldObject#conjugate()
 */
	public void conjugate()
	{
		;
	}

/**
 * This method adds real numbers together and changes this object to be the
 * result.
 * @see com.interworldtransport.clados.FieldObject#add(com.interworldtransport.clados.FieldObject)
 */
	public void add(FieldObject pvalue)
	{
		if (this.isTypeMatch(pvalue))
		{
			real += ((Real)pvalue).real;
		}
		else
		{
			real += pvalue.toReal().real;
		}
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
 * This method subtracts real numbers and changes this object to be the result.
 * @see com.interworldtransport.clados.FieldObject#subtract(com.interworldtransport.clados.FieldObject)
 */
	public void subtract(FieldObject pvalue)
	{
		if (this.isTypeMatch(pvalue))
		{
			real -= ((Real)pvalue).real;
		}
		else
		{
			real -= pvalue.toReal().real;
		}
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
 * This method multiplies real numbers and changes this object to be the result.
 * @see com.interworldtransport.clados.FieldObject#multiply(com.interworldtransport.clados.FieldObject)
 */
	public void multiply(FieldObject pvalue)
	{
		if (this.isTypeMatch(pvalue))
		{
			real *= ((Real)pvalue).real;
		}
		else
		{
			real *= pvalue.toReal().real;
		}
	}
/**
 * Multiply this real object by a double.
 * @param pvalue	double 
 */
	public void multiply(double pvalue) 
	{
		real *= pvalue;
	}
/**
 * This method divides real numbers and changes this object to be the result.
 * @see com.interworldtransport.clados.FieldObject#divide(com.interworldtransport.clados.FieldObject)
 */
	public void divide(FieldObject pvalue)
	{
		if (this.isTypeMatch(pvalue))
		{
			real /= ((Real)pvalue).real;
		}
		else
		{
			real /= pvalue.toReal().real;
		}
	}
/**
 * Divide this real object by a double.
 * @param pvalue	double 
 */
	public void divide(double pvalue) 
	{
		real /= pvalue;			
	}	
/**
 * Static method that creates a new Real with the conjugate of the parameter.
 * Since the conjugate of a real number is the real number, this method just
 * makes a new object that happens to copy the real number value.
 * @param pvalue	Real
 */
	public static Real conjugate(Real pvalue)
	{
		return new Real(pvalue.real);
	}
/**
 * Static add method that creates a new Real with the sum.
 * @param pvalue1	Real
 * @param pvalue2	Real
 */
	public static Real add(Real pvalue1, Real pvalue2) 
	{
		return new Real(pvalue1.real + pvalue2.real);
	}
/** 
 * Define a static subtract method that createsa new Real equal to
 * cvalue1 - cvalue2.
 * @param pvalue1	Real
 * @param pvalue2	Real  
 */
	public static Real subtract(Real pvalue1, Real pvalue2) 
	{
		return new Real(pvalue1.real - pvalue2.real);
	}
/** 
 * Define a static multiply method that createsa new Real with the 
 * product.
 * @param pvalue1	Real
 * @param pvalue2	Real 
 */
	public static Real multiply(Real pvalue1, Real pvalue2)
	{
		return new Real(pvalue1.real * pvalue2.real);
	}
/**
 * Define a static divide method that creates a new Real with the 
 * result of cvalue1/cvalue2.
 * @param pvalue1	Real
 * @param pvalue2	Real 
 */
	public static Real divide(Real pvalue1, Real pvalue2) 
	{
		return new Real(pvalue1.real / pvalue2.real);
	}

/** 
 * Return a string representation of the real value 
 */
	public String toString() 
	{
		return (real+"r");
	}
/**
 * Return a string representation of the real value. 
 * @see com.interworldtransport.clados.FieldObject#toParseableString()
 */
	public String toParseableString()
	{
		return ("("+real+")");
	}
	
/**
 * This object is already of type Real, so simply return it.
 * @see com.interworldtransport.clados.FieldObject#toReal()
 */
	public Real toReal()
	{
		return this;
	}
/**
 * Construct a complex number using the real part of this object.
 * @see com.interworldtransport.clados.FieldObject#toComplex()
 */
	public Complex toComplex()
	{
		return new Complex(real, 0.0);
	}

}

