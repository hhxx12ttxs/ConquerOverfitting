package mmclass.primitivetypes;

import mmclass.CppModel;
import mmclass.abs.PrimitiveType;

public class BooleanType extends PrimitiveType {
	private static BooleanType instance = null;
	public static BooleanType getInstance(){
		if(instance == null)
			instance = new BooleanType();
		return instance;
	}
	
	private BooleanType(){
		this.classType = "CppBooleanType";
		this.type.setName("bool");
	}
}

