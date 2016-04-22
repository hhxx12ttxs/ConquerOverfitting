/**
 * User: jatin
 */
public class DigitFactorials {
    public static int factorialSum(String x) {
        int sum = 0;
        for(int i=0; i < x.length(); i++) {
            sum += factorial( (x.charAt(i) - '0'));
        }
        return sum;
    }

    private static int factorial(int i) {
        if( i == 0) return 1;
        return i * factorial(i -1);
    }

    public static void main(String[] args) {
        int sum = 0;

        for(int i=3; i < 30000000; i++) {
            int factorialSum = factorialSum(i + "");
            if( i == factorialSum) {
                sum += factorialSum;
                System.out.println(factorialSum);
            }
        }
        System.out.println("factorial sum: " + sum);
    }
}

