if (Character.isUpperCase(ch)) {
result += (char) ((ch - &#39;A&#39; + 13) % 26 + &#39;A&#39;);
} else if (Character.isLowerCase(ch)) {
result += (char) ((ch - &#39;a&#39; + 13) % 26 + &#39;a&#39;);
} else if (Character.isDigit(ch)) {

