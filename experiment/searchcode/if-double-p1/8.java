package perp;

class Line {
Point p1, p2;
double dirX, dirY;

Line(Point p1, Point p2) {
dirY = p2.y - p1.y;
}

double slope() {
if (dirX == 0) return Double.NaN;
return dirY / dirX;
}
}

