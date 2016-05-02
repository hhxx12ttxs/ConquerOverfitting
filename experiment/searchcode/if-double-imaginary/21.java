package jeme.math;

import java.math.BigDecimal;

/**
 * Represents a complex number in Scheme. A complex number is made up of one
 * real and one imaginary part.
 * 
 * @author Erik Silkensen (silkense@colorado.edu)
 * @version Jun 20, 2009
 */
public class SchemeComplex extends SchemeNumber 
{   
    private SchemeReal real, imag;

    /**
     * Subclasses are allowed to take responsibility of initializing themselves
     * by using default constructors all of the way up the tower.
     */
    protected SchemeComplex() 
    {

    }

    /**
     * Creates a complex number with the specified real and imaginary parts.
     * 
     * @param real  the real part of this number
     * @param imag  the imaginary part of this number
     */
    public SchemeComplex(SchemeReal real, SchemeReal imag) 
    {
        setRealPart(real);
        setImagPart(imag);
    }
    
    /**
     * Returns whether or not this number is complex.
     * 
     * @return  <code>true</code>
     */
    public boolean isComplex() 
    {
        return true;
    }

    /**
     * Returns whether or not this number is real. A complex number is real if
     * and only if its imaginary part is exactly zero.
     * 
     * @return  <code>true</code> if this number is real, else 
     *     <code>false</code>
     */
    public boolean isReal() 
    {
        return getImagPart().isZero() && getImagPart().isExact();
    }

    /**
     * Returns the real part of this number.
     * 
     * @return  the real part of this number
     */
    public SchemeReal getRealPart() 
    {
        return real;
    }

    /**
     * Sets the real part of this number.
     * 
     * @param real  the real part of this number
     */
    protected void setRealPart(SchemeReal real) 
    {
        this.real = real;
    }

    /**
     * Returns the imaginary part of this number.
     * 
     * @return  the imaginary part of this number
     */
    public SchemeReal getImagPart() 
    {
        if (imag == null) {    
            setImagPart(SchemeInteger.ZERO);
        }
        
        return imag;
    }

    /**
     * Sets the imaginary part of this number.
     * 
     * @param imag  the imaginary part of this number
     */
    protected void setImagPart(SchemeReal imag) 
    {
        this.imag = imag;
    }

    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#isZero()
     */
    public boolean isZero() 
    {
        return getRealPart().isZero() && getImagPart().isZero();
    }

    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#isOne()
     */
    public boolean isOne() 
    {
        return getRealPart().isOne() && getImagPart().isZero();
    }

    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#isExact()
     */
    public boolean isExact() 
    {
        return getRealPart().isExact() && getImagPart().isExact();
    }

    /*
     * (non-Javadoc)
     * @see jeme.lang.SchemeObject#toString()
     */
    public String toString() 
    {
        if (isReal()) {
            return getRealPart().toString();
        }
        
        String real = getRealPart().isZero() ? "" : getRealPart().toString();
        String imag;
        if (getImagPart().isOne()) {
            imag = "";
        } else if (getImagPart().toString().equals("-1")) {
            imag = "-";
        } else {
            imag = getImagPart().toString();
        }
        //String imag = getImagPart().isOne() ? "" : getImagPart().toString();
        String sign = (imag.length() > 0 && imag.charAt(0) == '-') ? "" : "+";
        return real + sign + imag + "i";
    }

    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#add(jeme.math.SchemeNumber)
     */
    public SchemeNumber add(SchemeNumber augend) 
    {
        if (!(augend instanceof SchemeComplex)) {
            throw new IllegalArgumentException("augend must be complex");
        }
        
        SchemeComplex complex = (SchemeComplex) augend;
        SchemeNumber a = getRealPart();
        SchemeNumber b = getImagPart();
        SchemeNumber c = complex.getRealPart();
        SchemeNumber d = complex.getImagPart();

        SchemeReal real = (SchemeReal) a.add(c);
        SchemeReal imag = (SchemeReal) b.add(d);
        
        return new SchemeComplex(real, imag);
    }

    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#subtract(jeme.math.SchemeNumber)
     */
    public SchemeNumber subtract(SchemeNumber subtrahend) 
    {
        if (!(subtrahend instanceof SchemeComplex)) {
            throw new IllegalArgumentException("subtrahend must be complex");
        }

        SchemeComplex complex = (SchemeComplex) subtrahend;
        SchemeReal a = getRealPart();
        SchemeReal b = getImagPart();
        SchemeReal c = complex.getRealPart();
        SchemeReal d = complex.getImagPart();

        SchemeReal real = (SchemeReal) a.subtract(c);
        SchemeReal imag = (SchemeReal) b.subtract(d);
        
        return new SchemeComplex(real, imag);
    }

    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#multiply(jeme.math.SchemeNumber)
     */
    public SchemeNumber multiply(SchemeNumber multiplicand) 
    {
        if (!(multiplicand instanceof SchemeComplex)) {
            throw new IllegalArgumentException("multiplicand must be complex");
        }

        SchemeComplex complex = (SchemeComplex) multiplicand;
        SchemeReal a = getRealPart();
        SchemeReal b = getImagPart();
        SchemeReal c = complex.getRealPart();
        SchemeReal d = complex.getImagPart();
        SchemeNumber ac = a.multiply(c);
        SchemeNumber bd = b.multiply(d);
        SchemeNumber bc = b.multiply(c);
        SchemeNumber ad = a.multiply(d);

        SchemeReal real = (SchemeReal) ac.subtract(bd);
        SchemeReal imag = (SchemeReal) bc.add(ad);
        
        return new SchemeComplex(real, imag);
    }

    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#divide(jeme.math.SchemeNumber)
     */
    public SchemeNumber divide(SchemeNumber divisor) 
    {
        if (!(divisor instanceof SchemeComplex)) {
            throw new IllegalArgumentException("divisor");
        }

        SchemeComplex complex = (SchemeComplex) divisor;
        SchemeReal a = getRealPart();
        SchemeReal b = getImagPart();
        SchemeReal c = complex.getRealPart();
        SchemeReal d = complex.getImagPart();
        SchemeNumber ac = a.multiply(c);
        SchemeNumber bd = b.multiply(d);
        SchemeNumber bc = b.multiply(c);
        SchemeNumber ad = a.multiply(d);
        SchemeNumber cc = c.multiply(c);
        SchemeNumber dd = d.multiply(d);
        SchemeNumber ac_bd = ac.add(bd);
        SchemeNumber bc_ad = bc.subtract(ad);
        SchemeNumber cc_dd = cc.add(dd);
        
        SchemeReal real = (SchemeReal) ac_bd.divide(cc_dd);
        SchemeReal imag = (SchemeReal) bc_ad.divide(cc_dd);

        return new SchemeComplex(real, imag);
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#log()
     */
    public SchemeNumber log()
    {
        if (isReal()) {
            return getRealPart().log();
        }
        
        SchemeReal real = (SchemeReal) log(magnitude(this));
        SchemeReal imag = (SchemeReal) atan2(getImagPart(), getRealPart());
        
        return new SchemeComplex(real, imag);
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#sqrt()
     */
    public SchemeNumber sqrt()
    {
        if (isReal()) {
            return getRealPart().sqrt();
        }
        
        SchemeNumber TWO = SchemeInteger.ONE.add(SchemeInteger.ONE);
        SchemeNumber r = magnitude(this);
        SchemeReal x = getRealPart(), y = getImagPart();
        SchemeReal rpx = (SchemeReal) r.add(x);
        
        SchemeReal real = (SchemeReal) rpx.divide(TWO).sqrt();
        SchemeReal imag = (SchemeReal) y.divide(TWO.multiply(rpx).sqrt());
        
        return new SchemeComplex(real, imag);
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#sin()
     */
    public SchemeNumber sin()
    {  
        double value = getImagPart().getInexactValue().doubleValue();
        
        SchemeReal icosh = new SchemeReal(new BigDecimal(Math.cosh(value)));
        SchemeReal real = (SchemeReal) getRealPart().sin().multiply(icosh);
        
        double sinh = Math.sinh(value);
        SchemeReal isinh = new SchemeReal(new BigDecimal(sinh));
        SchemeReal imag = (SchemeReal) getImagPart().cos().multiply(isinh);
        
        return new SchemeComplex(real, imag);
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#cos()
     */
    public SchemeNumber cos()
    {
        double value = getImagPart().getInexactValue().doubleValue();
        
        SchemeReal icosh = new SchemeReal(new BigDecimal(Math.cosh(value)));
        SchemeReal real = (SchemeReal) getRealPart().cos().multiply(icosh);
        
        SchemeReal isinh = new SchemeReal(new BigDecimal(Math.sinh(value)));
        SchemeReal imag = (SchemeReal) getImagPart().sin().multiply(isinh);
        
        return new SchemeComplex(real, imag);
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#tan()
     */
    public SchemeNumber tan()
    {
        return sin().divide(cos());
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#asin()
     */
    public SchemeNumber asin()
    {
        return divide(SchemeInteger.ONE.subtract(multiply(this)).sqrt()).atan();
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#acos()
     */
    public SchemeNumber acos()
    {
        SchemeNumber TWO = SchemeInteger.ONE.add(SchemeInteger.ONE);
        
        return SchemeReal.PI.divide(TWO).subtract(asin());
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#atan()
     */
    public SchemeNumber atan()
    {
        SchemeComplex I = new SchemeComplex(SchemeInteger.ZERO, SchemeInteger.ONE);
        SchemeReal TWO = (SchemeReal) SchemeInteger.ONE.add(SchemeInteger.ONE);
        
        SchemeNumber zi = I.multiply(this);
        SchemeNumber n1 = SchemeInteger.ONE.add(zi);
        SchemeNumber n2 = SchemeInteger.ONE.subtract(zi);
        SchemeNumber n = n1.divide(n2).log();
        /*
         * (/ (log (/ (+ 1 (* +i z)) (- 1 (* +i z))))
         *    +2i
         */
        return n.divide(new SchemeComplex(SchemeInteger.ZERO, TWO));
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#atan(SchemeNumber)
     */
    public SchemeNumber atan2(SchemeNumber y)
    {
        throw new UnsupportedOperationException("atan2 only for real numbers");
    }

    /**
     * Returns a complex version of this number.
     * 
     * @return  <code>this</code>
     */
    public SchemeComplex toComplex() 
    {
        return this;
    }
    
    /*
     * (non-Javadoc)
     * @see jeme.math.SchemeNumber#negate()
     */
    public SchemeNumber negate()
    {
        SchemeReal real = (SchemeReal) getRealPart().negate();
        SchemeReal imag = (SchemeReal) getImagPart().negate();
        
        return new SchemeComplex(real, imag); 
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other)
    {
        if (other instanceof SchemeComplex) {
            SchemeComplex complex = (SchemeComplex) other;
            return getRealPart().equals(complex.getRealPart()) &&
                getImagPart().equals(complex.getImagPart());
        }
        
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(SchemeNumber other)
    {
        throw new UnsupportedOperationException("can only compare real numbers");
    }
}

