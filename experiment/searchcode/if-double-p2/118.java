import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SalaryCalculatorTest {

    protected SalaryCalculator solution;

    @Before
    public void setUp() {
        solution = new SalaryCalculator();
    }

    public static void assertEquals(double expected, double actual) {
        if (Double.isNaN(expected)) {
            Assert.assertTrue("expected: <NaN> but was: <" + actual + ">", Double.isNaN(actual));
            return;
        }
        double delta = Math.max(1e-9, 1e-9 * Math.abs(expected));
        Assert.assertEquals(expected, actual, delta);
    }

    @Test
    public void testCase0() {
        int P1 = 10;
        int P2 = 15;
        int salary = 1000;

        double expected = 100.0;
        double actual = solution.calcHours(P1, P2, salary);

        assertEquals(expected, actual);
    }

    @Test
    public void testCase1() {
        int P1 = 10;
        int P2 = 15;
        int salary = 3000;

        double expected = 266.6666666666667;
        double actual = solution.calcHours(P1, P2, salary);

        assertEquals(expected, actual);
    }

    @Test
    public void testCase2() {
        int P1 = 100;
        int P2 = 1;
        int salary = 1000000;

        double expected = 980200.0;
        double actual = solution.calcHours(P1, P2, salary);

        assertEquals(expected, actual);
    }

    @Test
    public void testCase3() {
        int P1 = 17;
        int P2 = 23;
        int salary = 973546;

        double expected = 42380.260869565216;
        double actual = solution.calcHours(P1, P2, salary);

        assertEquals(expected, actual);
    }

    @Test
    public void testCase4() {
        int P1 = 82;
        int P2 = 8;
        int salary = 12140;

        double expected = 148.0487804878049;
        double actual = solution.calcHours(P1, P2, salary);

        assertEquals(expected, actual);
    }

}

