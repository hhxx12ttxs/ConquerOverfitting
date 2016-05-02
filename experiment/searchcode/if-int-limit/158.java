public class NthPrime_10_Sieve_Wheel{

    // Start wheeling. We now also discard multiples of 3.
    // Fewer markings, less space, Although the index
    // arithmetic has become more complicated, this gains
    // another factor of about 1.5.
    //
    // In the sieve, bit number k corresponds to
    //     3*k+5 if k is even, 3*k+4 if k is odd
    public static int nthPrime(int n) {
        if (n < 2) return 2;
        if (n == 2) return 3;
        if (n == 3) return 5;
        int limit, root, count = 2;
        limit = (int)(n*(Math.log(n) + Math.log(Math.log(n)))) + 3;
        root = (int)Math.sqrt(limit);
        switch(limit%6) {
            case 0:
                limit = 2*(limit/6) - 1;
                break;
            case 5:
                limit = 2*(limit/6) + 1;
                break;
            default:
                limit = 2*(limit/6);
        }
        switch(root%6) {
            case 0:
                root = 2*(root/6) - 1;
                break;
            case 5:
                root = 2*(root/6) + 1;
                break;
            default:
                root = 2*(root/6);
        }
        int dim = (limit+31) >> 5;
        int[] sieve = new int[dim];
        for(int i = 0; i < root; ++i) {
            if ((sieve[i >> 5] & (1 << (i&31))) == 0) {
                ++count;
                int start, s1, s2;
                if ((i & 1) == 1) {
                    start = i*(3*i+8)+4;
                    s1 = 4*i+5;
                    s2 = 2*i+3;
                } else {
                    start = i*(3*i+10)+7;
                    s1 = 2*i+3;
                    s2 = 4*i+7;
                }
                for(int j = start; j < limit; j += s2) {
                    sieve[j >> 5] |= 1 << (j&31);
                    j += s1;
                    if (j >= limit) break;
                    sieve[j >> 5] |= 1 << (j&31);
                }
            }
        }
        int p;
        for(p = root; count < n; ++p) {
            if ((sieve[p >> 5] & (1 << (p&31))) == 0) {
                ++count;
            }
        }
        return 3*p+1+(p&1);
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

