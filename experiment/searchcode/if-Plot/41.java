public static AbstractPlotConnection getConnection(GuiPlotMode plotMode, IPlottingSystem plottingSystem) {

AbstractPlotConnection plotUI=null;
if (plotMode.equals(GuiPlotMode.ONED)) {
plottingSystem.setPlotType(PlotType.XY);
plotUI = new Plotting1DUI(plottingSystem);
} else if (plotMode.equals(GuiPlotMode.ONED_THREED)) {

