String[] ts = from.split(&quot;&quot;);
String finalContent = &quot;&quot;;
for (int i = 0; i < ts.length; i++) {
String t = ts[i];
if (t.matches(&quot;\\p{P}+&quot;) &amp;&amp; i + 1 < ts.length) {//如果是标点，且不是最后一个字

