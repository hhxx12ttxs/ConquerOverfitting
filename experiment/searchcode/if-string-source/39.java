private StringUtil() {
// utility class
}

public static String toLowerFirstChar(String source) {
if(source == null) {
return null;
}

if(source.length() == 1) {
return source.toLowerCase();
} else {

