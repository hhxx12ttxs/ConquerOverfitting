package ru.snilit;

public class CalcPi {

    static double pi;

    static void byLeibnic() {
        for (double i = 1; i < 100000000; i++) {
            if (i % 2 == 0) {
                pi -= 1 / (2 * i - 1);
            } else {
                pi += 1 / (2 * i - 1);
            }
        }
        pi *= 4;
        System.out.println("По алгоритму Лейбница: \t\t" + pi);
    }

    static void byVallis() {
        double pi_1 = 1, pi_2 = 1;
        for (double i = 2; i < 100000000; i += 2) {
            pi_1 *= i / (i + 1);
            pi_2 *= i / (i - 1);
        }
        pi = pi_1 * pi_2 * 2;
        System.out.println("Методом Валлиса: \t\t" + pi);
    }

    public static void main(String args[]) {
        byLeibnic();
        byVallis();
        System.out.println("Из библиотеки Java: \t\t" + Math.PI);
    }
}

