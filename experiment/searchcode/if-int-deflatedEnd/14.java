throw new SingularMatrixException();
}

final int m = realEigenvalues.length;
if (b.length != m) {
throw new IllegalArgumentException(&quot;constant vector has wrong length&quot;);
// step 1: accepting realEigenvalues
int deflatedEnd = end;
for (boolean deflating = true; deflating;) {

if (start >= deflatedEnd) {

