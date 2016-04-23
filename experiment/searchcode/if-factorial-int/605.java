public class Factorial {
    /** implement your recursive factorial method here **/


    public static void main(String[] args) {
        /**
         * Tests for the factorial() method.
         */
        System.out.println("3! should be 6: " + factorial(3));
        System.out.println("4! should be 24: " + factorial(4));
        System.out.println("5! should be 120: " + factorial(5));
    }
    
    public static int factorial(int n) {
        if (n <= 0) {
            return 1;
        }
        return n * factorial(n - 1);
    }
}
