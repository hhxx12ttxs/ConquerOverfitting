throw new SingularMatrixException();
}

final int m = realEigenvalues.length;
if (b.length != m) {
throw new IllegalArgumentException(&quot;constant vector has wrong length&quot;);
public boolean isNonSingular() {
for (int i = 0; i < realEigenvalues.length; ++i) {
if ((realEigenvalues[i] == 0) &amp;&amp; (imagEigenvalues[i] == 0)) {

