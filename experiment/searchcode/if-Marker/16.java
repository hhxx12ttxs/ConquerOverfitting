public void updateMarker(MarkerSource source, boolean draggable, Context context) {

if (hashMap.containsValue(source)) {
Marker marker = getMarkerFromSource(source);
for (Marker marker : hashMap.keySet()) {
MarkerSource object = getSourceFromMarker(marker);
if (!list.contains(object)) {

