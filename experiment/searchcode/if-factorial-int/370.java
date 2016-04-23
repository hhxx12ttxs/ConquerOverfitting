/**
   This program demonstrates factorials using recursion.
*/

public class Recursion
{
   public static void main(String[] args)
   {
      int n = 7;
      int x = 198;
      int y = 1260;

      // Test out the factorial
      System.out.println(n + " factorial equals ");
      System.out.println(Recursion.factorial(n));
      System.out.println();
   }
   
   /**
      This is the factorial method.
      @param n A number.
      @return The factorial of n.
   */
   
   public static int factorial(int n)
   {
      int temp;
      System.out.println("Method call -- " + 
                         "calculating " + 
                         "Factorial of: " + n);
      if (n == 0)
      {
         return 1;
      }
      else
      {
         // return (factorial(n - 1) * n);
         temp = factorial(n - 1);
         System.out.println("Factorial of: " + 
                            (n - 1) + " is " + 
                            temp);
         return (temp * n);
      }
   }
}
