public int[] merge (int[] A, int[] B){
int len = A.length + B.length;
int[] C = new int[len];
int countC = 0;
int countA = 0;
int countB = 0;
while (countC < len){
if (countA == A.length){
C[countC] = B[countB];

