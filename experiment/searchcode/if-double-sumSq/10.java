public static double variance( List<Double> dlist )
{
double n = dlist.size();
if (n <= 1)
{
return 0;
}
double mean = mean(dlist);

double sum = 0;
double sumsq = 0;
for (double d: dlist)
{
sum += (d - mean);

