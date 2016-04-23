package common;

import java.util.List;

import asema.entities.PrimitiveType;
import asema.entities.Type;
import asema.entities.VoidType;

public class CommonHelper {

	public static String Empty = "";
	
	public static boolean isNullOrEmpty(String s){
		return s == null || s.trim().length() == 0;
	}
	
	public static boolean isPredefinedClass(String name){
		return name.equals("Object") || name.equals("System");	
	}
	

	public static boolean isPrimitiveType(String name){
		return name.equals("int") ||
			   name.equals("char") ||
			   name.equals("boolean") ||
			   name.equals("string") ||
			   name.equals("void");
	}

	public static boolean isPrimitiveType(Type t){
		return t == PrimitiveType.Int ||  	
			   t == PrimitiveType.Boolean ||  	
			   t == PrimitiveType.Char ||  	
			   t == PrimitiveType.String ||  	
			   t == VoidType.VoidType;  	
	}
	
	public static boolean validFormalArgs(List<asema.entities.EntryVar> expectedVars, List<asema.entities.EntryVar> actualVars){
		boolean isValid = true;
		if(expectedVars.size() != actualVars.size())
			isValid = false;
		
		for (int i = 0; i < expectedVars.size(); i++) {
			if(!expectedVars.get(i).Type.Name.equals(actualVars.get(i).Type.Name)){
				isValid = false;
				break;
			}
		}
		return isValid;
	}
}

