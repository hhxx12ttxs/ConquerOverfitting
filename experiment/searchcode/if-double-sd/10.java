* @param int, 0=mean and 1=sd
* @return mean or sd
*/

public double calculateMeanOrSD(double val[], int size, int whatToCalculate)
{
if(size ==1)
return val[0];

double mean =0;
double sd =0;

