public double getMax(double []a)
{

double temp=a[0];
for(int i=0 ; i<a.length ; i++)
{
if(a[i]>temp)
public double StandardDeviation(double []a)
{
double avg=getAvg(a);
double sd=0;

for(int i=0 ; i< a.length ; i++)
{
sd=sd+Math.pow(a[i]-avg, 2);

