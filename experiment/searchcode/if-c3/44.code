for (int i = 0; i < s.length(); i++) {
if (s.charAt(i) == &#39;(&#39;) {
c1++;
c1Stack.push(new Status(c1, c2, c3));
}
if (s.charAt(i) == &#39;)&#39;) {
Status temp = c1Stack.pop();
if (temp.c2 != c2 || temp.c3 != c3) {
return false;
}
}
if (s.charAt(i) == &#39;[&#39;) {

