int ex = numberOfExtrema(series);
if (ex >= ubound) return false;
int i = getFirstMissing(series, 0);
if (i >= 0) {
series[i] = -1;
if (re) return true;
}
} else {
int re = numberOfExtrema(series);

