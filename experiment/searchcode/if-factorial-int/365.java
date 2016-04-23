import comp102x.IO;
/**
 * Factorial program.
 * 
 * @author Tibor 
 * @version 0.01
 */
public class factorial
{
    // instance variables - replace the example below with your own
    private int n;
    private long num;

    public static void main(String[] args) {
       factorial sample = new factorial();
       sample.print_factorial();
    }
    
    /**
     * Constructor for objects of class factorial
     */
    public factorial()
    {
        // initialise instance variables
        new_factorial();
    }

    public factorial(int inN)
    {
        // initialise instance variables
        n = inN;
        num = factorial(n);
    }
    
    private int fill_n()
    {
        System.out.print("n: ");
        return IO.inputInteger();
    }
        
    public void print_factorial()
    {
        System.out.println(num);
    }
    
    public void new_factorial()
    {
        n = fill_n();
        num = factorial(n);
    }
    
    private long factorial(int inN)
    {
        if (inN == 0)
            return(1);
        else
            return(inN * factorial(inN - 1));
    }
}

