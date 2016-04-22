package com.didactilab.jsjvm.client.classfile;

import com.didactilab.jsjvm.client.classfile.descriptor.DescType;

public enum PrimitiveType implements Type, DescType {
	BYTE		('B', "byte"),
	SHORT		('S', "short"),
	INT			('I', "int"),
	LONG		('J', "long"),
	FLOAT		('F', "float"),
	DOUBLE		('D', "double"),
	BOOLEAN		('Z', "boolean"),
	CHAR		('C', "char");
	
	public final char sign;
	public final String name;

	private PrimitiveType(char sign, String name) {
		this.sign = sign;
		this.name = name;
	}
	
	@Override
	public String getJavaName() {
		return name;
	}
	
	@Override
	public String getDescriptor() {
		return String.valueOf(sign);
	}
	
	public static PrimitiveType valueOf(char sign) {
		for (PrimitiveType p : values()) {
			if (p.sign == sign) {
				return p;
			}
		}
		return null;
	}

}
