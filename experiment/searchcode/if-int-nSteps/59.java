* @throws Exception
*/
double estimateMarginalLikelihood(int nSteps, double alpha, String rootDir, int burnInPercentage) throws Exception {
String sFormat = &quot;&quot;;
for (int i = nSteps; i > 0; i /= 10) {
sFormat += &quot;#&quot;;
}
formatter = new DecimalFormat(sFormat);

