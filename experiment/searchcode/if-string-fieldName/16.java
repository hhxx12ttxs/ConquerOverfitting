public static AbstractAutoCompleter getFor(String fieldName) {
AbstractAutoCompleter result;
if (fieldName.equals(&quot;author&quot;) || fieldName.equals(&quot;editor&quot;)) {
} else if (fieldName.equals(&quot;crossref&quot;)) {
result = new CrossrefAutoCompleter(fieldName);
} else if (fieldName.equals(&quot;journal&quot;) || fieldName.equals(&quot;publisher&quot;)) {

