private final Log log = LogFactory.getLog(ConjugateGradientOptimizer.class);
private final double minGNormSq = 1.0e-20;
private final double minMotionSq = 1.0e-14;
private static String toString(final double[] x) {
final StringBuilder b = new StringBuilder();
if(null!=x) {
b.append(&quot;[&quot;);

