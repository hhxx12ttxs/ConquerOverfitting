public static int indexOfAny(String str, char[] searchChars) {
if(!str.isEmpty() &amp;&amp; searchChars.length > 0) {
int csLen = str.length();
int csLast = csLen - 1;
int searchLen = searchChars.length;
int searchLast = searchLen - 1;

