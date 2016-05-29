private long solve(int dir, int c0, int c1, int c2, int c3, int c4) {
if (c0 == 0 &amp;&amp; c1 == 0 &amp;&amp; c2 == 0 &amp;&amp; c3 == 0 &amp;&amp; c4 == 0) {
return 1L;
}
if (cache[dir][c0][c1][c2][c3][c4] != null) {

