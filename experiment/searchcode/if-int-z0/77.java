new PatternValidator(&quot;^[-a-z0-9!#$%&amp;&#39;*+/=?^_`{|}~]+(?:\\.[-a-z0-9!#$%&amp;&#39;*+/=?^_`{|}~]+)*@(?:[a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\\.)*(?:aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])$&quot;) {
return &quot;emailValidator&quot;;
}
};


public static String truncate(String str, int maxLen) {
if (str != null &amp;&amp; str.length() > maxLen) {

