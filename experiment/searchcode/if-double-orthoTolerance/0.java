private double parTolerance = 1E-6;				/** Desired relative error in the approximate solution parameters. */
private double orthoTolerance = 1E-4;			/** Desired max orthogonality between the function vector and the columns of the jacobian. */
double[] params = problem.getParameterValues();
double[] data = problem.getTargets();
if (params == null || data == null)

