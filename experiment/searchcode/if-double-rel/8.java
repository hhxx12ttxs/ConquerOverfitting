double relPosX = target.x - origin.x;
double relPosY = target.y - origin.y;
double projectedDistance = origin.distance(target);
double heading;
if (relPosY >= 0) {
heading = Math.asin(relPosX / projectedDistance);

