int year = 2000;
if (year % 400 == 0 || ((year % 4 == 0) &amp;&amp; (year % 4 != 0)))
System.out.println(year + &quot; is a leap year.&quot;);
System.out.println(year + &quot;is NOT a leap year.&quot;);

year = 2004;
if (year % 400 == 0 || ((year % 4 == 0) &amp;&amp; (year % 100 != 0)))

