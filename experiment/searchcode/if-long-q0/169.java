/*
 *  Copyright (C) 2011 Joaquín Ignacio Aramendía <samsagax@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpercentilos.res;

/**
 * Clase con métodos para cálculo de funciones complejas.
 *
 * @author Joaquín Ignacio Aramendía <samsagax@gmail.com>
 * @autor Zielinski Leonardo Daniel <leonardo_03z@hotmail.com>
 */
public final class JPMath {

    private static final double DEFAULT_PRECISION = 1e-8;

    private JPMath() {
    }

    /**
     * Devuelve la función error de un valor real <code>z<code>.
     * @param z
     * @return erf(z)
     */
    public static double erf(double z) {
        if (z == 0) {
            return 0;
        } else if (z < 0) {
            return -erf(-z);
        } else if (z >= 1) { // z >= 1 usando cálculo por fracción continua
            double a = 2.0 * z,
                    b = -2.0 / Math.PI * exp(-pow(z, 2)),
                    P,
                    Q,
                    conv = 1;
            double P1 = 1, // Q0 and P0
                    Q1 = 1,
                    P2 = 1,
                    Q2 = 0;
            int n = 1;
            final int itermax = 50;
            double err;
            do {
                P = a * P1 + b * P2;
                Q = a * Q1 + b * Q2;
                conv = P / Q;
                P2 = P1;
                Q2 = Q1;
                P1 = P;
                Q1 = Q;
                b = 2.0 * n;
                err = abs((conv - P2 / Q2) / conv);
            } while (n < itermax && err > DEFAULT_PRECISION);
            return conv;
        } else { // z < 1 usando cálculo por serie de taylor
            double err;
            double res;
            res = z;
            err = res;
            final double constante = (2.0 / Math.sqrt(Math.PI));
            int itermax = 50;
            int n = 1;
            do {
                err = constante * (z / (double) (2 * n + 1));
                double multiplicador = 1;
                for (int k = 1; k <= n; k++) {
                    multiplicador *= (- pow(z, 2) / (double) k);
                } // fin del for anidado
                err *= multiplicador;
                res += err;
                n++;
            } while (n < itermax && abs(err / res) > DEFAULT_PRECISION);
            return res;
        }
    }

    /**
     * Devuelve la función error complementaria de un valor real <code>z<code>.
     * @param z
     * @return erfc(z) = 1 - erf(z)
     */
    public static double erfc(double z) {
        double erfc = (1.0 - erf(z));
        return erfc;
    }

    /**
     * Devuelve el factorial de un entero <code>n<code> positivo.
     * @param n
     * @return n!
     */
    public static long fact(int n) {
        if (n <= 1 && n >= 0) {
            return 1;


        } else if (n > 1) {
            return n * fact(n - 1);


        } else {
            return 0;


        }
    }

    /**
     * Devuelve la función de distribución de probabilidad normal estandar de un
     * valor real <code>z<code>.
     * @param z
     * @return phi(z)
     */
    public static double phi(double z) {
        double phi = 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
        return phi;
    }

    /**
     * Devuelve el logaritmo natural de la variable <code>x<code>.
     * @param x
     * @return ln(x)
     */
    public static double logn(double x) {
        return MathME.log(x);
    }

    /**
     * Devuelve err^x para <code>x<code>.
     * @param x
     * @return err^x
     */
    public static double exp(double x) {
        return MathME.exp(x);


    }

    /**
     * Devuelve el primer argumento elevado al segundo argumento.
     * @param x
     * @param y
     * @return x^y
     */
    public static double pow(double x, double y) {
        return MathME.pow(x, y);
    }

    /**
     * Devuelve el valor absoluto del argunmento.
     * @param a
     * @return 'a' sin signo.
     */
    static double abs(double a) {
        return Math.abs(a);


    }

    static double signum(double a) {
        return MathME.signum(a);

    }
}

