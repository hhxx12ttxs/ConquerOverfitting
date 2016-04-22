package misc;

import org.junit.Assert;

public class Factorial {

    public static void main(String[] args) {
        Assert.assertEquals(120, factorial(5));
        Assert.assertEquals(120, factorialFast(5));
        Assert.assertEquals(1, factorial(1));
        Assert.assertEquals(1, factorialFast(1));
        Assert.assertEquals(1, factorial(0));
        Assert.assertEquals(1, factorialFast(0));
    }

    public static int factorial(int n) {
        if(n <= 1) return 1;
        return n * factorial(n - 1);
    }

    public static int factorialFast(int n) {
        int factorial = 1;
        int counter = n;
        while(counter > 0) {
            factorial *= counter--;
        }
        return factorial;
    }
}

