List<Character> parentheses = new ArrayList<Character>();
for (int i = 0; i < s.length(); i++) {
char ch = s.charAt(i);
if (ch == &#39;(&#39; || ch == &#39;[&#39; || ch == &#39;{&#39;) {
parentheses.add(ch);

