int nrow = n, ncol = m;

for (int j = 0; j < x; j++) {
row = rotateClockwise(row, nrow);
ncol = tmp;
}

if (y > 0) {
row = rotateHorizontally(row, ncol);
col = rotateHorizontally(col, ncol);

