private double c;
private double alpha;

public LevyRNG(Random r, double c, double alpha) {
this.rng = r;
double u, v, t, s;


u = Math.PI * (rng.nextDouble()-0.5);

if(alpha == 1)                          //CAUCHY

