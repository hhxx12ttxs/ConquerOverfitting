double a = 0.;
double u = context.rng_unif_rand();    /* precaution if u = 0 is ever returned */
if (u <= q[0]) {
return a + u;
}

int i = 0;
double ustar = context.rng_unif_rand(), umin = ustar;

