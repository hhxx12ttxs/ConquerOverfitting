<<<<<<< HEAD
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.examples;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * A map/reduce program that estimates the value of Pi
 * using a quasi-Monte Carlo (qMC) method.
 * Arbitrary integrals can be approximated numerically by qMC methods.
 * In this example,
 * we use a qMC method to approximate the integral $I = \int_S f(x) dx$,
 * where $S=[0,1)^2$ is a unit square,
 * $x=(x_1,x_2)$ is a 2-dimensional point,
 * and $f$ is a function describing the inscribed circle of the square $S$,
 * $f(x)=1$ if $(2x_1-1)^2+(2x_2-1)^2 <= 1$ and $f(x)=0$, otherwise.
 * It is easy to see that Pi is equal to $4I$.
 * So an approximation of Pi is obtained once $I$ is evaluated numerically.
 * 
 * There are better methods for computing Pi.
 * We emphasize numerical approximation of arbitrary integrals in this example.
 * For computing many digits of Pi, consider using bbp.
 *
 * The implementation is discussed below.
 *
 * Mapper:
 *   Generate points in a unit square
 *   and then count points inside/outside of the inscribed circle of the square.
 *
 * Reducer:
 *   Accumulate points inside/outside results from the mappers.
 *
 * Let numTotal = numInside + numOutside.
 * The fraction numInside/numTotal is a rational approximation of
 * the value (Area of the circle)/(Area of the square) = $I$,
 * where the area of the inscribed circle is Pi/4
 * and the area of unit square is 1.
 * Finally, the estimated value of Pi is 4(numInside/numTotal).  
 */
public class QuasiMonteCarlo extends Configured implements Tool {
  static final String DESCRIPTION
      = "A map/reduce program that estimates Pi using a quasi-Monte Carlo method.";
  /** tmp directory for input/output */
  static private final Path TMP_DIR = new Path(
      QuasiMonteCarlo.class.getSimpleName() + "_TMP_3_141592654");
  
  /** 2-dimensional Halton sequence {H(i)},
   * where H(i) is a 2-dimensional point and i >= 1 is the index.
   * Halton sequence is used to generate sample points for Pi estimation. 
   */
  private static class HaltonSequence {
    /** Bases */
    static final int[] P = {2, 3}; 
    /** Maximum number of digits allowed */
    static final int[] K = {63, 40}; 

    private long index;
    private double[] x;
    private double[][] q;
    private int[][] d;

    /** Initialize to H(startindex),
     * so the sequence begins with H(startindex+1).
     */
    HaltonSequence(long startindex) {
      index = startindex;
      x = new double[K.length];
      q = new double[K.length][];
      d = new int[K.length][];
      for(int i = 0; i < K.length; i++) {
        q[i] = new double[K[i]];
        d[i] = new int[K[i]];
      }

      for(int i = 0; i < K.length; i++) {
        long k = index;
        x[i] = 0;
        
        for(int j = 0; j < K[i]; j++) {
          q[i][j] = (j == 0? 1.0: q[i][j-1])/P[i];
          d[i][j] = (int)(k % P[i]);
          k = (k - d[i][j])/P[i];
          x[i] += d[i][j] * q[i][j];
        }
      }
    }

    /** Compute next point.
     * Assume the current point is H(index).
     * Compute H(index+1).
     * 
     * @return a 2-dimensional point with coordinates in [0,1)^2
     */
    double[] nextPoint() {
      index++;
      for(int i = 0; i < K.length; i++) {
        for(int j = 0; j < K[i]; j++) {
          d[i][j]++;
          x[i] += q[i][j];
          if (d[i][j] < P[i]) {
            break;
          }
          d[i][j] = 0;
          x[i] -= (j == 0? 1.0: q[i][j-1]);
        }
      }
      return x;
    }
  }

  /**
   * Mapper class for Pi estimation.
   * Generate points in a unit square
   * and then count points inside/outside of the inscribed circle of the square.
   */
  public static class QmcMapper extends 
      Mapper<LongWritable, LongWritable, BooleanWritable, LongWritable> {

    /** Map method.
     * @param offset samples starting from the (offset+1)th sample.
     * @param size the number of samples for this map
     * @param context output {ture->numInside, false->numOutside}
     */
    public void map(LongWritable offset,
                    LongWritable size,
                    Context context) 
        throws IOException, InterruptedException {

      final HaltonSequence haltonsequence = new HaltonSequence(offset.get());
      long numInside = 0L;
      long numOutside = 0L;

      for(long i = 0; i < size.get(); ) {
        //generate points in a unit square
        final double[] point = haltonsequence.nextPoint();

        //count points inside/outside of the inscribed circle of the square
        final double x = point[0] - 0.5;
        final double y = point[1] - 0.5;
        if (x*x + y*y > 0.25) {
          numOutside++;
        } else {
          numInside++;
        }

        //report status
        i++;
        if (i % 1000 == 0) {
          context.setStatus("Generated " + i + " samples.");
        }
      }

      //output map results
      context.write(new BooleanWritable(true), new LongWritable(numInside));
      context.write(new BooleanWritable(false), new LongWritable(numOutside));
    }
  }

  /**
   * Reducer class for Pi estimation.
   * Accumulate points inside/outside results from the mappers.
   */
  public static class QmcReducer extends 
      Reducer<BooleanWritable, LongWritable, WritableComparable<?>, Writable> {
    
    private long numInside = 0;
    private long numOutside = 0;
      
    /**
     * Accumulate number of points inside/outside results from the mappers.
     * @param isInside Is the points inside? 
     * @param values An iterator to a list of point counts
     * @param context dummy, not used here.
     */
    public void reduce(BooleanWritable isInside,
        Iterable<LongWritable> values, Context context)
        throws IOException, InterruptedException {
      if (isInside.get()) {
        for (LongWritable val : values) {
          numInside += val.get();
        }
      } else {
        for (LongWritable val : values) {
          numOutside += val.get();
        }
      }
    }

    /**
     * Reduce task done, write output to a file.
     */
    @Override
    public void cleanup(Context context) throws IOException {
      //write output to a file
      Path outDir = new Path(TMP_DIR, "out");
      Path outFile = new Path(outDir, "reduce-out");
      Configuration conf = context.getConfiguration();
      FileSystem fileSys = FileSystem.get(conf);
      SequenceFile.Writer writer = SequenceFile.createWriter(fileSys, conf,
          outFile, LongWritable.class, LongWritable.class, 
          CompressionType.NONE);
      writer.append(new LongWritable(numInside), new LongWritable(numOutside));
      writer.close();
    }
  }

  /**
   * Run a map/reduce job for estimating Pi.
   *
   * @return the estimated value of Pi
   */
  public static BigDecimal estimatePi(int numMaps, long numPoints,
      Configuration conf
      ) throws IOException, ClassNotFoundException, InterruptedException {
    Job job = new Job(conf);
    //setup job conf
    job.setJobName(QuasiMonteCarlo.class.getSimpleName());
    job.setJarByClass(QuasiMonteCarlo.class);

    job.setInputFormatClass(SequenceFileInputFormat.class);

    job.setOutputKeyClass(BooleanWritable.class);
    job.setOutputValueClass(LongWritable.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    job.setMapperClass(QmcMapper.class);

    job.setReducerClass(QmcReducer.class);
    job.setNumReduceTasks(1);

    // turn off speculative execution, because DFS doesn't handle
    // multiple writers to the same file.
    job.setSpeculativeExecution(false);

    //setup input/output directories
    final Path inDir = new Path(TMP_DIR, "in");
    final Path outDir = new Path(TMP_DIR, "out");
    FileInputFormat.setInputPaths(job, inDir);
    FileOutputFormat.setOutputPath(job, outDir);

    final FileSystem fs = FileSystem.get(conf);
    if (fs.exists(TMP_DIR)) {
      throw new IOException("Tmp directory " + fs.makeQualified(TMP_DIR)
          + " already exists.  Please remove it first.");
    }
    if (!fs.mkdirs(inDir)) {
      throw new IOException("Cannot create input directory " + inDir);
    }

    try {
      //generate an input file for each map task
      for(int i=0; i < numMaps; ++i) {
        final Path file = new Path(inDir, "part"+i);
        final LongWritable offset = new LongWritable(i * numPoints);
        final LongWritable size = new LongWritable(numPoints);
        final SequenceFile.Writer writer = SequenceFile.createWriter(
            fs, conf, file,
            LongWritable.class, LongWritable.class, CompressionType.NONE);
        try {
          writer.append(offset, size);
        } finally {
          writer.close();
        }
        System.out.println("Wrote input for Map #"+i);
      }
  
      //start a map/reduce job
      System.out.println("Starting Job");
      final long startTime = System.currentTimeMillis();
      job.waitForCompletion(true);
      final double duration = (System.currentTimeMillis() - startTime)/1000.0;
      System.out.println("Job Finished in " + duration + " seconds");

      //read outputs
      Path inFile = new Path(outDir, "reduce-out");
      LongWritable numInside = new LongWritable();
      LongWritable numOutside = new LongWritable();
      SequenceFile.Reader reader = new SequenceFile.Reader(fs, inFile, conf);
      try {
        reader.next(numInside, numOutside);
      } finally {
        reader.close();
      }

      //compute estimated value
      final BigDecimal numTotal
          = BigDecimal.valueOf(numMaps).multiply(BigDecimal.valueOf(numPoints));
      return BigDecimal.valueOf(4).setScale(20)
          .multiply(BigDecimal.valueOf(numInside.get()))
          .divide(numTotal, RoundingMode.HALF_UP);
    } finally {
      fs.delete(TMP_DIR, true);
    }
  }

  /**
   * Parse arguments and then runs a map/reduce job.
   * Print output in standard out.
   * 
   * @return a non-zero if there is an error.  Otherwise, return 0.  
   */
  public int run(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: "+getClass().getName()+" <nMaps> <nSamples>");
      ToolRunner.printGenericCommandUsage(System.err);
      return 2;
    }
    
    final int nMaps = Integer.parseInt(args[0]);
    final long nSamples = Long.parseLong(args[1]);
        
    System.out.println("Number of Maps  = " + nMaps);
    System.out.println("Samples per Map = " + nSamples);
        
    System.out.println("Estimated value of Pi is "
        + estimatePi(nMaps, nSamples, getConf()));
    return 0;
  }

  /**
   * main method for running it as a stand alone command. 
   */
  public static void main(String[] argv) throws Exception {
    System.exit(ToolRunner.run(null, new QuasiMonteCarlo(), argv));
  }
=======
package trussoptimizater.Truss.Optimize;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


/**
 * This class extends line2D functality and is used for defining the symmetry axis when optimizing a truss.
 *
 * @author Chris
 */
public class MirrorLine extends Line2D.Double {

    /**
     * A tolerance is used when comparing Point2D to escape floating points issues
     */
    private double tolerance = 0.0001;


    public MirrorLine(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
    }

    /**
     *
     * @param origPoint
     * @return true if the point in question is left or below this line
     */
    public boolean isLeftorBelowLine(Point2D origPoint){
        if(relativeCCW(origPoint) == 1 || relativeCCW(origPoint) == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * This method calculates whether this line is vertical by finding the difference between
     * the x cordinates of both its points. If the difference is less than the specified tolerance,
     * then this line is vertical.
     * @return true if this line is vertical
     */
    public boolean isVertical() {
        double xdiff = getX2() - getX1();
        if (xdiff <= tolerance && xdiff >= -tolerance) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method calculates whether this line is horizntaol by finding the difference between
     * the y cordinates of both its points. If the difference is less than the specified tolerance,
     * then this line is horizontal.
     * @return true if this line is horizontal
     */
    public boolean isHorizotal() {
        double ydiff = getY2() - getY1();
        if (ydiff <= tolerance && ydiff >= -tolerance) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method checks whether this line is horizontal or vertical. If it is netiher it must be angled.
     * @return true if the line is not horizontal and not vertical
     */
    public boolean isAngled() {
        if (isHorizotal() || isVertical()) {
            return false;
        } else {
            return true;
        }
    }


 
    /**
     * Uses y = mx + c to work out y. Note this method assumes the line is infinite and will calculate a y value
     * even if the x cordinate you specifiy is not within p1.x and p2.x
     * @param x The horizontal distance at which you wish to find the corresponding y value
     * @return the y cordinate of the line at specified x cordinate
     * @throws Exception if the line is vertical as there is no way to tell what y ias at x
     */
    private double getYatX(double x) throws Exception {
        if (isHorizotal()) {
            return getY1();
        }

        if (this.isVertical()) {
            throw new Exception("Line is Vertical, therefore no way to tell what y is at " + x + "!");
        }


        double lineGradient = getGradient();
        double lineIntercept = getYIntercept();
        return lineGradient * x + lineIntercept;
    }

    /**
     * Uses y = mx + c to work out x. Note this method assumes the line is infinite and will calculate an x value
     * even if the y cordinate you specifiy is not within p1.y and p2.y
     * @param y The vertical distance at which you wish to find the corresponding x value
     * @return the x cordinate of the line at specified y cordinate
     * @throws Exception If the line is horzintal there is no way to tell what x is at y.
     */
    private double getXatY(double y) throws Exception {
        double lineGradient = getGradient();
        double lineIntercept = getYIntercept();

        if (isHorizotal()) {
            throw new Exception("Line is Horizontal, therefore no way to tell what x is at " + y + "!");
        }

        if (isVertical()) {
            return y;
        }

        if (lineIntercept > 0) {
            return (y - lineIntercept) / lineGradient;
        } else {
            return (y + lineIntercept) / lineGradient;
        }
    }

    /**
     * The super method always returns false, therefore this method has been overriden to include
     * @param p
     * @return true if point lies on line
     */
    @Override
    public boolean contains(Point2D p) {
        if (this.isVertical()) {
            if (p.getX() - getX1() <= tolerance && p.getX() - getX1() >= -tolerance ) {
                return true;
            } else {
                return false;
            }
        }
        if (this.isHorizotal()) {
            if (p.getY() - this.getY1() <= tolerance && p.getY() - this.getY1() >= -tolerance) {
                return true;
            } else {
                return false;
            }
        }
        try {
            if (p.getY() - getYatX(p.getX()) <= tolerance && p.getY() - getYatX(p.getX()) >= -tolerance ) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            System.out.println("Error in contains method " + ex);
            return false;
        }
    }



    /**
     * If this line is horizontal, then the y intercept will just be p1.y or p2.y. If this
     * line is angled then y intercept can be found by rearanging the straight line equation ie
     * c = y - mx.
     * @return the y cordinate where this line crosses the y axis
     * @throws Exception If this line is vertical, then there is no y intercept
     */
    private double getYIntercept() throws Exception {
        if (isHorizotal()) {
            return getY1();
        }

        if (this.isVertical()) {
            throw new Exception("Line is Vertical, therefore no y intercept");
        }

        double gradient = getGradient();
        if (gradient * getX1() > 0) {
            return getY1() - gradient * getX1();
        } else {
            return getY1() + gradient * getX1();
        }

    }

    /**
     * The gradient of this line is the (m) used in the y = mx+ c equation. Note that if this
     * line is horizontal gradient will be 0. And if this line is vertical graident will be infinity.
     * @return the gradient of the line
     */
    private double getGradient() {
        double xdiff = getX2() - getX1();
        double ydiff = getY2() - getY1();
        return ydiff / xdiff;
    }

    /**
     * Uses projections to find a the cordinates of a mirror Point.
     * <p>
     * For example if the mirror is vertical and starts at the cordinates (0,0).
     * If you use getReflectedPoint(new Point2D.Double(-5,0) it will return the mirror point with the
     * cordinates (5,0)
     * <p>
     * @param p The point that you want to obtain the mirror point of
     * @return a point mirror by this line
     */
    public Point2D getReflectedPoint(Point2D p) {
        //vector y (the point)
        double y1 = p.getX() - getX1();
        double y2 = p.getY() - getY1();

        //vector u (the line)
        double u1 = getX2() - getX1();
        double u2 = getY2() - getY1();

        //orthogonal projection of y onto u
        double scale = (y1 * u1 + y2 * u2) / (u1 * u1 + u2 * u2);
        double projX = scale * u1 + getX1();
        double projY = scale * u2 + getY1();

        return new Point2D.Double(2 * projX - p.getX(), 2 * projY - p.getY());
    }


>>>>>>> 76aa07461566a5976980e6696204781271955163
}

