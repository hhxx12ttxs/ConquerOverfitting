package algorithms.interview;

/**
 * Created on  May 21, 2011
 * @author leeing
 */
public class GCD {

    public static void main(String[] args) {
        System.out.println("gcd() = " + gcd(60, 153));
    }

    public static int gcd(int m, int n) {

        if (m % n == 0) {
            return n;
        } else {
            return gcd(n, m % n);
        }

    }
}

