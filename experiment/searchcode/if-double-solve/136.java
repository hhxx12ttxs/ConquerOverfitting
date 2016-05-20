package com.luver;

/**
 * Author: Vladislav Lubenskiy, vlad.lubenskiy@gmail.com
 */
public class DichotomySolver implements Solver {
    @Override
    public double solve(final Equation equation, double left, double right, double error) {
        Function function = new Function() {
            @Override
            public double eval(double x) {
                return equation.getLeft().eval(x) - equation.getRight().eval(x);
            }
        };

        if (right < left) throw new IllegalArgumentException("???i???? i???????!");
        double center = 0;

        while (right - left > error) {
            center = (right + left) / 2;
            if (function.eval(right) * function.eval(center) <= 0) {
                left = center;
            } else {
                right = center;
            }
        }
        double result = function.eval(center);
        if (result > -0.001 && result < 0.001)
            return center;
        else
            throw new RuntimeException("???? ?i?????!");
    }
}

