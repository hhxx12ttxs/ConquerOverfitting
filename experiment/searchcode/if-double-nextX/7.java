a[i][j] /= -a[i][i];
}
}
f[i] /= a[i][i];
}

double[] currentX = new double[N];
double[] nextX = new double[N];
for (int i = 0; i < N; ++i) {
if (max < Math.abs(currentX[i] - nextX[i])) {
max = Math.abs(currentX[i] - nextX[i]);

