public double[] evaluate() throws HasNoRealSolutionExeption {
double D = b*b - 4 * a * c;

if(D < 0) {
throw new HasNoRealSolutionExeption();
}

double[] roots = new double[2];
double sqD = Math.sqrt(D);
roots[0] = (-b + sqD) / (2*a);

