size = data.length;
}

double getMean()
{
double sum = 0.0;
for(double a : data)
sum += a;
return sum/size;
}

double getVariance()
{
double mean = getMean();

