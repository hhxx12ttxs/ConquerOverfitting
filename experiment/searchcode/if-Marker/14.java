Marker ret = markerset.createMarker(markerId, this.label, this.world, this.x, this.y, this.z, getMarkerIcon(markerApi, this.iconName), false // not persistent
);

if (ret == null) {
// UPDATE
// -------------------------------------------- //

public void update(MarkerAPI markerApi, Marker marker) {
if (!this.world.equals(marker.getWorld()) || this.x != marker.getX() || this.y != marker.getY() || this.z != marker.getZ()) {

