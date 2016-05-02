package org.gvsig.operators;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.gvsig.baseclasses.AbstractOperator;
import org.gvsig.baseclasses.IOperator;
import org.gvsig.expresions.EvalOperatorsTask;

/**
 * @author Vicente Caballero Navarro
 */
public class Log extends AbstractOperator{

	public String addText(String s) {
		return toString()+"("+s+")";
	}
	public String toString() {
		return "log";
	}
	public void eval(BSFManager interpreter) throws BSFException {
//		interpreter.eval(ExpressionFieldExtension.BEANSHELL,null,-1,-1,"double log(double value){return java.lang.Math.log(value);};");
		interpreter.exec(EvalOperatorsTask.JYTHON,null,-1,-1,"def log(value):\n" +
				"  import java.lang.Math\n" +
				"  return java.lang.Math.log(value)");
	}
	public boolean isEnable() {
		return (getType()==IOperator.NUMBER);
	}
	public String getDescription() {
        return  "parameter" + ": " +
         "numeric_value" + "\n" +
         "returns" + ": " +
         "numeric_value" + "\n" +
         "description" + ": " +
        "Returns the natural logarithm (base e) of a double  value. Special cases:\n" +
        "* If the argument is NaN or less than zero, then the result is NaN.\n" +
        "* If the argument is positive infinity, then the result is positive infinity.\n" +
        "* If the argument is positive zero or negative zero, then the result is negative infinity.";
    }
}

