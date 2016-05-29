if (s.equals(&quot;Unarmed&quot;)) {
return (mode >= 0 &amp;&amp; mode <= 2);
} else if (s.endsWith(&quot;whip&quot;)) {
return (mode == 0 || mode == 1 || mode == 3);
} else if (s.endsWith(&quot;Scythe&quot;)) {
return (mode >= 0 &amp;&amp; mode <= 3);
} else if (s.endsWith(&quot;bow&quot;) || s.startsWith(&quot;Crystal bow&quot;) || s.startsWith(&quot;Seercull&quot;)) {

