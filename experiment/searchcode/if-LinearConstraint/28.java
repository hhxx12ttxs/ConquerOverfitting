import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 3, 3}, 0);
ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
constraints.add(new LinearConstraint(new double[] {1, 2, -1}, Relationship.LEQ, 8));

