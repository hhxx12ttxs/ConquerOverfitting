// Compare a recursive algorithm to an iterative equivalent.
class Factorial
{
  // Recursive method calls itself on line 10
  int getFactorialRecursive(int n)
  {
    System.out.println("n equals " + n);
    int result;
    if(n==1) return 1;
    result = getFactorialRecursive(n-1) * n; // recursion
    return result;
  }

  // non-recursive, iterative equivalent.
  int getFactorialIterative(int n)
  {
    int t, result;
    result = 1;
    for(t=1; t <= n; t++)
    {
      System.out.println("n equals " + n);
      result *= t;
    }
    return result;
  }
}

class RecursionForFactorial {
  public static void main(String[] args)
  {
    Factorial f = new Factorial();

    System.out.println("Factorials using recursive method.");
    System.out.println("Factorial of 3 is " + f.getFactorialRecursive(3));
    System.out.println("Factorial of 4 is " + f.getFactorialRecursive(4));
    System.out.println("Factorial of 5 is " + f.getFactorialRecursive(5));
    System.out.println();

    System.out.println("Factorials using iterative method.");
    System.out.println("Factorial of 3 is " + f.getFactorialIterative(3));
    System.out.println("Factorial of 4 is " + f.getFactorialIterative(4));
    System.out.println("Factorial of 5 is " + f.getFactorialIterative(5));
  }
}
