package fml;

class Hypothesis{
double threshold;
int sign;
int dimension;
double alpha;
this.alpha = alpha;
}

public int label(double[] x){
if (x[dimension] < threshold)
return sign;
else

