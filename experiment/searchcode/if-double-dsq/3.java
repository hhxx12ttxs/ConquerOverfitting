public static double descr_dist_sq( SIFTFeature f1, SIFTFeature f2 )
{
double diff, dsq = 0;
double descr1[], descr2[];
int i, d;

d = f1.d;
if( f2.d != d )
return Double.MAX_VALUE;
descr1 = f1.descr;
descr2 = f2.descr;

