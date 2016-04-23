package com.mpdeimos.webscraper.util;

/**
 * Utility function for reflections.
 * 
 * @author mpdeimos
 */
public class Reflections
{

	/**
	 * Converts a string to a primitive type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T stringToPrimitive(Class<T> primitiveType, String value)
	{
		if (isOneOf(primitiveType, char.class, Character.class))
		{
			return (T) Character.valueOf(value.charAt(0));
		}
		if (isOneOf(primitiveType, byte.class, Byte.class))
		{
			return (T) Byte.valueOf(value);
		}
		if (isOneOf(primitiveType, short.class, Short.class))
		{
			return (T) Short.valueOf(value);
		}
		if (isOneOf(primitiveType, int.class, Integer.class))
		{
			return (T) Integer.valueOf(value);
		}
		if (isOneOf(primitiveType, long.class, Long.class))
		{
			return (T) Long.valueOf(value);
		}
		if (isOneOf(primitiveType, float.class, Float.class))
		{
			return (T) Float.valueOf(value);
		}
		if (isOneOf(primitiveType, double.class, Double.class))
		{
			return (T) Double.valueOf(value);
		}
		if (isOneOf(primitiveType, boolean.class, Boolean.class))
		{
			return (T) Boolean.valueOf(value);
		}

		throw new IllegalArgumentException("Could not convert '" + value //$NON-NLS-1$
				+ "' to type" + primitiveType); //$NON-NLS-1$
	}

	/** Checks if a class is one of a given list of classes. */
	public static boolean isOneOf(Class<?> clazz, Class<?>... comparees)
	{
		for (Class<?> comparee : comparees)
		{
			if (clazz == comparee)
			{
				return true;
			}
		}
		return false;
	}

	/** Checks whether the type is a primitive or a wrapper of a primitive type. */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz)
	{
		return clazz.isPrimitive()
				|| isOneOf(
						clazz,
						Character.class,
						Byte.class,
						Short.class,
						Integer.class,
						Long.class,
						Float.class,
						Double.class,
						Boolean.class);
	}
}

