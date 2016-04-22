package src.com.sobol;

/**
 * Created by Fawkes on 13.02.14.
 */
public class FactorialUtil {
    public static int factorial(int n)
    {
        if (n == 0) return 1;
        return n * factorial(n-1);
    }
}

