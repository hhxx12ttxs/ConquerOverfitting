package tasks;

/**
 */
public class Factorial_5 {


    public static void main(String[] args) {

        Factorial_5 factorial_5 = new Factorial_5();

        System.out.println(factorial_5.factorial(6));

        System.out.println(factorial_5.factorialRecursive(10));

    }


    public int factorial(int n) {

        int result = 1;

        for (int i = 2; i <= n; i++) {
            result *= i;
        }

        return result;
    }


    public int factorialRecursive(int n) {
        if (n == 1) return n;

        return factorial(n - 1) * n;
    }
}

