* @return a solver stopping the calculation after <code>maxIterations<code>
* iterations
*/
public static Solver getNewInstance(int maxIterations) {
return new UCRSolver(maxIterations);
public static Solver getNewInstance(int maxIterations, long timeout) {
// if(logger.isDebugEnabled()) {
// logger.debug(&quot;creating a decorated solver with &quot; + timeout + &quot;ms timeout&quot;);

