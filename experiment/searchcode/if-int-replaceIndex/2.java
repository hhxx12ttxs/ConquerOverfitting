* @return command name
*/
public static String cutCommandName(String userQuery) {
int replaceIndex = userQuery.indexOf(&#39; &#39;);
if (replaceIndex == -1) {
replaceIndex = userQuery.length();

