PlotCommandType commandType = command.getPlotCommandType();

if ( commandType == PlotCommandType.ADD_PLOT_LINE ) {
addPlotLine(
command.getPlotLineId()
);
}
else if ( commandType == PlotCommandType.ADD_PLOT_VALUE ) {

