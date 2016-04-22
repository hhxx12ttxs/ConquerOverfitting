package Calculator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Zolani
 */
public class FactorialClass extends As1Class{
    
    public FactorialClass(){
       
    }
    
    public double factorise(String vara){
    
        varfactor = Integer.parseInt(vara);
    
        // varfactor must be between 0 and 12 to avoid int limit;
        if(varfactor < zero || varfactor > factoriallimit){
          factorial = -1;
          
        }    
        else{
          factorial = getFactorial(varfactor);
        }
        return factorial;
    }
    
    //Find factorial through recursive method
    private int getFactorial(int varfactor){
    
      if(varfactor <= 1){
        varfactor = 1;
      }
      
      else{
        varfactor = varfactor * getFactorial(varfactor - 1);
      }
      
      return varfactor;
    
    }
    
}

