double average = sum/N;

double sumsq = 0.0;
for (int i = 0; i < N; i++) {
double dif = average - list[i];
double difsq = dif * dif;
sumsq += difsq;

