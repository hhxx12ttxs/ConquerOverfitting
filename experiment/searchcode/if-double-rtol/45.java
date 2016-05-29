// Base class for all ODE algorithms.
protected $double x;
protected double xold; // Used for dense output.
protected final double[] y, dydx;
protected double atol, rtol;
protected boolean dense;

