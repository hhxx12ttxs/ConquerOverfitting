package unam.ciencias.algs1.practica2;

/**
 * @author Ernesto Carrillo Espinosa.
 */
public class Factorial {

    /**
     * Numeros factoriales calculados de forma recursiva.
     * @param n n-esimo numero factorial.
     * @return el valor n!.
     */
    public static int factorialRec(int n) {
        if (n == 0) {
            return 1;
        } else {
            return n * factorialRec(n - 1);
        }
    }

    /**
     * Numeros factoriales calculados de forma iterativa.
     * @param n n-esimo numero factorial.
     * @return el valor n!.
     */
    public static int factorialIte(int n) {
        int r = 1;
        for (int i = 1; i <= n; i++) {
            r *= i;
        }
        return r;
    }

    /**
     * Numeros factoriales calculados con recursion de cola.
     * @param n n-esimo numero factorial.
     * @return el valor n!.
     */
    public static int factorialTail(int n) {
        return factorialTailAux(n, 1);
    }

    /**
     * Metodo auxiliar para realizar la recursion de cola de facotrialTail
     * @param n n-esimo numero factorial.
     * @param c acumulador
     * @return el valor n!.
     */
    private static int factorialTailAux(int n, int c) {
        if (n == 0) {
            return c;
        } else {
            return factorialTailAux(n - 1, c * n);
        }
    }

    /**
     * Metodo de pruebas.
     */
    public static void main(String[] args) {
        System.out.println(Factorial.factorialIte(10));
        System.out.println(Factorial.factorialRec(10));
        System.out.println(Factorial.factorialTail(10));
    }
}
