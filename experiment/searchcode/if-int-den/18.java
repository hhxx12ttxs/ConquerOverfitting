System.out.println(&quot;Number of ways : &quot; + coinChange(value,5));
}

public static int coinChange(int v,int den) {
if (v == 0) return 1;
int next_den = 0;
switch(den) {
case 5 : next_den = 2; break;

