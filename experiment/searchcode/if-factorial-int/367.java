
/**
 * Write a description of class binomialCoeff here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class binomialCoeff
{
    public static int binomialCoeff(int n, int k) {
        return factorial(n) / (factorial(n - k) * factorial(k));
    }

    private static int factorial(int n) {
        if(n == 0) {
            return 1;
        } else {
            return n * factorial(n - 1);
        }
    }
}

