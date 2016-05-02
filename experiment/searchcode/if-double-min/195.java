<<<<<<< HEAD
// This file is part of OpenTSDB.
// Copyright (C) 2010  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package net.opentsdb.core;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Utility class that provides common, generally useful aggregators.
 */
public final class Aggregators {

  /** Aggregator that sums up all the data points. */
  public static final Aggregator SUM = new Sum();

  /** Aggregator that returns the minimum data point. */
  public static final Aggregator MIN = new Min();

  /** Aggregator that returns the maximum data point. */
  public static final Aggregator MAX = new Max();

  /** Aggregator that returns the average value of the data point. */
  public static final Aggregator AVG = new Avg();

  /** Maps an aggregator name to its instance. */
  private static final HashMap<String, Aggregator> aggregators;

  static {
    aggregators = new HashMap<String, Aggregator>(4);
    aggregators.put("sum", SUM);
    aggregators.put("min", MIN);
    aggregators.put("max", MAX);
    aggregators.put("avg", AVG);
  }

  private Aggregators() {
    // Can't create instances of this utility class.
  }

  /**
   * Returns the set of the names that can be used with {@link #get get}.
   */
  public static Set<String> set() {
    return aggregators.keySet();
  }

  /**
   * Returns the aggregator corresponding to the given name.
   * @param name The name of the aggregator to get.
   * @throws NoSuchElementException if the given name doesn't exist.
   * @see #set
   */
  public static Aggregator get(final String name) {
    final Aggregator agg = aggregators.get(name);
    if (agg != null) {
      return agg;
    }
    throw new NoSuchElementException("No such aggregator: " + name);
  }

  private static final class Sum implements Aggregator {

    public long runLong(final Longs values) {
      long result = values.nextLongValue();
      while (values.hasNextValue()) {
        result += values.nextLongValue();
      }
      return result;
    }

    public double runDouble(final Doubles values) {
      double result = values.nextDoubleValue();
      while (values.hasNextValue()) {
        result += values.nextDoubleValue();
      }
      return result;
    }

    public String toString() {
      return "sum";
    }

  }

  private static final class Min implements Aggregator {

    public long runLong(final Longs values) {
      long min = values.nextLongValue();
      while (values.hasNextValue()) {
        final long val = values.nextLongValue();
        if (val < min) {
          min = val;
        }
      }
      return min;
    }

    public double runDouble(final Doubles values) {
      double min = values.nextDoubleValue();
      while (values.hasNextValue()) {
        final double val = values.nextDoubleValue();
        if (val < min) {
          min = val;
        }
      }
      return min;
    }

    public String toString() {
      return "min";
    }

  }

  private static final class Max implements Aggregator {

    public long runLong(final Longs values) {
      long max = values.nextLongValue();
      while (values.hasNextValue()) {
        final long val = values.nextLongValue();
        if (val > max) {
          max = val;
        }
      }
      return max;
    }

    public double runDouble(final Doubles values) {
      double max = values.nextDoubleValue();
      while (values.hasNextValue()) {
        final double val = values.nextDoubleValue();
        if (val > max) {
          max = val;
        }
      }
      return max;
    }

    public String toString() {
      return "max";
    }

  }

  private static final class Avg implements Aggregator {

    public long runLong(final Longs values) {
      long result = values.nextLongValue();
      int n = 1;
      while (values.hasNextValue()) {
        result += values.nextLongValue();
        n++;
      }
      return result / n;
    }

    public double runDouble(final Doubles values) {
      double result = values.nextDoubleValue();
      int n = 1;
      while (values.hasNextValue()) {
        result += values.nextDoubleValue();
        n++;
      }
      return result / n;
    }

    public String toString() {
      return "avg";
    }

  }

=======
/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.facet.geodistance;

import org.elasticsearch.search.facet.Facet;

import java.util.List;

/**
 * @author kimchy (shay.banon)
 */
public interface GeoDistanceFacet extends Facet, Iterable<GeoDistanceFacet.Entry> {

    /**
     * The type of the filter facet.
     */
    public static final String TYPE = "geo_distance";

    /**
     * An ordered list of geo distance facet entries.
     */
    List<Entry> entries();

    /**
     * An ordered list of geo distance facet entries.
     */
    List<Entry> getEntries();

    public class Entry {

        double from = Double.NEGATIVE_INFINITY;

        double to = Double.POSITIVE_INFINITY;

        long count;

        long totalCount;
        double total;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        /**
         * internal field used to see if this entry was already found for a doc
         */
        boolean foundInDoc = false;

        Entry() {
        }

        public Entry(double from, double to, long count, long totalCount, double total, double min, double max) {
            this.from = from;
            this.to = to;
            this.count = count;
            this.totalCount = totalCount;
            this.total = total;
            this.min = min;
            this.max = max;
        }

        public double from() {
            return this.from;
        }

        public double getFrom() {
            return from();
        }

        public double to() {
            return this.to;
        }

        public double getTo() {
            return to();
        }

        public long count() {
            return this.count;
        }

        public long getCount() {
            return count();
        }

        public long totalCount() {
            return this.totalCount;
        }

        public long getTotalCount() {
            return this.totalCount;
        }

        public double total() {
            return this.total;
        }

        public double getTotal() {
            return total();
        }

        /**
         * The mean of this facet interval.
         */
        public double mean() {
            return total / totalCount;
        }

        /**
         * The mean of this facet interval.
         */
        public double getMean() {
            return mean();
        }

        public double min() {
            return this.min;
        }

        public double getMin() {
            return this.min;
        }

        public double max() {
            return this.max;
        }

        public double getMax() {
            return this.max;
        }
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

