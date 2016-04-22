package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class IntType extends PrimitiveType{
	private static IntType instance = null;
	public static IntType getInstance(){
		if(instance == null)
			instance = new IntType();
		return instance;
	}
	
	private IntType(){
		this.classType = "CppIntType";
		this.type.setName("int");
	}
}

