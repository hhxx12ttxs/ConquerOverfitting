public static String getSha1(String str) {
if (str == null || str.length() == 0) {
return null;
}

char hexDigits[] = { &#39;0&#39;, &#39;1&#39;, &#39;2&#39;, &#39;3&#39;, &#39;4&#39;, &#39;5&#39;, &#39;6&#39;, &#39;7&#39;, &#39;8&#39;, &#39;9&#39;,
char buf[] = new char[j * 2];
int k = 0;
for (int i = 0; i < j; i++) {
byte byte0 = md[i];
buf[k++] = hexDigits[byte0 >>> 4 &amp; 0xf];

