<<<<<<< HEAD
// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

/**
 *
 * @author sk89q
 */
public class Vector2D {
    protected final double x, z;

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(int x, int z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(float x, float z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param pt
     */
    public Vector2D(Vector2D pt) {
        this.x = pt.x;
        this.z = pt.z;
    }

    /**
     * Construct the Vector2D object.
     */
    public Vector2D() {
        this.x = 0;
        this.z = 0;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the x
     */
    public int getBlockX() {
        return (int) Math.round(x);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(double x) {
        return new Vector2D(x, z);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(int x) {
        return new Vector2D(x, z);
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @return the z
     */
    public int getBlockZ() {
        return (int) Math.round(z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(double z) {
        return new Vector2D(x, z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(int z) {
        return new Vector2D(x, z);
    }

    /**
     * Adds two points.
     *
     * @param other
     * @return New point
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, z + other.z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D add(double x, double z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D add(int x, int z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Adds points.
     *
     * @param others
     * @return New point
     */
    public Vector2D add(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX += others[i].x;
            newZ += others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Subtracts two points.
     *
     * @param other
     * @return New point
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, z - other.z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D subtract(double x, double z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D subtract(int x, int z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract points.
     *
     * @param others
     * @return New point
     */
    public Vector2D subtract(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX -= others[i].x;
            newZ -= others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Component-wise multiplication
     *
     * @param other
     * @return New point
     */
    public Vector2D multiply(Vector2D other) {
        return new Vector2D(x * other.x, z * other.z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D multiply(double x, double z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D multiply(int x, int z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param others
     * @return New point
     */
    public Vector2D multiply(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX *= others[i].x;
            newZ *= others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(double n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(float n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(int n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Component-wise division
     *
     * @param other
     * @return New point
     */
    public Vector2D divide(Vector2D other) {
        return new Vector2D(x / other.x, z / other.z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D divide(double x, double z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D divide(int x, int z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(int n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(double n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(float n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Get the length of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(x * x + z * z);
    }

    /**
     * Get the length^2 of the vector.
     *
     * @return length^2
     */
    public double lengthSq() {
        return x * x + z * z;
    }

    /**
     * Get the distance away from a point.
     *
     * @param pt
     * @return distance
     */
    public double distance(Vector2D pt) {
        return Math.sqrt(Math.pow(pt.x - x, 2) +
                Math.pow(pt.z - z, 2));
    }

    /**
     * Get the distance away from a point, squared.
     *
     * @param pt
     * @return distance
     */
    public double distanceSq(Vector2D pt) {
        return Math.pow(pt.x - x, 2) +
                Math.pow(pt.z - z, 2);
    }

    /**
     * Get the normalized vector.
     *
     * @return vector
     */
    public Vector2D normalize() {
        return divide(length());
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other
     * @return the dot product of this and the other vector
     */
    public double dot(Vector2D other) {
        return x * other.x + z * other.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithin(Vector2D min, Vector2D max) {
        return x >= min.x && x <= max.x
                && z >= min.z && z <= max.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithinBlock(Vector2D min, Vector2D max) {
        return getBlockX() >= min.getBlockX() && getBlockX() <= max.getBlockX()
                && getBlockZ() >= min.getBlockZ() && getBlockZ() <= max.getBlockZ();
    }

    /**
     * Rounds all components down.
     *
     * @return
     */
    public Vector2D floor() {
        return new Vector2D(Math.floor(x), Math.floor(z));
    }

    /**
     * Rounds all components up.
     *
     * @return
     */
    public Vector2D ceil() {
        return new Vector2D(Math.ceil(x), Math.ceil(z));
    }

    /**
     * Rounds all components to the closest integer.<br>
     *<br>
     * Components < 0.5 are rounded down, otherwise up
     *
     * @return
     */
    public Vector2D round() {
        return new Vector2D(Math.floor(x + 0.5), Math.floor(z + 0.5));
    }

    /**
     * 2D transformation.
     *
     * @param angle in degrees
     * @param aboutX about which x coordinate to rotate
     * @param aboutZ about which z coordinate to rotate
     * @param translateX what to add after rotation
     * @param translateZ what to add after rotation
     * @return
     */
    public Vector2D transform2D(double angle,
            double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector2D(
            x2 + aboutX + translateX,
            z2 + aboutZ + translateZ
        );
    }

    public boolean isCollinearWith(Vector2D other) {
        if (x == 0 && z == 0) {
            // this is a zero vector
            return true;
        }

        final double otherX = other.x;
        final double otherZ = other.z;

        if (otherX == 0 && otherZ == 0) {
            // other is a zero vector
            return true;
        }

        if ((x == 0) != (otherX == 0)) return false;
        if ((z == 0) != (otherZ == 0)) return false;

        final double quotientX = otherX / x;
        if (!Double.isNaN(quotientX)) {
            return other.equals(multiply(quotientX));
        }

        final double quotientZ = otherZ / z;
        if (!Double.isNaN(quotientZ)) {
            return other.equals(multiply(quotientZ));
        }

        throw new RuntimeException("This should not happen");
    }

    /**
     * Gets a BlockVector version.
     *
     * @return BlockVector
     */
    public BlockVector2D toBlockVector2D() {
        return new BlockVector2D(this);
    }

    /**
     * Checks if another object is equivalent.
     *
     * @param obj
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        }

        Vector2D other = (Vector2D) obj;
        return other.x == this.x && other.z == this.z;

    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ((new Double(x)).hashCode() >> 13) ^
                (new Double(z)).hashCode();
    }

    /**
     * Returns string representation "(x, y, z)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

    /**
     * Creates a 3D vector by adding a zero Y component to this vector.
     *
     * @return Vector
     */
    public Vector toVector() {
        return new Vector(x, 0, z);
    }

    /**
     * Creates a 3D vector by adding the specified Y component to this vector.
     *
     * @return Vector
     */
    public Vector toVector(double y) {
        return new Vector(x, y, z);
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return minimum
     */
    public static Vector2D getMinimum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.min(v1.x, v2.x),
            Math.min(v1.z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return maximum
     */
    public static Vector2D getMaximum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.max(v1.x, v2.x),
            Math.max(v1.z, v2.z)
        );
=======
package ec.vector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Code;
import ec.util.DecodeReturn;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

/*
 * DoubleVectorIndividual.java
 * Created: Thu Mar 22 13:13:20 EST 2001
 */

/**
 * DoubleVectorIndividual is a VectorIndividual whose genome is an array of
 * doubles. Gene values may range from species.mingene(x) to species.maxgene(x),
 * inclusive. The default mutation method randomizes genes to new values in this
 * range, with <tt>species.mutationProbability</tt>. It can also add gaussian
 * noise to the genes, if so directed in the FloatVectorSpecies. If the gaussian
 * noise pushes the gene out of range, a new noise value is generated.
 * 
 * <p>
 * <P>
 * <b>From ec.Individual:</b>
 * 
 * <p>
 * In addition to serialization for checkpointing, Individuals may read and
 * write themselves to streams in three ways.
 * 
 * <ul>
 * <li>
 * <b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;
 * &nbsp;&nbsp;This method transmits or receives an individual in binary. It is
 * the most efficient approach to sending individuals over networks, etc. These
 * methods write the evaluated flag and the fitness, then call
 * <b>readGenotype/writeGenotype</b>, which you must implement to write those
 * parts of your Individual special to your functions-- the default versions of
 * readGenotype/writeGenotype throw errors. You don't need to implement them if
 * you don't plan on using read/writeIndividual.
 * 
 * <li>
 * <b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</
 * b>&nbsp;&nbsp;&nbsp;This approach transmits or receives an indivdual in text
 * encoded such that the individual is largely readable by humans but can be
 * read back in 100% by ECJ as well. To do this, these methods will encode
 * numbers using the <tt>ec.util.Code</tt> class. These methods are mostly used
 * to write out populations to files for inspection, slight modification, then
 * reading back in later on. <b>readIndividual</b> reads in the fitness and the
 * evaluation flag, then calls <b>parseGenotype</b> to read in the remaining
 * individual. You are responsible for implementing parseGenotype: the Code
 * class is there to help you. <b>printIndividual</b> writes out the fitness and
 * evaluation flag, then calls <b>genotypeToString</b> and printlns the
 * resultant string. You are responsible for implementing the genotypeToString
 * method in such a way that parseGenotype can read back in the individual
 * println'd with genotypeToString. The default form of genotypeToString simply
 * calls <b>toString</b>, which you may override instead if you like. The
 * default form of <b>parseGenotype</b> throws an error. You are not required to
 * implement these methods, but without them you will not be able to write
 * individuals to files in a simultaneously computer- and human-readable
 * fashion.
 * 
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption
 * only. <b>printIndividualForHumans</b> writes out the fitness and evaluation
 * flag, then calls <b>genotypeToStringForHumans</b> and printlns the resultant
 * string. You are responsible for implementing the genotypeToStringForHumans
 * method. The default form of genotypeToStringForHumans simply calls
 * <b>toString</b>, which you may override instead if you like (though note that
 * genotypeToString's default also calls toString). You should handle one of
 * these methods properly to ensure individuals can be printed by ECJ.
 * </ul>
 * 
 * <p>
 * In general, the various readers and writers do three things: they tell the
 * Fitness to read/write itself, they read/write the evaluated flag, and they
 * read/write the gene array. If you add instance variables to a
 * VectorIndividual or subclass, you'll need to read/write those variables as
 * well. <b>Default Base</b><br>
 * vector.double-vect-ind
 * 
 * @author Liviu Panait
 * @author Sean Luke and Liviu Panait
 * @version 2.0
 */

public class DoubleVectorIndividual extends VectorIndividual {
    public static final String P_DOUBLEVECTORINDIVIDUAL = "double-vect-ind";

    public double[] genome;

    public Parameter defaultBase() {
        return VectorDefaults.base().push(P_DOUBLEVECTORINDIVIDUAL);
    }

    public Object clone() {
        DoubleVectorIndividual myobj = (DoubleVectorIndividual) (super.clone());

        // must clone the genome
        myobj.genome = (double[]) (genome.clone());

        return myobj;
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base); // actually unnecessary (Individual.setup()
        // is empty)

        // since VectorSpecies set its constraint values BEFORE it called
        // super.setup(...) [which in turn called our setup(...)], we know that
        // stuff like genomeSize has already been set...

        Parameter def = defaultBase();

        if (!(species instanceof FloatVectorSpecies))
            state.output.fatal("DoubleVectorIndividual requires a FloatVectorSpecies", base, def);
        FloatVectorSpecies s = (FloatVectorSpecies) species;

        genome = new double[s.genomeSize];
    }

    public void defaultCrossover(EvolutionState state, int thread, VectorIndividual ind) {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        DoubleVectorIndividual i = (DoubleVectorIndividual) ind;
        double tmp;
        int point;

        if (genome.length != i.genome.length)
            state.output.fatal("Genome lengths are not the same for fixed-length vector crossover");
        switch (s.crossoverType) {
        case VectorSpecies.C_ONE_POINT:
            point = state.random[thread].nextInt((genome.length / s.chunksize) + 1);
            for (int x = 0; x < point * s.chunksize; x++) {
                tmp = i.genome[x];
                i.genome[x] = genome[x];
                genome[x] = tmp;
            }
            break;
        case VectorSpecies.C_TWO_POINT:
            int point0 = state.random[thread].nextInt((genome.length / s.chunksize) + 1);
            point = state.random[thread].nextInt((genome.length / s.chunksize) + 1);
            if (point0 > point) {
                int p = point0;
                point0 = point;
                point = p;
            }
            for (int x = point0 * s.chunksize; x < point * s.chunksize; x++) {
                tmp = i.genome[x];
                i.genome[x] = genome[x];
                genome[x] = tmp;
            }
            break;
        case VectorSpecies.C_ANY_POINT:
            for (int x = 0; x < genome.length / s.chunksize; x++)
                if (state.random[thread].nextBoolean(s.crossoverProbability))
                    for (int y = x * s.chunksize; y < (x + 1) * s.chunksize; y++) {
                        tmp = i.genome[y];
                        i.genome[y] = genome[y];
                        genome[y] = tmp;
                    }
            break;
        case VectorSpecies.C_LINE_RECOMB: {
            double alpha = state.random[thread].nextDouble() * (1 + 2 * s.lineDistance) - s.lineDistance;
            double beta = state.random[thread].nextDouble() * (1 + 2 * s.lineDistance) - s.lineDistance;
            double t, u, min, max;
            for (int x = 0; x < genome.length; x++) {
                min = s.minGene(x);
                max = s.maxGene(x);
                t = alpha * genome[x] + (1 - alpha) * i.genome[x];
                u = beta * i.genome[x] + (1 - beta) * genome[x];
                if (!(t < min || t > max || u < min || u > max)) {
                    genome[x] = t;
                    i.genome[x] = u;
                }
            }
        }
            break;
        case VectorSpecies.C_INTERMED_RECOMB: {
            double t, u, min, max;
            for (int x = 0; x < genome.length; x++) {
                do {
                    double alpha = state.random[thread].nextDouble() * (1 + 2 * s.lineDistance) - s.lineDistance;
                    double beta = state.random[thread].nextDouble() * (1 + 2 * s.lineDistance) - s.lineDistance;
                    min = s.minGene(x);
                    max = s.maxGene(x);
                    t = alpha * genome[x] + (1 - alpha) * i.genome[x];
                    u = beta * i.genome[x] + (1 - beta) * genome[x];
                } while (t < min || t > max || u < min || u > max);
                genome[x] = t;
                i.genome[x] = u;
            }
        }
        case VectorSpecies.C_SIMULATED_BINARY: {
            simulatedBinaryCrossover(state.random[thread], i, s.crossoverDistributionIndex);
        }
            break;
        }
    }

    /**
     * Splits the genome into n pieces, according to points, which *must* be
     * sorted. pieces.length must be 1 + points.length
     */
    public void split(int[] points, Object[] pieces) {
        int point0, point1;
        point0 = 0;
        point1 = points[0];
        for (int x = 0; x < pieces.length; x++) {
            pieces[x] = new double[point1 - point0];
            System.arraycopy(genome, point0, pieces[x], 0, point1 - point0);
            point0 = point1;
            if (x >= pieces.length - 2)
                point1 = genome.length;
            else
                point1 = points[x + 1];
        }
    }

    /** Joins the n pieces and sets the genome to their concatenation. */
    public void join(Object[] pieces) {
        int sum = 0;
        for (int x = 0; x < pieces.length; x++)
            sum += ((double[]) (pieces[x])).length;

        int runningsum = 0;
        double[] newgenome = new double[sum];
        for (int x = 0; x < pieces.length; x++) {
            System.arraycopy(pieces[x], 0, newgenome, runningsum, ((double[]) (pieces[x])).length);
            runningsum += ((double[]) (pieces[x])).length;
        }
        // set genome
        genome = newgenome;
    }

    /**
     * Destructively mutates the individual in some default manner. The default
     * form simply randomizes genes to a uniform distribution from the min and
     * max of the gene values. It can also add gaussian noise to the genes, if
     * so directed in the FloatVectorSpecies. If the gaussian noise pushes the
     * gene out of range, a new noise value is generated.
     * 
     * @author Sean Luke, Liviu Panait and Gabriel Balan
     */
    public void defaultMutate(EvolutionState state, int thread) {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        if (!(s.mutationProbability > 0.0))
            return;
        boolean mutationIsBounded = s.mutationIsBounded;
        MersenneTwisterFast rng = state.random[thread];
        if (s.mutationType == FloatVectorSpecies.C_GAUSS_MUTATION) {
            for (int x = 0; x < genome.length; x++)
                if (rng.nextBoolean(s.mutationProbability)) {
                    double val;
                    double min = s.minGene(x);
                    double max = s.maxGene(x);
                    double stdev = s.gaussMutationStdev;
                    int outOfBoundsLeftOverTries = s.outOfBoundsRetries;
                    boolean givingUpAllowed = s.outOfBoundsRetries != 0;
                    do {
                        val = rng.nextGaussian() * stdev + genome[x];
                        outOfBoundsLeftOverTries--;
                        if (mutationIsBounded && (val > max || val < min)) {
                            if (givingUpAllowed && (outOfBoundsLeftOverTries == 0)) {
                                val = min + rng.nextFloat() * (max - min);
                                s.outOfRangeRetryLimitReached(state);// it
                                                                     // better
                                                                     // get
                                                                     // inlined
                                break;
                            }
                        } else
                            break;
                    } while (true);
                    genome[x] = val;
                }
        } else if (s.mutationType == FloatVectorSpecies.C_POLYNOMIAL_MUTATION) {
            polynomialMutate(state.random[thread], s.crossoverDistributionIndex, s.polynomialIsAlternative,
                    s.mutationIsBounded);
        } else {// C_RESET_MUTATION
            for (int x = 0; x < genome.length; x++)
                if (rng.nextBoolean(s.mutationProbability))
                    genome[x] = s.minGene(x) + rng.nextDouble() * (s.maxGene(x) - s.minGene(x));
        }
    }

    /**
     * This function is broken out to keep it identical to NSGA-II's mutation.c
     * code. eta_m is the distribution index.
     */
    public void polynomialMutate(MersenneTwisterFast random, double eta_m, boolean alternativePolynomialVersion,
            boolean mutationIsBounded) {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        double[] ind = genome;
        double[] min_realvar = s.minGenes;
        double[] max_realvar = s.maxGenes;

        double rnd, delta1, delta2, mut_pow, deltaq;
        double y, yl, yu, val, xy;
        double y1;
        for (int j = 0; j < ind.length; j++) {
            if (random.nextBoolean(s.mutationProbability)) {
                y1 = y = ind[j];
                yl = min_realvar[j];
                yu = max_realvar[j];
                delta1 = (y - yl) / (yu - yl);
                delta2 = (yu - y) / (yu - yl);

                int totalTries = s.outOfBoundsRetries;
                int tries = 0;
                for (tries = 0; tries < totalTries || totalTries == 0; tries++) // keep
                                                                                // trying
                                                                                // until
                                                                                // totalTries
                                                                                // is
                                                                                // reached
                                                                                // if
                                                                                // it's
                                                                                // not
                                                                                // zero.
                                                                                // If
                                                                                // it's
                                                                                // zero,
                                                                                // go
                                                                                // on
                                                                                // forever.
                {
                    rnd = (random.nextDouble());
                    mut_pow = 1.0 / (eta_m + 1.0);
                    if (rnd <= 0.5) {
                        xy = 1.0 - delta1;
                        val = 2.0
                                * rnd
                                + (alternativePolynomialVersion ? (1.0 - 2.0 * rnd) * (Math.pow(xy, (eta_m + 1.0)))
                                        : 0.0);
                        deltaq = Math.pow(val, mut_pow) - 1.0;
                    } else {
                        xy = 1.0 - delta2;
                        val = 2.0
                                * (1.0 - rnd)
                                + (alternativePolynomialVersion ? 2.0 * (rnd - 0.5) * (Math.pow(xy, (eta_m + 1.0)))
                                        : 0.0);
                        deltaq = 1.0 - (Math.pow(val, mut_pow));
                    }
                    y1 = y + deltaq * (yu - yl);
                    if (mutationIsBounded && (y1 >= yl && y1 <= yu))
                        break; // yay, found one
                }

                // at this point, if tries is totalTries, we failed
                if (totalTries != 0 && tries == totalTries) {
                    // just randomize
                    y1 = (double) (min_realvar[j] + random.nextDouble() * (max_realvar[j] - min_realvar[j]));
                }
                ind[j] = y1;
            }
        }
    }

    public void simulatedBinaryCrossover(MersenneTwisterFast random, DoubleVectorIndividual other, double eta_c) {
        final double EPS = FloatVectorSpecies.SIMULATED_BINARY_CROSSOVER_EPS;
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        double[] parent1 = genome;
        double[] parent2 = other.genome;
        double[] min_realvar = s.minGenes;
        double[] max_realvar = s.maxGenes;

        double y1, y2, yl, yu;
        double c1, c2;
        double alpha, beta, betaq;
        double rand;

        for (int i = 0; i < parent1.length; i++) {
            if (random.nextBoolean()) // 0.5f
            {
                if (Math.abs(parent1[i] - parent2[i]) > EPS) {
                    if (parent1[i] < parent2[i]) {
                        y1 = parent1[i];
                        y2 = parent2[i];
                    } else {
                        y1 = parent2[i];
                        y2 = parent1[i];
                    }
                    yl = min_realvar[i];
                    yu = max_realvar[i];
                    rand = random.nextDouble();
                    beta = 1.0 + (2.0 * (y1 - yl) / (y2 - y1));
                    alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));
                    if (rand <= (1.0 / alpha)) {
                        betaq = Math.pow((rand * alpha), (1.0 / (eta_c + 1.0)));
                    } else {
                        betaq = Math.pow((1.0 / (2.0 - rand * alpha)), (1.0 / (eta_c + 1.0)));
                    }
                    c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
                    beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
                    alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));
                    if (rand <= (1.0 / alpha)) {
                        betaq = Math.pow((rand * alpha), (1.0 / (eta_c + 1.0)));
                    } else {
                        betaq = Math.pow((1.0 / (2.0 - rand * alpha)), (1.0 / (eta_c + 1.0)));
                    }
                    c2 = 0.5 * ((y1 + y2) + betaq * (y2 - y1));
                    if (c1 < yl)
                        c1 = yl;
                    if (c2 < yl)
                        c2 = yl;
                    if (c1 > yu)
                        c1 = yu;
                    if (c2 > yu)
                        c2 = yu;
                    if (random.nextBoolean()) {
                        parent1[i] = c2;
                        parent2[i] = c1;
                    } else {
                        parent1[i] = c1;
                        parent2[i] = c2;
                    }
                } else {
                    // do nothing
                }
            } else {
                // do nothing
            }
        }
    }

    /**
     * Initializes the individual by randomly choosing doubles uniformly from
     * mingene to maxgene.
     */
    public void reset(EvolutionState state, int thread) {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        for (int x = 0; x < genome.length; x++)
            genome[x] = (s.minGene(x) + state.random[thread].nextDouble() * (s.maxGene(x) - s.minGene(x)));
    }

    public int hashCode() {
        // stolen from GPIndividual. It's a decent algorithm.
        int hash = this.getClass().hashCode();

        hash = (hash << 1 | hash >>> 31);
        for (int x = 0; x < genome.length; x++) {
            long l = Double.doubleToLongBits(genome[x]);
            hash = (hash << 1 | hash >>> 31) ^ (int) ((l >>> 16) & 0xFFFFFFF) ^ (int) (l & 0xFFFF);
        }

        return hash;
    }

    public String genotypeToStringForHumans() {
        String s = "";
        for (int i = 0; i < genome.length; i++)
            s = s + " " + genome[i];
        return s;
    }

    public String genotypeToString() {
        StringBuffer s = new StringBuffer();
        s.append(Code.encode(genome.length));
        for (int i = 0; i < genome.length; i++)
            s.append(Code.encode(genome[i]));
        return s.toString();
    }

    protected void parseGenotype(final EvolutionState state, final LineNumberReader reader) throws IOException {
        // read in the next line. The first item is the number of genes
        String s = reader.readLine();
        DecodeReturn d = new DecodeReturn(s);
        Code.decode(d);
        int lll = (int) (d.l);

        genome = new double[lll];

        // read in the genes
        for (int i = 0; i < genome.length; i++) {
            Code.decode(d);
            genome[i] = d.d;
        }
    }

    public boolean equals(Object ind) {
        if (!(this.getClass().equals(ind.getClass())))
            return false; // SimpleRuleIndividuals are special.
        DoubleVectorIndividual i = (DoubleVectorIndividual) ind;
        if (genome.length != i.genome.length)
            return false;
        for (int j = 0; j < genome.length; j++)
            if (genome[j] != i.genome[j])
                return false;
        return true;
    }

    public Object getGenome() {
        return genome;
    }

    public void setGenome(Object gen) {
        genome = (double[]) gen;
    }

    public int genomeLength() {
        return genome.length;
    }

    public void writeGenotype(final EvolutionState state, final DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(genome.length);
        for (int x = 0; x < genome.length; x++)
            dataOutput.writeDouble(genome[x]);
    }

    public void readGenotype(final EvolutionState state, final DataInput dataInput) throws IOException {
        int len = dataInput.readInt();
        if (genome == null || genome.length != len)
            genome = new double[len];

        for (int x = 0; x < genome.length; x++)
            genome[x] = dataInput.readDouble();
    }

    /**
     * Clips each gene value to be within its specified [min,max] range. NaN is
     * presently considered in range but the behavior of this method should be
     * assumed to be unspecified on encountering NaN.
     */
    public void clamp() {
        FloatVectorSpecies _species = (FloatVectorSpecies) species;
        for (int i = 0; i < genomeLength(); i++) {
            double minGene = _species.minGene(i);
            if (genome[i] < minGene)
                genome[i] = minGene;
            else {
                double maxGene = _species.maxGene(i);
                if (genome[i] > maxGene)
                    genome[i] = maxGene;
            }
        }
    }

    public void setGenomeLength(int len) {
        double[] newGenome = new double[len];
        System.arraycopy(genome, 0, newGenome, 0, genome.length < newGenome.length ? genome.length : newGenome.length);
        genome = newGenome;
    }

    /**
     * Returns true if each gene value is within is specified [min,max] range.
     * NaN is presently considered in range but the behavior of this method
     * should be assumed to be unspecified on encountering NaN.
     */
    public boolean isInRange() {
        FloatVectorSpecies _species = (FloatVectorSpecies) species;
        for (int i = 0; i < genomeLength(); i++)
            if (genome[i] < _species.minGene(i) || genome[i] > _species.maxGene(i))
                return false;
        return true;
    }

    public double distanceTo(Individual otherInd) {
        if (!(otherInd instanceof DoubleVectorIndividual))
            return super.distanceTo(otherInd); // will return infinity!

        DoubleVectorIndividual other = (DoubleVectorIndividual) otherInd;
        double[] otherGenome = other.genome;
        double sumSquaredDistance = 0.0;
        for (int i = 0; i < other.genomeLength(); i++) {
            double dist = this.genome[i] - otherGenome[i];
            sumSquaredDistance += dist * dist;
        }
        return StrictMath.sqrt(sumSquaredDistance);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

