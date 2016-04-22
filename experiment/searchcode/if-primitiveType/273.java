package javatraits.symbols;

import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.type.Type;

public class BuiltInTypeSymbol extends BasicSymbol {
	public static final String bInt = "int";
	public static final String bLong = "long";
	public static final String bShort = "short";
	public static final String bDouble = "double";
	public static final String bByte = "byte";
	public static final String bChar = "char";
	public static final String bFloat = "float";
	public static final String bString = "String";
	public static final String bBoolean = "boolean";
	public static final String bVoid = "void";
	
	public BuiltInTypeSymbol(String name) {
		super(name, null, 0);
//		Type t = null;
//		boolean string = false;
//		switch (name){
//		case bInt:
//			t = new PrimitiveType(Primitive.Int);
//			break;
//		case bLong:
//			t = new PrimitiveType(Primitive.Long);
//			break;
//		case bShort:
//			t = new PrimitiveType(Primitive.Short);
//			break;
//		case bDouble:
//			t = new PrimitiveType(Primitive.Double);
//			break;
//		case bByte:
//			t = new PrimitiveType(Primitive.Byte);
//			break;
//		case bChar:
//			t = new PrimitiveType(Primitive.Char);
//			break;
//		case bFloat:
//			t = new PrimitiveType(Primitive.Float);
//			break;
//		case bBoolean:
//			t = new PrimitiveType(Primitive.Boolean);
//			break;
//		case bString:
//			string = true;
//			break;
//		}
//		if (string){
//			
//		} else {
//			this.type = t;
//		}
//		
	}
}

