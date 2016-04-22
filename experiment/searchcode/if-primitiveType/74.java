package de.htwg.SimpleDSLBuilder.Creator;

import java.util.HashSet;
import java.util.Set;

public enum PrimitiveType {
	BOOLEAN,BYTE,SHORT,INT,LONG,FLOAT,DOUBLE,CHAR;
	
	private static final Set<String> values = new HashSet<>();

	/**
	 * Returns the lower-case String representation of the primitive types in Java
	 * @return String values in set
	 */
	public static Set<String> getValues() {
		if(!values.isEmpty())
			return values;
		for (PrimitiveType pt : PrimitiveType.values()) {
			values.add(pt.toString().toLowerCase());
		}
		return values;
	}
	/**
	 * Returns lower-case String representation of a specific PrimitiveType
	 * @param pt the PrimitiveType
	 * @return
	 */
	public static String getStringOf(PrimitiveType pt){
		return pt.toString().toLowerCase();
	}
}

