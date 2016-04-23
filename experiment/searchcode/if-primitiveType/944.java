
public class Type {

  private static final Type INT = new PrimitiveType("int");
  private static final Type CHAR = new PrimitiveType("char");
  private static final Type STRING = new PrimitiveType("string");
  private static final Type VOID = new PrimitiveType("void");

  protected Type() {}

  public static Type newPrimitiveType(String typeName) {
    Type res;
    if (typeName.equals("int")) res = INT;
    else if (typeName.equals("char")) res = CHAR;
    else if (typeName.equals("string")) res = STRING;
    else if (typeName.equals("void")) res = VOID;
    else res = new PrimitiveType(typeName);
    return res;
  }

  public static Type newArrayType(Type baseType, int dimension) {
    return new ArrayType(baseType, dimension);
  }
  String name = "";
  // ---------- PrimitiveType static inner class ----------
  static class PrimitiveType extends Type {
    public PrimitiveType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
  
  Type baseType = null;
  int dimension = -999;
  boolean isArray = false;
  int depth = 0;
  // ---------- ArrayType static inner class ----------
  static class ArrayType extends Type {
    
    public ArrayType(Type baseType, int dimension) {
      this.baseType = baseType;
      this.dimension = dimension;
      this.isArray = true;
      this.depth = 1;
      Type tempBase = baseType;
      while (tempBase.toString().length() > 5 && 
    		  tempBase.toString().subSequence(0, 5).equals("array")) {
    	  this.depth++;
    	  tempBase = tempBase.baseType;
      }
    }

    @Override
    public String toString() {
      return "array " + dimension + " of " + baseType.toString();
    }
  }
  
  public boolean equals(Type t) {
	  if ((t.isArray && this.isArray) || (!t.isArray && !t.isArray)) {
		  if (t.isArray && this.isArray) {
			 return (t.toString().equals(this.toString())); 
		  }
		  else {
			  return (t.name.equals(this.name));
		  }
	  }
	  return false;
  }
}

