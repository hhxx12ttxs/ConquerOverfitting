// intersection of p0-p1 and p2-p3.
static P intersect(P p0, P p1, P p2, P p3) {
double a_x, a_y, b_x, b_y, r, s, t;
a_x = p1.x - p0.x;
a_y = p1.y - p0.y;
b_x = p3.x - p2.x;
b_y = p3.y - p2.y;

