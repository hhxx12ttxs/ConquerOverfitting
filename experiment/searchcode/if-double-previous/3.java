private double[][] previousIteration;

private int width = 0;
private int height = 0;

public Jacobi(double[][] matrix, int iterations) {
result = new double[height][width];
previousIteration = new double[height][width];
solve(matrix, iterations);

