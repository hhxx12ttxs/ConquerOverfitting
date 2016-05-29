for (int i = 0; i <= result.degree; i++) {
if (i <= p1.degree) {
if (i <= p2.degree) {
result.coeff[i].updateCoeff(p1.coeff[i].getCoeff() + p2.coeff[i].getCoeff());

