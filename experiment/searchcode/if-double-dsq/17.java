static final public double getLoweFactor_avg(ALocalFeaturesGroup<SIFTPCAFloat> sg1,
ALocalFeaturesGroup<SIFTPCAFloat> sg2) {
if (sg2.size() < 2)
return 0;
double sum = 0;
double distsq1 = Integer.MAX_VALUE;
double distsq2 = Integer.MAX_VALUE;
double dsq = 0;
SIFTPCAFloat curr, best = null;

