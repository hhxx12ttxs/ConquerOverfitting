public static String joinString(String[] str, String delimiter, int initialIndex) {
if (str.length == 0) {
return &quot;&quot;;
}
StringBuilder buffer = new StringBuilder(str[initialIndex]);
public static String joinString(Object[] str, String delimiter,
int initialIndex) {
if (str.length == 0) {
return &quot;&quot;;
}
StringBuilder buffer = new StringBuilder(str[initialIndex].toString());

