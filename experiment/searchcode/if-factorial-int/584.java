/**
 * 
 */
package ensimag.couretn.algorithms;


/**
 * <p>
 * Factorial algorithms.
 * @author Nathanael COURET
 *
 */
public class Factorial {
    
    /**
     *  <p>
     *  Recursively calculate the factorial of n. Assuming n >= 0.
     * @param n an integer >= 0.
     * @return the factorial of n, or 1 if n = 0.
     */
    public static int recFactorial(int n) {
        if(n == 0) {
            return 1;
        }
        return n * recFactorial(n-1);
    }
    
    /**
     * <p>
     * Iteratively calculate the factorial of n. Assuming n >= 0.
     * @param n an integer >= 0.
     * @return the factorial of n.
     */
    public static int itFactorial(int n) {
        int res = 1;
        for(int f = n; f > 0; f--) {
            res *= f;
        }
        return res;
    }

}

