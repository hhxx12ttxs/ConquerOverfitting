super.populateBean(plot);
if(plot instanceof PiePlot) {
PiePlot piePlot = (PiePlot)plot;
stylePane.populateBean(piePlot.getPlotStyle());

isSecondPlot.setSelected(false);
if(piePlot.getSubType() == ChartConstants.PIE_PIE) {

