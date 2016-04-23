package numbers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Finds the factorial of a number recursively.
 * @author David Robles <drobles@gmail.com>
 */
public class FactorialRecursive {

    public static int factorial(int n) {
        if (n == 1)
            return 1;
        return n * factorial(n - 1);
    }

    @Test
    public void testFactorialRecursive() {
        assertEquals(2, factorial(2));
        assertEquals(6, factorial(3));
        assertEquals(24, factorial(4));
        assertEquals(120, factorial(5));
        assertEquals(720, factorial(6));
    }
}

