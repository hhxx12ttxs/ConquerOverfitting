public static boolean balanceBracket(String s)
{
boolean ret = true;
Stack<Character> stack = new Stack<Character>();
else if(ch == &#39;]&#39; || ch == &#39;)&#39;)
{
Character left = stack.pop();
if(!(left == &#39;[&#39; &amp;&amp; ch == &#39;]&#39; || left == &#39;(&#39; &amp;&amp; ch == &#39;)&#39;))

