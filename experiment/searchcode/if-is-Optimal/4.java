double f;
while(x<b){
if (Thread.currentThread().isInterrupted()) return new OptimalSolverStatus(OptimalSolverStatus.INTERRUPTED);
f = fun.function(x);
if (isFind(x,f,value,dv)) return new OptimalSolverStatus(OptimalSolverStatus.DONE,x,f);

