int intVersion = -100;
try {
if (StringUtils.isBlank(path)) {
return intVersion;
}

path = path.replaceAll(&quot;\\\\&quot;, &quot;/&quot;);
if (!StringUtils.isBlank(version) &amp;&amp; version.length() >= 3) {
intVersion = Integer.valueOf(version.substring(version.length() - 3, version.length()));

