mCalibrationLevel = in.readFloat();
}

public void add(double x, double y) {
synchronized (mXYSeriesMovement) {
if (mNeedsClear) {
mXYSeriesMovement.clear();
mNeedsClear = false;
}

if (mXYSeriesMovement.getItemCount() >= SleepMonitoringService.MAX_POINTS_IN_A_GRAPH) {

