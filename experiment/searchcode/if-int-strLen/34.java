int i, j, cnt;
int lastIndex = s.lastIndexOf(&#39; &#39;);
int strLen = s.length();
if (lastIndex != strLen - 1) {
return strLen - lastIndex - 1;
} else {
for (i = strLen - 2; i >= 0; i--) {
if (s.charAt(i) != &#39; &#39;) break;

