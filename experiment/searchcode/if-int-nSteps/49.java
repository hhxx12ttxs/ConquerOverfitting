protected abstract double simulate(int nSteps, STEP_SIZE step_size, double start_val, double change);
* @return The portfolio with the simulated values.
*/
public Portfolio simulate(int nSteps, STEP_SIZE step_size, Portfolio p, double decimal_change) {

