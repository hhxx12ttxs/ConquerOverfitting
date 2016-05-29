double mu = mean(vals);
double var = 0.0;
double residual = 0.0;
for (int i = 0; i < vals.length; ++i) {
residual = vals[i] - mu;
var += residual * residual;
}
return var/vals.length;
}

public static double stdev(double[] vals) {

