* value of the block in the goal board of line i, column j
*/

private int blockValue(int i, int j) {
if (i == N - 1 &amp;&amp; j == N - 1)
return 0;

return i * N + j + 1;
private int blockLine(int v) {
if (v == 0)
return N - 1;
return (v - 1 - blockColumn(v)) / N;

