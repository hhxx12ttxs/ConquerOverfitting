package jeme.math;

import jeme.lang.SchemeObject;

/**
 * Represents any number in Scheme. This is the top of the numeric tower. See
 * <a href="http://www.r6rs.org/final/html/r6rs/r6rs-Z-H-6.html#node_chap_3">Ch.
 * 3</a> of R<sup>6</sup>RS for documentation of Scheme numbers.
 * 
 * @author Erik Silkensen (silkense@colorado.edu)
 * @version Jun 20, 2009
 */
public abstract class SchemeNumber extends SchemeObject implements Comparable<SchemeNumber>
{    
    /**
     * Returns whether or not this number is a number.
     * 
     * @return  <code>true</code>
     */
    public boolean isNumber() 
    {
        return true;
    }

    /**
     * Returns whether or not this number is complex.
     * 
     * @return  <code>true</code> if this number is complex, else 
     *     <code>false</code>
     */
    public boolean isComplex() 
    {
        return false;
    }

    /**
     * Returns whether or not this number is real.
     * 
     * @return  <code>true</code> if this number is real, else 
     *     <code>false</code>
     */
    public boolean isReal() 
    {
        return false;
    }

    /**
     * Returns whether or not this number is rational.
     * 
     * @return  <code>true</code> if this number is rational, else 
     *     <code>false</code>
     */
    public boolean isRational() 
    {
        return false;
    }

    /**
     * Returns whether or not this number is an integer.
     * 
     * @return  <code>true</code> if this number is an integer, else 
     *     <code>false</code>
     */
    public boolean isInteger() 
    {
        return false;
    }

    /**
     * Returns whether or not this number is zero.
     * 
     * @return  <code>true</code> if this number is zero, else 
     *     <code>false</code>
     */
    public abstract boolean isZero();

    /**
     * Returns whether or not this number is one.
     * 
     * @return  <code>true</code> if this number is one, else 
     *     <code>false</code>
     */
    public abstract boolean isOne();

    /**
     * Returns whether or not this number is exact.
     * 
     * @return  <code>true</code> if this number is exact, else 
     *     <code>false</code>
     */
    public abstract boolean isExact();

    /**
     * Returns the sum of this number and <code>augend</code>.
     * 
     * @param augend  the augend
     * @return  the sum of this number and <code>augend</code>
     */
    public abstract SchemeNumber add(SchemeNumber augend);

    /**
     * Returns the difference of this number and <code>subtrahend</code>.
     * 
     * @param subtrahend  the subtrahend
     * @return  the difference of this number and <code>subtrahend</code>
     */
    public abstract SchemeNumber subtract(SchemeNumber subtrahend);

    /**
     * Returns the product of this number and <code>multiplicand</code>.
     * 
     * @param multiplicand  the multiplicand
     * @return  the product of this number and <code>multiplicand</code>
     */
    public abstract SchemeNumber multiply(SchemeNumber multiplicand);

    /**
     * Returns the quotient of this number and <code>divisor</code>.
     * 
     * @param divisor  the divisor
     * @return  the quotient of this number and <code>divisor</code>
     */
    public abstract SchemeNumber divide(SchemeNumber divisor);
    
    /**
     * Returns the negation of this number.
     * 
     * @return  the negation of this number
     */
    public abstract SchemeNumber negate();
    
    /**
     * 
     * @return
     */
    public abstract SchemeNumber sin();
    
    /**
     * 
     * @return
     */
    public abstract SchemeNumber cos();
    
    /**
     * 
     * @return
     */
    public abstract SchemeNumber tan();
    
    /**
     * 
     * @return
     */
    public abstract SchemeNumber asin();
    
    /**
     * 
     * @return
     */
    public abstract SchemeNumber acos();
    
    /**
     * 
     * @return
     */
    public abstract SchemeNumber atan();
    
    /**
     * 
     * @param y
     * @return
     */
    public abstract SchemeNumber atan2(SchemeNumber y);
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber sin(SchemeNumber z)
    {
        return z.sin();
    }
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber cos(SchemeNumber z)
    {
        return z.cos();
    }
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber tan(SchemeNumber z)
    {   
        return z.tan();
    }
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber asin(SchemeNumber z)
    {
        return z.asin();
    }
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber acos(SchemeNumber z)
    {
        return z.acos();
    }
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber atan(SchemeNumber z)
    {
        return z.atan();
    }
    
    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public static SchemeNumber atan2(SchemeNumber x, SchemeNumber y)
    {
        return x.atan2(y);
    }
    
    /**
     * Returns the natural logarithm of this number.
     * 
     * @return  the natural logarithm of this number
     */
    public abstract SchemeNumber log();
    
    /**
     * Returns the natural logarithm of a number.
     * 
     * @param z  the number
     * @return  the natural logarithm of a number
     */
    public static SchemeNumber log(SchemeNumber z)
    {
        return z.log();
    }
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber angle(SchemeNumber z)
    {
        if (!(z instanceof SchemeComplex)) {
            throw new IllegalArgumentException("z must be complex");
        }
        
        SchemeComplex c = (SchemeComplex) z;
        
        return atan2(c.getImagPart(), c.getRealPart()); 
    }
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber magnitude(SchemeNumber z)
    {
        if (!(z instanceof SchemeComplex)) {
            throw new IllegalArgumentException("z must be complex");
        }
        
        SchemeComplex c = (SchemeComplex) z;
        SchemeReal real = c.getRealPart(), imag = c.getImagPart();
        
        return sqrt(real.multiply(real).add(imag.multiply(imag)));
    }
    
    /**
     * 
     * @return
     */
    public abstract SchemeNumber sqrt();
    
    /**
     * 
     * @param z
     * @return
     */
    public static SchemeNumber sqrt(SchemeNumber z)
    {
        return z.sqrt();
    }
}

