public static final int MAX_ITERATIONS = 50;

public int getNumIterations(Complex z0) {
return this.getNumIterations(z0, MAX_ITERATIONS, DEFAULT_BOUND);
}

public int getNumIterations(Complex z0, int maxIterations, double bound) {

