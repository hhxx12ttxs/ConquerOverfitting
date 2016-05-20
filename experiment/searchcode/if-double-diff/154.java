/**
 *
 */
package il.ac.technion.cs.ssdl.sequence;

import il.ac.technion.cs.ssdl.external.External;
import il.ac.technion.cs.ssdl.iteration.Iterables;
import il.ac.technion.cs.ssdl.utils.Separate;
import il.ac.technion.cs.ssdl.utils.Unbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Yossi Gil
 * @since 24 ???? 2011
 */
public class Ergodic {
  @External(name = "from") static int FROM = 1;
  @External(name = "to") static int TO = 100;
  @External(name = "N") static int N = 20;
  
  public static void main(final String[] args) {
    External.Introspector.extract(args, Ergodic.class);
    System.out.println(Separate.by(make(N, FROM, TO), " "));
    System.out.println(Separate.by(makeDouble(N, 0.01, 10), " "));
  }
  public static double[] makeDouble(final int max, final double... ds) {
    return makeDouble(max, Iterables.toList(ds));
  }
  public static int[] make(final int max, final int... is) {
    return makeInt(max, Iterables.toList(is));
  }
  private static double[] makeDouble(final int n, final List<Double> $) {
    for (;;) {
      if ($.size() >= n)
        break;
      final Double d = selectDouble($);
      if (d == null)
        break;
      $.add(d);
    }
    return Unbox.unbox($.toArray(new Double[$.size()]));
  }
  private static int[] makeInt(final int n, final List<Integer> $) {
    for (;;) {
      if ($.size() >= n)
        break;
      final Integer i = select($);
      if (i == null)
        break;
      $.add(i);
    }
    return Unbox.it($);
  }
  private static Integer select(final List<Integer> l) {
    final List<Integer> is = new ArrayList<Integer>(l);
    Collections.sort(is);
    final float maxDiff = maxDiff(is);
    if (maxDiff < 0)
      return null;
    final Integer $ = selectDiff(is, maxDiff, new Random(0).nextInt(countDiff(is, maxDiff)));
    return $;
  }
  private static Double selectDouble(final List<Double> l) {
    final List<Double> is = new ArrayList<Double>(l);
    Collections.sort(is);
    final double maxDiff = maxDiffDouble(is);
    if (maxDiff < 0)
      return null;
    final Double $ = selectDiffDouble(is, maxDiff, new Random(0).nextInt(countDiffDouble(is, maxDiff)));
    return $;
  }
  static int countDiff(final List<Integer> is, final float diff) {
    int $ = 0;
    for (int i = 1; i < is.size(); i++)
      if (diff(is, i) == diff)
        $++;
    return $;
  }
  static int countDiffDouble(final List<Double> is, final double diff) {
    int $ = 0;
    for (int i = 1; i < is.size(); i++)
      if (diffDouble(is, i) == diff)
        $++;
    return $;
  }
  private static Integer selectDiff(final List<Integer> is, final float maxDiff, final int nextInt) {
    int n = 0;
    for (int i = 1; i < is.size(); i++)
      if (valid(is, i) && diff(is, i) == maxDiff) {
        if (n == nextInt)
          return mid(is, i);
        n++;
      }
    return null;
  }
  private static Double selectDiffDouble(final List<Double> is, final double maxDiff, final int nextInt) {
    int n = 0;
    for (int i = 1; i < is.size(); i++)
      if (diffDouble(is, i) == maxDiff) {
        if (n == nextInt)
          return midDouble(is, i);
        n++;
      }
    return null;
  }
  private static Integer mid(final List<Integer> is, final int i) {
    return mid(is.get(i - 1).intValue(), is.get(i).intValue());
  }
  private static Double midDouble(final List<Double> is, final int i) {
    return midDouble(is.get(i - 1).doubleValue(), is.get(i).doubleValue());
  }
  static final float maxDiff(final List<Integer> is) {
    float $ = -1;
    for (int i = 1; i < is.size(); i++)
      if (valid(is, i) && diff(is, i) > $)
        $ = diff(is, i);
    return $;
  }
  static final double maxDiffDouble(final List<Double> is) {
    double $ = -1;
    for (int i = 1; i < is.size(); i++)
      if (diffDouble(is, i) > $)
        $ = diffDouble(is, i);
    return $;
  }
  private static final float diff(final List<Integer> is, final int i) {
    final int a = is.get(i - 1).intValue();
    final int b = is.get(i).intValue();
    return valid(is, i) ? (float) b / a : -1;
  }
  private static final double diffDouble(final List<Double> is, final int i) {
    return is.get(i).doubleValue() / is.get(i - 1).doubleValue();
  }
  private static boolean valid(final List<Integer> is, final int i) {
    return is.get(i - 1).intValue() != is.get(i).intValue() - 1;
  }
  private static final Integer mid(final long a, final long b) {
    return new Integer((int) Math.round(Math.sqrt(a * b)));
  }
  private static final Double midDouble(final double a, final double b) {
    return new Double(Math.round(Math.sqrt(a * b)));
  }
}

