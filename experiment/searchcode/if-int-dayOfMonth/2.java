int dayOfWeek = 0;
for(int i = 1; i <= 365; i++)
{
int dayOfMonth = i;
String month = &quot;&quot;;

if (i >= 1 &amp;&amp; i <= 31)
System.out.println(month + &quot; &quot; + i + &quot; is a &quot; + weekdays[dayOfWeek]);
}
if (i >= 32 &amp;&amp; i <= 59)
{
month = &quot;February&quot;;
dayOfMonth = dayOfMonth - 31;

