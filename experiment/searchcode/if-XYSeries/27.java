dataset.addSeries(cusumXYSeries);

updateDataset(dataset);
} else if (mode == SIMULATED_MODE) {
XYSeriesCollection dataset = new XYSeriesCollection();
dataset.addSeries(cusumXYSeries);

if (SOEASYController.getInstance().isSimulationRunning()) {

