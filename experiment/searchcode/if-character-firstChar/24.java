return charHeight;
}

public boolean canDisplay(char ch) {
if (Character.isLowerCase(ch) &amp;&amp; !hasLowercase) {
ch = Character.toUpperCase(ch);
if (Character.isLowerCase(ch) &amp;&amp; !hasLowercase) {
ch = Character.toUpperCase(ch);
}

if (ch >= firstChar &amp;&amp; ch < firstChar + numChars) {

