package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class ShortType extends PrimitiveType {
	private static ShortType instance = null;
	public static ShortType getInstance(){
		if(instance == null)
			instance = new ShortType();
		return instance;
	}
	
	private ShortType(){
		this.classType = "CppShortType";
		this.type.setName("short");
	}
}

