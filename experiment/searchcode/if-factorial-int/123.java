package ru.teach.skype.types.my.tests.factorial;

/**
 * This class calculates factorial specified number.
 */
public class CalculateFactorial implements ICalculateFactorial {

    /**
     * Calculates a factorial - forEach
     *
     * @param number factorial this number will be find.
     * @return factorial
     */
    public int calculateFactorialForEach(int number) {
        int[] num = new int[number];
        int factorial = 1;
        for (int i : num) {
            factorial = factorial * number;
            number--;
        }
        return factorial;

    }

    /**
     * Calculates a factorial - recursive.
     *
     * @param n factorial this number will be find.
     * @return factorial
     */
    public int calculateFactorialRecursive(int n) {
        if (n == 1) {
            return 1;
        }
        return calculateFactorialRecursive(n - 1) * n;
    }


}


