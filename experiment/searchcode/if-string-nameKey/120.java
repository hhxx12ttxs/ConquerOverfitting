package com.tll.model;

import com.tll.IDescriptorProvider;

/**
 * NameKey - Simple entity key that holds an entity name and also identifies the
 * field by which that name is retrieved from the entity.
 * @param <T> key type
 * @author jpk
 */
public final class NameKey<T> implements IDescriptorProvider {

	public static final String DEFAULT_FIELDNAME = INamedEntity.NAME;

	private final Class<T> type;

	/**
	 * The name used to identify the field that holds the name.
	 */
	private String nameProperty;

	/**
	 * The actual name value.
	 */
	private String name;

	/**
	 * Constructor
	 * @param entityClass
	 */
	public NameKey(Class<T> entityClass) {
		this(entityClass, null, DEFAULT_FIELDNAME);
	}

	/**
	 * Constructor
	 * @param entityClass
	 * @param name
	 */
	public NameKey(Class<T> entityClass, String name) {
		this(entityClass, name, DEFAULT_FIELDNAME);
	}

	/**
	 * Constructor
	 * @param type the entity class
	 * @param name
	 * @param propertyName
	 */
	public NameKey(Class<T> type, String name, String propertyName) {
		if(type == null) throw new IllegalArgumentException("A key type must be specified.");
		this.type = type;
		setName(name);
		setNameProperty(propertyName);
	}

	public Class<T> getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The name of the property that identifies the name.
	 */
	public final String getNameProperty() {
		return nameProperty;
	}

	/**
	 * Sets the name of the property that identifies the name.
	 * @param nameProperty
	 */
	public final void setNameProperty(String nameProperty) {
		if(nameProperty == null) throw new IllegalArgumentException("A field name must be specified");
		this.nameProperty = nameProperty;
	}

	@Override
	public String descriptor() {
		return getNameProperty() + ": " + getName();
	}

	public void clear() {
		this.name = null;
	}

	public boolean isSet() {
		return name != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nameProperty == null) ? 0 : nameProperty.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		NameKey<?> other = (NameKey<?>) obj;
		if(name == null) {
			if(other.name != null) return false;
		}
		else if(!name.equals(other.name)) return false;
		if(nameProperty == null) {
			if(other.nameProperty != null) return false;
		}
		else if(!nameProperty.equals(other.nameProperty)) return false;
		if(type == null) {
			if(other.type != null) return false;
		}
		else if(!type.equals(other.type)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "NameKey [type=" + type + ", nameProperty=" + nameProperty + ", name=" + name + "]";
	}
}

