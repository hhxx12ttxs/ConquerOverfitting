/*
<<<<<<< HEAD
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * A generic 3-element tuple that is represented by double-precision 
 * floating point x,y,z coordinates.
 *
 */
public abstract class Tuple3d implements java.io.Serializable, Cloneable {

    static final long serialVersionUID = 5542096614926168415L;

    /**
     * The x coordinate.
     */
    public	double	x;

    /**
     * The y coordinate.
     */
    public	double	y;

    /**
     * The z coordinate.
     */
    public	double	z;


    /**
     * Constructs and initializes a Tuple3d from the specified xyz coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public Tuple3d(double x, double y, double z)
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    /**
     * Constructs and initializes a Tuple3d from the array of length 3.
     * @param t the array of length 3 containing xyz in order
     */
    public Tuple3d(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
	this.z = t[2];
    }

    /**
     * Constructs and initializes a Tuple3d from the specified Tuple3d.
     * @param t1 the Tuple3d containing the initialization x y z data
     */
    public Tuple3d(Tuple3d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
	this.z = t1.z;
    }

    /**
     * Constructs and initializes a Tuple3d from the specified Tuple3f.
     * @param t1 the Tuple3f containing the initialization x y z data
     */
    public Tuple3d(Tuple3f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
	this.z = (double) t1.z;
    }

    /**
     * Constructs and initializes a Tuple3d to (0,0,0).
     */
    public Tuple3d()
    {
	this.x = (double) 0.0;
	this.y = (double) 0.0;
	this.z = (double) 0.0;
    }

    /**
     * Sets the value of this tuple to the specified xyz coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public final void set(double x, double y, double z)
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    /**
     * Sets the value of this tuple to the value of the xyz coordinates
     * located in the array of length 3.
     * @param t the array of length 3 containing xyz in order
     */
    public final void set(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
	this.z = t[2];
    }

    /**
     * Sets the value of this tuple to the value of tuple t1.
     * @param t1 the tuple to be copied
     */
    public final void set(Tuple3d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
	this.z = t1.z;
    }

    /**
     * Sets the value of this tuple to the value of tuple t1.
     * @param t1 the tuple to be copied
     */
    public final void set(Tuple3f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
	this.z = (double) t1.z;
    }

   /**
     * Copies the x,y,z coordinates of this tuple into the array t
     * of length 3.
     * @param t  the target array 
     */
    public final void get(double[] t)
    {
        t[0] = this.x;
        t[1] = this.y;
        t[2] = this.z;
    }


   /**
     * Copies the x,y,z coordinates of this tuple into the tuple t.
     * @param t  the Tuple3d object into which the values of this object are copied
     */
    public final void get(Tuple3d t)
    {
        t.x = this.x;
        t.y = this.y;
        t.z = this.z;
    }


    /**
     * Sets the value of this tuple to the sum of tuples t1 and t2.
     * @param t1 the first tuple
     * @param t2 the second tuple
     */
    public final void add(Tuple3d t1, Tuple3d t2)
    {
	this.x = t1.x + t2.x;
	this.y = t1.y + t2.y;
	this.z = t1.z + t2.z;
    }


    /**  
     * Sets the value of this tuple to the sum of itself and t1.
     * @param t1 the other tuple
     */  
    public final void add(Tuple3d t1)
    { 
        this.x += t1.x;
        this.y += t1.y;
        this.z += t1.z;
    }

    /**
     * Sets the value of this tuple to the difference of tuples
     * t1 and t2 (this = t1 - t2).
     * @param t1 the first tuple
     * @param t2 the second tuple
     */
    public final void sub(Tuple3d t1, Tuple3d t2)
    {
	this.x = t1.x - t2.x;
	this.y = t1.y - t2.y;
	this.z = t1.z - t2.z;
    }
 
    /**  
     * Sets the value of this tuple to the difference
     * of itself and t1 (this = this - t1).
     * @param t1 the other tuple
     */  
    public final void sub(Tuple3d t1)
    { 
        this.x -= t1.x;
        this.y -= t1.y;
        this.z -= t1.z;
    }


    /**
     * Sets the value of this tuple to the negation of tuple t1.
     * @param t1 the source tuple
     */
    public final void negate(Tuple3d t1)
    {
	this.x = -t1.x;
	this.y = -t1.y;
	this.z = -t1.z;
    }


    /**
     * Negates the value of this tuple in place.
     */
    public final void negate()
    {
	this.x = -this.x;
	this.y = -this.y;
	this.z = -this.z;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1.
     * @param s the scalar value
     * @param t1 the source tuple
     */
    public final void scale(double s, Tuple3d t1)
    {
	this.x = s*t1.x;
	this.y = s*t1.y;
	this.z = s*t1.z;
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
        this.z *= s;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1 and then adds tuple t2 (this = s*t1 + t2).
     * @param s the scalar value
     * @param t1 the tuple to be multipled
     * @param t2 the tuple to be added
     */
    public final void scaleAdd(double s, Tuple3d t1, Tuple3d t2)
    {
	this.x = s*t1.x + t2.x;
	this.y = s*t1.y + t2.y;
	this.z = s*t1.z + t2.z;
    }


    /**
     * @deprecated Use scaleAdd(double,Tuple3d) instead
     */  
    public final void scaleAdd(double s, Tuple3f t1) {
	scaleAdd(s, new Point3d(t1));
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself and then adds tuple t1 (this = s*this + t1).
     * @param s the scalar value
     * @param t1 the tuple to be added
     */  
    public final void scaleAdd(double s, Tuple3d t1) {
        this.x = s*this.x + t1.x;
        this.y = s*this.y + t1.y;
        this.z = s*this.z + t1.z;
    }



   /**
     * Returns a string that contains the values of this Tuple3d.
     * The form is (x,y,z).
     * @return the String representation
     */  
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }


    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Tuple3d objects with identical data values
     * (i.e., Tuple3d.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     * @return the integer hash code value
     */  
    public int hashCode() {
	long bits = 1L;
	bits = 31L * bits + VecMathUtil.doubleToLongBits(x);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(y);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(z);
	return (int) (bits ^ (bits >> 32));
    }


   /**
     * Returns true if all of the data members of Tuple3d t1 are
     * equal to the corresponding data members in this Tuple3d.
     * @param t1  the tuple with which the comparison is made
     * @return  true or false
     */  
    public boolean equals(Tuple3d t1)
    {
      try {
        return(this.x == t1.x && this.y == t1.y && this.z == t1.z);
      }
      catch (NullPointerException e2) {return false;}
    }

   /**
     * Returns true if the Object t1 is of type Tuple3d and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple3d.
     * @param t1  the Object with which the comparison is made
     * @return  true or false
     */  
    public boolean equals(Object t1)
    {
      try {
           Tuple3d t2 = (Tuple3d) t1;
           return(this.x == t2.x && this.y == t2.y && this.z == t2.z);
      }
      catch (ClassCastException   e1) {return false;}
      catch (NullPointerException e2) {return false;}

    }

   /**
     * Returns true if the L-infinite distance between this tuple
     * and tuple t1 is less than or equal to the epsilon parameter, 
     * otherwise returns false.  The L-infinite
     * distance is equal to MAX[abs(x1-x2), abs(y1-y2), abs(z1-z2)].
     * @param t1  the tuple to be compared to this tuple
     * @param epsilon  the threshold value  
     * @return  true or false
     */
    public boolean epsilonEquals(Tuple3d t1, double epsilon)
    {
       double diff;

       diff = x - t1.x;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       diff = y - t1.y;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       diff = z - t1.z;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       return true;

    }


    /**
     * @deprecated Use clamp(double,double,Tuple3d) instead
     */
    public final void clamp(float min, float max, Tuple3d t) {
	clamp((double)min, (double)max, t);
    }


    /**
     *  Clamps the tuple parameter to the range [low, high] and 
     *  places the values into this tuple.  
     *  @param min   the lowest value in the tuple after clamping
     *  @param max  the highest value in the tuple after clamping 
     *  @param t   the source tuple, which will not be modified
     */
    public final void clamp(double min, double max, Tuple3d t) {
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
 
        if( t.z > max ) {
          z = max;
        } else if( t.z < min ){
          z = min;
        } else {
          z = t.z;
        }

   }


    /** 
     * @deprecated Use clampMin(double,Tuple3d) instead
     */   
    public final void clampMin(float min, Tuple3d t) {
	clampMin((double)min, t);
    }


    /** 
     *  Clamps the minimum value of the tuple parameter to the min 
     *  parameter and places the values into this tuple.
     *  @param min   the lowest value in the tuple after clamping 
     *  @param t   the source tuple, which will not be modified
     */   
    public final void clampMin(double min, Tuple3d t) { 
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
 
        if( t.z < min ) {
          z = min;
        } else {
          z = t.z;
        }

   } 


    /** 
     * @deprecated Use clampMax(double,Tuple3d) instead
     */   
    public final void clampMax(float max, Tuple3d t) {
	clampMax((double)max, t);
    }


    /**  
     *  Clamps the maximum value of the tuple parameter to the max 
     *  parameter and places the values into this tuple.
     *  @param max the highest value in the tuple after clamping  
     *  @param t   the source tuple, which will not be modified
     */    
    public final void clampMax(double max, Tuple3d t) {  
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
 
        if( t.z > max ) {
          z = max;
        } else {
          z = t.z;
        }

   } 


  /**  
    *  Sets each component of the tuple parameter to its absolute 
    *  value and places the modified values into this tuple.
    *  @param t   the source tuple, which will not be modified
    */    
  public final void absolute(Tuple3d t)
  {
       x = Math.abs(t.x);
       y = Math.abs(t.y);
       z = Math.abs(t.z);

  } 


    /**
     * @deprecated Use clamp(double,double) instead
     */
    public final void clamp(float min, float max) {
	clamp((double)min, (double)max);
    }


    /**
     *  Clamps this tuple to the range [low, high].
     *  @param min  the lowest value in this tuple after clamping
     *  @param max  the highest value in this tuple after clamping
     */
    public final void clamp(double min, double max) {
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
 
        if( z > max ) {
          z = max;
        } else if( z < min ){
          z = min;
        }

   }

 
    /** 
     * @deprecated Use clampMin(double) instead
     */   
    public final void clampMin(float min) {
	clampMin((double)min);
    }


    /**
     *  Clamps the minimum value of this tuple to the min parameter.
     *  @param min   the lowest value in this tuple after clamping
     */
    public final void clampMin(double min) { 
      if( x < min ) x=min;
      if( y < min ) y=min;
      if( z < min ) z=min;

   } 
 
 
    /** 
     * @deprecated Use clampMax(double) instead
     */   
    public final void clampMax(float max) {
	clampMax((double)max);
    }


    /**
     *  Clamps the maximum value of this tuple to the max parameter.
     *  @param max   the highest value in the tuple after clamping
     */
    public final void clampMax(double max) { 
      if( x > max ) x=max;
      if( y > max ) y=max;
      if( z > max ) z=max;
   }


  /**
    *  Sets each component of this tuple to its absolute value.
    */
  public final void absolute()
  {
     x = Math.abs(x);
     y = Math.abs(y);
     z = Math.abs(z);
  }


    /**
     * @deprecated Use interpolate(Tuple3d,Tuple3d,double) instead
     */
    public final void interpolate(Tuple3d t1, Tuple3d t2, float alpha) {
	interpolate(t1, t2, (double)alpha);
    }


    /**
     *  Linearly interpolates between tuples t1 and t2 and places the 
     *  result into this tuple:  this = (1-alpha)*t1 + alpha*t2.
     *  @param t1  the first tuple
     *  @param t2  the second tuple  
     *  @param alpha  the alpha interpolation parameter  
     */   
    public final void interpolate(Tuple3d t1, Tuple3d t2, double alpha) {
	this.x = (1-alpha)*t1.x + alpha*t2.x;
	this.y = (1-alpha)*t1.y + alpha*t2.y;
	this.z = (1-alpha)*t1.z + alpha*t2.z;
    }
 
 
    /**
     * @deprecated Use interpolate(Tuple3d,double) instead
     */
    public final void interpolate(Tuple3d t1, float alpha) {
	interpolate(t1, (double)alpha);
    }


    /**   
    *  Linearly interpolates between this tuple and tuple t1 and 
    *  places the result into this tuple:  this = (1-alpha)*this + alpha*t1. 
    *  @param t1  the first tuple 
    *  @param alpha  the alpha interpolation parameter   
    */    
    public final void interpolate(Tuple3d t1, double alpha) {
	this.x = (1-alpha)*this.x + alpha*t1.x;
	this.y = (1-alpha)*this.y + alpha*t1.y;
	this.z = (1-alpha)*this.z + alpha*t1.z;
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
	 * @return  the <i>x</i> coordinate.
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

	/**
	 * Get the <i>z</i> coordinate.
	 * 
	 * @return the <i>z</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final double getZ() {
		return z;
	}


	/**
	 * Set the <i>z</i> coordinate.
	 * 
	 * @param z value to <i>z</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final void setZ(double z) {
		this.z = z;
	}
=======
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.primitives;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

import com.google.common.annotations.GwtCompatible;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;

/**
 * Static utility methods pertaining to {@code double} primitives, that are not
 * already found in either {@link Double} or {@link Arrays}.
 *
 * @author Kevin Bourrillion
 * @since 1.0
 */
@GwtCompatible
public final class Doubles {
  private Doubles() {}

  /**
   * The number of bytes required to represent a primitive {@code double}
   * value.
   *
   * @since 10.0
   */
  public static final int BYTES = Double.SIZE / Byte.SIZE;

  /**
   * Returns a hash code for {@code value}; equal to the result of invoking
   * {@code ((Double) value).hashCode()}.
   *
   * @param value a primitive {@code double} value
   * @return a hash code for the value
   */
  public static int hashCode(double value) {
    return ((Double) value).hashCode();
    // TODO(kevinb): do it this way when we can (GWT problem):
    // long bits = Double.doubleToLongBits(value);
    // return (int)(bits ^ (bits >>> 32));
  }

  /**
   * Compares the two specified {@code double} values. The sign of the value
   * returned is the same as that of <code>((Double) a).{@linkplain
   * Double#compareTo compareTo}(b)</code>. As with that method, {@code NaN} is
   * treated as greater than all other values, and {@code 0.0 > -0.0}.
   *
   * @param a the first {@code double} to compare
   * @param b the second {@code double} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; or zero if they are equal
   */
  public static int compare(double a, double b) {
    return Double.compare(a, b);
  }

  /**
   * Returns {@code true} if {@code value} represents a real number. This is
   * equivalent to, but not necessarily implemented as,
   * {@code !(Double.isInfinite(value) || Double.isNaN(value))}.
   *
   * @since 10.0
   */
  public static boolean isFinite(double value) {
    return NEGATIVE_INFINITY < value & value < POSITIVE_INFINITY;
  }

  /**
   * Returns {@code true} if {@code target} is present as an element anywhere in
   * {@code array}. Note that this always returns {@code false} when {@code
   * target} is {@code NaN}.
   *
   * @param array an array of {@code double} values, possibly empty
   * @param target a primitive {@code double} value
   * @return {@code true} if {@code array[i] == target} for some value of {@code
   *     i}
   */
  public static boolean contains(double[] array, double target) {
    for (double value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the index of the first appearance of the value {@code target} in
   * {@code array}. Note that this always returns {@code -1} when {@code target}
   * is {@code NaN}.
   *
   * @param array an array of {@code double} values, possibly empty
   * @param target a primitive {@code double} value
   * @return the least index {@code i} for which {@code array[i] == target}, or
   *     {@code -1} if no such index exists.
   */
  public static int indexOf(double[] array, double target) {
    return indexOf(array, target, 0, array.length);
  }

  // TODO(kevinb): consider making this public
  private static int indexOf(
      double[] array, double target, int start, int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the start position of the first occurrence of the specified {@code
   * target} within {@code array}, or {@code -1} if there is no such occurrence.
   *
   * <p>More formally, returns the lowest index {@code i} such that {@code
   * java.util.Arrays.copyOfRange(array, i, i + target.length)} contains exactly
   * the same elements as {@code target}.
   *
   * <p>Note that this always returns {@code -1} when {@code target} contains
   * {@code NaN}.
   *
   * @param array the array to search for the sequence {@code target}
   * @param target the array to search for as a sub-sequence of {@code array}
   */
  public static int indexOf(double[] array, double[] target) {
    checkNotNull(array, "array");
    checkNotNull(target, "target");
    if (target.length == 0) {
      return 0;
    }

    outer:
    for (int i = 0; i < array.length - target.length + 1; i++) {
      for (int j = 0; j < target.length; j++) {
        if (array[i + j] != target[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }

  /**
   * Returns the index of the last appearance of the value {@code target} in
   * {@code array}. Note that this always returns {@code -1} when {@code target}
   * is {@code NaN}.
   *
   * @param array an array of {@code double} values, possibly empty
   * @param target a primitive {@code double} value
   * @return the greatest index {@code i} for which {@code array[i] == target},
   *     or {@code -1} if no such index exists.
   */
  public static int lastIndexOf(double[] array, double target) {
    return lastIndexOf(array, target, 0, array.length);
  }

  // TODO(kevinb): consider making this public
  private static int lastIndexOf(
      double[] array, double target, int start, int end) {
    for (int i = end - 1; i >= start; i--) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the least value present in {@code array}, using the same rules of
   * comparison as {@link Math#min(double, double)}.
   *
   * @param array a <i>nonempty</i> array of {@code double} values
   * @return the value present in {@code array} that is less than or equal to
   *     every other value in the array
   * @throws IllegalArgumentException if {@code array} is empty
   */
  public static double min(double... array) {
    checkArgument(array.length > 0);
    double min = array[0];
    for (int i = 1; i < array.length; i++) {
      min = Math.min(min, array[i]);
    }
    return min;
  }

  /**
   * Returns the greatest value present in {@code array}, using the same rules
   * of comparison as {@link Math#max(double, double)}.
   *
   * @param array a <i>nonempty</i> array of {@code double} values
   * @return the value present in {@code array} that is greater than or equal to
   *     every other value in the array
   * @throws IllegalArgumentException if {@code array} is empty
   */
  public static double max(double... array) {
    checkArgument(array.length > 0);
    double max = array[0];
    for (int i = 1; i < array.length; i++) {
      max = Math.max(max, array[i]);
    }
    return max;
  }

  /**
   * Returns the values from each provided array combined into a single array.
   * For example, {@code concat(new double[] {a, b}, new double[] {}, new
   * double[] {c}} returns the array {@code {a, b, c}}.
   *
   * @param arrays zero or more {@code double} arrays
   * @return a single array containing all the values from the source arrays, in
   *     order
   */
  public static double[] concat(double[]... arrays) {
    int length = 0;
    for (double[] array : arrays) {
      length += array.length;
    }
    double[] result = new double[length];
    int pos = 0;
    for (double[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }

  /**
   * Returns an array containing the same values as {@code array}, but
   * guaranteed to be of a specified minimum length. If {@code array} already
   * has a length of at least {@code minLength}, it is returned directly.
   * Otherwise, a new array of size {@code minLength + padding} is returned,
   * containing the values of {@code array}, and zeroes in the remaining places.
   *
   * @param array the source array
   * @param minLength the minimum length the returned array must guarantee
   * @param padding an extra amount to "grow" the array by if growth is
   *     necessary
   * @throws IllegalArgumentException if {@code minLength} or {@code padding} is
   *     negative
   * @return an array containing the values of {@code array}, with guaranteed
   *     minimum length {@code minLength}
   */
  public static double[] ensureCapacity(
      double[] array, int minLength, int padding) {
    checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
    checkArgument(padding >= 0, "Invalid padding: %s", padding);
    return (array.length < minLength)
        ? copyOf(array, minLength + padding)
        : array;
  }

  // Arrays.copyOf() requires Java 6
  private static double[] copyOf(double[] original, int length) {
    double[] copy = new double[length];
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
    return copy;
  }

  /**
   * Returns a string containing the supplied {@code double} values, converted
   * to strings as specified by {@link Double#toString(double)}, and separated
   * by {@code separator}. For example, {@code join("-", 1.0, 2.0, 3.0)} returns
   * the string {@code "1.0-2.0-3.0"}.
   *
   * <p>Note that {@link Double#toString(double)} formats {@code double}
   * differently in GWT sometimes.  In the previous example, it returns the string
   * {@code "1-2-3"}.
   *
   * @param separator the text that should appear between consecutive values in
   *     the resulting string (but not at the start or end)
   * @param array an array of {@code double} values, possibly empty
   */
  public static String join(String separator, double... array) {
    checkNotNull(separator);
    if (array.length == 0) {
      return "";
    }

    // For pre-sizing a builder, just get the right order of magnitude
    StringBuilder builder = new StringBuilder(array.length * 12);
    builder.append(array[0]);
    for (int i = 1; i < array.length; i++) {
      builder.append(separator).append(array[i]);
    }
    return builder.toString();
  }

  /**
   * Returns a comparator that compares two {@code double} arrays
   * lexicographically. That is, it compares, using {@link
   * #compare(double, double)}), the first pair of values that follow any
   * common prefix, or when one array is a prefix of the other, treats the
   * shorter array as the lesser. For example,
   * {@code [] < [1.0] < [1.0, 2.0] < [2.0]}.
   *
   * <p>The returned comparator is inconsistent with {@link
   * Object#equals(Object)} (since arrays support only identity equality), but
   * it is consistent with {@link Arrays#equals(double[], double[])}.
   *
   * @see <a href="http://en.wikipedia.org/wiki/Lexicographical_order">
   *     Lexicographical order article at Wikipedia</a>
   * @since 2.0
   */
  public static Comparator<double[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }

  private enum LexicographicalComparator implements Comparator<double[]> {
    INSTANCE;

    @Override
    public int compare(double[] left, double[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = Doubles.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }

  /**
   * Copies a collection of {@code Double} instances into a new array of
   * primitive {@code double} values.
   *
   * <p>Elements are copied from the argument collection as if by {@code
   * collection.toArray()}.  Calling this method is as thread-safe as calling
   * that method.
   *
   * @param collection a collection of {@code Double} objects
   * @return an array containing the same values as {@code collection}, in the
   *     same order, converted to primitives
   * @throws NullPointerException if {@code collection} or any of its elements
   *     is null
   */
  public static double[] toArray(Collection<Double> collection) {
    if (collection instanceof DoubleArrayAsList) {
      return ((DoubleArrayAsList) collection).toDoubleArray();
    }

    Object[] boxedArray = collection.toArray();
    int len = boxedArray.length;
    double[] array = new double[len];
    for (int i = 0; i < len; i++) {
      // checkNotNull for GWT (do not optimize)
      array[i] = (Double) checkNotNull(boxedArray[i]);
    }
    return array;
  }

  /**
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * Arrays#asList(Object[])}. The list supports {@link List#set(int, Object)},
   * but any attempt to set a value to {@code null} will result in a {@link
   * NullPointerException}.
   *
   * <p>The returned list maintains the values, but not the identities, of
   * {@code Double} objects written to or read from it.  For example, whether
   * {@code list.get(0) == list.get(0)} is true for the returned list is
   * unspecified.
   *
   * <p>The returned list may have unexpected behavior if it contains {@code
   * NaN}, or if {@code NaN} is used as a parameter to any of its methods.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Double> asList(double... backingArray) {
    if (backingArray.length == 0) {
      return Collections.emptyList();
    }
    return new DoubleArrayAsList(backingArray);
  }

  @GwtCompatible
  private static class DoubleArrayAsList extends AbstractList<Double>
      implements RandomAccess, Serializable {
    final double[] array;
    final int start;
    final int end;

    DoubleArrayAsList(double[] array) {
      this(array, 0, array.length);
    }

    DoubleArrayAsList(double[] array, int start, int end) {
      this.array = array;
      this.start = start;
      this.end = end;
    }

    @Override public int size() {
      return end - start;
    }

    @Override public boolean isEmpty() {
      return false;
    }

    @Override public Double get(int index) {
      checkElementIndex(index, size());
      return array[start + index];
    }

    @Override public boolean contains(Object target) {
      // Overridden to prevent a ton of boxing
      return (target instanceof Double)
          && Doubles.indexOf(array, (Double) target, start, end) != -1;
    }

    @Override public int indexOf(Object target) {
      // Overridden to prevent a ton of boxing
      if (target instanceof Double) {
        int i = Doubles.indexOf(array, (Double) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }

    @Override public int lastIndexOf(Object target) {
      // Overridden to prevent a ton of boxing
      if (target instanceof Double) {
        int i = Doubles.lastIndexOf(array, (Double) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }

    @Override public Double set(int index, Double element) {
      checkElementIndex(index, size());
      double oldValue = array[start + index];
      array[start + index] = checkNotNull(element);  // checkNotNull for GWT (do not optimize)
      return oldValue;
    }

    @Override public List<Double> subList(int fromIndex, int toIndex) {
      int size = size();
      checkPositionIndexes(fromIndex, toIndex, size);
      if (fromIndex == toIndex) {
        return Collections.emptyList();
      }
      return new DoubleArrayAsList(array, start + fromIndex, start + toIndex);
    }

    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof DoubleArrayAsList) {
        DoubleArrayAsList that = (DoubleArrayAsList) object;
        int size = size();
        if (that.size() != size) {
          return false;
        }
        for (int i = 0; i < size; i++) {
          if (array[start + i] != that.array[that.start + i]) {
            return false;
          }
        }
        return true;
      }
      return super.equals(object);
    }

    @Override public int hashCode() {
      int result = 1;
      for (int i = start; i < end; i++) {
        result = 31 * result + Doubles.hashCode(array[i]);
      }
      return result;
    }

    @Override public String toString() {
      StringBuilder builder = new StringBuilder(size() * 12);
      builder.append('[').append(array[start]);
      for (int i = start + 1; i < end; i++) {
        builder.append(", ").append(array[i]);
      }
      return builder.append(']').toString();
    }

    double[] toDoubleArray() {
      // Arrays.copyOfRange() requires Java 6
      int size = size();
      double[] result = new double[size];
      System.arraycopy(array, start, result, 0, size);
      return result;
    }

    private static final long serialVersionUID = 0;
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

