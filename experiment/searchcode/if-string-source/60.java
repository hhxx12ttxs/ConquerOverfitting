public static String[] split(String source, String delim) {
String[] wordLists;
if (source == null) {
wordLists = new String[1];
wordLists[0] = source;
return wordLists;
}
if (delim == null) {
delim = &quot;,&quot;;
}
StringTokenizer st = new StringTokenizer(source, delim);

