/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package math;

/**
 *
 * @author hmedal
 */
public class BinomialCoef {

   // return integer nearest to x
   static long nint(double x) {
      if (x < 0.0) return (long) Math.ceil(x - 0.5);
      return (long) Math.floor(x + 0.5);
   }

   // return log n!
   static double logFactorial(int n) {
      double ans = 0.0;
      for (int i = 1; i <= n; i++)
         ans += Math.log(i);
      return ans;
   }

   // return the binomial coefficient n choose k.
   public static long binomial(int n, int k) {
      return nint(Math.exp(logFactorial(n) - logFactorial(k) - logFactorial(n-k)));
   }


   public static void main(String[] args) {
      int n = Integer.parseInt(args[0]);
      int k = Integer.parseInt(args[1]);
      if (n <= 0 || k > n || k < 0)
         System.out.println("Illegal input.");
      else
         System.out.println(binomial(n, k));
   }

}

