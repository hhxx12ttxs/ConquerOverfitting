/*
 * This file is part of seadams Utils.
 *
 * Copyright (c) 2008-2011 Sam Adams <seadams@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.seadams.util.maths;


public class Factorial {

    private static int NCACHE = 21;

    private static double[] logFactorials = new double[NCACHE];
    private static long[] factorials = new long[NCACHE];
    static {
        factorials[0] = 1;
        for (int i = 1; i < NCACHE; i++) {
            factorials[i] = factorials[i-1] * i;
            logFactorials[i] = Math.log10(factorials[i]);
        }
    }

    public static double logFactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Expected n>0, found n="+n);
        }
        if (n < NCACHE) {
            return logFactorials[n];
        }
        double fact = logFactorials[NCACHE-1];
        for (int i = NCACHE; i <= n; i++) {
            fact += Math.log10(i);
        }
        return fact;
    }

    public long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Expected n>0, found n="+n);
        }
        if (n < NCACHE) {
            return factorials[n];
        }
        long fact = factorials[NCACHE-1];
        for (int i = NCACHE; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }

}

