/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.fastplot.bridge;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author dcberrio
 * Adapted from various sources for calculating linear regression using only doubles, 
 * and including various BigDecimal utilities.
 */
public class HighPrecisionLinearRegression {
		
		private double[] x;
		private double[] y;
		private BigDecimal meanX;
		private BigDecimal meanY;
		private BigDecimal slope = null;
		private BigDecimal intercept = null;
		private BigDecimal stndDevX;
		private BigDecimal stndDevY;
		private static int requiredPrecision = 40;
		
		public HighPrecisionLinearRegression(double[] x, double[] y) {
			this.x = x;
			this.y = y;
			compute();
		}
		
		
		/** Compute the slope and intercept values from input x and y values.
		 * 
		 */
		private void compute() {
			int n = x.length;
			List<BigDecimal> xList = new ArrayList<BigDecimal>();
			List<BigDecimal> yList = new ArrayList<BigDecimal>();
			List<BigDecimal> x2List = new ArrayList<BigDecimal>();
			List<BigDecimal> y2List = new ArrayList<BigDecimal>();
			List<BigDecimal> xyList = new ArrayList<BigDecimal>();
			for (int i = 0; i < n; i++) {
				try {
					xList.add(BigDecimal.valueOf(x[i]));
				} catch (Exception e) {
					System.out.println(e.getMessage() + " x[i]: "+ x[i]);
				}
				yList.add(BigDecimal.valueOf(y[i]));
				x2List.add(BigDecimal.valueOf(x[i]).multiply(BigDecimal.valueOf(x[i]), new MathContext(requiredPrecision, RoundingMode.HALF_EVEN)));
				y2List.add(BigDecimal.valueOf(y[i]).multiply(BigDecimal.valueOf(y[i]), new MathContext(requiredPrecision, RoundingMode.HALF_EVEN)));
				xyList.add(BigDecimal.valueOf(x[i]).multiply(BigDecimal.valueOf(y[i]), new MathContext(requiredPrecision, RoundingMode.HALF_EVEN)));
			}
			BigDecimal sumx = BigDecimal.valueOf(0.00000), 
					   sumx2 = BigDecimal.valueOf(0.00000),
							sumxy = BigDecimal.valueOf(0.00000), 
							slopeNumerator = BigDecimal.valueOf(0.00), 
							slopeDenominator = BigDecimal.valueOf(0.00);
			sumx = sum(xList);
			sumxy = sum(xyList);
			sumx2 = sum(x2List);
			meanX = mean(xList, new MathContext(requiredPrecision, RoundingMode.HALF_EVEN));
			meanY = mean(yList, new MathContext(requiredPrecision, RoundingMode.HALF_EVEN));
			slopeNumerator = sumxy.subtract(sumx.multiply(meanY, new MathContext(requiredPrecision, RoundingMode.HALF_EVEN)), 
					new MathContext(requiredPrecision, RoundingMode.HALF_EVEN));
			slopeDenominator = sumx2.subtract(sumx.multiply(meanX, new MathContext(requiredPrecision, RoundingMode.HALF_EVEN)),
					new MathContext(requiredPrecision, RoundingMode.HALF_EVEN));
			if (slopeDenominator.compareTo(BigDecimal.valueOf(0.0)) == 0) {
				slope = null;
				intercept = null;
			} else {
				slope = slopeNumerator.divide(slopeDenominator, new MathContext(requiredPrecision, RoundingMode.HALF_EVEN));
				intercept = meanY.subtract(slope.multiply(meanX));
			}
		}
		
		/** Get the calculated slope as double value; return null if NaN.
		 * @return slope as dboule
		 */
		public double getSlope() {
			if (slope == null) {
				return Double.NaN;
			}
			return slope.doubleValue();
		}
		
		/** Get the calculated slope as double value; return null if NaN.
		 * @return slope as BigDecimal
		 */
		public BigDecimal getSlopeAsBigDecimal() {
			return slope;
		}
		
		/** Get the calculated intercept as BigDecimal
		 * @return intercept the calculated interecept
		 */
		public BigDecimal getIntercept() {
			return intercept;
		}
		
		/** Calculate and return the mean squared error.
		 * @return the mean squared error
		 */
		public double getRSquared() {
			double r = slope.multiply(stndDevX).divide(stndDevY, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			return r * r;
		}
		
		/** Get the x values.
		 * @return x
		 */
		public double[] getX() {
			return x;
		}
		
		/**Returns Y=mX+b with full precision, no rounding of numbers.
		 * @return the model
		 */
		public String getModel(){
			return "Y= "+slope+"X + "+intercept+" RSqrd="+getRSquared();
		}
		/**Returns Y=mX+b .
		 * @return the rounded model
		 */
		public String getRoundedModel(){
			return "Y= "+formatNumber(slope.doubleValue(),8)+"X + "+formatNumber(intercept.doubleValue(),3)+" RSqrd="+ formatNumber(getRSquared(),3);
		}
		/**Calculate Y given X.
		 * @return calculated Y
		 */
		public double calculateY (double x){
			return slope.doubleValue()*x+intercept.doubleValue();
		}
		/**Calculate X given Y.
		 * @return calculated X
		 */
		public double calculateX (double y){
			return (y-intercept.doubleValue())/slope.doubleValue();
		}
		
		/**Converts a double ddd.dddddddd to a user-determined number of decimal places right of the period.  
		 * @return formatted number as string
		 */
		public static String formatNumber(double num, int numberOfDecimalPlaces){
			NumberFormat f = NumberFormat.getNumberInstance();
			f.setMaximumFractionDigits(numberOfDecimalPlaces);
			f.setMinimumFractionDigits(numberOfDecimalPlaces);
			return f.format(num);
		}
		
	    public static final BigDecimal TWO = BigDecimal.valueOf(2);

	    /**
	     * Returns the sum number in the numbers list.
	     *
	     * @param numbers the numbers to calculate the sum.
	     * @return the sum of the numbers.
	     */
	    public static BigDecimal sum(List<BigDecimal> numbers) {
	        BigDecimal sum = new BigDecimal(0);
	        for (BigDecimal bigDecimal : numbers) {
	            sum = sum.add(bigDecimal);
	        }
	        return sum;
	    }

	    /**
	     * Returns the mean number in the numbers list.
	     *
	     * @param numbers the numbers to calculate the mean.
	     * @param context the MathContext.
	     * @return the mean of the numbers.
	     */
	    public static BigDecimal mean(List<BigDecimal> numbers, MathContext context) {
	        BigDecimal sum = sum(numbers);
	        return sum.divide(new BigDecimal(numbers.size()), context);
	    }

	    /**
	     * Returns the min number in the numbers list.
	     *
	     * @param numbers the numbers to calculate the min.
	     * @return the min number in the numbers list.
	     */
	    public static BigDecimal min(List<BigDecimal> numbers) {
	        return new TreeSet<BigDecimal>(numbers).first();
	    }

	    /**
	     * Returns the max number in the numbers list.
	     *
	     * @param numbers the numbers to calculate the max.
	     * @return the max number in the numbers list.
	     */
	    public static BigDecimal max(List<BigDecimal> numbers) {
	        return new TreeSet<BigDecimal>(numbers).last();
	    }

	    /**
	     * Returns the standard deviation of the numbers.
	     * Used in calcuation of either slope or R-squared.
	     * Double.NaN is returned if the numbers list is empty.
	     *
	     * @param numbers       the numbers to calculate the standard deviation.
	     * @param biasCorrected true if variance is calculated by dividing by n - 1. False if by n. stddev is a sqrt of the
	     *                      variance.
	     * @param context       the MathContext
	     * @return the standard deviation
	     */
	    public static BigDecimal stddev(List<BigDecimal> numbers, boolean biasCorrected, MathContext context) {
	        BigDecimal stddev;
	        int n = numbers.size();
	        if (n > 0) {
	            if (n > 1) {
	                stddev = sqrt(var(numbers, biasCorrected, context));
	            }
	            else {
	                stddev = BigDecimal.ZERO;
	            }
	        }
	        else {
	            stddev = BigDecimal.valueOf(Double.NaN);
	        }
	        return stddev;

	    }

	    /**
	     * Computes the variance of the available values. By default, the unbiased "sample variance" definitional formula is
	     * used: variance = sum((x_i - mean)^2) / (n - 1)
	     * <p/>
	     * The "population variance"  ( sum((x_i - mean)^2) / n ) can also be computed using this statistic.  The
	     * <code>biasCorrected</code> property determines whether the "population" or "sample" value is returned by the
	     * <code>evaluate</code> and <code>getResult</code> methods. To compute population variances, set this property to
	     * <code>false</code>.
	     *
	     * @param numbers       the numbers to calculate the variance.
	     * @param biasCorrected true if variance is calculated by dividing by n - 1. False if by n.
	     * @param context       the MathContext
	     * @return the variance of the numbers.
	     */
	    public static BigDecimal var(List<BigDecimal> numbers, boolean biasCorrected, MathContext context) {
	        int n = numbers.size();
	        if (n == 0) {
	            return BigDecimal.valueOf(Double.NaN);
	        }
	        else if (n == 1) {
	            return BigDecimal.ZERO;
	        }
	        BigDecimal mean = mean(numbers, context);
	        List<BigDecimal> squares = new ArrayList<BigDecimal>();
	        for (BigDecimal number : numbers) {
	            BigDecimal XminMean = number.subtract(mean);
	            squares.add(XminMean.pow(2, context));
	        }
	        BigDecimal sum = sum(squares);
	        return sum.divide(new BigDecimal(biasCorrected ? numbers.size() - 1 : numbers.size()), context);

	    }

	    /**
	     * Calculates the square root of the number.
	     *
	     * @param number the input number.
	     * @return the square root of the input number.
	     */
	    public static BigDecimal sqrt(BigDecimal number) {
	        int digits; // final precision
	        BigDecimal numberToBeSquareRooted;
	        BigDecimal iteration1;
	        BigDecimal iteration2;
	        BigDecimal temp1 = null;
	        BigDecimal temp2 = null; // temp values

	        int extraPrecision = number.precision();
	        MathContext mc = new MathContext(extraPrecision, RoundingMode.HALF_UP);
	        numberToBeSquareRooted = number;                                   // bd global variable
	        double num = numberToBeSquareRooted.doubleValue();             // bd to double

	        if (mc.getPrecision() == 0)
	            throw new IllegalArgumentException("\nRoots need a MathContext precision > 0");
	        if (num < 0.)
	            throw new ArithmeticException("\nCannot calculate the square root of a negative number");
	        if (num == 0.)
	            return number.round(mc);                    // return sqrt(0) immediately

	        if (mc.getPrecision() < 50)                // small precision is buggy..
	            extraPrecision += 10;                    // ..make more precise
	        int startPrecision = 1;                   // default first precision

	        /* create the initial values for the iteration procedure:
	        * x0:  x ~ sqrt(d)
	        * v0:  v = 1/(2*x)
	        */
	        if (num == Double.POSITIVE_INFINITY)       // d > 1.7E308
	        {
	            BigInteger bi = numberToBeSquareRooted.unscaledValue();
	            int biLen = bi.bitLength();
	            int biSqrtLen = biLen / 2;                // floors it too

	            bi = bi.shiftRight(biSqrtLen);          // bad guess sqrt(d)
	            iteration1 = new BigDecimal(bi);                 // x ~ sqrt(d)

	            MathContext mm = new MathContext(5, RoundingMode.HALF_DOWN);   // minimal precision
	            extraPrecision += 10;                   // make up for it later

	            iteration2 = BigDecimal.ONE.divide(TWO.multiply(iteration1, mm), mm);   // v = 1/(2*x)
	        }
	        else                                      // d < 1.7E10^308  (the usual numbers)
	        {
	            double s = Math.sqrt(num);
	            iteration1 = new BigDecimal(s);                  // x = sqrt(d)
	            iteration2 = new BigDecimal(1. / 2. / s);            // v = 1/2/x
	            // works because Double.MIN_VALUE * Double.MAX_VALUE ~ 9E-16, so: v > 0

	            startPrecision = 64;
	        }

	        digits = mc.getPrecision() + extraPrecision;        // global limit for procedure

	        // create initial MathContext(precision, RoundingMode)
	        MathContext n = new MathContext(startPrecision, mc.getRoundingMode());

	        return sqrtProcedure(n, digits, numberToBeSquareRooted, iteration1, iteration2, temp1, temp2);           // return square root using argument precision
	    }

	    /**
	     * Square root by coupled Newton iteration, sqrtProcedure() is the iteration part I adopted the Algorithm from the
	     * book "Pi-unleashed", so now it looks more natural I give sparse math comments from the book, it assumes argument
	     * mc precision >= 1
	     *
	     * @param mc
	     * @param digits
	     * @param numberToBeSquareRooted
	     * @param iteration1
	     * @param iteration2
	     * @param temp1
	     * @param temp2
	     * @return
	     */
	    private static BigDecimal sqrtProcedure(MathContext mc, int digits, BigDecimal numberToBeSquareRooted, BigDecimal iteration1,
	                                            BigDecimal iteration2, BigDecimal temp1, BigDecimal temp2) {
	        // next v                                         // g = 1 - 2*x*v
	        temp1 = BigDecimal.ONE.subtract(TWO.multiply(iteration1, mc).multiply(iteration2, mc), mc);
	        iteration2 = iteration2.add(temp1.multiply(iteration2, mc), mc); // v += g*v        ~ 1/2/sqrt(d)

	        // next x
	        temp2 = numberToBeSquareRooted.subtract(iteration1.multiply(iteration1, mc), mc); // e = d - x^2
	        iteration1 = iteration1.add(temp2.multiply(iteration2, mc), mc); // x += e*v        ~ sqrt(d)

	        // increase precision
	        int m = mc.getPrecision();
	        if (m < 2)
	            m++;
	        else
	            m = m * 2 - 1; // next Newton iteration supplies so many exact digits

	        if (m < 2 * digits) // digits limit not yet reached?
	        {
	            mc = new MathContext(m, mc.getRoundingMode()); // apply new precision
	            sqrtProcedure(mc, digits, numberToBeSquareRooted, iteration1, iteration2, temp1, temp2); // next iteration
	        }

	        return iteration1; // returns the iterated square roots
	    }
	    
		/**An example.*/
/*		public static void main(String[] args) {
			double[] x = {
					95, 85, 80, 70, 60
					
					1339714487419.9426, 
						  1339714489478.618, 
						  1339714491537.293, 
						  1339714493595.9683, 
						  1339714495654.6433, 
						  1339714497713.3184, 
						  1339714499771.9937, 
						  1339714501830.6687, 
						  1339714501443.9172, 
						  1339714503502.5923
  };
			double[] y = {
					85, 95, 70, 65, 70
					
				//	12.18, 11.25, 16.47, 13.72, 18.73, 15.61, 19.46, 17.76, 17.76, 24.5
					
			};
			HighPrecisionLinearRegression lr = new HighPrecisionLinearRegression(x, y);
			System.out.println(lr.getRoundedModel());
		}*/

}

