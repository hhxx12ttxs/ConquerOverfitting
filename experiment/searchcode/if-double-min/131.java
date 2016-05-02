<<<<<<< HEAD
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.StringTokenizer;


//FIX getColor()
public class Triangle {
	//Variables
	//Original X coordinates of the triangle
	double[] xs = new double[3];
	//Original Y coordinates of the triangle
	double[] ys = new double[3];
	//Original Z coordinates of the triangle
	double[] zs = new double[3];
	//color of v0
	double[] v0Color = new double[3];
	//color of v1
	double[] v1Color = new double[3];
	//color of v2
	double[] v2Color = new double[3];
	//Transformed X coordinates
	double[] xT = new double[3];
	//Transformed Y coordinates
	double[] yT = new double[3];
	//Transformed Z coordinates
	double[] zT = new double[3];
	//Shaded color of v0
	double[] v0ColorPrime = new double[3];
	//Shaded color of v1
	double[] v1ColorPrime = new double[3];
	//Shaded color of v2
	double[] v2ColorPrime = new double[3];
	//Z component of normal
	double d;
	
	//Constructor
	public Triangle(String line){
		//Loop for parsing the given line.
		StringTokenizer st = new StringTokenizer(line);
		int i = 0;
		int j = 0;
		while(st.hasMoreTokens()){
			if(i <= 8){
				if(i % 3 == 0){
					this.xs[j] = Double.parseDouble(st.nextToken());
				} else if (i % 3 == 1){
					this.ys[j] = Double.parseDouble(st.nextToken());
				} else if (i % 3 == 2){
					this.zs[j] = Double.parseDouble(st.nextToken());
					j++;
				}
			} else {
				if (i >= 9 && i <= 11){
					this.v0Color[i % 9] = Double.parseDouble(st.nextToken());
				} else if (i >= 12 && i <= 14){
					this.v1Color[i % 12] = Double.parseDouble(st.nextToken());
				} else if (i >= 15 && i <= 17){
					this.v2Color[i % 15] = Double.parseDouble(st.nextToken());
				}
			}
			i++;
		}
	}
	
	//Computes f01
	public double computef01(double x, double y){
		return ((yT[0] - yT[1]) * x) + ((xT[1] - xT[0]) * y) + xT[0]*yT[1] - xT[1]*yT[0];
	}
	//Computes f12
	public double computef12(double x, double y){
		return ((yT[1] - yT[2]) * x) + ((xT[2] - xT[1]) * y) + xT[1]*yT[2] - xT[2]*yT[1];
	}
	//Computes f20
	public double computef20(double x, double y){
		return ((yT[2] - yT[0]) * x) + ((xT[0] - xT[2]) * y) + xT[2]*yT[0] - xT[0]*yT[2];
	}
	
	//Applies the transform to the original points
	public void applyTransforms(double[] transform){
		
		xT[0] = transform[0] * xs[0] + transform[1] * ys[0] + transform[2] * zs[0] + transform[3];
		yT[0] = transform[4] * xs[0] + transform[5] * ys[0] + transform[6] * zs[0] + transform[7];
		zT[0] = transform[8] * xs[0] + transform[9] * ys[0] + transform[10] * zs[0] + transform[11];
		
		xT[1] = transform[0] * xs[1] + transform[1] * ys[1] + transform[2] * zs[1] + transform[3];
		yT[1] = transform[4] * xs[1] + transform[5] * ys[1] + transform[6] * zs[1] + transform[7];
		zT[1] = transform[8] * xs[1] + transform[9] * ys[1] + transform[10] * zs[1] + transform[11];
		
		xT[2] = transform[0] * xs[2] + transform[1] * ys[2] + transform[2] * zs[2] + transform[3];
		yT[2] = transform[4] * xs[2] + transform[5] * ys[2] + transform[6] * zs[2] + transform[7];
		zT[2] = transform[8] * xs[2] + transform[9] * ys[2] + transform[10] * zs[2] + transform[11];
		
		
	}
	
	//finds the highest X coordinate of the transformed X coordinates
	public double findMaxX(){
		double max = 0.0f;
		for(int i = 0; i < xT.length; i++){
			if(xT[i] > max)
				max = xT[i];
		}
		return max;
	}
	
	//finds the highest Y coordinate of the transformed Y coordinates
	public double findMaxY(){
		double max = 0.0f;
		for(int i = 0; i < yT.length; i++){
			if(yT[i] > max)
				max = yT[i];
		}
		return max;
	}
	
	//finds the lowest X coordinate of the transformed X coordinates
	public double findMinX(){
		double min = xT[0];
		for(int i = 0; i < xT.length; i++){
			if(xT[i] < min)
				min = xT[i];
		}
		return min;
	}
	
	//finds the highest Y coordinate of the transformed Y coordinates
	public double findMinY(){
		double min = yT[0];
		for(int i = 0; i < yT.length; i++){
			if(yT[i] < min)
				min = yT[i];
		}
		return min;
	}
	
	//finds the lowest Z coordinate of the transform Z coordinates
	public double findMinZ(){
		double min = zT[0];
		for(int i = 0; i < zT.length; i++){
			if(zT[i] < min)
				min = zT[i];
		}
		return min;
	}

	//checks whether or not the triangle is backfacing(verts are CW)
	public boolean isBackfacing(){
		boolean isBackfacing;
		double[] p1 = new double[3];
		double[] p2 = new double[3];
		p1[0] = xT[1] - xT[0];
		p1[1] = yT[1] - yT[0];
		p1[2] = zT[1] - zT[0];
		p2[0] = xT[2] - xT[0];
		p2[1] = yT[2] - yT[0];
		p2[2] = zT[2] - zT[0];
		double[] cross = cross(p1, p2);
		double magnitude = magnitude(cross);
		double[] nhat = new double[3];
		nhat[0] = cross[0] / magnitude;
		nhat[1] = cross[1] / magnitude;
		nhat[2] = -cross[2] / magnitude;
		if(nhat[2] <= 0){
			this.d = nhat[2];
			//System.out.println(nhat[2]);
			isBackfacing = true;
		} else {
			this.d = nhat[2];
			//System.out.println(nhat[2]);
			isBackfacing = false;
		}
		return isBackfacing;
	}
	
	//Computes the cross product of the two arrays
	public double[] cross(double[] p1, double[] p2){
		double[] result = new double[3];
		result[0] = p1[1] * p2[2] - p1[2] * p2[1];
		result[1] = p1[2] * p2[0] - p1[0] * p2[2];
		result[2] = p1[0] * p2[1] - p1[1] * p2[0];
		return result;
	}
	
	//Takes the dot product of two arrays
	public double dot(double[] p1, double[] p2){
		return p1[0] * p2[0] + p1[1] * p2[1] + p1[2] * p2[2];
	}
	
	//Takes the magnitude of an array
	public double magnitude(double[] p1){
		return Math.sqrt((p1[0] * p1[0]) + (p1[1] * p1[1]) + (p1[2] * p1[2])); 
	}
	
	//calculates the color of the point based on the three vert colors
	public Color getColor(double[] bary){
		return new Color((float)(this.v0Color[0]*bary[0] + this.v1Color[0]*bary[1] + this.v2Color[0]*bary[2]), 
						 (float)(this.v0Color[1]*bary[0] + this.v1Color[1]*bary[1] + this.v2Color[1]*bary[2]),
						 (float)(this.v0Color[2]*bary[0] + this.v1Color[2]*bary[1] + this.v2Color[2]*bary[2]));
						 
	}
	
	//multiplies the vert colors by the z component of the normal to shade them
	public void computePrimeColors(){
		double newV0CP0 = this.d * this.v0Color[0];
		double newV0CP1 = this.d * this.v0Color[1];
		double newV0CP2 = this.d * this.v0Color[2];
		
		double newV1CP0 = this.d * this.v1Color[0];
		double newV1CP1 = this.d * this.v1Color[1];
		double newV1CP2 = this.d * this.v1Color[2];
		
		double newV2CP0 = this.d * this.v2Color[0];
		double newV2CP1 = this.d * this.v2Color[1];
		double newV2CP2 = this.d * this.v2Color[2];
		
		v0ColorPrime[0] = newV0CP0;
		v0ColorPrime[1] = newV0CP1;
		v0ColorPrime[2] = newV0CP2;
		
		v1ColorPrime[0] = newV1CP0;
		v1ColorPrime[1] = newV1CP1;
		v1ColorPrime[2] = newV1CP2;
		
		v2ColorPrime[0] = newV2CP0;
		v2ColorPrime[1] = newV2CP1;
		v2ColorPrime[2] = newV2CP2;
	}
	
	//calculates the color of the point based on the shaded vert colors
	public Color getShadedColor(double[] bary){
		float r = (float) (this.v0ColorPrime[0]*bary[0] + this.v1ColorPrime[0]*bary[1] + this.v2ColorPrime[0]*bary[2]);
		float g = (float) (this.v0ColorPrime[1]*bary[0] + this.v1ColorPrime[1]*bary[1] + this.v2ColorPrime[1]*bary[2]);
		float b = (float) (this.v0ColorPrime[2]*bary[0] + this.v1ColorPrime[2]*bary[1] + this.v2ColorPrime[2]*bary[2]);
		if(r < 0)
			r = 0;
		if(r > 1)
			r = 1;
		if(g < 0)
			g = 0;
		if(g > 1)
			g = 1;
		if(b < 0)
			b = 0;
		if(b > 1)
			b = 1;
		return new Color(r, g, b);
	}
	
	public float getInterpZ (double[] bary){
		return (float) (this.zT[0]*bary[0] + this.zT[1]*bary[1] + this.zT[2]*bary[2]);
						 
	}
	
}
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
 * FloatVectorIndividual.java
 * Created: Thu Mar 22 13:13:20 EST 2001
 */

/**
 * FloatVectorIndividual is a VectorIndividual whose genome is an array of
 * floats. Gene values may range from species.mingene(x) to species.maxgene(x),
 * inclusive. The default mutation method randomizes genes to new values in this
 * range, with <tt>species.mutationProbability</tt>. It can also add gaussian
 * noise to the genes, if so directed in the FloatVectorSpecies. If the gaussian
 * noise pushes the gene out of range, a new noise value is generated.
 * 
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
 * vector.float-vect-ind
 * 
 * @author Liviu Panait
 * @version 2.0
 */

public class FloatVectorIndividual extends VectorIndividual {
    public static final String P_FLOATVECTORINDIVIDUAL = "float-vect-ind";

    public float[] genome;

    public Parameter defaultBase() {
        return VectorDefaults.base().push(P_FLOATVECTORINDIVIDUAL);
    }

    public Object clone() {
        FloatVectorIndividual myobj = (FloatVectorIndividual) (super.clone());

        // must clone the genome
        myobj.genome = (float[]) (genome.clone());

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
            state.output.fatal("FloatVectorIndividual requires an FloatVectorSpecies", base, def);
        FloatVectorSpecies s = (FloatVectorSpecies) species;

        genome = new float[s.genomeSize];
    }

    public void defaultCrossover(EvolutionState state, int thread, VectorIndividual ind) {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        FloatVectorIndividual i = (FloatVectorIndividual) ind;
        float tmp;
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
                    genome[x] = (float) t;
                    i.genome[x] = (float) u;
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
                genome[x] = (float) t;
                i.genome[x] = (float) u;
            }
        }
            break;
        case VectorSpecies.C_SIMULATED_BINARY: {
            simulatedBinaryCrossover(state.random[thread], i, s.crossoverDistributionIndex);
        }
            break;
        }
    }

    public void simulatedBinaryCrossover(MersenneTwisterFast random, FloatVectorIndividual other, double eta_c) {
        final double EPS = FloatVectorSpecies.SIMULATED_BINARY_CROSSOVER_EPS;
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        float[] parent1 = genome;
        float[] parent2 = other.genome;
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
                        parent1[i] = (float) c2;
                        parent2[i] = (float) c1;
                    } else {
                        parent1[i] = (float) c1;
                        parent2[i] = (float) c2;
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
     * Splits the genome into n pieces, according to points, which *must* be
     * sorted. pieces.length must be 1 + points.length
     */
    public void split(int[] points, Object[] pieces) {
        int point0, point1;
        point0 = 0;
        point1 = points[0];
        for (int x = 0; x < pieces.length; x++) {
            pieces[x] = new float[point1 - point0];
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
            sum += ((float[]) (pieces[x])).length;

        int runningsum = 0;
        float[] newgenome = new float[sum];
        for (int x = 0; x < pieces.length; x++) {
            System.arraycopy(pieces[x], 0, newgenome, runningsum, ((float[]) (pieces[x])).length);
            runningsum += ((float[]) (pieces[x])).length;
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
     * * @author Liviu Panait and Gabriel Balan
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
                    float val;
                    float min = (float) s.minGene(x);
                    float max = (float) s.maxGene(x);
                    float stdev = (float) s.gaussMutationStdev;
                    int outOfBoundsLeftOverTries = s.outOfBoundsRetries;
                    boolean givingUpAllowed = s.outOfBoundsRetries != 0;
                    do {
                        val = (float) (rng.nextGaussian() * stdev + genome[x]);
                        outOfBoundsLeftOverTries--;
                        if (mutationIsBounded && (val > max || val < min)) {
                            if (givingUpAllowed && (outOfBoundsLeftOverTries == 0)) {
                                val = (float) (min + rng.nextFloat() * (max - min));
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
            polynomialMutate(state.random[thread], this, s.mutationDistributionIndex, s.polynomialIsAlternative,
                    s.mutationIsBounded);
        } else {// C_RESET_MUTATION
            for (int x = 0; x < genome.length; x++)
                if (rng.nextBoolean(s.mutationProbability))
                    genome[x] = (float) ((float) s.minGene(x) + rng.nextFloat()
                            * ((float) s.maxGene(x) - (float) s.minGene(x)));
        }

    }

    /**
     * This function is broken out to keep it identical to NSGA-II's mutation.c
     * code. eta_m is the distribution index.
     */
    public void polynomialMutate(MersenneTwisterFast random, FloatVectorIndividual individual, double eta_m,
            boolean alternativePolynomialVersion, boolean mutationIsBounded) {
        FloatVectorSpecies s = (FloatVectorSpecies) individual.species;
        float[] ind = individual.genome;
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
                    y1 = (float) (min_realvar[j] + random.nextFloat() * (max_realvar[j] - min_realvar[j]));
                }
                ind[j] = (float) y1;
            }
        }

    }

    /**
     * Initializes the individual by randomly choosing floats uniformly from
     * mingene to maxgene.
     */
    public void reset(EvolutionState state, int thread) {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        for (int x = 0; x < genome.length; x++)
            genome[x] = (float) ((float) s.minGene(x) + state.random[thread].nextFloat()
                    * ((float) s.maxGene(x) - (float) s.minGene(x)));
    }

    public int hashCode() {
        // stolen from GPIndividual. It's a decent algorithm.
        int hash = this.getClass().hashCode();

        hash = (hash << 1 | hash >>> 31);
        for (int x = 0; x < genome.length; x++)
            hash = (hash << 1 | hash >>> 31) ^ Float.floatToIntBits(genome[x]);

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

        genome = new float[lll];

        // read in the genes
        for (int i = 0; i < genome.length; i++) {
            Code.decode(d);
            genome[i] = (float) (d.d);
        }
    }

    public boolean equals(Object ind) {
        if (!(this.getClass().equals(ind.getClass())))
            return false; // SimpleRuleIndividuals are special.
        FloatVectorIndividual i = (FloatVectorIndividual) ind;
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
        genome = (float[]) gen;
    }

    public int genomeLength() {
        return genome.length;
    }

    public void writeGenotype(final EvolutionState state, final DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(genome.length);
        for (int x = 0; x < genome.length; x++)
            dataOutput.writeFloat(genome[x]);
    }

    public void readGenotype(final EvolutionState state, final DataInput dataInput) throws IOException {
        int len = dataInput.readInt();
        if (genome == null || genome.length != len)
            genome = new float[len];
        for (int x = 0; x < genome.length; x++)
            genome[x] = dataInput.readFloat();
    }

    /**
     * Clips each gene value to be within its specified [min,max] range. NaN is
     * presently considered in range but the behavior of this method should be
     * assumed to be unspecified on encountering NaN.
     */
    public void clamp() {
        FloatVectorSpecies _species = (FloatVectorSpecies) species;
        for (int i = 0; i < genomeLength(); i++) {
            float minGene = (float) _species.minGene(i);
            if (genome[i] < minGene)
                genome[i] = minGene;
            else {
                float maxGene = (float) _species.maxGene(i);
                if (genome[i] > maxGene)
                    genome[i] = maxGene;
            }
        }
    }

    public void setGenomeLength(int len) {
        float[] newGenome = new float[len];
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
        if (!(otherInd instanceof FloatVectorIndividual))
            return super.distanceTo(otherInd); // will return infinity!

        FloatVectorIndividual other = (FloatVectorIndividual) otherInd;
        float[] otherGenome = other.genome;
        double sumSquaredDistance = 0.0;
        for (int i = 0; i < other.genomeLength(); i++) {
            double dist = this.genome[i] - otherGenome[i];
            sumSquaredDistance += dist * dist;
        }
        return StrictMath.sqrt(sumSquaredDistance);
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

