package org.zykoq.factorial;

/**
 * @author <a href="mailto:mr@mobi4friends.de">Maarten Roowaan</a>
 */
public class Factorial {

    public static long calculate(int factorial) {
        if (factorial < 2) {
            return 1;
        }
        return factorial * calculate(factorial - 1);
    }
}


