double maxRmsd = Double.MIN_VALUE;

double totalSumTm = 0;
double totalSumDsq = 0;
double totalLength = 0;

Point3d t = new Point3d();
double d0Sq = d0 * d0;

double sumTm = 0;
double sumDsq = 0;
for (int j = 0; j < orig.length; j++) {

