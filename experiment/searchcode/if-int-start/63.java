public class JumpGameII {
public int jump(int[] A) {
int n = A.length;
if (n == 1)
return 0;
count++;
for (int i = start; i <= end; i++) {
if (A[i] + i >= n - 1)
return count;
if (A[i] + i > max)

