char[] searchChars = str2.toCharArray();
if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
char ch = str.charAt(i);
for (int j = 0; j < searchChars.length; j++) {
if (searchChars[j] == ch) {

