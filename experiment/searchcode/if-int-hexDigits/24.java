StringBuilder hex = new StringBuilder();
for (int b : this.value) {
String hexDigits = Integer.toHexString(b).toUpperCase();
if (hexDigits.length() == 1) {
hex.append(&quot;0&quot;);
}
hex.append(hexDigits).append(&quot; &quot;);

