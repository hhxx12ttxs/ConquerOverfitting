ArrayList<CheckData> checkDatas=null;
if(lb==0&amp;&amp;xb==0&amp;&amp;id==0&amp;&amp;dcdw==&quot;&quot;)
{
try
{
checkDatas=CheckData.getCheckDatas(&quot;where cut=0 and checked=0&quot;);
e.printStackTrace();
}
}
else
{
String where=&quot;&quot;;
if(lb!=0&amp;&amp;xb!=0)
where+=&quot; where lb=&quot;+lb+&quot; and xb=&quot;+xb+&quot; and checked=0 and cut=0&quot;;

