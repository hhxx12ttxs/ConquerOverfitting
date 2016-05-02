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
package ca.grimoire.jnoise.modules.composition;

import ca.grimoire.jnoise.modules.Module;
import ca.grimoire.jnoise.util.Hash;

/**
 * Noise module that selects and blends between two source modules based on the
 * value of a selector module. Whenever the selector module returns a value
 * below the selection threshold, the lower module's noise value is selected; if
 * the selector module returns a value above the threshold, the upper noise
 * module is used instead. To smooth the transition, a falloff distance may be
 * provided, over which the two modules will be blended together in proportion
 * to the selector module's value.
 * <p>
 * If you are using the included XML noise configuration system, Select modules
 * can be declared as
 * <p>
 * <blockquote>
 * <code>&lt;select falloff="<var>falloff</var>" threshold="<var>threshold</var>" &gt;<br>
 * &nbsp;<var>&lt;lower source module /&gt;</var><br>
 * &nbsp;<var>&lt;upper source module /&gt;</var><br>
 * &nbsp;<var>&lt;selector module /&gt;</var><br>
 * &lt;/select&gt;</code> </blockquote>
 */
public final class Select implements Module {

  private static double lerp (double a, double b, double x, double lower,
      double upper) {
    assert (lower <= upper);
    assert (lower <= x);
    assert (x <= upper);

    double delta = normalise (x, lower, upper);
    double valueRange = b - a;
    return delta * valueRange + a;
  }

  private static double normalise (double value, double lowerBound,
      double upperBound) {
    assert (lowerBound < upperBound);

    return (value - lowerBound) / (upperBound - lowerBound);
  }

  private static double select (double a, double b, double selector,
      double threshold, double falloff) {
    assert (falloff >= 0);

    double lower = threshold - falloff;
    double upper = threshold + falloff;

    // Using <= here means that a fade of 0 doesn't cause a crash by arbitrarily
    // assigning the exact fade point to one side.
    if (selector <= lower)
      return a;
    if (selector > upper)
      return b;

    return lerp (a, b, selector, lower, upper);
  }

  /**
   * Creates a new Select module.
   * 
   * @param lowerModule
   *          the module providing values when the selector generates values
   *          below the selector.
   * @param upperModule
   *          the module providing values when the selector generates values
   *          above the selector
   * @param selector
   *          the module producing selector values.
   * @param threshold
   *          the selector value to switch sources at.
   * @param falloff
   *          the size of the falloff region around the threshold, where the
   *          sources are mixed.
   */
  public Select (Module lowerModule, Module upperModule, Module selector,
      double threshold, double falloff) {
    assert (lowerModule != null);
    assert (upperModule != null);
    assert (selector != null);

    if (falloff < 0.0)
      throw new IllegalArgumentException ("Selection falloff distance ("
          + falloff + ") must be non-negative.");

    this.lowerModule = lowerModule;
    this.upperModule = upperModule;
    this.selector = selector;
    this.threshold = threshold;
    this.falloff = falloff;
  }

  /**
   * Compares this module for equality with another object. Selector modules are
   * equal only to other selector modules with equal source modules and the same
   * selection threshold and falloff value.
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
    else if (!(object instanceof Select))
      return false;

    Select other = (Select) object;
    return lowerModule.equals (other.getLowerModule ())
        && upperModule.equals (other.getUpperModule ())
        && selector.equals (other.getSelector ())
        && threshold == other.getThreshold () && falloff == other.getFalloff ();
  }

  /**
   * Returns the falloff distance for the module. The Select module will blend
   * the two modules together whenever the selector returns a value within the
   * falloff distance from the threshold by interpolating between the two noise
   * sources.
   * 
   * @return the selector falloff distance.
   */
  public double getFalloff () {
    return falloff;
  }

  /**
   * Returns the module producing noise when the selector is below the threshold
   * value.
   * 
   * @return the selector's lower source module.
   */
  public Module getLowerModule () {
    return lowerModule;
  }

  /**
   * Returns the module producing selection values.
   * 
   * @return the selector module.
   */
  public Module getSelector () {
    return selector;
  }

  /**
   * Returns the threshold value where the dominant module changes. Below this
   * value, the dominant (or only) module providing noise is the lower source;
   * above the threshold value the upper source dominates.
   * 
   * @return the selection threshold.
   */
  public double getThreshold () {
    return threshold;
  }

  /**
   * Returns the module producing noise when the selector is above the threshold
   * value.
   * 
   * @return the selector's upper source module.
   */
  public Module getUpperModule () {
    return upperModule;
  }

  /**
   * Returns the noise value for a location. The selector module generates a
   * value used to determine which side of the threshold, by how far, to blend
   * towards.
   * 
   * @param x
   *          the X coordinate of the location.
   * @param y
   *          the Y coordinate of the location.
   * @param z
   *          the Z coordinate of the location.
   * @return the selected noise at the location.
   * @see jnoise.modules.Module#getValue(double, double, double)
   */
  public double getValue (double x, double y, double z) {
    double lowerValue = lowerModule.getValue (x, y, z);
    double upperValue = upperModule.getValue (x, y, z);
    double selectorValue = selector.getValue (x, y, z);

    return select (lowerValue, upperValue, selectorValue);
  }

  /**
   * Generates a hashcode for this selector module.
   * 
   * @return the module's hashcode.
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode () {
    return lowerModule.hashCode () ^ upperModule.hashCode ()
        ^ selector.hashCode () ^ Hash.hashDouble (threshold)
        ^ Hash.hashDouble (falloff);
  }

  private double select (double lower, double upper, double selector) {
    return select (lower, upper, selector, threshold, falloff);
  }

  private final double falloff;
  private final Module lowerModule;
  private final Module selector;
  private final double threshold;
  private final Module upperModule;
}

