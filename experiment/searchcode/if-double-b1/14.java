this.f = x;
}

void setP(Point x) {
this.p = x;
}

static double dist(Body b1, Body b2) {
return Math.sqrt((b1.p.x - b2.p.x) * (b1.p.x - b2.p.x)
double rz = b1.p.z - b2.p.z;

double k = 1;
double dist = dist(b1, b2);
double eps = 0.1;
if (dist < eps) {

