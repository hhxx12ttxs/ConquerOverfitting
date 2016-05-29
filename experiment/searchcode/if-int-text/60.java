public int count(P[] pattern, P[] text) {

int[] lsp = computeLSP(pattern);
int matches = 0;

int j = 0;
for (int i = 0; i < text.length; i++) {
while (j > 0 &amp;&amp; text[i] != pattern[j]) {

