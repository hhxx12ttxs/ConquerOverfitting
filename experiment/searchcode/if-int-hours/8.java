// TODO Auto-generated method stub

//Program transforms time from military format to AM/PM format

int hours = 1;
int minutes = 1;

if (hours > 0 &amp;&amp; hours < 1) {
hours += 12;
System.out.printf(&quot;%d:%d = %d.%d AM&quot;, hours, minutes , hours, minutes);

