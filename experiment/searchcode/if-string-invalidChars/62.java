public char[] increment(char[] string, int index) {
string[index] = (char) ((string[index] + 1 - &#39;a&#39;) % (&#39;z&#39; - &#39;a&#39; + 1) + &#39;a&#39;);
if (string[index] == &#39;a&#39; &amp;&amp; index != 0) {
public boolean containsOneOfCharacters(char[] string, List<Character> characters) {
for (char c : string) {
if (characters.contains(c)) {

