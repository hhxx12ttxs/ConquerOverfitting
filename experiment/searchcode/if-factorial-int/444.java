
/**
 * Write a description of class Factorial here.
 * 
 * @author Geoffrey Gross 
 * @version 9/19/2013
 */
public class Factorial
{

    /**
     * Constructor for objects of class Factorial
     */
    public Factorial()
    {
        
    }

   /**
    * public method for factorial.
    * @ return the nth factorial after calling recursive method.
    * @param the n factorial.
    */
   public int pubFact(int fact)
   {
       return factRecur(fact);
    }
    
    /**
     * private recursive method for factorial.
     * @param the nth factorial.
     * @return the nth factorial.
     */
    private int factRecur(int f)
    {
        if(f < 1)
            return 1;
        
        return f*factRecur(f-1);
    }
}

