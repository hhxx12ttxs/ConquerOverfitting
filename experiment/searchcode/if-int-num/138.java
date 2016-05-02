package ambenavente1.projecteuler;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class description goes here.
 *
 * @author Anthony Benavente
 * @version 4/23/14
 */
public class Problem4Fast {

    public void go() {

        // Number of digits to solve for. The problem calls for 3.
        final int digits = 3;

        long start = System.currentTimeMillis();

        // Biggest product of the specified number of digits
        int largestProd = (int) Math.pow(Math.pow(10, digits) - 1, 2);

        // Smallest product of the specified number of digits
        int smallestProd = (int) Math.pow(Math.pow(10, digits - 1), 2);

        int largestPal = 0;

        // Algorithm goes through starting from the largest product,
        // and the first palindrome that has two factors that are four
        // digits is the answer
        for (int i = largestProd; largestPal == 0 && i > smallestProd; i--) {
            if (isPalindrome(i)) {
                if (getFactorsOfLength(i, digits).size() > 1) {
                    largestPal = i;
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(largestPal);
        System.out.println("Time took: " + (end - start) + "ms");
    }

    private List<Long> getFactorsOfLength(long num, int digits) {

        List<Long> factors = new ArrayList<Long>();

        // Min number of digits
        long startNum = (int) Math.pow(10, digits - 1);

        // Max number of digits
        long maxNum = (int) Math.pow(10, digits);

        for (long i = startNum; i < Math.sqrt(num); i++) {
            if (num % i == 0) {
                if (num / i != num && num / i < maxNum) {
                    factors.add(i);
                    factors.add(num / i);
                }
            }
        }

        return factors;

    }

    private boolean isPalindrome(long num) {
        String numStr = String.valueOf(num);

        boolean pal = true;
        for (int i = 0; pal && i < numStr.length() / 2; i++) {
            pal = numStr.charAt(i) == numStr.charAt(numStr.length() - i - 1);
        }
        return pal;
    }

    /**
     * Checks if a number is a palindrome by converting it to a string. A
     * palindromic number is a number that reads the same forwards and
     * backwards.
     *
     * @param num the number to check for
     * @return if the number is a palindrome
     */
    private boolean isPalindrome(int num) {
        return isPalindrome((long)num);
    }

    private List<Long> getFactorsOfLength(int num, int digits) {
        return getFactorsOfLength((long)num, digits);
    }

    public static void main(String[] args) {
        new Problem4Fast().go();
    }
}

