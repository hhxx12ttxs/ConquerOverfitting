final double mij = matrix.getEntry(i, j);
final double mji = matrix.getEntry(j, i);
if (Math.abs(mij - mji) > (Math.max(Math.abs(mij), Math.abs(mji)) * eps)) {
final double g = work[sixI] + d[i] * work[sixI + 9] / work[sixI + 10];
final double absG = Math.abs(g);
if (absG < minG) {

