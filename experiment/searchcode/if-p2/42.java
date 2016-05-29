int len = A.length;
if(len < 2) {
return len;
}

int p1 = 1;
int p2 = 1;
while (p2 < len) {
if (A[p2] != A[p2 - 1]) {
A[p1] = A[p2];

