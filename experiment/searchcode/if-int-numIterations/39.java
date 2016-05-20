/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.mahout.clustering.iterator;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.mahout.clustering.classify.ClusterClassifier;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileValueIterator;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

import com.google.common.io.Closeables;

/**
 * This is an experimental clustering iterator which works with a
 * ClusteringPolicy and a prior ClusterClassifier which has been initialized
 * with a set of models. To date, it has been tested with k-means and Dirichlet
 * clustering. See examples DisplayKMeans and DisplayDirichlet which have been
 * switched over to use it.
 */
public class ClusterIterator {
  
  public static final String PRIOR_PATH_KEY = "org.apache.mahout.clustering.prior.path";
  public ClusterIterator(ClusteringPolicy policy) {
    this.policy = policy;
  }
  
  private final ClusteringPolicy policy;
  
  /**
   * Iterate over data using a prior-trained ClusterClassifier, for a number of
   * iterations
   * 
   * @param data
   *          a {@code List<Vector>} of input vectors
   * @param classifier
   *          a prior ClusterClassifier
   * @param numIterations
   *          the int number of iterations to perform
   * @return the posterior ClusterClassifier
   */
  public ClusterClassifier iterate(Iterable<Vector> data, ClusterClassifier classifier, int numIterations) {
    for (int iteration = 1; iteration <= numIterations; iteration++) {
      for (Vector vector : data) {
        // update the policy based upon the prior
        policy.update(classifier);
        // classification yields probabilities
        Vector probabilities = classifier.classify(vector);
        // policy selects weights for models given those probabilities
        Vector weights = policy.select(probabilities);
        // training causes all models to observe data
        for (Iterator<Vector.Element> it = weights.iterateNonZero(); it.hasNext();) {
          int index = it.next().index();
          classifier.train(index, vector, weights.get(index));
        }
      }
      // compute the posterior models
      classifier.close();
    }
    return classifier;
  }
  
  /**
   * Iterate over data using a prior-trained ClusterClassifier, for a number of
   * iterations using a sequential implementation
   * 
   * @param inPath
   *          a Path to input VectorWritables
   * @param priorPath
   *          a Path to the prior classifier
   * @param outPath
   *          a Path of output directory
   * @param numIterations
   *          the int number of iterations to perform
   * @throws IOException
   */
  public void iterateSeq(Path inPath, Path priorPath, Path outPath, int numIterations) throws IOException {
    ClusterClassifier classifier = new ClusterClassifier();
    classifier.readFromSeqFiles(priorPath);
    Configuration conf = new Configuration();
    for (int iteration = 1; iteration <= numIterations; iteration++) {
      for (VectorWritable vw : new SequenceFileDirValueIterable<VectorWritable>(inPath, PathType.LIST,
          PathFilters.logsCRCFilter(), conf)) {
        Vector vector = vw.get();
        // classification yields probabilities
        Vector probabilities = classifier.classify(vector);
        // policy selects weights for models given those probabilities
        Vector weights = policy.select(probabilities);
        // training causes all models to observe data
        for (Iterator<Vector.Element> it = weights.iterateNonZero(); it.hasNext();) {
          int index = it.next().index();
          classifier.train(index, vector, weights.get(index));
        }
      }
      // compute the posterior models
      classifier.close();
      // update the policy
      policy.update(classifier);
      // output the classifier
      classifier.writeToSeqFiles(new Path(outPath, "classifier-" + iteration));
    }
  }
  
  /**
   * Iterate over data using a prior-trained ClusterClassifier, for a number of
   * iterations using a mapreduce implementation
   * 
   * @param inPath
   *          a Path to input VectorWritables
   * @param priorPath
   *          a Path to the prior classifier
   * @param outPath
   *          a Path of output directory
   * @param numIterations
   *          the int number of iterations to perform
   */
  public void iterateMR(Path inPath, Path priorPath, Path outPath, int numIterations) throws IOException,
      InterruptedException, ClassNotFoundException {
    Configuration conf = new Configuration();
    HadoopUtil.delete(conf, outPath);
    for (int iteration = 1; iteration <= numIterations; iteration++) {
      conf.set(PRIOR_PATH_KEY, priorPath.toString());
      
      String jobName = "Cluster Iterator running iteration " + iteration + " over priorPath: " + priorPath;
      System.out.println(jobName);
      Job job = new Job(conf, jobName);
      job.setMapOutputKeyClass(IntWritable.class);
      job.setMapOutputValueClass(ClusterWritable.class);
      job.setOutputKeyClass(IntWritable.class);
      job.setOutputValueClass(ClusterWritable.class);
      
      job.setInputFormatClass(SequenceFileInputFormat.class);
      job.setOutputFormatClass(SequenceFileOutputFormat.class);
      job.setMapperClass(CIMapper.class);
      job.setReducerClass(CIReducer.class);
      
      FileInputFormat.addInputPath(job, inPath);
      Path clustersOut = new Path(outPath, "clusters-" + iteration);
      priorPath = clustersOut;
      FileOutputFormat.setOutputPath(job, clustersOut);
      
      job.setJarByClass(ClusterIterator.class);
      if (!job.waitForCompletion(true)) {
        throw new InterruptedException("Cluster Iteration " + iteration + " failed processing " + priorPath);
      }
      ClusterClassifier.writePolicy(policy, clustersOut);
      FileSystem fs = FileSystem.get(outPath.toUri(), conf);
      if (isConverged(clustersOut, conf, fs)) {
        break;
      }
    }
  }
  
  /**
   * Return if all of the Clusters in the parts in the filePath have converged
   * or not
   * 
   * @param filePath
   *          the file path to the single file containing the clusters
   * @return true if all Clusters are converged
   * @throws IOException
   *           if there was an IO error
   */
  private boolean isConverged(Path filePath, Configuration conf, FileSystem fs) throws IOException {
    for (FileStatus part : fs.listStatus(filePath, PathFilters.partFilter())) {
      SequenceFileValueIterator<ClusterWritable> iterator = new SequenceFileValueIterator<ClusterWritable>(
          part.getPath(), true, conf);
      while (iterator.hasNext()) {
        ClusterWritable value = iterator.next();
        if (!value.getValue().isConverged()) {
          Closeables.closeQuietly(iterator);
          return false;
        }
      }
    }
    return true;
  }
}

