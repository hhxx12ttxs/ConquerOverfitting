int maxDiff = 0;
for (int i = 0; i < stocks.length; i++) {
if (stocks[i] < stocks[min])
min = i;
int diff = stocks[i] - stocks[min];
if (diff > maxDiff) {

