for (int i = 0; i < message.length(); i++) {
char ch = message.charAt(i);
if (Character.isUpperCase(ch)) {
result += (char) ((ch - &#39;A&#39; + 13) % 26 + &#39;A&#39;);
} else if (Character.isLowerCase(ch)) {

