public static String stripStart(final String str, final String stripChars) {
int strLen;
if (str == null || (strLen = str.length()) == 0) return str;
int start = 0;
public static String stripEnd(final String str, final String stripChars) {
int end;
if (str == null || (end = str.length()) == 0) return str;

