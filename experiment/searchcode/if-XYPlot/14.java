//jfreechart.getLegend().setItemFont(new Font(&quot;SimSun&quot;, Font.ROMAN_BASELINE, 10));
XYPlot xyplot = (XYPlot) jfreechart.getPlot(); // 获得 plot：XYPlot！
if(xyplot.getRangeAxis().getUpperBound()<=1)  xyplot.getRangeAxis().setUpperBound(1.0);
else if (xyplot.getRangeAxis().getUpperBound()<=10) xyplot.getRangeAxis().setUpperBound(10.0);

