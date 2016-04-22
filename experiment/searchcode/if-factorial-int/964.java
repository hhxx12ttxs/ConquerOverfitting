
public class Twitter1 {

    public static void main(String[] args) {

        int stuff = 7;
        System.out.println(factorial(stuff-1));
        System.out.println(factorial(stuff-1)%stuff);
        System.out.print(factorialRemainder(stuff));
    }

    static int factorialRemainder(int n) {

        int count = 0;

        if (n == 1) {
            return 1;
        }

        for (int i = 1; i <= n; i++) {
            if ((factorial(i-1)%i) == (i - 1)) {
                count++;
            }
        }
        return count;
    }

    static long factorial(long n) {
        long fact = 1;

        if (n == 0 || n == 1) {
            return 1;
        }

        for (long i = n; i >= 1; i--) {
            fact *= i;
        }
        return fact;
    }
}

