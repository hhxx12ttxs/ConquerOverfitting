/* calculates age average and overdispersion
*/
static double[] solveMuXi(ArrayList<double[]> tst) throws Exception {
double[] MuXi = new double[2];
double xi = 0d, dxi;
double[] fdf = getfdf(tst,mu,xi);
// exit with zero if the first iteration sends you to negative xi

