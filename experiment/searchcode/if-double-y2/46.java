@Override
public double distance(DTWModel other) {
if (!(other instanceof Vector3DDTWModel)) {
Vector3DDTWModel o2 = (Vector3DDTWModel) other;
double x2 = o2.x;
double y2 = o2.y;
double z2 = o2.z;

return Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2) + (z - z2) * (z - z2));

