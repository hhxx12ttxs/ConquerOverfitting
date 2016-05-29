Plot plot = chart.getPlot();
if (plot instanceof CategoryPlot) {
CategoryPlot categoryPlot = (CategoryPlot)plot;
CategoryAxis categoryAxis = categoryPlot.getDomainAxis();

if (useIntegerTickUnits) {
valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

