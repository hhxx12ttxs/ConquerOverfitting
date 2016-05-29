normalize = normalize.replaceAll(&quot;\\s+&quot;, &quot; &quot;).trim();
if (normalize.indexOf(&quot;half and half&quot;) > -1
normalize = normalize.replaceAll(&quot;\\s+or\\s+\\w*\\W*\\d*\\D*&quot;, &quot; &quot;).trim();
if (normalize.length() >= 3) {
normalize = normalize.substring(0, normalize.length() - 3) + normalize.substring(normalize.length() - 3, normalize.length()).replace(&quot;ies&quot;, &quot; &quot;);

