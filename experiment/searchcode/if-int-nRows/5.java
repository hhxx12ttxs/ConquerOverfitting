public String convert(String s, int nRows) {
if (nRows <= 1) return s;
if (s.length() < nRows) return s;
for (int i = 0; i < nRows; i++) {
for (int j = i; j < s.length(); j += 2 * (nRows - 1)) {
result.append(s.charAt(j));

