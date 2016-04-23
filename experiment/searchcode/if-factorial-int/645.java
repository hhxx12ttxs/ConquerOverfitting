public class Exercise2a {
    /**
     * Calculates the factorial of a number. If the number is smaller or equal
     * zero the function will return a value of one.
     */
    static double calculateFactorial(int number) {
        double factorial = 1;

        if (number > 0) {
            for (int i = 1; i <= number; i++) {
                factorial *= i;
            }
        }

        return factorial;
    }

    public static void main(String[] args) {
        final int NUMBER = 5;

        double result = calculateFactorial(NUMBER);

        System.out.println("The Factorial of " + NUMBER + " is " + result);
    }
}

