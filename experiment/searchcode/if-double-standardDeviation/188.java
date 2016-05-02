package repast.simphony.data.array;

import org.apache.commons.math.stat.descriptive.moment.*;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.Product;

/**
 * Factory for creating and sharing DoubleArrayFunctions. Most of these
 * are based on those in commons-math.
 *
 * {@see <a href="http://jakarta.apache.org/commons/math/api-1.1/index.html>
 * the commons-math javadoc</a>}
 *
 * @author Nick Collier
 */
public class FunctionFactory {

  DoubleArrayFunction min, max, mean, sum, geoMean, kurtosis, product, skewness,
          stdDev, variance, count, log, exp, log10, sin, cos, tan, sind, cosd, tand,
          abs, sqr, sqrt, neg;

  /**
   * Creates a function to find the minimum
   * value of a DoubleArray.
   *
   * @return the min function.
   */
  public DoubleArrayFunction createMin() {
    if (min == null) {
      min = new StorelessStatDoubleArrayFunction(new Min());
    }

    return min;
  }

  /**
   * Creates a function to find the maximum
   * value of a DoubleArray.
   *
   * @return the max function.
   */
  public DoubleArrayFunction createMax() {
    if (max == null) {
      max = new StorelessStatDoubleArrayFunction(new Max());
    }

    return max;
  }

  /**
   * Creates a function to find the mean
   * value of a DoubleArray.
   *
   * @return the mean function.
   */
  public DoubleArrayFunction createMean() {
    if (mean == null) {
      mean = new StorelessStatDoubleArrayFunction(new Mean());
    }

    return mean;
  }

  /**
   * Creates a function to find the sum of all the
   * values in a DoubleArray.
   *
   * @return the sum function.
   */
  public DoubleArrayFunction createSum() {
    if (sum == null) {
      sum = new StorelessStatDoubleArrayFunction(new Sum());
    }

    return sum;
  }

  /**
   * Creates a function to find the geometric mean of the
   * values in a DoubleArray. Note that this uses exp( 1/n (sum of logs) ). Therefore,<ul>
   * <p/>
   * <li>If any of values are < 0, the result is NaN.
   * <li>If all values are non-negative and less than Double.POSITIVE_INFINITY, but at least
   * one value is 0, the result is 0.
   * <li>If both Double.POSITIVE_INFINITY and Double.NEGATIVE_INFINITY are among the values,
   * the result is NaN.
   * </ul>
   *
   * @return the geometric mean function.
   */
  public DoubleArrayFunction createGeometricMean() {
    if (geoMean == null) {
      geoMean = new StorelessStatDoubleArrayFunction(new GeometricMean());
    }

    return geoMean;
  }

  /**
   * Creates a function to calculate the kurtosis of
   * the values in a DoubleArray. The following (unbiased) formula
   * is used:<p>
   * kurtosis = { [n(n+1) / (n -1)(n - 2)(n-3)] sum[(x_i - mean)^4] / std^4 } - [3(n-1)^2 / (n-2)(n-3)]
   * <p/>
   * where n is the number of values, mean is the Mean and std is the StandardDeviation
   * <p/>
   * Note that this statistic is undefined for n < 4. Double.Nan is returned when there is not
   * sufficient data to compute the statistic.
   *
   * @return the Kurtosis function
   */
  public DoubleArrayFunction createKurtosis() {
    if (kurtosis == null) {
      kurtosis = new StorelessStatDoubleArrayFunction(new Kurtosis());
    }

    return kurtosis;
  }

  /**
   * Creates a function to calculate the product of
   * the values in a DoubleArray.
   *
   * @return the product function
   */
  public DoubleArrayFunction createProduct() {
    if (product == null) {
      product = new StorelessStatDoubleArrayFunction(new Product());
    }

    return product;
  }

  /**
   * Creates a function to calculate the standard deviation of
   * the values in a DoubleArray. This is the positive square root
   * of the bias-corrected (unbiased) sample variance.
   *
   * @return the standard deviation function
   */
  public DoubleArrayFunction createStdDev() {
    if (stdDev == null) {
      stdDev = new StorelessStatDoubleArrayFunction(new StandardDeviation());
    }

    return stdDev;
  }

  /**
   * Creates a function to calculate the skewness of
   * the values in a DoubleArray. The following (unbiased)
   * formula is used:<p/>
   * 
   * skewness = [n / (n -1) (n - 2)] sum[(x_i - mean)^3] / std^3
   * <p/>
   *
   * @return the skewness function
   */
  public DoubleArrayFunction createSkewness() {
    if (skewness == null) {
      skewness = new StorelessStatDoubleArrayFunction(new Skewness());
    }

    return skewness;
  }

  /**
   * Creates a function to calculate the variance of
   * the values in a DoubleArray. The following formula
   * is used:<p/>
   *
   * variance = sum((x_i - mean)^2) / (n - 1)
   * <p/>
   * 
   *
   * @return the variance function
   */
  public DoubleArrayFunction createVariance() {
    if (variance == null) {
      variance = new StorelessStatDoubleArrayFunction(new Variance());
    }

    return variance;
  }

  /**
   * Create a function that returns the count of
   * the number of elements in the DoubleArray.
   *
   * @return the count function.
   */
  public DoubleArrayFunction createCount() {
    if (count == null) {
      count = new DoubleArrayFunction() {
        public DoubleArray apply(double[] vals, int start, int length) {
          DoubleArray array = new DoubleArray();
          array.add(length);
          return array;
        }
      };
    }

    return count;
  }

  /**
   * Creates a function that calcs the sine  of each
   * array element and returns a DoubleArray of those
   * results. The array elements are assumed to be
   * radians.
   *
   * @return the sine function.
   */
  public DoubleArrayFunction createSin() {
    if (sin == null) {
      sin = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.sin(val);
        }
      };
    }

    return sin;
  }

  /**
   * Creates a function that calcs the sine of each
   * array element and returns a DoubleArray of those
   * results. The array elements are assumed to be
   * degrees.
   *
   * @return the sine function.
   */
  public DoubleArrayFunction createSinD() {
    if (sind == null) {
      sind = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.sin(Math.toRadians(val));
        }
      };
    }

    return sind;
  }

  /**
   * Creates a function that calcs the cosine  of each
   * array element and returns a DoubleArray of those
   * results. The array elements are assumed to be
   * radians.
   *
   * @return the cosine function.
   */
  public DoubleArrayFunction createCos() {
    if (cos == null) {
      cos = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.cos(val);
        }
      };
    }

    return cos;
  }

  /**
   * Creates a function that calcs the cosine of each
   * array element and returns a DoubleArray of those
   * results. The array elements are assumed to be
   * degrees.
   *
   * @return the cosine function.
   */
  public DoubleArrayFunction createCosD() {
    if (cosd == null) {
      cosd = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.cos(Math.toRadians(val));
        }
      };
    }

    return cosd;
  }

  /**
   * Creates a function that calcs the tangent  of each
   * array element and returns a DoubleArray of those
   * results. The array elements are assumed to be
   * radians.
   *
   * @return the tangent function.
   */
  public DoubleArrayFunction createTan() {
    if (tan == null) {
      tan = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.tan(val);
        }
      };
    }

    return tan;
  }

  /**
   * Creates a function that calcs the tangent of each
   * array element and returns a DoubleArray of those
   * results. The array elements are assumed to be
   * degrees.
   *
   * @return the tangent function.
   */
  public DoubleArrayFunction createTanD() {
    if (tand == null) {
      tand = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.tan(Math.toRadians(val));
        }
      };
    }

    return tand;
  }

  /**
   * Creates a function that calculates the negative
   * of each element in a double array and returns a DoubleArray
   * of those results.
   *
   * @return the neg function.
   */
  public DoubleArrayFunction createNeg() {
    if (neg == null) {
      neg = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return -val;
        }
      };
    }

    return neg;
  }

  /**
   * Creates a function that calculates the absolute
   * value of each element in a double array and
   * returns a DoubleArray of those results.
   *
   * @return the abs function
   */
  public DoubleArrayFunction createAbs() {
    if (abs == null) {
      abs = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.abs(val);
        }
      };
    }

    return abs;
  }

  /**
   * Creates a function that calculates the square
   * root of each element in a double array and
   * returns a DoubleArray of those results.
   *
   * @return the abs function
   */
  public DoubleArrayFunction createSqrt() {
    if (sqrt == null) {
      sqrt = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.sqrt(val);
        }
      };
    }

    return sqrt;
  }

  /**
   * Creates a function that calculates the square
   * of each element in a double array and
   * returns a DoubleArray of those results.
   *
   * @return the abs function
   */
  public DoubleArrayFunction createSqr() {
    if (sqr == null) {
      sqr = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return val * val;
        }
      };
    }

    return sqr;
  }

  /**
   * Creates a function that raises Euler's number
   * e by each number in a double array and
   * returns a DoubleArray of those results.
   *
   * @return the exp function
   */
  public DoubleArrayFunction createExp() {
    if (exp == null) {
      exp = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.exp(val);
        }
      };
    }

    return exp;
  }

  /**
   * Creates a function that calculates the natural
   * logarithm (base e) of each element in a
   * double array and returns a DoubleArray of
   * those results.
   *
   * @return the log function
   */
  public DoubleArrayFunction createLog() {
    if (log == null) {
      log = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.log(val);
        }
      };
    }

    return log;
  }

  /**
   * Creates a function that calculates the base 10
   * logarithm of each element in a
   * double array and returns a DoubleArray of
   * those results.
   *
   * @return the log function
   */
  public DoubleArrayFunction createLog10() {
    if (log10 == null) {
      log10 = new AbstractDoubleArrayFunction() {
        protected double apply(double val) {
          return Math.log10(val);
        }
      };
    }

    return log10;
  }


}


