int csLast = csLen - 1;
int searchLen = searchChars.length;
int searchLast = searchLen - 1;
for (int i = 0; i < csLen; i++) {            <3>
char ch = str.charAt(i);
for (int j = 0; j < searchLen; j++) {    <4>

