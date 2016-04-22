package codeBreakdown;

import enums.PrimitiveType;


/**
 * @author Eric Le Fort
 * @version 01
 */

public class Primitive<T> implements Comparable{
	private AccessModifier mod;
	private PrimitiveType type;
	private String name;
	private T value;

	/**
	 * Initializes a new Primitive of the given type that has no initialized value.
	 * @param mod
	 * @param type
	 * @param name
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public Primitive(AccessModifier mod, PrimitiveType type, String name, T value) throws IllegalArgumentException{
		if(!validValue(value)){
			throw new IllegalArgumentException("This isn't a legal value for this type of variable.");
		}

		this.mod = mod;
		this.type = type;
		this.name = name;
		this.value = value;
	}//Constructor

	/**
	 * Initializes a new Primitive of the given type that has no initialized value.
	 * @param type
	 * @param name
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public Primitive(PrimitiveType type, String name, T value) throws IllegalArgumentException{
		if(!validValue(value)){
			throw new IllegalArgumentException("This isn't a legal value for this type of variable.");
		}

		this.mod = AccessModifier.DEFAULT;
		this.type = type;
		this.name = name;
		this.value = value;
	}//Constructor

	/**
	 * Initializes a new Primitive of the given modifier and type that has no initialized value.
	 * @param mod
	 * @param type
	 */
	public Primitive(AccessModifier mod, PrimitiveType type){
		this.mod = mod;
		this.type = type;
	}//Constructor

	/**
	 * Initializes a new Primitive of the given type that has no initialized value.
	 * @param type
	 * @param name
	 */
	public Primitive(PrimitiveType type, String name){
		this.mod = AccessModifier.DEFAULT;
		this.type = type;
	}//Constructor

	/**
	 * Depending on the setting of this variable, check whether
	 * @return Whether the value is possible or not.
	 */
	private boolean validValue(T value){
		if(type == PrimitiveType.BOOLEAN){
			return value instanceof Boolean;
		}else if(type == PrimitiveType.BYTE){
			return value instanceof Byte;
		}else if(type == PrimitiveType.CHAR){
			return value instanceof Character;
		}else if(type == PrimitiveType.SHORT){
			return value instanceof Short;
		}else if(type == PrimitiveType.INT){
			return value instanceof Integer;
		}else if(type == PrimitiveType.FLOAT){
			return value instanceof Float;
		}else if(type == PrimitiveType.LONG){
			return value instanceof Long;
		}else{						//Double
			return value instanceof Double;
		}
	}//validValue()

	/**
	 * Simply calls the Wrapper class's version of equals or checks if both values are null.
	 */
	@Override
	public boolean equals(Object obj){
		if(value == null){
			return obj == null;
		}
		return value.equals(obj);
	}//equals()

	/**
	 * Checks whether this Primitive is greater than the Primitive passed in.
	 * @param comp
	 * @return Whether this is greater than that.
	 */
	public boolean greaterThan(Primitive comp){
		return compareTo(comp) > 0;
	}//greaterThan()
	
	/**
	 * Checks whether this Primitive is less than the Primitive passed in.
	 * @param comp
	 * @return Whether this is greater than that.
	 */
	public boolean lessThan(Primitive comp){
		return compareTo(comp) < 0;
	}//lessThan()

	/**
	 * Returns 1 if this is greater than that, 0 if they're equal or -1 if this is less than that.
	 * @return -1, 0, 1.
	 */
	@Override
	public int compareTo(Object that){
		if(type == PrimitiveType.BYTE){
			if((Byte)value > (Byte)((Primitive<Byte>)that).getValue()){
				
			}
		}
		return 0;
	}

	//TODO javaDoc
	public void setValue(T value) throws IllegalArgumentException{
		if(!validValue(value)){
			throw new IllegalArgumentException("This isn't a legal value for this type of variable.");
		}
		this.value = value;
	}//setValue()

	// Getters & Setters //
	public T getValue(){ return value; }//getValue()
	public PrimitiveType getType(){ return type; }//getType()

}//Primitive
