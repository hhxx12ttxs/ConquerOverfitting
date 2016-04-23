package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class FloatType extends PrimitiveType{
	private static FloatType instance = null;
	public static FloatType getInstance(){
		if(instance == null)
			instance = new FloatType();
		return instance;
	}
	
	private FloatType(){
		this.classType = "CppFloatType";
		this.type.setName("float");
	}
}

