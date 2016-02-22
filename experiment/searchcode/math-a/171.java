package uk.ac.lkl.migen.system.expresser.model;

import uk.ac.lkl.common.util.value.Unit;
import uk.ac.lkl.common.util.value.Value;

/**
 * The handle for an attribute.
 * 
 * This contains information about the name of an attribute, its type (e.g.
 * IntegerValue) and its units (?).
 * 
 * @author $Author: toontalk@gmail.com $
 * @version $Revision: 6996 $
 * @version $Date: 2010-08-26 18:44:16 +0200 (Thu, 26 Aug 2010) $
 * 
 */
public class AttributeHandle<T extends Value<T>> implements
	Comparable<AttributeHandle<?>> {

    private String name;

    private Unit units;

    private Class<T> valueClass;

    private Integer primaryKey;

    private Integer secondaryKey;

    private boolean visible;

    private boolean copyable = true;

    public AttributeHandle(Class<T> valueClass, String name) {
	this(valueClass, name, null);
    }

    public AttributeHandle(Class<T> valueClass, String name, int primaryKey) {
	this(valueClass, name, null, primaryKey, null);
    }

    public AttributeHandle(Class<T> valueClass, String name, int primaryKey,
	    int secondaryKey) {
	this(valueClass, name, null, primaryKey, secondaryKey);
    }

    public AttributeHandle(Class<T> valueClass, String name, Unit units) {
	this(valueClass, name, units, null, null);
    }

    public AttributeHandle(Class<T> valueClass, String name, Unit units,
	    Integer primaryKey) {
	this(valueClass, name, units, primaryKey, null);
    }

    public AttributeHandle(Class<T> valueClass, String name, Unit units,
	    Integer primaryKey, Integer secondaryKey) {
	this.valueClass = valueClass;
	this.name = name;
	this.units = units;
	this.primaryKey = primaryKey;
	this.secondaryKey = secondaryKey;
	setVisible(true);
    }

    public boolean isVisible() {
	return visible;
    }

    // need to fire event so attribute lists can change as applicable
    public void setVisible(boolean visible) {
	this.visible = visible;
    }

    public Class<T> getValueClass() {
	return valueClass;
    }

    /**
     * Get the name of this instance.
     * 
     * @return the name
     * 
     */
    public String getName() {
	return name;
    }

    /**
     * Get the units of this instance.
     * 
     * @return the units
     * 
     */
    public Unit getUnits() {
	return units;
    }

    public String toString() {
	return name + " (" + valueClass.getName() + ")";
    }

    public Integer getPrimaryKey() {
	return primaryKey;
    }

    public Integer getSecondaryKey() {
	return secondaryKey;
    }

    // hack: base-class behaviour so that NumericAttributeHandle returns true.
    public boolean isNumeric() {
	return false;
    }

    public int compareTo(AttributeHandle<?> other) {
	Integer primaryKeyComparison = compareKeys(this.primaryKey,
		other.primaryKey);

	if (primaryKeyComparison == null)
	    if (primaryKey == null)
		return this.name.compareTo(other.name);
	    else {
		Integer secondaryKeyComparison = compareKeys(this.secondaryKey,
			other.secondaryKey);
		if (secondaryKeyComparison == null)
		    return this.name.compareTo(other.name);
		else
		    return secondaryKeyComparison;
	    }
	else
	    return primaryKeyComparison;

	// if (this.primaryKey == null) {
	// if (other.primaryKey == null) {
	// return this.name.compareTo(other.name);
	// } else
	// return 1;
	// } else {
	// if (other.primaryKey == null) {
	// return -1;
	// } else {
	// if (this.primaryKey.equals(other.primaryKey)) {
	//
	// } else {
	// return this.primaryKey - other.primaryKey;
	// }
	// }
	// }
    }

    private Integer compareKeys(Integer thisKey, Integer otherKey) {
	if (thisKey == null)
	    if (otherKey == null)
		return null;
	    else
		return 1;
	else if (otherKey == null)
	    return -1;
	else if (thisKey.equals(otherKey))
	    return null;
	else
	    return thisKey - otherKey;
    }

    public boolean mayHaveChangeListener() {
	// if a subclass returns false then listeners aren't triggered
	return true;
    }
    
    // overriden by ColorResourceAttributeHandle
    public boolean isColorResourceHandle() {
	return false;
    }
    
    public boolean isIncrement() {
	return false;
    }

    public boolean isCopyable() {
	return copyable;
    }

    protected void setCopyable(boolean copyable) {
        this.copyable = copyable;
    }

}

