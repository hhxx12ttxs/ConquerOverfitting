double r2) {

int posX = -1;
int posY = -1;

if (b1.x() == b2.x()) {
double m = (double) Math.abs(b1.y() - b2.y());
posY = b1.y() - (int) yDist;
}

} else if (b1.y() == b2.y()) {
double m = (double) Math.abs(b1.x() - b2.x());
double xDist = (r1 * r1 - r2 * r2 + m * m) / (2 * m);

