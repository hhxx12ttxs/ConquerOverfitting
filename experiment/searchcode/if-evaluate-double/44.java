/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 * @version $$Id$$
 */
public class SingleExpressionTest {
    public static void main(String[] args) {
        checkAssert();
        testExpression("10", new Const(10), new Expression() {
            public double evaluate(double x) {
                return 10;
            }
        });
        testExpression("x", new Variable("x"), new Expression() {
            public double evaluate(double x) {
                return x;
            }
        });
        testExpression("x+2", new Add(new Variable("x"), new Const(2)), new Expression() {
            public double evaluate(double x) {
                return x + 2;
            }
        });
        testExpression("2-x", new Subtract(new Const(2), new Variable("x")), new Expression() {
            public double evaluate(double x) {
                return 2 - x;
            }
        });
        testExpression("3*x", new Multiply(new Const(3), new Variable("x")), new Expression() {
            public double evaluate(double x) {
                return 3*x;
            }
        });
        testExpression("x/-2", new Divide(new Variable("x"), new Const(-2)), new Expression() {
            public double evaluate(double x) {
                return -x / 2;
            }
        });
        testExpression("x*x+(x-1)/10", new Add(
                new Multiply(new Variable("x"), new Variable("x")),
                new Divide(new Subtract(new Variable("x"), new Const(1)), new Const(10))), new Expression() {
            public double evaluate(double x) {
                return x * x + (x - 1) / 10;
            }
        });
        System.out.println("OK");
    }

    private static void testExpression(String description, Expression actual, Expression expected) {
        System.out.println("Testing " + description);
        for (int i = 0; i < 10; i++) {
            assertEquals(String.format("f(%d)", i), actual.evaluate(i), expected.evaluate(i));
        }
    }


    private static void assertTrue(boolean condition, String message) {
        assert condition : message;
    }

    private static void assertEquals(String message, double actual, double expected) {
        assertTrue(Math.abs(actual - expected) < 1e-9, String.format("%s: Expected %f, found %f", message, expected, actual));
    }

    private static void checkAssert() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (!assertsEnabled) {
            throw new AssertionError("You should enable assertions by running 'java -ea SingleExpressionTest'");
        }
    }
}

