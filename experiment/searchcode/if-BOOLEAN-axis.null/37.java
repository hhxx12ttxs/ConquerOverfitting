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
package ca.grimoire.jnoise.modules.basic;

import ca.grimoire.jnoise.modules.Module;

/**
 * Generator module that calculates the distance from the requested location to
 * the X axis.
 * <p>
 * This module is completely unconfigurable, so all instances are necessarily
 * equivalent. An instance <code><var>MODULE</var></code> is provided.
 * <p>
 * If you are using the included XML noise configuration system, Axis modules
 * can be declared as
 * <p>
 * <blockquote> <code>&lt;axis /&gt;</code> </blockquote>
 */
public final class Axis implements Module {

  /** An axis distance module. */
  public static final Axis MODULE = new Axis ();

  /**
   * Compares this module for equality with another object. This module is equal
   * to an object if and only if the object is also an Axis module.
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

    return object instanceof Axis;
  }

  /**
   * Calculates the distance from the passed location to the X axis.
   * 
   * @param x
   *          the X coordinate of the location.
   * @param y
   *          the Y coordinate of the location.
   * @param z
   *          the Z coordinate of the location.
   * @return the distance from the location to the origin.
   * @see jnoise.modules.Module#getValue(double, double, double)
   */
  public double getValue (double x, double y, double z) {
    return Math.sqrt (y * y + z * z);
  }

  /**
   * Calcuates a hashcode for this object. The hash code is computed from the
   * JVM-derived hashcode for the <code>MODULE</code> instance.
   * 
   * @return the module's hashcode.
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode () {
    if (this == MODULE)
      // Pass up to Object#hashCode()
      return super.hashCode ();

    return MODULE.hashCode ();
  }
}

