if(to != null &amp;&amp; to.isPlayable(this, direction)){
if(to.isBrick())to.move(nCol, nRow);
}
col += nCol;
row += nRow;
int[] directionChange = iceTurn(nCol, nRow, to);
if (in().isIce())move(directionChange[0], directionChange[1]);
}
else if (to.isIce() &amp;&amp; !hasItem(&#39;i&#39;)){
col += nCol;
row += nRow;
int[] directionChange = iceTurn(nCol, nRow, to);

