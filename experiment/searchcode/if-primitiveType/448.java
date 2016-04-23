/*
 Copyright(c) 1996-1998
 IPA, ETL, AT21, FSIABC, FXIS, InArc, MRI, NUL, SBC, Sharp, TEC, TIS

 All rights reserved.  No guarantee.
 This technology is a result of the Advanced Software  Enrichment 
 Project of Information-technology Promotion Agency, Japan (IPA).

 Permissions  to  use,  copy, modify and distribute this software
 are governed by the terms and conditions set forth in  the  file 
 COPYRIGHT, located on the top directory of this software.
 */

/*
 * $Id: PrimitiveType.java,v 0.11 1998-03-19 14:22:29+09 otokawa Exp $
 */

/*
 * created by Hideyuki OTOKAWA, Sharp corp.
 * 1996.11.29
 */

package JP.go.ipa.oz.compiler;

/**
  * A class representing primitive types
  *
  * @see Type
  */

class PrimitiveType extends Type {
  /* values of primitive type */
  static final int T_BYTE = 0;
  static final int T_SHORT = 1;
  static final int T_INT = 2;
  static final int T_LONG = 3;
  static final int T_FLOAT = 4;
  static final int T_DOUBLE = 5;
  static final int T_BOOL = 6;
  static final int T_CHAR = 7;
  static final int T_CONDITION = 8;
  static final int T_VOID = 9;

  /* object representation of primitive type */
  static final PrimitiveType BYTE = new PrimitiveType (T_BYTE, "byte", 
						       "B", "java.lang.Byte");
  static final PrimitiveType SHORT = new PrimitiveType (T_SHORT, "short", 
							"S", "java.lang.Short");
  static final PrimitiveType INT = new PrimitiveType (T_INT, "int", 
						      "I", "java.lang.Integer");
  static final PrimitiveType LONG = new PrimitiveType (T_LONG, "long", 
						       "J", "java.lang.Long");
  static final PrimitiveType CHAR = new PrimitiveType (T_CHAR, "char", 
						       "C", "java.lang.Character");
  static final PrimitiveType FLOAT = new PrimitiveType (T_FLOAT, "float", 
							"F", "java.lang.Float");
  static final PrimitiveType DOUBLE = new PrimitiveType (T_DOUBLE, "double", 
							 "D", "java.lang.Double");
  static final PrimitiveType BOOL = new PrimitiveType (T_BOOL, "bool", 
						       "Z", "java.lang.Boolean");
  static final PrimitiveType CONDITION = new PrimitiveType (T_CONDITION, 
							    "condition", "N",
							    OzcProperty.CONDITION);
  static final PrimitiveType VOID = new PrimitiveType (T_VOID, "void", "V",
						       "java.lang.Void");

  /**
    * kind of primitive types (from sym class) 
    */
  private int kind;

  /**
    * java class name
    */
  private String java_name;

  /**
    * create new one
    *
    * @param k kind of primitive type
    * @param n type name
    * @param en encoded type name
    * @param jn java type name
    */
  PrimitiveType (int k, String n, String en, String jn) {
    kind = k;
    name = n;
    encoded_name = en;
    java_name = jn;
    java_class = "Class.forName (\"" + jn + "\")";
  }
  
  /**
    * override
    *
    * @see Type
    */
  boolean isSame (Type type) {
    return type == ANY ? true : this == type;
  }

  /**
    * override
    *
    * @see Type
    */
  int compare (Type t) throws TypeMismatch {
    PrimitiveType pt;
    int k;

    if (t == ANY || t == null) return 0;

    try {
      pt = (PrimitiveType) t;
      k = pt.kind;
    } catch (ClassCastException ex) {
      throw new TypeMismatch ();
    }

    if (kind == k) return 0;

    if (this == BYTE || this == SHORT || this == INT || this == LONG ||
	this == FLOAT || this == DOUBLE) {
      if (pt == CHAR || pt == BOOL || pt == CONDITION)
	throw new TypeMismatch ();
      return kind - k;
    }

    throw new TypeMismatch ();
  }

  /**
    * override
    *
    * @see Type
    */
  boolean isBool () { return isSame (PrimitiveType.BOOL); }

  /**
    * override
    *
    * @see Type
    */
  boolean isChar () { return isSame (PrimitiveType.CHAR); }

  /**
    * override
    *
    * @see Type
    */
  boolean isVoid () { return isSame (PrimitiveType.VOID); }

  /**
    * override
    *
    * @see Type
    */
  boolean isCondition () { return isSame (PrimitiveType.CONDITION); }

  /**
    * override
    *
    * @see Type
    */
  boolean isPrimitive () { return true; }

  /**
    * check if type is integral type (byte, short, int, long, char) 
    *
    * @param t type
    * @return if so true, otherwise false
    */
  static boolean isIntegral (Type t) {
    PrimitiveType pt;
    int k;

    if (t == ANY || t == null) return true;

    try {
      pt = (PrimitiveType) t;
      if (pt.kind < FLOAT.kind)
	return true;
    } catch (ClassCastException ex) {
      ozc.catchException (ex);
    }

    return false;
  }

  /**
    * check if type is arithmetic type (byte, short, int, long, float double)
    *
    * @param t type
    * @return if so true, otherwise false
    */
  static boolean isArithmetic (Type t) {
    PrimitiveType pt;
    int k;

    if (t == ANY || t == null) return true;

    try {
      pt = (PrimitiveType) t;
      if (pt.kind < BOOL.kind)
	return true;
    } catch (ClassCastException ex) {
      return false;
    }

    return false;
  }

  /**
    * override 
    */
  public String toString () { 
    if (Emitter.isEmittingForBrowser ()) 
      return encoded_name;

    if (!Emitter.isEmitting ()) 
      return name;

    if (this == BOOL) return "boolean";
    if (this == CONDITION) return OzcProperty.CONDITION;
    
    return name;
  }

  /**
    * get represention of java class name
    */
  String toStringAsJavaClassName () {
    return java_name;
  }
}

/* EOF */

