strGb = dG + &quot;&quot;;
} else if (size >= 1024 * 1024 * 1024) {
double dG = size / 1024.0 / 1024 / 1024.0;
strGb = String.format(&quot;%.2f&quot;, dG);// gb
String strGb = &quot;&quot;;
if (size < 1024 * 1024 &amp;&amp; size >= 0) {

double dG = size / 1024.0;
strGb = String.format(&quot;%.2f&quot;, dG);// kb

