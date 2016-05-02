<<<<<<< HEAD
/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 127 $
 * $Date: 2008-02-28 15:18:51 -0500 (Thu, 28 Feb 2008) $
 * $State$
 */

package javax.vecmath;

import java.lang.Math;

/**
 * A generic 2-element tuple that is represented by double-precision  
 * floating point x,y coordinates.
 *
 */
public abstract class Tuple2d implements java.io.Serializable, Cloneable {

    static final long serialVersionUID = 6205762482756093838L;

    /**
     * The x coordinate.
     */
    public	double	x;

    /**
     * The y coordinate.
     */
    public	double	y;


    /**
     * Constructs and initializes a Tuple2d from the specified xy coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Tuple2d(double x, double y)
    {
	this.x = x;
	this.y = y;
    }


    /**
     * Constructs and initializes a Tuple2d from the specified array.
     * @param t the array of length 2 containing xy in order
     */
    public Tuple2d(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
    }


    /**
     * Constructs and initializes a Tuple2d from the specified Tuple2d.
     * @param t1 the Tuple2d containing the initialization x y data
     */
    public Tuple2d(Tuple2d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
    }


    /**
     * Constructs and initializes a Tuple2d from the specified Tuple2f.
     * @param t1 the Tuple2f containing the initialization x y data
     */
    public Tuple2d(Tuple2f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
    }

    /**
     * Constructs and initializes a Tuple2d to (0,0).
     */
    public Tuple2d()
    {
	this.x = 0.0;
	this.y = 0.0;
    }


    /**
     * Sets the value of this tuple to the specified xy coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public final void set(double x, double y)
    {
	this.x = x;
	this.y = y;
    }


    /**
     * Sets the value of this tuple from the 2 values specified in 
     * the array.
     * @param t the array of length 2 containing xy in order
     */
    public final void set(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
    }


    /**
     * Sets the value of this tuple to the value of the Tuple2d argument.
     * @param t1 the tuple to be copied
     */
    public final void set(Tuple2d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
    }
 

    /**
     * Sets the value of this tuple to the value of Tuple2f t1.
     * @param t1 the tuple to be copied
     */
    public final void set(Tuple2f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
    }

   /**
    *  Copies the value of the elements of this tuple into the array t.
    *  @param t the array that will contain the values of the vector
    */
   public final void get(double[] t)
    {
        t[0] = this.x;
        t[1] = this.y;
    }


    /**
     * Sets the value of this tuple to the vector sum of tuples t1 and t2.
     * @param t1 the first tuple
     * @param t2 the second tuple
     */
    public final void add(Tuple2d t1, Tuple2d t2)
    {
	this.x = t1.x + t2.x;
	this.y = t1.y + t2.y;
    }


    /**
     * Sets the value of this tuple to the vector sum of itself and tuple t1.
     * @param t1 the other tuple
     */  
    public final void add(Tuple2d t1)
    {
        this.x += t1.x;
        this.y += t1.y;
    }


    /**
     * Sets the value of this tuple to the vector difference of 
     * tuple t1 and t2 (this = t1 - t2).    
     * @param t1 the first tuple
     * @param t2 the second tuple
     */  
    public final void sub(Tuple2d t1, Tuple2d t2)
    {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
    }  


    /**
     * Sets the value of this tuple to the vector difference of
     * itself and tuple t1 (this = this - t1).
     * @param t1 the other vector
     */  
    public final void sub(Tuple2d t1)
    {
        this.x -= t1.x;
        this.y -= t1.y;
    }


    /**
     * Sets the value of this tuple to the negation of tuple t1.
     * @param t1 the source vector
     */
    public final void negate(Tuple2d t1)
    {
	this.x = -t1.x;
	this.y = -t1.y;
    }


    /**
     * Negates the value of this vector in place.
     */
    public final void negate()
    {
	this.x = -this.x;
	this.y = -this.y;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1.
     * @param s the scalar value
     * @param t1 the source tuple
     */
    public final void scale(double s, Tuple2d t1)
    {
	this.x = s*t1.x;
	this.y = s*t1.y;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself.
     * @param s the scalar value
     */
    public final void scale(double s)
    {
	this.x *= s;
	this.y *= s;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1 and then adds tuple t2 (this = s*t1 + t2).
     * @param s the scalar value
     * @param t1 the tuple to be multipled
     * @param t2 the tuple to be added
     */  
    public final void scaleAdd(double s, Tuple2d t1, Tuple2d t2)
    {
        this.x = s*t1.x + t2.x; 
        this.y = s*t1.y + t2.y; 
    } 
 

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself and then adds tuple t1 (this = s*this + t1).
     * @param s the scalar value
     * @param t1 the tuple to be added
     */
    public final void scaleAdd(double s, Tuple2d t1)
    {
        this.x = s*this.x + t1.x;
        this.y = s*this.y + t1.y;
    }



    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Tuple2d objects with identical data values
     * (i.e., Tuple2d.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     * @return the integer hash code value
     */  
    public int hashCode() {
	long bits = 1L;
	bits = 31L * bits + VecMathUtil.doubleToLongBits(x);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(y);
	return (int) (bits ^ (bits >> 32));
    }


   /**   
     * Returns true if all of the data members of Tuple2d t1 are
     * equal to the corresponding data members in this Tuple2d.
     * @param t1  the vector with which the comparison is made
     * @return  true or false
     */  
    public boolean equals(Tuple2d t1)
    {
        try {
           return(this.x == t1.x && this.y == t1.y);
        }
        catch (NullPointerException e2) {return false;}

    }

   /**   
     * Returns true if the Object t1 is of type Tuple2d and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple2d.
     * @param t1  the object with which the comparison is made
     * @return  true or false
     */  
    public boolean equals(Object t1)
    {
        try {
           Tuple2d t2 = (Tuple2d) t1;
           return(this.x == t2.x && this.y == t2.y);
        }
        catch (NullPointerException e2) {return false;}
        catch (ClassCastException   e1) {return false;}

    }

   /**
     * Returns true if the L-infinite distance between this tuple
     * and tuple t1 is less than or equal to the epsilon parameter, 
     * otherwise returns false.  The L-infinite
     * distance is equal to MAX[abs(x1-x2), abs(y1-y2)]. 
     * @param t1  the tuple to be compared to this tuple
     * @param epsilon  the threshold value  
     * @return  true or false
     */
    public boolean epsilonEquals(Tuple2d t1, double epsilon)
    {
       double diff;

       diff = x - t1.x;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       diff = y - t1.y;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       return true;
    }

   /**
     * Returns a string that contains the values of this Tuple2d.
     * The form is (x,y).
     * @return the String representation
     */  
   public String toString()
   {
        return("(" + this.x + ", " + this.y + ")");
   }


  /**
    *  Clamps the tuple parameter to the range [low, high] and 
    *  places the values into this tuple.  
    *  @param min   the lowest value in the tuple after clamping
    *  @param max  the highest value in the tuple after clamping 
    *  @param t   the source tuple, which will not be modified
    */
   public final void clamp(double min, double max, Tuple2d t)
   {
        if( t.x > max ) { 
          x = max;
        } else if( t.x < min ){
          x = min;
        } else {
          x = t.x;
        }

        if( t.y > max ) { 
          y = max;
        } else if( t.y < min ){
          y = min;
        } else {
          y = t.y;
        }

   }


  /** 
    *  Clamps the minimum value of the tuple parameter to the min 
    *  parameter and places the values into this tuple.
    *  @param min   the lowest value in the tuple after clamping 
    *  @param t   the source tuple, which will not be modified
    */   
   public final void clampMin(double min, Tuple2d t) 
   { 
        if( t.x < min ) { 
          x = min;
        } else {
          x = t.x;
        }

        if( t.y < min ) { 
          y = min;
        } else {
          y = t.y;
        }

   } 


  /**  
    *  Clamps the maximum value of the tuple parameter to the max 
    *  parameter and places the values into this tuple.
    *  @param max   the highest value in the tuple after clamping  
    *  @param t   the source tuple, which will not be modified
    */    
   public final void clampMax(double max, Tuple2d t)  
   {  
        if( t.x > max ) { 
          x = max;
        } else { 
          x = t.x;
        }
 
        if( t.y > max ) {
          y = max;
        } else {
          y = t.y;
        }

   } 


  /**  
    *  Sets each component of the tuple parameter to its absolute 
    *  value and places the modified values into this tuple.
    *  @param t   the source tuple, which will not be modified
    */    
  public final void absolute(Tuple2d t)
  {
       x = Math.abs(t.x);
       y = Math.abs(t.y);
  } 



  /**
    *  Clamps this tuple to the range [low, high].
    *  @param min  the lowest value in this tuple after clamping
    *  @param max  the highest value in this tuple after clamping
    */
   public final void clamp(double min, double max)
   {
        if( x > max ) {
          x = max;
        } else if( x < min ){
          x = min;
        }
 
        if( y > max ) {
          y = max;
        } else if( y < min ){
          y = min;
        }

   }

 
  /**
    *  Clamps the minimum value of this tuple to the min parameter.
    *  @param min   the lowest value in this tuple after clamping
    */
   public final void clampMin(double min)
   { 
      if( x < min ) x=min;
      if( y < min ) y=min;
   } 
 
 
  /**
    *  Clamps the maximum value of this tuple to the max parameter.
    *  @param max   the highest value in the tuple after clamping
    */
   public final void clampMax(double max)
   { 
      if( x > max ) x=max;
      if( y > max ) y=max;
   }


  /**
    *  Sets each component of this tuple to its absolute value.
    */
  public final void absolute()
  {
     x = Math.abs(x);
     y = Math.abs(y);
  }


  /** 
    *  Linearly interpolates between tuples t1 and t2 and places the 
    *  result into this tuple:  this = (1-alpha)*t1 + alpha*t2.
    *  @param t1  the first tuple
    *  @param t2  the second tuple
    *  @param alpha  the alpha interpolation parameter
    */
  public final void interpolate(Tuple2d t1, Tuple2d t2, double alpha)
  {
       this.x = (1-alpha)*t1.x + alpha*t2.x;
       this.y = (1-alpha)*t1.y + alpha*t2.y;
  }


  /**  
    *  Linearly interpolates between this tuple and tuple t1 and 
    *  places the result into this tuple:  this = (1-alpha)*this + alpha*t1.
    *  @param t1  the first tuple
    *  @param alpha  the alpha interpolation parameter  
    */   
  public final void interpolate(Tuple2d t1, double alpha) 
  { 
       this.x = (1-alpha)*this.x + alpha*t1.x;
       this.y = (1-alpha)*this.y + alpha*t1.y;

  } 

    /**
     * Creates a new object of the same class as this object.
     *
     * @return a clone of this instance.
     * @exception OutOfMemoryError if there is not enough memory.
     * @see java.lang.Cloneable
     * @since vecmath 1.3
     */
    public Object clone() {
	// Since there are no arrays we can just use Object.clone()
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }


	/**
	 * Get the <i>x</i> coordinate.
	 * 
	 * @return the <i>x</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final double getX() {
		return x;
	}


	/**
	 * Set the <i>x</i> coordinate.
	 * 
	 * @param x  value to <i>x</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final void setX(double x) {
		this.x = x;
	}


	/**
	 * Get the <i>y</i> coordinate.
	 * 
	 * @return the <i>y</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final double getY() {
		return y;
	}


	/**
	 * Set the <i>y</i> coordinate.
	 * 
	 * @param y value to <i>y</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final void setY(double y) {
		this.y = y;
	}

}
=======
package jmat.data;

import jmat.function.DoubleFunction;


/**
<P>
   The RandomVaraibale Class provides static methods for generating random numbers.

@.author Yann RICHET.
@version 2.0
*/
public class RandomVariable
{
    //~ Methods ////////////////////////////////////////////////////////////////

    /** Generate a random number from a beta random variable.
    @param a    First parameter of the Beta random variable.
    @param b    Second parameter of the Beta random variable.
    @return      A double.
    */
    public static double beta(double a, double b)
    {
        double try_x;
        double try_y;

        do
        {
            try_x = Math.pow(rand(), 1 / a);
            try_y = Math.pow(rand(), 1 / b);
        }
        while ((try_x + try_y) > 1);

        return try_x / (try_x + try_y);
    }

    /** Generate a random number from a Cauchy random variable (Mean = Inf, and Variance = Inf).
    @param mu    Median of the Weibull random variable
    @param sigma    Second parameter of the Cauchy random variable.
    @return      A double.
    */
    public static double cauchy(double mu, double sigma)
    {
        double x = (sigma * Math.tan(Math.PI * (rand() - 0.5))) + mu;

        return x;
    }

    /** Generate a random number from a discrete random variable.
     @param values    Discrete values.
     @param prob    Probability of each value.
     @return      A double.
     */
    public static double dirac(double[] values, double[] prob)
    {
        double[] prob_cumul = new double[values.length];
        prob_cumul[0] = prob[0];

        for (int i = 1; i < values.length; i++)
        {
            prob_cumul[i] = prob_cumul[i - 1] + prob[i];
        }

        double y = rand();
        double x = 0;

        for (int i = 0; i < (values.length - 1); i++)
        {
            if ((y > prob_cumul[i]) & (y < prob_cumul[i + 1]))
            {
                x = values[i];
            }
        }

        return x;
    }

    /** Generate a random number from an exponantial random variable (Mean = 1/lambda, variance = 1/lambda^2).
    @param lambda    Parmaeter of the exponential random variable.
    @return      A double.
    */
    public static double exponential(double lambda)
    {
        double x = -1 / lambda * Math.log(rand());

        return x;
    }

    /** Generate a random number from a LogNormal random variable.
    @param mu    Mean of the Normal random variable.
    @param sigma    Standard deviation of the Normal random variable.
    @return      A double.
    */
    public static double logNormal(double mu, double sigma)
    {
        double x = mu +
            (sigma * Math.cos(2 * Math.PI * rand()) *
                Math.sqrt(-2 * Math.log(rand())));

        return x;
    }

    /** Generate a random number from a Gaussian (Normal) random variable.
    @param mu    Mean of the random variable.
    @param sigma    Standard deviation of the random variable.
    @return      A double.
    */
    public static double normal(double mu, double sigma)
    {
        double x = mu +
            (sigma * Math.cos(2 * Math.PI * rand()) *
                Math.sqrt(-2 * Math.log(rand())));

        return x;
    }

    /** Generate a random number from a random variable definied by its density methodName, using the rejection technic.
     *  !!! WARNING : this simulation technic can take a very long time !!!
     @param fun    Density methodName (may be not normalized) of the random variable.
     @param maxFun    Max of the methodName.
     @param min    Min of the random variable.
     @param max    Max of the random variable.
     @return      A double.
     */
    public static double rejection(DoubleFunction fun, double maxFun,
        double min, double max)
    {
        double[] try_x = new double[1];
        double try_y;

        do
        {
            try_x[0] = min + (rand() * (max - min));
            try_y = rand() * maxFun;
        }
        while (fun.eval(try_x) < try_y);

        return try_x[0];
    }

    /** Generate a random number from a symetric triangular random variable.
    @param min    Min of the random variable.
    @param max    Max of the random variable.
    @return      A double.
    */
    public static double triangular(double min, double max)
    {
        double x = (min / 2) + (((max - min) * rand()) / 2) + (min / 2) +
            (((max - min) * rand()) / 2);

        return x;
    }

    /** Generate a random number from a non-symetric triangular random variable.
    @param min    Min of the random variable.
    @param med    Value of the random variable with max density.
    @param max    Max of the random variable.
    @return      A double.
    */
    public static double triangular(double min, double med, double max)
    {
        double y = rand();

        //if min < x < med, y = (x-min)˛/(max-min)(med-min), else, med < x < max, and y = 1-(max-x)˛/(max-min)(max-med)
        double x = (y < ((med - min) / (max - min)))
            ? (min + Math.sqrt(y * (max - min) * (med - min)))
            : (max - Math.sqrt((1 - y) * (max - min) * (max - med)));

        return x;
    }

    /** Generate a random number from a uniform random variable.
     @param min    Min of the random variable.
     @param max    Max of the random variable.
     @return      A double.
     */
    public static double uniform(double min, double max)
    {
        double x = min + ((max - min) * rand());

        return x;
    }

    /** Generate a random number from a Weibull random variable.
    @param lambda    First parameter of the Weibull random variable.
    @param c    Second parameter of the Weibull random variable.
    @return      A double.
    */
    public static double weibull(double lambda, double c)
    {
        double x = Math.pow(-Math.log(1 - rand()), 1 / c) / lambda;

        return x;
    }

    /** Generate a random number between 0 and 1.
    @return      A double between 0 and 1.
    */
    protected static double rand()
    {
        double x = Math.random();

        return x;
    }

    /** Generate a random integer.
    @param i0    Min of the random variable.
    @param i1    Max of the random variable.
    @return      An int between i0 and i1.
    */
    protected static int randInt(int i0, int i1)
    {
        double x = rand();
        int i = i0 + new Double(Math.floor((i1 - i0 + 1) * x)).intValue();

        return i;
    }
}

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
>>>>>>> 76aa07461566a5976980e6696204781271955163

