package chapter7;

/**
 * Created by martynova on 2/22/15.
 */

class Factorial {
    int result;

    int fact (int n) {
        if (n == 1) {
            return 1;
        }
        result = fact(n - 1) * n;
        return result;
    }
}

public class Recursion {
    public static void main(String args[]) {
        Factorial factorial = new Factorial();

        System.out.println("1: " + factorial.fact(1));
        System.out.println("3: " + factorial.fact(3));
        System.out.println("4: " + factorial.fact(4));
        System.out.println("5: " + factorial.fact(5));
    }
}

