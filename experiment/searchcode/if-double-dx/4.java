public double distanceSquaredAabbPoint(double left, double right, double top, double bottom, double x, double y) {
double dx = 0.0;
double dy = 0.0;

if(x < left) {
dx = left - x;
} else if(x > right) {
dx = x - right;
}

if(y < top) {
dy = top - y;

