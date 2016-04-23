package net.mr_faton.Different_Things;

/**
 * Created by Mr_Faton on 14.01.2015.
 */
public class Factorial {
    public static void main(String[] args) {
        int x = 4;
        System.out.println("Факториал числа " + x + " = " + factorial(x));
    }

    public static int factorial(int x) {
        if (x == 1) {
            return 1;
        } else {
            return x * factorial(x - 1);
        }
    }
}

