import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.util.Precision;
List<LinearConstraint> constraints = new ArrayList<>();
for (OptimizationData data : input)
if(data instanceof LinearConstraint)

