public class Solution {
public int trap(int[] A) {
int N = A.length;
if (N == 0)
return 0;

// Max from left, from right:
int MaxL[] = new int[N];

