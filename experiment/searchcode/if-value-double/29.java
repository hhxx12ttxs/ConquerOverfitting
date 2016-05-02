/*
 * This file is auto-generated.
 * Do not edit.
 *
 */

package matula.lang;

import matula.runtime.Runtime;
import matula.runtime.NoMatchException;

public final class Double extends Node
{
  public static final int CLASS_ID = Runtime.requestClassId("matula.lang.Double");
  public static final int N    = 0;
  public static final int TO   = 1;
  public static final int FROM = 2;
  public static final int BY   = 3;
  public static final int WITH = 4;

  

  public final double value;

  public Double(double value)
  {
    super(CLASS_ID);
    this.value = value;
  }

  

  public static Double valueOf(double value)
  {
    
      return new Double(value);
    
  }

  public java.lang.String toString()
  {
    return java.lang.Double.toString(this.value);
  }

  public String asString()
  {
    return new String(this.toString());
  }

  private static void matchAndReference(Node a)
  {
    if(! ((a.id == Double.CLASS_ID)))
      throw new NoMatchException();
    a.reference();
  }

  private static void matchAndReference(Node a, Node b)
  {
    if(! (a.id == Double.CLASS_ID &&
          b.id == Double.CLASS_ID))
    {
      throw new NoMatchException();
    }

    a.reference();
    b.reference();
  }

  public static Node add_n_to(Node a, Node b)
  {
    matchAndReference(a,b);
    return new Double(((Double)a).value + ((Double) b).value);
  }

  public static Node subtract_n_from(Node a, Node b)
  {
    matchAndReference(a, b);
    return new Double(((Double)a).value - ((Double) b).value);
  }

  public static Node multiply_n_by(Node a, Node b)
  {
    matchAndReference(a, b);
    return new Double(((Double)a).value * ((Double)b).value);
  }

  public static Node increment_n(Node a)
  {
    matchAndReference(a);
    return new Double(((Double)a).value + 1);
  }

  public static Node decrement(Node a)
  {
    matchAndReference(a);
    return new Double(((Double)a).value - 1);
  }

  public static Node divide_n_by(Node a, Node b)
  {
    matchAndReference(a, b);
    return new Double(((Double)a).value / ((Double)b).value);
  }

  public static Node compare_n_with( Node a, Node b)
  {
    matchAndReference(a, b);
    
    switch(java.lang.Double.compare(((Double)a).value, ((Double)b).value))
    {
      case -1:
        return Order.LESS;
      case 0:
        return Order.EQUAL;
      case 1:
        return Order.GREATER;
      default:
        throw new RuntimeException();
    }
  }

  public static Node equal_n_to(Node a, Node b)
  {
    matchAndReference(a, b);

    if(((Double)a).value == ((Double)b).value)
      return matula.lang.Boolean.YES;
    else
      return matula.lang.Boolean.NO;
  }

  public double value()
  {
    return this.value;
  }

  public java.lang.Double toJava()
  {
    return java.lang.Double.valueOf(this.value);
  }

}

