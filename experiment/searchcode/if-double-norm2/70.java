public static double[] normalizeL2(double[] vector) {
// compute vector 2-norm
double norm2 = 0;
for (int i = 0; i < vector.length; i++) {
norm2 += vector[i] * vector[i];
}
norm2 = (double) Math.sqrt(norm2);

if (norm2 == 0) {

