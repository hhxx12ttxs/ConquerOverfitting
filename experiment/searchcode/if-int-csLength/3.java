public static String replace(final String text, final String searchString,
final String replacement, int max) {
if (empty(text) || empty(searchString) || replacement == null
return text;
}
int start = 0;
int end = text.indexOf(searchString, start);
if (end == -1) {
return text;

