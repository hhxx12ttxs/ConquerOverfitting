char firstChar = termBuf[0];
if (firstChar >= &#39;A&#39; &amp;&amp; firstChar < &#39;W&#39;) {
termAtt.setLength(1);
}
else if (firstChar >= &#39;a&#39; &amp;&amp; firstChar < &#39;w&#39;) {
termBuf[0] = Character.toUpperCase(firstChar);

