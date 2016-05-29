public static String escapeHtml(String source) {
if (StringUtils.isEmpty(source)) {
return &quot;&quot;;
}

return StringEscapeUtils.escapeHtml(source);
}

public static String unescapeHtml(String source) {
if (StringUtils.isEmpty(source)) {

