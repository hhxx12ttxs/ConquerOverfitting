JRPropertiesMap prop = jasperChart.getPropertiesMap();

if (plot instanceof CategoryPlot) {
//LineChart Y축의 값의 소수점표시 안돼도록 처리
ValueAxis axis = ((CategoryPlot)plot).getRangeAxis();
Range range = axis.getRange();
if (range.getLowerBound() < 0) {

