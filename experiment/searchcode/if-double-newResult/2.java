res += (coef.get(i, j) * result.get(i, 0));
}
if (Double.isInfinite(res) || Double.isNaN(res)
|| Math.abs(res - free.get(i, 0)) > eps) {
static public Matrix getResult(Matrix coef, Matrix free, Matrix result) {
int n = coef.getHeight();
double[][] newResult = new double[n][1];

