/**
 *
 */
package il.ac.technion.cs.ssdl.xy;

import static il.ac.technion.cs.ssdl.iteration.Iterables.doubles;
import static il.ac.technion.cs.ssdl.misc.LinearAlgebra.isReal;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import il.ac.technion.cs.ssdl.collections.DoublesArray;
import il.ac.technion.cs.ssdl.utils.____;

import org.junit.Test;

/**
 * An interface for processing a sequence of X-Y points
 *
 * @author Yossi Gil
 * @since February 15, 2012
 */
public interface XYProcessor {
  void p(double x, double y, double dy);
  void p(double x, double y);
  void p(int x, int y);
  void done();

  public abstract static class Vacuous implements XYProcessor {
    @Override public void p(@SuppressWarnings("unused") final double x, @SuppressWarnings("unused") final double y,
        @SuppressWarnings("unused") final double dy) {
      ____.nothing();
    }
    @Override public void p(final double x, final double y) {
      p(x, y, 0);
    }
    @Override public void p(final int x, final int y) {
      p((double) x, (double) y);
    }
    @Override public void done() {
      ____.nothing();
    }
    public Vacuous feed(final double xs[], final double ys[]) {
      assert xs.length == ys.length;
      final int n = Math.max(xs.length, ys.length);
      for (int i = 0; i < n; i++)
        p(xs[i], ys[i]);
      done();
      return this;
    }
    public Vacuous feed(final double xs[], final double ys[], final double dys[]) {
      assert xs.length == ys.length;
      assert ys.length == dys.length;
      final int n = Math.max(xs.length, ys.length);
      for (int i = 0; i < n; i++)
        p(xs[i], ys[i], dys[i]);
      done();
      return this;
    }
    public Vacuous feed(final XYSeries s) {
      for (int i = 0; i < s.n(); i++)
        p(s.x[i], s.y[i], s.dy[i]);
      done();
      return this;
    }
    public Vacuous feedHistogram(final double ys[], final double dys[]) {
      for (int i = 0; i < ys.length; i++)
        if (ys[i] != 0)
          p(i, ys[i], dys[i]);
      done();
      return this;
    }
    public Vacuous feedHistogram(final int as[]) {
      for (int i = 0; i < as.length; i++)
        if (as[i] != 0)
          p(i, as[i]);
      done();
      return this;
    }
    public Vacuous feedHistogram(final double ds[]) {
      for (int i = 0; i < ds.length; i++)
        if (ds[i] != 0)
          p(i, ds[i]);
      done();
      return this;
    }
  }

  public abstract static class Wrapper extends Vacuous {
    /**
     * Instantiate {@link Wrapper}.
     *
     * @param inner
     */
    public Wrapper(final XYProcessor inner) {
      this.inner = inner;
    }
    @Override public void p(final int x, final int y) {
      inner.p(x, y);
    }
    @Override public void p(final double x, final double y) {
      inner.p(x, y);
    }
    @Override public void p(final double x, final double y, final double dy) {
      inner.p(x, y, dy);
    }
    @Override public void done() {
      inner.done();
    }

    protected final XYProcessor inner;
  }

  public static class SquareErrorWrapper extends Wrapper {
    /**
     * Instantiate {@link SquareErrorWrapper}.
     *
     * @param inner
     */
    public SquareErrorWrapper(final XYProcessor inner) {
      super(inner);
    }
    @Override public void p(final double x, final double y) {
      if (y > 0)
        super.p(x, y, 1 / sqrt(y));
    }
    @Override public void p(final int x, final int y) {
      if (y > 0)
        super.p(x, y, 1 / sqrt(y));
    }
  }

  public static class LogLog extends Wrapper {
    /**
     * Instantiate {@link LogLog}.
     *
     * @param inner
     */
    public LogLog(final XYProcessor inner) {
      super(inner);
    }
    @Override public void p(final int x, final int y) {
      if (x > 0 && y > 0)
        super.p(log(x), log(y));
    }
    @Override public void p(final double x, final double y) {
      if (x > 0 && y > 0)
        super.p(log(x), log(y));
    }
    @Override public void p(final double x, final double y, final double dy) {
      if (x > 0 && y > 0)
        super.p(log(x), log(y), (log(y + dy) - log(y - dy)) / 2);
    }
  }

  public static class XShift extends Wrapper {
    private final double xshift;

    public XShift(final double xshift, final XYProcessor inner) {
      super(inner);
      this.xshift = xshift;
    }
    @Override public void p(final int x, final int y) {
      super.p(x + xshift, y);
    }
    @Override public void p(final double x, final double y) {
      super.p(x + xshift, y);
    }
    @Override public void p(final double x, final double y, final double dy) {
      super.p(x + xshift, y, dy);
    }
  }

  public static class MaxErrorFilter extends Wrapper {
    public MaxErrorFilter(final XYProcessor inner) {
      this(inner, DEFAULT_FACTOR);
    }
    public MaxErrorFilter(final XYProcessor inner, final double factor) {
      super(inner);
      this.factor = factor;
    }
    @Override public void p(final double x, final double y, final double dy) {
      if (Double.isNaN(dy) || Double.isInfinite(dy))
        super.p(x, y, factor * maxError);
      if (Double.isNaN(maxError) || Double.isInfinite(maxError))
        maxError = dy;
      maxError = Math.max(maxError, dy);
      super.p(x, y, dy);
    }

    private double maxError = Double.NaN;
    private static double DEFAULT_FACTOR = 2;
    private final double factor;
  }

  public static class Minimizer extends Vacuous {
    public double xMin() {
      return xMin;
    }
    public double yMin() {
      return yMin;
    }
    public double dyMin() {
      return dyMin;
    }
    @Override public void p(final double x, final double y, final double dy) {
      if (!(improved = y < yMin))
        return;
      xMin = x;
      yMin = y;
      dyMin = dy;
    }
    public boolean improved() {
      return improved;
    }

    private boolean improved = false;
    private double xMin = Double.NaN;
    private double yMin = Double.POSITIVE_INFINITY;
    private double dyMin = Double.NaN;
  }

  public static abstract class Gatherer extends Vacuous {
    @Override public final void p(final double x, final double y, final double dy) {
      gather(x, y, dy);
    }
    protected void gather(final double x, final double y, final double dy) {
      xs.push(x);
      ys.push(y);
      dys.push(dy);
    }

    public final double[] xs() {
      return xs.toArray();
    }
    public final double[] ys() {
      return ys.toArray();
    }
    public final double[] dys() {
      return dys.toArray();
    }

    private final DoublesArray xs = new DoublesArray();
    private final DoublesArray ys = new DoublesArray();
    private final DoublesArray dys = new DoublesArray();
  }

  public static abstract class Filter extends Gatherer {
    @Override public final void gather(final double x, final double y, final double dy) {
      if (valid(x, y, dy))
        super.gather(x, y, dy);
    }
    public abstract boolean valid(final double x, final double y, final double dy);

  }

  public static class RealsOnly extends Filter {
    @Override public boolean valid(final double x, final double y, final double dy) {
      return isReal(x) && isReal(y) && isReal(dy);
    }

    @SuppressWarnings({ "static-method" })//
    public static class TEST {
      @Test public void isDefinedTrue() {
        assertTrue(isReal(1));
        assertTrue(isReal(0));
      }
      @Test public void isDefinedFalse() {
        assertFalse(isReal(Double.POSITIVE_INFINITY));
        assertFalse(isReal(Double.NEGATIVE_INFINITY));
        assertFalse(isReal(Math.log(0)));
        assertFalse(isReal(Double.NaN));
      }
      @Test public void feed() {
        final RealsOnly p = new RealsOnly();
        p.feed(//
            doubles(Double.NaN, 1, 4, 3), //
            doubles(0, Double.NEGATIVE_INFINITY, 5, 3), //
            doubles(0, 1, 6, Double.NEGATIVE_INFINITY)//
        );
        assertEquals(1, p.xs().length);
        assertEquals(1, p.ys().length);
        assertEquals(1, p.dys().length);
        assertEquals(4.0, p.xs()[0], 1E-10);
        assertEquals(5.0, p.ys()[0], 1E-10);
        assertEquals(6.0, p.dys()[0], 1E-10);
      }
    }
  }
}

