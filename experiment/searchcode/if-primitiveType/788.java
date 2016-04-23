package org.xpaframework.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * Class initializing values for primitive types and maps this type to
 * its object representation.
 * 
 * @author Jan Janickovic
 */
public final class PrimitiveTypeInitializer {

	private Map<String, PrimitiveType> typeMap = new HashMap<String, PrimitiveType>();

	/**
	 * This class can not be initialized via constructor to keep
	 * singleton design pattern.
	 * 
	 * @see #getInitializer()
	 */
	private PrimitiveTypeInitializer() {
		init();
	}

	/**
	 * Static singleton instance of <code>this</code> class.
	 */
	private static PrimitiveTypeInitializer instance;
	
	/**
	 * @return instance of <code>this</code> class.
	 */
	protected static PrimitiveTypeInitializer getInitializer() {
		if(instance == null) {
			instance = new PrimitiveTypeInitializer();
		}
		
		return instance;
	}
	
	private void init() {
		init("bool", Boolean.class, false);
		init("byte", Byte.class, 0);
		init("char", Character.class, '\u0000');
		init("double", Double.class, 0.0d);
		init("float", Float.class, 0f);
		init("long", Long.class, 0L);
		init("int", Integer.class, 0);
		init("short", Short.class, 0);
	}
	
	private void init(String type, Class<?> classType, Object defaultValue) {
		PrimitiveType primitiveType = new PrimitiveType(classType, defaultValue);
		this.typeMap.put(type, primitiveType);
		this.typeMap.put(classType.getCanonicalName(), primitiveType);
	}

	/**
	 * Returns implicitly initialized values represented by the {@link Object} placeholder
	 * of the primitive type.
	 * 
	 * @param type - string representing one of primitive types.
	 * @return object representing the primitive type initialized by its default value. 
	 * 
	 * @throws IllegalArgumentException if <code>type</code> is <code>null</code> does not
	 * represent any primitive type.
	 */
	protected Object getObject(String type) throws NullPointerException, IllegalArgumentException {
		if(type == null) {
			throw new NullPointerException("Type not specified! Value: null");
		}
		
		PrimitiveType primitiveType = this.typeMap.get(type);
		
		if(primitiveType == null) {
			throw new IllegalArgumentException("Type '" + type + "' is not valid argument!");
		}
		
		return primitiveType.getDefaultValue();
	}

	/**
	 * Converts this primitive type to its class representation. The conversion
	 * is provided by canonical name of this class.
	 * 
	 * @param type - class representing primitive type.
	 * @return class representing this <code>type</code> or the original <code>type</code>
	 * if the regular class is passed into the method.
	 */
	protected Class<?> convertType(Class<?> type) {
		PrimitiveType primitiveType = this.typeMap.get(type.getCanonicalName());
		return primitiveType == null ? type : primitiveType.getClassType();
	}
	
	/**
	 * <p>Holder of primitive type defining class representing this type.
	 * This object holds classes equal to primitive data type and its
	 * primitive type default value.</p>
	 * 
	 * @author Jan Janickovic
	 */
	private class PrimitiveType {
		
		/**
		 * Class representing primitive type.
		 */
		private Class<?> classType;
		
		/**
		 * Default value of the primitive type represented by this {@link #classType}
		 */
		private Object defaultValue;
		
		/**
		 * @return type of the class.
		 */
		public Class<?> getClassType() {
			return classType;
		}

		/**
		 * @return default value for the primitive type that this {@link #classType}
		 * represents.
		 */
		public Object getDefaultValue() {
			return defaultValue;
		}

		/**
		 * Constructor passing information about the <code>classType</code>
		 * representing primitive type and its default value.
		 * 
		 * @param classType - type of the class representing primitive type.
		 * @param defaultValue - the default value of the primitive type.
		 */
		public PrimitiveType(Class<?> classType, Object defaultValue) {
			this.classType = classType;
			this.defaultValue = defaultValue;
		}
	}
}

