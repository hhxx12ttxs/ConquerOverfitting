public static int indexOfAny(String str, char[] searchChars) {

int csLen = str.length();
int csLast = csLen - 1;
int searchLast = searchLen - 1;
for (int i = 0; i < csLen; i++) {
char ch = str.charAt(i);
for (int j = 0; j < searchLen; j++) {

