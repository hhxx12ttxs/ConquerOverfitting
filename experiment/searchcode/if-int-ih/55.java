// Ported to ImageJ plugin by G.Landini from E Celebi&#39;s fourier_0.8 routines
int threshold;
int ih, it;
int first_bin;
int last_bin;
double term;
double[] P1 = new double[histogram.length]; /* cumulative normalized histogram */
double[] P2 = new double[histogram.length];

int total = 0;
for (ih = 0; ih < histogram.length; ih++)

