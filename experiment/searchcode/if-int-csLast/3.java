return INDEX_NOT_FOUND;
}
int csLen = str.length();
int csLast = csLen - 1;
int searchLen = searchChars.length;
char ch = str.charAt(i);
for (int j = 0; j < searchLen; j++) {
if (searchChars[j] == ch) {
if (i < csLast &amp;&amp; j < searchLast &amp;&amp; CharUtils.isHighSurrogate(ch)) {

