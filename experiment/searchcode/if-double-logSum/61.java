gamma[i][t] = (alpha[i][t] + beta[i][t]) - logsum(ab);
}
}

// Summing gamma over all states for each time t
double[] sumGammaoverN = new double[T];
for (int i = 0; i < N; i++) {
sumGammaoverTT[i] = logsum(gamma[i]);
}

//Calculating Xi
double[][][] xiNum = new double[N][N][T - 1]; // numerator for calculating Xi

