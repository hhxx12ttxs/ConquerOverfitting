public void setScaleByLimits (double miX, double maX, double miY, double maY)
{
log.dbg (2, &quot;setScaleByLimits&quot;, &quot;   minX maxX minY maxY &quot; + miX + &quot;, &quot; + maX + &quot;, &quot; + miY + &quot;, &quot; + maY);
double r = yppix / xppix; // r = 1/(A*B)

if (r == 1.) return; // it is ok!

double e = (r-1.) / (r+1.);

