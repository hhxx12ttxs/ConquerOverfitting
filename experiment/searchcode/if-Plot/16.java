public void plot(Graphics2D g, BaseToPlotCoordinateMapper plot, boolean fill) {
g.setPaint(getColor());
if (fill) {
g.fillRect(plot.getPlotXCoordinate(getAStart()), plot.getPlotYCoordinate(getBStart()),
plot.getPlotXDistance(getAEnd(),getAStart()), plot.getPlotYDistance(getBEnd(),getBStart()));
}
if(getCaption() != null) {

