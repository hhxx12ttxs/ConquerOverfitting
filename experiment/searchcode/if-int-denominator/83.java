package lilleswing.gcj.a2014.c1.a;

import com.google.common.collect.Lists;
import lilleswing.gcj.util.Problem;

import java.util.List;

public class A extends Problem<Case> {
    private static final String IMPOSSIBLE = "impossible";
    final int MAX_GEN = 2;
    long DENOMINATOR;
    List<Long> values = Lists.newArrayList();

    @Override
    public String solve(Case aCase) {
        if(!aCase.isValid) {
            return IMPOSSIBLE;
        }
        int counter = 1;
        for (final Long value : values) {
            if (aCase.getNumerator() > value) {
                return String.valueOf(counter);
            }
            counter++;
        }
        return IMPOSSIBLE;
    }

    @Override
    public List<Case> parse(String data) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void preCompute() {
        long startVal = 2;
        values.add(startVal);
        for(int i = 1; i < 2; i++) {
            startVal *= 2;
            values.add(startVal);
        }
        values = Lists.reverse(values);

        DENOMINATOR = exponent(2, MAX_GEN+1);
    }

    public long exponent(long value, long power) {
        long tValue = 1;
        for(int i = 0; i < power; i++) {
            tValue *= value;
        }
        return tValue;
    }

    public static void main(String[] args) {
        Problem a = new A();
        a.main(args[0]);
    }
}

class Case {
    final long numerator;
    final long baseDenominator;
    final boolean isValid;

    public Case(long numerator, long denominator, long baseDenominator) {
        long multiplier = baseDenominator / denominator;

        this.numerator = numerator * multiplier;
        this.baseDenominator = baseDenominator;
        if (multiplier * denominator != baseDenominator) {
            isValid = false;
        } else {
            isValid = true;
        }
    }

    public long getNumerator() {
        return numerator;
    }
}

