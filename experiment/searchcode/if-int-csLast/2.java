return INDEX_NOT_FOUND;
}
int csLen = str.length();                    <2>
int csLast = csLen - 1;
for (int j = 0; j < searchLen; j++) {    <4>
if (searchChars[j] == ch) {          <5>
if (i < csLast &amp;&amp; j < searchLast &amp;&amp; CharUtils.isHighSurrogate(ch)) {

