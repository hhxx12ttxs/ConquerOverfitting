public static long slove(int x,long start,long end)
{
if(start+1==end)
{
if(start*start <= x &amp;&amp; end*end> x) return start;
}
long temp = (long)((start+end)*(start+end)/4);
if(temp<x) return slove(x,(start+end)/2,end);

