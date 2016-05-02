//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.numeric;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import java.util.logging.Logger;

public class NumericUtils {

  static Logger log = JPF.getLogger("numeric");

  // this is NOT 'inexact' in the IEEE 754 meaning - it only means that
  // we suspect a value that has more than an initial roundoff error
  public static final Object INEXACT = new Object();

  // do we want to check cancellation at all?
  static boolean checkCancellation;
  
  // minimum number of non-cancelled bits
  static int minFloatBits, minDoubleBits;
  
  // do we want to throw an ArithmeticException in case of cancellation?
  static boolean throwOnCancellation;

  static double eps;

  static NumericAttrFactory attrFactory;
  static NumericAttrProperty attrProperty;

  public static void init (Config conf){
    checkCancellation = conf.getBoolean("numeric.check_cancellation", false);
    minDoubleBits = conf.getInt("numeric.min_double_bits", 10);
    minFloatBits = conf.getInt("numeric.min_float_bits", 4);
    throwOnCancellation = conf.getBoolean("numeric.throw_on_cancellation", false);
    eps = conf.getDouble("numeric.eps", Double.MIN_VALUE*2);

    attrFactory = conf.getInstance("numeric.attr_factory.class", NumericAttrFactory.class);
  }
  
  static public boolean checkNaN (double r) {
    return !(Double.isNaN(r) || Double.isInfinite(r));
  }
  
  static public boolean checkNaN (float r) {
    return !(Float.isNaN(r) || Float.isInfinite(r));
  }
  
  static public boolean checkNaNcompare (float r1, float r2) {
    return !(Float.isNaN(r1) || Float.isNaN(r2) ||
        (Float.isInfinite(r1) && Float.isInfinite(r2) && (r1 == r2)));
  }
  
  static public boolean checkNaNcompare (double r1, double r2) {
    return !(Double.isNaN(r1) || Double.isNaN(r2) ||
        (Double.isInfinite(r1) && Double.isInfinite(r2) && (r1 == r2)));
  }


  static public boolean throwOnCancellation() {
    return throwOnCancellation;
  }

  //--- utilities for checking cancellation
  
  static boolean bitSet (long l, int idx) {
    return ((l & (1L<<idx)) != 0);
  }


  static public boolean checkCancellation (double d1, double d2, boolean isSubtraction){
    if (!checkCancellation){
      return true;
    }

    if (d1 == d2){
      // that might be a false negative, but in any way the subtraction itself
      // does not cause additional errors - the problem has happened before
      return true;
    }
    
    long l1 = Double.doubleToLongBits(d1);
    long e1 = ((l1 & 0x7ff0000000000000L) >> 52) - 1023;
    //long m1 = (l1 & 0x000fffffffffffffL);

    long l2 = Double.doubleToLongBits(d2);
    long e2 = ((l2 & 0x7ff0000000000000L) >> 52) - 1023;
    //long m2 = (l2 & 0x000fffffffffffffL);

    //--- handle the obvious cases first

    // subtraction => different signs are safe
    if (((d1 >= 0) != (d2 >= 0)) == isSubtraction ){
      return true;
    }
    if (e1 != e2) {
      return true; // different exponents are safe
    }

    //--- check how many bits would not cancel out
    int i;
    for (i=51; i>=0; i--){
      if (bitSet(l1,i) != bitSet(l2,i)) break;
    }
    return (i >= minDoubleBits-1);
  }

  static public int mantissa (double d){
    return (int) (((Double.doubleToLongBits(d) & 0x7ff0000000000000L) >> 52) - 1023);
  }

  static public boolean absDifferenceWithinEps (double d1, double d2){
    return Math.abs(d1 - d2) < eps;
  }
  
  static boolean bitSet (int i, int idx) {
    return ((i & (1<<idx)) != 0);
  }

  static public boolean isAssumedInexact (long doubleBits){
    return isAssumedInexact(Double.longBitsToDouble(doubleBits));
  }

  static public boolean isAssumedInexact (double d){
    if (d == 0.0){
      return false;
    }

    // check if we have more than 5 digits
    double ds = d*100000;
    if (Math.abs(ds - Math.round(ds)) < 1e-100){
      return false;
    }

    // check boundaries
    if (d == Double.MIN_VALUE || d == Double.MAX_VALUE){
      return false;
    }

    // check known constants
    if (d == Math.E || d == Math.PI ){
      return false;
    }

    return true;
  }


  static public boolean checkCancellation (float f1, float f2, boolean isSubtraction){
    if (!checkCancellation){
      return true;
    }
    
    int i1 = Float.floatToIntBits(f1);
    int e1 = ((i1 & 0x7f800000) >> 22) - 127;
    //int m1 = (i1 & 0x007fffff);

    int i2 = Float.floatToIntBits(f2);
    int e2 = ((i2 & 0x7f800000) >> 22) - 127;
    //int m2 = (i2 & 0x007fffff);

    // subtraction => different signs are safe
    if (((f1 >= 0) != (f2 >= 0)) == isSubtraction ){
      return true;
    }
    if (e1 != e2){
      return true; // different exponents are safe
    }
    if (i1 == i2){
      return false;
    }


    //--- check how many bits would not cancel out
    int i;
    for (i=21; i>=0; i--){
      if (bitSet(i1,i) != bitSet(i2,i)) break;
    }
    return (i >= minFloatBits-1);
  }


  static public boolean isAssumedInexact (int floatBits){
    return isAssumedInexact(Float.intBitsToFloat(floatBits));
  }

  static public boolean isAssumedInexact (float f){
    if (f == 0.0){
      return false;
    }

    // check if we have more than 3 digits
    float fs = f*1000;
    if (Math.abs(fs - Math.round(fs)) < 1e-20){
      return false;
    }

    // check boundaries
    if (f == Float.MIN_VALUE || f == Float.MAX_VALUE){
      return false;
    }

    // checking constants wouldn't make sense - these are doubles

    return true;
  }


  //--- reporting

  static public void warning (ThreadInfo ti, Instruction insn, String msg){
    log.warning(msg + " at " + insn.getSourceLocation());
  }

  static public Instruction throwException (ThreadInfo ti, String msg){
    return ti.createAndThrowException("java.lang.ArithmeticException", msg);
  }


  //--- the numeric attribute interface

  static public void setNumericAttrProperty(NumericAttrProperty prop){
    attrProperty = prop;
  }

  static public NumericAttr getDoubleOperandAttr (ThreadInfo ti){
    StackFrame frame = ti.getTopFrame();
    NumericAttr a = frame.getLongOperandAttr(NumericAttr.class);
    if (a != null){
      return a;
    } else {
      if (attrFactory != null){
        double d = frame.peekDouble();
        return attrFactory.createAttr(d);
      } else {
        return null;
      }
    }
  }

  static public void setDoubleOperandAttr (ThreadInfo ti, NumericAttr a){
    if (a != null){
      StackFrame frame = ti.getModifiableTopFrame();
      frame.setLongOperandAttr(a);
    }
  }


  static public NumericAttr createAttr(double d){
    if (attrFactory != null){
      return attrFactory.createAttr(d);
    } else {
      return null;
    }
  }

  static public NumericAttr addAttrs (NumericAttr a, NumericAttr b){
    if (attrFactory != null){
      return a.add(b);
    } else {
      return null;
    }
  }

  static public NumericAttr subtractAttrs (NumericAttr a, NumericAttr b){
    if (attrFactory != null){
      return a.subtract(b);
    } else {
      return null;
    }
  }

  static public NumericAttr multiplyAttrs (NumericAttr a, NumericAttr b){
    if (attrFactory != null){
      return a.multiply(b);
    } else {
      return null;
    }
  }

  static public NumericAttr divideAttrs (NumericAttr a, NumericAttr b){
    if (attrFactory != null){
      return a.divide(b);
    } else {
      return null;
    }
  }


  static public String checkAttr (double d, NumericAttr a){
    if (a != null && attrProperty != null){
      return attrProperty.check(d, a);
    }

    return null;
  }
}

