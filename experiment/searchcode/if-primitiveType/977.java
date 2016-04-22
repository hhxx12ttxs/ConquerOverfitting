package core.evaluator.datatype;

import java.math.BigDecimal;
import java.util.List;

/**
 * Enumeration for primitive data type
 * 
 * @author Michael Fong
 *
 */
public enum PrimitiveType {
	
	/**
	 * boolean : true/false
	 */
	BOOLEAN(new Class[] {Boolean.class}), 
	
	/**
	 * numeric data
	 */
	NUMERIC(new Class[] { BigDecimal.class /*
											 * , Integer.class, Short.class,
											 * Long.class, Float.class,
											 * Double.class
											 */}),
	
	/**
	 * literal data
	 */
	LITERAL(new Class[] {String.class/*, Character.class*/})
	;
	
	/**
	 * associated java class
	 */
	private Class<?> supportClazz[];
	
	/**
	 * Constructor of {@code PrimitiveType}
	 * 
	 * @param clazz associated java class
	 */
	private PrimitiveType(Class<?> clazz[]) {
		this.supportClazz = clazz;
	}

	/**
	 * Determine if the given class match with the specified
	 * {@code PrimitiveType}
	 * 
	 * @param typeClass given java class
	 * 
	 * @return {@code true}, if the given class match with the specified
	 *         primitive type; {@code false}, otherwise.
	 */
	public boolean matchType(Class<?> typeClass) {
		for(Class<?> clazz: this.supportClazz) {
			if(clazz.equals(typeClass)) {
				return true;
			}
		}		
		return false;
	}
}
