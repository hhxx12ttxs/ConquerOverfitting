public static int indexOfAny(String str, char[] searchChars) {
if (isEmpty(str) || ArrayUtils.isEmpty(searchChars)) {
return INDEX_NOT_FOUND;
char ch = str.charAt(i);
for (int j = 0; j < searchLen; j++) {
if (searchChars[j] == ch) {
if (i < csLast &amp;&amp; j < searchLast &amp;&amp; CharUtils.isHighSurrogate(ch)) {

