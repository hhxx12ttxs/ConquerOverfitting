CSV_QUOTE, CR, LF };

public static String escapeCsv(String str) {
if (containsNone(str, CSV_SEARCH_CHARS)) {
private static boolean containsNone(String str, char[] invalidChars) {
if (str == null || invalidChars == null) {

