handle(event.getChart().getXYPlot(), event.getEntity());
}
}

private void handle(XYPlot plot, ChartEntity entity) {
if (entity instanceof XYItemEntity) {
private void highlight(XYPlot plot, XYItemEntity entity) {
if (plot instanceof CombinedDomainXYPlot) {
((CombinedDomainXYPlot) plot).getSubplots().forEach(o -> highlight((XYPlot) o, entity));

