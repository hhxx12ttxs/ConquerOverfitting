public class LevenbergMarquardt extends OptimizationMethod {

private final double epsfcn_, xtol_, gtol_;
public LevenbergMarquardt(final double epsfcn, final double xtol, final double gtol){
if (System.getProperty(&quot;EXPERIMENTAL&quot;) == null)

