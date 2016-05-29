public abstract class Integrator {

//
// private fields
//

private double absoluteAccuracy;
private int maxEvaluations;
// public constructors
//

public Integrator(final double absoluteAccuracy, final int maxEvaluations) {
QL.require(absoluteAccuracy > Constants.QL_EPSILON , &quot;required tolerance must be > epsilon&quot;); // TODO: message

