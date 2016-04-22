package main.general;

/**
 * Find factorial of a number.
 */
public class Factorial {
    public static void main(String[] args){
        int number = 5;
        System.out.println(factorialrecursive(number));
    }

    private static int factorial(int n){
        int factorial = 1;
        while(n >0){
            factorial = factorial * (n);
            n--;
        }
        return factorial;
    }

    private static int factorialrecursive(int n){
        if(n== 0){
            return 1;
        }
        return n*factorialrecursive(n-1);
    }
}

