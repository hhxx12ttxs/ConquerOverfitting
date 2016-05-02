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

import java.io.IOException;

/**
 *
 * @author Joaquín Ignacio Aramendía <samsagax@gmail.com>
 */
public class TablaNormales extends Table {

    public TablaNormales() throws IOException {
        super(new TextFileReaderME.ResourceFile("tables/normaltable"));
    }

    /**
     * Devuelve el valor de probabilidad para un valor de x dado. X posee una
     * distribución {normal} con parámetros mu (media) y sigma (desviación
     * estándar).
     * @param x
     * @param mu
     * @param sigma
     * @return
     */
    public double getPx(double x, double mu, double sigma) {
        double z = getStandardZ(x, mu, sigma);
        return getPz(z);
    }

    /**
     * Devuelve el valor de probabilidad para un valor de z determinado según la
     * tabla de distribución normal estandar.
     * @param z
     * @return
     */
    public double getPz(double z) {
//        return findInTable(z);
        return JPMath.phi(z);
    }

    public static double getStandardZ(double x, double mu, double sigma) {
        return (x - mu) / sigma;
    }

    /**
     * Busca el valor de probabilidad en la tabla para un valor de z dado.
     * Interpola de ser necesario.
     * @param z
     * @return
     */
    private double findInTable(double z) {
        if (z >= 0) {
            if (z <= 4.99) {
                return interpolateValue(z);
            } else {
                return 1;
            }
        } else {
            if (z >= -4.99) {
                return 1 - interpolateValue(-z);
            } else {
                return 0;
            }
        }
    }

    /**
     * Busca el valor de probabilidad más cercano en la tabla de normales
     * por debajo del valor de 'z' ingresado.
     * @param z
     * @return
     */
    private double findInTableLowNearest(double z) {
        double[] sp = splitValue(z);
        int column = (int) sp[0] + 1;
        int row = (int) Math.floor(sp[1] * 100);
        return getElementAt(row, column);
    }

    /**
     * Busca el valor de probabilidad más cercano en la tabla de normales
     * por encima del valor de 'z' ingresado.
     * @param z
     * @return
     */
    private double findInTableHighNearest(double z) {
        double[] sp = splitValue(z);
        int column = (int) sp[0];
        int index = (int) Math.ceil(sp[1] * 100);
        if (index == 100) {
            column++;
            index = 0;
        }
        return getElementAt(index, column);
    }

    /**
     * Divide el número en su parte real y fraccionaria.
     * @param z
     * @return
     */
    private static double[] splitValue(double z) {
        double[] res = new double[2]; //TODO Dividir el número
        res[0] = Math.floor(z);
        res[1] = (z - Math.floor(z));
        return res;
    }

    private double interpolateValue(double z) {
        double h = findInTableHighNearest(z);
        double l = findInTableLowNearest(z);
        double lz = Math.floor(z * 100) / 100;
        double hz = Math.ceil(z * 100) / 100;
        if (l != h) {
            return (l + (z - lz) * (h - l) / (hz - lz)); //TODO Buscar valores de z correspondientes a la tabla
        } else {
            return l;
        }
    }
}

