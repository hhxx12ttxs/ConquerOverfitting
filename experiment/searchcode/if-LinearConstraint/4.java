import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.linear.LinearConstraint;

public class LinearConstraintHitFunction implements HitFunction {

private List<LinearConstraint> constraints;

public LinearConstraintHitFunction(List<LinearConstraint> constraints) {

