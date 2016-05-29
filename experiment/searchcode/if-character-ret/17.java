Character []rTable(Character []t) {
Character[] ret = new Character[26];
for (int i = 0; i < 26; i++) {
if (t[i] != null) {
ret[t[i] - &#39;A&#39;] = (char)(i + &#39;A&#39;);

