int n = a.length;
long[][] ret = new long[n][n];
for (int i = 0; i < n; i++) {
for (int j = 0; j < n; j++) {
ret[i][j] += a[i][k] * b[k][j];
}
}
}
return ret;
}

static long[][] pow(long[][] a, long b) {

