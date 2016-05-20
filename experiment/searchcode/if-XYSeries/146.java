/*
 * Copyright 2012 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.androidplot.xy;

import android.graphics.*;
import com.androidplot.exception.PlotRenderException;
import com.androidplot.series.XYSeries;
import com.androidplot.util.ValPixConverter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Renders a point as a Bar
 */
public class BarRenderer extends XYSeriesRenderer<BarFormatter> {

    private BarWidthStyle style = BarWidthStyle.FIXED_WIDTH;
    private float barWidth = 5;

    public enum BarRenderStyle {
        STACKED,            // bars are overlaid in descending y-val order (largest val in back)
        SIDE_BY_SIDE        // bars are drawn next to each-other
    }

    public enum BarWidthStyle {
        FIXED_WIDTH,
        FIXED_SPACING
    }


    public BarRenderer(XYPlot plot) {
        super(plot);
    }

    /**
     * Sets the width of the bars draw.
     * @param barWidth
     */
    public void setBarWidth(float barWidth) {
        this.barWidth = barWidth;
    }

    /**
     * Synchronizes the current thread across multiple objects before
     * executing a given task.
     */
    /*private void multiSynch(Canvas canvas, RectF plotArea, List<XYSeries> sl, int depth) {
        if (sl != null) {
            synchronized (sl.get(depth)) {
                if (depth < sl.size()-1) {
                    multiSynch(canvas, plotArea, sl, ++depth);
                } else {
                    int longest = getLongestSeries(sl);
                    if(longest == 0) {
                        return;  // no data, nothing to do.
                    }
                    TreeMap<Number, XYSeries> seriesMap = new TreeMap<Number, XYSeries>();
                    for(int i = 0; i < longest; i++) {
                        seriesMap.clear();
                        List<XYSeries> seriesList = getPlot().getSeriesListForRenderer(this.getClass());
                        for(XYSeries series : seriesList) {
                            if(i < series.size()) {
                                seriesMap.put(series.getY(i), series);
                            }
                        }
                        drawBars(canvas, plotArea, seriesMap, i);
                    }
                }
            }
        }
    }*/

    @Override
    public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException {

        List<XYSeries> sl = getPlot().getSeriesListForRenderer(this.getClass());
        // need to synch on each series in sl before proceeding with render
        //multiSynch(canvas, plotArea, sl, 0);

        int longest = getLongestSeries(sl);
        if (longest == 0) {
            return;  // no data, nothing to do.
        }
        TreeMap<Number, XYSeries> seriesMap = new TreeMap<Number, XYSeries>();
        for (int i = 0; i < longest; i++) {
            seriesMap.clear();
            List<XYSeries> seriesList = getPlot().getSeriesListForRenderer(this.getClass());
            for (XYSeries series : seriesList) {
                if (i < series.size()) {
                    seriesMap.put(series.getY(i), series);
                }
            }
            drawBars(canvas, plotArea, seriesMap, i);
        }
    }

    @Override
    public void doDrawLegendIcon(Canvas canvas, RectF rect, BarFormatter formatter) {
        canvas.drawRect(rect, formatter.getFillPaint());
        canvas.drawRect(rect, formatter.getBorderPaint());
    }

    private int getLongestSeries(List<XYSeries> seriesList) {
        int longest = 0;

        for(XYSeries series : seriesList) {
            int seriesSize = series.size();
            if(seriesSize > longest) {
                longest = seriesSize;
            }
        }
        return longest;
    }


    private int getLongestSeries() {
        return getLongestSeries(getPlot().getSeriesListForRenderer(this.getClass()));
    }

    private void drawBars(Canvas canvas, RectF plotArea, TreeMap<Number, XYSeries> seriesMap, int x) {
        Paint p = new Paint();
        p.setColor(Color.RED);
        Object[] oa = seriesMap.entrySet().toArray();
        Map.Entry<Number, XYSeries> entry;
        for(int i = oa.length-1; i >= 0; i--) {
            entry = (Map.Entry<Number, XYSeries>) oa[i];
            BarFormatter formatter = getFormatter(entry.getValue()); // TODO: make this more efficient
            Number yVal = null;
            Number xVal = null;
            if(entry.getValue() != null) {
                yVal = entry.getValue().getY(x);
                xVal = entry.getValue().getX(x);
            }  
          
            if (yVal != null && xVal != null) {  // make sure there's a real value to draw
                switch (style) {
                    case FIXED_WIDTH:
                        float halfWidth = barWidth/2;
                        float pixX = ValPixConverter.valToPix(xVal.doubleValue(), getPlot().getCalculatedMinX().doubleValue(), getPlot().getCalculatedMaxX().doubleValue(), plotArea.width(), false) + (plotArea.left);
                        float pixY = ValPixConverter.valToPix(yVal.doubleValue(), getPlot().getCalculatedMinY().doubleValue(), getPlot().getCalculatedMaxY().doubleValue(), plotArea.height(), true) + plotArea.top;
                        canvas.drawRect(pixX - halfWidth, pixY, pixX + halfWidth, plotArea.bottom, formatter.getFillPaint());
                        canvas.drawRect(pixX - halfWidth, pixY, pixX + halfWidth, plotArea.bottom, formatter.getBorderPaint());
                        break;
                    default:
                        throw new UnsupportedOperationException("Not yet implemented.");
                }
            }
        }
    }
}

