final int width, int ox) {
String breakableCharacters = &quot; \t:\\/-&quot;;
if (width < 1) {
throw new IllegalArgumentException(&quot;Width must be positive&quot;); //$NON-NLS-1$
int newlinePos = text.indexOf(&#39;\n&#39;, pos);
int breakableCharPos = indexOfAny(text, breakableCharacters, pos);
if (newlinePos != -1

