* @param timeToExpiry Time to expiry
* @param steps Number of steps
* @param isCall True if call, false if put
*/
public RelativeOutperformanceOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall) {
for (int i = 0; i < nStepsP; ++i) {
double priceTmp2 = assetPrice2;
for (int j = 0; j < nStepsP; ++j) {
values[i][j] = Math.max(sign * (priceTmp1 / priceTmp2 - strike), 0.);

