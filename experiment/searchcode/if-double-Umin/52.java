public static double exp_rand(Session context) {
double a = 0.;
double u = context.rng.unif_rand();    /* precaution if u = 0 is ever returned */
double ustar = context.rng.unif_rand(), umin = ustar;
do {
ustar = context.rng.unif_rand();
if (umin > ustar) {

