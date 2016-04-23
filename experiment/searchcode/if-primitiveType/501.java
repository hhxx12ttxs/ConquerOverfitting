package com.willydupreez.prototype.config.util;

import java.util.HashMap;

public final class Types {

	private static final HashMap<Class<?>, Class<?>> AUTOBOX_TYPES = new HashMap<>(9);
	private static final HashMap<Class<?>, Class<?>> UNBOX_TYPES = new HashMap<>(9);

	static {
	    loadType(boolean.class, Boolean.class);
	    loadType(byte.class, Byte.class);
	    loadType(char.class, Character.class);
	    loadType(double.class, Double.class);
	    loadType(float.class, Float.class);
	    loadType(int.class, Integer.class);
	    loadType(long.class, Long.class);
	    loadType(short.class, Short.class);
	    loadType(void.class, Void.class);
	}

	private static void loadType(Class<?> primitiveType, Class<?> wrapperType) {
		AUTOBOX_TYPES.put(primitiveType, wrapperType);
		UNBOX_TYPES.put(wrapperType, primitiveType);
	}

	/**
	 * Convert primitive to wrapper type. If the provided type is not a
	 * primitive type, then the type is returned:
	 *
	 * int.class -> Integer.class
	 * String.class -> String.class
	 *
	 * @param primitiveType the primitive type
	 * @return the wrapper type
	 */
	public static Class<?> autoboxType(Class<?> primitiveType) {
		if (!primitiveType.isPrimitive()) {
			return primitiveType;
		}
		return AUTOBOX_TYPES.get(primitiveType);
	}

	/**
	 * Converts wrapper to primitive type. If the provided type is not a
	 * primitive type, then the type is returned:
	 *
	 * Integer.class -> int.class
	 * String.class -> String.class
	 *
	 * @param wrapperType the wrapper type
	 * @return the wrapper type
	 */
	public static Class<?> unboxType(Class<?> wrapperType) {
		if (!wrapperType.isPrimitive()) {
			return wrapperType;
		}
		return UNBOX_TYPES.get(wrapperType);
	}

	private Types() {
	}

}

