public static boolean contains(final CharSequence cs, final char... searchChars) {
if (empty(cs) || searchChars == null || searchChars.length == 0) {
final char ch = cs.charAt(i);
for (int j = 0; j < searchLength; j++) {
if (searchChars[j] == ch) {

