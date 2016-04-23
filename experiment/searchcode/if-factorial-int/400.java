package algo.sorting.insertion.algo.recursion;

/**
 * Created by Jainik on 1/28/2015.
 */
class Factorial {
    public static void main(String[] args)
    {
        System.out.println("Factorial of 6: " + factorial(6));
    }

    public static int factorial(int n)
    {
        if(n == 0 || n ==1)
        {
            return 1;
        }
        else {
            return n * (factorial(n-1));
        }
    }
}
