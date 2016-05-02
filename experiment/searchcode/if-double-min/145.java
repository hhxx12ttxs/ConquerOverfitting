<<<<<<< HEAD
package com.jjoe64.graphview;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.jjoe64.graphview.compatible.ScaleGestureDetector;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs.
 * This is the abstract base class for all graphs. Extend this class and implement {@link #drawSeries(Canvas, GraphViewData[], float, float, float, double, double, double, double, float)} to display a custom graph.
 * Use {@link LineGraphView} for creating a line chart.
 *
 * @author jjoe64 - jonas gehring - http://www.jjoe64.com
 *
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 */
abstract public class GraphView extends LinearLayout {
	static final private class GraphViewConfig {
		static final float BORDER = 20;
		static final float VERTICAL_LABEL_WIDTH = 100;
		static final float HORIZONTAL_LABEL_HEIGHT = 80;
	}

	private class GraphViewContentView extends View {
		private float lastTouchEventX;
		private float graphwidth;

		/**
		 * @param context
		 */
		public GraphViewContentView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {

            paint.setAntiAlias(true);

			// normal
			paint.setStrokeWidth(0);

			float border = GraphViewConfig.BORDER;
			float horstart = 0;
			float height = getHeight();
			float width = getWidth() - 1;
			double maxY = getMaxY();
			double minY = getMinY();
			double diffY = maxY - minY;
			double maxX = getMaxX(false);
			double minX = getMinX(false);
			double diffX = maxX - minX;
			float graphheight = height - (2 * border);
			graphwidth = width;

			if (horlabels == null) {
				horlabels = generateHorlabels(graphwidth);
			}
			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// vertical lines
			paint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				paint.setColor(Color.DKGRAY);
				float y = ((graphheight / vers) * i) + border;
				canvas.drawLine(horstart, y, width, y, paint);
			}

			// horizontal labels + lines
			int hors = horlabels.length - 1;
			for (int i = 0; i < horlabels.length; i++) {
				paint.setColor(Color.DKGRAY);
				float x = ((graphwidth / hors) * i) + horstart;
				canvas.drawLine(x, height - border, x, border, paint);
				paint.setTextAlign(Align.CENTER);
				if (i==horlabels.length-1)
					paint.setTextAlign(Align.RIGHT);
				if (i==0)
					paint.setTextAlign(Align.LEFT);
				paint.setColor(Color.WHITE);
				canvas.drawText(horlabels[i], x, height - 4, paint);
			}

			paint.setTextAlign(Align.CENTER);
			canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

			if (maxY != minY) {
				paint.setStrokeCap(Paint.Cap.ROUND);

				for (int i=0; i<graphSeries.size(); i++) {
					paint.setStrokeWidth(graphSeries.get(i).style.thickness);
					paint.setColor(graphSeries.get(i).style.color);
					drawSeries(canvas, _values(i), graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart);
				}

				if (showLegend) drawLegend(canvas, height, width);
			}
		}

		private void onMoveGesture(float f) {
			// view port update
			if (viewportSize != 0) {
				viewportStart -= f*viewportSize/graphwidth;

				// minimal and maximal view limit
				double minX = getMinX(true);
				double maxX = getMaxX(true);
				if (viewportStart < minX) {
					viewportStart = minX;
				} else if (viewportStart+viewportSize > maxX) {
					viewportStart = maxX - viewportSize;
				}

				// labels have to be regenerated
				horlabels = null;
				verlabels = null;
				viewVerLabels.invalidate();
			}
			invalidate();
		}

		/**
		 * @param event
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (!isScrollable()) {
				return super.onTouchEvent(event);
			}

			boolean handled = false;
			// first scale
			if (scalable && scaleDetector != null) {
				scaleDetector.onTouchEvent(event);
				handled = scaleDetector.isInProgress();
			}
			if (!handled) {
				// if not scaled, scroll
				if ((event.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN) {
					handled = true;
				}
				if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
					lastTouchEventX = 0;
					handled = true;
				}
				if ((event.getAction() & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE) {
					if (lastTouchEventX != 0) {
						onMoveGesture(event.getX() - lastTouchEventX);
					}
					lastTouchEventX = event.getX();
					handled = true;
				}
				if (handled)
					invalidate();
			}
			return handled;
		}
	}

	/**
	 * graph series style: color and thickness
	 */
	static public class GraphViewStyle {
		public int color = 0xff0077cc;
		public int thickness = 3;
		public GraphViewStyle() {
			super();
		}
		public GraphViewStyle(int color, int thickness) {
			super();
			this.color = color;
			this.thickness = thickness;
		}
	}

	/**
	 * one data set for a graph series
	 */
	static public class GraphViewData {
		public final double valueX;
		public final double valueY;
		public GraphViewData(double valueX, double valueY) {
			super();
			this.valueX = valueX;
			this.valueY = valueY;
		}
	}

	/**
	 * a graph series
	 */
	static public class GraphViewSeries {
		final String description;
		final GraphViewStyle style;
		final GraphViewData[] values;
		public GraphViewSeries(GraphViewData[] values) {
			description = null;
			style = new GraphViewStyle();
			this.values = values;
		}
		public GraphViewSeries(String description, GraphViewStyle style, GraphViewData[] values) {
			super();
			this.description = description;
			if (style == null) {
				style = new GraphViewStyle();
			}
			this.style = style;
			this.values = values;
		}
	}

	public enum LegendAlign {
		TOP, MIDDLE, BOTTOM
	}

	private class VerLabelsView extends View {
		/**
		 * @param context
		 */
		public VerLabelsView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 10));
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {
			// normal
			paint.setStrokeWidth(0);

			float border = GraphViewConfig.BORDER;
			float height = getHeight();
			float graphheight = height - (2 * border);

			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// vertical labels
			paint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				float y = ((graphheight / vers) * i) + border;
				paint.setColor(Color.WHITE);
				canvas.drawText(verlabels[i], 0, y, paint);
			}
		}
	}

	protected final Paint paint;
	private String[] horlabels;
	private String[] verlabels;
	private String title;
	private boolean scrollable;
	private double viewportStart;
	private double viewportSize;
	private final View viewVerLabels;
	private ScaleGestureDetector scaleDetector;
	private boolean scalable;
	private NumberFormat numberformatter;
	private final List<GraphViewSeries> graphSeries;
	private boolean showLegend = false;
	private float legendWidth = 120;
	private LegendAlign legendAlign = LegendAlign.MIDDLE;
	private boolean manualYAxis;
	private double manualMaxYValue;
	private double manualMinYValue;

	/**
	 *
	 * @param context
	 * @param title [optional]
	 */
	public GraphView(Context context, String title) {
		super(context);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		if (title == null)
			title = "";
		else
			this.title = title;

		paint = new Paint();
		graphSeries = new ArrayList<GraphViewSeries>();

		viewVerLabels = new VerLabelsView(context);
		addView(viewVerLabels);
		addView(new GraphViewContentView(context), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
	}

	private GraphViewData[] _values(int idxSeries) {
		GraphViewData[] values = graphSeries.get(idxSeries).values;
		if (viewportStart == 0 && viewportSize == 0) {
			// all data
			return values;
		} else {
			// viewport
			List<GraphViewData> listData = new ArrayList<GraphViewData>();
			for (int i=0; i<values.length; i++) {
				if (values[i].valueX >= viewportStart) {
					if (values[i].valueX > viewportStart+viewportSize) {
						listData.add(values[i]); // one more for nice scrolling
						break;
					} else {
						listData.add(values[i]);
					}
				} else {
					if (listData.isEmpty()) {
						listData.add(values[i]);
					}
					listData.set(0, values[i]); // one before, for nice scrolling
				}
			}
			return listData.toArray(new GraphViewData[listData.size()]);
		}
	}

	public void addSeries(GraphViewSeries series) {
		graphSeries.add(series);
	}

	public void removeSeries(int index)
	{
		if (index < 0 || index >= graphSeries.size())
		{
			throw new IndexOutOfBoundsException("No series at index " + index);
		}
		
		graphSeries.remove(index);
	}
	
	public void removeSeries(GraphViewSeries series)
	{
		graphSeries.remove(series);
	}
	
	protected void drawLegend(Canvas canvas, float height, float width) {
		int shapeSize = 15;

		// rect
		paint.setARGB(180, 100, 100, 100);
		float legendHeight = (shapeSize+5)*graphSeries.size() +5;
		float lLeft = width-legendWidth - 10;
		float lTop;
		switch (legendAlign) {
		case TOP:
			lTop = 10;
			break;
		case MIDDLE:
			lTop = height/2 - legendHeight/2;
			break;
		default:
			lTop = height - GraphViewConfig.BORDER - legendHeight -10;
		}
		float lRight = lLeft+legendWidth;
		float lBottom = lTop+legendHeight;
		canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, paint);

		for (int i=0; i<graphSeries.size(); i++) {
			paint.setColor(graphSeries.get(i).style.color);
			canvas.drawRect(new RectF(lLeft+5, lTop+5+(i*(shapeSize+5)), lLeft+5+shapeSize, lTop+((i+1)*(shapeSize+5))), paint);
			if (graphSeries.get(i).description != null) {
				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(graphSeries.get(i).description, lLeft+5+shapeSize+5, lTop+shapeSize+(i*(shapeSize+5)), paint);
			}
		}
	}

	abstract public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart);

	/**
	 * formats the label
	 * can be overwritten
	 * @param value x and y values
	 * @param isValueX if false, value y wants to be formatted
	 * @return value to display
	 */
	protected String formatLabel(double value, boolean isValueX) {
		if (numberformatter == null) {
			numberformatter = NumberFormat.getNumberInstance();
			double highestvalue = getMaxY();
			double lowestvalue = getMinY();
			if (highestvalue - lowestvalue < 0.1) {
				numberformatter.setMaximumFractionDigits(6);
			} else if (highestvalue - lowestvalue < 1) {
				numberformatter.setMaximumFractionDigits(4);
			} else if (highestvalue - lowestvalue < 20) {
				numberformatter.setMaximumFractionDigits(3);
			} else if (highestvalue - lowestvalue < 100) {
				numberformatter.setMaximumFractionDigits(1);
			} else {
				numberformatter.setMaximumFractionDigits(0);
			}
		}
		return numberformatter.format(value);
	}

	private String[] generateHorlabels(float graphwidth) {
		int numLabels = (int) (graphwidth/GraphViewConfig.VERTICAL_LABEL_WIDTH);
		String[] labels = new String[numLabels+1];
		double min = getMinX(false);
		double max = getMaxX(false);
		for (int i=0; i<=numLabels; i++) {
			labels[i] = formatLabel(min + ((max-min)*i/numLabels), true);
		}
		return labels;
	}

	synchronized private String[] generateVerlabels(float graphheight) {
		int numLabels = (int) (graphheight/GraphViewConfig.HORIZONTAL_LABEL_HEIGHT);
		String[] labels = new String[numLabels+1];
		double min = getMinY();
		double max = getMaxY();
		for (int i=0; i<=numLabels; i++) {
			labels[numLabels-i] = formatLabel(min + ((max-min)*i/numLabels), false);
		}
		return labels;
	}

	public LegendAlign getLegendAlign() {
		return legendAlign;
	}

	public float getLegendWidth() {
		return legendWidth;
	}

	/**
	 * returns the maximal X value of the current viewport (if viewport is set)
	 * otherwise maximal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart+viewportSize;
		} else {
			// otherwise use the max x value
			// values must be sorted by x, so the last value has the largest X value
			double highest = 0;
			if (graphSeries.size() > 0)
			{
				GraphViewData[] values = graphSeries.get(0).values;
				highest = values[values.length-1].valueX;
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					highest = Math.max(highest, values[values.length-1].valueX);
				}
			}
			return highest;
		}
	}

	/**
	 * returns the maximal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxY() {
		double largest;
		if (manualYAxis) {
			largest = manualMaxYValue;
		} else {
			largest = Integer.MIN_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				GraphViewData[] values = _values(i);
				for (int ii=0; ii<values.length; ii++)
					if (values[ii].valueY > largest)
						largest = values[ii].valueY;
			}
		}
		return largest;
	}

	/**
	 * returns the minimal X value of the current viewport (if viewport is set)
	 * otherwise minimal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart;
		} else {
			// otherwise use the min x value
			// values must be sorted by x, so the first value has the smallest X value
			double lowest = 0;
			if (graphSeries.size() > 0)
			{
				GraphViewData[] values = graphSeries.get(0).values;
				lowest = values[0].valueX;
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					lowest = Math.min(lowest, values[0].valueX);
				}
			}
			return lowest;
		}
	}

	/**
	 * returns the minimal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinY() {
		double smallest;
		if (manualYAxis) {
			smallest = manualMinYValue;
		} else {
			smallest = Integer.MAX_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				GraphViewData[] values = _values(i);
				for (int ii=0; ii<values.length; ii++)
					if (values[ii].valueY < smallest)
						smallest = values[ii].valueY;
			}
		}
		return smallest;
	}

	public boolean isScrollable() {
		return scrollable;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * set's static horizontal labels (from left to right)
	 * @param horlabels if null, labels were generated automatically
	 */
	public void setHorizontalLabels(String[] horlabels) {
		this.horlabels = horlabels;
	}

	public void setLegendAlign(LegendAlign legendAlign) {
		this.legendAlign = legendAlign;
	}

	public void setLegendWidth(float legendWidth) {
		this.legendWidth = legendWidth;
	}

	/**
	 * you have to set the bounds {@link #setManualYAxisBounds(double, double)}. That automatically enables manualYAxis-flag.
	 * if you want to disable the menual y axis, call this method with false.
	 * @param manualYAxis
	 */
	public void setManualYAxis(boolean manualYAxis) {
		this.manualYAxis = manualYAxis;
	}

	/**
	 * set manual Y axis limit
	 * @param max
	 * @param min
	 */
	public void setManualYAxisBounds(double max, double min) {
		manualMaxYValue = max;
		manualMinYValue = min;
		manualYAxis = true;
	}

	/**
	 * this forces scrollable = true
	 * @param scalable
	 */
	synchronized public void setScalable(boolean scalable) {
		this.scalable = scalable;
		if (scalable == true && scaleDetector == null) {
			scrollable = true; // automatically forces this
			scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
				@Override
				public boolean onScale(ScaleGestureDetector detector) {
					double center = viewportStart + viewportSize / 2;
					viewportSize /= detector.getScaleFactor();
					viewportStart = center - viewportSize / 2;
					
					// viewportStart must not be < minX
					double minX = getMinX(true);
					if (viewportStart < minX) {
						viewportStart = minX;
					}

					// viewportStart + viewportSize must not be > maxX
					double maxX = getMaxX(true);
					double overlap = viewportStart + viewportSize - maxX;
					if (overlap > 0) {
						// scroll left
						if (viewportStart-overlap > minX) {
							viewportStart -= overlap;
						} else {
							// maximal scale
							viewportStart = minX;
							viewportSize = maxX - viewportStart;
						}
					}

					verlabels = null;
					horlabels = null;
					numberformatter = null;
					invalidate();
					viewVerLabels.invalidate();
					return true;
				}
			});
		}
	}

	/**
	 * the user can scroll (horizontal) the graph. This is only useful if you use a viewport {@link #setViewPort(double, double)} which doesn't displays all data.
	 * @param scrollable
	 */
	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * set's static vertical labels (from top to bottom)
	 * @param verlabels if null, labels were generated automatically
	 */
	public void setVerticalLabels(String[] verlabels) {
		this.verlabels = verlabels;
	}

	/**
	 * set's the viewport for the graph.
	 * @param start x-value
	 * @param size
	 */
	public void setViewPort(double start, double size) {
		viewportStart = start;
		viewportSize = size;
	}
}

=======
/*
Copyright 2008 Matt Radkie
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
* 
*  $Revision: 638 $
*  $Date: 2009-02-07 14:17:29 -0700 (Sat, 07 Feb 2009) $
*  $Author: brian@tannerpages.com $
*  $HeadURL: http://rl-glue-ext.googlecode.com/svn/trunk/projects/codecs/Java/src/org/rlcommunity/rlglue/codec/taskspec/TaskSpecV3.java $
* 
*/

package org.rlcommunity.rlglue.codec.taskspec;
import java.util.StringTokenizer;

/**
 * The newest version of the Task Spec (May 15th 2008). With the release of
 * version 3, the framework of the Task Spec (in Java atleast) was overhauled.
 * The capability of adding more versions of the Task Spec without affecting
 * old versions was addded. @see rlglue.utilities.TaskSpecDelegate for more 
 * info. TaskSpecV3 now has the capability of appending a string of extra data
 * onto the end of the task spec.
 * 
 * @author mradkie
 */
class TaskSpecV3 extends TaskSpecDelegate {

    /**
     * Task Spec version. Should be 3.
     */
    private double version = 3;
    /**
     * Stores whether the environment is episodic or continuous.
     */
    private char episodic;
    /**
     * Total number of observations.
     */
    private int obs_dim;
    /**
     * Number of discrete observations.
     */
    private int num_discrete_obs_dims;
    /**
     * Number of continous observations.
     */
    private int num_continuous_obs_dims;
    /**
     * Array of types for the observations.
     */
    private char[] obs_types;
    /**
     * Array of the minimum value for the observations. (One min per observation)
     */
    private double[] obs_mins;
    /**
     * Array of the maximum value for the observations. (One max per observation)
     */
    private double[] obs_maxs;
    /**
     * Total number of actions
     */
    private int action_dim;
    /**
     * Number of discrete actions
     */
    private int num_discrete_action_dims;
    /**
     * Number of continous actions
     */
    private int num_continuous_action_dims;
    /**
     * Array of types for the actions
     */
    private char[] action_types;
    /**
     * Array of the minimum value for the actions. (One min per action)
     */
    private double[] action_mins;
    /**
     * Array of the maximum value for the actions. (One max per action)
     */
    private double[] action_maxs;
    /**
     * Maximum value for the reward.
     */
    private double reward_max;
    /**
     * Minimum value for the reward.
     */
    private double reward_min;
    /**
     * String of extra data to be appended onto the end of the Task Spec.
     */
    private String extraString;
    /**
     * Version of the parser used for this Task Spec.
     */
    static final int parser_version = 3;
    
    public TaskSpecV3(TaskSpecV2 oldTaskSpec){
        this.episodic=oldTaskSpec.getEpisodic();
        this.obs_dim=oldTaskSpec.getObsDim();
        this.action_dim=oldTaskSpec.getActionDim();
        this.num_continuous_action_dims=oldTaskSpec.getNumContinuousActionDims();
        this.num_continuous_obs_dims=oldTaskSpec.getNumContinuousObsDims();
        this.num_discrete_action_dims=oldTaskSpec.getNumDiscreteActionDims();
        this.num_discrete_obs_dims=oldTaskSpec.getNumDiscreteObsDims();
        this.obs_types=oldTaskSpec.getObsTypes();
        this.obs_mins=oldTaskSpec.getObsMins();
        this.obs_maxs=oldTaskSpec.getObsMaxs();
        this.action_types=oldTaskSpec.getActionTypes();
        this.action_mins=oldTaskSpec.getActionMins();
        this.action_maxs=oldTaskSpec.getActionMaxs();
        this.reward_max=oldTaskSpec.getRewardMax();
        this.reward_min=oldTaskSpec.getRewardMin();
        extraString="";
    }

    /**
     * The constructor for version 3 of the Task Spec taks a string as a 
     * parameter. This string is then parsed out, and the information from this
     * string, such as number of actions or observations can be accessed. The
     * format of the string should follow the conventions of the Task Spec 
     * language. Please refer to {@link rlglue.utilities.TaskSpec Task Spec}
     * for more information.
     * <p>
     * Version 3 of the Task Spec added the capability of appending a string of
     * extra data onto the end of the Task Spec.
     * 
     * @param taskSpecString String format of a Task Spec to be parsed into an
     * object.
     */
    public TaskSpecV3(String taskSpecString) {
        /* Break the task spec into its six component parts
         * The version number
         * The task style (episodic/continuous)
         * The observation data
         * The action data
         * The reward data (if version >= 2)
         * The extra data (if version >=3)
         */
        StringTokenizer tokenizer = new StringTokenizer(taskSpecString, ":");

        int numberOfTokens = tokenizer.countTokens();
        if (numberOfTokens < 6) {
            throw new IllegalArgumentException("TaskSpecV3 shouldn't parse task specs with less than 6 sections");
        }

        String versionString = this.removeWhiteSpace(tokenizer.nextToken());
        String taskStyle = this.removeWhiteSpace(tokenizer.nextToken());
        String observationString = this.removeWhiteSpace(tokenizer.nextToken());
        String actionString = this.removeWhiteSpace(tokenizer.nextToken());
        String rewardString;
        extraString = new String("");
        version = Double.parseDouble(versionString);

        // pull off the reward
        if (tokenizer.hasMoreTokens()) {
            rewardString = this.removeWhiteSpace(tokenizer.nextToken());
        } else {
            rewardString = "[]";
        }

        String thetoken = "";
        while (tokenizer.hasMoreTokens()) {
            thetoken = tokenizer.nextToken();
            extraString += thetoken;
        }

        episodic = taskStyle.charAt(0);
        // check to make sure this is a valid task type
        if (episodic != 'e' && episodic != 'c') {
            System.err.println("Invalid task type. Specify episodic (e) or continuous (c)");
           System.exit(1);
        }

        try {
            parseObservations(observationString);
            parseActions(actionString);
            parseRewards(rewardString);
            constraintCheck();
        } catch (Exception e) {
            System.err.println("Error parsing the Task Spec");
            System.err.println("Task Spec was: " + taskSpecString);
            System.err.println("Exception was: " + e);
            e.printStackTrace();
        }
    }

    TaskSpecV3() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Parses out the observation information from the string parameter and
     * stores it. Number of observations and observation types are parsed.
     * 
     * @param obsTypesString Observation portion of the Task Spec.
     * @throws java.lang.Exception
     * @return none
     */
    protected void parseObservationTypesAndDimensions(String obsTypesString) throws Exception {
        // Discard the [ ] around the types string
        obsTypesString = obsTypesString.substring(1, obsTypesString.length() - 1);

        // Split up the observation types
        StringTokenizer obsTypesTokenizer = new StringTokenizer(obsTypesString, ",");

        /* Parse the data out of obsTypesString.
         * Allocate and fill the obs_types array, and set the number 
         * of discrete and continuous observation dimensions.
         */
        this.obs_types = new char[obsTypesTokenizer.countTokens()];
        this.num_discrete_obs_dims = 0;
        this.num_continuous_obs_dims = 0;

        /* We get the observation type from the tokenizer, 
         * add it to the obs_types array, and update the discrete and continuous dimensions
         */
        int currentObservationTypeIndex = 0;
        while (obsTypesTokenizer.hasMoreTokens()) {
            char obsType = obsTypesTokenizer.nextToken().charAt(0);
            this.obs_types[currentObservationTypeIndex] = obsType;
            switch (obsType) {
                case 'i':
                    this.num_discrete_obs_dims += 1;
                    break;

                case 'f':
                    this.num_continuous_obs_dims += 1;
                    break;

                default:
                    throw new Exception("Unknown Observation Type: " + obsType);
            }
            currentObservationTypeIndex += 1;
        }
    }

    /**
     * Parses the ranges for the observations, storing the minimum values in one
     * array and the max values in a second array.
     * 
     * @param observationTokenizer Tokenizer on the observation string, 
     * tokenizing on the '_'.
     * @return none 
     */
    protected void parseObservationRanges(StringTokenizer observationTokenizer) {
        // Now we can allocate our obs mins and obs maxs arrays
        this.obs_mins = new double[this.obs_types.length];
        this.obs_maxs = new double[this.obs_types.length];
        int currentRange = 0;
        while (observationTokenizer.hasMoreTokens()) {
            String observationRange = observationTokenizer.nextToken();
            if (this.rangeKnown(observationRange)) {
                //observationRange = observationRange.substring(1, observationRange.length() - 1);
                StringTokenizer rangeTokenizer = new StringTokenizer(observationRange, ",");
                this.obs_mins[currentRange] = this.validValue(rangeTokenizer.nextToken());
                this.obs_maxs[currentRange] = this.validValue(rangeTokenizer.nextToken());
            } else {
                this.obs_mins[currentRange] = Double.NaN;
                this.obs_maxs[currentRange] = Double.NaN;
            }
            currentRange += 1;
        }
    }

    /**
     * Parses out the action information from the string parameter and
     * stores it. Number of actions and action types are parsed.
     * 
     * @param obsTypesString Action portion of the Task Spec.
     * @throws java.lang.Exception
     * @return none
     */
    protected void parseActionTypesAndDimensions(String actionTypesString) throws Exception {
        // Discard the [ ] around the types string
        actionTypesString = actionTypesString.substring(1, actionTypesString.length() - 1);

        // Split up the observation types
        StringTokenizer actionTypesTokenizer = new StringTokenizer(actionTypesString, ",");

        /* Parse the data out of obsTypesString.
         * Allocate and fill the obs_types array, and set the number 
         * of discrete and continuous observation dimensions.
         */
        this.action_types = new char[actionTypesTokenizer.countTokens()];
        this.num_discrete_action_dims = 0;
        this.num_continuous_action_dims = 0;

        /* We get the observation type from the tokenizer, 
         * add it to the obs_types array, and update the discrete and continuous dimensions
         */
        int currentActionTypeIndex = 0;
        while (actionTypesTokenizer.hasMoreTokens()) {
            char actionType = actionTypesTokenizer.nextToken().charAt(0);
            this.action_types[currentActionTypeIndex] = actionType;
            switch (actionType) {
                case 'i':
                    this.num_discrete_action_dims += 1;
                    break;

                case 'f':
                    this.num_continuous_action_dims += 1;
                    break;

                default:
                    throw new Exception("Unknown Action Type: " + actionType);
            }
            currentActionTypeIndex += 1;
        }
    }

     /**
     * Parses the ranges for the actions, storing the minimum values in one
     * array and the max values in a second array.
     * 
     * @param observationTokenizer Tokenizer on the action string, 
     * tokenizing on the '_'.
     * @return none 
     */
    protected void parseActionRanges(StringTokenizer actionTokenizer) {
        // Now we can allocate our obs mins and obs maxs arrays
        this.action_mins = new double[this.action_types.length];
        this.action_maxs = new double[this.action_types.length];
        int currentRange = 0;
        while (actionTokenizer.hasMoreTokens()) {
            String actionRange = actionTokenizer.nextToken();
            if (this.rangeKnown(actionRange)) {
                //actionRange = actionRange.substring(1, actionRange.length() - 1);
                StringTokenizer rangeTokenizer = new StringTokenizer(actionRange, ",");
                this.action_mins[currentRange] = this.validValue(rangeTokenizer.nextToken());
                //System.err.print(rangeTokenizer.nextToken() + "\n");
                this.action_maxs[currentRange] = this.validValue(rangeTokenizer.nextToken());
            } else {
                this.action_mins[currentRange] = Double.NaN;
                this.action_maxs[currentRange] = Double.NaN;
            }
            currentRange += 1;
        }
    }

    /**
     * Parses all information out of the observation portion of the Task Spec.
     * Observation string is passed in, the number of observations, the 
     * observation types and ranges are all parsed out of this string and stored
     * within the respective variables.
     * 
     * @param observationString Observation portion of the Task Spec string
     * @throws java.lang.Exception
     * @return none
     */
    protected void parseObservations(String observationString) throws Exception {
        /* Break the observation into its three component parts
         * The number of dimensions to the observation
         * The types of the observation
         * The ranges of the observations
         */
        StringTokenizer observationTokenizer = new StringTokenizer(observationString, "_");
        String obsDimensionString = observationTokenizer.nextToken();
        String obsTypesString = observationTokenizer.nextToken();

        this.obs_dim = Integer.parseInt(obsDimensionString);
        parseObservationTypesAndDimensions(obsTypesString);
        parseObservationRanges(observationTokenizer);
    }
    
    /**
     * Parses all information out of the actions portion of the Task Spec.
     * Action string is passed in, the number of actions, the 
     * action types and ranges are all parsed out of this string and stored
     * within the respective variables.
     * 
     * @param observationString Action portion of the Task Spec string
     * @throws java.lang.Exception
     * @return none
     */  
    protected void parseActions(String actionString) throws Exception {
        StringTokenizer actionTokenizer = new StringTokenizer(actionString, "_");
        String actionDimensionString = actionTokenizer.nextToken();
        String actionTypesString = actionTokenizer.nextToken();

        this.action_dim = Integer.parseInt(actionDimensionString);
        parseActionTypesAndDimensions(actionTypesString);
        parseActionRanges(actionTokenizer);
    }
    /**
     * Parses all information out of the reward portion of the Task Spec.
     * Reward string is passed in, the min and max reward is stored.
     * 
     * @param observationString Reward portion of the Task Spec string
     * @throws java.lang.Exception
     * @return none
     */
    protected void parseRewards(String rewardString) throws Exception {
        //if both min and max rewards are defined
        if (this.rangeKnown(rewardString)) {
            //rewardString = rewardString.substring(1, rewardString.length()-1);
            StringTokenizer rewardTokenizer = new StringTokenizer(rewardString, ",");
            this.reward_min = this.validValue(rewardTokenizer.nextToken());
            this.reward_max = this.validValue(rewardTokenizer.nextToken());
        } else {
            this.reward_min = Double.NaN;
            this.reward_max = Double.NaN;
        }
    }

    /**
     * Parses a double out of a string.  This method acts like an overloaded
     * Double.parseDouble(String) method, conventions for -infinity and infinity
     * were added.
     * 
     * @param valueString String to parse the double out of.
     * @return The double parsed out of the strng.
     */
    protected double validValue(String valueString) {
        if (valueString.equalsIgnoreCase("[-inf")) {
            return Double.NEGATIVE_INFINITY;
        } else if (valueString.equalsIgnoreCase("inf]")) {
            return Double.POSITIVE_INFINITY;
        } else if (valueString.equals("[")) {
            return Double.NaN;
        } else if (valueString.equals("]")) {
            return Double.NaN;
        } else {
            if (valueString.charAt(0) == '[') {
                valueString = valueString.substring(1);
            } else if (valueString.charAt(valueString.length() - 1) == ']') {
                if (valueString.length() == 1) {
                    return Double.NaN;
                }
                valueString = valueString.substring(0, valueString.length() - 1);
            }
            return Double.parseDouble(valueString);
        }
    }

    /**
     * Checks if the range of a given parameter is known. Observations, actions
     * and rewards all follow the convention [min,max]. If the min and max are
     * not specified, the range is unknown.
     * 
     * @param valueRange String of the form "[min,max]" where min and max may
     * not be specified ("[,] or []").
     * @return True if range is known, false otherwise.
     */
    protected boolean rangeKnown(String valueRange) {
        if (valueRange.equals("[,]")) {
            return false;
        } else if (valueRange.equals("[]")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Removes spaces from a given string.
     * 
     * @param input String to remove spaces from.
     * @return Input string after spaces are removed.
     */
    protected String removeWhiteSpace(String input) {
        StringTokenizer whiteTokens = new StringTokenizer(input, " ");
        String output = whiteTokens.nextToken();
        while (whiteTokens.hasMoreTokens()) {
            output += whiteTokens.nextToken();
        }
        return output;
    }

    /**
     * Checks to make sure that: observation mins < observation maxs;
     * action mins < action maxs and reward mins < reward maxs.
     * 
     * @throws java.lang.Exception Exception thrown if one of these conditions
     * is not met.
     */
    protected void constraintCheck() throws Exception {
        for (int i = 0; i < this.obs_dim; i++) {
            if (this.obs_mins[i] > this.obs_maxs[i]) {
                throw new Exception("Observation min>max at index: " + i);
            }
        }
        for (int i = 0; i < this.action_dim; i++) {
            if (this.action_mins[i] > this.action_maxs[i]) {
                throw new Exception("Action min>max at index: " + i);
            }
        }
        if (this.reward_min > this.reward_max) {
            throw new Exception("Reward min>max: " + this.reward_min);
        }
    }
    /**
     * @see rlglue.utilities.TaskSpec#isObsMinNegInfinity(int index)
     */
    public boolean isObsMinNegInfinity(int index) {
        return (this.obs_mins[index] == Double.NEGATIVE_INFINITY);
    }

    /**
     * @see rlglue.utilities.TaskSpec#isActionMinNegInfinity(int index)
     */
    public boolean isActionMinNegInfinity(int index) {
        return (this.action_mins[index] == Double.NEGATIVE_INFINITY);
    }
    /**
     * @see rlglue.utilities.TaskSpec#isObsMaxPosInfinity(int index)
     */
    public boolean isObsMaxPosInfinity(int index) {
        return (this.obs_maxs[index] == Double.POSITIVE_INFINITY);
    }
    /**
     * @see rlglue.utilities.TaskSpec#isActionMaxPosInfinity(int index)
     */
    public boolean isActionMaxPosInfinity(int index) {
        return (this.action_maxs[index] == Double.POSITIVE_INFINITY);
    }
    /**
     * @see rlglue.utilities.TaskSpec#isObsMinUnknown(int index)
     */
    public boolean isObsMinUnknown(int index) {
        return new Double(obs_mins[index]).isNaN();
    }
    /**
     * @see rlglue.utilities.TaskSpec#isObsMaxUnknown(int index)
     */
    public boolean isObsMaxUnknown(int index) {
        return new Double(obs_maxs[index]).isNaN();
    }
    /**
     * @see rlglue.utilities.TaskSpec#isActionMinUnknown(int index)
     */
    public boolean isActionMinUnknown(int index) {
        return new Double(action_mins[index]).isNaN();
    }
    /**
     * @see rlglue.utilities.TaskSpec#isActionMaxUnknown(int index)
     */
    public boolean isActionMaxUnknown(int index) {
        return new Double(action_maxs[index]).isNaN();
    }
    /**
     * @see rlglue.utilities.TaskSpec#isMinRewardNegInf()
     */
    public boolean isMinRewardNegInf() {
        return new Double(reward_min).isInfinite();

    }
    /**
     * @see rlglue.utilities.TaskSpec#isMaxRewardInf()
     */

    public boolean isMaxRewardInf() {
        return new Double(reward_max).isInfinite();

    }

    /**
     * @see rlglue.utilities.TaskSpec#isMinRewardUnknown()
     */
    public boolean isMinRewardUnknown() {
        return new Double(reward_min).isNaN();

    }

    /**
     * @see rlglue.utilities.TaskSpec#isMaxRewardUnknown()
     */
    public boolean isMaxRewardUnknown() {
        return new Double(reward_max).isNaN();

    }

    /**
     * Builds the string representation of the Task Spec, which follows the 
     * Task Spec language.
     * 
     * @param none
     * @return String representation of the Task Spec
     */
    public String getStringRepresentation() {
        //2:e:2_[f,f]_[-1.2,0.6]_[-0.07,0.07]:1_[i]_[0,2]:[0,3]:extrastringhere
        String taskSpec = "";
        taskSpec += (int) this.version + ":";
        taskSpec += this.episodic + ":";
        //add the observations
        taskSpec += buildObsString();
        //add the actions
        taskSpec += buildActionString();
        //add the reward
        taskSpec += "[" + this.reward_min + "," + this.reward_max + "]";
        //add the extra string
        taskSpec += ":" + this.extraString;

        return taskSpec;

    }

    /**
     * Builds the action portion of the Task Spec string from the information
     * stored within this Task Spec object.
     * 
     * @param none
     * @return String representation of the action information.
     */
    private String buildActionString() {
        String actionsString = "";
        int numactions = num_continuous_action_dims + num_discrete_action_dims;
        actionsString += (numactions) + "_[";

        for (int i = 0; i < numactions; i++) {
            actionsString += action_types[i] + ",";
        }
        actionsString = actionsString.substring(0, actionsString.length() - 1);//pull off extra ,
        actionsString += "]";
        for (int i = 0; i < numactions; i++) {
            actionsString += "_[" + action_mins[i] + "," + action_maxs[i] + "]";
        }
        return actionsString + ":";
    }

    /**
     * Builds the observation portion of the Task Spec string from the information
     * stored within this Task Spec object.
     * 
     * @param none
     * @return String representation of the observation information.
     */
    private String buildObsString() {
        String obsString = "";
        int numObs = num_continuous_obs_dims + num_discrete_obs_dims;
        obsString += (numObs) + "_[";

        int contIndex = 0;
        int descIndex = 0;
        for (int i = 0; i < numObs; i++) {
            obsString += obs_types[i] + ",";
        }
        obsString = obsString.substring(0, obsString.length() - 1);//pull off extra ,
        obsString += "]";
        for (int i = 0; i < numObs; i++) {
            obsString += "_[" + obs_mins[i] + "," + obs_maxs[i] + "]";
        }
        return obsString + ":";
    }

    /**
     * Builds a debug string of all the information stored within this Task Spec.
     * Instead of printing this debug info to the screen, it is returned so the
     * implementer can use it.
     * 
     * @param none
     * @return String full of debug information about the Task Spec object.
     */
    public String dump() {
        String obs_types_string = "";
        for (int i = 0; i < obs_types.length; ++i) {
            obs_types_string += obs_types[i] + " ";
        }

        String obs_mins_string = "";
        for (int i = 0; i < obs_mins.length; ++i) {
            obs_mins_string += obs_mins[i] + " ";
        }

        String obs_maxs_string = "";
        for (int i = 0; i < obs_maxs.length; ++i) {
            obs_maxs_string += obs_maxs[i] + " ";
        }

        String action_types_string = "";
        for (int i = 0; i < action_types.length; ++i) {
            action_types_string += action_types[i] + " ";
        }

        String action_mins_string = "";
        for (int i = 0; i < action_mins.length; ++i) {
            action_mins_string += action_mins[i] + " ";
        }

        String action_maxs_string = "";
        for (int i = 0; i < action_maxs.length; ++i) {
            action_maxs_string += action_maxs[i] + " ";
        }


        String taskSpecObject = "version: " + version + "\n" +
                "episodic: " + episodic + "\n" +
                "obs_dim: " + obs_dim + "\n" +
                "num_discrete_obs_dims: " + num_discrete_obs_dims + "\n" +
                "num_continuous_obs_dims: " + num_continuous_obs_dims + "\n" +
                "obs_types: " + obs_types_string + "\n" +
                "obs_mins: " + obs_mins_string + "\n" +
                "obs_maxs: " + obs_maxs_string + "\n" +
                "action_dim: " + action_dim + "\n" +
                "num_discrete_action_dims: " + num_discrete_action_dims + "\n" +
                "num_continuous_action_dims: " + num_continuous_action_dims + "\n" +
                "action_types: " + action_types_string + "\n" +
                "action_mins: " + action_mins_string + "\n" +
                "action_maxs: " + action_maxs_string + "\n" +
                "reward_min: " + this.reward_min + "\n" +
                "reward_max: " + this.reward_max;

        return taskSpecObject;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getVersion()
     */
    public double getVersion() {
        return this.version;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setVersion(int version)
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getEpisodic()
     */
    public char getEpisodic() {
        return this.episodic;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setEpisodic(char episodic)
     */
    public void setEpisodic(char episodic) {
        this.episodic = episodic;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getObsDim()
     */
    public int getObsDim() {
        return this.obs_dim;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setObsDim(int dim)
     */
    public void setObsDim(int dim) {
        this.obs_dim = dim;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getNumDiscreteObsDims()
     */
    public int getNumDiscreteObsDims() {
        return this.num_discrete_obs_dims;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setNumDiscreteObsDims(int numDisc)
     */
    public void setNumDiscreteObsDims(int numDisc) {
        this.num_discrete_obs_dims = numDisc;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getNumContinuousObsDims()
     */
    public int getNumContinuousObsDims() {
        return this.num_continuous_obs_dims;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setNumContinuousObsDims(int numCont)
     */
    public void setNumContinuousObsDims(int numCont) {
        this.num_continuous_obs_dims = numCont;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getObsTypes()
     */
    public char[] getObsTypes() {
        return this.obs_types;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setObsTypes(char[] types)
     */
    public void setObsTypes(char[] types) {
        this.obs_types = types.clone();
    }

    /**
     * @see rlglue.utilities.TaskSpec#getObsMins()
     */
    public double[] getObsMins() {
        return this.obs_mins;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setObsMins(double[] mins)
     */
    public void setObsMins(double[] mins) {
        this.obs_mins = mins.clone();
    }

    /**
     * @see rlglue.utilities.TaskSpec#getObsMaxs()
     */
    public double[] getObsMaxs() {
        return this.obs_maxs;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setObsMaxs(double[] maxs)
     */
    public void setObsMaxs(double[] maxs) {
        this.obs_maxs = maxs.clone();
    }
    
    /**
     * @see rlglue.utilities.TaskSpec#getActionDim()
     */
    public int getActionDim() {
        return this.action_dim;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setActionDim(int dim)
     */
    public void setActionDim(int dim) {
        this.action_dim = dim;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getNumDiscreteActionDims()
     */
    public int getNumDiscreteActionDims() {
        return this.num_discrete_action_dims;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setNumDiscreteActionDims(int numDisc)
     */
    public void setNumDiscreteActionDims(int numDisc) {
        this.num_discrete_action_dims = numDisc;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getNumContinuousActionDims()
     */
    public int getNumContinuousActionDims() {
        return this.num_continuous_action_dims;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setNumContinuousActionDims(int numCont)
     */
    public void setNumContinuousActionDims(int numCont) {
        this.num_continuous_action_dims = numCont;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getActionTypes()
     */
    public char[] getActionTypes() {
        return this.action_types;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setActionTypes(char[] types)
     */
    public void setActionTypes(char[] types) {
        this.action_types = types.clone();
    }

    /**
     * @see rlglue.utilities.TaskSpec#getActionMins()
     */
    public double[] getActionMins() {
        return this.action_mins;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setActionMins(double[] mins)
     */
    public void setActionMins(double[] mins) {
        this.action_mins = mins.clone();
    }

    /**
     * @see rlglue.utilities.TaskSpec#getActionMaxs()
     */
    public double[] getActionMaxs() {
        return this.action_maxs;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setActionMaxs(double[] maxs)
     */
    public void setActionMaxs(double[] maxs) {
        this.action_maxs = maxs.clone();
    }
    /**
     * @see rlglue.utilities.TaskSpec#getRewardMax()
     */
    public double getRewardMax() {
        return this.reward_max;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setRewardMax(double max)
     */
    public void setRewardMax(double max) {
        this.reward_max = max;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getRewardMin()
     */
    public double getRewardMin() {
        return this.reward_min;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setRewardMin(double min)
     */
    public void setRewardMin(double min) {
        this.reward_min = min;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getExtraString()
     */
    public String getExtraString() {
        return this.extraString;
    }

    /**
     * @see rlglue.utilities.TaskSpec#setExtraString(String newString)
     */
    public void setExtraString(String newString) {
        this.extraString = newString;
    }

    /**
     * @see rlglue.utilities.TaskSpec#getParserVersion()
     */
    public int getParserVersion() {
        return this.parser_version;
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
