// $Id: VariableStandardized.java,v 1.1 2007/03/26 13:07:24 dmedv Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ucar.nc2;

import ucar.ma2.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

/**
 * A "standardized" read-only Variable which implements:
 * <ul>
 * <li> packed data using <code> scale_factor and add_offset </code>
 * <li> invalid/missing data using <code> valid_min, valid_max, valid_range,
 *   missing_value or _FillValue </code>
 * </ul>
 * if those "standard attributes" are present. If they are not present,
 * it acts just like the original Variable. Uses the Decorator design pattern.
 *
 * <h2>Standard Use</h2>
 * <p> <b>Implementation rules for missing data</b>. Here "missing data" is a general
 *   name for invalid/never written/missing values. Use this interface when you dont need to
 *   distinguish these variants. See below for a lower-level interface if you do need to
 *   distinguish between them.
 * <ol>
 * <li> By default, hasMissing() is true if any of hasInvalidData(), hasFillData() or
 *   hasMissingValue() are true (see below). You can modify this behavior in the constuctor
 *   or by calling setInvalidDataIsMissing(), setFillDataIsMissing(), or setMissingValueIsMissing().
 * <li> Test specific values through isMissing(double val). Note that the data is converted and
 *   compared as a double.
 * <li> Data values of float or double NaN are considered missing data and will return
 *   true if called with isMissing(). (However hasMissing() will not detect if you are
 *   setting NaNs yourself).
 * <li> if you do not need to distinguish between _FillValue, missing_value and invalid, then
 *   set useNaNs = true in the constructor. When the Variable element type is float or double
 *   (or is set to double because its packed), then this sets isMissing() data values to NaNs, which
 *   makes further comparisions more efficient.
 * </ol>
 *
 * <p> <b>Implementation rules for scale/offset</b>
 * <ol>
 * <li> If scale_factor and/or add_offset variable attributes are present,
 *   then this is a "packed" Variable.
 * <li> The Variable element type is converted in the following way:
 * <ul>
 *   <li> If hasMissing() is not true:
 *     set to the widest type of 1) the data type 2) the scale_factor attribute type
 *     and 3) the add_offset attribute type. (byte < short < int < float < double ).
 *   <li> If hasMissing() is true:
 *     set to float if all attributes used are float (scale_factor, add_offset
 *     valid_min, valid_max, valid_range, missing_data and _FillValue) else
 *     set to type double.
 * </ul>
 * <li> external (packed) data is converted to internal (unpacked) data transparently
 *   during the read() call.
 * </ol>
 *
 * <p> <b>Implementation rules for missing data with scale/offset</b>
 * <ol>
 * <li> _FillValue and missing_value values are always in the units of the external
 *    (packed) data.
 * <li> If valid_range is the same type as scale_factor (actually the wider of
 *     scale_factor and add_offset) and this is wider than the external data, then it
 *     will be interpreted as being in the units of the internal (unpacked) data.
 *     Otherwise it is in the units of the external (packed) data.
 * </ol>
 *
 * <h2> Low Level Access </h2>
 *   The following provide more direct access to missing/invalid data. These are mostly convenience
 *   routines for checking the standard attributes. If you set useNaNs = true in the constructor,
 *   these routines cannot be used when the data has type float or double.
 *
 * <p> <b>Implementation rules for valid_range</b>
 * <ol>
 * <li> if valid_range is present, valid_min and valid_max attributes are
 *   ignored. Otherwise, the valid_min and/or valid_max is used to construct
 *   a valid range. If any of these exist, hasInvalidData() is true.
 * <li> To test a specific value, call isInvalidData(). Note that the data is converted and
 *   compared as a double. Or get the range through getValidMin() and getValidMax().
 * </ol>
 *
 * <p> <b>Implementation rules for _FillData </b>
 * <ol>
 * <li> if the _FillData attribute is present, it must have a scalar value of the same
 *   type as the data. In this case, hasFillData() returns true.
 * <li> Test specific values through isFillValue(). Note that the data is converted and
 *   compared as a double.
 * </ol>
 *
 * <p> <b>Implementation rules for missing_value</b>
 * <ol>
 * <li> if the missing_value attribute is present, it must have a scalar or vector
 *   value of the same type as the data. In this case, hasMissingValue() returns true.
 * <li> Test specific values through isMissingValue(). Note that the data is converted and
 *   compared as a double.
 * </ol>
 *
 * <p> <h2>Strategies for using VariableStandardized</h2>
 * <ol>
 *   <li> Low-level: use the is/has InvalidData/FillData/missingValue routines
 *     to "roll-your own" tests for the various kinds of missing/invalid data.
 *   <li> Standard: use is/hasMissing() to test for missing data when you dont need to
 *     distinguish between the variants. Use the setXXXisMissing() to customize the behavior
 *     if needed.
 *   <li> Efficient : If you expect to scan more than once for missing values, and
 *     you are not distinguishing between InvalidData/FillData/missingValue, then
 *     set useNaNs in the constructor. This sets isMissing() data values to NaNs when reading,
 *     and subsequent checks are more efficient.
 *  </ol>
 *

 * @author caron
 * @version $Revision: 1.1 $ $Date: 2007/03/26 13:07:24 $
 */

public class VariableStandardized extends Variable {
  private Variable orgVar;
  private boolean useNaNs;

  private boolean hasScaleOffset = false;
  private double scale = 1.0, offset = 0.0;

  private boolean hasValidRange = false, hasValidMin = false, hasValidMax = false;
  private boolean hasInvalidData = false;
  private double valid_min = Double.MIN_VALUE, valid_max = Double.MAX_VALUE;

  private boolean hasFillValue = false;
  private double fillValue;

  private boolean hasMissingValue = false;
  private double[] missingValue;

  private boolean invalidDataIsMissing, fillValueIsMissing, missingDataIsMissing;
  private boolean debug = false, debugRead = false;

  /**
   * Constructor, default values.
   * @param orgVar: the original Variable to decorate.
   */
  public VariableStandardized( Variable orgVar) {
    this(orgVar, false);
  }

    /**
   * Constructor.
   * @param orgVar: the original Variable to decorate.
   * @param useNaNs: pre-fill isMissing() data with NaNs
   */
  public VariableStandardized( Variable orgVar, boolean useNaNs) {
    this(orgVar, useNaNs, true, true, true);
  }

  /**
   * Constructor.
   * @param orgVar: the original Variable to decorate.
   * @param boolean useNaNs: pre-fill isMissing() data with NaNs
   * @param boolean fillValueIsMissing : use _FillValue for isMissing()
   * @param boolean invalidDataIsMissing: use valid_range for isMissing()
   * @param boolean missingDataIsMissing: use missing_value for isMissing()
   */
  public VariableStandardized( Variable orgVar, boolean useNaNs, boolean fillValueIsMissing,
    boolean invalidDataIsMissing, boolean missingDataIsMissing) {

    super(orgVar.getName());
    shape = orgVar.getShape();
    dimensions = orgVar.getDimensions();
    attributes = orgVar.getAttributes();
    isCoordinateVariable = orgVar.isCoordinateVariable();

    this.orgVar = orgVar;
    this.useNaNs = useNaNs;
    this.fillValueIsMissing = fillValueIsMissing;
    this.invalidDataIsMissing = invalidDataIsMissing;
    this.missingDataIsMissing = missingDataIsMissing;

    Class scaleType = null, missType = null, validType = null, fillType = null;
    if (debug) System.out.println("VariableStandardized = "+ orgVar.getName());
    Attribute att;

      // scale and offset
    if (null != (att = orgVar.findAttribute("scale_factor"))) {
      if (!att.isString()) {
        scale = att.getNumericValue().doubleValue();
        hasScaleOffset = true;
        scaleType = att.getValueType();
        if (debug) System.out.println("scale = "+ scale+" type "+scaleType.getName());
      }
    }
    if (null != (att = orgVar.findAttribute("add_offset"))) {
      if (!att.isString()) {
        offset = att.getNumericValue().doubleValue();
        hasScaleOffset = true;
        Class offType = att.getValueType();
        if (rank(offType) > rank(scaleType))
          scaleType = offType;
        if (debug) System.out.println("offset = "+ offset);
      }
    }

      ////// missing data : valid_range
    if (null != (att = orgVar.findAttribute("valid_range"))) {
      if (!att.isString() && att.isArray()) {
        valid_min = att.getNumericValue(0).doubleValue();
        valid_max = att.getNumericValue(1).doubleValue();
        hasValidRange = true;
        validType = att.getValueType();
        if (debug) System.out.println("valid_range = "+ valid_min+" "+valid_max);
      }
    }
    if (!hasValidRange) {
      if (null != (att = orgVar.findAttribute("valid_min"))) {
        if (!att.isString()) {
          valid_min = att.getNumericValue().doubleValue();
          hasValidMin = true;
          validType = att.getValueType();
          if (debug) System.out.println("valid_min = "+ valid_min);
        }
      }
      if (null != (att = orgVar.findAttribute("valid_max"))) {
        if (!att.isString()) {
          valid_max = att.getNumericValue().doubleValue();
          hasValidMax = true;
          Class t = att.getValueType();
          if (rank(t) > rank(validType))
            validType = t;
          if (debug) System.out.println("valid_min = "+ valid_max);
        }
      }
      if (hasValidMin && hasValidMax)
        hasValidRange = true;
    }
    boolean hasValidData = hasValidMin || hasValidMax || hasValidRange;

      /// _FillValue
    if ((null != (att = orgVar.findAttribute("_FillValue"))) && !att.isString()) {
      fillValue = att.getNumericValue().doubleValue();
      hasFillValue = true;
      fillType = att.getValueType();
      if (debug) System.out.println("missing_datum from _FillValue = "+ fillValue);
    }

      /// missing_value
    if ((null != (att = orgVar.findAttribute("missing_value"))) && !att.isString()) {
      if (!att.isArray()) {
        missingValue = new double[1];
        missingValue[0] = att.getNumericValue().doubleValue();
        if (debug) System.out.println("missing_datum = "+ missingValue[0]);
      } else {
        int n = att.getLength();
        missingValue = new double[n];
        if (debug) System.out.print("missing_data = ");
        for (int i=0; i<n; i++) {
          missingValue[i] = att.getNumericValue(i).doubleValue();
          if (debug) System.out.print(" "+missingValue[i]);
        }
        if (debug) System.out.println();
      }
      missType = att.getValueType();

      hasMissingValue = true;
    }

    // missing
    boolean hasMissing = (invalidDataIsMissing && hasValidData) ||
                ( fillValueIsMissing && hasFillValue) ||
                ( missingDataIsMissing && hasMissingValue);

      /// assign element type
    elementType = orgVar.getElementType();
    if (hasScaleOffset) {
      if (hasMissing) {
        // has missing data : must be float or double
        if (rank(scaleType) > rank(elementType))
          elementType = scaleType;
        if (missingDataIsMissing && rank(missType) > rank(elementType))
          elementType = missType;
        if (fillValueIsMissing && rank(fillType) > rank(elementType))
          elementType = fillType;
        if (invalidDataIsMissing && rank(validType) > rank(elementType))
          elementType = validType;
        if (rank(elementType) < rank(double.class))
          elementType = float.class;
      } else {
        // no missing data; can use wider of data and scale
        if (rank(scaleType) > rank(elementType))
          elementType = scaleType;
      }
    }
    if (debug) System.out.println("assign elemType = "+ elementType);

    // deal with case when theres both missing data and scaled data
    if (hasScaleOffset) {
      // fillValue always external (packed)
      if (hasFillValue) {
        fillValue = scale * fillValue + offset;
        if (debug) System.out.println("scale the fillValue");
      }

      // missingValue always external (packed)
      if (hasMissingValue) {
        for (int i=0; i<missingValue.length; i++)
          missingValue[i] = scale * missingValue[i] + offset;
        if (debug) System.out.println("scale the missing values");
      }

      // validData may be external or internal
      if (hasValidData) {
        Class orgType = orgVar.getElementType();

        // If valid_range is the same type as scale_factor (actually the wider of
        // scale_factor and add_offset) and this is wider than the external data, then it
        // will be interpreted as being in the units of the internal (unpacked) data.
        // Otherwise it is in the units of the external (unpacked) data.
        if ( !((rank(validType) == rank(scaleType)) && (rank(scaleType) > rank(orgType))) ) {
          if (hasValidRange || hasValidMin)
            valid_min = scale * valid_min + offset;
          if (hasValidRange || hasValidMax)
            valid_max = scale * valid_max + offset;
          if (debug) System.out.println("scale the range");
        }
      }
    }

    useNaNs = useNaNs && ((elementType == double.class) || (elementType == float.class));
    if (debug) System.out.println("useNaNs = "+useNaNs);
  }

  private int rank (Class c) {
    if (c == byte.class)
      return 0;
    else if (c == short.class)
      return 1;
    else if (c == int.class)
      return 2;
    else if (c == float.class)
      return 3;
    else if (c == double.class)
      return 4;
    else
      return -1;
  }

  /** true if Variable has valid_range, valid_min or valid_max attributes */
  public boolean hasInvalidData() { return hasValidRange || hasValidMin || hasValidMax; }
  /** return the minimum value in the valid range */
  public double getValidMin() { return valid_min; }
  /** return the maximum value in the valid range */
  public double getValidMax() { return valid_max; }
  /** return true if val is outside the valid range */
  public boolean isInvalidData( double val ) {
    if (hasValidRange)
      return ((val < valid_min) || (val > valid_max));
    else if (hasValidMin)
      return (val < valid_min);
    else if (hasValidMax)
      return (val > valid_max);
    return false;
  }

  /** true if Variable has _FillValue attribute */
  public boolean hasFillValue() { return hasFillValue; }
  /** return true if val equals the _FillValue  */
  public boolean isFillValue( double val ) { return hasFillValue && (val == fillValue); }

  /** true if Variable data will be converted using scale and offet */
  public boolean hasScaleOffset() { return hasScaleOffset; }
  /** true if Variable has missing_value attribute */
  public boolean hasMissingValue() { return hasMissingValue; }
  /** return true if val equals a missing_value  */
  public boolean isMissingValue( double val ) {
    if (!hasMissingValue)
      return false;
    for (int i=0; i<missingValue.length; i++)
      if (val == missingValue[i])
        return true;
    return false;
  }

  /** set if _FillValue is considered isMissing(); better set in constructor if possible */
  public void setFillValueIsMissing( boolean b) { this.fillValueIsMissing = b; }
  /** set if valid_range is considered isMissing(); better set in constructor if possible */
  public void setInvalidDataIsMissing( boolean b) { this.invalidDataIsMissing = b; }
  /** set if missing_data is considered isMissing(); better set in constructor if possible */
  public void setMissingDataIsMissing( boolean b) { this.missingDataIsMissing = b; }
  /** true if Variable has missing data values */
  public boolean hasMissing() {
    return (invalidDataIsMissing && hasInvalidData()) ||
           (fillValueIsMissing && hasFillValue()) ||
           (missingDataIsMissing && hasMissingValue());
  }
  /** true if val is a missing data value */
  public boolean isMissing( double val ) {
    if ( Double.isNaN(val)) return true;
    if (!hasMissing()) return false;
    return (invalidDataIsMissing && isInvalidData(val)) ||
      (fillValueIsMissing && isFillValue( val)) ||
      (missingDataIsMissing && isMissingValue( val));
  }

  /**
   * Read data from the netcdf file and return a memory resident Array, converting to
   *  internal (unpacked) units if required.
   * This Array has the same element type as the IOArray, and the requested shape.
   * <p>
   * <code>assert(origin[ii] + shape[ii] <= Variable.shape[ii]); </code>
   * <p>
   * @param origin int array specifying the starting index.
   * @param shape  int array specifying the extents in each
   *	dimension. This becomes the shape of the returned Array.
   * @return the requested data in a memory-resident Array
   */
  public Array read(int [] origin, int [] shape) throws IOException, InvalidRangeException  {
    if (debugRead) System.out.println("read (shape) ");
    ArrayAbstract result = (ArrayAbstract) orgVar.read(origin, shape);
    if (useNaNs)
      result = convertToNaNs( result);
    else if (hasScaleOffset)
      result = convertScaleOffset( result);
    return result;
  }

  /**
   * Read all the data from the netcdf file for this Variable and return a memory resident Array,
   * converting to internal (unpacked) units if required.
   * This Array has the same element type and shape as the Variable.
   * <p>
   * @return the requested data in a memory-resident Array.
   */
  public Array read() throws IOException {
    if (debugRead) System.out.println("read ");

    ArrayAbstract result = (ArrayAbstract) orgVar.read();
    if (useNaNs)
      result = convertToNaNs( result);
    else if (hasScaleOffset)
      result = convertScaleOffset( result);
    return result;
  }

  private ArrayAbstract convertScaleOffset(ArrayAbstract in) {
    ArrayAbstract out = ArrayAbstract.factory( getElementType(), in.getShape());

    IndexIterator iterIn = in.getIndexIteratorFast();
    IndexIterator iterOut = out.getIndexIteratorFast();

    if (debugRead) System.out.println("convertScaleOffset ");

    while (iterIn.hasNext()) {
      double val = iterIn.getDoubleNext();
      iterOut.setDoubleNext(scale * val + offset);
    }
    return out;
  }

  private ArrayAbstract convertToNaNs(ArrayAbstract in) {
    ArrayAbstract out = ArrayAbstract.factory( getElementType(), in.getShape());

    IndexIterator iterIn = in.getIndexIteratorFast();
    IndexIterator iterOut = out.getIndexIteratorFast();

    if (debugRead) System.out.println("convertToNaNs ");

    while (iterIn.hasNext()) {
      double val = scale * iterIn.getDoubleNext() + offset;
      iterOut.setDoubleNext(isMissing(val) ? Double.NaN : val);
    }
    return out;
  }

   /*
      // put boolean test on outside of iteration for speed
    if (hasMissingRange) {
      while (iterIn.hasNext()) {
        double val = iterIn.getDoubleNext();
        double sval = scale * val + offset;
        if ((sval < valid_min) || (sval > valid_max))
          iterOut.setDoubleNext(Double.NaN);
        else
          iterOut.setDoubleNext(sval);
      }
    } else if (hasMissingMin) {
      while (iterIn.hasNext()) {
        double sval = scale * iterIn.getDoubleNext() + offset;
        if (sval < valid_min)
          iterOut.setDoubleNext(Double.NaN);
        else
          iterOut.setDoubleNext(sval);
      }
    } else if (hasMissingMax) {
      while (iterIn.hasNext()) {
        double sval = scale * iterIn.getDoubleNext() + offset;
        if (sval > valid_max)
          iterOut.setDoubleNext(Double.NaN);
        else
          iterOut.setDoubleNext(sval);
      }
    } else if (hasMissingArray) {
      int n = missing_data.length;
elem: while (iterIn.hasNext()) {
        double val = iterIn.getDoubleNext();
        for (int i=0; i<n; i++) {
          if (val == missing_data[i]) {
            iterOut.setDoubleNext(Double.NaN);
            continue elem;
          }
        }  // for
        iterOut.setDoubleNext(scale * val + offset);
      } // while
    }
    converted = true;
    return out;
  } */



  public MultiArray sliceMA(int which_dim, int index_value) {
    return new MultiArrayAdapter(this, which_dim, index_value);
  }

  ///////////////////////////////////////////////////
  // delegated methods

  /** debugging: nicely formatted string representation of underlying ucar.netcdf.Variable. */
  public String toStringN() { return orgVar.toStringN(); }

}

/* Change History:
   $Log: VariableStandardized.java,v $
   Revision 1.1  2007/03/26 13:07:24  dmedv
   no message

   Revision 1.5  2001/05/16 16:26:23  caron
   typo

   Revision 1.4  2001/05/08 16:30:14  caron
   debug off

   Revision 1.3  2001/05/01 15:10:35  caron
   clean up VariableStandardized.java; add HTTP access

   Revision 1.2  2001/03/09 19:30:54  caron
   minor

   Revision 1.1  2001/02/21 21:21:26  caron
   add VariableStandardized

  */

