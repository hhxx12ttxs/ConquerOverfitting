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

    private double[][] tabla = readTableFile();

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
//        return findInTable(z);
        double per = JPMath.phi(z);
        return per;
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
                return 1-interpolateValue(-z);
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
        int column = (int) sp[0];
        int index = (int) StrictMath.floor(sp[1] * 100);
        return tabla[index][column];
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
        int index = (int) StrictMath.ceil(sp[1] * 100);
        if (index == 100) {
            column++;
            index = 0;
        }
        return tabla[index][column];
    }

    /**
     * Divide el número en su parte real y fraccionaria.
     * @param z
     * @return
     */
    private static double[] splitValue(double z) {
        double[] res = new double[2];
        res[0] = StrictMath.floor(z);
        res[1] = (z - StrictMath.floor(z));
        return res;
    }

    private double interpolateValue(double z) {
        double h = findInTableHighNearest(z);
        double l = findInTableLowNearest(z);
        double lz = StrictMath.floor(z * 100) / 100;
        double hz = StrictMath.ceil(z * 100) / 100;
        if (l != h) {
            return (l + (z - lz) * (h - l) / (hz - lz)); 
        } else {
            return l;
        }
    }

    /**
     * Lee la tabla de normales del archivo "normaltable".
     * @return
     */
    private static double[][] readTableFile() {
        /*TODO Controlar la salida, verificar que se lea correctamente el archivo,
        de no ser así enviar un mensaje de error */
        double[][] t = initializeTable();
            String s = TextScanner.retrieveText(new TextScanner.ResourceFile("tables/normaltable"));
            Scanner scan = new Scanner(s);
            scan.useLocale(Locale.US);
            try {
                scan.nextLine(); //Ignora la primer línea.
                for (int i = 0; i < t.length; i++) {
                    scan.nextDouble(); //Ignora el primer número
                    for (int j = 0; j < t[i].length; j++) {
                        if (scan.hasNextDouble()) {
                            t[i][j] = scan.nextDouble();
                        }
                    }
                }
            } catch (Exception ex){
                ex.printStackTrace();
            } finally {
                scan.close();
            }
        return t;
    }

    /**
     * Inicializa la tabla de 100x5 a ceros.
     * @return tabla de 100x5 de ceros
     */
    private static double[][] initializeTable() {
        double[][] table = new double[100][5];
        for (int i = 0; i < table.length; i++) {
            Arrays.fill(table[i], 0);
        }
        return table;
    }

    /**
     * Método main usado para testeo solamente.
     * @param args
     */
//    public static void main(String[] args) {
//        Test for reading table
//        TablaNormales tn = new TablaNormales();
//        double[][] tabla = tn.tabla;
//        for (int i = 0; i < tabla.length; i++) {
//            for (int j = 0; j < tabla[i].length; j++) {
//                double d = tabla[i][j];
//                System.out.print(d + "\t");
//            }
//            System.out.println();
//        }
//        double z = 1.53;
//        System.out.println(tn.getPz(z));
//    }
}

