package easy;

public class _276PaintFence {
public int numWays(int n, int k) {
if(n == 0) return 0;
if(n == 1) return k;
int diff = k*(k-1);
int same = k;

