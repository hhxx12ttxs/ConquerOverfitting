public BreakProb getVar() {
return var;
}

public BreakProb calcDirichletProb() {
double logSum = 0;
if (var.p1 == 0 || var.p4 == 0) {
logSum += Math.log(mean.p4 * (1 - mean.p4) / var.p4 - 1);
logSum /= 2;
double sum = Math.exp(logSum);

