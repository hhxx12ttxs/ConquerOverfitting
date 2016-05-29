package ekf;

public class PointDouble {

private double x;
private double y;

public PointDouble(double x, double y) {
public void setY(double y) {
this.y = y;
}

public double computeDistanceTo(PointDouble other) {
if (other == null)

