for(i=1;;i = i<<1,bit++){
if((p&amp;i) != 0)
break;
}
return bit;
}

private long getSolutions(int[] bits, int i, int c){
if(dp[i][c]!=-1)
return dp[i][c];
if(i == 63)
return 1;
long res = 0;
res += getSolutions(bits, i+1, bits[i+1] + c/2);

