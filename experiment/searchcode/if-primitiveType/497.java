package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class VoidType extends PrimitiveType {
	private static VoidType instance = null;
	public static VoidType getInstance(){
		if(instance == null)
			instance = new VoidType();
		return instance;
	}
	
	private VoidType(){
		this.classType = "CppVoidType";
		this.type.setName("void");
	}
}

