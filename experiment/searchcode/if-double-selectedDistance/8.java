Double selectedDistance = Double.MAX_VALUE;
Double curDistance;

if (regions.isEmpty()) {
curDistance = r.getPosition().distance(point2D);
if (Double.compare(curDistance, selectedDistance) < 0) {

