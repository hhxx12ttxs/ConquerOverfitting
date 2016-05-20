public class NthPrime_12_SFPP{

    // We only test candidates in int range, so these bases are sufficient
    // to prove primality for us.
    private static final int[] TEST_BASE = { 2, 7, 61 };
    private static final int NUM_BASES = 3;

    // calculate base^exponent mod modulus
    // |modulus| must be < 2^31.5 and nonzero, exponent nonnegative
    private static long modPow(long base, long exponent, long modulus) {
        if (exponent == 0) return 1;
        if (exponent < 0) throw new IllegalArgumentException("Can't handle negative exponents");
        if (modulus < 0) modulus = -modulus;
        base %= modulus;
        if (base < 0) base += modulus;
        long aux = 1;
        while(exponent > 1) {
            if ((exponent & 1) == 1) {
                aux = (aux * base) % modulus;
            }
            base = (base * base) % modulus;
            exponent >>= 1;
        }
        return (aux * base) % modulus;
    }

    private static boolean isPrime(int n) {
        if ((n & 1) == 0) return n == 2;
        if (n % 3 == 0) return n == 3;
        int step = 4, m;
        boolean onlyTD;
        if (n < 40000) {
            m = (int)Math.sqrt(n) + 1;
            onlyTD = true;
        } else {
            m = 100;
            onlyTD = false;
        }
        for(int i = 5; i < m; step = 6-step, i += step) {
            if (n % i == 0) {
                return false;
            }
        }
        if (onlyTD) {
            return true;
        }
        long md = n, n1 = n-1, exp = n-1;
        int s = 0;
        do {
            ++s;
            exp >>= 1;
        }while((exp & 1) == 0);
        // now n-1 = 2^s * exp
        for(int i = 0; i < NUM_BASES; ++i) {
            long r = modPow(TEST_BASE[i],exp,md);
            if (r == 1) continue;
            int j;
            for(j = s; j > 0; --j){
                if (r == n1) break;
                r = (r * r) % md;
            }
            if (j == 0) return false;
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

