public int getCount(int[] B, String operators)
{
int n = B.length;
int s = 1;
long t = 0;
final long INF = Long.MAX_VALUE;
for (int i=0; i<=n; i++) {
// value of the last number is
// a0*s + t  > 0
// a0*s > -t
if (s == -1) {

