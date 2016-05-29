* @throws NoDataException
*             if {@code x} or {@code y} are zero-length.
*/
public double mannWhitneyU(final double[] x, final double[] y) {
final double[] z = concatenateSamples(x, y);
final double[] ranks = naturalRanking.rank(z);

double sumRankX = 0;

/*
* The ranks for x is in the first x.length entries in ranks because x

