public void setBlock(byte type, int x, int y, int z) {
if ((x >= 0) &amp;&amp; (y >= 0) &amp;&amp; (z >= 0) &amp;&amp; (x < sz) &amp;&amp; (y < sz) &amp;&amp; (z < sz))
blocks[x][y][z] = type;
}
public byte getBlock(int x, int y, int z) {
if ((x >= 0) &amp;&amp; (y >= 0) &amp;&amp; (z >= 0) &amp;&amp; (x < sz) &amp;&amp; (y < sz) &amp;&amp; (z < sz))

