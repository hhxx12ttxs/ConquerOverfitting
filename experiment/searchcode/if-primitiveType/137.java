package org.metamaya.program;

import org.metamaya.build.Location;

public abstract class Type extends Definition {
	
	public static final PrimitiveType UNDEFINED = new PrimitiveType("undefined",TypeKind.UNDEFINED,null);
	public static final PrimitiveType VOID = new PrimitiveType("void",TypeKind.VOID,org.objectweb.asm.Type.VOID_TYPE);
	public static final PrimitiveType NULL = new PrimitiveType("null",TypeKind.NULL,null);

	public static final PrimitiveType BYTE = new PrimitiveType("byte",TypeKind.BYTE,org.objectweb.asm.Type.BYTE_TYPE);
	public static final PrimitiveType BOOL = new PrimitiveType("bool",TypeKind.BOOL,org.objectweb.asm.Type.BOOLEAN_TYPE);
	public static final PrimitiveType CHAR = new PrimitiveType("char",TypeKind.CHAR,org.objectweb.asm.Type.CHAR_TYPE);
	public static final PrimitiveType SHORT = new PrimitiveType("short",TypeKind.SHORT,org.objectweb.asm.Type.SHORT_TYPE);
	public static final PrimitiveType INT = new PrimitiveType("int",TypeKind.INT,org.objectweb.asm.Type.INT_TYPE);
	public static final PrimitiveType LONG = new PrimitiveType("long",TypeKind.LONG,org.objectweb.asm.Type.LONG_TYPE);
	public static final PrimitiveType FLOAT = new PrimitiveType("float",TypeKind.FLOAT,org.objectweb.asm.Type.FLOAT_TYPE);
	public static final PrimitiveType DOUBLE = new PrimitiveType("double",TypeKind.DOUBLE,org.objectweb.asm.Type.DOUBLE_TYPE);

	public static final ClassType OBJECT = new ClassType(Object.class);
	public static final ClassType STRING = new ClassType(String.class);

	public org.objectweb.asm.Type asmType;
	public final TypeKind typeKind;
	
	public Type(Location location, Definition parent, Ident ident, int modifiers, TypeKind kind) {
		super(location,parent,ident,modifiers);
		typeKind = kind;
	}

	public TypeKind getTypeKind() {
		return typeKind;
	}
	
	public final boolean isNumeric() {
		return typeKind.ordinal() >= TypeKind.BYTE.ordinal() && typeKind.ordinal() <= TypeKind.DOUBLE.ordinal();
	}
	
	public final boolean isIntegral() {
		return typeKind.ordinal() >= TypeKind.BYTE.ordinal() && typeKind.ordinal() <= TypeKind.LONG.ordinal();
	}
	
	public final boolean isNarrowerThan(TypeKind kind) {
		return typeKind.ordinal() >= TypeKind.BYTE.ordinal() && typeKind.ordinal() < kind.ordinal();
	}

	public final boolean isWiderThan(TypeKind kind) {
		return typeKind.ordinal() > kind.ordinal() && typeKind.ordinal() <= TypeKind.DOUBLE.ordinal();
	}
	
	public final boolean isNarrowerThan(Type t) {
		return typeKind.ordinal() >= TypeKind.BYTE.ordinal() && typeKind.ordinal() < t.typeKind.ordinal();
	}

	public final boolean isWiderThan(Type t) {
		return typeKind.ordinal() > t.typeKind.ordinal() && typeKind.ordinal() <= TypeKind.DOUBLE.ordinal();
	}

	public boolean isAssignableTo(Type t) {
		//numeric types
		if (this.isNumeric() && t.isWiderThan(this))
			return true;
		
		return canBeCastTo(t);
	}

	private boolean canBeCastTo(Type t) {
		return false;
	}

	@Override
	public String toString() {
		if (ident == null)
			return typeKind.toString();
		else
			return ident.toString();
	}
	
	
}

