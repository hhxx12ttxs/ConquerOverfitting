<<<<<<< HEAD
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
    }
=======

//
// (c) 2000 Sun Microsystems, Inc.
// ALL RIGHTS RESERVED
// 
// License Grant-
// 
// 
// Permission to use, copy, modify, and distribute this Software and its 
// documentation for NON-COMMERCIAL or COMMERCIAL purposes and without fee is 
// hereby granted.  
// 
// This Software is provided "AS IS".  All express warranties, including any 
// implied warranty of merchantability, satisfactory quality, fitness for a 
// particular purpose, or non-infringement, are disclaimed, except to the extent 
// that such disclaimers are held to be legally invalid.
// 
// You acknowledge that Software is not designed, licensed or intended for use in 
// the design, construction, operation or maintenance of any nuclear facility 
// ("High Risk Activities").  Sun disclaims any express or implied warranty of 
// fitness for such uses.  
//
// Please refer to the file http://www.sun.com/policies/trademarks/ for further 
// important trademark information and to 
// http://java.sun.com/nav/business/index.html for further important licensing 
// information for the Java Technology.
//
package jake2.util;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Locale;
import java.text.DecimalFormatSymbols;

/**
 * PrintfFormat allows the formatting of an array of
 * objects embedded within a string.  Primitive types
 * must be passed using wrapper types.  The formatting
 * is controlled by a control string.
 *<p>
 * A control string is a Java string that contains a
 * control specification.  The control specification
 * starts at the first percent sign (%) in the string,
 * provided that this percent sign
 *<ol>
 *<li>is not escaped protected by a matching % or is
 * not an escape % character,
 *<li>is not at the end of the format string, and
 *<li>precedes a sequence of characters that parses as
 * a valid control specification.
 *</ol>
 *</p><p>
 * A control specification usually takes the form:
 *<pre> % ['-+ #0]* [0..9]* { . [0..9]* }+
 *                { [hlL] }+ [idfgGoxXeEcs]
 *</pre>
 * There are variants of this basic form that are
 * discussed below.</p>
 *<p>
 * The format is composed of zero or more directives
 * defined as follows:
 *<ul>
 *<li>ordinary characters, which are simply copied to
 * the output stream;
 *<li>escape sequences, which represent non-graphic
 * characters; and
 *<li>conversion specifications,  each of which
 * results in the fetching of zero or more arguments.
 *</ul></p>
 *<p>
 * The results are undefined if there are insufficient
 * arguments for the format.  Usually an unchecked
 * exception will be thrown.  If the format is
 * exhausted while arguments remain, the excess
 * arguments are evaluated but are otherwise ignored.
 * In format strings containing the % form of
 * conversion specifications, each argument in the
 * argument list is used exactly once.</p>
 * <p>
 * Conversions can be applied to the <code>n</code>th
 * argument after the format in the argument list,
 * rather than to the next unused argument.  In this
 * case, the conversion characer % is replaced by the
 * sequence %<code>n</code>$, where <code>n</code> is
 * a decimal integer giving the position of the
 * argument in the argument list.</p>
 * <p>
 * In format strings containing the %<code>n</code>$
 * form of conversion specifications, each argument
 * in the argument list is used exactly once.</p>
 *
 *<h4>Escape Sequences</h4>
 *<p>
 * The following table lists escape sequences and
 * associated actions on display devices capable of
 * the action.
 *<table>
 *<tr><th align=left>Sequence</th>
 *    <th align=left>Name</th>
 *    <th align=left>Description</th></tr>
 *<tr><td>\\</td><td>backlash</td><td>None.
 *</td></tr>
 *<tr><td>\a</td><td>alert</td><td>Attempts to alert
 *          the user through audible or visible
 *          notification.
 *</td></tr>
 *<tr><td>\b</td><td>backspace</td><td>Moves the
 *          printing position to one column before
 *          the current position, unless the
 *          current position is the start of a line.
 *</td></tr>
 *<tr><td>\f</td><td>form-feed</td><td>Moves the
 *          printing position to the initial 
 *          printing position of the next logical
 *          page.
 *</td></tr>
 *<tr><td>\n</td><td>newline</td><td>Moves the
 *          printing position to the start of the
 *          next line.
 *</td></tr>
 *<tr><td>\r</td><td>carriage-return</td><td>Moves
 *          the printing position to the start of
 *          the current line.
 *</td></tr>
 *<tr><td>\t</td><td>tab</td><td>Moves the printing
 *          position to the next implementation-
 *          defined horizontal tab position.
 *</td></tr>
 *<tr><td>\v</td><td>vertical-tab</td><td>Moves the
 *          printing position to the start of the
 *          next implementation-defined vertical
 *          tab position.
 *</td></tr>
 *</table></p>
 *<h4>Conversion Specifications</h4>
 *<p>
 * Each conversion specification is introduced by
 * the percent sign character (%).  After the character
 * %, the following appear in sequence:</p>
 *<p>
 * Zero or more flags (in any order), which modify the
 * meaning of the conversion specification.</p>
 *<p>
 * An optional minimum field width.  If the converted
 * value has fewer characters than the field width, it
 * will be padded with spaces by default on the left;
 * t will be padded on the right, if the left-
 * adjustment flag (-), described below, is given to
 * the field width.  The field width takes the form
 * of a decimal integer.  If the conversion character
 * is s, the field width is the the minimum number of
 * characters to be printed.</p>
 *<p>
 * An optional precision that gives the minumum number
 * of digits to appear for the d, i, o, x or X
 * conversions (the field is padded with leading
 * zeros); the number of digits to appear after the
 * radix character for the e, E, and f conversions,
 * the maximum number of significant digits for the g
 * and G conversions; or the maximum number of
 * characters to be written from a string is s and S
 * conversions.  The precision takes the form of an
 * optional decimal digit string, where a null digit
 * string is treated as 0.  If a precision appears
 * with a c conversion character the precision is
 * ignored.
 * </p>
 *<p>
 * An optional h specifies that a following d, i, o,
 * x, or X conversion character applies to a type 
 * short argument (the argument will be promoted
 * according to the integral promotions and its value
 * converted to type short before printing).</p>
 *<p>
 * An optional l (ell) specifies that a following
 * d, i, o, x, or X conversion character applies to a
 * type long argument.</p>
 *<p>
 * A field width or precision may be indicated by an
 * asterisk (*) instead of a digit string.  In this
 * case, an integer argument supplised the field width
 * precision.  The argument that is actually converted
 * is not fetched until the conversion letter is seen,
 * so the the arguments specifying field width or
 * precision must appear before the argument (if any)
 * to be converted.  If the precision argument is
 * negative, it will be changed to zero.  A negative
 * field width argument is taken as a - flag, followed
 * by a positive field width.</p>
 * <p>
 * In format strings containing the %<code>n</code>$
 * form of a conversion specification, a field width
 * or precision may be indicated by the sequence
 * *<code>m</code>$, where m is a decimal integer
 * giving the position in the argument list (after the
 * format argument) of an integer argument containing
 * the field width or precision.</p>
 * <p>
 * The format can contain either numbered argument
 * specifications (that is, %<code>n</code>$ and
 * *<code>m</code>$), or unnumbered argument
 * specifications (that is % and *), but normally not
 * both.  The only exception to this is that %% can
 * be mixed with the %<code>n</code>$ form.  The
 * results of mixing numbered and unnumbered argument
 * specifications in a format string are undefined.</p>
 *
 *<h4>Flag Characters</h4>
 *<p>
 * The flags and their meanings are:</p>
 *<dl>
 * <dt>'<dd> integer portion of the result of a
 *      decimal conversion (%i, %d, %f, %g, or %G) will
 *      be formatted with thousands' grouping
 *      characters.  For other conversions the flag
 *      is ignored.  The non-monetary grouping
 *      character is used.
 * <dt>-<dd> result of the conversion is left-justified
 *      within the field.  (It will be right-justified
 *      if this flag is not specified).</td></tr>
 * <dt>+<dd> result of a signed conversion always
 *      begins with a sign (+ or -).  (It will begin
 *      with a sign only when a negative value is
 *      converted if this flag is not specified.)
 * <dt>&lt;space&gt;<dd> If the first character of a
 *      signed conversion is not a sign, a space
 *      character will be placed before the result.
 *      This means that if the space character and +
 *      flags both appear, the space flag will be
 *      ignored.
 * <dt>#<dd> value is to be converted to an alternative
 *      form.  For c, d, i, and s conversions, the flag
 *      has no effect.  For o conversion, it increases
 *      the precision to force the first digit of the
 *      result to be a zero.  For x or X conversion, a
 *      non-zero result has 0x or 0X prefixed to it,
 *      respectively.  For e, E, f, g, and G
 *      conversions, the result always contains a radix
 *      character, even if no digits follow the radix
 *      character (normally, a decimal point appears in
 *      the result of these conversions only if a digit
 *      follows it).  For g and G conversions, trailing
 *      zeros will not be removed from the result as
 *      they normally are.
 * <dt>0<dd> d, i, o, x, X, e, E, f, g, and G
 *      conversions, leading zeros (following any
 *      indication of sign or base) are used to pad to
 *      the field width;  no space padding is
 *      performed.  If the 0 and - flags both appear,
 *      the 0 flag is ignored.  For d, i, o, x, and X
 *      conversions, if a precision is specified, the
 *      0 flag will be ignored. For c conversions,
 *      the flag is ignored.
 *</dl>
 *
 *<h4>Conversion Characters</h4>
 *<p>
 * Each conversion character results in fetching zero
 * or more arguments.  The results are undefined if
 * there are insufficient arguments for the format.
 * Usually, an unchecked exception will be thrown.
 * If the format is exhausted while arguments remain,
 * the excess arguments are ignored.</p>
 *
 *<p>
 * The conversion characters and their meanings are:
 *</p>
 *<dl>
 * <dt>d,i<dd>The int argument is converted to a
 *        signed decimal in the style [-]dddd.  The
 *        precision specifies the minimum number of
 *        digits to appear;  if the value being
 *        converted can be represented in fewer
 *        digits, it will be expanded with leading
 *        zeros.  The default precision is 1.  The
 *        result of converting 0 with an explicit
 *        precision of 0 is no characters.
 * <dt>o<dd> The int argument is converted to unsigned
 *        octal format in the style ddddd.  The
 *        precision specifies the minimum number of
 *        digits to appear;  if the value being
 *        converted can be represented in fewer
 *        digits, it will be expanded with leading
 *        zeros.  The default precision is 1.  The
 *        result of converting 0 with an explicit
 *        precision of 0 is no characters.
 * <dt>x<dd> The int argument is converted to unsigned
 *        hexadecimal format in the style dddd;  the
 *        letters abcdef are used.  The precision
 *        specifies the minimum numberof digits to
 *        appear; if the value being converted can be
 *        represented in fewer digits, it will be
 *        expanded with leading zeros.  The default
 *        precision is 1.  The result of converting 0
 *        with an explicit precision of 0 is no
 *        characters.
 * <dt>X<dd> Behaves the same as the x conversion
 *        character except that letters ABCDEF are
 *        used instead of abcdef.
 * <dt>f<dd> The floating point number argument is
 *        written in decimal notation in the style
 *        [-]ddd.ddd, where the number of digits after
 *        the radix character (shown here as a decimal
 *        point) is equal to the precision
 *        specification.  A Locale is used to determine
 *        the radix character to use in this format.
 *        If the precision is omitted from the
 *        argument, six digits are written after the
 *        radix character;  if the precision is
 *        explicitly 0 and the # flag is not specified,
 *        no radix character appears.  If a radix
 *        character appears, at least 1 digit appears
 *        before it.  The value is rounded to the
 *        appropriate number of digits.
 * <dt>e,E<dd>The floating point number argument is
 *        written in the style [-]d.ddde{+-}dd
 *        (the symbols {+-} indicate either a plus or
 *        minus sign), where there is one digit before
 *        the radix character (shown here as a decimal
 *        point) and the number of digits after it is
 *        equal to the precision.  A Locale is used to
 *        determine the radix character to use in this
 *        format.  When the precision is missing, six
 *        digits are written after the radix character;
 *        if the precision is 0 and the # flag is not
 *        specified, no radix character appears.  The
 *        E conversion will produce a number with E
 *        instead of e introducing the exponent.  The
 *        exponent always contains at least two digits.
 *        However, if the value to be written requires
 *        an exponent greater than two digits,
 *        additional exponent digits are written as
 *        necessary.  The value is rounded to the
 *        appropriate number of digits.
 * <dt>g,G<dd>The floating point number argument is
 *        written in style f or e (or in sytle E in the
 *        case of a G conversion character), with the
 *        precision specifying the number of
 *        significant digits.  If the precision is
 *        zero, it is taken as one.  The style used
 *        depends on the value converted:  style e
 *        (or E) will be used only if the exponent
 *        resulting from the conversion is less than
 *        -4 or greater than or equal to the precision.
 *        Trailing zeros are removed from the result.
 *        A radix character appears only if it is
 *        followed by a digit.
 * <dt>c,C<dd>The integer argument is converted to a
 *        char and the result is written.
 *
 * <dt>s,S<dd>The argument is taken to be a string and
 *        bytes from the string are written until the
 *        end of the string or the number of bytes 
 *        indicated by the precision specification of
 *        the argument is reached.  If the precision
 *        is omitted from the argument, it is taken to
 *        be infinite, so all characters up to the end
 *        of the string are written.
 * <dt>%<dd>Write a % character;  no argument is
 *        converted.
 *</dl>
 *<p>
 * If a conversion specification does not match one of
 * the above forms, an IllegalArgumentException is
 * thrown and the instance of PrintfFormat is not
 * created.</p>
 *<p>
 * If a floating point value is the internal
 * representation for infinity, the output is
 * [+]Infinity, where Infinity is either Infinity or
 * Inf, depending on the desired output string length.
 * Printing of the sign follows the rules described
 * above.</p>
 *<p>
 * If a floating point value is the internal
 * representation for "not-a-number," the output is
 * [+]NaN.  Printing of the sign follows the rules
 * described above.</p>
 *<p>
 * In no case does a non-existent or small field width
 * cause truncation of a field;  if the result of a
 * conversion is wider than the field width, the field
 * is simply expanded to contain the conversion result.
 *</p>
 *<p>
 * The behavior is like printf.  One exception is that
 * the minimum number of exponent digits is 3 instead
 * of 2 for e and E formats when the optional L is used
 * before the e, E, g, or G conversion character.  The
 * optional L does not imply conversion to a long long
 * double. </p>
 * <p>
 * The biggest divergence from the C printf
 * specification is in the use of 16 bit characters.
 * This allows the handling of characters beyond the
 * small ASCII character set and allows the utility to
 * interoperate correctly with the rest of the Java
 * runtime environment.</p>
 *<p>
 * Omissions from the C printf specification are
 * numerous.  All the known omissions are present
 * because Java never uses bytes to represent
 * characters and does not have pointers:</p>
 *<ul>
 * <li>%c is the same as %C.
 * <li>%s is the same as %S.
 * <li>u, p, and n conversion characters. 
 * <li>%ws format.
 * <li>h modifier applied to an n conversion character.
 * <li>l (ell) modifier applied to the c, n, or s
 * conversion characters.
 * <li>ll (ell ell) modifier to d, i, o, u, x, or X
 * conversion characters.
 * <li>ll (ell ell) modifier to an n conversion
 * character.
 * <li>c, C, d,i,o,u,x, and X conversion characters
 * apply to Byte, Character, Short, Integer, Long
 * types.
 * <li>f, e, E, g, and G conversion characters apply
 * to Float and Double types.
 * <li>s and S conversion characters apply to String
 * types.
 * <li>All other reference types can be formatted
 * using the s or S conversion characters only.
 *</ul>
 * <p>
 * Most of this specification is quoted from the Unix
 * man page for the sprintf utility.</p>
 *
 * @author Allan Jacobs
 * @version 1
 * Release 1: Initial release.
 * Release 2: Asterisk field widths and precisions    
 *            %n$ and *m$
 *            Bug fixes
 *              g format fix (2 digits in e form corrupt)
 *              rounding in f format implemented
 *              round up when digit not printed is 5
 *              formatting of -0.0f
 *              round up/down when last digits are 50000...
 */
public class PrintfFormat {
	/**
	 * Constructs an array of control specifications
	 * possibly preceded, separated, or followed by
	 * ordinary strings.  Control strings begin with
	 * unpaired percent signs.  A pair of successive
	 * percent signs designates a single percent sign in
	 * the format.
	 * @param fmtArg  Control string.
	 * @exception IllegalArgumentException if the control
	 * string is null, zero length, or otherwise
	 * malformed.
	 */
	public PrintfFormat(String fmtArg) throws IllegalArgumentException {
		this(Locale.getDefault(), fmtArg);
	}
	/**
	 * Constructs an array of control specifications
	 * possibly preceded, separated, or followed by
	 * ordinary strings.  Control strings begin with
	 * unpaired percent signs.  A pair of successive
	 * percent signs designates a single percent sign in
	 * the format.
	 * @param fmtArg  Control string.
	 * @exception IllegalArgumentException if the control
	 * string is null, zero length, or otherwise
	 * malformed.
	 */
	public PrintfFormat(Locale locale, String fmtArg) throws IllegalArgumentException {
		dfs = new DecimalFormatSymbols(locale);
		int ePos = 0;
		ConversionSpecification sFmt = null;
		String unCS = this.nonControl(fmtArg, 0);
		if (unCS != null) {
			sFmt = new ConversionSpecification();
			sFmt.setLiteral(unCS);
			vFmt.addElement(sFmt);
		}
		while (cPos != -1 && cPos < fmtArg.length()) {
			for (ePos = cPos + 1; ePos < fmtArg.length(); ePos++) {
				char c = 0;
				c = fmtArg.charAt(ePos);
				if (c == 'i')
					break;
				if (c == 'd')
					break;
				if (c == 'f')
					break;
				if (c == 'g')
					break;
				if (c == 'G')
					break;
				if (c == 'o')
					break;
				if (c == 'x')
					break;
				if (c == 'X')
					break;
				if (c == 'e')
					break;
				if (c == 'E')
					break;
				if (c == 'c')
					break;
				if (c == 's')
					break;
				if (c == '%')
					break;
			}
			ePos = Math.min(ePos + 1, fmtArg.length());
			sFmt = new ConversionSpecification(fmtArg.substring(cPos, ePos));
			vFmt.addElement(sFmt);
			unCS = this.nonControl(fmtArg, ePos);
			if (unCS != null) {
				sFmt = new ConversionSpecification();
				sFmt.setLiteral(unCS);
				vFmt.addElement(sFmt);
			}
		}
	}
	/**
	 * Return a substring starting at
	 * <code>start</code> and ending at either the end
	 * of the String <code>s</code>, the next unpaired
	 * percent sign, or at the end of the String if the
	 * last character is a percent sign.
	 * @param s  Control string.
	 * @param start Position in the string
	 *     <code>s</code> to begin looking for the start
	 *     of a control string.
	 * @return the substring from the start position
	 *     to the beginning of the control string.
	 */
	private String nonControl(String s, int start) {
		String ret = "";
		cPos = s.indexOf("%", start);
		if (cPos == -1)
			cPos = s.length();
		return s.substring(start, cPos);
	}
	/**
	 * Format an array of objects.  Byte, Short,
	 * Integer, Long, Float, Double, and Character
	 * arguments are treated as wrappers for primitive
	 * types.
	 * @param o The array of objects to format.
	 * @return  The formatted String.
	 */
	public String sprintf(Object[] o) {
		Enumeration e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = 0;
		int i = 0;
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if (c == '\0')
				sb.append(cs.getLiteral());
			else if (c == '%')
				sb.append("%");
			else {
				if (cs.isPositionalSpecification()) {
					i = cs.getArgumentPosition() - 1;
					if (cs.isPositionalFieldWidth()) {
						int ifw = cs.getArgumentPositionForFieldWidth() - 1;
						cs.setFieldWidthWithArg(((Integer) o[ifw]).intValue());
					}
					if (cs.isPositionalPrecision()) {
						int ipr = cs.getArgumentPositionForPrecision() - 1;
						cs.setPrecisionWithArg(((Integer) o[ipr]).intValue());
					}
				} else {
					if (cs.isVariableFieldWidth()) {
						cs.setFieldWidthWithArg(((Integer) o[i]).intValue());
						i++;
					}
					if (cs.isVariablePrecision()) {
						cs.setPrecisionWithArg(((Integer) o[i]).intValue());
						i++;
					}
				}
				if (o[i] instanceof Byte)
					sb.append(cs.internalsprintf(((Byte) o[i]).byteValue()));
				else if (o[i] instanceof Short)
					sb.append(cs.internalsprintf(((Short) o[i]).shortValue()));
				else if (o[i] instanceof Integer)
					sb.append(cs.internalsprintf(((Integer) o[i]).intValue()));
				else if (o[i] instanceof Long)
					sb.append(cs.internalsprintf(((Long) o[i]).longValue()));
				else if (o[i] instanceof Float)
					sb.append(cs.internalsprintf(((Float) o[i]).floatValue()));
				else if (o[i] instanceof Double)
					sb.append(cs.internalsprintf(((Double) o[i]).doubleValue()));
				else if (o[i] instanceof Character)
					sb.append(cs.internalsprintf(((Character) o[i]).charValue()));
				else if (o[i] instanceof String)
					sb.append(cs.internalsprintf((String) o[i]));
				else
					sb.append(cs.internalsprintf(o[i]));
				if (!cs.isPositionalSpecification())
					i++;
			}
		}
		return sb.toString();
	}
	/**
	 * Format nothing.  Just use the control string.
	 * @return  the formatted String.
	 */
	public String sprintf() {
		Enumeration e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = 0;
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if (c == '\0')
				sb.append(cs.getLiteral());
			else if (c == '%')
				sb.append("%");
		}
		return sb.toString();
	}
	/**
	 * Format an int.
	 * @param x The int to format.
	 * @return  The formatted String.
	 * @exception IllegalArgumentException if the
	 *     conversion character is f, e, E, g, G, s,
	 *     or S.
	 */
	public String sprintf(int x) throws IllegalArgumentException {
		Enumeration e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = 0;
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if (c == '\0')
				sb.append(cs.getLiteral());
			else if (c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}
	/**
	 * Format an long.
	 * @param x The long to format.
	 * @return  The formatted String.
	 * @exception IllegalArgumentException if the
	 *     conversion character is f, e, E, g, G, s,
	 *     or S.
	 */
	public String sprintf(long x) throws IllegalArgumentException {
		Enumeration e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = 0;
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if (c == '\0')
				sb.append(cs.getLiteral());
			else if (c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}
	/**
	 * Format a double.
	 * @param x The double to format.
	 * @return  The formatted String.
	 * @exception IllegalArgumentException if the
	 *     conversion character is c, C, s, S,
	 *     d, d, x, X, or o.
	 */
	public String sprintf(double x) throws IllegalArgumentException {
		Enumeration e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = 0;
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if (c == '\0')
				sb.append(cs.getLiteral());
			else if (c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}
	/**
	 * Format a String.
	 * @param x The String to format.
	 * @return  The formatted String.
	 * @exception IllegalArgumentException if the
	 *   conversion character is neither s nor S.
	 */
	public String sprintf(String x) throws IllegalArgumentException {
		Enumeration e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = 0;
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if (c == '\0')
				sb.append(cs.getLiteral());
			else if (c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}
	/**
	 * Format an Object.  Convert wrapper types to
	 * their primitive equivalents and call the
	 * appropriate internal formatting method. Convert
	 * Strings using an internal formatting method for
	 * Strings. Otherwise use the default formatter
	 * (use toString).
	 * @param x the Object to format.
	 * @return  the formatted String.
	 * @exception IllegalArgumentException if the
	 *    conversion character is inappropriate for
	 *    formatting an unwrapped value.
	 */
	public String sprintf(Object x) throws IllegalArgumentException {
		Enumeration e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = 0;
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if (c == '\0')
				sb.append(cs.getLiteral());
			else if (c == '%')
				sb.append("%");
			else {
				if (x instanceof Byte)
					sb.append(cs.internalsprintf(((Byte) x).byteValue()));
				else if (x instanceof Short)
					sb.append(cs.internalsprintf(((Short) x).shortValue()));
				else if (x instanceof Integer)
					sb.append(cs.internalsprintf(((Integer) x).intValue()));
				else if (x instanceof Long)
					sb.append(cs.internalsprintf(((Long) x).longValue()));
				else if (x instanceof Float)
					sb.append(cs.internalsprintf(((Float) x).floatValue()));
				else if (x instanceof Double)
					sb.append(cs.internalsprintf(((Double) x).doubleValue()));
				else if (x instanceof Character)
					sb.append(cs.internalsprintf(((Character) x).charValue()));
				else if (x instanceof String)
					sb.append(cs.internalsprintf((String) x));
				else
					sb.append(cs.internalsprintf(x));
			}
		}
		return sb.toString();
	}
	/**
	 *<p>
	 * ConversionSpecification allows the formatting of
	 * a single primitive or object embedded within a
	 * string.  The formatting is controlled by a
	 * format string.  Only one Java primitive or
	 * object can be formatted at a time.
	 *<p>
	 * A format string is a Java string that contains
	 * a control string.  The control string starts at
	 * the first percent sign (%) in the string,
	 * provided that this percent sign
	 *<ol>
	 *<li>is not escaped protected by a matching % or
	 *     is not an escape % character,
	 *<li>is not at the end of the format string, and
	 *<li>precedes a sequence of characters that parses
	 *     as a valid control string.
	 *</ol>
	 *<p>
	 * A control string takes the form:
	 *<pre> % ['-+ #0]* [0..9]* { . [0..9]* }+
	 *                { [hlL] }+ [idfgGoxXeEcs]
	 *</pre>
	 *<p>
	 * The behavior is like printf.  One (hopefully the
	 * only) exception is that the minimum number of
	 * exponent digits is 3 instead of 2 for e and E
	 * formats when the optional L is used before the
	 * e, E, g, or G conversion character.  The 
	 * optional L does not imply conversion to a long
	 * long double.
	 */
	private class ConversionSpecification {
		/**
		 * Constructor.  Used to prepare an instance
		 * to hold a literal, not a control string.
		 */
		ConversionSpecification() {
		}
		/**
		 * Constructor for a conversion specification.
		 * The argument must begin with a % and end
		 * with the conversion character for the
		 * conversion specification.
		  * @param fmtArg  String specifying the
		 *     conversion specification.
		  * @exception IllegalArgumentException if the
		 *     input string is null, zero length, or
		 *     otherwise malformed.
		 */
		ConversionSpecification(String fmtArg) throws IllegalArgumentException {
			if (fmtArg == null)
				throw new NullPointerException();
			if (fmtArg.length() == 0)
				throw new IllegalArgumentException("Control strings must have positive" + " lengths.");
			if (fmtArg.charAt(0) == '%') {
				fmt = fmtArg;
				pos = 1;
				setArgPosition();
				setFlagCharacters();
				setFieldWidth();
				setPrecision();
				setOptionalHL();
				if (setConversionCharacter()) {
					if (pos == fmtArg.length()) {
						if (leadingZeros && leftJustify)
							leadingZeros = false;
						if (precisionSet && leadingZeros) {
							if (conversionCharacter == 'd'
								|| conversionCharacter == 'i'
								|| conversionCharacter == 'o'
								|| conversionCharacter == 'x') {
								leadingZeros = false;
							}
						}
					} else
						throw new IllegalArgumentException("Malformed conversion specification=" + fmtArg);
				} else
					throw new IllegalArgumentException("Malformed conversion specification=" + fmtArg);
			} else
				throw new IllegalArgumentException("Control strings must begin with %.");
		}
		/**
		 * Set the String for this instance.
		 * @param s the String to store.
		 */
		void setLiteral(String s) {
			fmt = s;
		}
		/**
		 * Get the String for this instance.  Translate
		 * any escape sequences.
		 *
		 * @return s the stored String.
		 */
		String getLiteral() {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			while (i < fmt.length()) {
				if (fmt.charAt(i) == '\\') {
					i++;
					if (i < fmt.length()) {
						char c = fmt.charAt(i);
						switch (c) {
							case 'a' :
								sb.append((char) 0x07);
								break;
							case 'b' :
								sb.append('\b');
								break;
							case 'f' :
								sb.append('\f');
								break;
							case 'n' :
								sb.append(System.getProperty("line.separator"));
								break;
							case 'r' :
								sb.append('\r');
								break;
							case 't' :
								sb.append('\t');
								break;
							case 'v' :
								sb.append((char) 0x0b);
								break;
							case '\\' :
								sb.append('\\');
								break;
						}
						i++;
					} else
						sb.append('\\');
				} else
					i++;
			}
			return fmt;
		}
		/**
		 * Get the conversion character that tells what
		 * type of control character this instance has.
		 *
		 * @return the conversion character.
		 */
		char getConversionCharacter() {
			return conversionCharacter;
		}
		/**
		 * Check whether the specifier has a variable
		 * field width that is going to be set by an
		 * argument.
		 * @return <code>true</code> if the conversion
		 *   uses an * field width; otherwise
		 *   <code>false</code>.
		 */
		boolean isVariableFieldWidth() {
			return variableFieldWidth;
		}
		/**
		 * Set the field width with an argument.  A
		 * negative field width is taken as a - flag
		 * followed by a positive field width.
		 * @param fw the field width.
		 */
		void setFieldWidthWithArg(int fw) {
			if (fw < 0)
				leftJustify = true;
			fieldWidthSet = true;
			fieldWidth = Math.abs(fw);
		}
		/**
		 * Check whether the specifier has a variable
		 * precision that is going to be set by an
		 * argument.
		 * @return <code>true</code> if the conversion
		 *   uses an * precision; otherwise
		 *   <code>false</code>.
		 */
		boolean isVariablePrecision() {
			return variablePrecision;
		}
		/**
		 * Set the precision with an argument.  A
		 * negative precision will be changed to zero.
		 * @param pr the precision.
		 */
		void setPrecisionWithArg(int pr) {
			precisionSet = true;
			precision = Math.max(pr, 0);
		}
		/**
		 * Format an int argument using this conversion
		  * specification.
		 * @param s the int to format.
		 * @return the formatted String.
		 * @exception IllegalArgumentException if the
		 *     conversion character is f, e, E, g, or G.
		 */
		String internalsprintf(int s) throws IllegalArgumentException {
			String s2 = "";
			switch (conversionCharacter) {
				case 'd' :
				case 'i' :
					if (optionalh)
						s2 = printDFormat((short) s);
					else if (optionall)
						s2 = printDFormat((long) s);
					else
						s2 = printDFormat(s);
					break;
				case 'x' :
				case 'X' :
					if (optionalh)
						s2 = printXFormat((short) s);
					else if (optionall)
						s2 = printXFormat((long) s);
					else
						s2 = printXFormat(s);
					break;
				case 'o' :
					if (optionalh)
						s2 = printOFormat((short) s);
					else if (optionall)
						s2 = printOFormat((long) s);
					else
						s2 = printOFormat(s);
					break;
				case 'c' :
				case 'C' :
					s2 = printCFormat((char) s);
					break;
				default :
					throw new IllegalArgumentException(
						"Cannot format a int with a format using a " + conversionCharacter + " conversion character.");
			}
			return s2;
		}
		/**
		 * Format a long argument using this conversion
		 * specification.
		 * @param s the long to format.
		 * @return the formatted String.
		 * @exception IllegalArgumentException if the
		 *     conversion character is f, e, E, g, or G.
		 */
		String internalsprintf(long s) throws IllegalArgumentException {
			String s2 = "";
			switch (conversionCharacter) {
				case 'd' :
				case 'i' :
					if (optionalh)
						s2 = printDFormat((short) s);
					else if (optionall)
						s2 = printDFormat(s);
					else
						s2 = printDFormat((int) s);
					break;
				case 'x' :
				case 'X' :
					if (optionalh)
						s2 = printXFormat((short) s);
					else if (optionall)
						s2 = printXFormat(s);
					else
						s2 = printXFormat((int) s);
					break;
				case 'o' :
					if (optionalh)
						s2 = printOFormat((short) s);
					else if (optionall)
						s2 = printOFormat(s);
					else
						s2 = printOFormat((int) s);
					break;
				case 'c' :
				case 'C' :
					s2 = printCFormat((char) s);
					break;
				default :
					throw new IllegalArgumentException(
						"Cannot format a long with a format using a " + conversionCharacter + " conversion character.");
			}
			return s2;
		}
		/**
		 * Format a double argument using this conversion
		 * specification.
		 * @param s the double to format.
		 * @return the formatted String.
		 * @exception IllegalArgumentException if the
		 *     conversion character is c, C, s, S, i, d,
		 *     x, X, or o.
		 */
		String internalsprintf(double s) throws IllegalArgumentException {
			String s2 = "";
			switch (conversionCharacter) {
				case 'f' :
					s2 = printFFormat(s);
					break;
				case 'E' :
				case 'e' :
					s2 = printEFormat(s);
					break;
				case 'G' :
				case 'g' :
					s2 = printGFormat(s);
					break;
				default :
					throw new IllegalArgumentException(
						"Cannot " + "format a double with a format using a " + conversionCharacter + " conversion character.");
			}
			return s2;
		}
		/**
		 * Format a String argument using this conversion
		 * specification.
		 * @param s the String to format.
		 * @return the formatted String.
		 * @exception IllegalArgumentException if the
		 *   conversion character is neither s nor S.
		 */
		String internalsprintf(String s) throws IllegalArgumentException {
			String s2 = "";
			if (conversionCharacter == 's' || conversionCharacter == 'S')
				s2 = printSFormat(s);
			else
				throw new IllegalArgumentException(
					"Cannot " + "format a String with a format using a " + conversionCharacter + " conversion character.");
			return s2;
		}
		/**
		 * Format an Object argument using this conversion
		 * specification.
		 * @param s the Object to format.
		 * @return the formatted String.
		 * @exception IllegalArgumentException if the
		 *     conversion character is neither s nor S.
		 */
		String internalsprintf(Object s) {
			String s2 = "";
			if (conversionCharacter == 's' || conversionCharacter == 'S')
				s2 = printSFormat(s.toString());
			else
				throw new IllegalArgumentException(
					"Cannot format a String with a format using" + " a " + conversionCharacter + " conversion character.");
			return s2;
		}
		/**
		 * For f format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  '+' character means that the conversion
		 * will always begin with a sign (+ or -).  The
		 * blank flag character means that a non-negative
		 * input will be preceded with a blank.  If both
		 * a '+' and a ' ' are specified, the blank flag
		 * is ignored.  The '0' flag character implies that
		 * padding to the field width will be done with
		 * zeros instead of blanks.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the number of digits
		 * to appear after the radix character.  Padding is
		 * with trailing 0s.
		 */
		private char[] fFormatDigits(double x) {
			// int defaultDigits=6;
			String sx, sxOut;
			int i, j, k;
			int n1In, n2In;
			int expon = 0;
			boolean minusSign = false;
			if (x > 0.0)
				sx = Double.toString(x);
			else if (x < 0.0) {
				sx = Double.toString(-x);
				minusSign = true;
			} else {
				sx = Double.toString(x);
				if (sx.charAt(0) == '-') {
					minusSign = true;
					sx = sx.substring(1);
				}
			}
			int ePos = sx.indexOf('E');
			int rPos = sx.indexOf('.');
			if (rPos != -1)
				n1In = rPos;
			else if (ePos != -1)
				n1In = ePos;
			else
				n1In = sx.length();
			if (rPos != -1) {
				if (ePos != -1)
					n2In = ePos - rPos - 1;
				else
					n2In = sx.length() - rPos - 1;
			} else
				n2In = 0;
			if (ePos != -1) {
				int ie = ePos + 1;
				expon = 0;
				if (sx.charAt(ie) == '-') {
					for (++ie; ie < sx.length(); ie++)
						if (sx.charAt(ie) != '0')
							break;
					if (ie < sx.length())
						expon = -Integer.parseInt(sx.substring(ie));
				} else {
					if (sx.charAt(ie) == '+')
						++ie;
					for (; ie < sx.length(); ie++)
						if (sx.charAt(ie) != '0')
							break;
					if (ie < sx.length())
						expon = Integer.parseInt(sx.substring(ie));
				}
			}
			int p;
			if (precisionSet)
				p = precision;
			else
				p = defaultDigits - 1;
			char[] ca1 = sx.toCharArray();
			char[] ca2 = new char[n1In + n2In];
			char[] ca3, ca4, ca5;
			for (j = 0; j < n1In; j++)
				ca2[j] = ca1[j];
			i = j + 1;
			for (k = 0; k < n2In; j++, i++, k++)
				ca2[j] = ca1[i];
			if (n1In + expon <= 0) {
				ca3 = new char[-expon + n2In];
				for (j = 0, k = 0; k < (-n1In - expon); k++, j++)
					ca3[j] = '0';
				for (i = 0; i < (n1In + n2In); i++, j++)
					ca3[j] = ca2[i];
			} else
				ca3 = ca2;
			boolean carry = false;
			if (p < -expon + n2In) {
				if (expon < 0)
					i = p;
				else
					i = p + n1In;
				carry = checkForCarry(ca3, i);
				if (carry)
					carry = startSymbolicCarry(ca3, i - 1, 0);
			}
			if (n1In + expon <= 0) {
				ca4 = new char[2 + p];
				if (!carry)
					ca4[0] = '0';
				else
					ca4[0] = '1';
				if (alternateForm || !precisionSet || precision != 0) {
					ca4[1] = '.';
					for (i = 0, j = 2; i < Math.min(p, ca3.length); i++, j++)
						ca4[j] = ca3[i];
					for (; j < ca4.length; j++)
						ca4[j] = '0';
				}
			} else {
				if (!carry) {
					if (alternateForm || !precisionSet || precision != 0)
						ca4 = new char[n1In + expon + p + 1];
					else
						ca4 = new char[n1In + expon];
					j = 0;
				} else {
					if (alternateForm || !precisionSet || precision != 0)
						ca4 = new char[n1In + expon + p + 2];
					else
						ca4 = new char[n1In + expon + 1];
					ca4[0] = '1';
					j = 1;
				}
				for (i = 0; i < Math.min(n1In + expon, ca3.length); i++, j++)
					ca4[j] = ca3[i];
				for (; i < n1In + expon; i++, j++)
					ca4[j] = '0';
				if (alternateForm || !precisionSet || precision != 0) {
					ca4[j] = '.';
					j++;
					for (k = 0; i < ca3.length && k < p; i++, j++, k++)
						ca4[j] = ca3[i];
					for (; j < ca4.length; j++)
						ca4[j] = '0';
				}
			}
			int nZeros = 0;
			if (!leftJustify && leadingZeros) {
				int xThousands = 0;
				if (thousands) {
					int xlead = 0;
					if (ca4[0] == '+' || ca4[0] == '-' || ca4[0] == ' ')
						xlead = 1;
					int xdp = xlead;
					for (; xdp < ca4.length; xdp++)
						if (ca4[xdp] == '.')
							break;
					xThousands = (xdp - xlead) / 3;
				}
				if (fieldWidthSet)
					nZeros = fieldWidth - ca4.length;
				if ((!minusSign && (leadingSign || leadingSpace)) || minusSign)
					nZeros--;
				nZeros -= xThousands;
				if (nZeros < 0)
					nZeros = 0;
			}
			j = 0;
			if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
				ca5 = new char[ca4.length + nZeros + 1];
				j++;
			} else
				ca5 = new char[ca4.length + nZeros];
			if (!minusSign) {
				if (leadingSign)
					ca5[0] = '+';
				if (leadingSpace)
					ca5[0] = ' ';
			} else
				ca5[0] = '-';
			for (i = 0; i < nZeros; i++, j++)
				ca5[j] = '0';
			for (i = 0; i < ca4.length; i++, j++)
				ca5[j] = ca4[i];

			int lead = 0;
			if (ca5[0] == '+' || ca5[0] == '-' || ca5[0] == ' ')
				lead = 1;
			int dp = lead;
			for (; dp < ca5.length; dp++)
				if (ca5[dp] == '.')
					break;
			int nThousands = (dp - lead) / 3;
			// Localize the decimal point.
			if (dp < ca5.length)
				ca5[dp] = dfs.getDecimalSeparator();
			char[] ca6 = ca5;
			if (thousands && nThousands > 0) {
				ca6 = new char[ca5.length + nThousands + lead];
				ca6[0] = ca5[0];
				for (i = lead, k = lead; i < dp; i++) {
					if (i > 0 && (dp - i) % 3 == 0) {
						// ca6[k]=',';
						ca6[k] = dfs.getGroupingSeparator();
						ca6[k + 1] = ca5[i];
						k += 2;
					} else {
						ca6[k] = ca5[i];
						k++;
					}
				}
				for (; i < ca5.length; i++, k++) {
					ca6[k] = ca5[i];
				}
			}
			return ca6;
		}
		/**
		 * An intermediate routine on the way to creating
		 * an f format String.  The method decides whether
		 * the input double value is an infinity,
		 * not-a-number, or a finite double and formats
		 * each type of input appropriately.
		 * @param x the double value to be formatted.
		 * @return the converted double value.
		 */
		private String fFormatString(double x) {
			boolean noDigits = false;
			char[] ca6, ca7;
			if (Double.isInfinite(x)) {
				if (x == Double.POSITIVE_INFINITY) {
					if (leadingSign)
						ca6 = "+Inf".toCharArray();
					else if (leadingSpace)
						ca6 = " Inf".toCharArray();
					else
						ca6 = "Inf".toCharArray();
				} else
					ca6 = "-Inf".toCharArray();
				noDigits = true;
			} else if (Double.isNaN(x)) {
				if (leadingSign)
					ca6 = "+NaN".toCharArray();
				else if (leadingSpace)
					ca6 = " NaN".toCharArray();
				else
					ca6 = "NaN".toCharArray();
				noDigits = true;
			} else
				ca6 = fFormatDigits(x);
			ca7 = applyFloatPadding(ca6, false);
			return new String(ca7);
		}
		/**
		 * For e format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  '+' character means that the conversion
		 * will always begin with a sign (+ or -).  The
		 * blank flag character means that a non-negative
		 * input will be preceded with a blank.  If both a
		 * '+' and a ' ' are specified, the blank flag is
		 * ignored.  The '0' flag character implies that
		 * padding to the field width will be done with
		 * zeros instead of blanks.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear after the radix character.
		 * Padding is with trailing 0s.
		 *
		 * The behavior is like printf.  One (hopefully the
		 * only) exception is that the minimum number of
		 * exponent digits is 3 instead of 2 for e and E
		 * formats when the optional L is used before the
		 * e, E, g, or G conversion character. The optional
		 * L does not imply conversion to a long long
		 * double.
		 */
		private char[] eFormatDigits(double x, char eChar) {
			char[] ca1, ca2, ca3;
			// int defaultDigits=6;
			String sx, sxOut;
			int i, j, k, p;
			int n1In, n2In;
			int expon = 0;
			int ePos, rPos, eSize;
			boolean minusSign = false;
			if (x > 0.0)
				sx = Double.toString(x);
			else if (x < 0.0) {
				sx = Double.toString(-x);
				minusSign = true;
			} else {
				sx = Double.toString(x);
				if (sx.charAt(0) == '-') {
					minusSign = true;
					sx = sx.substring(1);
				}
			}
			ePos = sx.indexOf('E');
			if (ePos == -1)
				ePos = sx.indexOf('e');
			rPos = sx.indexOf('.');
			if (rPos != -1)
				n1In = rPos;
			else if (ePos != -1)
				n1In = ePos;
			else
				n1In = sx.length();
			if (rPos != -1) {
				if (ePos != -1)
					n2In = ePos - rPos - 1;
				else
					n2In = sx.length() - rPos - 1;
			} else
				n2In = 0;
			if (ePos != -1) {
				int ie = ePos + 1;
				expon = 0;
				if (sx.charAt(ie) == '-') {
					for (++ie; ie < sx.length(); ie++)
						if (sx.charAt(ie) != '0')
							break;
					if (ie < sx.length())
						expon = -Integer.parseInt(sx.substring(ie));
				} else {
					if (sx.charAt(ie) == '+')
						++ie;
					for (; ie < sx.length(); ie++)
						if (sx.charAt(ie) != '0')
							break;
					if (ie < sx.length())
						expon = Integer.parseInt(sx.substring(ie));
				}
			}
			if (rPos != -1)
				expon += rPos - 1;
			if (precisionSet)
				p = precision;
			else
				p = defaultDigits - 1;
			if (rPos != -1 && ePos != -1)
				ca1 = (sx.substring(0, rPos) + sx.substring(rPos + 1, ePos)).toCharArray();
			else if (rPos != -1)
				ca1 = (sx.substring(0, rPos) + sx.substring(rPos + 1)).toCharArray();
			else if (ePos != -1)
				ca1 = sx.substring(0, ePos).toCharArray();
			else
				ca1 = sx.toCharArray();
			boolean carry = false;
			int i0 = 0;
			if (ca1[0] != '0')
				i0 = 0;
			else
				for (i0 = 0; i0 < ca1.length; i0++)
					if (ca1[i0] != '0')
						break;
			if (i0 + p < ca1.length - 1) {
				carry = checkForCarry(ca1, i0 + p + 1);
				if (carry)
					carry = startSymbolicCarry(ca1, i0 + p, i0);
				if (carry) {
					ca2 = new char[i0 + p + 1];
					ca2[i0] = '1';
					for (j = 0; j < i0; j++)
						ca2[j] = '0';
					for (i = i0, j = i0 + 1; j < p + 1; i++, j++)
						ca2[j] = ca1[i];
					expon++;
					ca1 = ca2;
				}
			}
			if (Math.abs(expon) < 100 && !optionalL)
				eSize = 4;
			else
				eSize = 5;
			if (alternateForm || !precisionSet || precision != 0)
				ca2 = new char[2 + p + eSize];
			else
				ca2 = new char[1 + eSize];
			if (ca1[0] != '0') {
				ca2[0] = ca1[0];
				j = 1;
			} else {
				for (j = 1; j < (ePos == -1 ? ca1.length : ePos); j++)
					if (ca1[j] != '0')
						break;
				if ((ePos != -1 && j < ePos) || (ePos == -1 && j < ca1.length)) {
					ca2[0] = ca1[j];
					expon -= j;
					j++;
				} else {
					ca2[0] = '0';
					j = 2;
				}
			}
			if (alternateForm || !precisionSet || precision != 0) {
				ca2[1] = '.';
				i = 2;
			} else
				i = 1;
			for (k = 0; k < p && j < ca1.length; j++, i++, k++)
				ca2[i] = ca1[j];
			for (; i < ca2.length - eSize; i++)
				ca2[i] = '0';
			ca2[i++] = eChar;
			if (expon < 0)
				ca2[i++] = '-';
			else
				ca2[i++] = '+';
			expon = Math.abs(expon);
			if (expon >= 100) {
				switch (expon / 100) {
					case 1 :
						ca2[i] = '1';
						break;
					case 2 :
						ca2[i] = '2';
						break;
					case 3 :
						ca2[i] = '3';
						break;
					case 4 :
						ca2[i] = '4';
						break;
					case 5 :
						ca2[i] = '5';
						break;
					case 6 :
						ca2[i] = '6';
						break;
					case 7 :
						ca2[i] = '7';
						break;
					case 8 :
						ca2[i] = '8';
						break;
					case 9 :
						ca2[i] = '9';
						break;
				}
				i++;
			}
			switch ((expon % 100) / 10) {
				case 0 :
					ca2[i] = '0';
					break;
				case 1 :
					ca2[i] = '1';
					break;
				case 2 :
					ca2[i] = '2';
					break;
				case 3 :
					ca2[i] = '3';
					break;
				case 4 :
					ca2[i] = '4';
					break;
				case 5 :
					ca2[i] = '5';
					break;
				case 6 :
					ca2[i] = '6';
					break;
				case 7 :
					ca2[i] = '7';
					break;
				case 8 :
					ca2[i] = '8';
					break;
				case 9 :
					ca2[i] = '9';
					break;
			}
			i++;
			switch (expon % 10) {
				case 0 :
					ca2[i] = '0';
					break;
				case 1 :
					ca2[i] = '1';
					break;
				case 2 :
					ca2[i] = '2';
					break;
				case 3 :
					ca2[i] = '3';
					break;
				case 4 :
					ca2[i] = '4';
					break;
				case 5 :
					ca2[i] = '5';
					break;
				case 6 :
					ca2[i] = '6';
					break;
				case 7 :
					ca2[i] = '7';
					break;
				case 8 :
					ca2[i] = '8';
					break;
				case 9 :
					ca2[i] = '9';
					break;
			}
			int nZeros = 0;
			if (!leftJustify && leadingZeros) {
				int xThousands = 0;
				if (thousands) {
					int xlead = 0;
					if (ca2[0] == '+' || ca2[0] == '-' || ca2[0] == ' ')
						xlead = 1;
					int xdp = xlead;
					for (; xdp < ca2.length; xdp++)
						if (ca2[xdp] == '.')
							break;
					xThousands = (xdp - xlead) / 3;
				}
				if (fieldWidthSet)
					nZeros = fieldWidth - ca2.length;
				if ((!minusSign && (leadingSign || leadingSpace)) || minusSign)
					nZeros--;
				nZeros -= xThousands;
				if (nZeros < 0)
					nZeros = 0;
			}
			j = 0;
			if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
				ca3 = new char[ca2.length + nZeros + 1];
				j++;
			} else
				ca3 = new char[ca2.length + nZeros];
			if (!minusSign) {
				if (leadingSign)
					ca3[0] = '+';
				if (leadingSpace)
					ca3[0] = ' ';
			} else
				ca3[0] = '-';
			for (k = 0; k < nZeros; j++, k++)
				ca3[j] = '0';
			for (i = 0; i < ca2.length && j < ca3.length; i++, j++)
				ca3[j] = ca2[i];

			int lead = 0;
			if (ca3[0] == '+' || ca3[0] == '-' || ca3[0] == ' ')
				lead = 1;
			int dp = lead;
			for (; dp < ca3.length; dp++)
				if (ca3[dp] == '.')
					break;
			int nThousands = dp / 3;
			// Localize the decimal point.
			if (dp < ca3.length)
				ca3[dp] = dfs.getDecimalSeparator();
			char[] ca4 = ca3;
			if (thousands && nThousands > 0) {
				ca4 = new char[ca3.length + nThousands + lead];
				ca4[0] = ca3[0];
				for (i = lead, k = lead; i < dp; i++) {
					if (i > 0 && (dp - i) % 3 == 0) {
						// ca4[k]=',';
						ca4[k] = dfs.getGroupingSeparator();
						ca4[k + 1] = ca3[i];
						k += 2;
					} else {
						ca4[k] = ca3[i];
						k++;
					}
				}
				for (; i < ca3.length; i++, k++)
					ca4[k] = ca3[i];
			}
			return ca4;
		}
		/**
		 * Check to see if the digits that are going to
		 * be truncated because of the precision should
		 * force a round in the preceding digits.
		 * @param ca1 the array of digits
		 * @param icarry the index of the first digit that
		 *     is to be truncated from the print
		 * @return <code>true</code> if the truncation forces
		 *     a round that will change the print
		 */
		private boolean checkForCarry(char[] ca1, int icarry) {
			boolean carry = false;
			if (icarry < ca1.length) {
				if (ca1[icarry] == '6' || ca1[icarry] == '7' || ca1[icarry] == '8' || ca1[icarry] == '9')
					carry = true;
				else if (ca1[icarry] == '5') {
					int ii = icarry + 1;
					for (; ii < ca1.length; ii++)
						if (ca1[ii] != '0')
							break;
					carry = ii < ca1.length;
					if (!carry && icarry > 0) {
						carry =
							(ca1[icarry - 1] == '1'
								|| ca1[icarry - 1] == '3'
								|| ca1[icarry - 1] == '5'
								|| ca1[icarry - 1] == '7'
								|| ca1[icarry - 1] == '9');
					}
				}
			}
			return carry;
		}
		/**
		 * Start the symbolic carry process.  The process
		 * is not quite finished because the symbolic
		 * carry may change the length of the string and
		 * change the exponent (in e format).
		 * @param cLast index of the last digit changed
		 *     by the round
		 * @param cFirst index of the first digit allowed
		 *     to be changed by this phase of the round
		 * @return <code>true</code> if the carry forces
		 *     a round that will change the print still
		 *     more
		 */
		private boolean startSymbolicCarry(char[] ca, int cLast, int cFirst) {
			boolean carry = true;
			for (int i = cLast; carry && i >= cFirst; i--) {
				carry = false;
				switch (ca[i]) {
					case '0' :
						ca[i] = '1';
						break;
					case '1' :
						ca[i] = '2';
						break;
					case '2' :
						ca[i] = '3';
						break;
					case '3' :
						ca[i] = '4';
						break;
					case '4' :
						ca[i] = '5';
						break;
					case '5' :
						ca[i] = '6';
						break;
					case '6' :
						ca[i] = '7';
						break;
					case '7' :
						ca[i] = '8';
						break;
					case '8' :
						ca[i] = '9';
						break;
					case '9' :
						ca[i] = '0';
						carry = true;
						break;
				}
			}
			return carry;
		}
		/**
		 * An intermediate routine on the way to creating
		 * an e format String.  The method decides whether
		 * the input double value is an infinity,
		 * not-a-number, or a finite double and formats
		 * each type of input appropriately.
		 * @param x the double value to be formatted.
		 * @param eChar an 'e' or 'E' to use in the
		 *     converted double value.
		 * @return the converted double value.
		 */
		private String eFormatString(double x, char eChar) {
			boolean noDigits = false;
			char[] ca4, ca5;
			if (Double.isInfinite(x)) {
				if (x == Double.POSITIVE_INFINITY) {
					if (leadingSign)
						ca4 = "+Inf".toCharArray();
					else if (leadingSpace)
						ca4 = " Inf".toCharArray();
					else
						ca4 = "Inf".toCharArray();
				} else
					ca4 = "-Inf".toCharArray();
				noDigits = true;
			} else if (Double.isNaN(x)) {
				if (leadingSign)
					ca4 = "+NaN".toCharArray();
				else if (leadingSpace)
					ca4 = " NaN".toCharArray();
				else
					ca4 = "NaN".toCharArray();
				noDigits = true;
			} else
				ca4 = eFormatDigits(x, eChar);
			ca5 = applyFloatPadding(ca4, false);
			return new String(ca5);
		}
		/**
		 * Apply zero or blank, left or right padding.
		 * @param ca4 array of characters before padding is
		 *     finished
		 * @param noDigits NaN or signed Inf
		 * @return a padded array of characters
		 */
		private char[] applyFloatPadding(char[] ca4, boolean noDigits) {
			char[] ca5 = ca4;
			if (fieldWidthSet) {
				int i, j, nBlanks;
				if (leftJustify) {
					nBlanks = fieldWidth - ca4.length;
					if (nBlanks > 0) {
						ca5 = new char[ca4.length + nBlanks];
						for (i = 0; i < ca4.length; i++)
							ca5[i] = ca4[i];
						for (j = 0; j < nBlanks; j++, i++)
							ca5[i] = ' ';
					}
				} else if (!leadingZeros || noDigits) {
					nBlanks = fieldWidth - ca4.length;
					if (nBlanks > 0) {
						ca5 = new char[ca4.length + nBlanks];
						for (i = 0; i < nBlanks; i++)
							ca5[i] = ' ';
						for (j = 0; j < ca4.length; i++, j++)
							ca5[i] = ca4[j];
					}
				} else if (leadingZeros) {
					nBlanks = fieldWidth - ca4.length;
					if (nBlanks > 0) {
						ca5 = new char[ca4.length + nBlanks];
						i = 0;
						j = 0;
						if (ca4[0] == '-') {
							ca5[0] = '-';
							i++;
							j++;
						}
						for (int k = 0; k < nBlanks; i++, k++)
							ca5[i] = '0';
						for (; j < ca4.length; i++, j++)
							ca5[i] = ca4[j];
					}
				}
			}
			return ca5;
		}
		/**
		 * Format method for the f conversion character.
		 * @param x the double to format.
		 * @return the formatted String.
		 */
		private String printFFormat(double x) {
			return fFormatString(x);
		}
		/**
		 * Format method for the e or E conversion
		 * character.
		 * @param x the double to format.
		 * @return the formatted String.
		 */
		private String printEFormat(double x) {
			if (conversionCharacter == 'e')
				return eFormatString(x, 'e');
			else
				return eFormatString(x, 'E');
		}
		/**
		 * Format method for the g conversion character.
		 *
		 * For g format, the flag character '-', means that
		 *  the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  '+' character means that the conversion
		 * will always begin with a sign (+ or -).  The
		 * blank flag character means that a non-negative
		 * input will be preceded with a blank.  If both a
		 * '+' and a ' ' are specified, the blank flag is
		 * ignored.  The '0' flag character implies that
		 * padding to the field width will be done with
		 * zeros instead of blanks.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear after the radix character.
		 * Padding is with trailing 0s.
		 * @param x the double to format.
		 * @return the formatted String.
		 */
		private String printGFormat(double x) {
			String sx, sy, sz, ret;
			int savePrecision = precision;
			int i;
			char[] ca4, ca5;
			boolean noDigits = false;
			if (Double.isInfinite(x)) {
				if (x == Double.POSITIVE_INFINITY) {
					if (leadingSign)
						ca4 = "+Inf".toCharArray();
					else if (leadingSpace)
						ca4 = " Inf".toCharArray();
					else
						ca4 = "Inf".toCharArray();
				} else
					ca4 = "-Inf".toCharArray();
				noDigits = true;
			} else if (Double.isNaN(x)) {
				if (leadingSign)
					ca4 = "+NaN".toCharArray();
				else if (leadingSpace)
					ca4 = " NaN".toCharArray();
				else
					ca4 = "NaN".toCharArray();
				noDigits = true;
			} else {
				if (!precisionSet)
					precision = defaultDigits;
				if (precision == 0)
					precision = 1;
				int ePos = -1;
				if (conversionCharacter == 'g') {
					sx = eFormatString(x, 'e').trim();
					ePos = sx.indexOf('e');
				} else {
					sx = eFormatString(x, 'E').trim();
					ePos = sx.indexOf('E');
				}
				i = ePos + 1;
				int expon = 0;
				if (sx.charAt(i) == '-') {
					for (++i; i < sx.length(); i++)
						if (sx.charAt(i) != '0')
							break;
					if (i < sx.length())
						expon = -Integer.parseInt(sx.substring(i));
				} else {
					if (sx.charAt(i) == '+')
						++i;
					for (; i < sx.length(); i++)
						if (sx.charAt(i) != '0')
							break;
					if (i < sx.length())
						expon = Integer.parseInt(sx.substring(i));
				}
				// Trim trailing zeros.
				// If the radix character is not followed by
				// a digit, trim it, too.
				if (!alternateForm) {
					if (expon >= -4 && expon < precision)
						sy = fFormatString(x).trim();
					else
						sy = sx.substring(0, ePos);
					i = sy.length() - 1;
					for (; i >= 0; i--)
						if (sy.charAt(i) != '0')
							break;
					if (i >= 0 && sy.charAt(i) == '.')
						i--;
					if (i == -1)
						sz = "0";
					else if (!Character.isDigit(sy.charAt(i)))
						sz = sy.substring(0, i + 1) + "0";
					else
						sz = sy.substring(0, i + 1);
					if (expon >= -4 && expon < precision)
						ret = sz;
					else
						ret = sz + sx.substring(ePos);
				} else {
					if (expon >= -4 && expon < precision)
						ret = fFormatString(x).trim();
					else
						ret = sx;
				}
				// leading space was trimmed off during
				// construction
				if (leadingSpace)
					if (x >= 0)
						ret = " " + ret;
				ca4 = ret.toCharArray();
			}
			// Pad with blanks or zeros.
			ca5 = applyFloatPadding(ca4, false);
			precision = savePrecision;
			return new String(ca5);
		}
		/**
		 * Format method for the d conversion specifer and
		 * short argument.
		 *
		 * For d format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  A '+' character means that the conversion
		 * will always begin with a sign (+ or -).  The
		 * blank flag character means that a non-negative
		 * input will be preceded with a blank.  If both a
		 * '+' and a ' ' are specified, the blank flag is
		 * ignored.  The '0' flag character implies that
		 * padding to the field width will be done with
		 * zeros instead of blanks.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the short to format.
		 * @return the formatted String.
		 */
		private String printDFormat(short x) {
			return printDFormat(Short.toString(x));
		}
		/**
		 * Format method for the d conversion character and
		 * long argument.
		 *
		 * For d format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  A '+' character means that the conversion
		 * will always begin with a sign (+ or -).  The
		 * blank flag character means that a non-negative
		 * input will be preceded with a blank.  If both a
		 * '+' and a ' ' are specified, the blank flag is
		 * ignored.  The '0' flag character implies that
		 * padding to the field width will be done with
		 * zeros instead of blanks.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the long to format.
		 * @return the formatted String.
		 */
		private String printDFormat(long x) {
			return printDFormat(Long.toString(x));
		}
		/**
		 * Format method for the d conversion character and
		 * int argument.
		 *
		 * For d format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  A '+' character means that the conversion
		 * will always begin with a sign (+ or -).  The
		 * blank flag character means that a non-negative
		 * input will be preceded with a blank.  If both a
		 * '+' and a ' ' are specified, the blank flag is
		 * ignored.  The '0' flag character implies that
		 * padding to the field width will be done with
		 * zeros instead of blanks.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the int to format.
		 * @return the formatted String.
		 */
		private String printDFormat(int x) {
			return printDFormat(Integer.toString(x));
		}
		/**
		 * Utility method for formatting using the d
		 * conversion character.
		 * @param sx the String to format, the result of
		 *     converting a short, int, or long to a
		 *     String.
		 * @return the formatted String.
		 */
		private String printDFormat(String sx) {
			int nLeadingZeros = 0;
			int nBlanks = 0, n = 0;
			int i = 0, jFirst = 0;
			boolean neg = sx.charAt(0) == '-';
			if (sx.equals("0") && precisionSet && precision == 0)
				sx = "";
			if (!neg) {
				if (precisionSet && sx.length() < precision)
					nLeadingZeros = precision - sx.length();
			} else {
				if (precisionSet && (sx.length() - 1) < precision)
					nLeadingZeros = precision - sx.length() + 1;
			}
			if (nLeadingZeros < 0)
				nLeadingZeros = 0;
			if (fieldWidthSet) {
				nBlanks = fieldWidth - nLeadingZeros - sx.length();
				if (!neg && (leadingSign || leadingSpace))
					nBlanks--;
			}
			if (nBlanks < 0)
				nBlanks = 0;
			if (leadingSign)
				n++;
			else if (leadingSpace)
				n++;
			n += nBlanks;
			n += nLeadingZeros;
			n += sx.length();
			char[] ca = new char[n];
			if (leftJustify) {
				if (neg)
					ca[i++] = '-';
				else if (leadingSign)
					ca[i++] = '+';
				else if (leadingSpace)
					ca[i++] = ' ';
				char[] csx = sx.toCharArray();
				jFirst = neg ? 1 : 0;
				for (int j = 0; j < nLeadingZeros; i++, j++)
					ca[i] = '0';
				for (int j = jFirst; j < csx.length; j++, i++)
					ca[i] = csx[j];
				for (int j = 0; j < nBlanks; i++, j++)
					ca[i] = ' ';
			} else {
				if (!leadingZeros) {
					for (i = 0; i < nBlanks; i++)
						ca[i] = ' ';
					if (neg)
						ca[i++] = '-';
					else if (leadingSign)
						ca[i++] = '+';
					else if (leadingSpace)
						ca[i++] = ' ';
				} else {
					if (neg)
						ca[i++] = '-';
					else if (leadingSign)
						ca[i++] = '+';
					else if (leadingSpace)
						ca[i++] = ' ';
					for (int j = 0; j < nBlanks; j++, i++)
						ca[i] = '0';
				}
				for (int j = 0; j < nLeadingZeros; j++, i++)
					ca[i] = '0';
				char[] csx = sx.toCharArray();
				jFirst = neg ? 1 : 0;
				for (int j = jFirst; j < csx.length; j++, i++)
					ca[i] = csx[j];
			}
			return new String(ca);
		}
		/**
		 * Format method for the x conversion character and
		 * short argument.
		 *
		 * For x format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  The '#' flag character means to lead with
		 * '0x'.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the short to format.
		 * @return the formatted String.
		 */
		private String printXFormat(short x) {
			String sx = null;
			if (x == Short.MIN_VALUE)
				sx = "8000";
			else if (x < 0) {
				String t;
				if (x == Short.MIN_VALUE)
					t = "0";
				else {
					t = Integer.toString((~(-x - 1)) ^ Short.MIN_VALUE, 16);
					if (t.charAt(0) == 'F' || t.charAt(0) == 'f')
						t = t.substring(16, 32);
				}
				switch (t.length()) {
					case 1 :
						sx = "800" + t;
						break;
					case 2 :
						sx = "80" + t;
						break;
					case 3 :
						sx = "8" + t;
						break;
					case 4 :
						switch (t.charAt(0)) {
							case '1' :
								sx = "9" + t.substring(1, 4);
								break;
							case '2' :
								sx = "a" + t.substring(1, 4);
								break;
							case '3' :
								sx = "b" + t.substring(1, 4);
								break;
							case '4' :
								sx = "c" + t.substring(1, 4);
								break;
							case '5' :
								sx = "d" + t.substring(1, 4);
								break;
							case '6' :
								sx = "e" + t.substring(1, 4);
								break;
							case '7' :
								sx = "f" + t.substring(1, 4);
								break;
						}
						break;
				}
			} else
				sx = Integer.toString((int) x, 16);
			return printXFormat(sx);
		}
		/**
		 * Format method for the x conversion character and
		 * long argument.
		 *
		 * For x format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  The '#' flag character means to lead with
		 * '0x'.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the long to format.
		 * @return the formatted String.
		 */
		private String printXFormat(long x) {
			String sx = null;
			if (x == Long.MIN_VALUE)
				sx = "8000000000000000";
			else if (x < 0) {
				String t = Long.toString((~(-x - 1)) ^ Long.MIN_VALUE, 16);
				switch (t.length()) {
					case 1 :
						sx = "800000000000000" + t;
						break;
					case 2 :
						sx = "80000000000000" + t;
						break;
					case 3 :
						sx = "8000000000000" + t;
						break;
					case 4 :
						sx = "800000000000" + t;
						break;
					case 5 :
						sx = "80000000000" + t;
						break;
					case 6 :
						sx = "8000000000" + t;
						break;
					case 7 :
						sx = "800000000" + t;
						break;
					case 8 :
						sx = "80000000" + t;
						break;
					case 9 :
						sx = "8000000" + t;
						break;
					case 10 :
						sx = "800000" + t;
						break;
					case 11 :
						sx = "80000" + t;
						break;
					case 12 :
						sx = "8000" + t;
						break;
					case 13 :
						sx = "800" + t;
						break;
					case 14 :
						sx = "80" + t;
						break;
					case 15 :
						sx = "8" + t;
						break;
					case 16 :
						switch (t.charAt(0)) {
							case '1' :
								sx = "9" + t.substring(1, 16);
								break;
							case '2' :
								sx = "a" + t.substring(1, 16);
								break;
							case '3' :
								sx = "b" + t.substring(1, 16);
								break;
							case '4' :
								sx = "c" + t.substring(1, 16);
								break;
							case '5' :
								sx = "d" + t.substring(1, 16);
								break;
							case '6' :
								sx = "e" + t.substring(1, 16);
								break;
							case '7' :
								sx = "f" + t.substring(1, 16);
								break;
						}
						break;
				}
			} else
				sx = Long.toString(x, 16);
			return printXFormat(sx);
		}
		/**
		 * Format method for the x conversion character and
		 * int argument.
		 *
		 * For x format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  The '#' flag character means to lead with
		 * '0x'.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the int to format.
		 * @return the formatted String.
		 */
		private String printXFormat(int x) {
			String sx = null;
			if (x == Integer.MIN_VALUE)
				sx = "80000000";
			else if (x < 0) {
				String t = Integer.toString((~(-x - 1)) ^ Integer.MIN_VALUE, 16);
				switch (t.length()) {
					case 1 :
						sx = "8000000" + t;
						break;
					case 2 :
						sx = "800000" + t;
						break;
					case 3 :
						sx = "80000" + t;
						break;
					case 4 :
						sx = "8000" + t;
						break;
					case 5 :
						sx = "800" + t;
						break;
					case 6 :
						sx = "80" + t;
						break;
					case 7 :
						sx = "8" + t;
						break;
					case 8 :
						switch (t.charAt(0)) {
							case '1' :
								sx = "9" + t.substring(1, 8);
								break;
							case '2' :
								sx = "a" + t.substring(1, 8);
								break;
							case '3' :
								sx = "b" + t.substring(1, 8);
								break;
							case '4' :
								sx = "c" + t.substring(1, 8);
								break;
							case '5' :
								sx = "d" + t.substring(1, 8);
								break;
							case '6' :
								sx = "e" + t.substring(1, 8);
								break;
							case '7' :
								sx = "f" + t.substring(1, 8);
								break;
						}
						break;
				}
			} else
				sx = Integer.toString(x, 16);
			return printXFormat(sx);
		}
		/**
		 * Utility method for formatting using the x
		 * conversion character.
		 * @param sx the String to format, the result of
		 *     converting a short, int, or long to a
		 *     String.
		 * @return the formatted String.
		 */
		private String printXFormat(String sx) {
			int nLeadingZeros = 0;
			int nBlanks = 0;
			if (sx.equals("0") && precisionSet && precision == 0)
				sx = "";
			if (precisionSet)
				nLeadingZeros = precision - sx.length();
			if (nLeadingZeros < 0)
				nLeadingZeros = 0;
			if (fieldWidthSet) {
				nBlanks = fieldWidth - nLeadingZeros - sx.length();
				if (alternateForm)
					nBlanks = nBlanks - 2;
			}
			if (nBlanks < 0)
				nBlanks = 0;
			int n = 0;
			if (alternateForm)
				n += 2;
			n += nLeadingZeros;
			n += sx.length();
			n += nBlanks;
			char[] ca = new char[n];
			int i = 0;
			if (leftJustify) {
				if (alternateForm) {
					ca[i++] = '0';
					ca[i++] = 'x';
				}
				for (int j = 0; j < nLeadingZeros; j++, i++)
					ca[i] = '0';
				char[] csx = sx.toCharArray();
				for (int j = 0; j < csx.length; j++, i++)
					ca[i] = csx[j];
				for (int j = 0; j < nBlanks; j++, i++)
					ca[i] = ' ';
			} else {
				if (!leadingZeros)
					for (int j = 0; j < nBlanks; j++, i++)
						ca[i] = ' ';
				if (alternateForm) {
					ca[i++] = '0';
					ca[i++] = 'x';
				}
				if (leadingZeros)
					for (int j = 0; j < nBlanks; j++, i++)
						ca[i] = '0';
				for (int j = 0; j < nLeadingZeros; j++, i++)
					ca[i] = '0';
				char[] csx = sx.toCharArray();
				for (int j = 0; j < csx.length; j++, i++)
					ca[i] = csx[j];
			}
			String caReturn = new String(ca);
			if (conversionCharacter == 'X')
				caReturn = caReturn.toUpperCase();
			return caReturn;
		}
		/**
		 * Format method for the o conversion character and
		 * short argument.
		 *
		 * For o format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the 
		 * left.  The '#' flag character means that the
		 * output begins with a leading 0 and the precision
		 * is increased by 1.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the short to format.
		 * @return the formatted String.
		 */
		private String printOFormat(short x) {
			String sx = null;
			if (x == Short.MIN_VALUE)
				sx = "100000";
			else if (x < 0) {
				String t = Integer.toString((~(-x - 1)) ^ Short.MIN_VALUE, 8);
				switch (t.length()) {
					case 1 :
						sx = "10000" + t;
						break;
					case 2 :
						sx = "1000" + t;
						break;
					case 3 :
						sx = "100" + t;
						break;
					case 4 :
						sx = "10" + t;
						break;
					case 5 :
						sx = "1" + t;
						break;
				}
			} else
				sx = Integer.toString((int) x, 8);
			return printOFormat(sx);
		}
		/**
		 * Format method for the o conversion character and
		 * long argument.
		 *
		 * For o format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the 
		 * left.  The '#' flag character means that the
		 * output begins with a leading 0 and the precision
		 * is increased by 1.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the long to format.
		 * @return the formatted String.
		 */
		private String printOFormat(long x) {
			String sx = null;
			if (x == Long.MIN_VALUE)
				sx = "1000000000000000000000";
			else if (x < 0) {
				String t = Long.toString((~(-x - 1)) ^ Long.MIN_VALUE, 8);
				switch (t.length()) {
					case 1 :
						sx = "100000000000000000000" + t;
						break;
					case 2 :
						sx = "10000000000000000000" + t;
						break;
					case 3 :
						sx = "1000000000000000000" + t;
						break;
					case 4 :
						sx = "100000000000000000" + t;
						break;
					case 5 :
						sx = "10000000000000000" + t;
						break;
					case 6 :
						sx = "1000000000000000" + t;
						break;
					case 7 :
						sx = "100000000000000" + t;
						break;
					case 8 :
						sx = "10000000000000" + t;
						break;
					case 9 :
						sx = "1000000000000" + t;
						break;
					case 10 :
						sx = "100000000000" + t;
						break;
					case 11 :
						sx = "10000000000" + t;
						break;
					case 12 :
						sx = "1000000000" + t;
						break;
					case 13 :
						sx = "100000000" + t;
						break;
					case 14 :
						sx = "10000000" + t;
						break;
					case 15 :
						sx = "1000000" + t;
						break;
					case 16 :
						sx = "100000" + t;
						break;
					case 17 :
						sx = "10000" + t;
						break;
					case 18 :
						sx = "1000" + t;
						break;
					case 19 :
						sx = "100" + t;
						break;
					case 20 :
						sx = "10" + t;
						break;
					case 21 :
						sx = "1" + t;
						break;
				}
			} else
				sx = Long.toString(x, 8);
			return printOFormat(sx);
		}
		/**
		 * Format method for the o conversion character and
		 * int argument.
		 *
		 * For o format, the flag character '-', means that
		 * the output should be left justified within the
		 * field.  The default is to pad with blanks on the
		 * left.  The '#' flag character means that the
		 * output begins with a leading 0 and the precision
		 * is increased by 1.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is to
		 * add no padding.  Padding is with blanks by
		 * default.
		 *
		 * The precision, if set, is the minimum number of
		 * digits to appear.  Padding is with leading 0s.
		 * @param x the int to format.
		 * @return the formatted String.
		 */
		private String printOFormat(int x) {
			String sx = null;
			if (x == Integer.MIN_VALUE)
				sx = "20000000000";
			else if (x < 0) {
				String t = Integer.toString((~(-x - 1)) ^ Integer.MIN_VALUE, 8);
				switch (t.length()) {
					case 1 :
						sx = "2000000000" + t;
						break;
					case 2 :
						sx = "200000000" + t;
						break;
					case 3 :
						sx = "20000000" + t;
						break;
					case 4 :
						sx = "2000000" + t;
						break;
					case 5 :
						sx = "200000" + t;
						break;
					case 6 :
						sx = "20000" + t;
						break;
					case 7 :
						sx = "2000" + t;
						break;
					case 8 :
						sx = "200" + t;
						break;
					case 9 :
						sx = "20" + t;
						break;
					case 10 :
						sx = "2" + t;
						break;
					case 11 :
						sx = "3" + t.substring(1);
						break;
				}
			} else
				sx = Integer.toString(x, 8);
			return printOFormat(sx);
		}
		/**
		 * Utility method for formatting using the o
		 * conversion character.
		 * @param sx the String to format, the result of
		 *     converting a short, int, or long to a
		 *     String.
		 * @return the formatted String.
		 */
		private String printOFormat(String sx) {
			int nLeadingZeros = 0;
			int nBlanks = 0;
			if (sx.equals("0") && precisionSet && precision == 0)
				sx = "";
			if (precisionSet)
				nLeadingZeros = precision - sx.length();
			if (alternateForm)
				nLeadingZeros++;
			if (nLeadingZeros < 0)
				nLeadingZeros = 0;
			if (fieldWidthSet)
				nBlanks = fieldWidth - nLeadingZeros - sx.length();
			if (nBlanks < 0)
				nBlanks = 0;
			int n = nLeadingZeros + sx.length() + nBlanks;
			char[] ca = new char[n];
			int i;
			if (leftJustify) {
				for (i = 0; i < nLeadingZeros; i++)
					ca[i] = '0';
				char[] csx = sx.toCharArray();
				for (int j = 0; j < csx.length; j++, i++)
					ca[i] = csx[j];
				for (int j = 0; j < nBlanks; j++, i++)
					ca[i] = ' ';
			} else {
				if (leadingZeros)
					for (i = 0; i < nBlanks; i++)
						ca[i] = '0';
				else
					for (i = 0; i < nBlanks; i++)
						ca[i] = ' ';
				for (int j = 0; j < nLeadingZeros; j++, i++)
					ca[i] = '0';
				char[] csx = sx.toCharArray();
				for (int j = 0; j < csx.length; j++, i++)
					ca[i] = csx[j];
			}
			return new String(ca);
		}
		/**
		 * Format method for the c conversion character and
		 * char argument.
		 *
		 * The only flag character that affects c format is
		 * the '-', meaning that the output should be left
		 * justified within the field.  The default is to
		 * pad with blanks on the left.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  Padding is with
		 * blanks by default.  The default width is 1.
		 *
		 * The precision, if set, is ignored.
		 * @param x the char to format.
		 * @return the formatted String.
		 */
		private String printCFormat(char x) {
			int nPrint = 1;
			int width = fieldWidth;
			if (!fieldWidthSet)
				width = nPrint;
			char[] ca = new char[width];
			int i = 0;
			if (leftJustify) {
				ca[0] = x;
				for (i = 1; i <= width - nPrint; i++)
					ca[i] = ' ';
			} else {
				for (i = 0; i < width - nPrint; i++)
					ca[i] = ' ';
				ca[i] = x;
			}
			return new String(ca);
		}
		/**
		 * Format method for the s conversion character and
		 * String argument.
		 *
		 * The only flag character that affects s format is
		 * the '-', meaning that the output should be left
		 * justified within the field.  The default is to
		 * pad with blanks on the left.
		 *
		 * The field width is treated as the minimum number
		 * of characters to be printed.  The default is the
		 * smaller of the number of characters in the the
		 * input and the precision.  Padding is with blanks
		 * by default.
		 *
		 * The precision, if set, specifies the maximum
		 * number of characters to be printed from the
		 * string.  A null digit string is treated
		 * as a 0.  The default is not to set a maximum
		 * number of characters to be printed.
		 * @param x the String to format.
		 * @return the formatted String.
		 */
		private String printSFormat(String x) {
			int nPrint = x.length();
			int width = fieldWidth;
			if (precisionSet && nPrint > precision)
				nPrint = precision;
			if (!fieldWidthSet)
				width = nPrint;
			int n = 0;
			if (width > nPrint)
				n += width - nPrint;
			if (nPrint >= x.length())
				n += x.length();
			else
				n += nPrint;
			char[] ca = new char[n];
			int i = 0;
			if (leftJustify) {
				if (nPrint >= x.length()) {
					char[] csx = x.toCharArray();
					for (i = 0; i < x.length(); i++)
						ca[i] = csx[i];
				} else {
					char[] csx = x.substring(0, nPrint).toCharArray();
					for (i = 0; i < nPrint; i++)
						ca[i] = csx[i];
				}
				for (int j = 0; j < width - nPrint; j++, i++)
					ca[i] = ' ';
			} else {
				for (i = 0; i < width - nPrint; i++)
					ca[i] = ' ';
				if (nPrint >= x.length()) {
					char[] csx = x.toCharArray();
					for (int j = 0; j < x.length(); i++, j++)
						ca[i] = csx[j];
				} else {
					char[] csx = x.substring(0, nPrint).toCharArray();
					for (int j = 0; j < nPrint; i++, j++)
						ca[i] = csx[j];
				}
			}
			return new String(ca);
		}
		/**
		 * Check for a conversion character.  If it is
		 * there, store it.
		 * @param x the String to format.
		 * @return <code>true</code> if the conversion
		 *     character is there, and
		 *     <code>false</code> otherwise.
		 */
		private boolean setConversionCharacter() {
			/* idfgGoxXeEcs */
			boolean ret = false;
			conversionCharacter = '\0';
			if (pos < fmt.length()) {
				char c = fmt.charAt(pos);
				if (c == 'i'
					|| c == 'd'
					|| c == 'f'
					|| c == 'g'
					|| c == 'G'
					|| c == 'o'
					|| c == 'x'
					|| c == 'X'
					|| c == 'e'
					|| c == 'E'
					|| c == 'c'
					|| c == 's'
					|| c == '%') {
					conversionCharacter = c;
					pos++;
					ret = true;
				}
			}
			return ret;
		}
		/**
		 * Check for an h, l, or L in a format.  An L is
		 * used to control the minimum number of digits
		 * in an exponent when using floating point
		 * formats.  An l or h is used to control
		 * conversion of the input to a long or short,
		 * respectively, before formatting.  If any of
		 * these is present, store them.
		 */
		private void setOptionalHL() {
			optionalh = false;
			optionall = false;
			optionalL = false;
			if (pos < fmt.length()) {
				char c = fmt.charAt(pos);
				if (c == 'h') {
					optionalh = true;
					pos++;
				} else if (c == 'l') {
					optionall = true;
					pos++;
				} else if (c == 'L') {
					optionalL = true;
					pos++;
				}
			}
		}
		/**
		 * Set the precision.
		 */
		private void setPrecision() {
			int firstPos = pos;
			precisionSet = false;
			if (pos < fmt.length() && fmt.charAt(pos) == '.') {
				pos++;
				if ((pos < fmt.length()) && (fmt.charAt(pos) == '*')) {
					pos++;
					if (!setPrecisionArgPosition()) {
						variablePrecision = true;
						precisionSet = true;
					}
					return;
				} else {
					while (pos < fmt.length()) {
						char c = fmt.charAt(pos);
						if (Character.isDigit(c))
							pos++;
						else
							break;
					}
					if (pos > firstPos + 1) {
						String sz = fmt.substring(firstPos + 1, pos);
						precision = Integer.parseInt(sz);
						precisionSet = true;
					}
				}
			}
		}
		/**
		 * Set the field width.
		 */
		private void setFieldWidth() {
			int firstPos = pos;
			fieldWidth = 0;
			fieldWidthSet = false;
			if ((pos < fmt.length()) && (fmt.charAt(pos) == '*')) {
				pos++;
				if (!setFieldWidthArgPosition()) {
					variableFieldWidth = true;
					fieldWidthSet = true;
				}
			} else {
				while (pos < fmt.length()) {
					char c = fmt.charAt(pos);
					if (Character.isDigit(c))
						pos++;
					else
						break;
				}
				if (firstPos < pos && firstPos < fmt.length()) {
					String sz = fmt.substring(firstPos, pos);
					fieldWidth = Integer.parseInt(sz);
					fieldWidthSet = true;
				}
			}
		}
		/**
		 * Store the digits <code>n</code> in %n$ forms.
		 */
		private void setArgPosition() {
			int xPos;
			for (xPos = pos; xPos < fmt.length(); xPos++) {
				if (!Character.isDigit(fmt.charAt(xPos)))
					break;
			}
			if (xPos > pos && xPos < fmt.length()) {
				if (fmt.charAt(xPos) == '$') {
					positionalSpecification = true;
					argumentPosition = Integer.parseInt(fmt.substring(pos, xPos));
					pos = xPos + 1;
				}
			}
		}
		/**
		 * Store the digits <code>n</code> in *n$ forms.
		 */
		private boolean setFieldWidthArgPosition() {
			boolean ret = false;
			int xPos;
			for (xPos = pos; xPos < fmt.length(); xPos++) {
				if (!Character.isDigit(fmt.charAt(xPos)))
					break;
			}
			if (xPos > pos && xPos < fmt.length()) {
				if (fmt.charAt(xPos) == '$') {
					positionalFieldWidth = true;
					argumentPositionForFieldWidth = Integer.parseInt(fmt.substring(pos, xPos));
					pos = xPos + 1;
					ret = true;
				}
			}
			return ret;
		}
		/**
		 * Store the digits <code>n</code> in *n$ forms.
		 */
		private boolean setPrecisionArgPosition() {
			boolean ret = false;
			int xPos;
			for (xPos = pos; xPos < fmt.length(); xPos++) {
				if (!Character.isDigit(fmt.charAt(xPos)))
					break;
			}
			if (xPos > pos && xPos < fmt.length()) {
				if (fmt.charAt(xPos) == '$') {
					positionalPrecision = true;
					argumentPositionForPrecision = Integer.parseInt(fmt.substring(pos, xPos));
					pos = xPos + 1;
					ret = true;
				}
			}
			return ret;
		}
		boolean isPositionalSpecification() {
			return positionalSpecification;
		}
		int getArgumentPosition() {
			return argumentPosition;
		}
		boolean isPositionalFieldWidth() {
			return positionalFieldWidth;
		}
		int getArgumentPositionForFieldWidth() {
			return argumentPositionForFieldWidth;
		}
		boolean isPositionalPrecision() {
			return positionalPrecision;
		}
		int getArgumentPositionForPrecision() {
			return argumentPositionForPrecision;
		}
		/**
		 * Set flag characters, one of '-+#0 or a space.
		 */
		private void setFlagCharacters() {
			/* '-+ #0 */
			thousands = false;
			leftJustify = false;
			leadingSign = false;
			leadingSpace = false;
			alternateForm = false;
			leadingZeros = false;
			for (; pos < fmt.length(); pos++) {
				char c = fmt.charAt(pos);
				if (c == '\'')
					thousands = true;
				else if (c == '-') {
					leftJustify = true;
					leadingZeros = false;
				} else if (c == '+') {
					leadingSign = true;
					leadingSpace = false;
				} else if (c == ' ') {
					if (!leadingSign)
						leadingSpace = true;
				} else if (c == '#')
					alternateForm = true;
				else if (c == '0') {
					if (!leftJustify)
						leadingZeros = true;
				} else
					break;
			}
		}
		/**
		 * The integer portion of the result of a decimal
		 * conversion (i, d, u, f, g, or G) will be
		 * formatted with thousands' grouping characters.
		 * For other conversions the flag is ignored.
		 */
		private boolean thousands = false;
		/**
		 * The result of the conversion will be
		 * left-justified within the field.
		 */
		private boolean leftJustify = false;
		/**
		 * The result of a signed conversion will always
		 * begin with a sign (+ or -).
		 */
		private boolean leadingSign = false;
		/**
		 * Flag indicating that left padding with spaces is
		 * specified.
		 */
		private boolean leadingSpace = false;
		/**
		 * For an o conversion, increase the precision to
		 * force the first digit of the result to be a
		 * zero.  For x (or X) conversions, a non-zero
		 * result will have 0x (or 0X) prepended to it.
		 * For e, E, f, g, or G conversions, the result
		 * will always contain a radix character, even if
		 * no digits follow the point.  For g and G
		 * conversions, trailing zeros will not be removed
		 * from the result.
		 */
		private boolean alternateForm = false;
		/**
		 * Flag indicating that left padding with zeroes is
		 * specified.
		 */
		private boolean leadingZeros = false;
		/**
		 * Flag indicating that the field width is *.
		 */
		private boolean variableFieldWidth = false;
		/**
		 * If the converted value has fewer bytes than the
		 * field width, it will be padded with spaces or
		 * zeroes.
		 */
		private int fieldWidth = 0;
		/**
		 * Flag indicating whether or not the field width
		 * has been set.
		 */
		private boolean fieldWidthSet = false;
		/**
		 * The minimum number of digits to appear for the
		 * d, i, o, u, x, or X conversions.  The number of
		 * digits to appear after the radix character for
		 * the e, E, and f conversions.  The maximum number
		 *  of significant digits for the g and G 
		 * conversions.  The maximum number of bytes to be
		 * printed from a string in s and S conversions.
		 */
		private int precision = 0;
		/** Default precision. */
		private final static int defaultDigits = 6;
		/**
		 * Flag indicating that the precision is *.
		 */
		private boolean variablePrecision = false;
		/**
		 * Flag indicating whether or not the precision has
		 * been set.
		 */
		private boolean precisionSet = false;
		/*
		 */
		private boolean positionalSpecification = false;
		private int argumentPosition = 0;
		private boolean positionalFieldWidth = false;
		private int argumentPositionForFieldWidth = 0;
		private boolean positionalPrecision = false;
		private int argumentPositionForPrecision = 0;
		/**
		 * Flag specifying that a following d, i, o, u, x,
		 * or X conversion character applies to a type
		 * short int.
		 */
		private boolean optionalh = false;
		/**
		 * Flag specifying that a following d, i, o, u, x,
		 * or X conversion character applies to a type lont
		 * int argument.
		 */
		private boolean optionall = false;
		/**
		 * Flag specifying that a following e, E, f, g, or
		 * G conversion character applies to a type double
		 * argument.  This is a noop in Java.
		 */
		private boolean optionalL = false;
		/** Control string type. */
		private char conversionCharacter = '\0';
		/**
		 * Position within the control string.  Used by
		 * the constructor.
		 */
		private int pos = 0;
		/** Literal or control format string. */
		private String fmt;
	}
	/** Vector of control strings and format literals. */
	private Vector vFmt = new Vector();
	/** Character position.  Used by the constructor. */
	private int cPos = 0;
	/** Character position.  Used by the constructor. */
	private DecimalFormatSymbols dfs = null;
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

