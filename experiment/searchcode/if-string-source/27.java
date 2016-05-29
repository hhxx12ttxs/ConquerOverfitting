public class StringUtils {
public String changeToJavaPath(String source) {
return source.replaceAll(&quot;\\\\&quot;, &quot;/&quot;);
}
public String deleteLastSlash(String source) {
if (source.substring(source.length() - 1, source.length()).equals(&quot;/&quot;) || source.substring(source.length() - 1, source.length()).equals(&quot;\\&quot;)) {

