String strLine = null;

if(mode==0)
{
LINES_TO_WRITE=&quot;Point\t1\t2\t3\t4\t5\t6\t7\t8\t9\tRight\tLeft\tUp\tDown\t\r\n&quot;;
while((strLine = br.readLine()) != null)
{
tempString = &quot;&quot;;

if(!strLine.contains(&quot;####&quot;) &amp;&amp; !(strLine.length() == 0))

