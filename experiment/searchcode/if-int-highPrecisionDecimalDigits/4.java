char[] buf = new char[a.length()];

for (int i = 0; i < buf.length; i++) {
buf[i] = a.charAt(i);

if (buf[i] >= &#39;1&#39; &amp;&amp; buf[i] <= &#39;9&#39;) {
result[0] = new Dfp(this, new String(buf, 0, sp));

for (int i = 0; i < buf.length; i++) {
buf[i] = a.charAt(i);
if (buf[i] >= &#39;0&#39; &amp;&amp; buf[i] <= &#39;9&#39; &amp;&amp; i < sp) {

