package com.stubhub.codegen.template;

import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;

public class PropEntity {
	public String name;
	public String className;
	public String type;
	
	public PropEntity(String name, String className, String type){
		this.name = name;
		this.className = className;
		this.type = type;
	}
	
	static enum PropType{
		NUMBER,
		GENERIC_CLASS,
		SIMPLE_CLASS,
		BOOL,
	}
	
	public PropType getType(){
		String ltype = type.toLowerCase();
		if("list".equals(ltype))
			return PropType.GENERIC_CLASS;
		else if("bool".equals(ltype) || "boolean".equals(ltype))
			return PropType.BOOL;
		else if(isNumber(type))
			return PropType.NUMBER;
		else 
			return PropType.SIMPLE_CLASS;
	}
	
	private boolean isNumber(String type){
		String ltype = type.toLowerCase();
		if("int".equals(ltype) || "short".equals(ltype) || "long".equals(ltype)
				|| "float".equals(ltype) || "double".equals(ltype))
			return true;
		else
			return false;
	}
	
	public Code returnNumberType(String type){
		String ltype = type.toLowerCase();
		
		if("int".equals(ltype))
			return PrimitiveType.INT;
		else if("short".equals(ltype))
			return PrimitiveType.SHORT;
		else if("long".equals(ltype))
			return PrimitiveType.LONG;
		else if("float".equals(ltype))
			return PrimitiveType.FLOAT;
		else if("double".equals(ltype))
			return PrimitiveType.DOUBLE;
		else
			return PrimitiveType.VOID;
	}
}

