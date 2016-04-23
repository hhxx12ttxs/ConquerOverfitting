package mmclass.primitivetypes;

import mmclass.abs.PrimitiveType;

public class SignedType extends PrimitiveType{
	private static SignedType instance = null;
	public static SignedType getInstance(){
		if(instance == null)
			instance = new SignedType();
		return instance;
	}
	
	private SignedType(){
		this.classType = "CppSignedType";
		this.type.setName("signed");
	}
}

