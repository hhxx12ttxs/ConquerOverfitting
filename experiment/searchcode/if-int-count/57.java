for (int i = 0; i < n - 1; i++) {
if (A[i] == A[i + 1])
result = result + 1;
}
int r = 0;
for (int i = 0; i < n; i++) {
int count = 0;
if (i > 0) {

