<<<<<<< HEAD
package opendata.incremental.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MathLib {
	public static long min(Collection<Long> l) {
		Long min = Long.MAX_VALUE;
		for (Long v : l) {
			if (v < min)
				min = v;
		}
		return min;
	}

	public static double minDouble(Collection<Double> l) {
		Double min = Double.MAX_VALUE;
		for (Double v : l) {
			if (v < min)
				min = v;
		}
		return min;
	}

	public static long max(Collection<Long> l) {
		Long max = Long.MIN_VALUE;
		for (Long v : l) {
			if (v > max)
				max = v;
		}
		return max;
	}

	public static double maxDouble(Collection<Double> l) {
		Double max = Double.MIN_VALUE;
		for (Double v : l) {
			if (v > max)
				max = v;
		}
		return max;
	}

	public static double mean(Collection<Long> l) {
		Long sum = 0L;
		for (Long v : l) {
			sum += v;
		}
		return sum.doubleValue() / l.size();
	}

	public static double meanDouble(Collection<Double> l) {
		Double sum = 0.0;
		for (Double v : l) {
			sum += v;
		}
		return sum.doubleValue() / l.size();
	}

	public static double median(Collection<Long> l) {
		ArrayList<Long> list = new ArrayList<Long>(l);
		Collections.sort(list);

		int middle = (int) Math.floor(((double) list.size()) / 2);

		if (list.size() % 2 == 1) {
			return list.get(middle);
		} else {
			return (list.get(middle - 1) + list.get(middle)) / 2.0;
		}
	}

	public static double medianDouble(Collection<Double> l) {
		ArrayList<Double> list = new ArrayList<Double>(l);
		Collections.sort(list);

		int middle = (int) Math.floor(((double) list.size()) / 2);

		if (list.size() % 2 == 1) {
			return list.get(middle);
		} else {
			return (list.get(middle - 1) + list.get(middle)) / 2.0;
		}
	}

	public static double sdev(Collection<Long> l) {
		ArrayList<Long> list = new ArrayList<Long>(l);
		Collections.sort(list);
		int sum = 0;
		double mean = mean(list);

		for (Long i : list)
			sum += Math.pow((i - mean), 2);
		return list.size() == 1 ? 0 : Math.sqrt(sum / (list.size() - 1));
	}

	public static double sdevDouble(Collection<Double> l) {
		ArrayList<Double> list = new ArrayList<Double>(l);
		Collections.sort(list);
		int sum = 0;
		double mean = meanDouble(list);

		for (Double i : list)
			sum += Math.pow((i - mean), 2);
		return list.size() == 1 ? 0 : Math.sqrt(sum / (list.size() - 1));
	}
=======
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

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

