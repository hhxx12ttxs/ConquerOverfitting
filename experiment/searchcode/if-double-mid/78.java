int i = 0;
int j = x;
while (i <= j) {
int mid = (i+j) >>> 1;
if ((double)mid*mid  > (double)x ) {
j = mid - 1;
} else {

