public double errorVariance(int pos) {
if (H == null) {
return 0;
}
int i = pos % nvars;
return H.get(i, i);
}
int j = pos / nvars;
if (j != hpos) {
measurements.H(j, H);

