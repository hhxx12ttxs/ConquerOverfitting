private LinearObjectiveFunction function;
/**
* Linear constraints.
*/
private Collection<LinearConstraint> linearConstraints;
// not provided in the argument list.
for (OptimizationData data : optData) {
if (data instanceof LinearObjectiveFunction) {

