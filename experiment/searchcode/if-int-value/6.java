package uk.ac.lkl.migen.mockup.shapebuilder.model.expression;

import org.apache.log4j.Logger;

import uk.ac.lkl.migen.mockup.shapebuilder.model.Expression;

/**
 * An expression that is just a double value.
 * 
 * This class abstracts can be used to implement constants and variables. A
 * constant is a value that is always locked and a variable is a value that is
 * unlocked (and changed) some of the time.
 * 
 * @author $Author: darren.pearce $
 * @version $Revision: 799 $
 * @version $Date: 2008-06-03 18:11:17 +0200 (Tue, 03 Jun 2008) $
 * 
 */
public class Value extends Expression {

    /**
     * The value of this instance.
     * 
     */
    private double value;

    private boolean locked;

    Logger logger = Logger.getLogger(Value.class);

    public Value(int value) {
	this((double) value);
    }

    public Value(int value, boolean locked) {
	this((double) value, locked);
    }

    /**
     * Create a new instance with the given value.
     * 
     * @param value
     *                the value
     * 
     */
    public Value(Double value) {
	this(value, true);
    }

    public Value(Double value, boolean locked) {
	this.value = value;
	setLocked(locked);
    }

    // hack: returns true always since may be mutable at some point
    public boolean isMutable() {
	return true;
    }

    // hack: returns true
    public boolean isIndirectlyVariable() {
	return true;
    }

    public void setLocked(boolean locked) {
	if (locked == this.locked)
	    return;
	this.locked = locked;
	fireObjectUpdated();
    }

    public void toggleLocked() {
	setLocked(!locked);
	String lockedState = locked ? "locked" : "unlocked";
	logger.info("Value ID:" + this.getId() + " (name: " + this.getName()
		+ "," + " value: " + this.getValue() + ")" + " is now "
		+ lockedState + ".");
    }

    public boolean isLocked() {
	return locked;
    }

    /**
     * Get the value of this instance.
     * 
     * @return the value
     * 
     */
    public Double getValue() {
	return value;
    }

    // todo: should be Double not double
    public boolean setValue(double value) {
	if (isLocked())
	    return false;

	if (value == this.value)
	    return false;

	double oldValue = this.value;
	this.value = value;
	fireObjectUpdated();
	logger.info("Value ID:" + this.getId() + " (name: " + this.getName()
		+ ")" + " changes from " + oldValue + " to " + this.getValue()
		+ ".");
	return true;
    }

    public void decrement() {
	setValue(value - 1);
    }

    public void increment() {
	setValue(value + 1);
    }

    public boolean isCompound() {
	return false;
    }

    public void prettyPrint(int indent) {
	for (int i = 0; i < indent; i++)
	    System.out.print(" ");
	System.out.println(value);
    }

    /**
     * Return a string representation of the value.
     * 
     * @return the double as a string
     * 
     */
    public String toString() {
	return Double.toString(value);
    }
}

