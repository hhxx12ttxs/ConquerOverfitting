return INDEX_NOT_FOUND;
}
int csLen = str.length();                    <2>
int csLast = csLen - 1;
for (int i = 0; i < csLen; i++) {            <3>
char ch = str.charAt(i);
for (int j = 0; j < searchLen; j++) {    <4>

