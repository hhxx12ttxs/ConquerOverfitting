// Start typing your Java solution below
// DO NOT write main() function
if (A.length <= 1) return 0;
int len = A.length;
for (int j = 1; j <= A[marker]; j++){
if (j + marker >= len) break;
if (A[marker + j] + marker + j > maxRange){

