for (int j = 0; j < n; j++) {
res += (coef.get(i, j) * result.get(i, 0));
}
if (Double.isInfinite(res) || Double.isNaN(res)
static public Matrix getResult(Matrix coef, Matrix free, Matrix result) {
int n = coef.getHeight();
double[][] newResult = new double[n][1];
for (int i = 0; i < n; i++) {

