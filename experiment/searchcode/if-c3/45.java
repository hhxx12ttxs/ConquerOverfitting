public static void main(String []arg)
{
for(COLOR c1:COLOR.values())
for(COLOR c2:COLOR.values())
for(COLOR c3:COLOR.values())
{
if(c1!=c2&amp;&amp;c1!=c3&amp;&amp;c2!=c3)
{
System.out.println(c1+&quot;,&quot;+c2+&quot;,&quot;+c3);
}
}
}
}

