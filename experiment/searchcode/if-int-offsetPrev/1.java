* We append the previous link only if the result is not 1. If it&#39;s 1, there is no previous.
*/
if (rowOffset != 0) {
int offsetPrev = rowOffset - rowLimit;
if (offsetPrev <= 0) {
offsetPrev = 0;

