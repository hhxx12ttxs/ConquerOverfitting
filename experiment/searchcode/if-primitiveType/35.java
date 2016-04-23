package semanticAnalyzer.types;

public enum PrimitiveType implements Type {
	BOOLEAN(1, "bool"),
	CHARACTER(1, "char"),
	INTEGER(4, "int"),
	FLOAT(8, "float"),
	STRING(referenceTypeSize, "string"),
	VOID(0, "void"),
	ERROR(0, ""),			// use as a value when a syntax error has occurred
	NO_TYPE(0, "");			// use as a value when no type has been assigned.
	
	private int sizeInBytes;
	private String typeString;
	
	private PrimitiveType(int size) {
		this.sizeInBytes = size;
		this.typeString = toString();
	}
	private PrimitiveType(int size, String typeString) {
		this.sizeInBytes = size;
		this.typeString= typeString;
	}
	public int getSize() {
		return sizeInBytes;
	}
	public String typeString() {
		return typeString;
	}
	public static boolean isPrimitiveType(String lexeme) {
		return (lexeme.equals(PrimitiveType.BOOLEAN.typeString) ||
				lexeme.equals(PrimitiveType.CHARACTER.typeString) ||
				lexeme.equals(PrimitiveType.FLOAT.typeString) ||
				lexeme.equals(PrimitiveType.INTEGER.typeString) ||
				lexeme.equals(PrimitiveType.STRING.typeString));
	}
	public static PrimitiveType returnPrimitiveType(String lexeme) {
		if (lexeme.equals(PrimitiveType.BOOLEAN.typeString)) 		return PrimitiveType.BOOLEAN;
		else if (lexeme.equals(PrimitiveType.CHARACTER.typeString)) return PrimitiveType.CHARACTER;
		else if (lexeme.equals(PrimitiveType.FLOAT.typeString)) 	return PrimitiveType.FLOAT;
		else if (lexeme.equals(PrimitiveType.INTEGER.typeString)) 	return PrimitiveType.INTEGER;
		else 	/*string type*/ 									return PrimitiveType.STRING;
	}
	
	public boolean isReferenceType() {
		if (this == STRING) {
			return true;
		}
		return false;
	}
	
	public boolean match(Type type) {
		return this == type;
	}
}

