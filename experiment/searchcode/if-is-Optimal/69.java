while(true){
if (Thread.currentThread().isInterrupted()) return new OptimalSolverStatus(OptimalSolverStatus.INTERRUPTED);
if (Math.abs(a-b)<=e) return new OptimalSolverStatus(OptimalSolverStatus.DONE,(a+b)/2,fun.function((a+b)/2));
if ((Double.isNaN(f1))&amp;&amp;(Double.isNaN(f2))) return new OptimalSolverStatus(OptimalSolverStatus.BADINPUTDATA);

