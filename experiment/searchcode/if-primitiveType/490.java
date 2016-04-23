package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class DoubleType extends PrimitiveType{
	private static DoubleType instance = null;
	public static DoubleType getInstance(){
		if(instance == null)
			instance = new DoubleType();
		return instance;
	}
	
	private DoubleType(){
		this.classType = "CppDoubleType";
		this.type.setName("double");
	}
}

