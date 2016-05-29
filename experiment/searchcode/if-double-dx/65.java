public int countOverlaps(double x, double y, double z) {
if (cnt == 0) return 0;
double dx = x-xs[0];
double dy = y-ys[0];
double dz = z-zs[0];
if (dx >  L/2) x -= L;
if (dx < -L/2) x += L;
if (dy >  L/2) y -= L;
if (dy < -L/2) y += L;

