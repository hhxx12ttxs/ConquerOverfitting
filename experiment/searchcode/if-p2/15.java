static int comp(double a) {
if (a < eps) return -1;
if (a > eps) return 1;
return 0;
}
static double sqr(double a) {
return new P2(a.x / b, a.y / b);
}
static boolean eq(P2 a, P2 b) {
if (comp(a.x - b.x) != 0) return false;
if (comp(a.y - b.y) != 0) return false;

