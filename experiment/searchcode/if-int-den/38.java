vect[i][j] = new frac(nextInt(), 1);

int max;
frac tmp;
long num, den;
frac buf = new frac(1, 1);
vect[i][j].mul(num, den);

for (int p = i + 1; p < n; p++) {
if (vect[p][i].num == 0)

