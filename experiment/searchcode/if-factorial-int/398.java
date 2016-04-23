package factorialImplementations;

import factorialInterafaces.Factorial;

public class FactorialOptimal
        implements Factorial {

    private static final int MAXIMAL_ARG_VALUE = 12;

    public String calculate(int arg) {
        Factorial result;

        if (arg <= MAXIMAL_ARG_VALUE) {
            result = new FactorialRecursive();
        } else {
            result = new FactorialBigDecimal();
        }

        return result.calculate(arg);
    }
}

