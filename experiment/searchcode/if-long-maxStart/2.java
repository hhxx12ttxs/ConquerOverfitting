public static void main (String[]args) {
int max = 0;
long maxStart = 0;
for (long i = 1000000; i >= 1; i--) {
int count = 1;
long j = i;
while (j != 1) {
count++;
if (j % 2 == 0) {
j /= 2;
}
else {

