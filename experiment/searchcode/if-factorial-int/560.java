package recursion;

/*
 * Factorial Defintion
 * factorial(n) = n * (n - 1) * (n - 2) * (n - 3) * . . . * 1 for any integer n > 0
 * factorial(0) = 1
 */
public class Factorial 
{
    public static void main(String[] args)
    {
        Factorial factObj = new Factorial();
        int result;
        
        // Recursuve Factorial method
        result = factObj.recursiveFactorial(3);
        System.out.println(result);
        
        // Iteractive Factorial method
        result = factObj.iterativeFactorial(5);
        System.out.println(result);
    }
    
    /**
     * Recursively computes the factorial of a nonnegative integer.
     * 
     * Recursive Definition
     *  0! = 1  for n = 0
     *  n! = n * factorial(n - 1)   for n > 0
     * 
     * Example: n = 3
     * Depth of Recursion
     * n = 3    3 * factorial(2)
     * n = 2    2 * factorial(1)
     * n = 1    1 * factorial(0)
     * n = 0        1               ** Base case **
     * 
     * Return back
     * factorial(0) = 1
     * factorial(1) = 1 * factorial(0) = 1 * 1 = 1
     * factorial(2) = 2 * factorial(1) = 2 * 1 = 2
     * factorial(3) = 3 * factorial(2) = 3 * 2 = 6
     * 
     * @param   n   Nth factorial to compute 
     * @throws  IllegalArgumentException
     * @return  factorial of n
     */
    public int recursiveFactorial(int n) throws IllegalArgumentException
    {
        // Negative number case
        if (n < 0)
        {
            throw new IllegalArgumentException("Argument cannot be negative.");
        }
        
        // Large number case due to int storage limitations (32 bit = 4 bytes; 2^31)
        else if(n > 16)
        {
            throw new IllegalArgumentException("Argument cannot exceed 16.");
        }
        
        // Base case: 0! = 1
        else if (n == 0)
        {
            return 1;
        }
        
        // n > 0; recurse with n - l on next call 
        else
        {
            return n * recursiveFactorial(n - 1);
        }
    }  
    
    /**
     * Computes the nth factorial iteratively.
     * @param n     Nth factorial to compute
     * @return      n!
     */
    public int iterativeFactorial(int n) throws IllegalArgumentException
    {    
        // Negative number case
        if (n < 0)
        {
            throw new IllegalArgumentException("Argument cannot be negative.");
        }
        
        // Large number case due to int storage limitations (32 bit = 4 bytes; 2^31)
        else if(n > 16)
        {
            throw new IllegalArgumentException("Argument cannot exceed 16.");
        }
        
        int result = 1; // start with initial case 
        
        while(n > 0)
        {
            result = result * n;
            n--;
        }
        
        return result;
    }
}

