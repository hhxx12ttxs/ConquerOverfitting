package jetdrone.convert;

class V {
double x;
double y;
double z;

V(String[] args) {
if(args.length > 0) x = Double.valueOf(args[0]);
if(args.length > 2) z = Double.valueOf(args[2]);
}

static boolean eq(V v1, V v2) {
if(v1 == null &amp;&amp; v2 == null) return true;

