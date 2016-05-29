* @throws OptimizationException if the maximal iteration count has been
* exceeded or if the model is found not to have a bounded solution
*/
protected void doIteration(final SimplexTableau tableau)
* @return whether Phase 1 is solved
*/
private boolean isPhase1Solved(final SimplexTableau tableau) {
if (tableau.getNumArtificialVariables() == 0) {

