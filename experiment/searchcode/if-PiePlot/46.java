public void populateBean(Plot plot)
{
super.populateBean(plot);
if(plot instanceof PiePlot)
stylePane.populateBean(Integer.valueOf(pieplot.getPlotStyle()));
isSecondPlot.setSelected(false);
if(pieplot.getSubType() == 2)

