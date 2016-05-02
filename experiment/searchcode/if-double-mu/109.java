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

import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author Joaquín Ignacio Aramendía <samsagax@gmail.com>
 */
public class TablaNormales {

    /**
     * Devuelve el valor de probabilidad para un valor de x dado. X posee una
     * distribución {normal} con parámetros mu (media) y sigma (desviación
     * estándar).
     * @param x
     * @param mu
     * @param sigma
     * @return
     */
    public static double getPx(double x, double mu, double sigma) {
        double z = getStandardZ(x, mu, sigma);
        return getPz(z);
    }

    /**
     * Devuelve el valor de probabilidad para un valor de z determinado según la
     * tabla de distribución normal estandar.
     * @param z
     * @return
     */
    public static double getPz(double z) {
        double per = JPMath.phi(z);
        return per;
    }

    /**
     * Devuelve el valor de <code>z = (x - mu) / sigma<code>, donde x es una
     * variable aleatoria con distribución normal de media <code>mu<code> y
     * varianza <code>sigma<code>. La variable <code>z<code> resultante es uan
     * variable con distribución normal estándar.
     * @param x
     * @param mu
     * @param sigma
     * @return (x - mu) / sigma
     */
    public static double getStandardZ(double x, double mu, double sigma) {
        return (x - mu) / sigma;
    }

}

