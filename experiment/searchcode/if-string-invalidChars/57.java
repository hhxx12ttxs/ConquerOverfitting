public String containsInvalidChar(String text) {
String invalidChars[] = { &quot;?&quot;, &quot;\&quot;&quot;, &quot;<&quot;, &quot;>&quot;, &quot;\\&quot;, &quot;|&quot;, &quot;:&quot;, &quot;*&quot;, &quot;#&quot;, &quot;/&quot; };

for (int i = 0; i < invalidChars.length; i++) {
if (text.indexOf(invalidChars[i]) != -1) {

