static double distLinePoint(P v, P w, P p) {
double l = dist2(v, w);
if(l == 0) return dist(p, v);
double t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l;
if(t < 0) return dist(p, v);

