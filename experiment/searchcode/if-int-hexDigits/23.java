private static final char[] hexdigits = {&#39;0&#39;, &#39;1&#39;, &#39;2&#39;, &#39;3&#39;, &#39;4&#39;, &#39;5&#39;,
&#39;6&#39;, &#39;7&#39;, &#39;8&#39;, &#39;9&#39;, &#39;A&#39;, &#39;B&#39;, &#39;C&#39;, &#39;D&#39;, &#39;E&#39;, &#39;F&#39;};

public static String intToHex2(int n) {
return &quot;&quot; + hexdigits[(n &amp; 0xf0) >> 4]
+ hexdigits[n &amp; 0xf];
}

public static String intToHex4(int n) {

