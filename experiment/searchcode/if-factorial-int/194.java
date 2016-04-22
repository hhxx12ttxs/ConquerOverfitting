package corejava.factorial;

public class FactorialByRecursion {
    public long factorialOfNumber(int i) {
        if (i < 0) {
            throw new FactorialProgramRuntimeException("FactorialProgramRuntimeException specified that number should be non negative?");
        }

        if (i == 0) {
            return 1;
        }
        return i * factorialOfNumber(i - 1);
    }
}

