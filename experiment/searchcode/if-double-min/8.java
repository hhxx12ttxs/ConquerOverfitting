package org.gvsig.operators;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.gvsig.baseclasses.AbstractOperator;
import org.gvsig.baseclasses.IOperator;
import org.gvsig.expresions.EvalOperatorsTask;
/**
 * @author Vicente Caballero Navarro
 */
public class Min extends AbstractOperator{

	public String addText(String s) {
		return toString()+"("+s+")";
	}
	public String toString() {
		return "min";
	}
	public void eval(BSFManager interpreter) throws BSFException {
//		interpreter.eval(ExpressionFieldExtension.BEANSHELL,null,-1,-1,"double min(double value1,double value2){return java.lang.Math.min(value1,value2);};");
		interpreter.exec(EvalOperatorsTask.JYTHON,null,-1,-1,"def min(value1, value2):\n" +
				"  import java.lang.Math\n" +
				"  return java.lang.Math.min(value1,value2)");
	}
	public boolean isEnable() {
		return (getType()==IOperator.NUMBER);
	}
	public String getTooltip(){
		return "operator"+":  "+toString()+ "("+ "parameter"+"1,"+"parameter"+"2"+")\n"+getDescription();
	}
	public String getDescription() {
        return  "parameter" + "1"+": " +
         "numeric_value" + "\n"+
         "parameter" + "2"+": " +
         "numeric_value" + "\n"+
         "returns" + ": " +
         "numeric_value" + "\n" +
         "description" + ": " +
        "Returns the smaller of two int values. That is, the result the argument closer to the value of Integer.MIN_VALUE.\n" +
        "If the arguments have the same value, the result is that same value.";
    }
}

