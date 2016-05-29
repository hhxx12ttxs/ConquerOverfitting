if (Thread.currentThread().isInterrupted()) return new OptimalSolverStatus(OptimalSolverStatus.INTERRUPTED);
isOptimal(x,fun.function(x));
x+=e;
}
isOptimal(b,fun.function(b));
if (Double.isNaN(minF)) return new OptimalSolverStatus(OptimalSolverStatus.NORESOLVE);

