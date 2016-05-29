
public class MatrixNormalize
{
public static double maxConvergenceError = .0001;

//if normalizeRows is true, rows are normalizer, otherwise columns are normalized
double normalizer;
int independentDim, dependentDim;

if (normalizeRows)
{
independentDim = someMatrix.length;

