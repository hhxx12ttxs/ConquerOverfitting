int a = (int)(num/epsilon);
return a * epsilon;
}

public double intersectY() {
if (p2.x == p1.x) {
return Double.MAX_VALUE;
} else {
return p1.y - slope()*p1.x;
}
}

public double slope() {
if (p2.x == p1.x) {

