System.out.println(&quot;N&quot;+&quot;\t&quot;+&quot;Average hop&quot;);
double chisquare=0.0;
while(scan.hasNext())
{
ListSeries HMeanSeries = new ListSeries();
double chi =(stats.meanX - 2*a)/stats.stddevX;
if(stats.stddevX!=0){
System.out.printf (&quot;chi steps   = %.5f%n&quot;, chi);
chisquare += chi*chi;

