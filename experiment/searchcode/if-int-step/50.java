public class NthPrime_05_Sqrt_Wheel{

    // The next improvement. After skipping even numbers, we also
    // skip multiples of 3, so we need to try only every third number.
    // That leaves numbers of the form 6*k Âą1 to check, with alternate
    // increments of 2 and 4.
    // This idea can be taken further, one can skip all multiples of
    // the first w primes, reducing the fraction of numbers to test
    // with each additional prime in the wheel. Adding a prime p
    // reduces the fraction by a factor of (p-1)/p, so the return
    // diminishes while the sequence of steps from one candidate to
    // the next becomes more complicated. Therefore large wheels are
    // rarely used, typically a wheel contains no more than six or
    // seven primes.
    private static boolean isPrime(int n) {
        if ((n & 1) == 0) return n == 2;
        if (n % 3 == 0) return n == 3;
        int step = 4, m = (int)Math.sqrt(n) + 1;
        for(int i = 5; i < m; step = 6-step, i += step) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static int nthPrime(int n) {
        if (n < 2) return 2;
        if (n == 2) return 3;
        int candidate, count, step = 4;
        for(candidate = 5, count = 2; count < n; step = 6-step, candidate += step) {
            if (isPrime(candidate)) {
                ++count;
            }
        }
        return candidate-step;
    }

    public static void main(String[] args) {
        for(int i = 0; i < args.length; ++i) {
            try {
                int target = Integer.parseInt(args[i]);
                long start = System.currentTimeMillis();
                int prime = nthPrime(target);
                long end = System.currentTimeMillis();
                System.out.println(String.format("Prime No. %8d: %10d\nTime: %8dms",target,prime,(end-start)));
            }
            catch(NumberFormatException nfe) {
                System.out.println("Bad input: " + args[i]);
            }
        }
    }
}

