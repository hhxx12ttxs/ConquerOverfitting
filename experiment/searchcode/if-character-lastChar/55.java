stack.push(s.charAt(i));
} else {
if (stack.isEmpty()) return false;
char lastChar = stack.peek();
if (s.charAt(i)==&#39;)&#39; &amp;&amp; lastChar==&#39;(&#39; ||
s.charAt(i)==&#39;]&#39; &amp;&amp; lastChar==&#39;[&#39; ||

