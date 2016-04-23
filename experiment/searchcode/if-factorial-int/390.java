package Factorial;

/**
 * Author: Alex Yang
 * Date: 9/25/14
 * Dependencies:
 * - N/A
 * Description:
 * - N/A
 * Solution:
 * - N/A
 */
public class Factorial {
  public static void main(String args[]) {
    System.out.println(factorial(4));
  }

  public static int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
  }
}

