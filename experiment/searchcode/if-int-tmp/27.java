public int mySqrt(int x) {
if (x <= 0)
return 0;
int i = 1;
while (x / i >= i) {
while (i > 0) {
int tmp = result + i;
if (x / tmp == tmp)
return tmp;

