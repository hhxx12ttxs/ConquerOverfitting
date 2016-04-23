package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class UnsignedType extends PrimitiveType{
	private static UnsignedType instance = null;
	public static UnsignedType getInstance(){
		if(instance == null)
			instance = new UnsignedType();
		return instance;
	}
	
	private UnsignedType(){
		this.classType = "CppUnsignedType";
		this.type.setName("unsigned");
	}
}

