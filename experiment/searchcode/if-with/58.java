public static Object evaluateVariableExpression(final String with) {
final Object withValue;
if ( with.startsWith(&quot;${&quot;) ) {
final String navigateExpression = stripVariablePlaceholder(with);
public static String preprocessNavigationExpression(String with) {
if ( null == with || 0 >= with.trim().length() ) {
throw new IllegalArgumentException(&quot;Argument with is empty or null.&quot;);

