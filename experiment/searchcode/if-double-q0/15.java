public Quaternion(double [] p) {
if (p.length != 4) throw new RuntimeException(&quot;Length of the double array should be 4&quot;);
q0 = p[0];
public boolean isIllegal() {
if (Double.isInfinite(q0) || Double.isNaN(q0)) return true;
if (Double.isInfinite(q1) || Double.isNaN(q1)) return true;

