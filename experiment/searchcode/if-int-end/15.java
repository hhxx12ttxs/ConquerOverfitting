public void merge(int A[], int m, int B[], int n) {

int end = n + m;

while (end > 0) {
if (n <= 0) {
while (m > 0)
A[--end] = A[--m];
break;
}
if (m <= 0) {

