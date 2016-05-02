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
import java.util.Iterator;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * A Map-reduce program to estimate the value of Pi
 * using quasi-Monte Carlo method.
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
 * the value (Area of the circle)/(Area of the square),
 * where the area of the inscribed circle is Pi/4
 * and the area of unit square is 1.
 * Then, Pi is estimated value to be 4(numInside/numTotal).  
 */
public class PiEstimator extends Configured implements Tool {
  /** tmp directory for input/output */
  static private final Path TMP_DIR = new Path(
      PiEstimator.class.getSimpleName() + "_TMP_3_141592654");
  
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
  public static class PiMapper extends MapReduceBase
    implements Mapper<LongWritable, LongWritable, BooleanWritable, LongWritable> {

    /** Map method.
     * @param offset samples starting from the (offset+1)th sample.
     * @param size the number of samples for this map
     * @param out output {ture->numInside, false->numOutside}
     * @param reporter
     */
    public void map(LongWritable offset,
                    LongWritable size,
                    OutputCollector<BooleanWritable, LongWritable> out,
                    Reporter reporter) throws IOException {

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
          reporter.setStatus("Generated " + i + " samples.");
        }
      }

      //output map results
      out.collect(new BooleanWritable(true), new LongWritable(numInside));
      out.collect(new BooleanWritable(false), new LongWritable(numOutside));
    }
  }

  /**
   * Reducer class for Pi estimation.
   * Accumulate points inside/outside results from the mappers.
   */
  public static class PiReducer extends MapReduceBase
    implements Reducer<BooleanWritable, LongWritable, WritableComparable<?>, Writable> {
    
    private long numInside = 0;
    private long numOutside = 0;
    private JobConf conf; //configuration for accessing the file system
      
    /** Store job configuration. */
    @Override
    public void configure(JobConf job) {
      conf = job;
    }

    /**
     * Accumulate number of points inside/outside results from the mappers.
     * @param isInside Is the points inside? 
     * @param values An iterator to a list of point counts
     * @param output dummy, not used here.
     * @param reporter
     */
    public void reduce(BooleanWritable isInside,
                       Iterator<LongWritable> values,
                       OutputCollector<WritableComparable<?>, Writable> output,
                       Reporter reporter) throws IOException {
      if (isInside.get()) {
        for(; values.hasNext(); numInside += values.next().get());
      } else {
        for(; values.hasNext(); numOutside += values.next().get());
      }
    }

    /**
     * Reduce task done, write output to a file.
     */
    @Override
    public void close() throws IOException {
      //write output to a file
      Path outDir = new Path(TMP_DIR, "out");
      Path outFile = new Path(outDir, "reduce-out");
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
  public static BigDecimal estimate(int numMaps, long numPoints, JobConf jobConf
      ) throws IOException {
    //setup job conf
    jobConf.setJobName(PiEstimator.class.getSimpleName());

    jobConf.setInputFormat(SequenceFileInputFormat.class);

    jobConf.setOutputKeyClass(BooleanWritable.class);
    jobConf.setOutputValueClass(LongWritable.class);
    jobConf.setOutputFormat(SequenceFileOutputFormat.class);

    jobConf.setMapperClass(PiMapper.class);
    jobConf.setNumMapTasks(numMaps);

    jobConf.setReducerClass(PiReducer.class);
    jobConf.setNumReduceTasks(1);

    // turn off speculative execution, because DFS doesn't handle
    // multiple writers to the same file.
    jobConf.setSpeculativeExecution(false);

    //setup input/output directories
    final Path inDir = new Path(TMP_DIR, "in");
    final Path outDir = new Path(TMP_DIR, "out");
    FileInputFormat.setInputPaths(jobConf, inDir);
    FileOutputFormat.setOutputPath(jobConf, outDir);

    final FileSystem fs = FileSystem.get(jobConf);
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
            fs, jobConf, file,
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
      JobClient.runJob(jobConf);
      final double duration = (System.currentTimeMillis() - startTime)/1000.0;
      System.out.println("Job Finished in " + duration + " seconds");

      //read outputs
      Path inFile = new Path(outDir, "reduce-out");
      LongWritable numInside = new LongWritable();
      LongWritable numOutside = new LongWritable();
      SequenceFile.Reader reader = new SequenceFile.Reader(fs, inFile, jobConf);
      try {
        reader.next(numInside, numOutside);
      } finally {
        reader.close();
      }

      //compute estimated value
      return BigDecimal.valueOf(4).setScale(20)
          .multiply(BigDecimal.valueOf(numInside.get()))
          .divide(BigDecimal.valueOf(numMaps))
          .divide(BigDecimal.valueOf(numPoints));
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
      return -1;
    }
    
    final int nMaps = Integer.parseInt(args[0]);
    final long nSamples = Long.parseLong(args[1]);
        
    System.out.println("Number of Maps  = " + nMaps);
    System.out.println("Samples per Map = " + nSamples);
        
    final JobConf jobConf = new JobConf(getConf(), getClass());
    System.out.println("Estimated value of Pi is "
        + estimate(nMaps, nSamples, jobConf));
    return 0;
  }

  /**
   * main method for running it as a stand alone command. 
   */
  public static void main(String[] argv) throws Exception {
    System.exit(ToolRunner.run(null, new PiEstimator(), argv));
=======
/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Instance.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Class for handling an instance. All values (numeric, date, nominal, string
 * or relational) are internally stored as floating-point numbers. If an
 * attribute is nominal (or a string or relational), the stored value is the
 * index of the corresponding nominal (or string or relational) value in the
 * attribute's definition. We have chosen this approach in favor of a more
 * elegant object-oriented approach because it is much faster. <p>
 *
 * Typical usage (code from the main() method of this class): <p>
 *
 * <code>
 * ... <br>
 *      
 * // Create empty instance with three attribute values <br>
 * Instance inst = new Instance(3); <br><br>
 *     
 * // Set instance's values for the attributes "length", "weight", and "position"<br>
 * inst.setValue(length, 5.3); <br>
 * inst.setValue(weight, 300); <br>
 * inst.setValue(position, "first"); <br><br>
 *   
 * // Set instance's dataset to be the dataset "race" <br>
 * inst.setDataset(race); <br><br>
 *   
 * // Print the instance <br>
 * System.out.println("The instance: " + inst); <br>
 *
 * ... <br>
 * </code><p>
 *
 * All methods that change an instance are safe, ie. a change of an
 * instance does not affect any other instances. All methods that
 * change an instance's attribute values clone the attribute value
 * vector before it is changed. If your application heavily modifies
 * instance values, it may be faster to create a new instance from scratch.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.28 $ 
 */
public class Instance
  implements Copyable, Serializable, RevisionHandler {
  
  /** for serialization */
  static final long serialVersionUID = 1482635194499365122L;
  
  /** Constant representing a missing value. */
  protected static final double MISSING_VALUE = Double.NaN;

  /** 
   * The dataset the instance has access to.  Null if the instance
   * doesn't have access to any dataset.  Only if an instance has
   * access to a dataset, it knows about the actual attribute types.  
   */
  protected /*@spec_public@*/ Instances m_Dataset;

  /** The instance's attribute values. */
  protected /*@spec_public non_null@*/ double[] m_AttValues;

  /** The instance's weight. */
  protected double m_Weight;

  /**
   * Constructor that copies the attribute values and the weight from
   * the given instance. Reference to the dataset is set to null.
   * (ie. the instance doesn't have access to information about the
   * attribute types)
   *
   * @param instance the instance from which the attribute
   * values and the weight are to be copied 
   */
  //@ ensures m_Dataset == null;
  public Instance(/*@non_null@*/ Instance instance) {
    
    m_AttValues = instance.m_AttValues;
    m_Weight = instance.m_Weight;
    m_Dataset = null;
  }

  /**
   * Constructor that inititalizes instance variable with given
   * values. Reference to the dataset is set to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param weight the instance's weight
   * @param attValues a vector of attribute values 
   */
  //@ ensures m_Dataset == null;
  public Instance(double weight,  /*@non_null@*/ double[]attValues){
    
    m_AttValues = attValues;
    m_Weight = weight;
    m_Dataset = null;
  }

  /**
   * Constructor of an instance that sets weight to one, all values to
   * be missing, and the reference to the dataset to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param numAttributes the size of the instance 
   */
  //@ requires numAttributes > 0;    // Or maybe == 0 is okay too?
  //@ ensures m_Dataset == null;
  public Instance(int numAttributes) {
    
    m_AttValues = new double[numAttributes];
    for (int i = 0; i < m_AttValues.length; i++) {
      m_AttValues[i] = MISSING_VALUE;
    }
    m_Weight = 1;
    m_Dataset = null;
  }

  /**
   * Returns the attribute with the given index.
   *
   * @param index the attribute's index
   * @return the attribute at the given position
   * @throws UnassignedDatasetException if instance doesn't have access to a
   * dataset
   */ 
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute attribute(int index) {
   
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(index);
  }

  /**
   * Returns the attribute with the given index. Does the same
   * thing as attribute().
   *
   * @param indexOfIndex the index of the attribute's index 
   * @return the attribute at the given position
   * @throws UnassignedDatasetException if instance doesn't have access to a
   * dataset
   */ 
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute attributeSparse(int indexOfIndex) {
   
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(indexOfIndex);
  }

  /**
   * Returns class attribute.
   *
   * @return the class attribute
   * @throws UnassignedDatasetException if the class is not set or the
   * instance doesn't have access to a dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute classAttribute() {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classAttribute();
  }

  /**
   * Returns the class attribute's index.
   *
   * @return the class index as an integer 
   * @throws UnassignedDatasetException if instance doesn't have access to a dataset 
   */
  //@ requires m_Dataset != null;
  //@ ensures  \result == m_Dataset.classIndex();
  public /*@pure@*/ int classIndex() {
    
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classIndex();
  }

  /**
   * Tests if an instance's class is missing.
   *
   * @return true if the instance's class is missing
   * @throws UnassignedClassException if the class is not set or the instance doesn't
   * have access to a dataset
   */
  //@ requires classIndex() >= 0;
  public /*@pure@*/ boolean classIsMissing() {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    return isMissing(classIndex());
  }

  /**
   * Returns an instance's class value in internal format. (ie. as a
   * floating-point number)
   *
   * @return the corresponding value as a double (If the 
   * corresponding attribute is nominal (or a string) then it returns the 
   * value's index as a double).
   * @throws UnassignedClassException if the class is not set or the instance doesn't
   * have access to a dataset 
   */
  //@ requires classIndex() >= 0;
  public /*@pure@*/ double classValue() {
    
    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    return value(classIndex());
  }

  /**
   * Produces a shallow copy of this instance. The copy has
   * access to the same dataset. (if you want to make a copy
   * that doesn't have access to the dataset, use 
   * <code>new Instance(instance)</code>
   *
   * @return the shallow copy
   */
  //@ also ensures \result != null;
  //@ also ensures \result instanceof Instance;
  //@ also ensures ((Instance)\result).m_Dataset == m_Dataset;
  public /*@pure@*/ Object copy() {

    Instance result = new Instance(this);
    result.m_Dataset = m_Dataset;
    return result;
  }

  /**
   * Returns the dataset this instance has access to. (ie. obtains
   * information about attribute types from) Null if the instance
   * doesn't have access to a dataset.
   *
   * @return the dataset the instance has accesss to
   */
  //@ ensures \result == m_Dataset;
  public /*@pure@*/ Instances dataset() {

    return m_Dataset;
  }

  /**
   * Deletes an attribute at the given position (0 to 
   * numAttributes() - 1). Only succeeds if the instance does not
   * have access to any dataset because otherwise inconsistencies
   * could be introduced.
   *
   * @param position the attribute's position
   * @throws RuntimeException if the instance has access to a
   * dataset 
   */
  //@ requires m_Dataset != null;
  public void deleteAttributeAt(int position) {

    if (m_Dataset != null) {
      throw new RuntimeException("Instance has access to a dataset!");
    }
    forceDeleteAttributeAt(position);
  }

  /**
   * Returns an enumeration of all the attributes.
   *
   * @return enumeration of all the attributes
   * @throws UnassignedDatasetException if the instance doesn't
   * have access to a dataset 
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ Enumeration enumerateAttributes() {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.enumerateAttributes();
  }

  /**
   * Tests if the headers of two instances are equivalent.
   *
   * @param inst another instance
   * @return true if the header of the given instance is 
   * equivalent to this instance's header
   * @throws UnassignedDatasetException if instance doesn't have access to any
   * dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ boolean equalHeaders(Instance inst) {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.equalHeaders(inst.m_Dataset);
  }

  /**
   * Tests whether an instance has a missing value. Skips the class attribute if set.
   * @return true if instance has a missing value.
   * @throws UnassignedDatasetException if instance doesn't have access to any
   * dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ boolean hasMissingValue() {
    
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    for (int i = 0; i < numAttributes(); i++) {
      if (i != classIndex()) {
	if (isMissing(i)) {
	  return true;
	}
      }
    }
    return false;
  }

  /**
   * Returns the index of the attribute stored at the given position.
   * Just returns the given value.
   *
   * @param position the position 
   * @return the index of the attribute stored at the given position
   */
  public /*@pure@*/ int index(int position) {

    return position;
  }

  /**
   * Inserts an attribute at the given position (0 to 
   * numAttributes()). Only succeeds if the instance does not
   * have access to any dataset because otherwise inconsistencies
   * could be introduced.
   *
   * @param position the attribute's position
   * @throws RuntimeException if the instance has accesss to a
   * dataset
   * @throws IllegalArgumentException if the position is out of range
   */
  //@ requires m_Dataset == null;
  //@ requires 0 <= position && position <= numAttributes();
  public void insertAttributeAt(int position) {

    if (m_Dataset != null) {
      throw new RuntimeException("Instance has accesss to a dataset!");
    }
    if ((position < 0) ||
	(position > numAttributes())) {
      throw new IllegalArgumentException("Can't insert attribute: index out "+
                                         "of range");
    }
    forceInsertAttributeAt(position);
  }

  /**
   * Tests if a specific value is "missing".
   *
   * @param attIndex the attribute's index
   * @return true if the value is "missing"
   */
  public /*@pure@*/ boolean isMissing(int attIndex) {

    if (Double.isNaN(m_AttValues[attIndex])) {
      return true;
    }
    return false;
  }

  /**
   * Tests if a specific value is "missing". Does
   * the same thing as isMissing() if applied to an Instance.
   *
   * @param indexOfIndex the index of the attribute's index 
   * @return true if the value is "missing"
   */
  public /*@pure@*/ boolean isMissingSparse(int indexOfIndex) {

    if (Double.isNaN(m_AttValues[indexOfIndex])) {
      return true;
    }
    return false;
  }

  /**
   * Tests if a specific value is "missing".
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @return true if the value is "missing"
   */
  public /*@pure@*/ boolean isMissing(Attribute att) {

    return isMissing(att.index());
  }

  /**
   * Tests if the given value codes "missing".
   *
   * @param val the value to be tested
   * @return true if val codes "missing"
   */
  public static /*@pure@*/ boolean isMissingValue(double val) {

    return Double.isNaN(val);
  }

  /**
   * Merges this instance with the given instance and returns
   * the result. Dataset is set to null.
   *
   * @param inst the instance to be merged with this one
   * @return the merged instances
   */
  public Instance mergeInstance(Instance inst) {

    int m = 0;
    double [] newVals = new double[numAttributes() + inst.numAttributes()];
    for (int j = 0; j < numAttributes(); j++, m++) {
      newVals[m] = value(j);
    }
    for (int j = 0; j < inst.numAttributes(); j++, m++) {
      newVals[m] = inst.value(j);
    }
    return new Instance(1.0, newVals);
  }

  /**
   * Returns the double that codes "missing".
   *
   * @return the double that codes "missing"
   */
  public /*@pure@*/ static double missingValue() {

    return MISSING_VALUE;
  }

  /**
   * Returns the number of attributes.
   *
   * @return the number of attributes as an integer
   */
  //@ ensures \result == m_AttValues.length;
  public /*@pure@*/ int numAttributes() {

    return m_AttValues.length;
  }

  /**
   * Returns the number of class labels.
   *
   * @return the number of class labels as an integer if the 
   * class attribute is nominal, 1 otherwise.
   * @throws UnassignedDatasetException if instance doesn't have access to any
   * dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ int numClasses() {
    
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.numClasses();
  }

  /**
   * Returns the number of values present. Always the same as numAttributes().
   *
   * @return the number of values
   */
  //@ ensures \result == m_AttValues.length;
  public /*@pure@*/ int numValues() {

    return m_AttValues.length;
  }

  /** 
   * Replaces all missing values in the instance with the
   * values contained in the given array. A deep copy of
   * the vector of attribute values is performed before the
   * values are replaced.
   *
   * @param array containing the means and modes
   * @throws IllegalArgumentException if numbers of attributes are unequal
   */
  public void replaceMissingValues(double[] array) {
	 
    if ((array == null) || 
	(array.length != m_AttValues.length)) {
      throw new IllegalArgumentException("Unequal number of attributes!");
    }
    freshAttributeVector();
    for (int i = 0; i < m_AttValues.length; i++) {
      if (isMissing(i)) {
	m_AttValues[i] = array[i];
      }
    }
  }

  /**
   * Sets the class value of an instance to be "missing". A deep copy of
   * the vector of attribute values is performed before the
   * value is set to be missing.
   *
   * @throws UnassignedClassException if the class is not set
   * @throws UnassignedDatasetException if the instance doesn't
   * have access to a dataset
   */
  //@ requires classIndex() >= 0;
  public void setClassMissing() {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setMissing(classIndex());
  }

  /**
   * Sets the class value of an instance to the given value (internal
   * floating-point format).  A deep copy of the vector of attribute
   * values is performed before the value is set.
   *
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   * @throws UnassignedClassException if the class is not set
   * @throws UnaddignedDatasetException if the instance doesn't
   * have access to a dataset 
   */
  //@ requires classIndex() >= 0;
  public void setClassValue(double value) {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setValue(classIndex(), value);
  }

  /**
   * Sets the class value of an instance to the given value. A deep
   * copy of the vector of attribute values is performed before the
   * value is set.
   *
   * @param value the new class value (If the class
   * is a string attribute and the value can't be found,
   * the value is added to the attribute).
   * @throws UnassignedClassException if the class is not set
   * @throws UnassignedDatasetException if the dataset is not set
   * @throws IllegalArgumentException if the attribute is not
   * nominal or a string, or the value couldn't be found for a nominal
   * attribute 
   */
  //@ requires classIndex() >= 0;
  public final void setClassValue(String value) {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setValue(classIndex(), value);
  }

  /**
   * Sets the reference to the dataset. Does not check if the instance
   * is compatible with the dataset. Note: the dataset does not know
   * about this instance. If the structure of the dataset's header
   * gets changed, this instance will not be adjusted automatically.
   *
   * @param instances the reference to the dataset 
   */
  public final void setDataset(Instances instances) {
    
    m_Dataset = instances;
  }

  /**
   * Sets a specific value to be "missing". Performs a deep copy
   * of the vector of attribute values before the value is set to
   * be missing.
   *
   * @param attIndex the attribute's index
   */
  public final void setMissing(int attIndex) {

    setValue(attIndex, MISSING_VALUE);
  }

  /**
   * Sets a specific value to be "missing". Performs a deep copy
   * of the vector of attribute values before the value is set to
   * be missing. The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   */
  public final void setMissing(Attribute att) {

    setMissing(att.index());
  }

  /**
   * Sets a specific value in the instance to the given value 
   * (internal floating-point format). Performs a deep copy
   * of the vector of attribute values before the value is set.
   *
   * @param attIndex the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public void setValue(int attIndex, double value) {
    
    freshAttributeVector();
    m_AttValues[attIndex] = value;
  }

  /**
   * Sets a specific value in the instance to the given value 
   * (internal floating-point format). Performs a deep copy
   * of the vector of attribute values before the value is set.
   * Does exactly the same thing as setValue().
   *
   * @param indexOfIndex the index of the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public void setValueSparse(int indexOfIndex, double value) {
    
    freshAttributeVector();
    m_AttValues[indexOfIndex] = value;
  }

  /**
   * Sets a value of a nominal or string attribute to the given
   * value. Performs a deep copy of the vector of attribute values
   * before the value is set.
   *
   * @param attIndex the attribute's index
   * @param value the new attribute value (If the attribute
   * is a string attribute and the value can't be found,
   * the value is added to the attribute).
   * @throws UnassignedDatasetException if the dataset is not set
   * @throws IllegalArgumentException if the selected
   * attribute is not nominal or a string, or the supplied value couldn't 
   * be found for a nominal attribute 
   */
  //@ requires m_Dataset != null;
  public final void setValue(int attIndex, String value) {
    
    int valIndex;

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    if (!attribute(attIndex).isNominal() &&
	!attribute(attIndex).isString()) {
      throw new IllegalArgumentException("Attribute neither nominal nor string!");
    }
    valIndex = attribute(attIndex).indexOfValue(value);
    if (valIndex == -1) {
      if (attribute(attIndex).isNominal()) {
	throw new IllegalArgumentException("Value not defined for given nominal attribute!");
      } else {
	attribute(attIndex).forceAddValue(value);
	valIndex = attribute(attIndex).indexOfValue(value);
      }
    }
    setValue(attIndex, (double)valIndex); 
  }

  /**
   * Sets a specific value in the instance to the given value
   * (internal floating-point format). Performs a deep copy of the
   * vector of attribute values before the value is set, so if you are
   * planning on calling setValue many times it may be faster to
   * create a new instance using toDoubleArray.  The given attribute
   * has to belong to a dataset.
   *
   * @param att the attribute 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public final void setValue(Attribute att, double value) {

    setValue(att.index(), value);
  }

  /**
   * Sets a value of an nominal or string attribute to the given
   * value. Performs a deep copy of the vector of attribute values
   * before the value is set, so if you are planning on calling setValue many
   * times it may be faster to create a new instance using toDoubleArray.
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @param value the new attribute value (If the attribute
   * is a string attribute and the value can't be found,
   * the value is added to the attribute).
   * @throws IllegalArgumentException if the the attribute is not
   * nominal or a string, or the value couldn't be found for a nominal
   * attribute 
   */
  public final void setValue(Attribute att, String value) {

    if (!att.isNominal() &&
	!att.isString()) {
      throw new IllegalArgumentException("Attribute neither nominal nor string!");
    }
    int valIndex = att.indexOfValue(value);
    if (valIndex == -1) {
      if (att.isNominal()) {
	throw new IllegalArgumentException("Value not defined for given nominal attribute!");
      } else {
	att.forceAddValue(value);
	valIndex = att.indexOfValue(value);
      }
    }
    setValue(att.index(), (double)valIndex);
  }

  /**
   * Modifies the instances value for an attribute (floating point
   * representation). Unlike in <code>setValue</code> no deep copy is
   * produced, i.e. the actual value is modified.
   *
   * @param attIndex the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).
   * @author Arne Muller (arne.muller@gmail.com)
   */
  public void modifyValue(int attIndex, double value) {
    
    m_AttValues[attIndex] = value;
  }
  
  /**
   * Sets the weight of an instance.
   *
   * @param weight the weight
   */
  public final void setWeight(double weight) {

    m_Weight = weight;
  }

  /** 
   * Returns the relational value of a relational attribute.
   *
   * @param attIndex the attribute's index
   * @return the corresponding relation as an Instances object
   * @throws IllegalArgumentException if the attribute is not a
   * relation-valued attribute
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  //@ requires m_Dataset != null;
  public final /*@pure@*/ Instances relationalValue(int attIndex) {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    } 
    return relationalValue(m_Dataset.attribute(attIndex));
  }


  /** 
   * Returns the relational value of a relational attribute.
   *
   * @param att the attribute
   * @return the corresponding relation as an Instances object
   * @throws IllegalArgumentException if the attribute is not a
   * relation-valued attribute
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  public final /*@pure@*/ Instances relationalValue(Attribute att) {

    int attIndex = att.index();
    if (att.isRelationValued()) {
      return att.relation((int) value(attIndex));
    } else {
      throw new IllegalArgumentException("Attribute isn't relation-valued!");
    }
  }

  /** 
   * Returns the value of a nominal, string, date, or relational attribute
   * for the instance as a string.
   *
   * @param attIndex the attribute's index
   * @return the value as a string
   * @throws IllegalArgumentException if the attribute is not a nominal,
   * string, date, or relation-valued attribute.
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  //@ requires m_Dataset != null;
  public final /*@pure@*/ String stringValue(int attIndex) {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    } 
    return stringValue(m_Dataset.attribute(attIndex));
  }


  /** 
   * Returns the value of a nominal, string, date, or relational attribute
   * for the instance as a string.
   *
   * @param att the attribute
   * @return the value as a string
   * @throws IllegalArgumentException if the attribute is not a nominal,
   * string, date, or relation-valued attribute.
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  public final /*@pure@*/ String stringValue(Attribute att) {

    int attIndex = att.index();
    switch (att.type()) {
    case Attribute.NOMINAL:
    case Attribute.STRING:
      return att.value((int) value(attIndex));
    case Attribute.DATE:
      return att.formatDate(value(attIndex));
    case Attribute.RELATIONAL:
      return att.relation((int) value(attIndex)).stringWithoutHeader();
    default:
      throw new IllegalArgumentException("Attribute isn't nominal, string or date!");
    }
  }

  /**
   * Returns the values of each attribute as an array of doubles.
   *
   * @return an array containing all the instance attribute values
   */
  public double[] toDoubleArray() {

    double[] newValues = new double[m_AttValues.length];
    System.arraycopy(m_AttValues, 0, newValues, 0, 
		     m_AttValues.length);
    return newValues;
  }

  /**
   * Returns the description of one instance. If the instance
   * doesn't have access to a dataset, it returns the internal
   * floating-point values. Quotes string
   * values that contain whitespace characters.
   *
   * @return the instance's description as a string
   */
  public String toString() {

    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < m_AttValues.length; i++) {
      if (i > 0) text.append(",");
      text.append(toString(i));
    }

    if (m_Weight != 1.0) {
      text.append(",{" + Utils.doubleToString(m_Weight, 6) + "}");
    }

    return text.toString();
  }

  /**
   * Returns the description of one value of the instance as a 
   * string. If the instance doesn't have access to a dataset, it 
   * returns the internal floating-point value. Quotes string
   * values that contain whitespace characters, or if they
   * are a question mark.
   *
   * @param attIndex the attribute's index
   * @return the value's description as a string
   */
  public final /*@pure@*/ String toString(int attIndex) {

   StringBuffer text = new StringBuffer();
   
   if (isMissing(attIndex)) {
     text.append("?");
   } else {
     if (m_Dataset == null) {
       text.append(Utils.doubleToString(m_AttValues[attIndex],6));
     } else {
       switch (m_Dataset.attribute(attIndex).type()) {
       case Attribute.NOMINAL:
       case Attribute.STRING:
       case Attribute.DATE:
       case Attribute.RELATIONAL:
         text.append(Utils.quote(stringValue(attIndex)));
         break;
       case Attribute.NUMERIC:
	 text.append(Utils.doubleToString(value(attIndex),6));
         break;
       default:
         throw new IllegalStateException("Unknown attribute type");
       }
     }
   }
   return text.toString();
  }

  /**
   * Returns the description of one value of the instance as a 
   * string. If the instance doesn't have access to a dataset it 
   * returns the internal floating-point value. Quotes string
   * values that contain whitespace characters, or if they
   * are a question mark.
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @return the value's description as a string
   */
  public final String toString(Attribute att) {
   
   return toString(att.index());
  }

  /**
   * Returns an instance's attribute value in internal format.
   *
   * @param attIndex the attribute's index
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a 
   * double).
   */
  public /*@pure@*/ double value(int attIndex) {

    return m_AttValues[attIndex];
  }

  /**
   * Returns an instance's attribute value in internal format.
   * Does exactly the same thing as value() if applied to an Instance.
   *
   * @param indexOfIndex the index of the attribute's index
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a 
   * double).
   */
  public /*@pure@*/ double valueSparse(int indexOfIndex) {

    return m_AttValues[indexOfIndex];
  }  

  /**
   * Returns an instance's attribute value in internal format.
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a
   * double).
   */
  public /*@pure@*/ double value(Attribute att) {

    return value(att.index());
  }

  /**
   * Returns the instance's weight.
   *
   * @return the instance's weight as a double
   */
  public final /*@pure@*/ double weight() {

    return m_Weight;
  }

  /**
   * Deletes an attribute at the given position (0 to 
   * numAttributes() - 1).
   *
   * @param position the attribute's position
   */
  void forceDeleteAttributeAt(int position) {

    double[] newValues = new double[m_AttValues.length - 1];

    System.arraycopy(m_AttValues, 0, newValues, 0, position);
    if (position < m_AttValues.length - 1) {
      System.arraycopy(m_AttValues, position + 1, 
		       newValues, position, 
		       m_AttValues.length - (position + 1));
    }
    m_AttValues = newValues;
  }

  /**
   * Inserts an attribute at the given position
   * (0 to numAttributes()) and sets its value to be missing. 
   *
   * @param position the attribute's position
   */
  void forceInsertAttributeAt(int position)  {

    double[] newValues = new double[m_AttValues.length + 1];

    System.arraycopy(m_AttValues, 0, newValues, 0, position);
    newValues[position] = MISSING_VALUE;
    System.arraycopy(m_AttValues, position, newValues, 
		     position + 1, m_AttValues.length - position);
    m_AttValues = newValues;
  }

  /**
   * Private constructor for subclasses. Does nothing.
   */
  protected Instance() {
  }

  /**
   * Clones the attribute vector of the instance and
   * overwrites it with the clone.
   */
  private void freshAttributeVector() {

    m_AttValues = toDoubleArray();
  }

  /**
   * Main method for testing this class.
   * 
   * @param options the commandline options - ignored
   */
  //@ requires options != null;
  public static void main(String[] options) {

    try {

      // Create numeric attributes "length" and "weight"
      Attribute length = new Attribute("length");
      Attribute weight = new Attribute("weight");
      
      // Create vector to hold nominal values "first", "second", "third" 
      FastVector my_nominal_values = new FastVector(3); 
      my_nominal_values.addElement("first"); 
      my_nominal_values.addElement("second"); 
      my_nominal_values.addElement("third"); 
      
      // Create nominal attribute "position" 
      Attribute position = new Attribute("position", my_nominal_values);
      
      // Create vector of the above attributes 
      FastVector attributes = new FastVector(3);
      attributes.addElement(length);
      attributes.addElement(weight);
      attributes.addElement(position);
      
      // Create the empty dataset "race" with above attributes
      Instances race = new Instances("race", attributes, 0);
      
      // Make position the class attribute
      race.setClassIndex(position.index());
      
      // Create empty instance with three attribute values
      Instance inst = new Instance(3);
      
      // Set instance's values for the attributes "length", "weight", and "position"
      inst.setValue(length, 5.3);
      inst.setValue(weight, 300);
      inst.setValue(position, "first");
      
      // Set instance's dataset to be the dataset "race"
      inst.setDataset(race);
      
      // Print the instance
      System.out.println("The instance: " + inst);
      
      // Print the first attribute
      System.out.println("First attribute: " + inst.attribute(0));
      
      // Print the class attribute
      System.out.println("Class attribute: " + inst.classAttribute());
      
      // Print the class index
      System.out.println("Class index: " + inst.classIndex());
      
      // Say if class is missing
      System.out.println("Class is missing: " + inst.classIsMissing());
      
      // Print the instance's class value in internal format
      System.out.println("Class value (internal format): " + inst.classValue());
      
      // Print a shallow copy of this instance
      Instance copy = (Instance) inst.copy();
      System.out.println("Shallow copy: " + copy);
      
      // Set dataset for shallow copy
      copy.setDataset(inst.dataset());
      System.out.println("Shallow copy with dataset set: " + copy);
      
      // Unset dataset for copy, delete first attribute, and insert it again
      copy.setDataset(null);
      copy.deleteAttributeAt(0);
      copy.insertAttributeAt(0);
      copy.setDataset(inst.dataset());
      System.out.println("Copy with first attribute deleted and inserted: " + copy); 
      
      // Enumerate attributes (leaving out the class attribute)
      System.out.println("Enumerating attributes (leaving out class):");
      Enumeration enu = inst.enumerateAttributes();
      while (enu.hasMoreElements()) {
	Attribute att = (Attribute) enu.nextElement();
	System.out.println(att);
      }
      
      // Headers are equivalent?
      System.out.println("Header of original and copy equivalent: " +
			 inst.equalHeaders(copy));

      // Test for missing values
      System.out.println("Length of copy missing: " + copy.isMissing(length));
      System.out.println("Weight of copy missing: " + copy.isMissing(weight.index()));
      System.out.println("Length of copy missing: " + 
			 Instance.isMissingValue(copy.value(length)));
      System.out.println("Missing value coded as: " + Instance.missingValue());

      // Prints number of attributes and classes
      System.out.println("Number of attributes: " + copy.numAttributes());
      System.out.println("Number of classes: " + copy.numClasses());

      // Replace missing values
      double[] meansAndModes = {2, 3, 0};
      copy.replaceMissingValues(meansAndModes);
      System.out.println("Copy with missing value replaced: " + copy);

      // Setting and getting values and weights
      copy.setClassMissing();
      System.out.println("Copy with missing class: " + copy);
      copy.setClassValue(0);
      System.out.println("Copy with class value set to first value: " + copy);
      copy.setClassValue("third");
      System.out.println("Copy with class value set to \"third\": " + copy);
      copy.setMissing(1);
      System.out.println("Copy with second attribute set to be missing: " + copy);
      copy.setMissing(length);
      System.out.println("Copy with length set to be missing: " + copy);
      copy.setValue(0, 0);
      System.out.println("Copy with first attribute set to 0: " + copy);
      copy.setValue(weight, 1);
      System.out.println("Copy with weight attribute set to 1: " + copy);
      copy.setValue(position, "second");
      System.out.println("Copy with position set to \"second\": " + copy);
      copy.setValue(2, "first");
      System.out.println("Copy with last attribute set to \"first\": " + copy);
      System.out.println("Current weight of instance copy: " + copy.weight());
      copy.setWeight(2);
      System.out.println("Current weight of instance copy (set to 2): " + copy.weight());
      System.out.println("Last value of copy: " + copy.toString(2));
      System.out.println("Value of position for copy: " + copy.toString(position));
      System.out.println("Last value of copy (internal format): " + copy.value(2));
      System.out.println("Value of position for copy (internal format): " + 
			 copy.value(position));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.28 $");
>>>>>>> 76aa07461566a5976980e6696204781271955163
  }
}

