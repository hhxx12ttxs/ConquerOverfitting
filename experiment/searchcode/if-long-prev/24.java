long divisorl = Math.abs((long)divisor);
long prev = 0;
long curr = 1;

while (curr * divisorl < dividendl) {
while (prev <= curr) {
long mid = (prev + curr) / 2;
long temp = (mid * divisorl);

if (temp == dividendl) {

