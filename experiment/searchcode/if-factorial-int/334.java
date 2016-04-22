package com.sandbox.recursion;

public class Factorial {
  public static void main(String... args) {
    System.out.println("Factorial 6! = " + factorial(6));
    System.out.println("Factorial 6! = " + factorialIter(6));
  }

  private static int factorial(int i) {
    if (i <= 1) {
      return 1;
    }

    return i * factorial(i - 1);
  }

  private static int factorialIter(int i) {
    int fact = 1;
    for (int j = i; j > 1; j--) {
      fact *= j;
    }

    return fact;
  }
}

