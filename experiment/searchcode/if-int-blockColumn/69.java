s.setColor(colorScale * 0, colorScale * 143, colorScale * 70, 0);
}

return s;
}

public void receiveTouch(int gridX, int gridY, int player) {
if (mToggleDelay <= 0 &amp;&amp; gridX < SQUARES_PER_ROW &amp;&amp; gridY < NUMBER_OF_ROWS) {
this.blockRow(currentPlayer);
} else if (gridX == mSquareTouchX) {
this.blockColumn(currentPlayer);

