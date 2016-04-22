package kuy7.factorial;

public class Factorial {

    public int factorial(int n) {
        int factorial = 1;

        if (n < 0 || n > 12) {
            throw new IllegalArgumentException("Not available argument:" + n);
        } else if(n == 0) {
            return 1;
        }

        for (int i = 1; i <= n; i ++) {
            factorial *= i;
        }

        return factorial;
    }

}

