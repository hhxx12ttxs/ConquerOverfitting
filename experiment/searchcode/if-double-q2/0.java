double Q1 = h/6  * (f.value(a) + 4*f.value(c) + f.value(b));
double Q2 = h/12 * (f.value(a) + 4*f.value(d) + 2*f.value(c) + 4*f.value(e) + f.value(b));
if (Math.abs(Q2 - Q1) <= EPSILON) {
return Q2 + (Q2 - Q1) / 15;
} else {

