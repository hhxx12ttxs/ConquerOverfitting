return iter <= maxiter;
}

@Override
public int[] next() {
if (!hasNext()) {
int[] ij = new int[]{i, j};
if ((iter &amp; 1) == 0) {
j += incj;

