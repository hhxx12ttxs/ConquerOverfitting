import com.androidplot.xy.XYPlot;
import com.lab4u.sensors.persistence.FilePersistSensorInfo;
import com.lab4u.sensors.persistence.ILab4uSensorPersistence;
public void setZ(Lab4uSimpleXYSeries z) {
this.z = z;
}

public void verifiedSizeToPlot() {
// get rid the oldest sample in history:
if (x.size() > SensorListViewItemModel.HISTORY_SIZE) {

