public static double[] embedded_estimate(double h, double[] xold, double[] xnew, double[] xe, double[] atol, double[] rtol, double p, double aMax, double aMin, double alpha)
double epsilon;
double hNew;

// calculations

StdMet.tau(tau, xold, xnew, atol, rtol);   // get the array of

