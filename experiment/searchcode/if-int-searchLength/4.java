final int wordLength = word.length();
int lenDiff = Math.abs(wordLength-searchLength);
if(closestDistance == WORD_SIZE || lenDiff <= closestDistance+1) {
int dist = distance(word, wordLength, searchWord, searchLength);
if(dist < this.closestDistance) {
this.closestDistance = dist;

