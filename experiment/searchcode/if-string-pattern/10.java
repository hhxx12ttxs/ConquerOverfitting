public SpotlightItemsFilter(String pattern) {
patternMatcher = new SearchPattern();
setPattern(pattern);
}

public void setPattern(String pattern) {
String stringPattern = &quot;&quot;; //$NON-NLS-1$
if (pattern != null &amp;&amp; !pattern.equals(&quot;*&quot;) &amp;&amp; !pattern.isEmpty()) { //$NON-NLS-1$

