private final int maxIterations;
/** Limit for fitness value. */
private final double stopFitness;
/** Stop if x-changes larger stopTolUpX. */
final RealMatrix arz, final int[] arindex,
final RealMatrix xold) {
double negccov = 0;
if (ccov1 + ccovmu > 0) {
final RealMatrix arpos = bestArx.subtract(repmat(xold, 1, mu))

