public class Factorial {

    public int factorial(int n) {
        if (n<0 || n>12) {
            throw new IllegalArgumentException("Input parameter out of range: " + n);
        } else {
            if (n==0) {
                return 1;
            } else {
                return n*factorial(n-1);
            }
        }
    }
}
