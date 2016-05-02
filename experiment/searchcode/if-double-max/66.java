<<<<<<< HEAD
/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.chart;

import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.DialRenderer.Type;
import org.achartengine.util.MathHelper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

/**
 * The dial chart rendering class.
 */
public class DialChart extends RoundChart {
  /** The radius of the needle. */
  private static final int NEEDLE_RADIUS = 10;
  /** The series renderer. */
  private DialRenderer mRenderer;

  /**
   * Builds a new dial chart instance.
   * 
   * @param dataset the series dataset
   * @param renderer the dial renderer
   */
  public DialChart(CategorySeries dataset, DialRenderer renderer) {
    super(dataset, renderer);
    mRenderer = renderer;
  }

  /**
   * The graphical representation of the dial chart.
   * 
   * @param canvas the canvas to paint to
   * @param x the top left x value of the view to draw to
   * @param y the top left y value of the view to draw to
   * @param width the width of the view to draw to
   * @param height the height of the view to draw to
   * @param paint the paint
   */
  @Override
  public void draw(Canvas canvas, int x, int y, int width, int height, Paint paint) {
    paint.setAntiAlias(mRenderer.isAntialiasing());
    paint.setStyle(Style.FILL);
    paint.setTextSize(mRenderer.getLabelsTextSize());
    int legendSize = getLegendSize(mRenderer, height / 5, 0);
    int left = x;
    int top = y;
    int right = x + width;

    int sLength = mDataset.getItemCount();
    String[] titles = new String[sLength];
    for (int i = 0; i < sLength; i++) {
      titles[i] = mDataset.getCategory(i);
    }

    if (mRenderer.isFitLegend()) {
      legendSize = drawLegend(canvas, mRenderer, titles, left, right, y, width, height, legendSize,
          paint, true);
    }
    int bottom = y + height - legendSize;
    drawBackground(mRenderer, canvas, x, y, width, height, paint, false, DefaultRenderer.NO_COLOR);

    int mRadius = Math.min(Math.abs(right - left), Math.abs(bottom - top));
    int radius = (int) (mRadius * 0.35 * mRenderer.getScale());
    if (mCenterX == NO_VALUE) {
      mCenterX = (left + right) / 2;
    }
    if (mCenterY == NO_VALUE) {
      mCenterY = (bottom + top) / 2;
    }
    float shortRadius = radius * 0.9f;
    float longRadius = radius * 1.1f;
    double min = mRenderer.getMinValue();
    double max = mRenderer.getMaxValue();
    double angleMin = mRenderer.getAngleMin();
    double angleMax = mRenderer.getAngleMax();
    if (!mRenderer.isMinValueSet() || !mRenderer.isMaxValueSet()) {
      int count = mRenderer.getSeriesRendererCount();
      for (int i = 0; i < count; i++) {
        double value = mDataset.getValue(i);
        if (!mRenderer.isMinValueSet()) {
          min = Math.min(min, value);
        }
        if (!mRenderer.isMaxValueSet()) {
          max = Math.max(max, value);
        }
      }
    }
    if (min == max) {
      min = min * 0.5;
      max = max * 1.5;
    }

    paint.setColor(mRenderer.getLabelsColor());
    double minorTicks = mRenderer.getMinorTicksSpacing();
    double majorTicks = mRenderer.getMajorTicksSpacing();
    if (minorTicks == MathHelper.NULL_VALUE) {
      minorTicks = (max - min) / 30;
    }
    if (majorTicks == MathHelper.NULL_VALUE) {
      majorTicks = (max - min) / 10;
    }
    drawTicks(canvas, min, max, angleMin, angleMax, mCenterX, mCenterY, longRadius, radius,
        minorTicks, paint, false);
    drawTicks(canvas, min, max, angleMin, angleMax, mCenterX, mCenterY, longRadius, shortRadius,
        majorTicks, paint, true);

    int count = mRenderer.getSeriesRendererCount();
    for (int i = 0; i < count; i++) {
      double angle = getAngleForValue(mDataset.getValue(i), angleMin, angleMax, min, max);
      paint.setColor(mRenderer.getSeriesRendererAt(i).getColor());
      boolean type = mRenderer.getVisualTypeForIndex(i) == Type.ARROW;
      drawNeedle(canvas, angle, mCenterX, mCenterY, shortRadius, type, paint);
    }
    drawLegend(canvas, mRenderer, titles, left, right, y, width, height, legendSize, paint, false);
    drawTitle(canvas, x, y, width, paint);
  }

  /**
   * Returns the angle for a specific chart value.
   * 
   * @param value the chart value
   * @param minAngle the minimum chart angle value
   * @param maxAngle the maximum chart angle value
   * @param min the minimum chart value
   * @param max the maximum chart value
   * @return the angle
   */
  private double getAngleForValue(double value, double minAngle, double maxAngle, double min,
      double max) {
    double angleDiff = maxAngle - minAngle;
    double diff = max - min;
    return Math.toRadians(minAngle + (value - min) * angleDiff / diff);
  }

  /**
   * Draws the chart tick lines.
   * 
   * @param canvas the canvas
   * @param min the minimum chart value
   * @param max the maximum chart value
   * @param minAngle the minimum chart angle value
   * @param maxAngle the maximum chart angle value
   * @param centerX the center x value
   * @param centerY the center y value
   * @param longRadius the long radius
   * @param shortRadius the short radius
   * @param ticks the tick spacing
   * @param paint the paint settings
   * @param labels paint the labels
   * @return the angle
   */
  private void drawTicks(Canvas canvas, double min, double max, double minAngle, double maxAngle,
      int centerX, int centerY, double longRadius, double shortRadius, double ticks, Paint paint,
      boolean labels) {
    for (double i = min; i <= max; i += ticks) {
      double angle = getAngleForValue(i, minAngle, maxAngle, min, max);
      double sinValue = Math.sin(angle);
      double cosValue = Math.cos(angle);
      int x1 = Math.round(centerX + (float) (shortRadius * sinValue));
      int y1 = Math.round(centerY + (float) (shortRadius * cosValue));
      int x2 = Math.round(centerX + (float) (longRadius * sinValue));
      int y2 = Math.round(centerY + (float) (longRadius * cosValue));
      canvas.drawLine(x1, y1, x2, y2, paint);
      if (labels) {
        paint.setTextAlign(Align.LEFT);
        if (x1 <= x2) {
          paint.setTextAlign(Align.RIGHT);
        }
        String text = i + "";
        if (Math.round(i) == (long) i) {
          text = (long) i + "";
        }
        canvas.drawText(text, x1, y1, paint);
      }
    }
  }

  /**
   * Returns the angle for a specific chart value.
   * 
   * @param canvas the canvas
   * @param angle the needle angle value
   * @param centerX the center x value
   * @param centerY the center y value
   * @param radius the radius
   * @param arrow if a needle or an arrow to be painted
   * @param paint the paint settings
   * @return the angle
   */
  private void drawNeedle(Canvas canvas, double angle, int centerX, int centerY, double radius,
      boolean arrow, Paint paint) {
    double diff = Math.toRadians(90);
    int needleSinValue = (int) (NEEDLE_RADIUS * Math.sin(angle - diff));
    int needleCosValue = (int) (NEEDLE_RADIUS * Math.cos(angle - diff));
    int needleX = (int) (radius * Math.sin(angle));
    int needleY = (int) (radius * Math.cos(angle));
    int needleCenterX = centerX + needleX;
    int needleCenterY = centerY + needleY;
    float[] points;
    if (arrow) {
      int arrowBaseX = centerX + (int) (radius * 0.85 * Math.sin(angle));
      int arrowBaseY = centerY + (int) (radius * 0.85 * Math.cos(angle));
      points = new float[] { arrowBaseX - needleSinValue, arrowBaseY - needleCosValue,
          needleCenterX, needleCenterY, arrowBaseX + needleSinValue, arrowBaseY + needleCosValue };
      float width = paint.getStrokeWidth();
      paint.setStrokeWidth(5);
      canvas.drawLine(centerX, centerY, needleCenterX, needleCenterY, paint);
      paint.setStrokeWidth(width);
    } else {
      points = new float[] { centerX - needleSinValue, centerY - needleCosValue, needleCenterX,
          needleCenterY, centerX + needleSinValue, centerY + needleCosValue };
    }
    drawPath(canvas, points, paint, true);
  }

}
=======
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

/**
 * This class is used to model the statistics
 * of a fix of numbers.  For the statistics
 * we choose here it is not necessary to store
 * all the numbers - just keeping a running total
 * of how many, the sum and the sum of the squares
 * is sufficient (plus max and min, for max and min).
 * <p/>
 * Warning: the geometric mean calculation is only valid if all numbers
 * added to the summary are positive (>0) - no warnings
 * are given if this is not the case - you'll just get a wrong answer
 * for the gm() !!!
 */

public class StatisticalSummary implements java.io.Serializable
{

// a temporary fix for an immediate need
// this should really be handled with a more general
// predicate class

public static class Watch
{
    double x;
    public int count;

    public Watch(double x)
    {
        this.x = x;
        count = 0;
    }

    public void note(double val)
    {
        if (val == x)
        {
            count++;
        }
    }

    public String toString()
    {
        return x + " occured " + count + " times ";
    }

    public void reset()
    {
        count = 0;
    }
}

// following line can cause prog to hang - bug in Java?
// protected long serialVersionUID = new Double("-1490108905720833569").longValue();
// protected long serialVersionUID = 123;
public String name; // defaults to ""
private double logsum; // for calculating the geometric mean
private double sum;
private double sumsq;
private double min;
private double max;

private double mean;
private double gm; // geometric mean
private double sd;

// trick class loader into loading this now
//    private static StatisticalTests dummy = new StatisticalTests();

int n;
boolean valid;
public Watch watch;

public StatisticalSummary()
{
    this("");
    // System.out.println("Exited default...");
}

public StatisticalSummary(String name)
{
    // System.out.println("Creating SS");
    this.name = name;
    n = 0;
    sum = 0;
    sumsq = 0;
    // ensure that the first number to be
    // added will fix up min and max to
    // be that number
    min = Double.POSITIVE_INFINITY;
    max = Double.NEGATIVE_INFINITY;
    // System.out.println("Finished Creating SS");
    watch = null;
    valid = false;
}

public final void reset()
{
    n = 0;
    sum = 0;
    sumsq = 0;
    logsum = 0;
    // ensure that the first number to be
    // added will fix up min and max to
    // be that number
    min = Double.POSITIVE_INFINITY;
    max = Double.NEGATIVE_INFINITY;
    if (watch != null)
    {
        watch.reset();
    }
}


public double max()
{
    return max;
}

public double min()
{
    return min;
}

public double mean()
{
    if (!valid)
        computeStats();
    return mean;
}

public double gm()
{
    if (!valid)
        computeStats();
    return gm;
}
/* erroneous
  public static double sigDiff( StatisticalSummary s1 , StatisticalSummary s2 ) {
    return StatisticalTests.tNotPaired(
      s1.mean(), s2.mean(), s1.sumsq, s2.sumsq, s1.n, s2.n, true);
  }
*/

public static double sigDiff(StatisticalSummary s1, StatisticalSummary s2)
{
    return StatisticalTests.tNotPaired(
            s1.mean(), s2.mean(), s1.sumSquareDiff(), s2.sumSquareDiff(), s1.n, s2.n, true);
}

/**
 * returns the sum of the squares of the differences
 * between the mean and the ith values
 */
public double sumSquareDiff()
{
    return sumsq - n * mean() * mean();
}

private void computeStats()
{
    if (!valid)
    {
        mean = sum / n;
        gm = Math.exp(logsum / n);
        double num = sumsq - (n * mean * mean);
        if (num < 0)
        {
            // avoids tiny negative numbers possible through imprecision
            num = 0;
        }
        // System.out.println("Num = " + num);
        sd = Math.sqrt(num / (n - 1));
        // System.out.println(" Test: sd = " + sd);
        // System.out.println(" Test: n = " + n);
        valid = true;
    }
}

public double sd()
{
    if (!valid)
        computeStats();
    return sd;
}

public int n()
{
    return n;
}

public double stdErr()
{
    return sd() / Math.sqrt(n);
}

public void add(StatisticalSummary ss)
{
    // implications for Watch?
    n += ss.n;
    sum += ss.sum;
    sumsq += ss.sumsq;
    logsum += ss.logsum;
    max = Math.max(max, ss.max);
    min = Math.min(min, ss.min);
    valid = false;
}

public void add(double d)
{
    n++;
    sum += d;
    sumsq += d * d;
    if (d > 0)
    {
        logsum += Math.log(d);
    }
    min = Math.min(min, d);
    max = Math.max(max, d);
    if (watch != null)
    {
        watch.note(d);
    }
    valid = false;
}

public void add(Number n)
{
    add(n.doubleValue());
}

public void add(double[] d)
{
    for (int i = 0; i < d.length; i++)
    {
        add(d[i]);
    }
}

public void add(Vector v)
{
    for (int i = 0; i < v.size(); i++)
    {
        try
        {
            add(((Number) v.elementAt(i)).doubleValue());
        } catch (Exception e)
        {
        }
    }
}

public String toString()
{
    String s = (name == null) ? "" : name + "\n";
    s += " min = " + min() + "\n" +
            " max = " + max() + "\n" +
            " ave = " + mean() + "\n" +
            " sd  = " + sd() + "\n" +
            // " se  = " + stdErr() + "\n" +
            // " sum  = " + sum + "\n" +
            // " sumsq  = " + sumsq + "\n" +
            // " watch = " + watch + "\n" +
            " n   = " + n;
    return s;

}

public void save(String path)
{
    try
    {
        ObjectOutputStream oos =
                new ObjectOutputStream(
                        new FileOutputStream(path));
        oos.writeObject(this);
        oos.close();
    } catch (Exception e)
    {
        System.out.println(e);
    }
}

public static StatisticalSummary load(String path)
{
    try
    {
        ObjectInputStream ois =
                new ObjectInputStream(
                        new FileInputStream(path));
        StatisticalSummary ss = (StatisticalSummary) ois.readObject();
        ois.close();
        return ss;
    } catch (Exception e)
    {
        System.out.println(e);
        return null;
    }
}


public static void main(String[] args) throws Exception
{
    // demonstrate some possible usage...

    StatisticalSummary ts1 = new StatisticalSummary();
    StatisticalSummary ts2 = new StatisticalSummary();
    for (int i = 0; i < 100; i++)
    {
        ts1.add(i / 10);
        ts2.add(i / 10 + new Double(args[0]).doubleValue());
    }

    System.out.println(ts1);
    System.out.println(ts2);
    System.out.println(StatisticalSummary.sigDiff(ts1, ts2));
    System.out.println((ts2.mean() - ts1.mean()) / ts1.stdErr());

    System.exit(0);

    System.out.println("Creating summaries");

    StatisticalSummary trainSummary = new StatisticalSummary();
    System.out.println("1");
    // StatisticalSummary testSummary = new VisualSummary("EA");
    System.out.println("2");
    // testSummary.watch = new StatisticalSummary.Watch( 1.0 );
    System.out.println("3");
    // StatisticalSummary ostiaTrainSummary = new StatisticalSummary();
    System.out.println("4");
    // ostiaTestSummary = new VisualSummary("OSTIA");
    System.out.println("5");
    // ostiaTestSummary.watch = new StatisticalSummary.Watch( 1.0 );

    System.out.println("Created summaries");


    StatisticalSummary s10 = new StatisticalSummary();
    StatisticalSummary s20 = new StatisticalSummary();
    StatisticalSummary s3 = new StatisticalSummary();
    StatisticalSummary s4 = new StatisticalSummary();
    StatisticalSummary s5 = new StatisticalSummary();
    StatisticalSummary ss = new StatisticalSummary("Hello");
    for (int i = 0; i < 20; i++)
    {
        ss.add(0.71);
    }
    System.out.println(ss);
    System.exit(0);

    StatisticalSummary s1 = new StatisticalSummary();
    StatisticalSummary s2 = new StatisticalSummary();

    System.out.println(sigDiff(s1, s2));

    for (int i = 0; i < 20; i++)
    {
        s1.add(Math.random());
        s2.add(Math.random() + 0.5);
        // s1.add(i);
        // s2.add(i+2);
        System.out.println(sigDiff(s1, s2));
    }
}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

