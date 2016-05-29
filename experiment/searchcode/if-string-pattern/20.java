class CharPatternGenerator extends PatternGenerator {
private String pattern;
private PatternGenerator next;
private static char[] REGEX_CHARS = new char[] {
&#39;$&#39;, &#39;(&#39;, &#39;)&#39;, &#39;+&#39;, &#39;.&#39;, &#39;\\&#39;, &#39;]&#39;, &#39;^&#39;, &#39;{&#39;, &#39;|&#39;, &#39;}&#39;, };

public CharPatternGenerator(char pattern, PatternGenerator next) {
if (binarySearch(REGEX_CHARS, pattern) >= 0)

