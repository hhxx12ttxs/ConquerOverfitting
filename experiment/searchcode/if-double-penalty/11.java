See: http://en.wikipedia.org/wiki/Logistic_function
*/

public static double logisticPenalty( double value,
double maxPenalty ) {

if( value < minPenaltyAt )
return 0;
if( value > maxPenaltyAt )
return maxPenalty;

double midPoint = (minPenaltyAt + maxPenaltyAt) / 2;

