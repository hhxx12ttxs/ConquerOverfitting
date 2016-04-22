package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class LongType extends PrimitiveType{
	private static LongType instance = null;
	public static LongType getInstance(){
		if(instance == null)
			instance = new LongType();
		return instance;
	}
	
	private LongType(){
		this.classType = "CppLongType";
		this.type.setName("long");
	}
}

