Marker marker = markers.get(options);
if (marker == null) {
markers.put(options, googleMap.addMarker(options));
public void remove(MarkerOptions options) {
Marker marker = markers.remove(options);
if (marker != null) {
marker.remove();

