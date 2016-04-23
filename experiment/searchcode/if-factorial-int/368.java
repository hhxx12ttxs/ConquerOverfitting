package practice;
import java.math.BigInteger;

public class Factorial {


  public static void main(String[] args) {
    int N = 5;
    System.out.println(fact(N));
    System.out.println(factorial(20));

  }

  private static int fact(int n) { 
    if (n <= 1) return 1;
    return n*fact(n-1);
  }
  
  //larger factorial
  private static BigInteger factorial(int number) {
    
    BigInteger factorial = BigInteger.ONE;

    for (int i = number; i > 0; i--) {
        factorial = factorial.multiply(BigInteger.valueOf(i));
    }

    return factorial;
}

}

