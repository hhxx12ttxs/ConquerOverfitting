import com.nutiteq.style.StyleSet;

public class GeometryStyle {

public int pointColor;
public int lineColor;
public int polygonColor;
public GeometryStyle(int minZoom) {
this.minZoom = minZoom;
}

public PointStyle toPointStyle() {
return PointStyle.builder().setSize(size).setPickingSize(pickingSize).setColor(pointColor).build();

