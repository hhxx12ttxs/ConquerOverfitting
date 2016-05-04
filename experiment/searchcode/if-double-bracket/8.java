
package equationevaluator;

/**
 * A union of variables, operators and bracket types
 * @author michael
 */
public class MathObject {
    static final int NO_TYPE = -1;
    static final int VAR_TYPE = 0;
    static final int VAL_TYPE = 1;
    static final int OP_TYPE = 2;
    static final int BRAC_TYPE = 3;
    
    char var;
    double val;
    boolean sign;
    Operation Operator = Operation.NONE;
    char bracket;
    
    int type;// -1, indeterminint, 0 for var, 1 for val and 2 for operator, 3 for bracket
    /**
     * Initializes math object as a variable
     * @param c the variable, that mathobject will represent
     */
    
    /**
     * Initializes math object as a variable or bracket
     * @param c the variable or bracket that mathobject will represent
     * @param brac if true <c> is a bracket, if false <c> is a variable
     */
    public MathObject(char c, boolean brac, boolean sign){
        if(brac){
            bracket = c;
            type = 3;
        }else{
            this.sign = sign;
            var = c;
            type = 0;
        }
    }
    
    /**
     * Initializes Math object as a value
     * @param d the value that the mathobject will represent
     */
    public MathObject(double d){
        val = d;
        type = 1;
    }
    
    /**
     * Initializes Mathobject as a Operation
     * @param op the Operation that math object will represent
     */
    public MathObject(Operation op){
        Operator = op;
        type = 2;
    }
      /**
     * Initializes MathObject as a none type
     */
    public MathObject(){
          type = -1;
      }
    
    /**
     * Makes math object a variable
     * @param var The variable to be set as
     */
    public void setVar(char var, boolean sign){
       type = 0;
       this.sign = sign;
       this.var = var;
    }
    /**
     * Makes math object a value
     * @param d the double to be set as
     */
    public void setVal(double d){
        type = 1;
        val = d;
    }
    /**
     * Makes math object an operation
     * @param op the operation to be set as
     */
    public void setOperation(Operation op){
        type = 2;
        Operator = op;
    }
    /**
     * Sets the Mathobject to a bracket
     * 
     * @param brac The bracket to be set as
     */
    public void setBracket(char brac){
        type = 3;
        bracket = brac;
    }
    
    /**
     * Compares this object with <m>
     * @param m The mathobject to compare to this object
     * @return true if mathobjects are identical, false otherwise or if non type
     */
    public boolean isEqual(MathObject m){
        if(m.type != type){
            return false;
        }
        switch(type){
            case 0:
                return var == m.var;
            case 1:
                return val == m.val;
            case 2:
                return Operator == m.Operator;
            case 3:
                return bracket == m.bracket;
            default: 
                return false; //indeterminent is always false
        }
    }
    
    /**
     * Prints the value represented by this object
     */
    public void PrintRepresentation(){
        switch(type){
            case 0:
                System.out.print(var);
                break;
            case 1:
                System.out.print(val);
                break;
            case 2:
                System.out.print(Operator.toString());
                break;
            case 3:
                System.out.print(bracket);
                break;
            default:
                System.out.print(" Err ");
        }
    }
}

