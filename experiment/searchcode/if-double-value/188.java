<<<<<<< HEAD
/*
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
 * $Revision$
 * $Date$
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
=======

package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.JsonWriter.OutputType;

/** Container for a JSON object, array, string, double, long, boolean, or null.
 * <p>
 * JsonValue children are a linked list. Iteration of arrays or objects is easily done using a for loop, like the example below.
 * This is more efficient than accessing children by index when there are many children.<br>
 * 
 * <pre>
 * JsonValue map = ...;
 * for (JsonValue entry = map.child(); entry != null; entry = entry.next())
 * 	System.out.println(entry.name() + " = " + entry.asString());
 * </pre>
 * @author Nathan Sweet */
public class JsonValue {
	private String name;
	private ValueType type;

	private String stringValue;
	private Boolean booleanValue;
	private Double doubleValue;
	private long longValue;

	private JsonValue child, next, prev;
	private int size;

	public JsonValue (ValueType type) {
		this.type = type;
	}

	/** @param value May be null. */
	public JsonValue (String value) {
		set(value);
	}

	public JsonValue (double value) {
		set(value);
	}

	public JsonValue (long value) {
		set(value);
	}

	public JsonValue (boolean value) {
		set(value);
	}

	/** Returns the child at the specified index.
	 * @return May be null. */
	public JsonValue get (int index) {
		JsonValue current = child;
		while (current != null && index > 0) {
			index--;
			current = current.next;
		}
		return current;
	}

	/** Returns the child with the specified name.
	 * @return May be null. */
	public JsonValue get (String name) {
		JsonValue current = child;
		while (current != null && !current.name.equalsIgnoreCase(name))
			current = current.next;
		return current;
	}

	/** Returns the child at the specified index.
	 * @throws IllegalArgumentException if the child was not found. */
	public JsonValue require (int index) {
		JsonValue current = child;
		while (current != null && index > 0) {
			index--;
			current = current.next;
		}
		if (current == null) throw new IllegalArgumentException("Child not found with index: " + index);
		return current;
	}

	/** Returns the child with the specified name.
	 * @throws IllegalArgumentException if the child was not found. */
	public JsonValue require (String name) {
		JsonValue current = child;
		while (current != null && !current.name.equalsIgnoreCase(name))
			current = current.next;
		if (current == null) throw new IllegalArgumentException("Child not found with name: " + name);
		return current;
	}

	/** Removes the child with the specified name.
	 * @return May be null. */
	public JsonValue remove (int index) {
		JsonValue child = get(index);
		if (child == null) return null;
		if (child.prev == null) {
			this.child = child.next;
			if (this.child != null) this.child.prev = null;
		} else {
			child.prev.next = child.next;
			if (child.next != null) child.next.prev = child.prev;
		}
		size--;
		return child;
	}

	/** Removes the child with the specified name.
	 * @return May be null. */
	public JsonValue remove (String name) {
		JsonValue child = get(name);
		if (child == null) return null;
		if (child.prev == null) {
			this.child = child.next;
			if (this.child != null) this.child.prev = null;
		} else {
			child.prev.next = child.next;
			if (child.next != null) child.next.prev = child.prev;
		}
		size--;
		return child;
	}

	/** Returns this number of children in the array or object. */
	public int size () {
		return size;
	}

	/** Returns this value as a string.
	 * @return May be null if this value is null.
	 * @throws IllegalStateException if this an array or object. */
	public String asString () {
		if (stringValue != null) return stringValue;
		if (doubleValue != null) {
			if (doubleValue % 1 == 0) return Long.toString(longValue);
			return Double.toString(doubleValue);
		}
		if (booleanValue != null) return Boolean.toString(booleanValue);
		if (type == ValueType.nullValue) return null;
		throw new IllegalStateException("Value cannot be converted to string: " + type);
	}

	/** Returns this value as a float.
	 * @throws IllegalStateException if this an array or object. */
	public float asFloat () {
		if (doubleValue != null) return doubleValue.floatValue();
		if (stringValue != null) {
			try {
				return Float.parseFloat(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to float: " + type);
	}

	/** Returns this value as a double.
	 * @throws IllegalStateException if this an array or object. */
	public double asDouble () {
		if (doubleValue != null) return doubleValue;
		if (stringValue != null) {
			try {
				return Double.parseDouble(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to double: " + type);
	}

	/** Returns this value as a long.
	 * @throws IllegalStateException if this an array or object. */
	public long asLong () {
		if (doubleValue != null) return longValue;
		if (stringValue != null) {
			try {
				return Long.parseLong(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to long: " + type);
	}

	/** Returns this value as an int.
	 * @throws IllegalStateException if this an array or object. */
	public int asInt () {
		if (doubleValue != null) return (int)longValue;
		if (stringValue != null) {
			try {
				return Integer.parseInt(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to int: " + type);
	}

	/** Returns this value as a boolean.
	 * @throws IllegalStateException if this an array or object. */
	public boolean asBoolean () {
		if (booleanValue != null) return booleanValue;
		if (doubleValue != null) return longValue == 0;
		if (stringValue != null) return stringValue.equalsIgnoreCase("true");
		throw new IllegalStateException("Value cannot be converted to boolean: " + type);
	}

	/** Finds the child with the specified name and returns its first child.
	 * @return May be null. */
	public JsonValue getChild (String name) {
		JsonValue child = get(name);
		return child == null ? null : child.child;
	}

	/** Finds the child with the specified name and returns it as a string. Returns defaultValue if not found.
	 * @param defaultValue May be null. */
	public String getString (String name, String defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue() || child.isNull()) ? defaultValue : child.asString();
	}

	/** Finds the child with the specified name and returns it as a float. Returns defaultValue if not found. */
	public float getFloat (String name, float defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asFloat();
	}

	/** Finds the child with the specified name and returns it as a double. Returns defaultValue if not found. */
	public double getDouble (String name, double defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asDouble();
	}

	/** Finds the child with the specified name and returns it as a long. Returns defaultValue if not found. */
	public long getLong (String name, long defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asLong();
	}

	/** Finds the child with the specified name and returns it as an int. Returns defaultValue if not found. */
	public int getInt (String name, int defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asInt();
	}

	/** Finds the child with the specified name and returns it as a boolean. Returns defaultValue if not found. */
	public boolean getBoolean (String name, boolean defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asBoolean();
	}

	/** Finds the child with the specified name and returns it as a string.
	 * @throws IllegalArgumentException if the child was not found. */
	public String getString (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asString();
	}

	/** Finds the child with the specified name and returns it as a float.
	 * @throws IllegalArgumentException if the child was not found. */
	public float getFloat (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asFloat();
	}

	/** Finds the child with the specified name and returns it as a double.
	 * @throws IllegalArgumentException if the child was not found. */
	public double getDouble (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asDouble();
	}

	/** Finds the child with the specified name and returns it as a long.
	 * @throws IllegalArgumentException if the child was not found. */
	public long getLong (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asLong();
	}

	/** Finds the child with the specified name and returns it as an int.
	 * @throws IllegalArgumentException if the child was not found. */
	public int getInt (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asInt();
	}

	/** Finds the child with the specified name and returns it as a boolean.
	 * @throws IllegalArgumentException if the child was not found. */
	public boolean getBoolean (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asBoolean();
	}

	/** Finds the child with the specified index and returns it as a string.
	 * @throws IllegalArgumentException if the child was not found. */
	public String getString (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asString();
	}

	/** Finds the child with the specified index and returns it as a float.
	 * @throws IllegalArgumentException if the child was not found. */
	public float getFloat (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asFloat();
	}

	/** Finds the child with the specified index and returns it as a double.
	 * @throws IllegalArgumentException if the child was not found. */
	public double getDouble (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asDouble();
	}

	/** Finds the child with the specified index and returns it as a long.
	 * @throws IllegalArgumentException if the child was not found. */
	public long getLong (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asLong();
	}

	/** Finds the child with the specified index and returns it as an int.
	 * @throws IllegalArgumentException if the child was not found. */
	public int getInt (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asInt();
	}

	/** Finds the child with the specified index and returns it as a boolean.
	 * @throws IllegalArgumentException if the child was not found. */
	public boolean getBoolean (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asBoolean();
	}

	public ValueType type () {
		return type;
	}

	public void setType (ValueType type) {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		this.type = type;
	}

	public boolean isArray () {
		return type == ValueType.array;
	}

	public boolean isObject () {
		return type == ValueType.object;
	}

	public boolean isString () {
		return type == ValueType.stringValue;
	}

	/** Returns true if this is a double or long value. */
	public boolean isNumber () {
		return type == ValueType.doubleValue || type == ValueType.longValue;
	}

	public boolean isDouble () {
		return type == ValueType.doubleValue;
	}

	public boolean isLong () {
		return type == ValueType.longValue;
	}

	public boolean isBoolean () {
		return type == ValueType.booleanValue;
	}

	public boolean isNull () {
		return type == ValueType.nullValue;
	}

	/** Returns true if this is not an array or object. */
	public boolean isValue () {
		switch (type) {
		case stringValue:
		case doubleValue:
		case longValue:
		case booleanValue:
		case nullValue:
			return true;
		}
		return false;
	}

	/** Returns the name for this object value.
	 * @return May be null. */
	public String name () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	/** Returns the first child for this object or array.
	 * @return May be null. */
	public JsonValue child () {
		return child;
	}

	public void addChild (JsonValue newChild) {
		size++;
		JsonValue current = child;
		if (current == null) {
			child = newChild;
			return;
		}
		while (true) {
			if (current.next == null) {
				current.next = newChild;
				newChild.prev = current;
				return;
			}
			current = current.next;
		}
	}

	/** Returns the next sibling of this value.
	 * @return May be null. */
	public JsonValue next () {
		return next;
	}

	public void setNext (JsonValue next) {
		this.next = next;
	}

	/** Returns the previous sibling of this value.
	 * @return May be null. */
	public JsonValue prev () {
		return prev;
	}

	public void setPrev (JsonValue prev) {
		this.prev = prev;
	}

	/** @param value May be null. */
	public void set (String value) {
		stringValue = value;
		type = value == null ? ValueType.nullValue : ValueType.stringValue;
	}

	public void set (double value) {
		doubleValue = value;
		longValue = (long)value;
		type = ValueType.doubleValue;
	}

	public void set (long value) {
		longValue = value;
		doubleValue = (double)value;
		type = ValueType.longValue;
	}

	public void set (boolean value) {
		booleanValue = value;
		type = ValueType.booleanValue;
	}

	public String toString () {
		return prettyPrint(OutputType.minimal, 0);
	}

	public String prettyPrint (OutputType outputType, int singleLineColumns) {
		StringBuilder buffer = new StringBuilder(512);
		prettyPrint(this, buffer, outputType, 0, singleLineColumns);
		return buffer.toString();
	}

	private void prettyPrint (JsonValue object, StringBuilder buffer, OutputType outputType, int indent, int singleLineColumns) {
		if (object.isObject()) {
			if (object.child() == null) {
				buffer.append("{}");
			} else {
				boolean newLines = !isFlat(object);
				int start = buffer.length();
				outer:
				while (true) {
					buffer.append(newLines ? "{\n" : "{ ");
					int i = 0;
					for (JsonValue child = object.child(); child != null; child = child.next()) {
						if (newLines) indent(indent, buffer);
						buffer.append(outputType.quoteName(child.name()));
						buffer.append(": ");
						prettyPrint(child, buffer, outputType, indent + 1, singleLineColumns);
						if (child.next() != null) buffer.append(",");
						buffer.append(newLines ? '\n' : ' ');
						if (!newLines && buffer.length() - start > singleLineColumns) {
							buffer.setLength(start);
							newLines = true;
							continue outer;
						}
					}
					break;
				}
				if (newLines) indent(indent - 1, buffer);
				buffer.append('}');
			}
		} else if (object.isArray()) {
			if (object.child() == null) {
				buffer.append("[]");
			} else {
				boolean newLines = !isFlat(object);
				int start = buffer.length();
				outer:
				while (true) {
					buffer.append(newLines ? "[\n" : "[ ");
					for (JsonValue child = object.child(); child != null; child = child.next()) {
						if (newLines) indent(indent, buffer);
						prettyPrint(child, buffer, outputType, indent + 1, singleLineColumns);
						if (child.next() != null) buffer.append(",");
						buffer.append(newLines ? '\n' : ' ');
						if (!newLines && buffer.length() - start > singleLineColumns) {
							buffer.setLength(start);
							newLines = true;
							continue outer;
						}
					}
					break;
				}
				if (newLines) indent(indent - 1, buffer);
				buffer.append(']');
			}
		} else if (object.isString()) {
			buffer.append(outputType.quoteValue(object.asString()));
		} else if (object.isDouble()) {
			double doubleValue = object.asDouble();
			long longValue = object.asLong();
			buffer.append(doubleValue == longValue ? longValue : doubleValue);
		} else if (object.isLong()) {
			buffer.append(object.asLong());
		} else if (object.isBoolean()) {
			buffer.append(object.asBoolean());
		} else if (object.isNull()) {
			buffer.append("null");
		} else
			throw new SerializationException("Unknown object type: " + object);
	}

	static private boolean isFlat (JsonValue object) {
		for (JsonValue child = object.child(); child != null; child = child.next())
			if (child.isObject() || child.isArray()) return false;
		return true;
	}

	static private void indent (int count, StringBuilder buffer) {
		for (int i = 0; i < count; i++)
			buffer.append('\t');
	}

	public enum ValueType {
		object, array, stringValue, doubleValue, longValue, booleanValue, nullValue
>>>>>>> 76aa07461566a5976980e6696204781271955163
	}
}

