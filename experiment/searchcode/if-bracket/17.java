for (int i = 0; i < s.length(); i++) {
if (bracket.empty()) bracket.push(s.charAt(i));
if (s.charAt(i) == &#39;(&#39; || s.charAt(i) == &#39;[&#39; || s.charAt(i) == &#39;{&#39;)
bracket.push(s.charAt(i));
if (s.charAt(i) == &#39;)&#39;) {

