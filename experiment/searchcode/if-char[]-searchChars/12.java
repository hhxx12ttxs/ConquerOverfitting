public static int indexOfAny(String str, char[] searchChars) {
if (isEmpty(str) || ArrayUtils.isEmpty(searchChars)) { <1>
return INDEX_NOT_FOUND;
if (i < csLast &amp;&amp; j < searchLast &amp;&amp; CharUtils.isHighSurrogate(ch)) {
if (searchChars[j + 1] == str.charAt(i + 1)) {

