double mean2 = StatUtils.mean(y);

double sumRankX = 0;

/*
* The ranks for x is in the first x.length entries in ranks because x
* U1 = R1 - (n1 * (n1 + 1)) / 2 where R1 is sum of ranks for sample 1,
* e.g. x, n1 is the number of observations in sample 1.
*/
final double U1 = sumRankX - (x.length * (x.length + 1)) / 2;

