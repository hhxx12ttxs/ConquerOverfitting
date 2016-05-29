public static String convert(String s, int nRows) {
StringBuffer res = new StringBuffer();
if (s == null || s.length() <= nRows || nRows <= 1) {
for (int j = i; j < s.length(); j += (nRows - 1) * 2) {
res.append(s.charAt(j));
if (j + (nRows - i - 1) * 2 < s.length()) {

