public static String getMaxOccChar(String s) {
if (s == null || s.length() == 0) {
return null;
} else {
int startIndex = 0;
for (int i = 1; i < s.length(); i++) {
if (s.charAt(i) != s.charAt(startIndex)) {
if (i - startIndex > maxCount) {

