import static java.lang.Math.*;


public class PhiFourth extends Dynamics1D {
public double[] field, scratch;
int N_dx;
double eps, beta;
N_dx = N / dx;

field = new double[N_dx];
scratch = new double[N_dx];

for (int i = 0; i < N_dx; i++)

