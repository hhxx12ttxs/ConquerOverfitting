/*
 * Factorial.java
 */

// Zadok - A multi-language test suite runner.
// Copyright (C) 2004  Simon P. Chappell
//
// Zadok is free software licenced under the GNU General Public Licence (GPL),
// version 2 or any later version. Read COPYING.txt for the full licence.

public class Factorial {
    public static void main(String[] argv) {
        System.out.print(factorial(1));
        for (int i=2; i <= 10; i++) {
            System.out.print(","+factorial(i));
        }
    }

    private static long factorial(int n) {
        if (n == 1) {
            return 1L;
        } else {
            return n * factorial(n-1);
        }
    }
}
