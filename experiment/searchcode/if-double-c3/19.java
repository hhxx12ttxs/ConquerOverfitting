double disc = b * b - 4 * a * c;
if (disc < 0)
return null;
disc = Math.sqrt(disc);
double q = ((b < 0) ? -0.5 * (b - disc) : -0.5 * (b + disc));
double c3 = d * inva;
double c4 = e * inva;
double c12 = c1 * c1;
double p = -0.375 * c12 + c2;

