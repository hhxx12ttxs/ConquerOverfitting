private void placeTop(char[] boardElements, int position, int dimension) {
if (position - dimension * 2 + 1 >= 0) {
if (position % dimension + 1 < dimension)
private void placeLeft(char[] boardElements, int position, int dimension) {
if (position % dimension - 2 >= 0) {

