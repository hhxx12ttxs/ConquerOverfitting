class LCS {
public int lcs (int[] A) {
if (A == null) {
return 0;
}
int len = A.length;
if (len <= 1) {
return len;
}
int[] f = new int[len];
f[0] = 1;
for (int i = 1; i < len; i++) {

