public class Solution {
public int trap(int[] A) {
int len = A.length;
if (A.length < 2)
return 0;
int np = 0;
int ans = 0;
int[] maxL = new int[len];

