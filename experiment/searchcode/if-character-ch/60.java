private static boolean isAllowed(char ch) {
ch = Character.toLowerCase(ch);
return (ch == &#39;a&#39; || ch == &#39;b&#39; || ch == &#39;c&#39; || ch == &#39;d&#39; || ch == &#39;e&#39;
char ch = buffer[i];
if (isSeparator(ch)) {
if (wordPos >= 3) {
words.add(new Word(new String(word, 0, wordPos)

