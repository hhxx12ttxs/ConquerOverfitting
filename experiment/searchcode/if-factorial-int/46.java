/**
 * Calculates n!
 *
 * @version     $Id$
 *
 * @author      hp bischof
 *
 * Revisions:
 *	      $Log$
 */

class Factorial
{
   int n;
   public  Factorial(int k)	{
	k = k;
   }
   private int factorial(int n)
   {
	if ( n <= 0 )
		return 1;
	else
		return n * this.factorial(n - 1);
   }

   public static void main(String args[])
   {
      int n = 5;
      Factorial aFactorial = new Factorial(9);
      Factorial bFactorial = new Factorial(10);
      Factorial cFactorial = new Factorial(11);
      Factorial dFactorial = new Factorial(12);

      System.out.println( aFactorial.factorial(n) );
   }
}


