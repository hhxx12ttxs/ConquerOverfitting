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
package org.achartengine.renderer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.achartengine.util.MathHelper;

import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Multiple XY series renderer.
 */
public class XYMultipleSeriesRenderer extends DefaultRenderer {
  /** The X axis title. */
  private String mXTitle = "";
  /** The Y axis title. */
  private String[] mYTitle;
  /** The axis title text size. */
  private float mAxisTitleTextSize = 12;
  /** The start value in the X axis range. */
  private double[] mMinX;
  /** The end value in the X axis range. */
  private double[] mMaxX;
  /** The start value in the Y axis range. */
  private double[] mMinY;
  /** The end value in the Y axis range. */
  private double[] mMaxY;
  /** The approximative number of labels on the x axis. */
  private int mXLabels = 5;
  /** The approximative number of labels on the y axis. */
  private int mYLabels = 5;
  /** The current orientation of the chart. */
  private Orientation mOrientation = Orientation.HORIZONTAL;
  /** The X axis text labels. */
  private Map<Double, String> mXTextLabels = new HashMap<Double, String>();
  /** The Y axis text labels. */
  private Map<Integer, Map<Double, String>> mYTextLabels = new LinkedHashMap<Integer, Map<Double, String>>();
  /** A flag for enabling or not the pan on the X axis. */
  private boolean mPanXEnabled = true;
  /** A flag for enabling or not the pan on the Y axis. */
  private boolean mPanYEnabled = true;
  /** A flag for enabling or not the zoom on the X axis. */
  private boolean mZoomXEnabled = true;
  /** A flag for enabling or not the zoom on the Y axis . */
  private boolean mZoomYEnabled = true;
  /** The spacing between bars, in bar charts. */
  private double mBarSpacing = 0;
  /** The margins colors. */
  private int mMarginsColor = NO_COLOR;
  /** The pan limits. */
  private double[] mPanLimits;
  /** The zoom limits. */
  private double[] mZoomLimits;
  /** The X axis labels rotation angle. */
  private float mXLabelsAngle;
  /** The Y axis labels rotation angle. */
  private float mYLabelsAngle;
  /** The initial axis range. */
  private final Map<Integer, double[]> initialRange = new LinkedHashMap<Integer, double[]>();
  /** The point size for charts displaying points. */
  private float mPointSize = 3;
  /** The grid color. */
  private int mGridColor = Color.argb(75, 200, 200, 200);
  /** The number of scales. */
  private final int scalesCount;
  /** The X axis labels alignment. */
  private Align xLabelsAlign = Align.CENTER;
  /** The Y axis labels alignment. */
  private Align[] yLabelsAlign;
  /** The Y axis alignment. */
  private Align[] yAxisAlign;
  /** draw axes below series **/
  private boolean mDrawAxesBelowSeries;
  /** draw the y-axis **/
  private boolean mShowYAxis = true;
private boolean mShowAverageLines;

  /**
   * An enum for the XY chart orientation of the X axis.
   */
  public enum Orientation {
    HORIZONTAL(0), VERTICAL(90);
    /** The rotate angle. */
    private int mAngle = 0;

    private Orientation(int angle) {
      mAngle = angle;
    }

    /**
     * Return the orientation rotate angle.
     * 
     * @return the orientaion rotate angle
     */
    public int getAngle() {
      return mAngle;
    }
  }

  public XYMultipleSeriesRenderer() {
    this(1);
  }

  public XYMultipleSeriesRenderer(int scaleNumber) {
    scalesCount = scaleNumber;
    initAxesRange(scaleNumber);
  }

  public void initAxesRange(int scales) {
    mYTitle = new String[scales];
    yLabelsAlign = new Align[scales];
    yAxisAlign = new Align[scales];
    mMinX = new double[scales];
    mMaxX = new double[scales];
    mMinY = new double[scales];
    mMaxY = new double[scales];
    for (int i = 0; i < scales; i++) {
      initAxesRangeForScale(i);
    }
  }

  public void initAxesRangeForScale(int i) {
    mMinX[i] = MathHelper.NULL_VALUE;
    mMaxX[i] = -MathHelper.NULL_VALUE;
    mMinY[i] = MathHelper.NULL_VALUE;
    mMaxY[i] = -MathHelper.NULL_VALUE;
    double[] range = new double[] { mMinX[i], mMaxX[i], mMinY[i], mMaxY[i] };
    initialRange.put(i, range);
    mYTitle[i] = "";
    mYTextLabels.put(i, new HashMap<Double, String>());
    yLabelsAlign[i] = Align.CENTER;
    yAxisAlign[i] = Align.LEFT;
  }

  /**
   * Returns the current orientation of the chart X axis.
   * 
   * @return the chart orientation
   */
  public Orientation getOrientation() {
    return mOrientation;
  }

  /**
   * Sets the current orientation of the chart X axis.
   * 
   * @param orientation the chart orientation
   */
  public void setOrientation(Orientation orientation) {
    mOrientation = orientation;
  }

  /**
   * Returns the title for the X axis.
   * 
   * @return the X axis title
   */
  public String getXTitle() {
    return mXTitle;
  }

  /**
   * Sets the title for the X axis.
   * 
   * @param title the X axis title
   */
  public void setXTitle(String title) {
    mXTitle = title;
  }

  /**
   * Returns the title for the Y axis.
   * 
   * @return the Y axis title
   */
  public String getYTitle() {
    return getYTitle(0);
  }

  /**
   * Returns the title for the Y axis.
   * 
   * @param scale the renderer scale
   * @return the Y axis title
   */
  public String getYTitle(int scale) {
    return mYTitle[scale];
  }

  /**
   * Sets the title for the Y axis.
   * 
   * @param title the Y axis title
   */
  public void setYTitle(String title) {
    setYTitle(title, 0);
  }

  /**
   * Sets the title for the Y axis.
   * 
   * @param title the Y axis title
   * @param scale the renderer scale
   */
  public void setYTitle(String title, int scale) {
    mYTitle[scale] = title;
  }

  /**
   * Returns the axis title text size.
   * 
   * @return the axis title text size
   */
  public float getAxisTitleTextSize() {
    return mAxisTitleTextSize;
  }

  /**
   * Sets the axis title text size.
   * 
   * @param textSize the chart axis text size
   */
  public void setAxisTitleTextSize(float textSize) {
    mAxisTitleTextSize = textSize;
  }

  /**
   * Returns the start value of the X axis range.
   * 
   * @return the X axis range start value
   */
  public double getXAxisMin() {
    return getXAxisMin(0);
  }

  /**
   * Sets the start value of the X axis range.
   * 
   * @param min the X axis range start value
   */
  public void setXAxisMin(double min) {
    setXAxisMin(min, 0);
  }

  /**
   * Returns if the minimum X value was set.
   * 
   * @return the minX was set or not
   */
  public boolean isMinXSet() {
    return isMinXSet(0);
  }

  /**
   * Returns the end value of the X axis range.
   * 
   * @return the X axis range end value
   */
  public double getXAxisMax() {
    return getXAxisMax(0);
  }

  /**
   * Sets the end value of the X axis range.
   * 
   * @param max the X axis range end value
   */
  public void setXAxisMax(double max) {
    setXAxisMax(max, 0);
  }

  /**
   * Returns if the maximum X value was set.
   * 
   * @return the maxX was set or not
   */
  public boolean isMaxXSet() {
    return isMaxXSet(0);
  }

  /**
   * Returns the start value of the Y axis range.
   * 
   * @return the Y axis range end value
   */
  public double getYAxisMin() {
    return getYAxisMin(0);
  }

  /**
   * Sets the start value of the Y axis range.
   * 
   * @param min the Y axis range start value
   */
  public void setYAxisMin(double min) {
    setYAxisMin(min, 0);
  }

  /**
   * Returns if the minimum Y value was set.
   * 
   * @return the minY was set or not
   */
  public boolean isMinYSet() {
    return isMinYSet(0);
  }

  /**
   * Returns the end value of the Y axis range.
   * 
   * @return the Y axis range end value
   */
  public double getYAxisMax() {
    return getYAxisMax(0);
  }

  /**
   * Sets the end value of the Y axis range.
   * 
   * @param max the Y axis range end value
   */
  public void setYAxisMax(double max) {
    setYAxisMax(max, 0);
  }

  /**
   * Returns if the maximum Y value was set.
   * 
   * @return the maxY was set or not
   */
  public boolean isMaxYSet() {
    return isMaxYSet(0);
  }

  /**
   * Returns the start value of the X axis range.
   * 
   * @param scale the renderer scale
   * @return the X axis range start value
   */
  public double getXAxisMin(int scale) {
    return mMinX[scale];
  }

  /**
   * Sets the start value of the X axis range.
   * 
   * @param min the X axis range start value
   * @param scale the renderer scale
   */
  public void setXAxisMin(double min, int scale) {
    if (!isMinXSet(scale)) {
      initialRange.get(scale)[0] = min;
    }
    mMinX[scale] = min;
  }

  /**
   * Returns if the minimum X value was set.
   * 
   * @param scale the renderer scale
   * @return the minX was set or not
   */
  public boolean isMinXSet(int scale) {
    return mMinX[scale] != MathHelper.NULL_VALUE;
  }

  /**
   * Returns the end value of the X axis range.
   * 
   * @param scale the renderer scale
   * @return the X axis range end value
   */
  public double getXAxisMax(int scale) {
    return mMaxX[scale];
  }

  /**
   * Sets the end value of the X axis range.
   * 
   * @param max the X axis range end value
   * @param scale the renderer scale
   */
  public void setXAxisMax(double max, int scale) {
    if (!isMaxXSet(scale)) {
      initialRange.get(scale)[1] = max;
    }
    mMaxX[scale] = max;
  }

  /**
   * Returns if the maximum X value was set.
   * 
   * @param scale the renderer scale
   * @return the maxX was set or not
   */
  public boolean isMaxXSet(int scale) {
    return mMaxX[scale] != -MathHelper.NULL_VALUE;
  }

  /**
   * Returns the start value of the Y axis range.
   * 
   * @param scale the renderer scale
   * @return the Y axis range end value
   */
  public double getYAxisMin(int scale) {
    return mMinY[scale];
  }

  /**
   * Sets the start value of the Y axis range.
   * 
   * @param min the Y axis range start value
   * @param scale the renderer scale
   */
  public void setYAxisMin(double min, int scale) {
    if (!isMinYSet(scale)) {
      initialRange.get(scale)[2] = min;
    }
    mMinY[scale] = min;
  }

  /**
   * Returns if the minimum Y value was set.
   * 
   * @param scale the renderer scale
   * @return the minY was set or not
   */
  public boolean isMinYSet(int scale) {
    return mMinY[scale] != MathHelper.NULL_VALUE;
  }

  /**
   * Returns the end value of the Y axis range.
   * 
   * @param scale the renderer scale
   * @return the Y axis range end value
   */
  public double getYAxisMax(int scale) {
    return mMaxY[scale];
  }

  /**
   * Sets the end value of the Y axis range.
   * 
   * @param max the Y axis range end value
   * @param scale the renderer scale
   */
  public void setYAxisMax(double max, int scale) {
    if (!isMaxYSet(scale)) {
      initialRange.get(scale)[3] = max;
    }
    mMaxY[scale] = max;
  }

  /**
   * Returns if the maximum Y value was set.
   * 
   * @param scale the renderer scale
   * @return the maxY was set or not
   */
  public boolean isMaxYSet(int scale) {
    return mMaxY[scale] != -MathHelper.NULL_VALUE;
  }

  /**
   * Returns the approximate number of labels for the X axis.
   * 
   * @return the approximate number of labels for the X axis
   */
  public int getXLabels() {
    return mXLabels;
  }

  /**
   * Sets the approximate number of labels for the X axis.
   * 
   * @param xLabels the approximate number of labels for the X axis
   */
  public void setXLabels(int xLabels) {
    mXLabels = xLabels;
  }

  /**
   * Adds a new text label for the specified X axis value.
   * 
   * @param x the X axis value
   * @param text the text label
   * @deprecated use addXTextLabel instead
   */
  public void addTextLabel(double x, String text) {
    addXTextLabel(x, text);
  }

  /**
   * Adds a new text label for the specified X axis value.
   * 
   * @param x the X axis value
   * @param text the text label
   */
  public void addXTextLabel(double x, String text) {
    mXTextLabels.put(x, text);
  }

  /**
   * Returns the X axis text label at the specified X axis value.
   * 
   * @param x the X axis value
   * @return the X axis text label
   */
  public String getXTextLabel(Double x) {
    return mXTextLabels.get(x);
  }

  /**
   * Returns the X text label locations.
   * 
   * @return the X text label locations
   */
  public Double[] getXTextLabelLocations() {
    return mXTextLabels.keySet().toArray(new Double[0]);
  }

  /**
   * Clears the existing text labels.
   * 
   * @deprecated use clearXTextLabels instead
   */
  public void clearTextLabels() {
    clearXTextLabels();
  }

  /**
   * Clears the existing text labels on the X axis.
   */
  public void clearXTextLabels() {
    mXTextLabels.clear();
  }

  /**
   * Adds a new text label for the specified Y axis value.
   * 
   * @param y the Y axis value
   * @param text the text label
   */
  public void addYTextLabel(double y, String text) {
    addYTextLabel(y, text, 0);
  }

  /**
   * Adds a new text label for the specified Y axis value.
   * 
   * @param y the Y axis value
   * @param text the text label
   * @param scale the renderer scale
   */
  public void addYTextLabel(double y, String text, int scale) {
    mYTextLabels.get(scale).put(y, text);
  }

  /**
   * Returns the Y axis text label at the specified Y axis value.
   * 
   * @param y the Y axis value
   * @return the Y axis text label
   */
  public String getYTextLabel(Double y) {
    return getYTextLabel(y, 0);
  }

  /**
   * Returns the Y axis text label at the specified Y axis value.
   * 
   * @param y the Y axis value
   * @param scale the renderer scale
   * @return the Y axis text label
   */
  public String getYTextLabel(Double y, int scale) {
    return mYTextLabels.get(scale).get(y);
  }

  /**
   * Returns the Y text label locations.
   * 
   * @return the Y text label locations
   */
  public Double[] getYTextLabelLocations() {
    return getYTextLabelLocations(0);
  }

  /**
   * Returns the Y text label locations.
   * 
   * @param scale the renderer scale
   * @return the Y text label locations
   */
  public Double[] getYTextLabelLocations(int scale) {
    return mYTextLabels.get(scale).keySet().toArray(new Double[0]);
  }

  /**
   * Clears the existing text labels on the Y axis.
   */
  public void clearYTextLabels() {
    mYTextLabels.clear();
  }

  /**
   * Returns the approximate number of labels for the Y axis.
   * 
   * @return the approximate number of labels for the Y axis
   */
  public int getYLabels() {
    return mYLabels;
  }

  /**
   * Sets the approximate number of labels for the Y axis.
   * 
   * @param yLabels the approximate number of labels for the Y axis
   */
  public void setYLabels(int yLabels) {
    mYLabels = yLabels;
  }

  /**
   * Sets if the chart point values should be displayed as text.
   * 
   * @param display if the chart point values should be displayed as text
   * @deprecated use SimpleSeriesRenderer.setDisplayChartValues() instead
   */
  public void setDisplayChartValues(boolean display) {
    SimpleSeriesRenderer[] renderers = getSeriesRenderers();
    for (SimpleSeriesRenderer renderer : renderers) {
      renderer.setDisplayChartValues(display);
    }
  }

  /**
   * Sets the chart values text size.
   * 
   * @param textSize the chart values text size
   * @deprecated use SimpleSeriesRenderer.setChartValuesTextSize() instead
   */
  public void setChartValuesTextSize(float textSize) {
    SimpleSeriesRenderer[] renderers = getSeriesRenderers();
    for (SimpleSeriesRenderer renderer : renderers) {
      renderer.setChartValuesTextSize(textSize);
    }
  }

  /**
   * Returns the enabled state of the pan on at least one axis.
   * 
   * @return if pan is enabled
   */
  public boolean isPanEnabled() {
    return isPanXEnabled() || isPanYEnabled();
  }

  /**
   * Returns the enabled state of the pan on X axis.
   * 
   * @return if pan is enabled on X axis
   */
  public boolean isPanXEnabled() {
    return mPanXEnabled;
  }

  /**
   * Returns the enabled state of the pan on Y axis.
   * 
   * @return if pan is enabled on Y axis
   */
  public boolean isPanYEnabled() {
    return mPanYEnabled;
  }

  /**
   * Sets the enabled state of the pan.
   * 
   * @param enabledX pan enabled on X axis
   * @param enabledY pan enabled on Y axis
   */
  public void setPanEnabled(boolean enabledX, boolean enabledY) {
    mPanXEnabled = enabledX;
    mPanYEnabled = enabledY;
  }

  /**
   * Returns the enabled state of the zoom on at least one axis.
   * 
   * @return if zoom is enabled
   */
  public boolean isZoomEnabled() {
    return isZoomXEnabled() || isZoomYEnabled();
  }

  /**
   * Returns the enabled state of the zoom on X axis.
   * 
   * @return if zoom is enabled on X axis
   */
  public boolean isZoomXEnabled() {
    return mZoomXEnabled;
  }

  /**
   * Returns the enabled state of the zoom on Y axis.
   * 
   * @return if zoom is enabled on Y axis
   */
  public boolean isZoomYEnabled() {
    return mZoomYEnabled;
  }

  /**
   * Sets the enabled state of the zoom.
   * 
   * @param enabledX zoom enabled on X axis
   * @param enabledY zoom enabled on Y axis
   */
  public void setZoomEnabled(boolean enabledX, boolean enabledY) {
    mZoomXEnabled = enabledX;
    mZoomYEnabled = enabledY;
  }

  /**
   * Returns the spacing between bars, in bar charts.
   * 
   * @return the spacing between bars
   * @deprecated use getBarSpacing instead
   */
  public double getBarsSpacing() {
    return getBarSpacing();
  }

  /**
   * Returns the spacing between bars, in bar charts.
   * 
   * @return the spacing between bars
   */
  public double getBarSpacing() {
    return mBarSpacing;
  }

  /**
   * Sets the spacing between bars, in bar charts. Only available for bar
   * charts. This is a coefficient of the bar width. For instance, if you want
   * the spacing to be a half of the bar width, set this value to 0.5.
   * 
   * @param spacing the spacing between bars coefficient
   */
  public void setBarSpacing(double spacing) {
    mBarSpacing = spacing;
  }

  /**
   * Returns the margins color.
   * 
   * @return the margins color
   */
  public int getMarginsColor() {
    return mMarginsColor;
  }

  /**
   * Sets the color of the margins.
   * 
   * @param color the margins color
   */
  public void setMarginsColor(int color) {
    mMarginsColor = color;
  }

  /**
   * Returns the grid color.
   * 
   * @return the grid color
   */
  public int getGridColor() {
    return mGridColor;
  }

  /**
   * Sets the color of the grid.
   * 
   * @param color the grid color
   */
  public void setGridColor(int color) {
    mGridColor = color;
  }

  /**
   * Returns the pan limits.
   * 
   * @return the pan limits
   */
  public double[] getPanLimits() {
    return mPanLimits;
  }

  /**
   * Sets the pan limits as an array of 4 values. Setting it to null or a
   * different size array will disable the panning limitation. Values:
   * [panMinimumX, panMaximumX, panMinimumY, panMaximumY]
   * 
   * @param panLimits the pan limits
   */
  public void setPanLimits(double[] panLimits) {
    mPanLimits = panLimits;
  }

  /**
   * Returns the zoom limits.
   * 
   * @return the zoom limits
   */
  public double[] getZoomLimits() {
    return mZoomLimits;
  }

  /**
   * Sets the zoom limits as an array of 4 values. Setting it to null or a
   * different size array will disable the zooming limitation. Values:
   * [zoomMinimumX, zoomMaximumX, zoomMinimumY, zoomMaximumY]
   * 
   * @param zoomLimits the zoom limits
   */
  public void setZoomLimits(double[] zoomLimits) {
    mZoomLimits = zoomLimits;
  }

  /**
   * Returns the rotation angle of labels for the X axis.
   * 
   * @return the rotation angle of labels for the X axis
   */
  public float getXLabelsAngle() {
    return mXLabelsAngle;
  }

  /**
   * Sets the rotation angle (in degrees) of labels for the X axis.
   * 
   * @param angle the rotation angle of labels for the X axis
   */
  public void setXLabelsAngle(float angle) {
    mXLabelsAngle = angle;
  }

  /**
   * Returns the rotation angle of labels for the Y axis.
   * 
   * @return the approximate number of labels for the Y axis
   */
  public float getYLabelsAngle() {
    return mYLabelsAngle;
  }

  /**
   * Sets the rotation angle (in degrees) of labels for the Y axis.
   * 
   * @param angle the rotation angle of labels for the Y axis
   */
  public void setYLabelsAngle(float angle) {
    mYLabelsAngle = angle;
  }

  /**
   * Returns the size of the points, for charts displaying points.
   * 
   * @return the point size
   */
  public float getPointSize() {
    return mPointSize;
  }

  /**
   * Sets the size of the points, for charts displaying points.
   * 
   * @param size the point size
   */
  public void setPointSize(float size) {
    mPointSize = size;
  }

  public void setRange(double[] range) {
    setRange(range, 0);
  }

  /**
   * Sets the axes range values.
   * 
   * @param range an array having the values in this order: minX, maxX, minY,
   *          maxY
   * @param scale the renderer scale
   */
  public void setRange(double[] range, int scale) {
    setXAxisMin(range[0], scale);
    setXAxisMax(range[1], scale);
    setYAxisMin(range[2], scale);
    setYAxisMax(range[3], scale);
  }

  public boolean isInitialRangeSet() {
    return isInitialRangeSet(0);
  }

  /**
   * Returns if the initial range is set.
   * 
   * @param scale the renderer scale
   * @return the initial range was set or not
   */
  public boolean isInitialRangeSet(int scale) {
    return initialRange.get(scale) != null;
  }

  /**
   * Returns the initial range.
   * 
   * @return the initial range
   */
  public double[] getInitialRange() {
    return getInitialRange(0);
  }

  /**
   * Returns the initial range.
   * 
   * @param scale the renderer scale
   * @return the initial range
   */
  public double[] getInitialRange(int scale) {
    return initialRange.get(scale);
  }

  /**
   * Sets the axes initial range values. This will be used in the zoom fit tool.
   * 
   * @param range an array having the values in this order: minX, maxX, minY,
   *          maxY
   */
  public void setInitialRange(double[] range) {
    setInitialRange(range, 0);
  }

  /**
   * Sets the axes initial range values. This will be used in the zoom fit tool.
   * 
   * @param range an array having the values in this order: minX, maxX, minY,
   *          maxY
   * @param scale the renderer scale
   */
  public void setInitialRange(double[] range, int scale) {
    initialRange.put(scale, range);
  }

  /**
   * Returns the X axis labels alignment.
   * 
   * @return X labels alignment
   */
  public Align getXLabelsAlign() {
    return xLabelsAlign;
  }

  /**
   * Sets the X axis labels alignment.
   * 
   * @param align the X labels alignment
   */
  public void setXLabelsAlign(Align align) {
    xLabelsAlign = align;
  }

  /**
   * Returns the Y axis labels alignment.
   * 
   * @param scale the renderer scale
   * @return Y labels alignment
   */
  public Align getYLabelsAlign(int scale) {
    return yLabelsAlign[scale];
  }

  public void setYLabelsAlign(Align align) {
    setYLabelsAlign(align, 0);
  }

  public Align getYAxisAlign(int scale) {
    return yAxisAlign[scale];
  }

  public void setYAxisAlign(Align align, int scale) {
    yAxisAlign[scale] = align;
  }

  /**
   * Sets the Y axis labels alignment.
   * 
   * @param align the Y labels alignment
   */
  public void setYLabelsAlign(Align align, int scale) {
    yLabelsAlign[scale] = align;
  }

  public int getScalesCount() {
    return scalesCount;
  }

  public boolean drawAxesBelowSeries() {
	  return mDrawAxesBelowSeries;
  }

  public void setDrawAxesBelowSeries(boolean below) {
	  mDrawAxesBelowSeries = below;
  }

  public boolean isShowYAxis() {
	  return mShowYAxis;
  }

  public void setShowYAxis(boolean show) {
	  mShowYAxis = show;
  }

  public boolean isShowAverageLines() {
	  return mShowAverageLines;
  }

  public void setShowAverageLines(boolean show) {
	  mShowAverageLines = show;
  }

}

=======
/*
 * This file is part of the prefuse visualization
 * toolkit which you can find at: http://prefuse.org/
 *
 * Modified by Markus Echterhoff <evopaint@markusechterhoff.com>
 * Modified lines marked with "// MODIFIED"
 * Any modifications are licensed as the rest of EvoPaint under GPLv3+
 *
 * all of prefuse was released under the following license:
 */

/*
  Copyright (c) 2004-2007 Regents of the University of California.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

  1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

  3.  Neither the name of the University nor the names of its contributors
  may be used to endorse or promote products derived from this software
  without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.
 */

package evopaint.gui.rulesetmanager.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>Implements a Swing-based Range slider, which allows the user to enter a
 * range (minimum and maximum) value.</p>
 *
 * @author Ben Bederson
 * @author Jesse Grosjean
 * @author Jon Meyer
 * @author Lance Good
 * @author jeffrey heer
 * @author Colin Combe
 */
public class JRangeSlider extends JComponent
    implements MouseListener, MouseMotionListener, KeyListener
{
    /*
     * NOTE: This is a modified version of the original class distributed by
     * Ben Bederson, Jesse Grosjean, and Jon Meyer as part of an HCIL Tech
     * Report.  It is modified to allow both vertical and horitonal modes.
     * It also fixes a bug with offset on the buttons. Also fixed a bug with
     * rendering using (x,y) instead of (0,0) as origin.  Also modified to
     * render arrows as a series of lines rather than as a GeneralPath.
     * Also modified to fix rounding errors on toLocal and toScreen.
     *
     * With inclusion in prefuse, this class has been further modified to use a
     * bounded range model, support keyboard commands and more extensize
     * parameterization of rendering/appearance options. Furthermore, a stub
     * method has been introduced to allow subclasses to perform custom
     * rendering within the slider through.
     */

    final public static int VERTICAL = 0;
    final public static int HORIZONTAL = 1;
    final public static int LEFTRIGHT_TOPBOTTOM = 0;
    final public static int RIGHTLEFT_BOTTOMTOP = 1;

    final public static int PREFERRED_BREADTH = 16;
    final public static int PREFERRED_LENGTH = 100;
    final protected static int ARROW_SZ = 16;
    final protected static int ARROW_WIDTH = 8;
    final protected static int ARROW_HEIGHT = 4;

    protected BoundedRangeModel model;
    protected int orientation;
    protected int direction;
    protected boolean empty;
    protected int increment = 1;
    protected int minExtent = 0; // min extent, in pixels

    protected ArrayList listeners = new ArrayList();
    protected ChangeEvent changeEvent = null;
    protected ChangeListener lstnr;

    protected Color thumbColor = new Color(150,180,220);

    // ------------------------------------------------------------------------

    /**
     * Create a new range slider.
     *
     * @param minimum - the minimum value of the range.
     * @param maximum - the maximum value of the range.
     * @param lowValue - the current low value shown by the range slider's bar.
     * @param highValue - the current high value shown by the range slider's bar.
     * @param orientation - construct a horizontal or vertical slider?
     */
    public JRangeSlider(int minimum, int maximum, int lowValue, int highValue, int orientation) {
        this(new DefaultBoundedRangeModel(lowValue, highValue - lowValue, minimum, maximum),
                orientation,LEFTRIGHT_TOPBOTTOM);
    }

    /**
     * Create a new range slider.
     *
     * @param minimum - the minimum value of the range.
     * @param maximum - the maximum value of the range.
     * @param lowValue - the current low value shown by the range slider's bar.
     * @param highValue - the current high value shown by the range slider's bar.
     * @param orientation - construct a horizontal or vertical slider?
     * @param direction - Is the slider left-to-right/top-to-bottom or right-to-left/bottom-to-top
     */
    public JRangeSlider(int minimum, int maximum, int lowValue, int highValue, int orientation, int direction) {
        this(new DefaultBoundedRangeModel(lowValue, highValue - lowValue, minimum, maximum),
                orientation, direction);
    }

    /**
     * Create a new range slider.
     *
     * @param model - a BoundedRangeModel specifying the slider's range
     * @param orientation - construct a horizontal or vertical slider?
     * @param direction - Is the slider left-to-right/top-to-bottom or right-to-left/bottom-to-top
     */
    public JRangeSlider(BoundedRangeModel model, int orientation, int direction) {
        super.setFocusable(true);
        this.model = model;
        this.orientation = orientation;
        this.direction = direction;

        setForeground(Color.LIGHT_GRAY);

        this.lstnr = createListener();
        model.addChangeListener(lstnr);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    /**
     * Create a listener to relay change events from the bounded range model.
     * @return a ChangeListener to relay events from the range model
     */
    protected ChangeListener createListener() {
        return new RangeSliderChangeListener();
    }

    /**
     * Listener that fires a change event when it receives  change event from
     * the slider list model.
     */
    protected class RangeSliderChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the current "low" value shown by the range slider's bar. The low
     * value meets the constraint minimum <= lowValue <= highValue <= maximum.
     */
    public int getLowValue() {
        return model.getValue();
    }

    /**
     * Sets the low value shown by this range slider. This causes the range slider to be
     * repainted and a ChangeEvent to be fired.
     * @param lowValue the low value to use
     */
    public void setLowValue(int lowValue) {
        int e = (model.getValue()-lowValue)+model.getExtent();
        model.setRangeProperties(lowValue, e,
            model.getMinimum(), model.getMaximum(), false);
        model.setValue(lowValue);
    }

    /**
     * Returns the current "high" value shown by the range slider's bar. The high
     * value meets the constraint minimum <= lowValue <= highValue <= maximum.
     */
    public int getHighValue() {
        return model.getValue()+model.getExtent();
    }

    /**
     * Sets the high value shown by this range slider. This causes the range slider to be
     * repainted and a ChangeEvent to be fired.
     * @param highValue the high value to use
     */
    public void setHighValue(int highValue) {
        model.setExtent(highValue-model.getValue());
    }

    /**
     * Set the slider range span.
     * @param lowValue the low value of the slider range
     * @param highValue the high value of the slider range
     */
    public void setRange(int lowValue, int highValue) {
        model.setRangeProperties(lowValue, highValue-lowValue,
                model.getMinimum(), model.getMaximum(), false);
    }

    /**
     * Gets the minimum possible value for either the low value or the high value.
     * @return the minimum possible range value
     */
    public int getMinimum() {
        return model.getMinimum();
    }

    /**
     * Sets the minimum possible value for either the low value or the high value.
     * @param minimum the minimum possible range value
     */
    public void setMinimum(int minimum) {
        model.setMinimum(minimum);
    }

    /**
     * Gets the maximum possible value for either the low value or the high value.
     * @return the maximum possible range value
     */
    public int getMaximum() {
        return model.getMaximum();
    }

    /**
     * Sets the maximum possible value for either the low value or the high value.
     * @param maximum the maximum possible range value
     */
    public void setMaximum(int maximum) {
        model.setMaximum(maximum);
    }

    /**
     * Sets the minimum extent (difference between low and high values).
     * This method <strong>does not</strong> change the current state of the
     * model, but can affect all subsequent interaction.
     * @param minExtent the minimum extent allowed in subsequent interaction
     */
    public void setMinExtent(int minExtent) {
        this.minExtent = minExtent;
    }

    /**
     * Sets whether this slider is empty.
     * @param empty true if set to empty, false otherwise
     */
    public void setEmpty(boolean empty) {
        this.empty = empty;
        repaint();
    }

    /**
     * Get the slider thumb color. This is the part of the slider between
     * the range resize buttons.
     * @return the slider thumb color
     */
    public Color getThumbColor() {
        return thumbColor;
    }

    /**
     * Set the slider thumb color. This is the part of the slider between
     * the range resize buttons.
     * @param thumbColor the slider thumb color
     */
    public void setThumbColor(Color thumbColor) {
        this.thumbColor = thumbColor;
    }

    /**
     * Get the BoundedRangeModel backing this slider.
     * @return the slider's range model
     */
    public BoundedRangeModel getModel() {
        return model;
    }

    /**
     * Set the BoundedRangeModel backing this slider.
     * @param brm the slider range model to use
     */
    public void setModel(BoundedRangeModel brm) {
        model.removeChangeListener(lstnr);
        model = brm;
        model.addChangeListener(lstnr);
        repaint();
    }

    /**
     * Registers a listener for ChangeEvents.
     * @param cl the ChangeListener to add
     */
    public void addChangeListener(ChangeListener cl) {
        if ( !listeners.contains(cl) )
            listeners.add(cl);
    }

    /**
     * Removes a listener for ChangeEvents.
     * @param cl the ChangeListener to remove
     */
    public void removeChangeListener(ChangeListener cl) {
        listeners.remove(cl);
    }

    /**
     * Fire a change event to all listeners.
     */
    protected void fireChangeEvent() {
        repaint();
        if ( changeEvent == null )
            changeEvent = new ChangeEvent(this);
        Iterator iter = listeners.iterator();
        while ( iter.hasNext() )
            ((ChangeListener)iter.next()).stateChanged(changeEvent);
    }

    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        if (orientation == VERTICAL) {
            return new Dimension(PREFERRED_BREADTH, PREFERRED_LENGTH);
        }
        else {
            return new Dimension(PREFERRED_LENGTH, PREFERRED_BREADTH);
        }
    }

    // ------------------------------------------------------------------------
    // Rendering

    /**
     * Override this method to perform custom painting of the slider trough.
     * @param g a Graphics2D context for rendering
     * @param width the width of the slider trough
     * @param height the height of the slider trough
     */
    protected void customPaint(Graphics2D g, int width, int height) {
        // does nothing in this class
        // subclasses can override to perform custom painting
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        Rectangle bounds = getBounds();
        int width = (int)bounds.getWidth() - 1;
        int height = (int)bounds.getHeight() - 1;

        int min = toScreen(getLowValue());
        int max = toScreen(getHighValue());

        // Paint the full slider if the slider is marked as empty
        if (empty) {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                min = ARROW_SZ;
                max = (orientation == VERTICAL) ? height-ARROW_SZ : width-ARROW_SZ;
            }
            else {
                min = (orientation == VERTICAL) ? height-ARROW_SZ : width-ARROW_SZ;
                max = ARROW_SZ;
            }
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f)); // MODIFIED
        }

        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, width, height);
        g2.setColor(getForeground());
        g2.drawRect(0, 0, width, height);

        customPaint(g2, width, height);

        // Draw arrow and thumb backgrounds
        g2.setStroke(new BasicStroke(1));
        if (orientation == VERTICAL) {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                g2.setColor(getForeground());
                g2.fillRect(0, min - ARROW_SZ, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,min-ARROW_SZ,width,ARROW_SZ-1);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(0, min, width, max - min-1);
                    paint3DRectLighting(g2,0,min,width,max-min-1);
                }

                g2.setColor(getForeground());
                g2.fillRect(0, max, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,max,width,ARROW_SZ-1);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, min - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, true);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, max + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, false);
            }
            else {
                g2.setColor(getForeground());
                g2.fillRect(0, min, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,min,width,ARROW_SZ-1);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(0, max, width, min-max-1);
                    paint3DRectLighting(g2,0,max,width,min-max-1);
                }

                g2.setColor(getForeground());
                g2.fillRect(0, max-ARROW_SZ, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,max-ARROW_SZ,width,ARROW_SZ-1);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, min + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, false);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, max - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, true);
            }
        }
        else {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                g2.setColor(getForeground());
                g2.fillRect(min - ARROW_SZ, 0, ARROW_SZ-1, height);
                paint3DRectLighting(g2,min-ARROW_SZ,0,ARROW_SZ-1,height);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(min, 0, max - min - 1, height);
                    paint3DRectLighting(g2,min,0,max-min-1,height);
                }

                g2.setColor(getForeground());
                g2.fillRect(max, 0, ARROW_SZ-1, height);
                paint3DRectLighting(g2,max,0,ARROW_SZ-1,height);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, min - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, true);
                paintArrow(g2, max + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, false);
            }
            else {
                g2.setColor(getForeground());
                g2.fillRect(min, 0, ARROW_SZ - 1, height);
                paint3DRectLighting(g2,min,0,ARROW_SZ-1,height);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(max, 0, min - max - 1, height);
                    paint3DRectLighting(g2,max,0,min-max-1,height);
                }

                g2.setColor(getForeground());
                g2.fillRect(max-ARROW_SZ, 0, ARROW_SZ-1, height);
                paint3DRectLighting(g2,max-ARROW_SZ,0,ARROW_SZ-1,height);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, min + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, true);
                paintArrow(g2, max - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, false);
            }
        }
    }

    /**
     * This draws an arrow as a series of lines within the specified box.
     * The last boolean specifies whether the point should be at the
     * right/bottom or left/top.
     */
    protected void paintArrow(Graphics2D g2, double x, double y, int w, int h,
                              boolean topDown)
    {
        int intX = (int)(x+0.5);
        int intY = (int)(y+0.5);

        if (orientation == VERTICAL) {
            if (w % 2 == 0) {
                w = w - 1;
            }

            if (topDown) {
                for(int i=0; i<(w/2+1); i++) {
                    g2.drawLine(intX+i,intY+i,intX+w-i-1,intY+i);
                }
            }
            else {
                for(int i=0; i<(w/2+1); i++) {
                    g2.drawLine(intX+w/2-i,intY+i,intX+w-w/2+i-1,intY+i);
                }
            }
        }
        else {
            if (h % 2 == 0) {
                h = h - 1;
            }

            if (topDown) {
                for(int i=0; i<(h/2+1); i++) {
                    g2.drawLine(intX+i,intY+i,intX+i,intY+h-i-1);
                }
            }
            else {
                for(int i=0; i<(h/2+1); i++) {
                    g2.drawLine(intX+i,intY+h/2-i,intX+i,intY+h-h/2+i-1);
                }
            }
        }
    }

    /**
     * Adds Windows2K type 3D lighting effects
     */
    protected void paint3DRectLighting(Graphics2D g2, int x, int y,
                                       int width, int height)
    {
        g2.setColor(Color.white);
        g2.drawLine(x+1,y+1,x+1,y+height-1);
        g2.drawLine(x+1,y+1,x+width-1,y+1);
        g2.setColor(Color.gray);
        g2.drawLine(x+1,y+height-1,x+width-1,y+height-1);
        g2.drawLine(x+width-1,y+1,x+width-1,y+height-1);
        g2.setColor(Color.darkGray);
        g2.drawLine(x,y+height,x+width,y+height);
        g2.drawLine(x+width,y,x+width,y+height);
    }

    /**
     * Converts from screen coordinates to a range value.
     */
    protected int toLocal(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }
        else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }

        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return (int) (((xOrY - ARROW_SZ) / scale) + min + 0.5);
        }
        else {
            if (orientation == VERTICAL) {
                return (int) ((sz.height - xOrY - ARROW_SZ) / scale + min + 0.5);
            }
            else {
                return (int) ((sz.width - xOrY - ARROW_SZ) / scale + min + 0.5);
            }
        }
    }

    /**
     * Converts from a range value to screen coordinates.
     */
    protected int toScreen(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }
        else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }

        // If the direction is left/right_top/bottom then we subtract the min and multiply times scale
        // Otherwise, we have to invert the number by subtracting the value from the height
        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return (int)(ARROW_SZ + ((xOrY - min) * scale) + 0.5);
        }
        else {
            if (orientation == VERTICAL) {
                return (int)(sz.height-(xOrY - min) * scale - ARROW_SZ + 0.5);
            }
            else {
                return (int)(sz.width-(xOrY - min) * scale - ARROW_SZ + 0.5);
            }
        }
    }

    /**
     * Converts from a range value to screen coordinates.
     */
    protected double toScreenDouble(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum()+1 - min);
        }
        else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum()+1 - min);
        }

        // If the direction is left/right_top/bottom then we subtract the min and multiply times scale
        // Otherwise, we have to invert the number by subtracting the value from the height
        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return ARROW_SZ + ((xOrY - min) * scale);
        }
        else {
            if (orientation == VERTICAL) {
                return sz.height-(xOrY - min) * scale - ARROW_SZ;
            }
            else {
                return sz.width-(xOrY - min) * scale - ARROW_SZ;
            }
        }
    }


    // ------------------------------------------------------------------------
    // Event Handling

    static final int PICK_NONE = 0;
    static final int PICK_LEFT_OR_TOP = 1;
    static final int PICK_THUMB = 2;
    static final int PICK_RIGHT_OR_BOTTOM = 3;
    int pick;
    int pickOffsetLow;
    int pickOffsetHigh;
    int mouse;

    private int pickHandle(int xOrY) {
        int min = toScreen(getLowValue());
        int max = toScreen(getHighValue());
        int pick = PICK_NONE;

        if (direction == LEFTRIGHT_TOPBOTTOM) {
            if ((xOrY > (min - ARROW_SZ)) && (xOrY < min)) {
                pick = PICK_LEFT_OR_TOP;
            } else if ((xOrY >= min) && (xOrY <= max)) {
                pick = PICK_THUMB;
            } else if ((xOrY > max) && (xOrY < (max + ARROW_SZ))) {
                pick = PICK_RIGHT_OR_BOTTOM;
            }
        }
        else {
            if ((xOrY > min) && (xOrY < (min + ARROW_SZ))) {
                pick = PICK_LEFT_OR_TOP;
            } else if ((xOrY <= min) && (xOrY >= max)) {
                pick = PICK_THUMB;
            } else if ((xOrY > (max - ARROW_SZ) && (xOrY < max))) {
                pick = PICK_RIGHT_OR_BOTTOM;
            }
        }

        return pick;
    }

    private void offset(int dxOrDy) {
        model.setValue(model.getValue()+dxOrDy);
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        if (orientation == VERTICAL) {
            pick = pickHandle(e.getY());
            pickOffsetLow = e.getY() - toScreen(getLowValue());
            pickOffsetHigh = e.getY() - toScreen(getHighValue());
            mouse = e.getY();
        }
        else {
            pick = pickHandle(e.getX());
            pickOffsetLow = e.getX() - toScreen(getLowValue());
            pickOffsetHigh = e.getX() - toScreen(getHighValue());
            mouse = e.getX();
        }
        repaint();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
        requestFocus();
        int value = (orientation == VERTICAL) ? e.getY() : e.getX();

        int minimum = getMinimum();
        int maximum = getMaximum();
        int lowValue = getLowValue();
        int highValue = getHighValue();

        switch (pick) {
            case PICK_LEFT_OR_TOP:
                int low = toLocal(value-pickOffsetLow);

                if (low < minimum) {
                    low = minimum;
                }
                if (low > maximum - minExtent) {
                    low = maximum - minExtent;
                }
                if (low > highValue-minExtent) {
                    setRange(low, low + minExtent);
                }
                else
                    setLowValue(low);
                break;

            case PICK_RIGHT_OR_BOTTOM:
                int high = toLocal(value-pickOffsetHigh);

                if (high < minimum + minExtent) {
                    high = minimum + minExtent;
                }
                if (high > maximum) {
                    high = maximum;
                }
                if (high < lowValue+minExtent) {
                    setRange(high - minExtent, high);
                }
                else
                    setHighValue(high);
                break;

            case PICK_THUMB:
                int dxOrDy = toLocal(value - pickOffsetLow) - lowValue;
                if ((dxOrDy < 0) && ((lowValue + dxOrDy) < minimum)) {
                    dxOrDy = minimum - lowValue;
                }
                if ((dxOrDy > 0) && ((highValue + dxOrDy) > maximum)) {
                    dxOrDy = maximum - highValue;
                }
                if (dxOrDy != 0) {
                    offset(dxOrDy);
                }
                break;
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        pick = PICK_NONE;
        repaint();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
        if (orientation == VERTICAL) {
            switch (pickHandle(e.getY())) {
                case PICK_LEFT_OR_TOP:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_RIGHT_OR_BOTTOM:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_THUMB:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_NONE :
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
            }
        }
        else {
            switch (pickHandle(e.getX())) {
                case PICK_LEFT_OR_TOP:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_RIGHT_OR_BOTTOM:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_THUMB:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_NONE :
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }

    private void grow(int increment) {
        model.setRangeProperties(model.getValue()-increment,
            model.getExtent()+2*increment,
            model.getMinimum(), model.getMaximum(), false);
    }

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        boolean v = (orientation == VERTICAL);
        boolean d = (kc == KeyEvent.VK_DOWN);
        boolean u = (kc == KeyEvent.VK_UP);
        boolean l = (kc == KeyEvent.VK_LEFT);
        boolean r = (kc == KeyEvent.VK_RIGHT);

        int minimum = getMinimum();
        int maximum = getMaximum();
        int lowValue = getLowValue();
        int highValue = getHighValue();

        if ( v&&r || !v&&u ) {
            if ( lowValue-increment >= minimum &&
                 highValue+increment <= maximum ) {
                grow(increment);
            }
        } else if ( v&&l || !v&&d ) {
            if ( highValue-lowValue >= 2*increment ) {
                grow(-1*increment);
            }
        } else if ( v&&d || !v&&l ) {
            if ( lowValue-increment >= minimum ) {
                offset(-increment);
            }
        } else if ( v&&u || !v&&r ) {
            if ( highValue+increment <= maximum ) {
                offset(increment);
            }
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
    }
    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

} // end of class JRangeSlider
>>>>>>> 76aa07461566a5976980e6696204781271955163
