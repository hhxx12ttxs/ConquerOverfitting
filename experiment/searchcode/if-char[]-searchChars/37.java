if (search.length() > text.length()) return -1;


char[] textChars = text.toCharArray();
char[] searchChars = search.toCharArray();

for (int i = 0; i < textChars.length - searchChars.length + 1; i++) {

