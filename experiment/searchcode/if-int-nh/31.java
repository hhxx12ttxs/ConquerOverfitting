fname = be.args[0];
}
int dim = NH.NDIM;
if (be.args.length > 1) dim = Integer.parseInt(be.args[1]);
NH.NDIM = dim;
for (int i = 0; i< evals.length; i+=NH.NDIM) {
boolean isDominated = false;
for (int j=i; j<evals.length; j+=NH.NDIM) {
if (evals[i] < evals[j] &amp;&amp; evals[i+1] > evals[j+1]) {

