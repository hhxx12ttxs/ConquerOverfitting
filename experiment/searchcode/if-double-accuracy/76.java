private final GeoCoordinate geoCoordinate;
private final double accuracy;

public static GeoCoordinateWithAccuracy of(double latitude, double longitude, double accuracy) {
GeoCoordinateWithAccuracy that = (GeoCoordinateWithAccuracy)o;

if (Double.compare(that.accuracy, accuracy) != 0) {
return false;

