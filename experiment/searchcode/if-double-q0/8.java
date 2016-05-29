double q2 = Math.exp((-Ed2) / (k*T));

if (Double.isInfinite(q0)) {
return 1e100;
}

/* Calculating quartic polynomial coefficients */
root *= q0;

if (root <= 0) root = 1e5;
}
else root *= q0;

/* Input of some debug values */

double Nd1_c = Nd1*root / (root + q0*q1);

