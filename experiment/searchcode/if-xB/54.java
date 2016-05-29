int bxXN = bxGetOffProbability.get(&quot;GZ&quot;) + bxGetOffProbability.get(&quot;CP&quot;) + bxGetOffProbability.get(&quot;WG&quot;) + bxGetOffProbability.get(&quot;XP&quot;) + bxGetOffProbability.get(&quot;XY&quot;) + bxGetOffProbability.get(&quot;XN&quot;);

if (direction.equals(&quot;XB&quot;)) {
int num = random.nextInt(1000);
if (num >= 0 &amp;&amp; num < xbXY)
wantedStation = &quot;XY&quot;;
else if (num >= xbXY &amp;&amp; num < xbXP)

