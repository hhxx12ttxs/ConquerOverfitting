int distance = Coordinate.computeDistance(this.XY, drone.getCoordinates());
if (distance < selectedDistance){
if (!drone.isAllocated()){
int distance = Coordinate.computeDistance(this.XY, drone.getCoordinates());
if (distance < selectedDistance){
if (drone.isAllocated() &amp;&amp; drone.getAllocatedZone() == this){

