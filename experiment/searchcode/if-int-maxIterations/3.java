public class JacobiMethod implements LinearEquationSolver {
int dimension;
int maxIterations;

public JacobiMethod(int dimension, int maxIterations) {
Arrays.fill(result, 0);
float[] nextResult = new float[dimension];

for(int a = 0; a < maxIterations; a++) {
for(int i = 0; i < dimension; i++) {

