public static long F(long prev, long sd)
{
long sum =0;
int i =0;
if(prev == 0) i++;
for(;i<=9;i++)
{
long curr = Long.parseLong(&quot;&quot; + prev +&quot;&quot; +i);
if(curr >= max)return sum;
if(isPrime(curr))
{

if( curr > 10 &amp;&amp; isPrime(prev/sd))

