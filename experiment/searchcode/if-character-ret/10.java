skipWhiteSpace();
if (c != CharacterIterator.DONE) {
ret = false;
boolean ret = true;
for (t = ci.next(); t != CharacterIterator.DONE; t = ci.next()) {
if (t != nextCharacter()) {

