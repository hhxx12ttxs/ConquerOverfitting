package com.itelios.itframework.utils;

/**
 * Utils class used to cast values
 * @author Marc Duvignaud
 */
public class CastUtils {
	
	@SuppressWarnings("unchecked")
	public static <PrimitiveType> PrimitiveType castPrimitiveTypeValue(Object valueToCast, Class<PrimitiveType> primitiveType) throws ClassCastException
	{
		if(primitiveType.equals(String.class))
		{
			String castedValue = valueToCast.toString();
			return primitiveType.cast(castedValue);
		}
		else if(primitiveType.equals(Integer.TYPE) || primitiveType.equals(Integer.class))
		{
			Integer castedValue = Integer.valueOf(valueToCast.toString());
			PrimitiveType realCast = (PrimitiveType) castedValue;
			return realCast;
		}
		else if(primitiveType.equals(Double.TYPE) || primitiveType.equals(Double.class))
		{
			Double castedValue = Double.valueOf(valueToCast.toString());
			PrimitiveType realCast = (PrimitiveType) castedValue;
			return realCast;
		}
		else if(primitiveType.equals(Float.TYPE) || primitiveType.equals(Float.class))
		{
			Float castedValue = Float.valueOf(valueToCast.toString());
			PrimitiveType realCast = (PrimitiveType) castedValue;
			return realCast;
		}
		else if(primitiveType.equals(Long.TYPE) || primitiveType.equals(Long.class))
		{
			Long castedValue = Long.valueOf(valueToCast.toString());
			PrimitiveType realCast = (PrimitiveType) castedValue;
			return realCast;
		}
		else if(primitiveType.equals(Short.TYPE) || primitiveType.equals(Short.class))
		{
			Short castedValue = Short.valueOf(valueToCast.toString());
			PrimitiveType realCast = (PrimitiveType) castedValue;
			return realCast;
		}
		else if(primitiveType.equals(Boolean.TYPE) || primitiveType.equals(Boolean.class))
		{
			Boolean castedValue = Boolean.valueOf(valueToCast.toString());
			PrimitiveType realCast = (PrimitiveType) castedValue;
			return realCast;
		}
		
		return null;
	}
}

