public class ODE1SolverCVode extends ODE1Slave {

// Solver settings
private double rtol, atol; // relative and absolute error tolerance
int idid = jcvode(ctxt.threadInx, ctxt, neqn, x0, xend,
yend, ydot, rtol, atol,
maxsteps, stiff, callbacks());
if (idid < 0) throw new Xcept(

