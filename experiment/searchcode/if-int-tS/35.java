public class TempAnalyze {

double closestToZero(double[] ts){

if (ts.length == 0)
{
return 0;
}
double minValue=ts[0];

for (int i=1; i<ts.length; i++)

