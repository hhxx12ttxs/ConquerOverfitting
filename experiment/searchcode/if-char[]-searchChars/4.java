for (int j = 0; j < searchLen; j++) {
if (searchChars[j] == ch) {
if (i < csLast &amp;&amp; j < searchLast &amp;&amp; CharUtils.isHighSurrogate(ch)) {
if (searchChars[j + 1] == str.charAt(i + 1)) {
return i;
}
} else {

