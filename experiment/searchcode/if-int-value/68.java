// Think from thief picking up max value items perspective
// M[i][0] = 0;
// M[i][j] = M[i - 1][j] if s[i] > j
M[i][j] = Math.max(M[i - 1][j], M[i - 1][j - s[i]] + v[i]);
}
}

int maxValue = 0;
for (int j = 0; j < C; j++) {
if(M[n - 1][j] > maxValue)
maxValue = M[n - 1][j];
}

System.out.println(maxValue);
}
}

