package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class CharType extends PrimitiveType {
	private static CharType instance = null;
	public static CharType getInstance(){
		if(instance == null)
			instance = new CharType();
		return instance;
	}
	
	private CharType(){
		this.classType = "CppCharType";
		this.type.setName("char");
	}
}

