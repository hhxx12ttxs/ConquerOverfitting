char firstChar = p.charAt(0);
if (firstChar != &#39;*&#39; &amp;&amp; firstChar != &#39;?&#39;) {
return (firstChar == s.charAt(0)) &amp;&amp; (isMatch(s.substring(1), p.substring(1)));
}

if (firstChar == &#39;?&#39;) {
return isMatch(s.substring(1), p.substring(1));

