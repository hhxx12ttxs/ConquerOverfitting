skipWhiteSpace();
if (c != CharacterIterator.DONE) {
ret = error(&quot;end&quot;, col);
boolean ret = true;
for (t = ci.next(); t != CharacterIterator.DONE; t = ci.next()) {
if (t != nextCharacter()) {

