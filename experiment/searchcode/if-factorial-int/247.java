package chapter_9;

public class Factorial {

  public static void main(String[] args) {
    System.out.println(factorial(4));
  }

  public static int factorial(int n) {
    if(n == 1 || n == 0) {
      return n;
    }

    return n * factorial(n - 1);
  }
}
