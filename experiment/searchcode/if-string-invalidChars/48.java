while (name.indexOf(&quot;  &quot;) >= 0) {
name = name.replaceAll(&quot;  &quot;, &quot; &quot;);
}
String[] invalidChars = {&quot; &quot;, &quot;-&quot;, &quot;.&quot;, &quot;+&quot;, &quot;*&quot;, &quot;[&quot;, &quot;]&quot;};
for (String invalidChar : invalidChars) {
name = StringUtils.replace(name, invalidChar, &quot;&quot;);

