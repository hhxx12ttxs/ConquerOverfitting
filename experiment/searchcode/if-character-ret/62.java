return true;

boolean ret = true;
it = new StringCharacterIterator(input);
c = it.first();
col = 1;
if (!value()) {
ret = error(&quot;value&quot;, 1);
} else {
skipWhiteSpace();
if (c != CharacterIterator.DONE) {

