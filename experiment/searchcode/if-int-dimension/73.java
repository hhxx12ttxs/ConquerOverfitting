private int countNeighbours(int x, int y) {
int numOfNeighbours = 0;
if (0 <= x - 1 &amp;&amp; x - 1 < dimension &amp;&amp; 0 <= y - 1 &amp;&amp; y - 1 < dimension
for (int i = 0; i < dimension; i++) {
for (int j = 0; j < dimension; j++) {
if (feld[i][j].getZustand()) {

