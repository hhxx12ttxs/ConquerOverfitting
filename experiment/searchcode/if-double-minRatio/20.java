double avgError = 0;
double maxError = 0;

double minRatio = 1;
double maxRatio = 0;
double avgRatio = 0;
final double ratio = (double)correspondences / (double)candidates;

if ( ratio <= minRatio )
{
minRatio = ratio;
worstView = view;

