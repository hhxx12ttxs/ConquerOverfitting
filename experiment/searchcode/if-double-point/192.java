<<<<<<< HEAD
/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

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
 * LongVectorIndividual.java
 * Created: Tue Mar 13 15:03:12 EST 2001
 */

/**
 * LongVectorIndividual is a VectorIndividual whose genome is an array of longs.
 * Gene values may range from species.mingene(x) to species.maxgene(x),
 * inclusive. The default mutation method randomizes genes to new values in this
 * range, with <tt>species.mutationProbability</tt>.
 * 
 * 
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
 * well.
 * 
 * <p>
 * <b>Default Base</b><br>
 * vector.long-vect-ind
 * 
 * @author Sean Luke
 * @version 1.0
 */

public class LongVectorIndividual extends VectorIndividual {
    public static final String P_LONGVECTORINDIVIDUAL = "long-vect-ind";
    public long[] genome;

    public Parameter defaultBase() {
        return VectorDefaults.base().push(P_LONGVECTORINDIVIDUAL);
    }

    public Object clone() {
        LongVectorIndividual myobj = (LongVectorIndividual) (super.clone());

        // must clone the genome
        myobj.genome = (long[]) (genome.clone());

        return myobj;
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base); // actually unnecessary (Individual.setup() is
                                  // empty)

        Parameter def = defaultBase();

        if (!(species instanceof IntegerVectorSpecies))
            state.output.fatal("LongVectorIndividual requires an IntegerVectorSpecies", base, def);
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;

        genome = new long[s.genomeSize];
    }

    // Because Math.floor only goes within the double integer space
    long longFloor(double x) {
        long l = (long) x; // pulls towards zero

        if (x >= 0) {
            return l; // NaN will get shunted to 0 apparently
        } else if (x < Long.MIN_VALUE + 1) // it'll go to Long.MIN_VALUE
        {
            return Long.MIN_VALUE;
        } else if (l == x) // it's exact
        {
            return l;
        } else {
            return l - 1;
        }
    }

    public void defaultCrossover(EvolutionState state, int thread, VectorIndividual ind) {
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        LongVectorIndividual i = (LongVectorIndividual) ind;
        long tmp;
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
            long t, u;
            long min, max;
            for (int x = 0; x < genome.length; x++) {
                min = s.minGene(x);
                max = s.maxGene(x);
                t = longFloor(alpha * genome[x] + (1 - alpha) * i.genome[x] + 0.5);
                u = longFloor(beta * i.genome[x] + (1 - beta) * genome[x] + 0.5);
                if (!(t < min || t > max || u < min || u > max)) {
                    genome[x] = t;
                    i.genome[x] = u;
                }
            }
        }
            break;
        case VectorSpecies.C_INTERMED_RECOMB: {
            long t, u;
            long min, max;
            for (int x = 0; x < genome.length; x++) {
                do {
                    double alpha = state.random[thread].nextDouble() * (1 + 2 * s.lineDistance) - s.lineDistance;
                    double beta = state.random[thread].nextDouble() * (1 + 2 * s.lineDistance) - s.lineDistance;
                    min = s.minGene(x);
                    max = s.maxGene(x);
                    t = longFloor(alpha * genome[x] + (1 - alpha) * i.genome[x] + 0.5);
                    u = longFloor(beta * i.genome[x] + (1 - beta) * genome[x] + 0.5);
                } while (t < min || t > max || u < min || u > max);
                genome[x] = t;
                i.genome[x] = u;
            }
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
            pieces[x] = new long[point1 - point0];
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
            sum += ((long[]) (pieces[x])).length;

        int runningsum = 0;
        long[] newgenome = new long[sum];
        for (int x = 0; x < pieces.length; x++) {
            System.arraycopy(pieces[x], 0, newgenome, runningsum, ((long[]) (pieces[x])).length);
            runningsum += ((long[]) (pieces[x])).length;
        }
        // set genome
        genome = newgenome;
    }

    /**
     * Returns a random value from between min and max inclusive. This method
     * handles overflows that complicate this computation. Does NOT check that
     * min is less than or equal to max. You must check this yourself.
     */
    public long randomValueFromClosedInterval(long min, long max, MersenneTwisterFast random) {
        if (max - min < 0) // we had an overflow
        {
            long l = 0;
            do
                l = random.nextLong();
            while (l < min || l > max);
            return l;
        } else
            return min + random.nextLong(max - min + 1L);
    }

    /**
     * Destructively mutates the individual in some default manner. The default
     * form simply randomizes genes to a uniform distribution from the min and
     * max of the gene values.
     */
    public void defaultMutate(EvolutionState state, int thread) {
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        if (s.mutationProbability > 0.0)
            for (int x = 0; x < genome.length; x++)
                if (state.random[thread].nextBoolean(s.mutationProbability))
                    genome[x] = randomValueFromClosedInterval(s.minGene(x), s.maxGene(x), state.random[thread]);
    }

    /**
     * Initializes the individual by randomly choosing Longs uniformly from
     * mingene to maxgene.
     */
    public void reset(EvolutionState state, int thread) {
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        for (int x = 0; x < genome.length; x++)
            genome[x] = randomValueFromClosedInterval(s.minGene(x), s.maxGene(x), state.random[thread]);
    }

    public int hashCode() {
        // stolen from GPIndividual. It's a decent algorithm.
        int hash = this.getClass().hashCode();

        hash = (hash << 1 | hash >>> 31);
        for (int x = 0; x < genome.length; x++)
            hash = (hash << 1 | hash >>> 31) ^ (int) ((genome[x] >>> 16) & 0xFFFFFFFF) ^ (int) (genome[x] & 0xFFFF);

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

        genome = new long[lll];

        // read in the genes
        for (int i = 0; i < genome.length; i++) {
            Code.decode(d);
            genome[i] = d.l;
        }
    }

    public boolean equals(Object ind) {
        if (!(this.getClass().equals(ind.getClass())))
            return false; // SimpleRuleIndividuals are special.
        LongVectorIndividual i = (LongVectorIndividual) ind;
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
        genome = (long[]) gen;
    }

    public int genomeLength() {
        return genome.length;
    }

    public void writeGenotype(final EvolutionState state, final DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(genome.length);
        for (int x = 0; x < genome.length; x++)
            dataOutput.writeLong(genome[x]);
    }

    public void readGenotype(final EvolutionState state, final DataInput dataInput) throws IOException {
        int len = dataInput.readInt();
        if (genome == null || genome.length != len)
            genome = new long[len];
        for (int x = 0; x < genome.length; x++)
            genome[x] = dataInput.readLong();
    }

    /** Clips each gene value to be within its specified [min,max] range. */
    public void clamp() {
        IntegerVectorSpecies _species = (IntegerVectorSpecies) species;
        for (int i = 0; i < genomeLength(); i++) {
            long minGene = _species.minGene(i);
            if (genome[i] < minGene)
                genome[i] = minGene;
            else {
                long maxGene = _species.maxGene(i);
                if (genome[i] > maxGene)
                    genome[i] = maxGene;
            }
        }
    }

    public void setGenomeLength(int len) {
        long[] newGenome = new long[len];
        System.arraycopy(genome, 0, newGenome, 0, genome.length < newGenome.length ? genome.length : newGenome.length);
        genome = newGenome;
    }

    /** Returns true if each gene value is within is specified [min,max] range. */
    public boolean isInRange() {
        IntegerVectorSpecies _species = (IntegerVectorSpecies) species;
        for (int i = 0; i < genomeLength(); i++)
            if (genome[i] < _species.minGene(i) || genome[i] > _species.maxGene(i))
                return false;
        return true;
    }

    public double distanceTo(Individual otherInd) {
        if (!(otherInd instanceof LongVectorIndividual))
            return super.distanceTo(otherInd); // will return infinity!

        LongVectorIndividual other = (LongVectorIndividual) otherInd;
        long[] otherGenome = other.genome;
        double sumSquaredDistance = 0.0;
        for (int i = 0; i < other.genomeLength(); i++) {
            // can't subtract two longs and expect a long. Must convert to
            // doubles :-(
            double dist = this.genome[i] - (double) otherGenome[i];
            sumSquaredDistance += dist * dist;
        }
        return StrictMath.sqrt(sumSquaredDistance);
    }
}
=======
package org.newdawn.slick.geom;

import java.io.Serializable;

/**
 * The description of any 2D shape that can be transformed. The points provided approximate the intent
 * of the shape. 
 * 
 * @author Mark
 */
public abstract class Shape implements Serializable {
    /** The points representing this polygon. */
    protected float points[];
    /** Center point of the polygon. */
    protected float center[];
    /** The left most point of this shape. */
    protected float x;
    /** The top most point of this shape. */
    protected float y;
    /** The right most point of this shape */
    protected float maxX;
    /** The bottom most point of this shape */
    protected float maxY;
    /** The left most point of this shape. */
    protected float minX;
    /** The top most point of this shape. */
    protected float minY;
    /** Radius of a circle that can completely enclose this shape. */
    protected float boundingCircleRadius;
    /** Flag to tell whether points need to be generated */
    protected boolean pointsDirty;
    /** The triangles that define the shape */
    protected transient Triangulator tris;
    /** True if the triangles need updating */
    protected boolean trianglesDirty;
    
    /**
     * Shape constructor.
     *
     */
    public Shape() {
        pointsDirty = true;
    }
    
    /**
     * Set the location of this shape
     * 
     * @param x The x coordinate of the new location of the shape
     * @param y The y coordinate of the new location of the shape
     */
    public void setLocation(float x, float y) {
    	setX(x);
    	setY(y);
    }
    
    /**
     * Apply a transformation and return a new shape.  This will not alter the current shape but will 
     * return the transformed shape.
     * 
     * @param transform The transform to be applied
     * @return The transformed shape.
     */
    public abstract Shape transform(Transform transform);

    /**
     * Subclasses implement this to create the points of the shape.
     *
     */
    protected abstract void createPoints();
    
    /**
     * Get the x location of the left side of this shape.
     * 
     * @return The x location of the left side of this shape.
     */
    public float getX() {
        return x;
    }
    
    /**
     * Set the x position of the left side this shape.
     * 
     * @param x The new x position of the left side this shape.
     */
    public void setX(float x) {
    	if (x != this.x) {
    		float dx = x - this.x;
	        this.x = x;
	        
	        if ((points == null) || (center == null)) {
	        	checkPoints();
	        }
	        // update the points in the special case
    		for (int i=0;i<points.length/2;i++) {
    			points[i*2] += dx;
    		}
    		center[0] += dx;
    		x += dx;
    		maxX += dx;
    		minX += dx;
	        trianglesDirty = true;
    	}
    }
    
    /**
     * Set the y position of the top of this shape.
     * 
     * @param y The new y position of the top of this shape.
     */
    public void setY(float y) {
    	if (y != this.y) {
    		float dy = y - this.y;
	        this.y = y;

	        if ((points == null) || (center == null)) {
	        	checkPoints();
	        }
	        // update the points in the special case
    		for (int i=0;i<points.length/2;i++) {
    			points[(i*2)+1] += dy;
    		}
    		center[1] += dy;
    		y += dy;
    		maxY += dy;
    		minY += dy;
	        trianglesDirty = true;
    	}
    }

    /**
     * Get the y position of the top of this shape.
     * 
     * @return The y position of the top of this shape.
     */
    public float getY() {
        return y;
    }
    
    /**
     * Set the location of this shape
     * 
     * @param loc The new location of the shape
     */
    public void setLocation(Vector2f loc) {
    	setX(loc.x);
    	setY(loc.y);
    }
    
    /**
     * Get the x center of this shape.
     * 
     * @return The x center of this shape.
     */
    public float getCenterX() {
        checkPoints();
        
        return center[0];
    }
    
    /**
     * Set the x center of this shape.
     * 
     * @param centerX The center point to set.
     */
    public void setCenterX(float centerX) {
        if ((points == null) || (center == null)) {
        	checkPoints();
        }
        
        float xDiff = centerX - getCenterX();
        setX(x + xDiff);
    }

    /**
     * Get the y center of this shape.
     * 
     * @return The y center of this shape.
     */
    public float getCenterY() {
        checkPoints();
        
        return center[1];
    }
    
    /**
     * Set the y center of this shape.
     * 
     * @param centerY The center point to set.
     */
    public void setCenterY(float centerY) {
        if ((points == null) || (center == null)) {
        	checkPoints();
        }
        
        float yDiff = centerY - getCenterY();
        setY(y + yDiff);
    }
    
    /**
     * Get the right most point of this shape.
     * 
     * @return The right most point of this shape.
     */
    public float getMaxX() {
        checkPoints();
        return maxX;
    }
    /**
     * Get the bottom most point of this shape.
     * 
     * @return The bottom most point of this shape.
     */
    public float getMaxY() {
        checkPoints();
        return maxY;
    }
    
    /**
     * Get the left most point of this shape.
     * 
     * @return The left most point of this shape.
     */
    public float getMinX() {
        checkPoints();
        return minX;
    }
    
    /**
     * Get the top most point of this shape.
     * 
     * @return The top most point of this shape.
     */
    public float getMinY() {
        checkPoints();
        return minY;
    }
    
    /**
     * Get the radius of a circle that can completely enclose this shape.
     * 
     * @return The radius of the circle.
     */
    public float getBoundingCircleRadius() {
        checkPoints();
        return boundingCircleRadius;
    }
    
    /**
     * Get the point closet to the center of all the points in this Shape
     * 
     * @return The x,y coordinates of the center.
     */
    public float[] getCenter() {
        checkPoints();
        return center;
    }

    /**
     * Get the points that outline this shape.  Use CW winding rule
     * 
     * @return an array of x,y points
     */
    public float[] getPoints() {
        checkPoints();
        return points;
    }

    /**
     * Get the number of points in this polygon
     * 
     * @return The number of points in this polygon
     */
    public int getPointCount() {
        checkPoints();
        return points.length / 2;
    }

    /**
     * Get a single point in this polygon
     * 
     * @param index The index of the point to retrieve
     * @return The point's coordinates
     */
    public float[] getPoint(int index) {
        checkPoints();

        float result[] = new float[2];
        
        result[0] = points[index * 2];
        result[1] = points[index * 2 + 1];
        
        return result;
    }
    
    /**
     * Get the combine normal of a given point
     * 
     * @param index The index of the point whose normal should be retrieved
     * @return The combined normal of a given point
     */
    public float[] getNormal(int index) {
    	float[] current = getPoint(index);
    	float[] prev = getPoint(index - 1 < 0 ? getPointCount() - 1 : index - 1);
    	float[] next = getPoint(index + 1 >= getPointCount() ? 0 : index + 1);
    	
    	float[] t1 = getNormal(prev, current);
    	float[] t2 = getNormal(current, next);
    	
    	if ((index == 0) && (!closed())) {
    		return t2;
    	}
    	if ((index == getPointCount()-1) && (!closed())) {
    		return t1;
    	}
    	
    	float tx = (t1[0]+t2[0])/2;
    	float ty = (t1[1]+t2[1])/2;
    	float len = (float) Math.sqrt((tx*tx)+(ty*ty));
    	return new float[] {tx/len,ty/len};
    }

    /**
     * Check if the shape passed is entirely contained within 
     * this shape.
     * 
     * @param other The other shape to test against this one
     * @return True if the other shape supplied is entirely contained
     * within this one.
     */
    public boolean contains(Shape other) {
    	if (other.intersects(this)) {
    		return false;
    	}
    	
    	for (int i=0;i<other.getPointCount();i++) {
    		float[] pt = other.getPoint(i);
    		if (!contains(pt[0], pt[1])) {
    			return false;
    		}
    	}
    	
    	return true;
    }
    /**
     * Get the normal of the line between two points
     * 
     * @param start The start point
     * @param end The end point
     * @return The normal of the line between the two points
     */
    private float[] getNormal(float[] start, float[] end) {
		float dx = start[0] - end[0];
		float dy = start[1] - end[1];
		float len = (float) Math.sqrt((dx*dx)+(dy*dy));
		dx /= len;
		dy /= len;
		return new float[] {-dy,dx};
    }
    
    /**
     * Check if the given point is part of the path that
     * forms this shape
     * 
     * @param x The x position of the point to check
     * @param y The y position of the point to check
     * @return True if the point is includes in the path of the polygon
     */
    public boolean includes(float x, float y) {
    	if (points.length == 0) {
    		return false;
    	}
    	
    	checkPoints();
    	
    	Line testLine = new Line(0,0,0,0);
    	Vector2f pt = new Vector2f(x,y);
    	
    	for (int i=0;i<points.length;i+=2) {
    		int n = i+2;
    		if (n >= points.length) {
    			n = 0;
    		}
    		testLine.set(points[i], points[i+1], points[n], points[n+1]);
    		
    		if (testLine.on(pt)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    /**
     * Get the index of a given point
     * 
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @return The index of the point or -1 if the point is not part of this shape path
     */
    public int indexOf(float x, float y) {
    	for (int i=0;i<points.length;i+=2) {
    		if ((points[i] == x) && (points[i+1] == y)) {
    			return i / 2;
    		}
    	}
    	
    	return -1;
    }
    
    /**
     * Check if this polygon contains the given point
     * 
     * @param x The x position of the point to check
     * @param y The y position of the point to check
     * @return True if the point is contained in the polygon
     */
    public boolean contains(float x, float y) {
    	checkPoints();
    	if (points.length == 0) {
    		return false;
    	}
    	
        boolean result = false;
        float xnew,ynew;
        float xold,yold;
        float x1,y1;
        float x2,y2;
        int npoints = points.length;

        xold=points[npoints - 2];
        yold=points[npoints - 1];
        for (int i=0;i < npoints;i+=2) {
             xnew = points[i];
             ynew = points[i + 1];
             if (xnew > xold) {
                  x1 = xold;
                  x2 = xnew;
                  y1 = yold;
                  y2 = ynew;
             }
             else {
                  x1 = xnew;
                  x2 = xold;
                  y1 = ynew;
                  y2 = yold;
             }
             if ((xnew < x) == (x <= xold)          /* edge "open" at one end */
              && ((double)y - (double)y1) * (x2 - x1)
               < ((double)y2 - (double)y1) * (x - x1)) {
                  result = !result;
             }
             xold = xnew;
             yold = ynew;
        }
        
        return result;
    }
    
    /**
     * Check if this shape intersects with the shape provided.
     * 
     * @param shape The shape to check if it intersects with this one.
     * @return True if the shapes do intersect, false otherwise.
     */
    public boolean intersects(Shape shape) {
        /*
         * Intersection formula used:
         *      (x4 - x3)(y1 - y3) - (y4 - y3)(x1 - x3)
         * UA = ---------------------------------------
         *      (y4 - y3)(x2 - x1) - (x4 - x3)(y2 - y1)
         *      
         *      (x2 - x1)(y1 - y3) - (y2 - y1)(x1 - x3)
         * UB = ---------------------------------------
         *      (y4 - y3)(x2 - x1) - (x4 - x3)(y2 - y1)
         *      
         * if UA and UB are both between 0 and 1 then the lines intersect.
         * 
         * Source: http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
         */
        checkPoints();

        boolean result = false;
        float points[] = getPoints();           // (x3, y3)  and (x4, y4)
        float thatPoints[] = shape.getPoints(); // (x1, y1)  and (x2, y2)
        int length = points.length;
        int thatLength = thatPoints.length;
        double unknownA;
        double unknownB;
        
        if (!closed()) {
        	length -= 2;
        }
        if (!shape.closed()) {
        	thatLength -= 2;
        }
        
        // x1 = thatPoints[j]
        // x2 = thatPoints[j + 2]
        // y1 = thatPoints[j + 1]
        // y2 = thatPoints[j + 3]
        // x3 = points[i]
        // x4 = points[i + 2]
        // y3 = points[i + 1]
        // y4 = points[i + 3]
        for(int i=0;i<length;i+=2) {        	
        	int iNext = i+2;
	    	if (iNext >= points.length) {
	    		iNext = 0;
	    	}
	    	
            for(int j=0;j<thatLength;j+=2) {
            	int jNext = j+2;
            	if (jNext >= thatPoints.length) {
            		jNext = 0;
            	}

                unknownA = (((points[iNext] - points[i]) * (double) (thatPoints[j + 1] - points[i + 1])) - 
                        ((points[iNext+1] - points[i + 1]) * (thatPoints[j] - points[i]))) / 
                        (((points[iNext+1] - points[i + 1]) * (thatPoints[jNext] - thatPoints[j])) - 
                                ((points[iNext] - points[i]) * (thatPoints[jNext+1] - thatPoints[j + 1])));
                unknownB = (((thatPoints[jNext] - thatPoints[j]) * (double) (thatPoints[j + 1] - points[i + 1])) - 
                        ((thatPoints[jNext+1] - thatPoints[j + 1]) * (thatPoints[j] - points[i]))) / 
                        (((points[iNext+1] - points[i + 1]) * (thatPoints[jNext] - thatPoints[j])) - 
                                ((points[iNext] - points[i]) * (thatPoints[jNext+1] - thatPoints[j + 1])));
                
                if(unknownA >= 0 && unknownA <= 1 && unknownB >= 0 && unknownB <= 1) {
                    result = true;
                    break;
                }
            }
            if(result) {
                break;
            }
        }

        return result;
    }

    /**
     * Check if a particular location is a vertex of this polygon
     * 
     * @param x The x coordinate to check
     * @param y The y coordinate to check
     * @return True if the cordinates supplied are a vertex of this polygon
     */
    public boolean hasVertex(float x, float y) {    	
    	if (points.length == 0) {
    		return false;
    	}
    	
    	checkPoints();
    	
    	for (int i=0;i<points.length;i+=2) {
    		if ((points[i] == x) && (points[i+1] == y)) {
    			return true;
    		}
    	}
    	
    	return false;
    }

    /**
     * Get the center of this polygon.
     *
     */
    protected void findCenter() {
        center = new float[]{0, 0};
        int length = points.length;
        for(int i=0;i<length;i+=2) {
            center[0] += points[i];
            center[1] += points[i + 1];
        }
        center[0] /= (length / 2);
        center[1] /= (length / 2);
    }
    
    /**
     * Calculate the radius of a circle that can completely enclose this shape.
     *
     */
    protected void calculateRadius() {
        boundingCircleRadius = 0;
        
        for(int i=0;i<points.length;i+=2) {
            float temp = ((points[i] - center[0]) * (points[i] - center[0])) + 
                    ((points[i + 1] - center[1]) * (points[i + 1] - center[1]));
            boundingCircleRadius = (boundingCircleRadius > temp) ? boundingCircleRadius : temp;
        }
        boundingCircleRadius = (float)Math.sqrt(boundingCircleRadius);
    }
    
    /**
     * Calculate the triangles that can fill this shape
     */
    protected void calculateTriangles() {
    	if ((!trianglesDirty) && (tris != null)) {
    		return;
    	}
    	if (points.length >= 6) {
    		boolean clockwise = true;
    		float area = 0;
    		for (int i=0;i<(points.length/2)-1;i++) {
    			float x1 = points[(i*2)];
    			float y1 = points[(i*2)+1];
    			float x2 = points[(i*2)+2];
    			float y2 = points[(i*2)+3];
    			
    			area += (x1 * y2) - (y1 * x2);
    		}
    		area /= 2;
    		clockwise = area > 0;

        	tris = new NeatTriangulator();
    		for (int i=0;i<points.length;i+=2) {
	    		tris.addPolyPoint(points[i], points[i+1]);
	    	}
    		tris.triangulate();
    	}
    	
    	trianglesDirty = false;
    }
    
    /**
     * Increase triangulation
     */
    public void increaseTriangulation() {
    	checkPoints();
    	calculateTriangles();
    	
    	tris = new OverTriangulator(tris);
    }
    
    /**
     * The triangles that define the filled version of this shape
     * 
     * @return The triangles that define the 
     */
    public Triangulator getTriangles() {
        checkPoints();
        calculateTriangles();
    	return tris;
    }
    
    /**
     * Check the dirty flag and create points as necessary.
     */
    protected final void checkPoints() {
        if (pointsDirty) {
            createPoints();
            findCenter();
            calculateRadius();
            
            if (points.length > 0) {
	            maxX = points[0];
	            maxY = points[1];
	            minX = points[0];
	            minY = points[1];
	            for (int i=0;i<points.length/2;i++) {
	            	maxX = Math.max(points[i*2],maxX);
	            	maxY = Math.max(points[(i*2)+1],maxY);
	            	minX = Math.min(points[i*2],minX);
	            	minY = Math.min(points[(i*2)+1],minY);
	            }
            }
            pointsDirty = false;
            trianglesDirty = true;
        }
    }
    
    /**
     * Cause all internal state to be generated and cached
     */
    public void preCache() {
    	checkPoints();
    	getTriangles();
    }
    
    /**
     * True if this is a closed shape
     * 
     * @return True if this is a closed shape
     */
    public boolean closed() {
    	return true;
    }
    
    /**
     * Prune any required points in this shape
     * 
     * @return The new shape with points pruned
     */
    public Shape prune() {
    	Polygon result = new Polygon();
    	
    	for (int i=0;i<getPointCount();i++) {
    		int next = i+1 >= getPointCount() ? 0 : i+1;
    		int prev = i-1 < 0 ? getPointCount() - 1 : i-1;
    		
    		float dx1 = getPoint(i)[0] - getPoint(prev)[0];
    		float dy1 = getPoint(i)[1] - getPoint(prev)[1];
    		float dx2 = getPoint(next)[0] - getPoint(i)[0];
    		float dy2 = getPoint(next)[1] - getPoint(i)[1];
    		
    		float len1 = (float) Math.sqrt((dx1*dx1) + (dy1*dy1));
    		float len2 = (float) Math.sqrt((dx2*dx2) + (dy2*dy2));
    		dx1 /= len1;
    		dy1 /= len1;
    		dx2 /= len2;
    		dy2 /= len2;
    		
    		if ((dx1 != dx2) || (dy1 != dy2)) {
    			result.addPoint(getPoint(i)[0],getPoint(i)[1]);
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Subtract the given shape from this one. Note that this method only deals
     * with edges, it will not create holes in polygons.
     * 
     * @param other The other shape to subtract from this one
     * @return The newly created set of shapes resulting from the operation
     */
    public Shape[] subtract(Shape other) {
    	return new GeomUtil().subtract(this, other);
    }

    /**
     * Join this shape with another.
     * 
     * @param other The other shape to join with this one
     * @return The newly created set of shapes resulting from the operation
     */
    public Shape[] union(Shape other) {
    	return new GeomUtil().union(this, other);
    }
    
    /**
     * Get the width of the shape
     * 
     * @return The width of the shape
     */
    public float getWidth() {
    	checkPoints();
    	return maxX - minX;
    }

    
    /**
     * Get the height of the shape
     * 
     * @return The height of the shape
     */
    public float getHeight() {
    	checkPoints();
    	return maxY - minY;
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

