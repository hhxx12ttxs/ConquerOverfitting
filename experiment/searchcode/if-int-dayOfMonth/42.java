int daysPassed = 2;
int month = 1;
int dayOfMonth = 1;
int year = 1901;
int startingSundays = 0;
while (year < 2001)
{
if (daysPassed%7 == 0 &amp;&amp; dayOfMonth == 1)
{
startingSundays++;
System.out.printf(&quot;%d/%d/%d\n&quot;,month, dayOfMonth, year);

