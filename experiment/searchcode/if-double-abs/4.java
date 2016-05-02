package org.gvsig.operators;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.gvsig.baseclasses.AbstractOperator;
import org.gvsig.baseclasses.IOperator;
import org.gvsig.expresions.EvalOperatorsTask;

/**
 * 
 * @author Vicente Caballero Navarro
 */
public class Abs extends AbstractOperator {
	public String addText(String s) {
		return toString() + "(" + s + ")";
	}

	public String toString() {
		return "abs";
	}

	public void eval(BSFManager interpreter) throws BSFException {
		interpreter.exec(EvalOperatorsTask.JYTHON, null, -1, -1,
				"def abs(value):\n" + "  import java.lang.Math\n"
						+ "  return java.lang.Math.abs(value)\n");
	}

	public boolean isEnable() {
		return (getType() == IOperator.NUMBER);
	}

	public double abs(double value) {
		return java.lang.Math.abs(value);
	}

	public String getDescription() {
		return "parameter"
				+ ": "
				+ "numeric_value"
				+ "\n"
				+ "returns"
				+ ": "
				+ "numeric_value"
				+ "\n"
				+ "description"
				+ ": "
				+ "Returns the absolute value of a double value. If the argument is not negative, the argument is returned. "
				+ "If the argument is negative, the negation of the argument is returned.\n "
				+ "Special cases:\n"
				+ "* If the argument is positive zero or negative zero, the result is positive zero.\n"
				+ "* If the argument is infinite, the result is positive infinity.\n"
				+ "* If the argument is NaN, the result is NaN.";
	}
}

