return climbStairsDp(0, new int[n+2], n);
}

private int climbStairsDp(int step, int[] cache, int n)
{
if(step == n)
return 0;
}

int step_plus_1, step_plus_2;

if(cache[step + 1] == 0)
{
cache[step+1] = climbStairsDp(step+1, cache, n);

