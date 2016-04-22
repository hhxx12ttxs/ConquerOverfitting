package trickyexamples;

public class Factorial {

  public static void main(String[] args) {
    System.out.println(factorialRec(3));
    System.out.println(factorialIter(3));
    System.out.println(factorialTail(1, 3));
  }

  static int factorialIter(int n) {
    int fact = 1, i = n;

    while (i > 0) {
      fact = fact * i;
      i--;
    }
    return fact;
  }

  static int factorialRec(int n) {
    if (0 == n) {
      return 1;
    } else {
      return n * factorialRec(n - 1);
    }
  }

  static int factorialTail(int acc, int n) {
    if (0 == n) {
      return acc;
    } else {
      return factorialTail(n * acc, n - 1);
    }
  }
}

