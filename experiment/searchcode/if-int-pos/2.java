int len = A.length;

if (len <= 2) {
return len;
}

int pos = 1;
int save = A[pos];

for (int i = 2; i < len; i++) {
if (i - 2 != pos) {

