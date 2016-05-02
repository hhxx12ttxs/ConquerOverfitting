package org.technikradio.cf;

import org.technikradio.task.ID;

/**
 * This is not anything more than a double yet but it is necessary because lists donÂ´t accept primitive types.
 * @author Doralitze
 *
 */
public class Value {
	private double value;
	private final ID UUID; //Nessessary to make shure that the value will be pushed on the stack even if there is already one of that type
	
	public Value(double value){
		setValue(value);
		UUID = new ID("value:".concat(Double.toHexString(value)), false);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public ID getUUID() {
		return UUID;
	}
}

