while(infile.hasNext())
{
s = infile.next();
if(check(s) == true)
System.out.println(s + &quot; is good.&quot;);
char lastChar = stack.pop();

if(((c == &#39;)&#39;) &amp;&amp; (lastChar != &#39;(&#39; )) || ((c == &#39;]&#39;) &amp;&amp; (lastChar != &#39;[&#39;)) || ((c == &#39;}&#39;) &amp;&amp; (lastChar != &#39;{&#39; ) || c == &#39;<&#39; &amp;&amp; lastChar != &#39;>&#39;))

