return value;
}

StringBuffer newValue = null;

for (int i = 0; i < value.length(); i++) {
if (c != &#39; &#39; &amp;&amp; Character.isWhitespace(c)) {
if (newValue == null) {
newValue = new StringBuffer(value.substring(0, i));

