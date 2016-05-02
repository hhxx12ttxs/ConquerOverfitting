<<<<<<< HEAD
/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.utils.statistics;


import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class implements some simple statistical functions
 * on arrays of numbers, namely, the mean, variance, standard
 * deviation, covariance, min and max.
 */

public class Stats
{

/**
 * Converts a vector of Numbers into an array of double.
 * This function does not necessarily belong here, but
 * is commonly required in order to apply the
 * statistical functions conveniently, since they only
 * deal with arrays of double.  (Note that a Number of
 * the common superclass of all the Object versions of the
 * primitives, such as Integer, Double etc.).
 */
// package that at present just provides average and sd of a
// vector of doubles

// also enables writing the
// Gnuplot comments begin with #

// next need to find out how to select a particular line style
// found it :
// This plots sin(x) and cos(x) with linespoints, using the same line type but different point types:
//  plot sin(x) with linesp lt 1 pt 3, cos(x) with linesp lt 1 pt 4
public static double[] v2a(Vector v)
{
    double[] d = new double[v.size()];
    int i = 0;
    for (Enumeration e = v.elements(); e.hasMoreElements();)
        d[i++] = ((Number) e.nextElement()).doubleValue();
    return d;
}

/**
 * Calculates the square of a double.
 *
 * @return Returns x*x
 */

public static double sqr(double x)
{
    return x * x;
}

/**
 * Returns the average of an array of double.
 */

public static double mean(double[] v)
{
    double tot = 0.0;
    for (int i = 0; i < v.length; i++)
        tot += v[i];
    return tot / v.length;
}

/**
 * @param v - sample
 * @return the average of an array of int.
 */

public static double mean(int[] v)
{
    double tot = 0.0;
    for (int i = 0; i < v.length; i++)
        tot += v[i];
    return tot / v.length;
}

/**
 * Returns the sample standard deviation of an array
 * of double.
 */

public static double sdev(double[] v)
{
    return Math.sqrt(variance(v));
}

/**
 * Returns the standard error of an array of double,
 * where this is defined as the standard deviation
 * of the sample divided by the square root of the
 * sample size.
 */

public static double stderr(double[] v)
{
    return sdev(v) / Math.sqrt(v.length);
}

/**
 * Returns the variance of the array of double.
 */

public static double variance(double[] v)
{
    double mu = mean(v);
    double sumsq = 0.0;
    for (int i = 0; i < v.length; i++)
        sumsq += sqr(mu - v[i]);
    return sumsq / (v.length);
    // return 1.12; this was done to test a discrepancy with Business Statistics
}

/**
 * this alternative version was used to check
 * correctness
 */

private static double variance2(double[] v)
{
    double mu = mean(v);
    double sumsq = 0.0;
    for (int i = 0; i < v.length; i++)
        sumsq += sqr(v[i]);
    System.out.println(sumsq + " : " + mu);
    double diff = (sumsq - v.length * sqr(mu));
    System.out.println("Diff = " + diff);
    return diff / (v.length);
}

/**
 * Returns the covariance of the paired arrays of
 * double.
 */

public static double covar(double[] v1, double[] v2)
{
    double m1 = mean(v1);
    double m2 = mean(v2);
    double sumsq = 0.0;
    for (int i = 0; i < v1.length; i++)
        sumsq += (m1 - v1[i]) * (m2 - v2[i]);
    return sumsq / (v1.length);
}

public static double correlation(double[] v1, double[] v2)
{
    // an inefficient implementation!!!
    return covar(v1, v2) / (sdev(v1) * sdev(v2));
}

public static double correlation2(double[] v1, double[] v2)
{
    // an inefficient implementation!!!
    return sqr(covar(v1, v2)) / (covar(v1, v1) * covar(v2, v2));
}

/**
 * Returns the maximum value in the array.
 */

public static double max(double[] v)
{
    double m = v[0];
    for (int i = 1; i < v.length; i++)
        m = Math.max(m, v[i]);
    return m;
}

/**
 * Returns the minimum value in the array.
 */

public static double min(double[] v)
{
    double m = v[0];
    for (int i = 1; i < v.length; i++)
        m = Math.min(m, v[i]);
    return m;
}

/**
 * Prints the means and standard deviation of
 * the data to the standard output.
 */

public static void analyse(double[] v)
{
    analyse(v, System.out);
    // System.out.println("Average = " + mean(v) + "  sd = " + sdev(v));
}

/**
 * Prints the means and standard deviation of
 * the data to the specified PrintStream
 *
 * @param v contains the data
 * @param s is the corresponding PrintStream
 */

public static void analyse(double[] v, PrintStream s)
{
    s.println("Average = " + mean(v) + "  sd = " + sdev(v));
}

/**
 * @param v contains the data
 * @return A String summary of the with the mean and standard deviation of
 *         the data.
 */

public static String analysisString(double[] v)
{
    return "Average = " + mean(v) + "  sd = " + sdev(v)
            + "  min = " + min(v) + "  max = " + max(v);
}

/**
 * Returns a string that compares the root mean square
 * of the data with the standard deviation of the
 * data.  This is probably too specialised to be of
 * much general use.
 *
 * @param v contains the data
 * @return root mean square = <...> standard deviation = <...>
 */
public static String rmsString(double[] v)
{
    double[] tv = new double[v.length];
    for (int i = 0; i < v.length; i++)
        tv[i] = v[i] * v[i];
    return "rms = " + mean(tv) + " sd = " + sdev(v) + "\n";
}

/**
 * Runs through some utils using the functions
 * defined in this class.
 *
 * @throws java.io.IOException
 */

public static void main(String[] args) throws IOException
{

    double[] d = new double[0];

    double dd = mean(d);

    System.out.println(dd + "\t" + Double.isNaN(dd));

    for (int i = 0; i < 3; i++)
    {
        double[] x = new double[i];
        System.out.println(mean(x) + "\t " + stderr(x) + "\t " + sdev(x));
    }
}

}

=======
package liblinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;


/**
 * <h2>Java port of <a href="http://www.csie.ntu.edu.tw/~cjlin/liblinear/">liblinear</a> 1.33</h2>
 *
 * <p>
 * The usage should be pretty similar to the C version of <tt>liblinear</tt>.<br/>
 * Please consider reading the <tt>README</tt> file of <tt>liblinear</tt>.<br/>
 * </p>
 *
 * <p><em>The port was done by Benedikt Waldvogel (mail at bwaldvogel.de)</em></p>
 *
 * @version 1.33
 */
public class Linear {

   static final Charset      FILE_CHARSET        = Charset.forName("ISO-8859-1");

   static final Locale       DEFAULT_LOCALE      = Locale.ENGLISH;

   /** set this to false if you don't want anything written to stdout */
   private static boolean    DEBUG_OUTPUT        = true;

   /** platform-independent new-line string */
   final static String       NL                  = System.getProperty("line.separator");

   private static final long DEFAULT_RANDOM_SEED = 0L;
   static Random             random              = new Random(DEFAULT_RANDOM_SEED);

   /**
    * @param target predicted classes
    */
   public static void crossValidation( Problem prob, Parameter param, int nr_fold, int[] target ) {
      int i;
      int[] fold_start = new int[nr_fold + 1];
      int l = prob.l;
      int[] perm = new int[l];

      for ( i = 0; i < l; i++ )
         perm[i] = i;
      for ( i = 0; i < l; i++ ) {
         int j = i + random.nextInt(l - i);
         swap(perm, i, j);
      }
      for ( i = 0; i <= nr_fold; i++ )
         fold_start[i] = i * l / nr_fold;

      for ( i = 0; i < nr_fold; i++ ) {
         int begin = fold_start[i];
         int end = fold_start[i + 1];
         int j, k;
         Problem subprob = new Problem();

         subprob.bias = prob.bias;
         subprob.n = prob.n;
         subprob.l = l - (end - begin);
         subprob.x = new FeatureNode[l][];
         subprob.y = new int[subprob.l];

         k = 0;
         for ( j = 0; j < begin; j++ ) {
            subprob.x[k] = prob.x[perm[j]];
            subprob.y[k] = prob.y[perm[j]];
            ++k;
         }
         for ( j = end; j < l; j++ ) {
            subprob.x[k] = prob.x[perm[j]];
            subprob.y[k] = prob.y[perm[j]];
            ++k;
         }
         Model submodel = train(subprob, param);
         for ( j = begin; j < end; j++ )
            target[perm[j]] = predict(submodel, prob.x[perm[j]]);
      }
   }

   /** used as complex return type */
   private static class GroupClassesReturn {

      final int[] count;
      final int[] label;
      final int   nr_class;
      final int[] start;

      GroupClassesReturn( int nr_class, int[] label, int[] start, int[] count ) {
         this.nr_class = nr_class;
         this.label = label;
         this.start = start;
         this.count = count;
      }
   }

   private static GroupClassesReturn groupClasses( Problem prob, int[] perm ) {
      int l = prob.l;
      int max_nr_class = 16;
      int nr_class = 0;

      int[] label = new int[max_nr_class];
      int[] count = new int[max_nr_class];
      int[] data_label = new int[l];
      int i;

      for ( i = 0; i < l; i++ ) {
         int this_label = prob.y[i];
         int j;
         for ( j = 0; j < nr_class; j++ ) {
            if ( this_label == label[j] ) {
               ++count[j];
               break;
            }
         }
         data_label[i] = j;
         if ( j == nr_class ) {
            if ( nr_class == max_nr_class ) {
               max_nr_class *= 2;
               label = copyOf(label, max_nr_class);
               count = copyOf(count, max_nr_class);
            }
            label[nr_class] = this_label;
            count[nr_class] = 1;
            ++nr_class;
         }
      }

      int[] start = new int[nr_class];
      start[0] = 0;
      for ( i = 1; i < nr_class; i++ )
         start[i] = start[i - 1] + count[i - 1];
      for ( i = 0; i < l; i++ ) {
         perm[start[data_label[i]]] = i;
         ++start[data_label[i]];
      }
      start[0] = 0;
      for ( i = 1; i < nr_class; i++ )
         start[i] = start[i - 1] + count[i - 1];

      return new GroupClassesReturn(nr_class, label, start, count);
   }

   static void info( String message ) {
      if ( !DEBUG_OUTPUT ) return;
      System.out.print(message);
   }

   static void info( String format, Object... args ) {
      if ( !DEBUG_OUTPUT ) return;
      System.out.printf(format, args);
   }


   static void infoFlush() {
      if ( !DEBUG_OUTPUT ) return;
      System.out.flush();
   }

   /**
    * @param s the string to parse for the double value
    * @throws IllegalArgumentException if s is empty or represents NaN or Infinity
    * @throws NumberFormatException see {@link Double#parseDouble(String)}
    */
   static double atof( String s ) {
      if ( s == null || s.length() < 1 ) throw new IllegalArgumentException("Can't convert empty string to integer");
      double d = Double.parseDouble(s);
      if ( Double.isNaN(d) || Double.isInfinite(d) ) {
         throw new IllegalArgumentException("NaN or Infinity in input: " + s);
      }
      return (d);
   }

   /**
    * @param s the string to parse for the integer value
    * @throws IllegalArgumentException if s is empty
    * @throws NumberFormatException see {@link Integer#parseInt(String)}
    */
   static int atoi( String s ) throws NumberFormatException {
      if ( s == null || s.length() < 1 ) throw new IllegalArgumentException("Can't convert empty string to integer");
      // Integer.parseInt doesn't accept '+' prefixed strings
      if ( s.charAt(0) == '+' ) s = s.substring(1);
      return Integer.parseInt(s);
   }

   /**
    * Java5 'backport' of Arrays.copyOf
    */
   public static double[] copyOf( double[] original, int newLength ) {
      double[] copy = new double[newLength];
      System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
      return copy;
   }

   /**
    * Java5 'backport' of Arrays.copyOf
    */
   public static int[] copyOf( int[] original, int newLength ) {
      int[] copy = new int[newLength];
      System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
      return copy;
   }

   /**
    * Loads the model from inputReader.
    * It uses {@link Locale.ENGLISH} for number formatting.
    *
    * <p><b>Note: The inputReader is closed after reading or in case of an exception.</b></p>
    */
   public static Model loadModel( BufferedReader inputReader ) throws IOException {
      Model model = new Model();

      model.label = null;

      Pattern whitespace = Pattern.compile("\\s+");

      try {
         String line = null;
         while ( (line = inputReader.readLine()) != null ) {
            String[] split = whitespace.split(line);
            if ( split[0].equals("solver_type") ) {
               SolverType solver = SolverType.valueOf(split[1]);
               if ( solver == null ) {
                  throw new RuntimeException("unknown solver type");
               }
               model.solverType = solver;
            } else if ( split[0].equals("nr_class") ) {
               model.nr_class = atoi(split[1]);
               Integer.parseInt(split[1]);
            } else if ( split[0].equals("nr_feature") ) {
               model.nr_feature = atoi(split[1]);
            } else if ( split[0].equals("bias") ) {
               model.bias = atof(split[1]);
            } else if ( split[0].equals("w") ) {
               break;
            } else if ( split[0].equals("label") ) {
               model.label = new int[model.nr_class];
               for ( int i = 0; i < model.nr_class; i++ ) {
                  model.label[i] = atoi(split[i + 1]);
               }
            } else {
               throw new RuntimeException("unknown text in model file: [" + line + "]");
            }
         }

         int n = model.nr_feature;
         if ( model.bias >= 0 ) n++;

         int nr_w = model.nr_class;
         if ( model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS ) nr_w = 1;

         model.w = new double[n * nr_w];
         int[] buffer = new int[128];

         for ( int i = 0; i < n; i++ ) {
            for ( int j = 0; j < nr_w; j++ ) {
               int b = 0;
               while ( true ) {
                  int ch = inputReader.read();
                  if ( ch == -1 ) {
                     throw new EOFException("unexpected EOF");
                  }
                  if ( ch == ' ' ) {
                     model.w[i * nr_w + j] = atof(new String(buffer, 0, b));
                     break;
                  } else {
                     buffer[b++] = ch;
                  }
               }
            }
         }
      }
      finally {
         closeQuietly(inputReader);
      }

      return model;
   }

   /**
    * Loads the model from the file with ISO-8859-1 charset.
    * It uses {@link Locale.ENGLISH} for number formatting.
    */
   public static Model loadModel( File modelFile ) throws IOException {
      BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), FILE_CHARSET));
      return loadModel(inputReader);
   }

   static void closeQuietly( Closeable c ) {
      if ( c == null ) return;
      try {
         c.close();
      }
      catch ( Throwable t ) {}
   }

   public static int predict( Model model, FeatureNode[] x ) {
      double[] dec_values = new double[model.nr_class];
      return predictValues(model, x, dec_values);
   }

   public static int predictProbability( Model model, FeatureNode[] x, double[] prob_estimates ) {
      if ( model.solverType == SolverType.L2_LR ) {
         int nr_class = model.nr_class;
         int nr_w;
         if ( nr_class == 2 )
            nr_w = 1;
         else
            nr_w = nr_class;

         int label = predictValues(model, x, prob_estimates);
         for ( int i = 0; i < nr_w; i++ )
            prob_estimates[i] = 1 / (1 + Math.exp(-prob_estimates[i]));

         if ( nr_class == 2 ) // for binary classification
            prob_estimates[1] = 1. - prob_estimates[0];
         else {
            double sum = 0;
            for ( int i = 0; i < nr_class; i++ )
               sum += prob_estimates[i];

            for ( int i = 0; i < nr_class; i++ )
               prob_estimates[i] = prob_estimates[i] / sum;
         }

         return label;
      } else
         return 0;
   }

   static int predictValues( Model model, FeatureNode[] x, double[] dec_values ) {
      int n;
      if ( model.bias >= 0 )
         n = model.nr_feature + 1;
      else
         n = model.nr_feature;

      double[] w = model.w;

      int nr_w;
      if ( model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS )
         nr_w = 1;
      else
         nr_w = model.nr_class;

      for ( int i = 0; i < nr_w; i++ )
         dec_values[i] = 0;

      for ( FeatureNode lx : x ) {
         int idx = lx.index;
         // the dimension of testing data may exceed that of training
         if ( idx <= n ) {
            for ( int i = 0; i < nr_w; i++ ) {
               dec_values[i] += w[(idx - 1) * nr_w + i] * lx.value;
            }
         }
      }

      if ( model.nr_class == 2 )
         return (dec_values[0] > 0) ? model.label[0] : model.label[1];
      else {
         int dec_max_idx = 0;
         for ( int i = 1; i < model.nr_class; i++ ) {
            if ( dec_values[i] > dec_values[dec_max_idx] ) dec_max_idx = i;
         }
         return model.label[dec_max_idx];
      }
   }


   static void printf( Formatter formatter, String format, Object... args ) throws IOException {
      formatter.format(format, args);
      IOException ioException = formatter.ioException();
      if ( ioException != null ) throw ioException;
   }

   /**
    * Writes the model to the modelOutput.
    * It uses {@link Locale.ENGLISH} for number formatting.
    *
    * <p><b>Note: The modelOutput is closed after reading or in case of an exception.</b></p>
    */
   public static void saveModel( Writer modelOutput, Model model ) throws IOException {
      int nr_feature = model.nr_feature;
      int n = nr_feature;
      if ( model.bias >= 0 ) n++;

      int nr_w = model.nr_class;
      if ( model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS ) nr_w = 1;

      Formatter formatter = new Formatter(modelOutput, DEFAULT_LOCALE);
      try {
         printf(formatter, "solver_type %s\n", model.solverType.name());
         printf(formatter, "nr_class %d\n", model.nr_class);

         printf(formatter, "label");
         for ( int i = 0; i < model.nr_class; i++ ) {
            printf(formatter, " %d", model.label[i]);
         }
         printf(formatter, "\n");

         printf(formatter, "nr_feature %d\n", nr_feature);
         printf(formatter, "bias %.16g\n", model.bias);

         printf(formatter, "w\n");
         for ( int i = 0; i < n; i++ ) {
            for ( int j = 0; j < nr_w; j++ ) {
               double value = model.w[i * nr_w + j];

               /** this optimization is the reason for {@link Model#equals(double[], double[])} */
               if ( value == 0.0 ) {
                  printf(formatter, "%d ", 0);
               } else {
                  printf(formatter, "%.16g ", value);
               }
            }
            printf(formatter, "\n");
         }

         formatter.flush();
         IOException ioException = formatter.ioException();
         if ( ioException != null ) throw ioException;
      }
      finally {
         formatter.close();
      }
   }

   /**
    * Writes the model to the file with ISO-8859-1 charset.
    * It uses {@link Locale.ENGLISH} for number formatting.
    */
   public static void saveModel( File modelFile, Model model ) throws IOException {
      BufferedWriter modelOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelFile), FILE_CHARSET));
      saveModel(modelOutput, model);
   }

   private static void solve_linear_c_svc( Problem prob, double[] w, double eps, double Cp, double Cn, SolverType solver_type ) {
      int l = prob.l;
      int n = prob.n;
      int i, s, iter = 0;
      double C, d, G;
      double[] QD = new double[l];
      int max_iter = 20000;
      int[] index = new int[l];
      double[] alpha = new double[l];
      byte[] y = new byte[l];
      int active_size = l;

      // PG: projected gradient, for shrinking and stopping
      double PG;
      double PGmax_old = Double.POSITIVE_INFINITY;
      double PGmin_old = Double.NEGATIVE_INFINITY;
      double PGmax_new, PGmin_new;

      // default solver_type: L2LOSS_SVM_DUAL
      double diag_p = 0.5 / Cp, diag_n = 0.5 / Cn;
      double upper_bound_p = Double.POSITIVE_INFINITY, upper_bound_n = Double.POSITIVE_INFINITY;
      if ( solver_type == SolverType.L1LOSS_SVM_DUAL ) {
         diag_p = 0;
         diag_n = 0;
         upper_bound_p = Cp;
         upper_bound_n = Cn;
      }

      for ( i = 0; i < n; i++ )
         w[i] = 0;
      for ( i = 0; i < l; i++ ) {
         alpha[i] = 0;
         if ( prob.y[i] > 0 ) {
            y[i] = +1;
            QD[i] = diag_p;
         } else {
            y[i] = -1;
            QD[i] = diag_n;
         }

         for ( FeatureNode xi : prob.x[i] ) {
            QD[i] += xi.value * xi.value;
         }
         index[i] = i;
      }

      while ( iter < max_iter ) {
         PGmax_new = Double.NEGATIVE_INFINITY;
         PGmin_new = Double.POSITIVE_INFINITY;

         for ( i = 0; i < active_size; i++ ) {
            int j = i + random.nextInt(active_size - i);
            swap(index, i, j);
         }

         for ( s = 0; s < active_size; s++ ) {
            i = index[s];
            G = 0;
            byte yi = y[i];

            for ( FeatureNode xi : prob.x[i] ) {
               G += w[xi.index - 1] * xi.value;
            }
            G = G * yi - 1;

            if ( yi == 1 ) {
               C = upper_bound_p;
               G += alpha[i] * diag_p;
            } else {
               C = upper_bound_n;
               G += alpha[i] * diag_n;
            }

            PG = 0;
            if ( alpha[i] == 0 ) {
               if ( G > PGmax_old ) {
                  active_size--;
                  swap(index, s, active_size);
                  s--;
                  continue;
               } else if ( G < 0 ) {
                  PG = G;
               }
            } else if ( alpha[i] == C ) {
               if ( G < PGmin_old ) {
                  active_size--;
                  swap(index, s, active_size);
                  s--;
                  continue;
               } else if ( G > 0 ) {
                  PG = G;
               }
            } else {
               PG = G;
            }

            PGmax_new = Math.max(PGmax_new, PG);
            PGmin_new = Math.min(PGmin_new, PG);

            if ( Math.abs(PG) > 1.0e-12 ) {
               double alpha_old = alpha[i];
               alpha[i] = Math.min(Math.max(alpha[i] - G / QD[i], 0.0), C);
               d = (alpha[i] - alpha_old) * yi;

               for ( FeatureNode xi : prob.x[i] ) {
                  w[xi.index - 1] += d * xi.value;
               }
            }
         }

         iter++;
         if ( iter % 10 == 0 ) {
            info(".");
            infoFlush();
         }

         if ( PGmax_new - PGmin_new <= eps ) {
            if ( active_size == l )
               break;
            else {
               active_size = l;
               info("*");
               infoFlush();
               PGmax_old = Double.POSITIVE_INFINITY;
               PGmin_old = Double.NEGATIVE_INFINITY;
               continue;
            }
         }
         PGmax_old = PGmax_new;
         PGmin_old = PGmin_new;
         if ( PGmax_old <= 0 ) PGmax_old = Double.POSITIVE_INFINITY;
         if ( PGmin_old >= 0 ) PGmin_old = Double.NEGATIVE_INFINITY;
      }

      info(NL + "optimization finished, #iter = %d" + NL, iter);
      if ( iter >= max_iter ) info("Warning: reaching max number of iterations\n");

      // calculate objective value

      double v = 0;
      int nSV = 0;
      for ( i = 0; i < n; i++ )
         v += w[i] * w[i];
      for ( i = 0; i < l; i++ ) {
         if ( y[i] == 1 )
            v += alpha[i] * (alpha[i] * diag_p - 2);
         else
            v += alpha[i] * (alpha[i] * diag_n - 2);
         if ( alpha[i] > 0 ) ++nSV;
      }
      info("Objective value = %f" + NL, v / 2);
      info("nSV = %d" + NL, nSV);
   }

   static void swap( double[] array, int idxA, int idxB ) {
      double temp = array[idxA];
      array[idxA] = array[idxB];
      array[idxB] = temp;
   }

   static void swap( int[] array, int idxA, int idxB ) {
      int temp = array[idxA];
      array[idxA] = array[idxB];
      array[idxB] = temp;
   }

   static void swap( IntArrayPointer array, int idxA, int idxB ) {
      int temp = array.get(idxA);
      array.set(idxA, array.get(idxB));
      array.set(idxB, temp);
   }

   /**
    * @throws IllegalArgumentException if the feature nodes of prob are not sorted in ascending order
    */
   public static Model train( Problem prob, Parameter param ) {

      if ( prob == null ) throw new IllegalArgumentException("problem must not be null");
      if ( param == null ) throw new IllegalArgumentException("parameter must not be null");

      for ( FeatureNode[] nodes : prob.x ) {
         int indexBefore = 0;
         for ( FeatureNode n : nodes ) {
            if ( n.index <= indexBefore ) {
               throw new IllegalArgumentException("feature nodes must be sorted by index in ascending order");
            }
            indexBefore = n.index;
         }
      }

      int i, j;
      int l = prob.l;
      int n = prob.n;
      Model model = new Model();

      if ( prob.bias >= 0 )
         model.nr_feature = n - 1;
      else
         model.nr_feature = n;
      model.solverType = param.solverType;
      model.bias = prob.bias;

      int[] perm = new int[l];
      // group training data of the same class
      GroupClassesReturn rv = groupClasses(prob, perm);
      int nr_class = rv.nr_class;
      int[] label = rv.label;
      int[] start = rv.start;
      int[] count = rv.count;

      model.nr_class = nr_class;
      model.label = new int[nr_class];
      for ( i = 0; i < nr_class; i++ )
         model.label[i] = label[i];

      // calculate weighted C
      double[] weighted_C = new double[nr_class];
      for ( i = 0; i < nr_class; i++ ) {
         weighted_C[i] = param.C;
      }

      for ( i = 0; i < param.getNumWeights(); i++ ) {
         for ( j = 0; j < nr_class; j++ )
            if ( param.weightLabel[i] == label[j] ) break;
         if ( j == nr_class ) throw new IllegalArgumentException("class label " + param.weightLabel[i] + " specified in weight is not found");

         weighted_C[j] *= param.weight[i];
      }

      // constructing the subproblem
      FeatureNode[][] x = new FeatureNode[l][];
      for ( i = 0; i < l; i++ )
         x[i] = prob.x[perm[i]];

      int k;
      Problem sub_prob = new Problem();
      sub_prob.l = l;
      sub_prob.n = n;
      sub_prob.x = new FeatureNode[sub_prob.l][];
      sub_prob.y = new int[sub_prob.l];

      for ( k = 0; k < sub_prob.l; k++ )
         sub_prob.x[k] = x[k];

      // multi-class svm by Crammer and Singer
      if ( param.solverType == SolverType.MCSVM_CS ) {
         model.w = new double[n * nr_class];
         for ( i = 0; i < nr_class; i++ ) {
            for ( j = start[i]; j < start[i] + count[i]; j++ ) {
               sub_prob.y[j] = i;
            }
         }

         SolverMCSVM_CS solver = new SolverMCSVM_CS(sub_prob, nr_class, weighted_C, param.eps);
         solver.solve(model.w);
      } else {
         if ( nr_class == 2 ) {
            model.w = new double[n];

            int e0 = start[0] + count[0];
            k = 0;
            for ( ; k < e0; k++ )
               sub_prob.y[k] = +1;
            for ( ; k < sub_prob.l; k++ )
               sub_prob.y[k] = -1;

            train_one(sub_prob, param, model.w, weighted_C[0], weighted_C[1]);
         } else {
            model.w = new double[n * nr_class];
            double[] w = new double[n];
            for ( i = 0; i < nr_class; i++ ) {
               int si = start[i];
               int ei = si + count[i];

               k = 0;
               for ( ; k < si; k++ )
                  sub_prob.y[k] = -1;
               for ( ; k < ei; k++ )
                  sub_prob.y[k] = +1;
               for ( ; k < sub_prob.l; k++ )
                  sub_prob.y[k] = -1;

               train_one(sub_prob, param, w, weighted_C[i], param.C);

               for ( j = 0; j < n; j++ )
                  model.w[j * nr_class + i] = w[j];
            }
         }

      }
      return model;
   }

   private static void train_one( Problem prob, Parameter param, double[] w, double Cp, double Cn ) {
      double eps = param.eps;
      int pos = 0;
      for ( int i = 0; i < prob.l; i++ )
         if ( prob.y[i] == +1 ) pos++;
      int neg = prob.l - pos;

      Function fun_obj = null;
      switch ( param.solverType ) {
      case L2_LR: {
         fun_obj = new L2LrFunction(prob, Cp, Cn);
         Tron tron_obj = new Tron(fun_obj, eps * Math.min(pos, neg) / prob.l);
         tron_obj.tron(w);
         break;
      }
      case L2LOSS_SVM: {
         fun_obj = new L2LossSVMFunction(prob, Cp, Cn);
         Tron tron_obj = new Tron(fun_obj, eps * Math.min(pos, neg) / prob.l);
         tron_obj.tron(w);
         break;
      }
      case L2LOSS_SVM_DUAL:
         solve_linear_c_svc(prob, w, eps, Cp, Cn, SolverType.L2LOSS_SVM_DUAL);
         break;
      case L1LOSS_SVM_DUAL:
         solve_linear_c_svc(prob, w, eps, Cp, Cn, SolverType.L1LOSS_SVM_DUAL);
         break;
      default:
         throw new IllegalStateException("unknown solver type: " + param.solverType);
      }
   }

   public static void disableDebugOutput() {
      DEBUG_OUTPUT = false;
   }

   public static void enableDebugOutput() {
      DEBUG_OUTPUT = true;
   }

   /**
    * resets the PRNG
    *
    * this is i.a. needed for regression testing (eg. the Weka wrapper)
    */
   public static void resetRandom() {
      random = new Random(DEFAULT_RANDOM_SEED);
   }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
