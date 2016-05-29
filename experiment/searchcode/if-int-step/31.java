
/////////////////// My implementation

int[] steps = new int[n];

int climbStair(int n){
//let step[i] denote the number of ways to climb to (i+1) step
step[2] = step[0] + ste[1] + 1;
if(n<=3) return step[n-1];
for(int i=3; i<n; i++)
step[n] = step[n-3] + step[n-2] + step[n-1];

return step[n-1];
}

