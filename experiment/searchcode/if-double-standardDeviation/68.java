/**
 * Copyright 2004-2006 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * This file is part of MARY TTS.
 *
 * MARY TTS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package marytts.util.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import marytts.util.string.StringUtils;



/**
 * @author Marc Schr&ouml;der, Oytun Tuerk
 * 
 * 
 * An uninstantiable class, containing static utility methods in the Math domain.
 *
 */
public class MathUtils {
    public static final double TINY_PROBABILITY = 1e-50;
    public static final double TINY_PROBABILITY_LOG = Math.log(TINY_PROBABILITY);
    public static final double TINY = 1e-50;
    public static final double TINY_LOG = Math.log(TINY);
    
    protected static final double PASCAL = 2E-5;
    protected static final double PASCALSQUARE = 4E-10;
    protected static final double LOG10 = Math.log(10);

    public static final double TWOPI = 2*Math.PI;

    public static final int EQUALS = 0;
    public static final int GREATER_THAN = 1;
    public static final int GREATER_THAN_OR_EQUALS = 2;
    public static final int LESS_THAN = 3;
    public static final int LESS_THAN_OR_EQUALS = 4;
    public static final int NOT_EQUALS = 5;

    public static boolean isPowerOfTwo(int N)
    {
        final int maxBits = 32;
        int n=2;
        for (int i=2; i<=maxBits; i++) {
            if (n==N) return true;
            n<<=1;
        }
        return false;
    }

    public static int closestPowerOfTwoAbove(int N)
    {
        return 1<<(int) Math.ceil(Math.log(N)/Math.log(2));
    }

    public static int findNextValleyLocation(double[] data, int startIndex)
    {
        for (int i=startIndex+1; i<data.length; i++) {
            if (data[i-1]<data[i]) return i-1;
        }
        return data.length-1;
    }

    public static int findNextPeakLocation(double[] data, int startIndex)
    {
        for (int i=startIndex+1; i<data.length; i++) {
            if (data[i-1]>data[i]) return i-1;
        }
        return data.length-1;
    }

    /**
     * Find the maximum of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return the index number of the maximum element
     */
    public static int findGlobalPeakLocation(double[] data)
    {
        double max = Double.NaN;
        int imax = -1;
        for (int i=0; i<data.length; i++) {
            if (Double.isNaN(data[i])) continue;
            if (Double.isNaN(max)|| data[i] > max) {
                max = data[i];
                imax = i;
            }
        }
        return imax;
    }
    
    /**
     * Find the maximum of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return the index number of the maximum element
     */
    public static int findGlobalPeakLocation(float[] data)
    {
        float max = Float.NaN;
        int imax = -1;
        for (int i=0; i<data.length; i++) {
            if (Float.isNaN(data[i])) continue;
            if (Float.isNaN(max)|| data[i] > max) {
                max = data[i];
                imax = i;
            }
        }
        return imax;
    }

    /**
     * Find the minimum of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return the index number of the minimum element
     */
    public static int findGlobalValleyLocation(double[] data)
    {
        double min = Double.NaN;
        int imin = -1;
        for (int i=0; i<data.length; i++) {
            if (Double.isNaN(data[i])) continue;
            if (Double.isNaN(min)|| data[i] < min) {
                min = data[i];
                imin = i;
            }
        }
        return imin;
    }


    /**
     * Find the minimum of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return the index number of the minimum element
     */
    public static int findGlobalValleyLocation(float[] data)
    {
        float min = Float.NaN;
        int imin = -1;
        for (int i=0; i<data.length; i++) {
            if (Float.isNaN(data[i])) continue;
            if (Float.isNaN(min)|| data[i] < min) {
                min = data[i];
                imin = i;
            }
        }
        return imin;
    }

    /**
     * Build the sum of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return
     */
    public static double sum(double[] data)
    {
        double sum = 0.0;
        for (int i=0; i<data.length; i++) {
            if (Double.isNaN(data[i])) continue; 
            sum += data[i];
        }
        return sum;
    }

    public static float sum(float[] data)
    {
        float sum = 0.0f;
        for (int i=0; i<data.length; i++) {
            if (Float.isNaN(data[i])) continue; 
            sum += data[i];
        }
        return sum;
    }
    
    public static int sum(int[] data)
    {
        int sum = 0;
        for (int i=0; i<data.length; i++)
            sum += data[i];
        
        return sum;
    }
    
    
    public static double sumSquared(double[] data)
    {
        return sumSquared(data, 0.0);
    }
    
    //Computes sum_i=0^data.length-1 (data[i]+term)^2
    public static double sumSquared(double[] data, double term)
    {
        double sum = 0.0;
        for (int i=0; i<data.length; i++) {
            if (Double.isNaN(data[i])) continue; 
            sum += (data[i]+term)*(data[i]+term);
        }
        return sum;
    }
    
    public static double sumSquared(double[] data, int startInd, int endInd)
    {
        return sumSquared(data, startInd, endInd, 0.0);
    }
    
    //Computes sum_i=0^data.length-1 (data[i]+term)^2
    public static double sumSquared(double[] data, int startInd, int endInd, double term)
    {
        double sum = 0.0;
        for (int i=startInd; i<=endInd; i++) 
            sum += (data[i]+term)*(data[i]+term);
        
        return sum;
    }

    /**
     * Find the maximum of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return
     */
    public static double max(double[] data)
    {
        double max = Double.NaN;
        for (int i=0; i<data.length; i++) {
            if (Double.isNaN(data[i])) continue;
            if (Double.isNaN(max)|| data[i] > max) max = data[i];
        }
        return max;
    }

    public static int max(int[] data)
    {
        int max = data[0];
        for (int i=1; i<data.length; i++) {
            if (data[i] > max) 
                max = data[i];
        }
        return max;
    }

    /**
     * Find the maximum of the absolute values of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return
     */
    public static double absMax(double[] data)
    {
        return absMax(data, 0, data.length);
    }
    
    /**
     * Find the maximum of the absolute values of all elements in the given subarray, ignoring elements that are NaN.
     * @param data
     * @param off
     * @param len
     * @return
     */
    public static double absMax(double[] data, int off, int len) {
        double max = Double.NaN;
        for (int i=off; i<off+len; i++) {
            if (Double.isNaN(data[i])) continue;
            double abs = Math.abs(data[i]);
            if (Double.isNaN(max)|| abs > max) max = abs;
        }
        return max;
    }

    /**
     * Find the minimum of all elements in the array, ignoring elements that are NaN.
     * @param data
     * @return
     */
    public static double min(double[] data)
    {
        double min = Double.NaN;
        for (int i=0; i<data.length; i++) {
            if (Double.isNaN(data[i])) continue;
            if (Double.isNaN(min)|| data[i] < min) min = data[i];
        }
        return min;
    }

    public static int min(int[] data)
    {
        int min = data[0];
        for (int i=1; i<data.length; i++) {
            if (data[i] < min) 
                min = data[i];
        }
        return min;
    }

    public static double mean(double[] data)
    {
        return mean(data, 0, data.length-1);
    }

    /**
     * Compute the mean of all elements in the array. No missing values (NaN) are allowed.
     * @throws IllegalArgumentException if the array contains NaN values. 
     */
    public static double mean(double[] data, int startIndex, int endIndex)
    {
        double mean = 0;
        int total = 0;
        startIndex = Math.max(startIndex, 0);
        startIndex = Math.min(startIndex, data.length-1);
        endIndex = Math.max(endIndex, 0);
        endIndex = Math.min(endIndex, data.length-1);

        if (startIndex>endIndex)
            startIndex = endIndex;

        for (int i=startIndex; i<=endIndex; i++) {
            if (Double.isNaN(data[i]))
                throw new IllegalArgumentException("NaN not allowed in mean calculation");
            mean += data[i];
            total++;
        }
        mean /= total;
        return mean;
    }

    /**
     * Compute the mean of all elements in the array with given indices. No missing values (NaN) are allowed.
     * @throws IllegalArgumentException if the array contains NaN values. 
     */
    public static double mean(double[] data, int [] inds)
    {
        double mean = 0;
        for (int i=0; i<inds.length; i++) {
            if (Double.isNaN(data[inds[i]]))
                throw new IllegalArgumentException("NaN not allowed in mean calculation");

            mean += data[inds[i]];
        }
        mean /= inds.length;
        return mean;
    }

    /**
     * Compute the mean of all elements in the array. No missing values (NaN) are allowed.
     * @throws IllegalArgumentException if the array contains NaN values. 
     */
    public static float mean(float[] data, int startIndex, int endIndex)
    {
        float mean = 0;
        int total = 0;
        startIndex = Math.max(startIndex, 0);
        startIndex = Math.min(startIndex, data.length-1);
        endIndex = Math.max(endIndex, 0);
        endIndex = Math.min(endIndex, data.length-1);

        if (startIndex>endIndex)
            startIndex = endIndex;

        for (int i=startIndex; i<=endIndex; i++) {
            if (Float.isNaN(data[i]))
                throw new IllegalArgumentException("NaN not allowed in mean calculation");
            mean += data[i];
            total++;
        }
        mean /= total;
        return mean;
    }

    public static float mean(float[] data)
    {
        return mean(data, 0, data.length-1);
    }

    /**
     * Compute the mean of all elements in the array with given indices. No missing values (NaN) are allowed.
     * @throws IllegalArgumentException if the array contains NaN values. 
     */
    public static float mean(float[] data, int [] inds)
    {
        float mean = 0;
        for (int i=0; i<inds.length; i++) {
            if (Float.isNaN(data[inds[i]]))
                throw new IllegalArgumentException("NaN not allowed in mean calculation");

            mean += data[inds[i]];
        }
        mean /= inds.length;
        return mean;
    }
    
    /**
     * Compute the mean of all elements in the array. this function can deal with NaNs
     * @param data double[]
     * @param opt 0:  arithmetic mean<p>
                  1:  geometric mean<p>
     */
    public static double mean(double[] data, int opt)
    {
      if(opt==0){
        int numData = 0;
        double mean = 0;
        for (int i=0; i<data.length; i++) {
          if (!Double.isNaN(data[i])){
            mean += data[i];
            numData++;
          }
        }
        mean /= numData;
        return mean;
      } else {
        int numData = 0;
        double mean = 0;
        for (int i=0; i<data.length; i++) {
          if (!Double.isNaN(data[i])){
            mean += Math.log(data[i]);
            numData++;
          }
        }
        mean = mean/numData;        
        return Math.exp(mean);        
      }
        
    }
    

    public static double standardDeviation(double[] data)
    {
        return standardDeviation(data, mean(data));
    }

    public static double standardDeviation(double[] data, double meanVal)
    {
        return standardDeviation(data, meanVal, 0, data.length-1);
    }

    public static double standardDeviation(double[] data, double meanVal, int startIndex, int endIndex)
    {
        return Math.sqrt(variance(data, meanVal, startIndex, endIndex));
    }
    
    /**
     * Compute the standard deviation of the given data, this function can deal with NaNs
     * @param data double[]
     * @param opt 0:  normalizes with N-1, this provides the square root of best unbiased estimator of the variance<p>
                  1:  normalizes with N, this provides the square root of the second moment around the mean<p>
     * @return
     */
    public static double standardDeviation(double[] data, int opt) {
        if(opt==0)
          return Math.sqrt(variance(data,opt));
        else
          return Math.sqrt(variance(data,opt));
    }

    /**
     * Compute the variance in the array. This function can deal with NaNs
     * @param data double[]
     * @param opt 0:  normalizes with N-1, this provides the square root of best unbiased estimator of the variance<p>
                  1:  normalizes with N, this provides the square root of the second moment around the mean<p>
     */
    public static double variance(double[] data, int opt)
    {
      // Pseudocode from wikipedia, which cites Knuth:
      // n = 0
      // mean = 0
      // S = 0
      // foreach x in data:
      //   n = n + 1
      //   delta = x - mean
      //   mean = mean + delta/n
      //   S = S + delta*(x - mean)      // This expression uses the new value of mean
      // end for
      // variance = S/(n - 1)
      double mean = 0;
      double S = 0;
      double numData=0;
      for (int i=0; i< data.length; i++) {
        if (!Double.isNaN(data[i])){
          double delta = data[i] - mean;           
          mean += delta / (numData+1);
          S += delta * (data[i] - mean);
          numData++;
        }
      }
      if(opt==0)
        return (S/(numData-1));
      else
        return (S/numData);
    }

    
    public static double variance(double[] data)
    {
        return variance(data, mean(data));
    }
    
    public static float variance(float[] data)
    {
        return variance(data, mean(data));
    }

    public static double variance(double[] data, double meanVal)
    {
        return variance(data, meanVal, 0, data.length-1);
    }
    
    public static float variance(float[] data, float meanVal)
    {
        return variance(data, meanVal, 0, data.length-1);
    }

    public static float variance(float[] data, float meanVal, int startIndex, int endIndex)
    {
        double[] ddata = new double[data.length];
        for (int i=0; i<data.length; i++)
            ddata[i] = data[i];
        
        return (float)variance(ddata, meanVal, startIndex, endIndex);
    }
    
    public static double variance(double[] data, double meanVal, int startIndex, int endIndex)
    {
        double var = 0.0;

        if (startIndex<0)
            startIndex=0;
        if (startIndex>data.length-1)
            startIndex=data.length-1;
        if (endIndex<startIndex)
            endIndex=startIndex;
        if (endIndex>data.length-1)
            endIndex=data.length-1;

        for (int i=startIndex; i<=endIndex; i++)
            var += (data[i]-meanVal)*(data[i]-meanVal);

        if (endIndex-startIndex>1)
            var /= (endIndex-startIndex);

        return var;
    }

    public static double[] variance(double[][]x, double[] meanVector)
    {
        return variance(x, meanVector, true);   
    }

    /**
     * Returns the variance of rows or columns of matrix x
     * @param x the matrix consisting of row vectors
     * @param mean the vector of mean values -- a column vector if row-wise variances are to be
     * computed, or a row vector if column-wise variances are to be calculated.
     * param isAlongRows if true, compute the variance of x[0][0], x[1][0] etc. given mean[0];
     * if false, compute the variances for the vectors x[0], x[1] etc. separately, given the respective mean[0], mean[1] etc.
     */
    public static double[] variance(double[][]x, double[] meanVector, boolean isAlongRows)
    {
        double[] var = null;

        if (x!=null && x[0]!=null && x[0].length>0 && meanVector != null)
        {
            if (isAlongRows) 
            {
                var = new double[x[0].length];
                int j, i;
                for (j=0; j<x[0].length; j++)
                {
                    for (i=0; i<x.length; i++)
                        var[j] += (x[i][j]-meanVector[j])*(x[i][j]-meanVector[j]);

                    var[j] /= (x.length-1);
                }
            } 
            else 
            {
                var = new double[x.length];
                for (int i=0; i<x.length; i++) {
                    var[i] = variance(x[i], meanVector[i]);
                }
            }
        }

        return var;
    }

    public static double[] mean(double[][] x)
    {
        return mean(x, true);
    }

    public static double[] mean(double[][] x, boolean isAlongRows)
    {
        int[] indices = null;
        int i;

        if (isAlongRows)
        {
            indices = new int[x.length];
            for (i=0; i<x.length; i++)
                indices[i] = i;
        }
        else
        {
            indices = new int[x[0].length]; 
            for (i=0; i<x[0].length; i++)
                indices[i] = i;
        }

        return mean(x, isAlongRows, indices);
    }

    //If isAlongRows==true, the observations are row-by-row
    // if isAlongRows==false, they are column-by-column
    public static double[] mean(double[][] x, boolean isAlongRows, int[] indicesOfX)
    {
        double[] meanVector = null;
        int i, j;
        if (isAlongRows)
        {
            meanVector = new double[x[indicesOfX[0]].length];
            Arrays.fill(meanVector, 0.0);

            for (i=0; i<indicesOfX.length; i++)
            {
                for (j=0; j<x[indicesOfX[0]].length; j++)
                    meanVector[j] += x[indicesOfX[i]][j];
            }

            for (j=0; j<meanVector.length; j++)
                meanVector[j] /= indicesOfX.length;
        }
        else
        {
            meanVector = new double[x.length];
            Arrays.fill(meanVector, 0.0);

            for (i=0; i<indicesOfX.length; i++)
            {
                for (j=0; j<x.length; j++)
                    meanVector[j] += x[j][indicesOfX[i]];
            }

            for (j=0; j<meanVector.length; j++)
                meanVector[j] /= indicesOfX.length;
        }

        return meanVector;
    }

    //The observations are taken row by row
    public static double[][] covariance(double[][] x)
    {
        return covariance(x, true);
    }

    //The observations are taken row by row
    public static double[][] covariance(double[][] x, double[] meanVector)
    {        
        return covariance(x, meanVector, true);
    }

    //If isAlongRows==true, the observations are row-by-row
    // if isAlongRows==false, they are column-by-column
    public static double[][] covariance(double[][] x, boolean isAlongRows)
    {
        double[] meanVector = mean(x, isAlongRows);

        return covariance(x, meanVector, isAlongRows);
    }

    public static double[][] covariance(double[][] x, double[] meanVector, boolean isAlongRows)
    {

        int[] indices = null;
        int i;

        if (isAlongRows)
        {
            indices = new int[x.length];
            for (i=0; i<x.length; i++)
                indices[i] = i;
        }
        else
        {
            indices = new int[x[0].length]; 
            for (i=0; i<x[0].length; i++)
                indices[i] = i;
        }

        return covariance(x, meanVector, isAlongRows, indices);
    }

    //If isAlongRows==true, the observations are row-by-row
    // if isAlongRows==false, they are column-by-column
    public static double[][] covariance(double[][] x, double[] meanVector, boolean isAlongRows, int[] indicesOfX)
    {
        int numObservations;
        int dimension;
        int i, j, p;
        double[][] cov = null;
        double[][] tmpMatrix = null;
        double[][] zeroMean = null;
        double[][] zeroMeanTranspoze = null;

        if (x!=null && meanVector!=null)
        {
            if (isAlongRows)
            {
                for (i=0; i<indicesOfX.length; i++)
                    assert meanVector.length == x[indicesOfX[i]].length;

                numObservations = indicesOfX.length;
                dimension = x[indicesOfX[0]].length;

                cov = new double[dimension][dimension];
                tmpMatrix = new double[dimension][dimension];
                zeroMean = new double[dimension][1];
                double[] tmpVector;

                for (i=0; i<dimension; i++)
                    Arrays.fill(cov[i], 0.0);

                for (i=0; i<numObservations; i++)
                {
                    tmpVector = subtract(x[indicesOfX[i]], meanVector);
                    zeroMean = transpoze(tmpVector);
                    zeroMeanTranspoze = transpoze(zeroMean);

                    tmpMatrix = matrixProduct(zeroMean, zeroMeanTranspoze);
                    cov = add(cov, tmpMatrix);
                }

                cov = divide(cov, numObservations-1);
            }
            else
            {
                assert meanVector.length == x.length;
                numObservations = indicesOfX.length;

                for (i=1; i<indicesOfX.length; i++)
                    assert x[indicesOfX[i]].length==x[indicesOfX[0]].length;

                dimension = x.length;

                cov = transpoze(covariance(transpoze(x), meanVector, true, indicesOfX));
            }
        }

        return cov;
    }
    
    /***
     * Sample correlation coefficient
     * Ref: http://en.wikipedia.org/wiki/Correlation_and_dependence
     * @return
     */   
    public static double correlation(double[] x, double[] y){
      
      if(x.length == y.length){
        // mean
        double mx = MathUtils.mean(x);
        double my = MathUtils.mean(y);
        // standard deviation
        double sx = Math.sqrt(MathUtils.variance(x));
        double sy = Math.sqrt(MathUtils.variance(y));
        
        int n = x.length;
        double nval=0.0;
        for(int i=0; i<n; i++){
          nval += (x[i] - mx) * (y[i] - my);        
        }
        double r = nval / ( (n-1) * sx * sy);
                
        return r;      
      } else
        throw new IllegalArgumentException("vectors of different size");
    }
    

    public static double[] diagonal(double[][]x)
    {
        double[] d = null;
        int dim = x.length;
        int i;
        for (i=1; i<dim; i++)
            assert x[i].length==dim;

        if (x!=null)
        {
            d = new double[dim];

            for (i=0; i<x.length; i++)
                d[i] = x[i][i]; 
        }

        return d;
    }
    
    public static double[][] toDiagonalMatrix(double[] x)
    {
        double[][] m = null;
        
        if (x!=null && x.length>0)
        {
            m = new double[x.length][x.length];
            int i;
            for (i=0; i<x.length; i++)
                Arrays.fill(m[i], 0.0);
            
            for (i=0; i<x.length; i++)
                m[i][i] = x[i];
        }
        
        return m;
    }

    public static double[][] transpoze(double[] x)
    {
        double[][] y = new double[x.length][1];
        for (int i=0; i<x.length; i++)
            y[i][0] = x[i];

        return y;
    }

    public static double[][] transpoze(double[][] x)
    {
        double[][] y = null;

        if (x!=null)
        {
            int i, j;
            int rowSizex = x.length;
            int colSizex = x[0].length;
            for (i=1; i<rowSizex; i++)
                assert x[i].length==colSizex;

            y = new double[colSizex][rowSizex];
            for (i=0; i<rowSizex; i++)
            {
                for (j=0; j<colSizex; j++)
                    y[j][i] = x[i][j];
            }
        }

        return y;
    }
    
    public static ComplexNumber[][] transpoze(ComplexNumber[][] x)
    {
        ComplexNumber[][] y = null;

        if (x!=null)
        {
            int i, j;
            int rowSizex = x.length;
            int colSizex = x[0].length;
            for (i=1; i<rowSizex; i++)
                assert x[i].length==colSizex;

            y = new ComplexNumber[colSizex][rowSizex];
            for (i=0; i<rowSizex; i++)
            {
                for (j=0; j<colSizex; j++)
                    y[j][i] = new ComplexNumber(x[i][j]);
            }
        }

        return y;
    }
    
    public static ComplexNumber[][] hermitianTranspoze(ComplexNumber[][] x)
    {
        ComplexNumber[][] y = null;

        if (x!=null)
        {
            int i, j;
            int rowSizex = x.length;
            int colSizex = x[0].length;
            for (i=1; i<rowSizex; i++)
                assert x[i].length==colSizex;

            y = new ComplexNumber[colSizex][rowSizex];
            for (i=0; i<rowSizex; i++)
            {
                for (j=0; j<colSizex; j++)
                    y[j][i] = new ComplexNumber(x[i][j].real, -1.0*x[i][j].imag);
            }
        }

        return y;
    }
    
    public static ComplexNumber[][] diagonalComplexMatrix(double[] diag)
    {
        ComplexNumber[][] x = null;
        int N = diag.length;
        if (N>0)
        {
            x = new ComplexNumber[N][N];
            for (int i=0; i<N; i++)
            {
                for (int j=0; j<N; j++)
                {
                    if (i==j)
                        x[i][j] = new ComplexNumber(diag[i], 0.0);
                    else
                        x[i][j] = new ComplexNumber(0.0, 0.0);
                }
            }
        }
        
        return x;
    }
    
    public static double[][] diagonalMatrix(double[] diag)
    {
        double[][] x = null;
        int N = diag.length;
        if (N>0)
        {
            x = new double[N][N];
            for (int i=0; i<N; i++)
            {
                for (int j=0; j<N; j++)
                {
                    if (i==j)
                        x[i][j] = diag[i];
                    else
                        x[i][j] = 0.0;
                }
            }
        }
        
        return x;
    }
    
    public static ComplexNumber ampPhase2ComplexNumber(double amp, double phaseInRadians)
    {
        return new ComplexNumber(amp*Math.cos(phaseInRadians), amp*Math.sin(phaseInRadians));
    }
    
    public static ComplexNumber[] polar2complex(double[] amps, float[] phasesInRadian)
    {
        if (amps.length!=phasesInRadian.length) {
        	throw new IllegalArgumentException("Arrays must have same length, but are "+amps.length+" vs. "+phasesInRadian.length);
        }
        
        ComplexNumber[] comps = new ComplexNumber[amps.length];
        for (int i=0; i<amps.length; i++)
            comps[i] = ampPhase2ComplexNumber(amps[i], phasesInRadian[i]);
        
        return comps;
    }

    public static ComplexNumber[] polar2complex(double[] amps, double[] phasesInRadian)
    {
        if (amps.length!=phasesInRadian.length) {
        	throw new IllegalArgumentException("Arrays must have same length, but are "+amps.length+" vs. "+phasesInRadian.length);
        }
       
        ComplexNumber[] comps = new ComplexNumber[amps.length];
        for (int i=0; i<amps.length; i++)
            comps[i] = ampPhase2ComplexNumber(amps[i], phasesInRadian[i]);
       
        return comps;
    }
    public static double[] add(double[] x, double[] y)
    {
        assert x.length==y.length;
        double[] z = new double[x.length];
        for (int i=0; i<x.length; i++)
            z[i] = x[i]+y[i];

        return z;
    }

    public static double[] add(double[] a, double b)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] + b;
        }
        return c;        
    }

    public static double[] subtract(double[] a, double[] b)
    {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] - b[i];
        }
        return c;
    }

    public static double[] subtract(double[] a, double b)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] - b;
        }
        return c;
    }

    public static double[] multiply(double[] a, double[] b)
    {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] * b[i];
        }
        return c;        
    }
    
    public static float[] multiply(float[] a, float[] b)
    {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        float[] c = new float[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] * b[i];
        }
        return c;        
    }

    public static double[] multiply(double[] a, double b)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] * b;
        }
        return c;        
    }
    
    public static float[] multiply(float[] a, float b)
    {
        float[] c = new float[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] * b;
        }
        return c;        
    }
    
    /**
     * Returns the multiplicative inverse (element-wise 1/x) of an array
     * 
     * @param a
     *            array to invert
     * @return a new array of the same size as <b>a</b>, in which each element is equal to the multiplicative inverse of the
     *         corresponding element in <b>a</b>
     * @throws IllegalArgumentException
     *             if the array is null
     */
    public static double[] invert(double[] a) throws IllegalArgumentException {
        if (a == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = 1.0 / a[i];
        }
        return c;
    }

    /**
     * @see #invert(double[])
     */
    public static float[] invert(float[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
        float[] c = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = 1.0f / a[i];
        }
        return c;
    }
    
    public static ComplexNumber[] multiplyComplex(ComplexNumber[] a, double b)
    {
        ComplexNumber[] c = new ComplexNumber[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = MathUtils.multiply(b, a[i]);
        }
        return c;        
    }
    
    public static ComplexNumber complexConjugate(ComplexNumber x)
    {
        return new ComplexNumber(x.real, -1.0*x.imag);
    }
    
    public static ComplexNumber complexConjugate(double xReal, double xImag)
    {
        return new ComplexNumber(xReal, -1.0*xImag);
    }
    
    public static ComplexNumber addComplex(ComplexNumber x1, ComplexNumber x2)
    {
        return new ComplexNumber(x1.real+x2.real, x1.imag+x2.imag);
    }
    
    public static ComplexNumber addComplex(ComplexNumber x, double yReal, double yImag)
    {
        return new ComplexNumber(x.real+yReal, x.imag+yImag);
    }
    
    public static ComplexNumber addComplex(double yReal, double yImag, ComplexNumber x)
    {
        return new ComplexNumber(x.real+yReal, x.imag+yImag);
    }
    
    public static ComplexNumber addComplex(double xReal, double xImag, double yReal, double yImag)
    {
        return new ComplexNumber(xReal+yReal, xImag+yImag);
    }
    
    public static ComplexNumber subtractComplex(ComplexNumber x1, ComplexNumber x2)
    {
        return new ComplexNumber(x1.real-x2.real, x1.imag-x2.imag);
    }
    
    public static ComplexNumber subtractComplex(ComplexNumber x, double yReal, double yImag)
    {
        return new ComplexNumber(x.real-yReal, x.imag-yImag);
    }
    
    public static ComplexNumber subtractComplex(double yReal, double yImag, ComplexNumber x)
    {
        return new ComplexNumber(yReal-x.real, yImag-x.imag);
    }
    
    public static ComplexNumber subtractComplex(double xReal, double xImag, double yReal, double yImag)
    {
        return new ComplexNumber(xReal-yReal, xImag-yImag);
    }
    
    public static ComplexNumber multiplyComplex(ComplexNumber x1, ComplexNumber x2)
    {
        return new ComplexNumber(x1.real*x2.real-x1.imag*x2.imag, x1.real*x2.imag+x1.imag*x2.real);
    }
    
    public static ComplexNumber multiplyComplex(ComplexNumber x, double yReal, double yImag)
    {
        return new ComplexNumber(x.real*yReal-x.imag*yImag, x.real*yImag+x.imag*yReal);
    }
    
    public static ComplexNumber multiplyComplex(double yReal, double yImag, ComplexNumber x)
    {
        return new ComplexNumber(x.real*yReal-x.imag*yImag, x.real*yImag+x.imag*yReal);
    }

    public static ComplexNumber multiplyComplex(double xReal, double xImag, double yReal, double yImag)
    {
        return new ComplexNumber(xReal*yReal-xImag*yImag, xReal*yImag+xImag*yReal);
    }
    
    public static ComplexNumber multiply(double x1, ComplexNumber x2)
    {
        return new ComplexNumber(x1*x2.real, x1*x2.imag);
    }
    
    public static ComplexNumber divideComplex(ComplexNumber x, double yReal, double yImag)
    {
        double denum = magnitudeComplexSquared(yReal, yImag);

        return new ComplexNumber((x.real*yReal+x.imag*yImag)/denum, (x.imag*yReal-x.real*yImag)/denum);
    }
    
    public static ComplexNumber divideComplex(double yReal, double yImag, ComplexNumber x)
    {
        double denum = magnitudeComplexSquared(x.real, x.imag);

        return new ComplexNumber((yReal*x.real+yImag*x.imag)/denum, (yImag*x.real-yReal*x.imag)/denum);
    }
    
    public static ComplexNumber divideComplex(ComplexNumber x1, ComplexNumber x2)
    {
        double denum = magnitudeComplexSquared(x2.real, x2.imag);

        return new ComplexNumber((x1.real*x2.real+x1.imag*x2.imag)/denum, (x1.imag*x2.real-x1.real*x2.imag)/denum);
    }
    
    public static ComplexNumber divideComplex(double xReal, double xImag, double yReal, double yImag)
    {
        double denum = magnitudeComplexSquared(yReal, yImag);

        return new ComplexNumber((xReal*yReal+xImag*yImag)/denum, (xImag*yReal-xReal*yImag)/denum);
    }
    
    public static ComplexNumber divide(ComplexNumber x1, double x2)
    {
        return new ComplexNumber(x1.real/x2, x1.imag/x2);
    }
    
    public static ComplexNumber divide(double x1, ComplexNumber x2)
    {
        return divideComplex(x1, 0.0, x2);
    }
    
    public static double magnitudeComplexSquared(ComplexNumber x)
    {
        return x.real*x.real+x.imag*x.imag;
    }
    
    public static double magnitudeComplexSquared(double xReal, double xImag)
    {
        return xReal*xReal+xImag*xImag;
    }
    
    public static double magnitudeComplex(ComplexNumber x)
    {
        return Math.sqrt(magnitudeComplexSquared(x));
    }
    
    public static double[] magnitudeComplex(ComplexNumber[] xs)
    {
        double[] mags = new double[xs.length];
        
        for (int i=0; i<xs.length; i++)
            mags[i] = magnitudeComplex(xs[i]);
        
        return mags;
    }
    
    public static double[] magnitudeComplex(ComplexArray x)
    {
        assert x.real.length == x.imag.length;
        double[] mags = new double[x.real.length];
        
        for (int i=0; i<x.real.length; i++)
            mags[i] = magnitudeComplex(new ComplexNumber(x.real[i], x.imag[i]));
        
        return mags;
    }
    
    public static double magnitudeComplex(double xReal, double xImag)
    {
        return Math.sqrt(magnitudeComplexSquared(xReal, xImag));
    }
    
    public static double phaseInRadians(ComplexNumber x)
    {
        /*
        double modul = MathUtils.magnitudeComplex(x); // modulus
        double phase = Math.atan2(x.imag, x.real); // use atan2: theta ranges from [-pi,pi]

        if (x.imag<0.0) // lower half plane (Im<0), needs shifting
        {
            phase += MathUtils.TWOPI; // shift by adding 2pi to lower half plane
            
            // fix the discontinuity between phase = 0 and phase = 2pi
            if (x.real>0.0 && x.imag<0.0 && Math.abs(x.imag)<1e-10)
                phase = 0.0;
        }
        
        return phase;
        */
        
        return Math.atan2(x.imag, x.real); 
    }
    
    public static float phaseInRadiansFloat(ComplexNumber x)
    {
        return (float)phaseInRadians(x); 
    }
    
    public static double phaseInRadians(double xReal, double xImag)
    {
        return phaseInRadians(new ComplexNumber(xReal, xImag));
    }
    
    public static double[] phaseInRadians(ComplexNumber[] xs)
    {
        double[] phases = new double[xs.length];
        
        for (int i=0; i<xs.length; i++)
            phases[i] = phaseInRadians(xs[i]);
        
        return phases;
    }
    
    public static float[] phaseInRadiansFloat(ComplexNumber[] xs)
    {
        float[] phases = new float[xs.length];
        
        for (int i=0; i<xs.length; i++)
            phases[i] = phaseInRadiansFloat(xs[i]);
        
        return phases;
    }
    
    public static double[] phaseInRadians(ComplexArray x)
    {
        assert x.real.length==x.imag.length;
        
        double[] phases = new double[x.real.length];
        
        for (int i=0; i<x.real.length; i++)
            phases[i] = phaseInRadians(x.real[i], x.imag[i]);
        
        return phases;
    }
    
    //Returns a+jb such that a+jb=r.exp(j.theta) where theta is in radians
    public static ComplexNumber complexNumber(double r, double theta)
    {
        return new ComplexNumber(r*Math.cos(theta), r*Math.sin(theta));
    }
    
    public static double[] divide(double[] a, double[] b)
    {
        if (a == null || b == null || a.length != b.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] / b[i];
        }
        return c;
    }

    public static double[] divide(double[] a, double b)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] / b;
        }
        return c;
    }

    //Returns the summ of two matrices, i.e. x+y
    //x and y should be of same size
    public static double[][] add(double[][] x, double[][] y)
    {
        double[][] z = null;

        if (x!=null && y!=null)
        {
            int i, j;
            assert x.length==y.length;
            for (i=0; i<x.length; i++)
            {
                assert x[i].length==x[0].length;
                assert x[i].length==y[i].length;
            }

            z = new double[x.length][x[0].length];


            for (i=0; i<x.length; i++)
            {
                for (j=0; j<x[i].length; j++)
                    z[i][j] = x[i][j]+y[i][j];
            }
        }

        return z;
    }

    //Returns the difference of two matrices, i.e. x-y
    //x and y should be of same size
    public static double[][] subtract(double[][] x, double[][] y)
    {
        double[][] z = null;

        if (x!=null && y!=null)
        {
            int i, j;
            assert x.length==y.length;
            for (i=0; i<x.length; i++)
            {
                assert x[i].length==x[0].length;
                assert x[i].length==y[i].length;
            }

            z = new double[x.length][x[0].length];


            for (i=0; i<x.length; i++)
            {
                for (j=0; j<x[i].length; j++)
                    z[i][j] = x[i][j]-y[i][j];
            }
        }

        return z;
    }

    //Returns multiplication of matrix entries with a constant, i.e. ax
    //x and y should be of same size
    public static double[][] multiply(double a, double[][] x)
    {
        double[][] z = null;

        if (x!=null)
        {
            int i, j;
            for (i=1; i<x.length; i++)
                assert x[i].length==x[0].length;

            z = new double[x.length][x[0].length];

            for (i=0; i<x.length; i++)
            {
                for (j=0; j<x[i].length; j++)
                    z[i][j] = a*x[i][j];
            }
        }

        return z;
    }

    //Returns the division of matrix entries with a constant, i.e. x/a
    //x and y should be of same size
    public static double[][] divide(double[][] x, double a)
    {
        return multiply(1.0/a, x);
    }
    
    //Matrix of size NxM multiplied by an appropriate sized vector, i.e. Mx1, returns a vector of size Nx1
    public static double[] matrixProduct(double[][] x, double[] y)
    {
        double[][] y2 = new double[y.length][1];
        int i;
        for (i=0; i<y.length; i++)
            y2[i][0] = y[i];
        
        y2 = matrixProduct(x, y2);
        
        double[] y3 = new double[y2.length];
        for (i=0; i<y2.length; i++)
            y3[i] = y2[i][0];
        
        return y3;
    }
    
    public static double[] matrixProduct(double[][] x, float[] y)
    {
        double[][] y2 = new double[y.length][1];
        int i;
        for (i=0; i<y.length; i++)
            y2[i][0] = y[i];
        
        y2 = matrixProduct(x, y2);
        
        double[] y3 = new double[y2.length];
        for (i=0; i<y2.length; i++)
            y3[i] = y2[i][0];
        
        return y3;
    }
    
    public static ComplexNumber[] matrixProduct(ComplexNumber[][] x, ComplexNumber[] y)
    {
        ComplexNumber[][] y2 = new ComplexNumber[y.length][1];
        int i;
        for (i=0; i<y.length; i++)
            y2[i][0] = new ComplexNumber(y[i]);
        
        y2 = matrixProduct(x, y2);
        
        ComplexNumber[] y3 = new ComplexNumber[y2.length];
        for (i=0; i<y2.length; i++)
            y3[i] = new ComplexNumber(y2[i][0]);
        
        return y3;
    }
    
    public static ComplexNumber[] matrixProduct(ComplexNumber[][] x, double[] y)
    {
        ComplexNumber[][] y2 = new ComplexNumber[y.length][1];
        int i;
        for (i=0; i<y.length; i++)
            y2[i][0] = new ComplexNumber(y[i], 0.0);
        
        y2 = matrixProduct(x, y2);
        
        ComplexNumber[] y3 = new ComplexNumber[y2.length];
        for (i=0; i<y2.length; i++)
            y3[i] = new ComplexNumber(y2[i][0]);
        
        return y3;
    }
    
    //Vector of size N is multiplied with matrix of size NxM
    // Returns a matrix of size NxM
    public static double[][] matrixProduct(double[] x, double[][] y)
    {
        double[][] x2 = new double[x.length][1];
        int i;
        for (i=0; i<x.length; i++)
            x2[i][0] = x[i];
        
        return matrixProduct(x2, y);
    }
    
    public static ComplexNumber[][] matrixProduct(ComplexNumber[] x, ComplexNumber[][] y)
    {
        ComplexNumber[][] x2 = new ComplexNumber[x.length][1];
        int i;
        for (i=0; i<x.length; i++)
            x2[i][0] = new ComplexNumber(x[i]);
        
        return matrixProduct(x2, y);
    }

    //This is a "*" product --> should return a matrix provided that the sizes are appropriate
    public static double[][] matrixProduct(double[][] x, double[][] y)
    {
        double[][] z = null;

        if (x!=null && y!=null)
        {
            if (x.length==1 && y.length==1) //Special case -- diagonal matrix multiplication, returns a diagonal matrix
            {
                assert x[0].length==y[0].length;
                z = new double[1][x[0].length];
                for (int i=0; i<x[0].length; i++)
                    z[0][i] = x[0][i]*y[0][i];
            }
            else
            {
                int i, j, m;
                int rowSizex = x.length;
                int colSizex = x[0].length;
                int rowSizey = y.length;
                int colSizey = y[0].length;
                for (i=1; i<x.length; i++)
                    assert x[i].length == colSizex;
                for (i=1; i<y.length; i++)
                    assert y[i].length == colSizey;
                assert colSizex==rowSizey;

                z = new double[rowSizex][colSizey];
                double tmpSum;
                for (i=0; i<rowSizex; i++)
                {
                    for (j=0; j<colSizey; j++)
                    {
                        tmpSum = 0.0;
                        for (m=0; m<x[i].length; m++)
                            tmpSum += x[i][m]*y[m][j];

                        z[i][j] = tmpSum;
                    }
                }
            }
        }

        return z;
    }

    //This is a "*" product --> should return a matrix provided that the sizes are appropriate
    public static ComplexNumber[][] matrixProduct(ComplexNumber[][] x, ComplexNumber[][] y)
    {
        ComplexNumber[][] z = null;

        if (x!=null && y!=null)
        {
            if (x.length==1 && y.length==1) //Special case -- diagonal matrix multiplication, returns a diagonal matrix
            {
                assert x[0].length==y[0].length;
                z = new ComplexNumber[1][x[0].length];
                for (int i=0; i<x[0].length; i++)
                    z[0][i] = multiplyComplex(x[0][i],y[0][i]);
            }
            else
            {
                int i, j, m;
                int rowSizex = x.length;
                int colSizex = x[0].length;
                int rowSizey = y.length;
                int colSizey = y[0].length;
                for (i=1; i<x.length; i++)
                    assert x[i].length == colSizex;
                for (i=1; i<y.length; i++)
                    assert y[i].length == colSizey;
                assert colSizex==rowSizey;

                z = new ComplexNumber[rowSizex][colSizey];
               
                /** Marc Schröder, 3 July 2009: The following implementation used up
                 * about 93% of total processing time. Replacing it with a less elegant
                 * but more efficient implementation:
                
                ComplexNumber tmpSum;
                for (i=0; i<rowSizex; i++)
                {
                    for (j=0; j<colSizey; j++)
                    {
                        tmpSum = new ComplexNumber(0.0, 0.0);
                        for (m=0; m<x[i].length; m++)
                            tmpSum = addComplex(tmpSum, multiplyComplex(x[i][m],y[m][j]));

                        z[i][j] = new ComplexNumber(tmpSum);
                    }
                }
                 */
                
                for (i=0; i<rowSizex; i++)
                {
                    for (j=0; j<colSizey; j++)
                    {
                        float real = 0f, imag = 0f;
                        for (m=0; m<x[i].length; m++) {
                            ComplexNumber x1 = x[i][m];
                            ComplexNumber x2 = y[m][j];
                            real += x1.real*x2.real-x1.imag*x2.imag;
                            imag += x1.real*x2.imag+x1.imag*x2.real;
                        }

                        z[i][j] = new ComplexNumber(real, imag);
                    }
                }
            }
        }

        return z;
    }
    
    //"x" product of two vectors
    public static double[][] vectorProduct(double[] x, boolean isColumnVectorX, double[] y, boolean isColumnVectorY)
    {
        double[][] xx = null;
        double[][] yy = null;
        int i;
        if (isColumnVectorX)
        {
            xx = new double[x.length][1];
            for (i=0; i<x.length; i++)
                xx[i][0] = x[i];
        }
        else
        {
            xx = new double[1][x.length];
            System.arraycopy(x, 0, xx[0], 0, x.length);
        }

        if (isColumnVectorY)
        {
            yy = new double[y.length][1];
            for (i=0; i<y.length; i++)
                yy[i][0] = y[i];
        }
        else
        {
            yy = new double[1][y.length];
            System.arraycopy(y, 0, yy[0], 0, y.length);
        }

        return matrixProduct(xx, yy);
    }

    public static double dotProduct(double[] x, double[] y)
    {
        assert x.length==y.length;

        double tmpSum = 0.0;
        for (int i=0; i<x.length; i++)
            tmpSum += x[i]*y[i];

        return tmpSum;
    }

    public static double[][] dotProduct(double[][] x, double[][] y)
    {
        double[][] z = null;
        assert x.length==y.length;
        int numRows = x.length;
        int numCols = x[0].length;
        int i;
        for (i=1; i<numRows; i++)
        {
            assert numCols == x[i].length;
            assert numCols == y[i].length;
        }

        if (x!=null)
        {
            int j;
            z = new double[numRows][numCols];
            for (i=0; i<numRows; i++)
            {
                for (j=0; j<numCols; j++)
                    z[i][j] = x[i][j]*y[i][j];
            }
        }

        return z;
    }

    /**
     * Convert energy from linear scale to db SPL scale (comparing energies to  
     * the minimum audible energy, one Pascal squared).
     * @param energy in time or frequency domain, on a linear energy scale
     * @return energy on a db scale, or NaN if energy is less than or equal to 0.
     */
    public static double dbSPL(double energy)
    {
        if (energy <= 0) return Double.NaN;
        else return 10 * log10(energy/PASCALSQUARE);
    }

    public static double[] dbSPL(double[] energies)
    {
        return multiply(log10(divide(energies, PASCALSQUARE)), 10);
    }

    /**
     * Convert energy from linear scale to db scale.
     * @param energy in time or frequency domain, on a linear energy scale
     * @return energy on a db scale, or NaN if energy is less than or equal to 0.
     */
    public static double db(double energy)
    {
        if (energy <= 1e-80) return -200.0;
        else return 10 * log10(energy);
    }

    public static double amp2db(double amp)
    {
        if (amp <= 1e-80) return -200.0;
        else return 20 * log10(amp);
    }
    
    public static double amp2neper(double amp)
    {
        if (amp <= 1e-80) return -200.0;
        else return Math.log(amp);
    }

    public static double[] db(double[] energies)
    {
        return multiply(log10(energies), 10);
    }
    
    public static double[] abs(ComplexArray c)
    {
        int len = Math.min(c.real.length, c.imag.length);
        
        return abs(c, 0, len-1);
    }
    
    public static double[] abs(ComplexNumber[] x)
    {
        double[] absMags = null;
        
        if (x.length>0)
        {
            absMags = new double[x.length];
         
            for (int i=0; i<x.length; i++)
                absMags[i] = magnitudeComplex(x[i]);
        }
        
        return absMags;
    }
    
    public static double[] abs(ComplexArray c, int startInd, int endInd)
    {
        if (startInd<0)
            startInd=0;
        if (startInd>Math.min(c.real.length-1,c.imag.length-1))
            startInd=Math.min(c.real.length-1,c.imag.length-1);
        if (endInd<startInd)
            endInd=startInd;
        if (endInd>Math.min(c.real.length-1,c.imag.length-1))
            endInd=Math.min(c.real.length-1,c.imag.length-1);

        double[] absVals = new double[endInd-startInd+1];
        for (int i=startInd; i<=endInd; i++)
            absVals[i-startInd] = Math.sqrt(c.real[i]*c.real[i]+c.imag[i]*c.imag[i]);

        return absVals;
    }
    public static double[] amp2db(double[] amps)
    {
        return multiply(log10(amps), 20);
    }
    
    public static double[] amp2neper(double[] amps)
    {
        double[] newAmps = new double[amps.length];
        for (int i=0; i<amps.length; i++)
            newAmps[i] = amp2neper(amps[i]);
        
        return newAmps;
    }

    public static double[] dft2ampdb(ComplexArray c)
    {
        return dft2ampdb(c, 0, c.real.length-1);
    }

    public static double[] dft2ampdb(ComplexArray c, int startInd, int endInd)
    {
        if (startInd<0)
            startInd=0;
        if (startInd>Math.min(c.real.length-1,c.imag.length-1))
            startInd=Math.min(c.real.length-1,c.imag.length-1);
        if (endInd<startInd)
            endInd=startInd;
        if (endInd>Math.min(c.real.length-1,c.imag.length-1))
            endInd=Math.min(c.real.length-1,c.imag.length-1);

        double[] dbs = new double[endInd-startInd+1];
        for (int i=startInd; i<=endInd; i++)
            dbs[i-startInd] = amp2db(Math.sqrt(c.real[i]*c.real[i]+c.imag[i]*c.imag[i]));

        return dbs;
    }

    /**
     * Convert energy from db scale to linear scale.
     * @param energy in time or frequency domain, on a db energy scale
     * @return energy on a linear scale.
     */
    public static double db2linear(double dbEnergy)
    {
        if (Double.isNaN(dbEnergy)) return 0.;
        else return exp10(dbEnergy/10);
    }

    public static double[] db2linear(double[] dbEnergies)
    {
        return exp10(divide(dbEnergies, 10.0));
    }
    
    public static double[] linear2db(double[] linears)
    {
        return multiply(log10(linears), 10.0);
    }

    public static float db2amp(float dbAmplitude)
    {
        if (Float.isNaN(dbAmplitude)) return 0.0f;
        else return (float)(Math.pow(10.0, dbAmplitude/20));
    }

    public static double db2amp(double dbAmplitude)
    {
        if (Double.isNaN(dbAmplitude)) return 0.;
        else return Math.pow(10.0, dbAmplitude/20);
    }
    
    public static float[] db2amp(float[] dbAmplitudes)
    {
        float[] amps = new float[dbAmplitudes.length];
        for (int i=0; i<dbAmplitudes.length; i++)
            amps[i] = db2amp(dbAmplitudes[i]);
        
        return amps;
    }

    public static double[] db2amp(double[] dbAmplitudes)
    {
        double[] amps = new double[dbAmplitudes.length];
        for (int i=0; i<dbAmplitudes.length; i++)
            amps[i] = db2amp(dbAmplitudes[i]);
        
        return amps;
    }

    public static float radian2degrees(float rad)
    {
        return (float)((rad/MathUtils.TWOPI)*360.0f);
    }

    public static double radian2degrees(double rad)
    {
        return (rad/MathUtils.TWOPI)*360.0;
    }
    
    public static float degrees2radian(float deg)
    {
        return (float)((deg/360.0)*MathUtils.TWOPI);
    }

    public static double degrees2radian(double deg)
    {
        return ((deg/360.0)*MathUtils.TWOPI);
    }

    /**
     * Build the sum of the squared difference of all elements 
     * with the same index numbers in the arrays.
     * Any NaN values in either a or b are ignored in computing the error.
     * @param a
     * @param b
     * @return
     */
    public static double sumSquaredError(double[] a, double[] b)
    {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        double sum = 0;
        for (int i=0; i<a.length; i++) {
            double delta = a[i] - b[i];
            if (!Double.isNaN(delta)) {
            	sum += delta*delta;
            }
        }
        return sum;
    }


    public static double log10(double x)
    {
        return Math.log(x)/LOG10;
    }

    public static double[] log(double[] a)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = Math.log(a[i]);
        }
        return c;
    }

    //A special log operation
    //The values smaller than or equal to minimumValue are set to fixedValue
    //The values greater than minimumValue are converted to log
    public static double[] log(double[] a, double minimumValue, double fixedValue)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) 
        {
            if (a[i]>minimumValue)
                c[i] = Math.log(a[i]);
            else
                c[i] = fixedValue;
        }
        return c;
    }

    public static double[] log10(double[] a)
    {
        double[] c = null;
        
        if (a!=null)
        {
            c = new double[a.length];
            
            for (int i=0; i<a.length; i++)
                c[i] = log10(a[i]);
        }
        
        return c;
    }

    public static double exp10(double x)
    {
        return Math.exp(LOG10*x);
    }

    public static double[] exp(double[] a)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = Math.exp(a[i]);
        }
        return c;
    }

    public static double[] exp10(double[] a)
    {
        double[] c = new double[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = exp10(a[i]);
        }
        return c;
    }


    public static float[] add(float[] a, float[] b)
    {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        float[] c = new float[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;        
    }

    public static float[] add(float[] a, float b)
    {
        float[] c = new float[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] + b;
        }
        return c;        
    }

    public static float[] subtract(float[] a, float[] b)
    {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        float[] c = new float[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] - b[i];
        }
        return c;
    }

    public static float[] subtract(float[] a, float b)
    {
        float[] c = new float[a.length];
        for (int i=0; i<a.length; i++) {
            c[i] = a[i] - b;
        }
        return c;
    }


    public static double euclidianLength(float[] a)
    {
        double len = 0.;
        for (int i=0; i<a.length; i++) {
            len += a[i]*a[i];
        }
        return Math.sqrt(len);
    }

    public static double euclidianLength(double[] a)
    {
        double len = 0.;
        for (int i=0; i<a.length; i++) {
            len += a[i]*a[i];
        }
        return Math.sqrt(len);
    }

    /**
     * Convert a pair of arrays from cartesian (x, y) coordinates to 
     * polar (r, phi) coordinates. Phi will be in radians, i.e. a full circle is two pi.
     * @param x as input, the x coordinate; as output, the r coordinate;
     * @param y as input, the y coordinate; as output, the phi coordinate.
     */
    public static void toPolarCoordinates(double[] x, double[] y)
    {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        for (int i=0; i<x.length; i++) {
            double r = Math.sqrt(x[i]*x[i] + y[i]*y[i]);
            double phi = Math.atan2(y[i], x[i]);
            x[i] = r;
            y[i] = phi;
        }
    }

    /**
     * Convert a pair of arrays from polar (r, phi) coordinates to
     * cartesian (x, y) coordinates. Phi is in radians, i.e. a whole circle is two pi.
     * @param r as input, the r coordinate; as output, the x coordinate;
     * @param phi as input, the phi coordinate; as output, the y coordinate.
     */
    public static void toCartesianCoordinates(double[] r, double[] phi)
    {
        if (r.length != phi.length) {
            throw new IllegalArgumentException("Arrays must be equal length");
        }
        for (int i=0; i<r.length; i++) {
            double x = r[i] * Math.cos(phi[i]);
            double y = r[i] * Math.sin(phi[i]);
            r[i] = x;
            phi[i] = y;
        }
    }

    /**
     * For a given angle in radians, return the equivalent angle in the range [-PI, PI].
     * @param angle
     * @return
     */
    public static double angleToDefaultAngle(double angle)
    {
        return (angle+Math.PI)%(-TWOPI)+Math.PI;
    }

    /**
     * For each of an array of angles (in radians), return the equivalent angle in the range [-PI, PI].
     * @param angle
     * @return
     */
    public static void angleToDefaultAngle(double[] angle)
    {
        for (int i=0; i<angle.length; i++) {
            angle[i] = angleToDefaultAngle(angle[i]);
        }
    }

    /**
     * This is the Java source code for a Levinson Recursion.
     * from http://www.nauticom.net/www/jdtaft/JavaLevinson.htm
     * @param r contains the autocorrelation lags as input [r(0)...r(m)].
     * @param m
     * @return the array of whitening coefficients
     */
    public static double[] levinson(double[] r, int m)
    {
        // The matrix l is unit lower triangular.
        // It's i-th row contains upon completion the i-th prediction error filter,
        // with the coefficients in reverse order. The vector e contains upon 
        // completion the prediction errors.
        // The last section extracts the maximum length whitening filter
        // coefficients from matrix l.
        int i;
        int j;
        int k;
        double gap;
        double gamma;
        double e[] = new double[m + 1];
        double l[][] = new double[m + 1][m + 1];
        double[] coeffs = new double[m+1];

        /* compute recursion  */
        for (i = 0; i <= m; i++) {
            for (j = i + 1; j <= m; j++) {
                l[i][j] = 0.;
            }
        }
        l[0][0] = 1.;
        l[1][1] = 1.;
        l[1][0] = -r[1] / r[0];
        e[0] = r[0];
        e[1] = e[0] * (1. - l[1][0] * l[1][0]);
        for (i = 2; i <= m; i++) {
            gap = 0.;
            for (k = 0; k <= i - 1; k++) {
                gap += r[k + 1] * l[i - 1][k];
            }
            gamma = gap / e[i - 1];
            l[i][0] = -gamma;
            for (k = 1; k <= i - 1; k++) {
                l[i][k] = l[i - 1][k - 1] - gamma * l[i - 1][i - 1 - k];
            }
            l[i][i] = 1.;
         
