public abstract PlotId getPlotId(PlotArea plotworld, int x, int y, int z);

// If you have a circular plot, just return the corner if it were a square
public abstract Location getPlotTopLocAbs(PlotArea plotworld, PlotId plotid);

/*
* Plot clearing (return false if you do not support some method)
*/
public abstract boolean clearPlot(PlotArea plotworld, Plot plot, Runnable whenDone);

