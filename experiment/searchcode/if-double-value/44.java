/*
<<<<<<< HEAD
 * Copyright (C) 2011 René Jeschke <rene_jeschke@yahoo.de>
 * See LICENSE.txt for licensing information.
 */
package com.github.rjeschke.weel;

/**
 * A Weel value.
 * 
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
public final class Value
{
    /** This Value's type. */
    ValueType type;
    /** This Value's double value. */
    double number;
    /** This Value's object. */
    Object object;

    /**
     * Creates a Value of type NULL.
     */
    public Value()
    {
        this.type = ValueType.NULL;
    }

    /**
     * Creates a Value of type NUMBER.
     * 
     * @param value
     *            The value.
     */
    public Value(final int value)
    {
        this.type = ValueType.NUMBER;
        this.number = value;
    }

    /**
     * Creates a Value of type NUMBER.
     * 
     * @param value
     *            The value.
     */
    public Value(final double value)
    {
        this.type = ValueType.NUMBER;
        this.number = value;
    }

    /**
     * Creates a Value of type STRING.
     * 
     * @param value
     *            The value.
     */
    public Value(final String value)
    {
        this.type = ValueType.STRING;
        this.object = value;
    }

    /**
     * Creates a Value of type MAP.
     * 
     * @param value
     *            The value.
     */
    public Value(final ValueMap value)
    {
        this.type = ValueType.MAP;
        this.object = value;
    }

    /**
     * Creates a Value of type FUNCTION.
     * 
     * @param value
     *            The value.
     */
    public Value(final WeelFunction value)
    {
        this.type = ValueType.FUNCTION;
        this.object = value;
    }

    /**
     * Creates a Value of type OBJECT.
     * 
     * @param value
     *            The value.
     */
    public Value(final Object value)
    {
        this.type = ValueType.OBJECT;
        this.object = value;
    }

    /**
     * Changes the type of this Value to <code>NULL</code>, clears references.
     */
    public void setNull()
    {
        this.type = ValueType.NULL;
        this.number = 0;
        this.object = null;
    }

    /**
     * Returns a boolean interpretation of this value.
     * 
     * <ul>
     * <li><code>NULL</code> returns <code>false</code></li>
     * <li>A <code>NUMBER</code> equal to 0 returns <code>false</code></li>
     * <li>A <code>STRING</code> with a length of 0 returns <code>false</code></li>
     * <li>A <code>MAP</code> with a length of 0 returns <code>false</code></li>
     * <li>A <code>OBJECT</code> with a value of null returns <code>false</code>
     * </li>
     * <li>Everything else returns <code>true</code></li>
     * </ul>
     * 
     * @return A boolean interpretation of this value.
     */
    public boolean toBoolean()
    {
        switch (this.type)
        {
        case NULL:
            return false;
        case STRING:
            return ((String)this.object).length() > 0;
        case NUMBER:
            return this.number != 0;
        case MAP:
            return ((ValueMap)this.object).size != 0;
        case OBJECT:
            return this.object != null;
        default:
            return true;
        }
    }

    /**
     * Copies this value into another.
     * 
     * @param other
     *            Value to copy to.
     */
    public void copyTo(final Value other)
    {
        other.type = this.type;
        other.number = this.number;
        other.object = this.object;
    }

    /** @see java.lang.Object#clone() */
    @Override
    public Value clone()
    {
        final Value v = new Value();
        this.copyTo(v);
        return v;
    }

    /** @see java.lang.Object#toString() */
    @Override
    public String toString()
    {
        switch (this.type)
        {
        case NULL:
            return "null";
        case NUMBER:
        {
            // Hack
            final long temp = (long)this.number;
            if(temp == this.number)
                return Long.toString(temp);
            return Double.toString(this.number);
        }
        case STRING:
            return (String)this.object;
        case MAP:
            return "map(" + ((ValueMap)this.object).size + ")";
        case FUNCTION:
            return ((WeelFunction)this.object).toString();
        case OBJECT:
            return this.object.toString();
        }
        return "null";
    }

    /**
     * Gets this Value's type.
     * 
     * @return The type of this value.
     */
    public ValueType getType()
    {
        return this.type;
    }

    /**
     * Check if this Value is NULL.
     * 
     * @return <code>true</code> if this Value is NULL.
     */
    public boolean isNull()
    {
        return this.type == ValueType.NULL;
    }

    /**
     * Check if this Value is a NUMBER.
     * 
     * @return <code>true</code> if this Value is a NUMBER.
     */
    public boolean isNumber()
    {
        return this.type == ValueType.NUMBER;
    }

    /**
     * Check if this Value is a STRING.
     * 
     * @return <code>true</code> if this Value is a STRING.
     */
    public boolean isString()
    {
        return this.type == ValueType.STRING;
    }

    /**
     * Check if this Value is a MAP.
     * 
     * @return <code>true</code> if this Value is a MAP.
     */
    public boolean isMap()
    {
        return this.type == ValueType.MAP;
    }

    /**
     * Check if this Value is a FUNCTION.
     * 
     * @return <code>true</code> if this Value is a FUNCTION.
     */
    public boolean isFunction()
    {
        return this.type == ValueType.FUNCTION;
    }

    /**
     * Check if this Value is an OBJECT.
     * 
     * @return <code>true</code> if this Value is an OBJECT.
     */
    public boolean isObject()
    {
        return this.type == ValueType.OBJECT;
    }

    /**
     * Gets this Value's NUMBER.
     * 
     * @return The NUMBER of this Value.
     */
    public double getNumber()
    {
        if (this.type != ValueType.NUMBER)
            throw new WeelException("Value is not a NUMBER");
        return this.number;
    }

    /**
     * Gets this Value's STRING.
     * 
     * @return The STRING of this Value.
     */
    public String getString()
    {
        if (this.type != ValueType.STRING)
            throw new WeelException("Value is not a STRING");
        return (String)this.object;
    }

    /**
     * Gets this Value's MAP.
     * 
     * @return The MAP of this Value.
     */
    public ValueMap getMap()
    {
        if (this.type != ValueType.MAP)
            throw new WeelException("Value is not a MAP");
        return (ValueMap)this.object;
    }

    /**
     * Gets this Value's FUNCTION.
     * 
     * @return The FUNCTION of this Value.
     */
    public WeelFunction getFunction()
    {
        if (this.type != ValueType.FUNCTION)
            throw new WeelException("Value is not a FUNCTION");
        return (WeelFunction)this.object;
    }

    /**
     * Gets this Value's OBJECT.
     * 
     * @return The OBJECT of this Value.
     */
    public Object getObject()
    {
        if (this.type != ValueType.OBJECT)
            throw new WeelException("Value is not an OBJECT");
        return this.object;
    }

    /**
     * Returns the size of this value.
     * 
     * @return The size as a double.
     */
    public double size()
    {
        switch (this.type)
        {
        case NUMBER:
            return Math.abs(this.number);
        case STRING:
            return ((String)this.object).length();
        case MAP:
            return ((ValueMap)this.object).size();
        case FUNCTION:
            return ((WeelFunction)this.object).arguments;
        default:
            return 0;
        }
    }
=======
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * Source:
 * http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/jsr166e/extra/AtomicDouble.java?revision=1.13
 * (Modified to adapt to guava coding conventions and
 * to use AtomicLongFieldUpdater instead of sun.misc.Unsafe)
 */

package com.google.common.util.concurrent;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * A {@code double} value that may be updated atomically.  See the
 * {@link java.util.concurrent.atomic} package specification for
 * description of the properties of atomic variables.  An {@code
 * AtomicDouble} is used in applications such as atomic accumulation,
 * and cannot be used as a replacement for a {@link Double}.  However,
 * this class does extend {@code Number} to allow uniform access by
 * tools and utilities that deal with numerically-based classes.
 *
 * <p><a name="bitEquals">This class compares primitive {@code double}
 * values in methods such as {@link #compareAndSet} by comparing their
 * bitwise representation using {@link Double#doubleToRawLongBits},
 * which differs from both the primitive double {@code ==} operator
 * and from {@link Double#equals}, as if implemented by:
 *  <pre> {@code
 * static boolean bitEquals(double x, double y) {
 *   long xBits = Double.doubleToRawLongBits(x);
 *   long yBits = Double.doubleToRawLongBits(y);
 *   return xBits == yBits;
 * }}</pre>
 *
 * <p>It is possible to write a more scalable updater, at the cost of
 * giving up strict atomicity.  See for example
 * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/DoubleAdder.html">
 * DoubleAdder</a>
 * and
 * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/DoubleMaxUpdater.html">
 * DoubleMaxUpdater</a>.
 *
 * @author Doug Lea
 * @author Martin Buchholz
 * @since 11.0
 */
public class AtomicDouble extends Number implements java.io.Serializable {
  private static final long serialVersionUID = 0L;

  private transient volatile long value;

  private static final AtomicLongFieldUpdater<AtomicDouble> updater =
      AtomicLongFieldUpdater.newUpdater(AtomicDouble.class, "value");

  /**
   * Creates a new {@code AtomicDouble} with the given initial value.
   *
   * @param initialValue the initial value
   */
  public AtomicDouble(double initialValue) {
    value = doubleToRawLongBits(initialValue);
  }

  /**
   * Creates a new {@code AtomicDouble} with initial value {@code 0.0}.
   */
  public AtomicDouble() {
    // assert doubleToRawLongBits(0.0) == 0L;
  }

  /**
   * Gets the current value.
   *
   * @return the current value
   */
  public final double get() {
    return longBitsToDouble(value);
  }

  /**
   * Sets to the given value.
   *
   * @param newValue the new value
   */
  public final void set(double newValue) {
    long next = doubleToRawLongBits(newValue);
    value = next;
  }

  /**
   * Eventually sets to the given value.
   *
   * @param newValue the new value
   */
  public final void lazySet(double newValue) {
    set(newValue);
    // TODO(user): replace with code below when jdk5 support is dropped.
    // long next = doubleToRawLongBits(newValue);
    // updater.lazySet(this, next);
  }

  /**
   * Atomically sets to the given value and returns the old value.
   *
   * @param newValue the new value
   * @return the previous value
   */
  public final double getAndSet(double newValue) {
    long next = doubleToRawLongBits(newValue);
    return longBitsToDouble(updater.getAndSet(this, next));
  }

  /**
   * Atomically sets the value to the given updated value
   * if the current value is <a href="#bitEquals">bitwise equal</a>
   * to the expected value.
   *
   * @param expect the expected value
   * @param update the new value
   * @return {@code true} if successful. False return indicates that
   * the actual value was not bitwise equal to the expected value.
   */
  public final boolean compareAndSet(double expect, double update) {
    return updater.compareAndSet(this,
                                 doubleToRawLongBits(expect),
                                 doubleToRawLongBits(update));
  }

  /**
   * Atomically sets the value to the given updated value
   * if the current value is <a href="#bitEquals">bitwise equal</a>
   * to the expected value.
   *
   * <p>May <a
   * href="http://download.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/package-summary.html#Spurious">
   * fail spuriously</a>
   * and does not provide ordering guarantees, so is only rarely an
   * appropriate alternative to {@code compareAndSet}.
   *
   * @param expect the expected value
   * @param update the new value
   * @return {@code true} if successful
   */
  public final boolean weakCompareAndSet(double expect, double update) {
    return updater.weakCompareAndSet(this,
                                     doubleToRawLongBits(expect),
                                     doubleToRawLongBits(update));
  }

  /**
   * Atomically adds the given value to the current value.
   *
   * @param delta the value to add
   * @return the previous value
   */
  public final double getAndAdd(double delta) {
    while (true) {
      long current = value;
      double currentVal = longBitsToDouble(current);
      double nextVal = currentVal + delta;
      long next = doubleToRawLongBits(nextVal);
      if (updater.compareAndSet(this, current, next)) {
        return currentVal;
      }
    }
  }

  /**
   * Atomically adds the given value to the current value.
   *
   * @param delta the value to add
   * @return the updated value
   */
  public final double addAndGet(double delta) {
    while (true) {
      long current = value;
      double currentVal = longBitsToDouble(current);
      double nextVal = currentVal + delta;
      long next = doubleToRawLongBits(nextVal);
      if (updater.compareAndSet(this, current, next)) {
        return nextVal;
      }
    }
  }

  /**
   * Returns the String representation of the current value.
   * @return the String representation of the current value
   */
  public String toString() {
    return Double.toString(get());
  }

  /**
   * Returns the value of this {@code AtomicDouble} as an {@code int}
   * after a narrowing primitive conversion.
   */
  public int intValue() {
    return (int) get();
  }

  /**
   * Returns the value of this {@code AtomicDouble} as a {@code long}
   * after a narrowing primitive conversion.
   */
  public long longValue() {
    return (long) get();
  }

  /**
   * Returns the value of this {@code AtomicDouble} as a {@code float}
   * after a narrowing primitive conversion.
   */
  public float floatValue() {
    return (float) get();
  }

  /**
   * Returns the value of this {@code AtomicDouble} as a {@code double}.
   */
  public double doubleValue() {
    return get();
  }

  /**
   * Saves the state to a stream (that is, serializes it).
   *
   * @serialData The current value is emitted (a {@code double}).
   */
  private void writeObject(java.io.ObjectOutputStream s)
      throws java.io.IOException {
    s.defaultWriteObject();

    s.writeDouble(get());
  }

  /**
   * Reconstitutes the instance from a stream (that is, deserializes it).
   */
  private void readObject(java.io.ObjectInputStream s)
      throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();

    set(s.readDouble());
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

