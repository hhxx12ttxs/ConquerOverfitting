package Week4;

/**
 * Created by williamrobertshaw on 07/03/15.
 */
public class Factorial {
    public static int factorial (int n){
        if(n == 1){
            return 1;
        }
        return n * factorial(n - 1);
    }
}

