if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) return INDEX_NOT_FOUND;
final int csLen = cs.length();
final int csLast = csLen - 1;
for (int j = 0; j < searchLen; j++) {
if (searchChars[j] == ch) {
if (i < csLast &amp;&amp; j < searchLast &amp;&amp; Character.isHighSurrogate(ch)) {

