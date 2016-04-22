/*
 * Program to calculate the factorial values of the upplied numbers
 * First time using the functions in Java to calculate factorial
 * Usage :
 *  java Factorial <number>
 * Author : Dhruman Bhadeshiya <dhrumangajjar@gmail.com>
*/
class Factorial
{
  public static void main(String[] arg)
  {
    System.out.println("Calculating factorial of " + arg[0]);
    int answer = factorial(Integer.parseInt(arg[0]));
    System.out.println("factorial is " + answer);
  }
  public static int factorial(int x)
  {
    if(x == 0)
      return 1;
    else
      return x*factorial(x-1);
  }
}

