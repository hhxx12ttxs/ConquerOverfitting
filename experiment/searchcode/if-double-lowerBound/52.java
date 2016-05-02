/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Copyright (C) 2005 Owen Jacobson <angrybaldguy@gmail.com>
 */
package ca.grimoire.jnoise.modules.map;

import ca.grimoire.jnoise.modules.Module;
import ca.grimoire.jnoise.modules.SingleSourceModule;
import ca.grimoire.jnoise.util.Hash;

/**
 * Module that clamps the output of another module to a fixed range.
 * <p>
 * If you are using the included XML noise configuration system, Clamp modules
 * can be declared as
 * <p>
 * <blockquote>
 * <code>&lt;clamp lower="<var>bound</var>" upper="<var>bound</var>"&gt;<br>
 * &nbsp;<var>&lt;source module /&gt;</var><br>
 * &lt;/clamp&gt;</code> </blockquote>
 */
public final class Clamp extends SingleSourceModule {

  /**
   * Create a new Clamp module. The passed bounds must be in ascending order, or
   * an <code>IllegalArgumentException</code> will be thrown.
   * 
   * @param source
   *          the module to clamp.
   * @param lowerBound
   *          the lower bound to clamp to.
   * @param upperBound
   *          the upper bound to clamp to.
   */
  public Clamp (Module source, double lowerBound, double upperBound) {
    super (source);

    if (lowerBound > upperBound)
      throw new IllegalArgumentException ("Clamp lower bound (" + lowerBound
          + ") must be less than or equal to upper bound (" + upperBound + ")");

    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * Compare the module for equality with another object. The module is equal to
   * an object if and only if the object is also a Clamp module with the same
   * bounds fed by an equal source module.
   * 
   * @param object
   *          the object to compare.
   * @return <code>true</code> if <var>object</var> is an equal module.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals (Object object) {
    if (object == null)
      return false;
    else if (!(object instanceof Clamp))
      return false;

    Clamp other = (Clamp) object;
    return getSource ().equals (other.getSource ())
        && other.getLowerBound () == lowerBound
        && other.getUpperBound () == upperBound;
  }

  /**
   * Get the lower bound of the module's clamp range. Generated noise values
   * will never be lower than this.
   * 
   * @return the module's lower bound.
   */
  public double getLowerBound () {
    return lowerBound;
  }

  /**
   * Get the upper bound of the module's clamp range. Generated noise values
   * will never be greater than this.
   * 
   * @return the module's upper bound.
   */
  public double getUpperBound () {
    return upperBound;
  }

  /**
   * Get the value at a given location. The value is clamped to the module's
   * range and will never be less than the lower bound nor greater than the
   * upper bound.
   * 
   * @param x
   *          the X coordinate of the location.
   * @param y
   *          the Y coordinate of the location.
   * @param z
   *          the Z coordinate of the location.
   * @return the clamped value at the location.
   * @see jnoise.modules.Module#getValue(double, double, double)
   */
  public double getValue (double x, double y, double z) {
    return clamp (getSource ().getValue (x, y, z));
  }

  /**
   * Calcuate a hashcode for the object according to the general
   * hashcode/equality contract for Object. The hash code is computed from the
   * hashcode of the underlying module and the bounds of the module.
   * 
   * @return the module's hashcode.
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode () {
    return getSource ().hashCode () ^ Hash.hashDouble (lowerBound)
        ^ Hash.hashDouble (upperBound);
  }

  private double clamp (double value) {
    assert (lowerBound <= upperBound);

    if (value < lowerBound)
      return lowerBound;
    if (value > upperBound)
      return upperBound;

    return value;
  }

  private final double lowerBound;
  private final double upperBound;
}

