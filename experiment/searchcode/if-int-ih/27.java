* @see com.frank.dip.threshold.ThresholdFinder#threshold(com.frank.dip.GrayImage)
*/
@Override
public int threshold(GrayImage image)
data[pixel]++;
int threshold;
int ih, it;
double crit;
double max_crit;
double[] norm_histo = new double[256]; /* normalized histogram */

