package curso.ej04_01;



public class Ejercicio09 {

    public static void main(String[] args) {
        // TODO 04.01.09
        /*
         * Cree un programa que calcule el factorial de un número.
         */

        for (int i = 0; i < 20; i++) {
            System.out.println(i + "! = " + factorial_r(i));
        }
    }

    public static long factorial(int n) {
        long factorial = 1;
        for (; n > 1; n--) {
            factorial *= n;
        }

        return factorial;
    }

    public static long factorial_r(int n) {
        if (n == 0 || n == 1) {
            return 1;
        }

        return factorial_r(n - 1) * n;
    }
}

