for (int i : nums) {
s ^= i;
}
int tmp = s;
int k = 0;
while (((tmp >> k) &amp; 1) == 0) {
k++;
}
for (int i : nums) {

