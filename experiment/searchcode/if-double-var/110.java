package tp3sim.util;

import java.util.Random;

public abstract class Generator {

    private static int NUM1 = 43;
    private static int NUM2 = 37;
    private static int NUM3 = 100;
    public static final int MAX = 100;
//    private double vec[];

    public Generator() {
    }

    public static Object[] vectorRandomMixto(int semilla, int cant) {
        int j = semilla;
        Object nro[] = new Object[cant];
        for (int i = 0; i < cant; i++) {
            nro[i] = (NUM1 * j + NUM2) % NUM3;
            j = Integer.parseInt(nro[i].toString());
        }
        return nro;
    }

    public static Object[] vectorRandomMultiplicativo(int semilla, int cant) {
        int j = semilla;
        Object nro[] = new Object[cant];
        for (int i = 0; i < cant; i++) {
            nro[i] = (NUM1 * j) % NUM3;
            j = Integer.parseInt(nro[i].toString());
        }
        return nro;
    }

    public static Object[] vectorRandomJava(int cant) {
        Object[] nro = new Object[cant];
        Random rnd = new Random();
        for (int i = 0; i < cant; i++) {
            //nro[i] = rnd.nextInt(MAX);
            nro[i] = rnd.nextDouble();
        }
        return nro;
    }

    public static double randomJava() {
        Random rnd = new Random();
        double rand = rnd.nextDouble();
        return rand;

    }

    public static Double[] randomJavaArray(int cant) {
        Double[] array = new Double[cant];
        for (int i = 0; i < array.length; i++) {
            array[i] = randomJava();
        }
        return array;
    }

    public static double exponentialVariable(double u, double lambda) {
        double var = 0;
        var = (-1) * (1 / lambda) * Math.log(1 - u);
        return var;
    }

    public static Double[] exponentialVariableArray(double lambda, int cant) {
        Double[] array = new Double[cant];
        for (int i = 0; i < array.length; i++) {
            array[i] = exponentialVariable(randomJava(), lambda);
        }
        return array;
    }

    public static double poissonVariable(double u, double lambda) {
        double a = Math.exp(-lambda);
        double b = 1.0;
        int i = -1;

        do {
            b *= randomJava();
            i++;
        } while (b >= a);

        return i;
    }

    public static Double[] poissonVariableArray(double lambda, int cant) {
        Double[] array = new Double[cant];
        for (int i = 0; i < array.length; i++) {
            array[i] = poissonVariable(randomJava(), lambda);
        }
        return array;
    }

    public static double normalVariable(double u1, double u2, boolean sin, double des, double med) {

        double var;
        if (sin) {
            var = Math.sqrt((-2) * (Math.log(u1))) * Math.sin(2 * Math.PI * u2);
        } else {
            var = Math.sqrt((-2) * (Math.log(u1))) * Math.cos(2 * Math.PI * u2);
        }
        var = (var * des) + med;
        return var;
    }

    public static Double[] normalVariableArray(int cant, double des, double med) {
        Double[] array = new Double[cant];
        for (int i = 0; i < array.length; i++) {
            if (i < cant) {
                array[i] = normalVariable(randomJava(), randomJava(), true, des, med);
            } else {
                array[i] = normalVariable(randomJava(), randomJava(), false, des, med);
            }
        }
        return array;
    }
}



