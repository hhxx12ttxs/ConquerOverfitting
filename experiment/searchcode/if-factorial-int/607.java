/**
 * Created by ����� on 29.10.2015.
 */
public class Factorial {
    public static int factorial (int n){
        if (n==1){
            return 1;
        } else {
            return n*factorial(n-1);
        }
    }
}

