package chapter1.partial1;

/**
 * exercise 20 from chapter 1 partial 1
 * - compute the value of ln(N!)
 */
public class exercise20 {
    public static double lnOfFactorial (int N)
    {
        double factN = (double)factorial(N);
        return Math.log(factN);
    }

    public static long factorial (int N)
    {
        if (N < 0)
            return 0;
        if (N == 0)
            return 1;
        if (N == 1 || N == 2)
            return N;

        return factorialTree(2, N);
    }

    private static long factorialTree (int l, int r)
    {
        if (l > r)
            return 1;
        if (l == r)
            return l;
        if (r - l == 1)
            return (long) l * r;

        int m = (l + r) / 2;

        return factorialTree(l, m) * factorialTree(m + 1, r);
    }

    public static void main (String[] args)
    {
        int N = Integer.parseInt(args[0]);

        System.out.println(lnOfFactorial(N));
    }
}

