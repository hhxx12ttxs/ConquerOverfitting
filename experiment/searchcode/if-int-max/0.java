int max(int x, int y, int z)
{
int max = x;
max = y > max ? y : max;
max = z > max ? z : max;
return max;
}



int max(int x, int y, int z)
{
if ((x > y) &amp;&amp; (x > z))

