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

<<<<<<< HEAD
package org.apache.mahout.clustering.evaluation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.common.ClassUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterEvaluator {

  private static final Logger log = LoggerFactory.getLogger(ClusterEvaluator.class);

  private final Map<Integer, List<VectorWritable>> representativePoints;

  private final List<Cluster> clusters;

  private final DistanceMeasure measure;

  private boolean pruned;

  /**
   * For testing only
   * 
   * @param representativePoints
   *            a Map<Integer,List<VectorWritable>> of representative points keyed by clusterId
   * @param clusters
   *            a Map<Integer,Cluster> of the clusters keyed by clusterId
   * @param measure
   *            an appropriate DistanceMeasure
   */
  public ClusterEvaluator(Map<Integer, List<VectorWritable>> representativePoints,
                          List<Cluster> clusters, DistanceMeasure measure) {
    this.representativePoints = representativePoints;
    this.clusters = clusters;
    this.measure = measure;
  }

  /**
   * Initialize a new instance from job information
   * 
   * @param conf
   *            a Configuration with appropriate parameters
   * @param clustersIn
   *            a String path to the input clusters directory
   */
  public ClusterEvaluator(Configuration conf, Path clustersIn) {
    measure = ClassUtils.instantiateAs(conf.get(RepresentativePointsDriver.DISTANCE_MEASURE_KEY), DistanceMeasure.class);
    representativePoints = RepresentativePointsMapper.getRepresentativePoints(conf);
    clusters = loadClusters(conf, clustersIn);
  }

  /**
   * Load the clusters from their sequence files
   * 
   * @param clustersIn 
   *            a String pathname to the directory containing input cluster files
   * @return a List<Cluster> of the clusters
   */
  private static List<Cluster> loadClusters(Configuration conf, Path clustersIn) {
    List<Cluster> clusters = Lists.newArrayList();
    for (Cluster value :
         new SequenceFileDirValueIterable<Cluster>(clustersIn, PathType.LIST, PathFilters.logsCRCFilter(), conf)) {
      clusters.add(value);
    }
    return clusters;
  }

  /**
   * Return if the cluster is valid. Valid clusters must have more than 2 representative points,
   * and at least one of them must be different than the cluster center. This is because the
   * representative points extraction will duplicate the cluster center if it is empty.
   * 
   * @param clusterI a Cluster
   * @return a boolean
   */
  private boolean invalidCluster(Cluster clusterI) {
    List<VectorWritable> repPts = representativePoints.get(clusterI.getId());
    if (repPts.size() < 2) {
      return true;
    }
    for (VectorWritable vw : repPts) {
      Vector vector = vw.get();
      if (!vector.equals(clusterI.getCenter())) {
        return false;
      }
    }
    return true;
  }

  private void pruneInvalidClusters() {
    if (pruned) {
      return;
    }
    for (Iterator<Cluster> it = clusters.iterator(); it.hasNext();) {
      Cluster cluster = it.next();
      if (invalidCluster(cluster)) {
        log.info("Pruning cluster Id={}", cluster.getId());
        it.remove();
        representativePoints.remove(cluster.getId());
      }
    }
    pruned = true;
  }

  /**
   * Computes the inter-cluster density as defined in "Mahout In Action"
   * 
   * @return the interClusterDensity
   */
  public double interClusterDensity() {
    pruneInvalidClusters();
    double max = 0;
    double min = Double.MAX_VALUE;
    double sum = 0;
    int count = 0;
    for (int i = 0; i < clusters.size(); i++) {
      Cluster clusterI = clusters.get(i);
      for (int j = i + 1; j < clusters.size(); j++) {
        Cluster clusterJ = clusters.get(j);
        double d = measure.distance(clusterI.getCenter(), clusterJ.getCenter());
        min = Math.min(d, min);
        max = Math.max(d, max);
        sum += d;
        count++;
      }
    }
    double density = (sum / count - min) / (max - min);
    log.info("Inter-Cluster Density = {}", density);
    return density;
  }

  /**
   * Computes the intra-cluster density as the average distance of the representative points
   * from each other
   * 
   * @return the intraClusterDensity of the representativePoints
   */
  public double intraClusterDensity() {
    pruneInvalidClusters();
    double avgDensity = 0;
    for (Cluster cluster : clusters) {
      int count = 0;
      double max = 0;
      double min = Double.MAX_VALUE;
      double sum = 0;
      List<VectorWritable> repPoints = representativePoints.get(cluster.getId());
      for (int i = 0; i < repPoints.size(); i++) {
        for (int j = i + 1; j < repPoints.size(); j++) {
          double d = measure.distance(repPoints.get(i).get(), repPoints.get(j).get());
          min = Math.min(d, min);
          max = Math.max(d, max);
          sum += d;
          count++;
        }
      }
      double density = (sum / count - min) / (max - min);
      avgDensity += density;
      log.info("Intra-Cluster Density[{}] = {}", cluster.getId(), density);
    }
    avgDensity = clusters.isEmpty() ? 0 : avgDensity / clusters.size();
    log.info("Intra-Cluster Density = {}", avgDensity);
    return avgDensity;

  }
=======
package org.apache.mahout.ga.watchmaker.cd.tool;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.RandomUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.examples.MahoutTestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public final class CDInfosToolTest extends MahoutTestCase {

  /** max number of distinct values for any nominal attribute */
  private static final int MAX_NOMINAL_VALUES = 50;
  private Random rng;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    rng = RandomUtils.getRandom();
  }

  private Descriptors randomDescriptors(int nbattributes, double numRate, double catRate) {
    char[] descriptors = new char[nbattributes];
    for (int index = 0; index < nbattributes; index++) {
      double rnd = rng.nextDouble();
      if (rnd < numRate) {
        // numerical attribute
        descriptors[index] = 'N';
      } else if (rnd < (numRate + catRate)) {
        // categorical attribute
        descriptors[index] = 'C';
      } else {
        // ignored attribute
        descriptors[index] = 'I';
      }
    }

    return new Descriptors(descriptors);
  }

  /**
   * generate random descriptions given the attibutes descriptors.<br> -
   * numerical attributes: generate random min and max values<br> - nominal
   * attributes: generate a random list of values
   */
  private Object[][] randomDescriptions(Descriptors descriptors) {
    int nbattrs = descriptors.size();
    Object[][] descriptions = new Object[nbattrs][];

    for (int index = 0; index < nbattrs; index++) {
      if (descriptors.isNumerical(index)) {
        // numerical attribute

        // srowen: I 'fixed' this to not use Double.{MAX,MIN}_VALUE since
        // it does not seem like that has the desired effect
        double min = rng.nextDouble() * ((long) Integer.MAX_VALUE - Integer.MIN_VALUE) + Integer.MIN_VALUE;
        double max = rng.nextDouble() * (Integer.MAX_VALUE - min) + min;

        descriptions[index] = new Double[] { min, max };
      } else if (descriptors.isNominal(index)) {
        // categorical attribute
        int nbvalues = rng.nextInt(MAX_NOMINAL_VALUES) + 1;
        descriptions[index] = new Object[nbvalues];
        for (int vindex = 0; vindex < nbvalues; vindex++) {
          descriptions[index][vindex] = "val_" + index + '_' + vindex;
        }
      }
    }

    return descriptions;
  }

  private void randomDataset(FileSystem fs, Path input, Descriptors descriptors,
      Object[][] descriptions) throws IOException {
    boolean[][] appeared = new boolean[descriptions.length][];
    for (int desc = 0; desc < descriptors.size(); desc++) {
      // appeared is used only by nominal attributes
      if (descriptors.isNominal(desc)) {
        appeared[desc] = new boolean[descriptions[desc].length];
      }
    }

    int nbfiles = rng.nextInt(20) + 1;

    for (int floop = 0; floop < nbfiles; floop++) {
      FSDataOutputStream out = fs.create(new Path(input, "file." + floop));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

      try {
        // make sure we have enough room to allow all nominal values to appear in the data
        int nblines = rng.nextInt(200) + MAX_NOMINAL_VALUES;

        for (int line = 0; line < nblines; line++) {
          writer.write(randomLine(descriptors, descriptions, appeared));
          writer.newLine();
        }
      } finally {
        Closeables.closeQuietly(writer);
      }
    }
  }

  /**
   * generates a random line using the given information
   *
   * @param descriptors attributes descriptions
   * @param descriptions detailed attributes descriptions:<br> - min and max
   *        values for numerical attributes<br> - all distinct values for
   *        nominal attributes
   * @param appeared used to make sure that each nominal attribute's value
   *        appears at least once in the dataset
   */
  private String randomLine(Descriptors descriptors, Object[][] descriptions, boolean[][] appeared) {
    StringBuilder buffer = new StringBuilder();

    for (int index = 0; index < descriptors.size(); index++) {
      if (descriptors.isNumerical(index)) {
        // numerical attribute
        double min = (Double) descriptions[index][0];
        double max = (Double) descriptions[index][1];
        double value = rng.nextDouble() * (max - min) + min;

        buffer.append(value);
      } else if (descriptors.isNominal(index)) {
        // categorical attribute
        int nbvalues = descriptions[index].length;
        // chose a random value
        int vindex;
        if (ArrayUtils.contains(appeared[index], false)) {
          // if some values never appeared in the dataset, start with them
          do {
            vindex = rng.nextInt(nbvalues);
          } while (appeared[index][vindex]);
        } else {
          // chose any value
          vindex = rng.nextInt(nbvalues);
        }

        buffer.append(descriptions[index][vindex]);

        appeared[index][vindex] = true;
      } else {
        // ignored attribute (any value is correct)
        buffer.append('I');
      }

      if (index < descriptors.size() - 1) {
        buffer.append(',');
      }
    }

    return buffer.toString();
  }

  private static int nbNonIgnored(Descriptors descriptors) {
    int nbattrs = 0;
    for (int index = 0; index < descriptors.size(); index++) {
      if (!descriptors.isIgnored(index)) {
        nbattrs++;
      }
    }
    
    return nbattrs;
  }

  @Test
  public void testGatherInfos() throws Exception {
    int n = 1; // put a greater value when you search for some nasty bug
    for (int nloop = 0; nloop < n; nloop++) {
      int maxattr = 100; // max number of attributes
      int nbattrs = rng.nextInt(maxattr) + 1;

      // random descriptors
      double numRate = rng.nextDouble();
      double catRate = rng.nextDouble() * (1.0 - numRate);
      Descriptors descriptors = randomDescriptors(nbattrs, numRate, catRate);

      // random descriptions
      Object[][] descriptions = randomDescriptions(descriptors);

      // random dataset
      Path inpath = getTestTempDirPath("input");
      Path output = getTestTempDirPath("output");
      Configuration conf = new Configuration();
      FileSystem fs = FileSystem.get(inpath.toUri(), conf);
      HadoopUtil.delete(conf, inpath);

      randomDataset(fs, inpath, descriptors, descriptions);

      // Start the tool
      List<String> result = Lists.newArrayList();
      fs.delete(output, true); // It's unhappy if this directory exists
      CDInfosTool.gatherInfos(descriptors, inpath, output, result);

      // check the results
      Collection<String> target = Lists.newArrayList();

      assertEquals(nbNonIgnored(descriptors), result.size());
      int rindex = 0;
      for (int index = 0; index < nbattrs; index++) {
        if (descriptors.isIgnored(index)) {
          continue;
        }

        String description = result.get(rindex++);

        if (descriptors.isNumerical(index)) {
          // numerical attribute
          double min = (Double) descriptions[index][0];
          double max = (Double) descriptions[index][1];
          double[] range = DescriptionUtils.extractNumericalRange(description);

          assertTrue("bad min value for attribute (" + index + ')', min <= range[0]);
          assertTrue("bad max value for attribute (" + index + ')', max >= range[1]);
        } else if (descriptors.isNominal(index)) {
          // categorical attribute
          Object[] values = descriptions[index];
          target.clear();
          DescriptionUtils.extractNominalValues(description, target);

          assertEquals(values.length, target.size());
          assertTrue(target.containsAll(Arrays.asList(values)));
        }
      }
    }
  }

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

