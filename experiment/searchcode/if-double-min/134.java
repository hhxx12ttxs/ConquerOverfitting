<<<<<<< HEAD
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

=======
/*
Copyright ďż˝ 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/
package org.apache.mahout.math.jet.random;

import org.apache.mahout.common.RandomUtils;

import java.util.Random;

public class Uniform extends AbstractContinousDistribution {

  private double min;
  private double max;

  /**
   * Constructs a uniform distribution with the given minimum and maximum, using a {@link
   * org.apache.mahout.math.jet.random.engine.MersenneTwister} seeded with the given seed.
   */
  public Uniform(double min, double max, int seed) {
    this(min, max, RandomUtils.getRandom(seed));
  }

  /** Constructs a uniform distribution with the given minimum and maximum. */
  public Uniform(double min, double max, Random randomGenerator) {
    setRandomGenerator(randomGenerator);
    setState(min, max);
  }

  /** Constructs a uniform distribution with <tt>min=0.0</tt> and <tt>max=1.0</tt>. */
  public Uniform(Random randomGenerator) {
    this(0, 1, randomGenerator);
  }

  /** Returns the cumulative distribution function (assuming a continous uniform distribution). */
  @Override
  public double cdf(double x) {
    if (x <= min) {
      return 0.0;
    }
    if (x >= max) {
      return 1.0;
    }
    return (x - min) / (max - min);
  }

  /** Returns a uniformly distributed random <tt>boolean</tt>. */
  public boolean nextBoolean() {
    return randomDouble() > 0.5;
  }

  /**
   * Returns a uniformly distributed random number in the open interval <tt>(min,max)</tt> (excluding <tt>min</tt> and
   * <tt>max</tt>).
   */
  @Override
  public double nextDouble() {
    return min + (max - min) * randomDouble();
  }

  /**
   * Returns a uniformly distributed random number in the open interval <tt>(from,to)</tt> (excluding <tt>from</tt> and
   * <tt>to</tt>). Pre conditions: <tt>from &lt;= to</tt>.
   */
  public double nextDoubleFromTo(double from, double to) {
    return from + (to - from) * randomDouble();
  }

  /**
   * Returns a uniformly distributed random number in the open interval <tt>(from,to)</tt> (excluding <tt>from</tt> and
   * <tt>to</tt>). Pre conditions: <tt>from &lt;= to</tt>.
   */
  public float nextFloatFromTo(float from, float to) {
    return (float) nextDoubleFromTo(from, to);
  }

  /**
   * Returns a uniformly distributed random number in the closed interval
   *  <tt>[from,to]</tt> (including <tt>from</tt>
   * and <tt>to</tt>). Pre conditions: <tt>from &lt;= to</tt>.
   */
  public int nextIntFromTo(int from, int to) {
    return (int) ((long) from + (long) ((1L + (long) to - (long) from) * randomDouble()));
  }

  /**
   * Returns a uniformly distributed random number in the closed interval <tt>[from,to]</tt> (including <tt>from</tt>
   * and <tt>to</tt>). Pre conditions: <tt>from &lt;= to</tt>.
   */
  public long nextLongFromTo(long from, long to) {
    /* Doing the thing turns out to be more tricky than expected.
       avoids overflows and underflows.
       treats cases like from=-1, to=1 and the like right.
       the following code would NOT solve the problem: return (long) (Doubles.randomFromTo(from,to));

       rounding avoids the unsymmetric behaviour of casts from double to long: (long) -0.7 = 0, (long) 0.7 = 0.
       checking for overflows and underflows is also necessary.
    */

    // first the most likely and also the fastest case.
    if (from >= 0 && to < Long.MAX_VALUE) {
      return from + (long) nextDoubleFromTo(0.0, to - from + 1);
    }

    // would we get a numeric overflow?
    // if not, we can still handle the case rather efficient.
    double diff = (double) to - (double) from + 1.0;
    if (diff <= Long.MAX_VALUE) {
      return from + (long) nextDoubleFromTo(0.0, diff);
    }

    // now the pathologic boundary cases.
    // they are handled rather slow.
    long random;
    if (from == Long.MIN_VALUE) {
      if (to == Long.MAX_VALUE) {
        //return Math.round(nextDoubleFromTo(from,to));
        int i1 = nextIntFromTo(Integer.MIN_VALUE, Integer.MAX_VALUE);
        int i2 = nextIntFromTo(Integer.MIN_VALUE, Integer.MAX_VALUE);
        return ((i1 & 0xFFFFFFFFL) << 32) | (i2 & 0xFFFFFFFFL);
      }
      random = Math.round(nextDoubleFromTo(Long.MIN_VALUE, to + 1));
      if (random > to) {
        random = Long.MIN_VALUE;
      }
    } else {
      random = Math.round(nextDoubleFromTo(from - 1, to));
      if (random < from) {
        random = to;
      }
    }
    return random;
  }

  /** Returns the probability distribution function (assuming a continous uniform distribution). */
  @Override
  public double pdf(double x) {
    if (x <= min || x >= max) {
      return 0.0;
    }
    return 1.0 / (max - min);
  }

  /** Sets the internal state. */
  public void setState(double min, double max) {
    if (max < min) {
      setState(max, min);
      return;
    }
    this.min = min;
    this.max = max;
  }


  /** Returns a String representation of the receiver. */
  @Override
  public String toString() {
    return this.getClass().getName() + '(' + min + ',' + max + ')';
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

