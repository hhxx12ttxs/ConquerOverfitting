if (name.length()<4) return null;
char ch4 = name.charAt(3);
if (&#39;A&#39; <= ch4 &amp;&amp; ch4 <= &#39;Z&#39;) return toItemName(ch4,name,3);
if (name.length()<4) return null;
if (!name.startsWith(&quot;set&quot;)) return null;
char ch4 = name.charAt(3);
if (&#39;A&#39; <= ch4 &amp;&amp; ch4 <= &#39;Z&#39;) return toItemName(ch4,name,3);

