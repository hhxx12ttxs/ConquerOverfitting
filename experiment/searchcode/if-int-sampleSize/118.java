/*
 * Copyright (c) 2007, Manuel D. Rossetti (rossetti@uark.edu)
 *
 * Contact:
 *	Manuel D. Rossetti, Ph.D., P.E.
 *	Department of Industrial Engineering
 *	University of Arkansas
 *	4207 Bell Engineering Center
 *	Fayetteville, AR 72701
 *	Phone: (479) 575-6756
 *	Email: rossetti@uark.edu
 *	Web: www.uark.edu/~rossetti
 *
 * This file is part of the JSL (a Java Simulation Library). The JSL is a framework
 * of Java classes that permit the easy development and execution of discrete event
 * simulation programs.
 *
 * The JSL is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The JSL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the JSL (see file COPYING in the distribution);
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA, or see www.fsf.org
 *
 */
package jsl.utilities.random.robj;

import java.util.ArrayList;
import jsl.utilities.random.rng.RNStreamFactory;

import jsl.utilities.random.rng.RngIfc;

/**
 * @author rossetti
 *
 */
public class Permutation {

    /** Randomly permutes the supplied array using the default random
     *  number generator.  The array is changed
     *
     * @param x
     */
    public final static void permutation(double[] x) {
        permutation(x, RNStreamFactory.getDefaultStream());
    }

    /** Randomly permutes the supplied array using the suppled random
     *  number generator, the array is changed
     *
     * @param x
     * @param rng
     */
    public final static void permutation(double[] x, RngIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *  using the default random number generator
     *
     * @param x
     * @param sampleSize
     */
    public final static void sampleWithoutReplacement(double[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, RNStreamFactory.getDefaultStream());
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x
     * @param sampleSize
     * @param rng
     */
    public final static void sampleWithoutReplacement(double[] x, int sampleSize, RngIfc rng) {
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (rng == null) {
            throw new IllegalArgumentException("The supplied random number generator was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }

        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            double temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /** Randomly permutes the supplied array using the default random
     *  number generator.  The array is changed
     *
     * @param x
     */
    public final static void permutation(int[] x) {
        permutation(x, RNStreamFactory.getDefaultStream());
    }

    /** Randomly permutes the supplied array using the suppled random
     *  number generator, the array is changed
     *
     * @param x
     * @param rng
     */
    public final static void permutation(int[] x, RngIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *  using ehe default random number generator
     *
     * @param x
     * @param sampleSize
     */
    public final static void sampleWithoutReplacement(int[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, RNStreamFactory.getDefaultStream());
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x
     * @param sampleSize
     * @param rng
     */
    public final static void sampleWithoutReplacement(int[] x, int sampleSize, RngIfc rng) {
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (rng == null) {
            throw new IllegalArgumentException("The supplied random number generator was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }

        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            int temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /** Randomly permutes the supplied array using the default random
     *  number generator.  The array is changed
     *
     * @param x
     */
    public final static void permutation(boolean[] x) {
        permutation(x, RNStreamFactory.getDefaultStream());
    }

    /** Randomly permutes the supplied array using the suppled random
     *  number generator, the array is changed
     *
     * @param x
     * @param rng
     */
    public final static void permutation(boolean[] x, RngIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *  using ehe default random number generator
     *
     * @param x
     * @param sampleSize
     */
    public final static void sampleWithoutReplacement(boolean[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, RNStreamFactory.getDefaultStream());
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x
     * @param sampleSize
     * @param rng
     */
    public final static void sampleWithoutReplacement(boolean[] x, int sampleSize, RngIfc rng) {
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (rng == null) {
            throw new IllegalArgumentException("The supplied random number generator was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }

        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            boolean temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /** Randomly permutes the supplied array using the default random
     *  number generator.  The array is changed
     *
     * @param x
     */
    public final static <T> void permutation(T[] x) {
        permutation(x, RNStreamFactory.getDefaultStream());
    }

    /** Randomly permutes the supplied array using the suppled random
     *  number generator, the array is changed
     *
     * @param x
     * @param rng
     */
    public final static <T> void permutation(T[] x, RngIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *  using ehe default random number generator
     *
     * @param x
     * @param sampleSize
     */
    public final static <T> void sampleWithoutReplacement(T[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, RNStreamFactory.getDefaultStream());
    }

    /** The array x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x
     * @param sampleSize
     * @param rng
     */
    public final static <T> void sampleWithoutReplacement(T[] x, int sampleSize, RngIfc rng) {
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (rng == null) {
            throw new IllegalArgumentException("The supplied random number generator was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }

        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            T temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /** Randomly permutes the supplied ArrayList using the suppled random
     *  number generator, the list is changed
     *
     * @param x
     */
    public final static <T> void permutation(ArrayList<T> x) {
        permutation(x, RNStreamFactory.getDefaultStream());
    }

    /** Randomly permutes the supplied ArrayList using the suppled random
     *  number generator, the list is changed
     *
     * @param x
     * @param rng
     */
    public final static <T> void permutation(ArrayList<T> x, RngIfc rng) {
        sampleWithoutReplacement(x, x.size(), rng);
    }

    /** The ArrayList x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x.get(0), x.get(1), ... , x.get(sampleSize-1) is the random sample without replacement
     *  using the default random number generator
     *
     * @param x
     * @param sampleSize
     */
    public final static <T> void sampleWithoutReplacement(ArrayList<T> x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, RNStreamFactory.getDefaultStream());
    }

    /** The ArrayList x is changed, such that the first sampleSize elements contain the sample.
     *  That is, x.get(0), x.get(1), ... , x.get(sampleSize-1) is the random sample without replacement
     *
     * @param x
     * @param sampleSize
     * @param rng
     */
    public final static <T> void sampleWithoutReplacement(ArrayList<T> x, int sampleSize, RngIfc rng) {
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (rng == null) {
            throw new IllegalArgumentException("The supplied random number generator was null");
        }
        int n = x.size();
        if (sampleSize > n) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }

        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, n - 1);
            T temp = x.get(j);
            x.set(j, x.get(i));
            x.set(i, temp);
        }
    }
}

