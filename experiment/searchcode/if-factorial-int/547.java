package Recursion1;

/**
 * Created by IntelliJ IDEA.
 * User: Matthew M Jenkins
 * Date: 2/17/12
 * Time: 4:23 PM
 */
public class factorial {
    public int factorial(int n){
        if(n == 1) return 1;
        return n*factorial(n-1);
    }
}

